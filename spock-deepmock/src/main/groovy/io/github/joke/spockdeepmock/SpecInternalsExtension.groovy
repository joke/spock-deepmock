package io.github.joke.spockdeepmock

import groovy.transform.CompileStatic
import org.spockframework.lang.SpecInternals

@CompileStatic
class SpecInternalsExtension {

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse())
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(), closure)
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options))
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options), closure)
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(), specifiedType)
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(), specifiedType, closure)
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options), specifiedType)
    }

    static Object DeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options), specifiedType, closure)
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse())
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(), closure)
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options))
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options), closure)
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(), specifiedType)
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(), specifiedType, closure)
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options), specifiedType)
    }

    static Object GroovyDeepMockImpl(SpecInternals self, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
        self::MockImpl(inferredName, inferredType, addDefaultResponse(options), specifiedType, closure)
    }

    private static Map<String, Object> addDefaultResponse(Map options = [:]) {
        if (options.defaultResponse) {
            throw new UnsupportedOperationException("DeepMocks with default response are not supported.")
        }
        [*:options, defaultResponse: DeepMockResponse.instance as Object]
    }
}
