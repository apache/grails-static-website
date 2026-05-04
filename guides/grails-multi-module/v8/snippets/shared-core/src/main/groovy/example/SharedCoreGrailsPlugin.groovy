package example

import grails.plugins.Plugin

class SharedCoreGrailsPlugin extends Plugin {

    def profiles = ['web']

    Closure doWithSpring() {
        { ->
            // Optional shared beans go here.
        }
    }

    void doWithDynamicMethods() {
    }

    void doWithApplicationContext() {
    }

    void onChange(Map<String, Object> event) {
    }

    void onConfigChange(Map<String, Object> event) {
    }

    void onShutdown(Map<String, Object> event) {
    }
}
