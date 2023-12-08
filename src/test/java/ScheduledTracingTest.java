import brave.Tracing;
import brave.baggage.BaggagePropagation;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import brave.schedule.ScheduledTracingThreadPoolExecutor;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

public class ScheduledTracingTest {
    public static void main(String[] args) {
        Propagation.Factory propagationFactory = BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
                .build();

        CurrentTraceContext.ScopeDecorator correlationScopeDecorator = MDCScopeDecorator.newBuilder().build();

        CurrentTraceContext currentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(correlationScopeDecorator)
                .build();

        Tracing tracing = Tracing.newBuilder()
                .localServiceName("test")
                .sampler(Sampler.ALWAYS_SAMPLE)
                .propagationFactory(propagationFactory)
                .currentTraceContext(currentTraceContext)
                .build();

        ScheduledTracingThreadPoolExecutor executor = new ScheduledTracingThreadPoolExecutor(10, tracing);


        MDC.put("test","test");
        executor.scheduleAtFixedRate(() -> {
            System.out.printf("Thread%s start, traceId:%s \n", Thread.currentThread().getId(), MDC.get("traceId"));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("Thread%s end, traceId:%s \n", Thread.currentThread().getId(), MDC.get("traceId"));
        }, 1, 2, TimeUnit.SECONDS);
    }
}
