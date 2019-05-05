package com.thecodesmith.maven.memory.profiler

import spock.lang.Ignore
import spock.lang.Specification

@Ignore('not yet working')
class ProfilerSpec extends Specification {

    def setupSpec() {
        Thread.start {
            new PseudoMavenProcess().run()
        }
    }

    def 'foo'() {
        println 'running test'
        given: sleep 60000
        expect: true
    }

    class PseudoMavenProcess {

        Process parent

        def runTestProcess = '''groovy -e 'println "TESTS listening on ${System.getProperty('com.sun.management.jmxremote.port')}" && sleep 60000" -Dcom.sun.management.jmxremote.port=9876 '''
        def runSurefirePlugin = "['sh', '-c', '$runTestProcess'].execute().waitFor()"

        void run() {
            parent = groovy(runSurefirePlugin)
            println "Maven process started: ${parent.pid()}"
            parent.waitFor()
        }

        Process groovy(String command) {
            ['groovy', '-e', command].execute()
        }
    }
}
