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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 3)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InitializationBenchmark {

    private final Class<?> CLAZZ = WeatherNotifier.class;
    private final String OWNER = "Adam";

    private Constructor<?> reflectionConstructor;
    private MethodHandle mhConstructor;

    @Setup
    public void setup() throws Throwable {
        // reflection call
        reflectionConstructor = CLAZZ.getConstructor(String.class);

        // method handles call
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        mhConstructor = publicLookup.findConstructor(CLAZZ, methodType(void.class, String.class));
    }

    @Benchmark
    public WeatherNotifier testDirect() {
        return new WeatherNotifier(OWNER);
    }

    @Benchmark
    public WeatherNotifier testReflection() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return (WeatherNotifier) reflectionConstructor.newInstance(OWNER);
    }

    @Benchmark
    public WeatherNotifier testInvokeDynamic() throws Throwable {
        return (WeatherNotifier) mhConstructor.invoke(OWNER);
    }
}
