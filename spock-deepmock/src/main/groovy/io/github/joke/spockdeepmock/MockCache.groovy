package io.github.joke.spockdeepmock

import groovy.transform.Memoized
import org.apache.groovy.util.Maps
import org.spockframework.mock.IMockObject
import org.spockframework.mock.MockImplementation
import spock.lang.Specification

import java.lang.reflect.Type

import static java.lang.Integer.MAX_VALUE
import static org.spockframework.mock.MockNature.MOCK

class MockCache {

    DeepMockResponse deepMockResponse

    MockCache(final DeepMockResponse deepMockResponse) {
        this.deepMockResponse = deepMockResponse
    }

    @Memoized(protectedCacheSize = MAX_VALUE)
    Object createMock(final IMockObject mockObject, final Specification specification, final Type exactReturnType, final MockImplementation implementation) {
        return specification.createMock(mockObject.name, exactReturnType, MOCK, implementation, Maps.of("defaultResponse", deepMockResponse), null)
    }
}
