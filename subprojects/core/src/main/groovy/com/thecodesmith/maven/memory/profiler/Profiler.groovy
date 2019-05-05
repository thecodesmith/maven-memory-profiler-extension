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
	private AtomicBoolean status = new AtomicBoolean(false)
	private Thread thread
    private JmxServer connection = null

    String getUrl(Long port) {
        "service:jmx:rmi:///jndi/rmi://localhost:$port/jmxrmi"
    }

	boolean isProfiling() {
		status.get()
	}

	Long getMavenPid() {
	    try {
            def id = ManagementFactory.runtimeMXBean.name - ~/@.*/
            log.info "Maven process ID: $id"
	        return Long.parseLong(id)
	    } catch (e) {
	        log.debug "Error parsing Maven process ID: $e"
	        return -1
	    }
	}

	List<Long> getForkedPids(Long pid) {
        List<Long> ids = "pgrep -P $pid"
                .execute()
                .text.trim()
                .tokenize()
                .collect { Long.parseLong(it.trim()) }
	}

	List<Long> getForkedJmxPorts(Long pid) {
	    def process = ['sh', '-c', "lsof -nP -i4TCP | grep LISTEN | grep $pid"].execute()
	    process.waitFor()
        def ports = process.text
                .tokenize('\n')
                .collect { Long.parseLong(it.tokenize().get(8) - ~/^.*:/) }
        ports
	}

	void start() {
		log.info 'Starting memory profiler'
		def mavenPid = getMavenPid()
		thread = Thread.start {
			status.set(true)
			log.debug 'Profiler running in new thread'
			while (status.get()) {
			    try {
			        def pids = getForkedPids(mavenPid).collect { pid ->
                        getForkedPids(pid)
                    }.flatten()
                    log.info "[Maven PID $mavenPid] Forked PIDs: $pids"
                    def ports = pids.collectEntries { pid ->
                        def ports = getForkedJmxPorts(pid)
                        def port = ports.find { isValidJmxPort(it) }
                        [(pid): port]
			        }
			        log.info "JMX ports by PID: $ports"
			        if (ports.size() > 1) {
                        log.warn 'More than one JMX port found, connecting to the first one'
			        }
                    checkMemory(ports.find().value)
			    } catch (e) {
			        log.info "Failed to get JMX port: $e.message"
			        log.debug "Failed to get JMX port: $e"
			    }
			    sleep 2000
			}
		}
	}

	boolean isValidJmxPort(Long port) {
	    try {
	        connect(port)
	        return true
	    } catch (e) {
	        log.warn "Invalid JMX port: $port"
	        return false
	    }
	}

	void stop() {
		log.info 'Stopping memory profiler'
		status.set(false)
		thread.join()
	}

	protected void checkMemory(Long port) {
        try {
            log.info 'Checking memory usage via JMX'
            def server = getServer(port)
            log.info "Successfully connected to server: $server"
            def usage = queryMemoryUsage(server)
            def percentage = (usage.used / usage.max * 100.0).round(1)
            log.info "[$timestamp] $usage.used/$usage.max - $percentage %"
        } catch (e) {
            log.info "JMX connection failed: $e"
            // e.printStackTrace()
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

    protected JmxServer getServer(Long port) {
        if (!connection) {
            connection = connect(port)
        }
        connection
    }

    protected JmxServer connect(Long port) {
        def url = getUrl(port)
        log.info "Connecting to JMX at $url"
        def server = JmxFactory.connect(new JmxUrl(url))
        log.info "Successfully connected to JMX: $server"
        def mbeanServer = server.MBeanServerConnection
        log.info "Successfully acquired connection: $mbeanServer"
        mbeanServer
    }

	protected String getTimestamp() {
	    new Date().format('HH:mm:ss')
	}
}
