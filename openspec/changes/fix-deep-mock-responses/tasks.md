## 1. Test setup

- [x] 1.1 Add `org.mockito:mockito-core` 5.23.0 to the `constraints` of the `dependencies` platform.
- [x] 1.2 Add `mockito-core` to the `spock24groovy40Implementation` dependencies in `examples/build.gradle`.
- [x] 1.3 Add the final class `FinalLeaf` to the Java fixtures and to the Groovy fixtures. Give it one method that returns a `String`.
- [x] 1.4 Add methods to `Nested` and `GroovyNested` that return `void`, `int`, `boolean`, `Integer`, `List<Nested>` and `FinalLeaf`.

## 2. Failing specifications

- [x] 2.1 Write a specification for each scenario of the requirement "Deep mock creation" that this change modifies.
- [x] 2.2 Write a specification for each scenario of the requirement "Nested mock options".
- [x] 2.3 Write a specification for each scenario of the requirement "Nested mock names".
- [x] 2.4 Delete `UnmockableTypeTest`. Its scenario does not exist after this change.
- [x] 2.5 Run `./gradlew :examples:testspock24groovy40`. Make sure that the new specifications fail for the expected reason.

## 3. Fallback answer

- [x] 3.1 Remove `requireMockable` and `unmockableReason` from `MockCache`.
- [x] 3.2 In `DeepMockResponse`, use `ZeroOrNullResponse.INSTANCE` for `void`, primitive and array return types. Do not ask the cache for these types.
- [x] 3.3 In `MockCache`, catch `CannotCreateMockException` from the mock creation. Record the key of the failed creation (exact return type, mock implementation, nested options).
- [x] 3.4 In `DeepMockResponse`, use the fallback answer when the creation failed or when the key is recorded as failed. Do not register an interaction for a fallback answer.

## 4. Option inheritance and names

- [x] 4.1 In `MockCache`, build the nested options from the `IMockConfiguration` of the parent: `defaultResponse`, `mockMaker` (only when not null), `verified` and `useObjenesis`.
- [x] 4.2 In `MockCache`, make the path name `<parent>.<method>(<arguments>)` as design.md D5 describes. Use the mock name for mock arguments and Groovy `inspect()` for other arguments.
- [x] 4.3 Use the simple name of the parent type when the parent mock has no name.

## 5. README

- [x] 5.1 Change the license badge in `README.adoc` so that it points to `joke/spock-deepmock`.
- [x] 5.2 Replace the link to the Spock 1.3 documentation with a link to the Spock 2.4 page "Interaction Based Testing".
- [x] 5.3 Correct the misspelled `<depenencies>` element in the Maven snippet.
- [x] 5.4 Replace the links `examples-java` and `examples-groovy` with a link to `examples/src/test/groovy/io/github/joke/test`.
- [x] 5.5 Change the usage example so that it uses the methods of the example classes `Caller` and `Nested`.
- [x] 5.6 Add the section "Requirements": Spock 2.4 or later, Java 8 or later, tested with Groovy 4.0.
- [x] 5.7 Add the section "How deep mocks answer". Describe the nested mock for each call and the same nested mock for the same call with equal arguments.
- [x] 5.8 In the section "How deep mocks answer", describe the `Mock()` answer for return types that cannot be mocked, the lifetime of one specification instance, and the path names.
- [x] 5.9 In the section "How deep mocks answer", state that `DeepMock()` creates a Java mock and `GroovyDeepMock()` creates a Groovy mock.
- [x] 5.10 Add the section "Options". List the options that nested mocks inherit and the options that they do not inherit. State that `defaultResponse` is rejected.
- [x] 5.11 Add the section "Migrating from 4.x". List these changes: `GroovyDeepMock()` creates a Groovy mock, each call has its own nested mock, nested mocks live for one specification instance, return types that cannot be mocked get the `Mock()` answer, nested mocks have path names, and Spock 2.4 is necessary.

## 6. Verification

- [x] 6.1 Run `./gradlew :examples:testspock24groovy40`. Make sure that all specifications pass, including `MockImplementationTest`.
- [x] 6.2 Write the commit message with `!` and a `BREAKING CHANGE:` footer as design.md D7 describes.
- [x] 6.3 Run `./gradlew check`. If there is a violation, stop and fix it. Do not continue until `./gradlew check` passes with no violations.
