package com.thecodesmith.maven.memory.profiler;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import org.codehaus.plexus.logging.Logger;

@Component(role = EventSpy.class, hint = "memory-profiler", description = "Capture forked test JVM memory usage")
public class ProfilerEventSpy extends AbstractEventSpy {

    @Requirement
    private Logger logger;
    private Profiler profiler;

    public ProfilerEventSpy() {
		profiler = new Profiler();
    }

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
        logger.info("Memory profiler extension registered");
    }

    @Override
    public void onEvent(Object event) throws Exception {
        super.onEvent(event);

        if (!(event instanceof ExecutionEvent)) {
            return;
        }

		ExecutionEvent executionEvent = (ExecutionEvent)event;
        if (executionEvent.getMojoExecution() == null) {
        	return;
		}

		String phase = executionEvent.getMojoExecution().getLifecyclePhase();

		if (phase.equals("test")) {
			if (!profiler.isProfiling()) {
                logger.debug("Event phase 'test', starting memory profiler");
				profiler.start();
			} else {
                logger.debug("Event phase 'test', stopping memory profiler");
				profiler.stop();
			}
		}
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
