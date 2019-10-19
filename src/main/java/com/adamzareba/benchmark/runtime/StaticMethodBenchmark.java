package com.adamzareba.benchmark.runtime;

import com.adamzareba.service.WeatherNotifier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 3)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StaticMethodBenchmark {

    private final Class<?> CLAZZ = WeatherNotifier.class;
    private final int repeatCount = 5;
    private final String staticMethodNameNoParam = "repeatDescription";

    private Method reflectionRepeatDescriptionMethod;

    private MethodHandle mhRepeatDescriptionMethod;

    @Setup
    public void setup() throws Throwable {
        // reflection call
        reflectionRepeatDescriptionMethod = CLAZZ.getMethod(staticMethodNameNoParam, int.class);

        // method handles call
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        mhRepeatDescriptionMethod = publicLookup.findStatic(CLAZZ, staticMethodNameNoParam, methodType(String.class, int.class));
    }

    @Benchmark
    public String testDirect() {
        return WeatherNotifier.repeatDescription(repeatCount);
    }

    @Benchmark
    public String testReflection() throws IllegalAccessException, InvocationTargetException {
        return (String) reflectionRepeatDescriptionMethod.invoke(null, repeatCount);
    }

    @Benchmark
    public String testInvokeDynamic() throws Throwable {
        return (String) mhRepeatDescriptionMethod.invoke(repeatCount);
    }
}
