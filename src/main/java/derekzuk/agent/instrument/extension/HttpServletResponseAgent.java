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
		install(instrumentation);
	}

	private static void install(Instrumentation instrumentation) {
		createAgent(instrumentation);
	}

	private static AgentBuilder createAgent(Instrumentation instrumentation) {
		return new AgentBuilder.Default().disableClassFormatChanges()
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.type(isAnnotatedWith(named("org.springframework.web.bind.annotation.RestController")))
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
						).visit(Advice.to(MetricsTransformer.EnterAdvice.class, MetricsTransformer.ExitAdviceMethods.class)
								.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PutMapping")))
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

			final AsmVisitorWrapper putMappingVisitor =
					Advice.to(EnterAdvice.class, ExitAdviceMethods.class)
							.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PostMapping")));

			return builder.visit(getMappingVisitor).visit(postMappingVisitor).visit(putMappingVisitor);
		}

		private static class EnterAdvice {
			@Advice.OnMethodEnter
			static long enter() {
				return System.nanoTime();
			}
		}

		/**
		 * This Exit Advice method requires that an HttpServletResponse object be the first argument in the
		 * HTTP request method. This could be improved upon. Ideally, we should not have to depend on the
		 * HttpServletResponse object being the first argument. Further, in a Spring Boot application it is
		 * more common to use a ResponseEntity as the return object of a method in a Rest Controller.
		 */
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
				String UUID = createUUID();
				r.setHeader("UUID", UUID);

				// Get response size in bytes
				final long responseSizeBytes = getObjectSize(r);

				// Create metrics
				MetricsCollector.report(executable.getName(), duration, responseSizeBytes, UUID);
			}
		}
	}
}