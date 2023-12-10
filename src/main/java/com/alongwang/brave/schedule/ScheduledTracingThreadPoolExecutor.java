package com.alongwang.brave.schedule;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class ScheduledTracingThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    final Tracing tracing;
    final CurrentTraceContext currentTraceContext;
    final Tracer tracer;
    ThreadLocal<Span> spanThreadLocal = new ThreadLocal<>();
    ThreadLocal<Tracer.SpanInScope> wsThreadLocal = new ThreadLocal<>();


    public ScheduledTracingThreadPoolExecutor(int corePoolSize, Tracing tracing) {
        super(corePoolSize);
        this.tracing = tracing;
        this.currentTraceContext = tracing.currentTraceContext();
        this.tracer = tracing.tracer();
    }

    public ScheduledTracingThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, Tracing tracing) {
        super(corePoolSize, threadFactory);
        this.tracing = tracing;
        this.currentTraceContext = tracing.currentTraceContext();
        this.tracer = tracing.tracer();
    }

    public ScheduledTracingThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler,
                                              Tracing tracing) {
        super(corePoolSize, handler);
        this.tracing = tracing;
        this.currentTraceContext = tracing.currentTraceContext();
        this.tracer = tracing.tracer();
    }

    public ScheduledTracingThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory,
                                              RejectedExecutionHandler handler, Tracing tracing) {
        super(corePoolSize, threadFactory, handler);
        this.tracing = tracing;
        this.currentTraceContext = tracing.currentTraceContext();
        this.tracer = tracing.tracer();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        TraceContext maybeParent = currentTraceContext.get();
        Span span;
        if (maybeParent == null) {
            span = tracer.nextSpan(TraceContextOrSamplingFlags.EMPTY);
        } else {
            span = tracer.newChild(maybeParent);
        }

        if (!tracing.isNoop()) {
            span.start();
        }

        Tracer.SpanInScope ws = tracer.withSpanInScope(span);
        spanThreadLocal.set(span);
        wsThreadLocal.set(ws);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Span span = spanThreadLocal.get();
        Tracer.SpanInScope ws = wsThreadLocal.get();
        if (span != null) {
            span.finish();
        }

        if (ws != null) {
            ws.close();
        }
        super.afterExecute(r, t);
    }
}
