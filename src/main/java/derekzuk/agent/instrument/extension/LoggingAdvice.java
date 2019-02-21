package derekzuk.agent.instrument.extension;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import net.bytebuddy.asm.Advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAdvice {
//	@Advice.OnMethodEnter
//	public static void intercept(@Advice.BoxedArguments Object[] allArguments,
//			@Advice.Origin Method method) {
//		System.out.println("TEST");
//		Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
//		logger.error("Method {} of class {} called", method.getName(), method
//				.getDeclaringClass().getSimpleName());
//
//		for (Object argument : allArguments) {
//			logger.error("Method {}, parameter type {}, value={}",
//					method.getName(), argument.getClass().getSimpleName(),
//					argument.toString());
//		}
//	}
//	@Advice.OnMethodEnter
//	public static long enter(@Advice.BoxedArguments Object[] allArguments,
//							 @Advice.Origin Method method) {
//		return System.nanoTime();
//	}
}
