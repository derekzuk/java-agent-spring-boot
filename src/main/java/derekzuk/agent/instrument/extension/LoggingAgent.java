package derekzuk.agent.instrument.extension;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import javax.servlet.http.HttpServletResponse;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Executable;
import java.util.UUID;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class LoggingAgent {
	private static volatile Instrumentation globalInstrumentation;

	private static volatile int headerCounter = 0;

	private static final String DEMO_INSTRUMENTED_CLASS_NAME = "com.derekzuk.springbootangular.hello.GreetingController";

	public static long getObjectSize(final Object object) {
		return globalInstrumentation.getObjectSize(object);
	}

	public static String getIncrementedHeaderCounter() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Allows installation of java agent from command line.
	 *
	 * @param agentArguments  arguments for agent
	 * @param instrumentation instrumentation instance
	 */
	public static void premain(String agentArguments,
							   Instrumentation instrumentation) {
		globalInstrumentation = instrumentation;
		new AgentBuilder.Default()
				.type(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RestController")))
				.transform(new MetricsTransformer())
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
		install(DEMO_INSTRUMENTED_CLASS_NAME, instrumentation);
	}

	private static void install(String className, Instrumentation instrumentation) {
		createAgent(instrumentation);
	}

	private static AgentBuilder createAgent(Instrumentation instrumentation) {
		return new AgentBuilder.Default().disableClassFormatChanges()
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.type(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RestController")))
				.transform(new AgentBuilder.Transformer() {

					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
															TypeDescription typeDescription,
															ClassLoader classLoader,
															JavaModule javaModule) {
						return builder.visit(Advice.to(MetricsTransformer.EnterAdvice.class, MetricsTransformer.ExitAdviceMethods.class)
								.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.GetMapping")))
						).visit(Advice.to(MetricsTransformer.EnterAdvice.class, MetricsTransformer.ExitAdviceMethods.class)
								.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PostMapping")))
						);
					}

				})
				.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
	}

	private static class MetricsTransformer implements AgentBuilder.Transformer {
		@Override
		public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
												TypeDescription typeDescription,
												ClassLoader classLoader,
												JavaModule javaModule) {
			final AsmVisitorWrapper getMappingVisitor =
					Advice.to(EnterAdvice.class, ExitAdviceMethods.class)
							.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.GetMapping")));

			final AsmVisitorWrapper postMappingVisitor =
					Advice.to(EnterAdvice.class, ExitAdviceMethods.class)
							.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PostMapping")));

			return builder.visit(getMappingVisitor).visit(postMappingVisitor);
		}

		private static class EnterAdvice {
			@Advice.OnMethodEnter
			static long enter() {
				return System.nanoTime();
			}
		}

		private static class ExitAdviceMethods {
			@Advice.OnMethodExit(onThrowable = Throwable.class)
			static void exit(@Advice.Origin final Executable executable,
							 @Advice.Enter final long startTime,
							 @Advice.Argument(0) Object httpRes) {
				// Get duration
				final long duration = System.nanoTime() - startTime;

				// Get response object
				HttpServletResponse r = (HttpServletResponse) httpRes;

				// Add unique ID to response header
				String responseUniqueID = getIncrementedHeaderCounter();
				r.setHeader("responseUniqueID", responseUniqueID);

				// Get response size in bytes
				final long responseSizeBytes = getObjectSize(r);

				// Create metrics
				MetricsCollector.report(executable.getName(), duration, responseSizeBytes, responseUniqueID);
			}
		}
	}
}