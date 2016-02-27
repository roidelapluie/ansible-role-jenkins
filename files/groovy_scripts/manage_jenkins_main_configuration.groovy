#!/usr/bin/env groovy

import jenkins.model.*
import jenkins.model.ProjectNamingStrategy
import jenkins.model.ProjectNamingStrategy.PatternProjectNamingStrategy \
    as PatternProjectNaming
import hudson.model.*
import groovy.json.*


/**
    Convert Json string to Groovy Object

    @param String arg Json string to parse
    @return Object Groovy object used to get data
*/
def Object parse_data(String arg) {

    try {
        def JsonSlurper jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(arg)
    }
    catch(e) {
        throw new Exception("Parse data error, incoming data : ${arg}, "
                            + "error message : ${e.getMessage()}")
    }
}


/**
    Set the Jenkins number of executors

    @param Jenkins Jenkins singleton
    @param Integer New number of executors
    @return Boolean True if changed, else false
*/
def Boolean set_number_of_executors(Jenkins jenkins_instance,
                                    Integer executors_number) {

    // Get current value, used to check if changed
    def Integer cur_value = jenkins_instance.getNumExecutors()
    if (cur_value == executors_number) {
        return false
    }

    try {
        jenkins_instance.setNumExecutors(executors_number)
    }
    catch(Exception e) {
        throw new Exception('An error occurs during number of executor change')
    }

    return true
}


/**
    Set the new Jenkins mode

    @param Jenkins Jenkins singleton
    @param String New mode
    @return Boolean True if changed, else false
*/
def Boolean set_mode(Jenkins jenkins_instance, String mode) {

    // Get current value, used to check if changed
    def Node.Mode cur_value = jenkins_instance.getMode()
    if (cur_value.getName() == mode) {
        return false
    }

    try {
        def Node.Mode new_mode = Node.Mode.valueOf(mode)
        jenkins_instance.setMode(new_mode)
    }
    catch(Exception e) {
        throw new Exception('An error occurs during mode change')
    }

    return true
}


/**
    Set the new Jenkins project naming strategy

    @param Jenkins Jenkins singleton
    @param String New project naming strategy regex
    @return Boolean True if changed, else false
*/
def Boolean set_project_naming_strategy(Jenkins jenkins_instance,
                                        Map strategy) {

    try {
        def PatternProjectNaming new_value = new PatternProjectNaming(
                                                        strategy.pattern,
                                                        strategy.description,
                                                        strategy.force)
        // Get current value, used to check if changed
        def ProjectNamingStrategy cur_value = jenkins_instance
                                               .getProjectNamingStrategy()

        if (cur_value == new_value
          && cur_value.getNamePattern() == new_value.getNamePattern()
          && cur_value.getDescription() == new_value.getDescription()
          && cur_value.isForceExistingJobs() == new_value.isForceExistingJobs()
        ) {
            return false
        }
        jenkins_instance.setProjectNamingStrategy(new_value)
    }
    catch(Exception e) {
        throw new Exception(
            'An error occurs during project naming strategy change')
    }

    return true
}


/**
    Set the Jenkins quiet period

    @param Jenkins Jenkins singleton
    @param Integer New quiet period
    @return Boolean True if changed, else false
*/
def Boolean set_quiet_period(Jenkins jenkins_instance, Integer quiet_period) {

    // Get current value, used to check if changed
    def Integer cur_value = jenkins_instance.getQuietPeriod()
    if (cur_value == quiet_period) {
        return false
    }

    try {
        jenkins_instance.setQuietPeriod(quiet_period)
    }
    catch(Exception e) {
        throw new Exception('An error occurs during quiet period change')
    }

    return true
}


/* SCRIPT */

try {
    def Jenkins jenkins_instance = Jenkins.getInstance()
    data = parse_data(args[0])

    // Manage configuration with user data
    set_number_of_executors(jenkins_instance, data['number_of_executors'])
    set_mode(jenkins_instance, data['mode'])
    set_project_naming_strategy(jenkins_instance,
                                data['project_naming_strategy'])
    set_quiet_period(jenkins_instance, data['quiet_period'])
}
catch(Exception e) {
    throw new RuntimeException(e.getMessage())
}

// Build json result
result = new JsonBuilder()
result {
    changed false
    output data
}

println result

