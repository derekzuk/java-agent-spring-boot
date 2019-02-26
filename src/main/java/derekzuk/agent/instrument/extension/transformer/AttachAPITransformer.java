package derekzuk.agent.instrument.extension.transformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class AttachAPITransformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                            TypeDescription typeDescription,
                                            ClassLoader classLoader,
                                            JavaModule javaModule) {
        return builder.visit(Advice.to(MetricTransformer.EnterAdvice.class, MetricTransformer.ExitAdviceMethods.class)
                .on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.GetMapping")))
        ).visit(Advice.to(MetricTransformer.EnterAdvice.class, MetricTransformer.ExitAdviceMethods.class)
                .on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PostMapping")))
        ).visit(Advice.to(MetricTransformer.EnterAdvice.class, MetricTransformer.ExitAdviceMethods.class)
                .on(ElementMatchers.isAnnotatedWith(named("org.springframework.web.bind.annotation.PutMapping")))
        );
    }
}
