# brave schedule

为定时任务组件提供链路监控能力

## 如何使用
引入依赖
```xml
<dependency>
    <groupId>com.xmair</groupId>
    <artifactId>breave-instrumentation-scheduled</artifactId>
    <version>5.14.1-SNAPSHOT</version>
</dependency>
```

在定时任务线程池配置中，TaskScheduler使用ScheduledTracingThreadPoolExecutor

```java
ScheduledTracingThreadPoolExecutor executor = new ScheduledTracingThreadPoolExecutor(10, tracing);
```

