package com.thecodesmith.maven.memory.profiler

import javax.management.ObjectName
import javax.management.openmbean.CompositeData
import javax.management.remote.JMXConnectorFactory as JmxFactory
import javax.management.remote.JMXServiceURL as JmxUrl
import javax.management.MBeanServerConnection as JmxServer
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicBoolean

import groovy.util.logging.Slf4j

@Slf4j
class Profiler {
    private int port = 9123
    private String url = "service:jmx:rmi:///jndi/rmi://localhost:$port/jmxrmi"
	private AtomicBoolean status = new AtomicBoolean(false)
	private Thread thread
    private JmxServer connection = null

	boolean isProfiling() {
		status.get()
	}

	void start() {
		log.info 'Starting memory profiler'
		thread = Thread.start {
			status.set(true)
			log.debug 'Profiler running in new thread'
			while (status.get()) {
			    checkMemory()
			    sleep 1000
			}
		}
	}

	void stop() {
		log.info 'Stopping memory profiler'
		status.set(false)
		thread.join()
	}

	protected void checkMemory() {
        try {
            log.debug 'Checking memory usage via JMX'
            def usage = queryMemoryUsage(server)
            def percentage = (usage.used / usage.max * 100.0).round(1)
            log.info "[$timestamp] $usage.used/$usage.max - $percentage %"
        } catch (e) {
            log.debug "JMX connection failed: $e"
        }
	}

    protected Map queryMemoryUsage(JmxServer server) {
        def data = new GroovyMBean(server, 'java.lang:type=Memory').HeapMemoryUsage as CompositeData
        data.contents.collectEntries { k, v ->
            def mb = (v / 1024.0 / 1024.0).round(1)
            [(k): mb]
        }
    }

    protected Map queryGarbageCollector(JmxServer server) {
        def query = new ObjectName("java.lang:type=GarbageCollector,*")
        def data = server.queryNames(query, null)
        def gcs = data.collect {
            ManagementFactory.newPlatformMXBeanProxy(server, it.canonicalName, GarbageCollectorMXBean)
        }

        gcs.each {
            println "$it.name - $it.collectionTime"
        }

        gcs
    }

    protected JmxServer getServer() {
        if (!connection) {
            connection = connect()
        }
        connection
    }

    protected JmxServer connect() {
        log.debug "Connecting to JMX at $url"
        JmxFactory.connect(new JmxUrl(url)).MBeanServerConnection
    }

	protected String getTimestamp() {
	    new Date().format('HH:mm:ss')
	}
}
