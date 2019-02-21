//package pl.halun.demo.bytebuddy.logging;
//
//import net.bytebuddy.agent.builder.AgentBuilder;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.asm.AsmVisitorWrapper;
//import net.bytebuddy.description.type.TypeDescription;
//import net.bytebuddy.dynamic.DynamicType;
//import net.bytebuddy.implementation.MethodDelegation;
//import net.bytebuddy.matcher.ElementMatchers;
//import net.bytebuddy.utility.JavaModule;
//
//import javax.servlet.ServletOutputStream;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import java.io.IOException;
//import java.lang.instrument.Instrumentation;
//import java.lang.reflect.Executable;
//
//import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
//import static net.bytebuddy.matcher.ElementMatchers.named;
//
//public class ServletAgent {
//
//    public static void premain(String arguments, Instrumentation instrumentation) throws ClassNotFoundException { // (1)
//        new AgentBuilder.Default()
//                .type(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RestController")))
//                .transform(new MetricsTransformer())
//                .installOn(instrumentation);
//    }
//
//    public static class Interceptor {
//        public static void intercept(ServletRequest req, ServletResponse res) {
//            try {
//                final ServletOutputStream outputStream = res.getOutputStream();
//
//                outputStream.println("Hello, transformed world!");
//                outputStream.flush();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    private static class MetricsTransformer implements AgentBuilder.Transformer {
//        @Override
//        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
//                                                TypeDescription typeDescription,
//                                                ClassLoader classLoader,
//                                                JavaModule javaModule) {
////            final AsmVisitorWrapper getMappingVisitor =
////                    Advice.to(ServletAgent.MetricsTransformer.EnterAdvice.class, ServletAgent.MetricsTransformer.ExitAdviceMethods.class)
////                            .on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RequestMapping")));
//
//            ElementMatchers.isNamed()
//            return builder.method(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.RequestMapping")))
//                    .intercept(
//                            MethodDelegation.to(Interceptor.class)
//                    );
//        }
//
//        private static class EnterAdvice {
//            @Advice.OnMethodEnter
//            static long enter() {
//                return System.nanoTime();
//            }
//        }
//
//        private static class ExitAdviceMethods {
//            @Advice.OnMethodExit(onThrowable = Throwable.class)
//            static void exit(@Advice.Origin final Executable executable,
//                             @Advice.Enter final long startTime) {
//                final long duration = System.nanoTime() - startTime;
//                MetricsCollector.report(executable.toGenericString(), duration);
//            }
//        }
//
//        private static class ExitAdviceConstructors {
//            @Advice.OnMethodExit
//            static void exit(@Advice.Origin final Executable executable,
//                             @Advice.Enter final long startTime) {
//                final long duration = System.nanoTime() - startTime;
//                MetricsCollector.report(executable.toGenericString(), duration);
//            }
//        }
//    }
//}