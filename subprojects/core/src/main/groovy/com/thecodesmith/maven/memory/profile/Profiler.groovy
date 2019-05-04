package com.thecodesmith.maven.memory.profile

class Profiler implements Runnable {
	@Override
	void run() {
		println 'com.thecodesmith.maven.memory.profile.Profiler running'
		new File('foo').text = 'hello'
	}
}
