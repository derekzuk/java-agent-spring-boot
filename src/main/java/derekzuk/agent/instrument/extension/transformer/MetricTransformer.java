package derekzuk.agent.instrument.extension.transformer;

import derekzuk.agent.instrument.extension.HttpServletResponseAgent;
import derekzuk.agent.instrument.extension.MetricsCollector;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Executable;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class MetricTransformer implements AgentBuilder.Transformer {
    
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
                        .on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PutMapping")));

        return builder.visit(getMappingVisitor).visit(postMappingVisitor).visit(putMappingVisitor);
    }

    public static class EnterAdvice {
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
    public static class ExitAdviceMethods {
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        static void exit(@Advice.Origin final Executable executable,
                         @Advice.Enter final long startTime,
                         @Advice.AllArguments Object[] args) {
            // Obtain the HttpServletResponse from the method arguments
            for (Object arg : args) {
                if (arg instanceof HttpServletResponse) {
                    // Get duration
                    final long duration = System.nanoTime() - startTime;

                    // Get response object
                    HttpServletResponse r = (HttpServletResponse) arg;

                    // Add unique ID to response header
                    String UUID = HttpServletResponseAgent.createUUID();
                    r.setHeader("UUID", UUID);

                    // Get response size in bytes
                    // TODO: This calculates only the size of the HttpServletResponse object, not the response body.
                    final long responseSizeBytes = HttpServletResponseAgent.getObjectSize(r);

                    // Create metrics
                    MetricsCollector.report(executable.getName(), duration, responseSizeBytes, UUID);
                }
            }
        }
    }
}
