package io.github.joke.spockdeepmock;

import groovy.lang.Closure;
import org.apache.groovy.util.Maps;
import org.spockframework.lang.SpecInternals;

import java.util.Map;

import static org.spockframework.runtime.GroovyRuntimeUtil.invokeMethod;

public class SpecInternalsExtension {

    private final static DeepMockResponse deepMockResponse = new DeepMockResponse();

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse());
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(), closure);
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options));
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options), closure);
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(), specifiedType);
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(), specifiedType, closure);
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options), specifiedType);
    }

    public static Object DeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options), specifiedType, closure);
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse());
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(), closure);
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options));
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options), closure);
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(), specifiedType);
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(), specifiedType, closure);
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options), specifiedType);
    }

    public static Object GroovyDeepMockImpl(final SpecInternals self, final String inferredName, final Class<?> inferredType, final Map<String, Object> options, final Class<?> specifiedType, final Closure<?> closure) {
        return invokeMethod(self, "MockImpl", inferredName, inferredType, addDefaultResponse(options), specifiedType, closure);
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
