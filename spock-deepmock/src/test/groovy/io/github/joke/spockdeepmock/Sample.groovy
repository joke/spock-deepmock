package io.github.joke.spockdeepmock

import java.lang.reflect.Method
import java.lang.reflect.Type
import org.spockframework.mock.IMockMethod

/**
 * Methods with different return types, so specs can look up real methods as Spock's {@link IMockMethod}.
 */
class Sample {

    static IMockMethod method(String name) {
        new ReflectedMethod(Sample.declaredMethods.find { it.name == name })
    }

    int primitive() {
        0
    }

    String[] array() {
        null
    }

    String object() {
        null
    }

    String withArguments(String first, int second) {
        first + second
    }

    GroovyObject groovyObject() {
        null
    }

    private static final class ReflectedMethod implements IMockMethod {

        private final Method method

        ReflectedMethod(final Method method) {
            this.method = method
        }

        @Override
        String getName() {
            method.name
        }

        @Override
        List<Class<?>> getParameterTypes() {
            method.parameterTypes.toList()
        }

        @Override
        List<Type> getExactParameterTypes() {
            method.genericParameterTypes.toList()
        }

        @Override
        Class<?> getReturnType() {
            method.returnType
        }

        @Override
        Type getExactReturnType() {
            method.genericReturnType
        }

        @Override
        boolean isStatic() {
            false
        }
    }
}
