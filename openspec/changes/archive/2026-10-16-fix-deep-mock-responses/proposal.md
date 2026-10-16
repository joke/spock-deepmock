## Why

An unstubbed call on a deep mock fails when the mock makers cannot mock the return type. This includes every `void` method, so `nested.child.close()` throws. A plain `Mock()` answers such calls with `null`, `0` or `false`. Nested mocks also ignore the options of their parent and hard-code "final classes cannot be mocked", although Spock 2.4 mock makers such as `mockito` can mock final classes. Fix this before 5.0.0, because these are behavior changes.

## What Changes

- **BREAKING**: An unstubbed call whose return type the mock makers cannot mock (`void`, primitives, arrays, and classes such as `String` or `Integer`) answers like a Spock `Mock()`: `null`, `0` or `false`. Before, it threw an `InvalidSpecException` (unreleased) or a `CannotCreateMockException` (4.x).
- The mock makers decide if a return type can be mocked. The library tries to create the nested mock. Only when the mock makers reject the type does it fall back to the `Mock()` answer. A final class gives a nested mock when an available mock maker can mock it.
- Nested mocks inherit the `mockMaker`, `verified` and `useObjenesis` options of their parent. They do not inherit `name`, `additionalInterfaces`, `global` or `constructorArgs`.
- **BREAKING**: A nested mock is named after the call that produced it, for example `nested.getChild().child('first')`. Before, every nested mock had the name of the top-level mock.
- A loop over an unstubbed collection returns no elements: the nested `Iterator` mock answers `hasNext()` with `false`.
- Update `README.adoc`:
  - Fix stale items: the license badge, the link to the Spock 1.3 documentation, the misspelled Maven `<dependencies>` element, the links to example folders that do not exist, and the usage example.
  - Document the behavior on `main`: requirements, nested mock identity per call, lifetime per specification instance, and `GroovyDeepMock()` as a Groovy mock.
  - Document the behavior of this change: the `Mock()` answers, the option inheritance and the path names.
  - Start a migration section for users of 4.x.

Out of scope: how `DeepMock()` gets into the Spock compiler, and the chain interaction rules. The change `rewrite-deepmock-early` covers both.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `deep-mocking`: unstubbed calls with a return type that cannot be mocked answer like `Mock()`; the mock makers decide if a type can be mocked; nested mocks inherit options of their parent; nested mocks are named after their call.

## Impact

- Library: `DeepMockResponse` and `MockCache` change. The `InvalidSpecException` for return types that cannot be mocked goes away.
- Tests: `UnmockableTypeTest` is replaced. New specifications cover the fallback answers, the option inheritance and the names. The fixtures `Nested` and `GroovyNested` get methods that return `void`, primitives, a wrapper type, a collection and a final class.
- Build: the `examples` test classpath gets `org.mockito:mockito-core`, with its version in the `dependencies` platform, to test final classes.
- Documentation: `README.adoc`.
- Consumers: specifications that expected an exception for such calls now get `null`, `0` or `false`. Failure messages show path names for nested mocks.
- Release: part of 5.0.0.
