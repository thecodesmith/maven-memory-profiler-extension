package com.thecodesmith.maven.memory.profiler

import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.AtomicBoolean

@Slf4j
class Profiler {
	AtomicBoolean profiling = new AtomicBoolean(false)
	Thread thread

	void start() {
		log.info 'Profiler starting new thread'
		thread = Thread.start {
			profiling.set(true)
			log.info 'Profiler is running in separate thread!!'
			new File('foo').text = 'hello'
		}
	}

	void stop() {
		log.info 'Profiler stopping thread'
		thread.join()
		profiling.set(false)
	}

	boolean isProfiling() {
		profiling.get()
	}
}
