package io.github.joke.spockdeepmock;

import groovy.lang.Closure;
import org.apache.groovy.util.Maps;
import org.spockframework.runtime.SpecInternals;
import spock.lang.Specification;

import java.util.Map;

import static org.spockframework.runtime.GroovyRuntimeUtil.invokeMethod;

public class SpecInternalsExtension {

    private final static DeepMockResponse deepMockResponse = new DeepMockResponse();

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse());
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(), closure);
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options));
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options), closure);
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(), specifiedType);
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(), specifiedType, closure);
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options), specifiedType);
    }

    public static Object DeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options), specifiedType, closure);
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse());
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(), closure);
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options));
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options), closure);
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(), specifiedType);
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(), specifiedType, closure);
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options), specifiedType);
    }

    public static Object GroovyDeepMockImpl(final Class<?> owner, final Specification self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(SpecInternals.class, "MockImpl", self, inferredName, inferredType, addDefaultResponse(options), specifiedType, closure);
    }

    private static Map<String, Object> addDefaultResponse(final Map<String, Object> options) {
        if (options.containsKey("defaultResponse")) {
            throw new UnsupportedOperationException("DeepMocks with default response are not supported.");
        }
        options.put("defaultResponse", deepMockResponse);
        return options;
    }

    private static Map<String, Object> addDefaultResponse() {
        return Maps.of("defaultResponse", deepMockResponse);
    }
}
