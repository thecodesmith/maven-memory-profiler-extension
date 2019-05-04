package com.thecodesmith.maven.memory.profile;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

@Component(role = EventSpy.class, hint = "memory-profiler", description = "Capture forked test JVM memory usage")
public class MyProfilerEventSpy extends AbstractEventSpy {

    @Requirement
    private Logger logger;

    public MyProfilerEventSpy(Logger logger) {
        this.logger = logger;
    }

    public MyProfilerEventSpy() {

    }

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
        System.out.println("PROFILER STARTING");
        logger.info("ProfilerEventSpy is registered.");
//        Profiler profiler = new Profiler();
//        Thread thread = new Thread(profiler);
//        thread.start();
    }

    @Override
    public void onEvent(Object event) throws Exception {
        super.onEvent(event);
        logger.info("Event: " + event.getClass() + " - " + event.toString());
    }

    @Override
    public void close() throws Exception {
        super.close();
        System.out.println("PROFILER DONE");
    }
}
