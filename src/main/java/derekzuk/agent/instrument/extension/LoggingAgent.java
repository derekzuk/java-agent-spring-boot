package derekzuk.agent.instrument.extension;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Executable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import javax.servlet.http.HttpServletResponse;

public class LoggingAgent {
	private static volatile Instrumentation globalInstrumentation;

	private static volatile int headerCounter = 0;

	private static final String DEMO_INSTRUMENTED_CLASS_NAME = "com.derekzuk.springbootangular.hello.GreetingController";

	public static long getObjectSize(final Object object) {
		if (globalInstrumentation == null) {
			throw new IllegalStateException("Agent not initialized.");
		}
		return globalInstrumentation.getObjectSize(object);
	}

	public static String getIncrementedHeaderCounter() {
		return String.valueOf(headerCounter++);
	}

	public static String executePost(String targetURL) {
		HttpURLConnection connection = null;

		try {
			//Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			//Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.close();

			//Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
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

	private static void install(String className,
								Instrumentation instrumentation) {
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
								.on(isAnnotatedWith(named("org.springframework.web.bind.annotation.RequestMapping")))
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
			final AsmVisitorWrapper requestMappingVisitor =
					Advice.to(EnterAdvice.class, ExitAdviceMethods.class)
							.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RequestMapping")));

			final AsmVisitorWrapper getMappingVisitor =
					Advice.to(EnterAdviceGetMapping.class, ExitAdviceMethodsGetMapping.class)
							.on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.GetMapping")));

			return builder.visit(getMappingVisitor);
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
							 @Advice.Return Object httpRes) {
				final long duration = System.nanoTime() - startTime;
				MetricsCollector.report(executable.toGenericString(), duration);
//				if (httpRes instanceof ResponseEntity) {
//					System.out.println("YES IT IS a ResponseEntity");
//				}
//				ResponseEntity r = (ResponseEntity) httpRes;
//				return r;
			}
		}

		private static class ExitAdviceConstructors {
			@Advice.OnMethodExit
			static void exit(@Advice.Origin final Executable executable,
							 @Advice.Enter final long startTime) {
				final long duration = System.nanoTime() - startTime;
				MetricsCollector.report(executable.toGenericString(), duration);
			}
		}


		private static class EnterAdviceGetMapping {
			@Advice.OnMethodEnter
			static long enter() {
				return System.nanoTime();
			}
		}

		private static class ExitAdviceMethodsGetMapping {
			@Advice.OnMethodExit(onThrowable = Throwable.class)
			static void exit(@Advice.Origin final Executable executable,
							 @Advice.Enter final long startTime,
							 @Advice.Argument(0) Object httpRes) {
//				httpRes is a string...;
				final long duration = System.nanoTime() - startTime;
				MetricsCollector.report(executable.toGenericString(), duration);
				if (httpRes instanceof HttpServletResponse) {
					System.out.println("This object is an instanceof HttpServletResponse");
				}
				HttpServletResponse r = (HttpServletResponse) httpRes;
				String counter = getIncrementedHeaderCounter();
				r.setHeader("testheader", counter);
				System.out.println("Response size: " + getObjectSize(r) + " bytes.");

				System.out.println(executePost("http://localhost:8080/responseEntity/1/2/3"));
			}
		}
	}
}