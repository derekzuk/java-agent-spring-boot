package derekzuk.agent.instrument.extension;

import derekzuk.agent.instrument.extension.transformer.AttachAPITransformer;
import derekzuk.agent.instrument.extension.transformer.MetricTransformer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.UUID;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class HttpServletResponseAgent {
	private static volatile Instrumentation globalInstrumentation;

	public static long getObjectSize(final Object object) {
		return globalInstrumentation.getObjectSize(object);
	}

	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Allows installation of java agent from command line.
	 * See README for instructions
	 *
	 * @param agentArguments  arguments for agent
	 * @param instrumentation instrumentation instance
	 */
	public static void premain(String agentArguments,
							   Instrumentation instrumentation) {
		globalInstrumentation = instrumentation;
		new AgentBuilder.Default()
				.type(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RestController")))
				.transform(new MetricTransformer())
				.with(AgentBuilder.Listener.StreamWriting.toSystemOut())
				.with(AgentBuilder.TypeStrategy.Default.REDEFINE)
				.installOn(instrumentation);
	}

	/**
	 * Allows installation of java agent with Attach API.
	 *
	 * @param agentArguments  arguments for agent
	 * @param instrumentation instrumentation instance
	 */
	public static void agentmain(String agentArguments,
								 Instrumentation instrumentation) {
		install(instrumentation);
	}

	private static void install(Instrumentation instrumentation) {
		createAgent(instrumentation);
	}

	private static AgentBuilder createAgent(Instrumentation instrumentation) {
		return new AgentBuilder.Default().disableClassFormatChanges()
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.type(isAnnotatedWith(named("org.springframework.web.bind.annotation.RestController")))
				.transform(new AttachAPITransformer())
				.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
	}
}