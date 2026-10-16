package io.github.joke.test

import java.lang.ref.WeakReference

import spock.lang.Stepwise
import spock.lang.Specification

@Stepwise
class LifetimeTest extends Specification {

    static Nested firstFeatureChild

    static WeakReference<Nested> releasedChild

    Nested nested = DeepMock()

    def 'first feature records its nested mock'() {
        when:
        firstFeatureChild = nested.child

        then:
        firstFeatureChild != null
    }

    def 'second feature gets its own nested mock'() {
        expect:
        firstFeatureChild != null
        !nested.child.is(firstFeatureChild)

        cleanup:
        firstFeatureChild = null
    }

    def 'several threads get the same nested mock'() {
        given:
        def children = Collections.synchronizedList([])

        when:
        def threads = (1..4).collect { Thread.start { children << nested.child } }
        threads*.join()

        then:
        children.size() == 4
        children.every { it.is(children[0]) }
    }

    def 'nested deep mock call works in a cleanup block'() {
        expect:
        true

        cleanup:
        assert nested.child.child != null
    }

    def 'toString returns no mock'() {
        expect:
        nested.toString() instanceof String
        nested.toString().contains('Nested')
    }

    def 'equals and hashCode keep the Object behavior and return no mock'() {
        expect:
        nested.equals(nested)
        !nested.equals(nested.child)
        nested.hashCode() == nested.hashCode()
        nested.hashCode() instanceof Integer
    }

    def 'nested mock is referenced while its specification instance runs'() {
        when:
        releasedChild = new WeakReference(nested.child)

        then:
        releasedChild.get() != null
    }

    def 'nested mock is garbage collected after its specification instance ends'() {
        expect:
        isCollected(releasedChild)
    }

    def cleanup() {
        assert nested.child.child != null
    }

    static boolean isCollected(final WeakReference<?> reference) {
        for (int attempt = 0; attempt < 50 && reference.get() != null; attempt++) {
            System.gc()
            Thread.sleep(20)
        }
        reference.get() == null
    }
}
