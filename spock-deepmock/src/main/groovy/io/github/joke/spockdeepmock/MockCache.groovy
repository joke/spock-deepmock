package io.github.joke.spockdeepmock

import groovy.transform.Memoized
import org.apache.groovy.util.Maps
import org.spockframework.mock.IMockObject
import org.spockframework.mock.MockImplementation
import org.spockframework.runtime.SpecInternals
import spock.lang.Specification

import java.lang.reflect.Type

import static java.lang.Integer.MAX_VALUE
import static org.spockframework.mock.MockImplementation.GROOVY

class MockCache {

    DeepMockResponse deepMockResponse

    MockCache(final DeepMockResponse deepMockResponse) {
        this.deepMockResponse = deepMockResponse
    }

    @Memoized(protectedCacheSize = MAX_VALUE)
    Object createMock(final IMockObject mockObject, final Specification specification, final Type exactReturnType, final MockImplementation implementation) {
        final options = Maps.of("defaultResponse", deepMockResponse)
        return implementation == GROOVY
                ? SpecInternals.GroovyMockImpl(specification, mockObject.name, exactReturnType, options)
                : SpecInternals.MockImpl(specification, mockObject.name, exactReturnType, options)
    }
}
