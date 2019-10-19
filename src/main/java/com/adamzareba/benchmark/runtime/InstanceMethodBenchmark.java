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
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodType.methodType;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 3)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class InstanceMethodBenchmark {

    private final Class<?> CLAZZ = WeatherNotifier.class;
    private final String OWNER = "Adam";
    private final String instanceMethodNameNoParam = "getMessage";
    private final String instanceMethodNameWithParam = "getPrint";

    private Constructor<?> reflectionConstructor;
    private MethodHandle mhConstructor;

    private Method reflectionPrintMethod;
    private Method reflectionGetMessageMethod;

    private MethodHandle mhPrint;
    private MethodHandle mhGetMessage;

    private WeatherNotifier directInstance;
    private WeatherNotifier reflectionInstance;
    private WeatherNotifier mhInstance;

    @Setup
    public void setup() throws Throwable {
        // direct call
        directInstance = new WeatherNotifier(OWNER);

        // reflection call
        reflectionConstructor = CLAZZ.getConstructor(String.class);
        reflectionInstance = (WeatherNotifier) reflectionConstructor.newInstance(OWNER);
        reflectionGetMessageMethod = reflectionInstance.getClass().getMethod(instanceMethodNameNoParam);
        reflectionPrintMethod = reflectionInstance.getClass().getMethod(instanceMethodNameWithParam, String.class);

        // method handles call
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        mhConstructor = publicLookup.findConstructor(CLAZZ, methodType(void.class, String.class));
        mhInstance = (WeatherNotifier) mhConstructor.invoke(OWNER);
        mhGetMessage = publicLookup.findVirtual(CLAZZ, instanceMethodNameNoParam, methodType(String.class));
        mhPrint = publicLookup.findVirtual(CLAZZ, instanceMethodNameWithParam, methodType(String.class, String.class));
    }

    @Benchmark
    public String testDirect() {
        return directInstance.getPrint(directInstance.getMessage());
    }

    @Benchmark
    public String testReflection() throws IllegalAccessException, InvocationTargetException {
        return (String) reflectionPrintMethod.invoke(reflectionInstance, reflectionGetMessageMethod.invoke(reflectionInstance));
    }

    @Benchmark
    public String testInvokeDynamic() throws Throwable {
        return (String) mhPrint.invoke(mhInstance, (String) mhGetMessage.invoke(mhInstance));
    }
}
