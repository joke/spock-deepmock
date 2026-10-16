## 1. Preconditions and spikes

- [x] 1.1 Make sure that the change `fix-deep-mock-responses` is complete. Do not start this change before that.
- [x] 1.2 Do spike S1 from design.md D9. Record the result in design.md.
- [x] 1.3 Do spike S2 from design.md D9. Record the result in design.md.
- [x] 1.4 Do spike S3 from design.md D9. Record the result in design.md.
- [x] 1.5 Do spike S4 from design.md D9. Record the result in design.md.
- [x] 1.6 If a spike failed, stop. Update design.md and get approval before you continue.
- [x] 1.7 Delete the spike code.
- [x] 1.8 Add `org.junit.platform:junit-platform-testkit` to the `spock24groovy40Implementation` dependencies in `examples/build.gradle`. `EmbeddedSpecRunner` needs it, and `spock-core` does not declare it.

## 2. Failing specifications

- [x] 2.1 Write a specification for each scenario of the requirement "Strict mocking with nested chains". For the scenario that must fail, call `EmbeddedSpecRunner.run` directly and expect the error with `thrown()`.
- [x] 2.2 Write a specification for each scenario of the requirement "Same syntax and options as Spock mocks".
- [x] 2.3 Write a specification for each scenario of the requirements "Chain evaluation counts no invocation" and "Unmatched calls register no interaction".
- [x] 2.4 Write a specification for the scenario of the requirement "Readable implicit interactions". Use `spock.util.EmbeddedSpecRunner` for features that must fail. Expect the error with `thrown()`, because the runner throws the first failure.
- [ ] 2.5 Run `./gradlew :examples:testspock24groovy40`. Make sure that the new specifications fail for the expected reason.

## 3. Rewrite of DeepMock

- [x] 3.1 Add the public class `DeepMockSupport` with `RESPONSE`, `rejectDefaultResponse()`, `options(Object)` and `arguments(Object)`, as design.md D2 describes.
- [x] 3.2 Add the global AST transformation `DeepMockTransformation` for the `CONVERSION` phase. Make it rewrite `DeepMock` and `GroovyDeepMock` calls with the rules of design.md D2. Copy the source positions to the new nodes.
- [x] 3.3 Replace `SpockTransformationInitialization` with `DeepMockTransformation` in `META-INF/services/org.codehaus.groovy.transform.ASTTransformation`.
- [x] 3.4 Delete `SpockTransformationInitialization`, `SpecInternalsExtension` and `src/main/resources/dsld/spk.dsld`.
- [x] 3.5 Move the extension module descriptor to `META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule`. Remove `SpecInternalsExtension` from its list.
- [x] 3.6 In `MockingApiExtension`, add `mockMaker` to all `@NamedParams`. Change the exception message to say that the compiler did not rewrite the call.
- [x] 3.7 Run `./gradlew :examples:testspock24groovy40`. Make sure that the specifications from before this change pass.

## 4. Chain handling

- [x] 4.1 In `DeepMockTransformation`, wrap the targets of interaction statements as design.md D4 describes.
- [x] 4.2 In `DeepMockSupport`, add `beginWalk` and `endWalk` and the walker interaction, as design.md D5 describes.
- [x] 4.3 In `MockCache`, keep a link from each nested mock to its cache entry. Add a method that returns the path from the root deep mock to a nested mock.
- [x] 4.4 In `endWalk`, register one implicit step interaction for each step of the path, root first, as design.md D6 describes.
- [x] 4.5 In `DeepMockInteraction`, store the line and the column of the declaring interaction. Implement all `IMockInteraction` methods as design.md D8 describes. Remove all `UnreachableCodeError`s.
- [x] 4.6 In `DeepMockResponse`, remove the interaction registration, the `MockController` cast and the `NoSuchElementException` handling.

## 5. README

- [x] 5.1 In the section "Usage" of `README.adoc`, show that `DeepMock()` and `GroovyDeepMock()` accept all argument forms and options of `Mock()` and `GroovyMock()`, except `defaultResponse`.
- [x] 5.2 In the section "Usage", add examples for the `mockMaker` option, for options and types from variables, and for an interaction closure with a chain. Start the chain in the closure with `it.`, for example `it.child.name >> 'Hello'`. State that a chain in a mock closure must start with `it.`, as for all Spock mocks.
- [x] 5.3 Add the section "Chains and interactions". Explain the `_ *` rule with an example.
- [x] 5.4 In the section "Chains and interactions", state that `0 * _` works in any position.
- [x] 5.5 In the section "Chains and interactions", state that an interaction that verifies a step must come before the chain.
- [x] 5.6 In the section "Chains and interactions", state that a specification must not stub a step to another object and also use a chain through that step.
- [x] 5.7 In the section "Chains and interactions", state that a chain declared only in `given:` does not pass a `0 * _` in `then:`, as for all Spock interactions.
- [x] 5.8 In the section "Migrating from 4.x", add that evaluating a chain in an interaction declaration no longer counts invocations. Specifications that compensated for this count must use the real count.

## 6. Verification

- [x] 6.1 Run `./gradlew :examples:testspock24groovy40`. Make sure that all specifications pass, including `LifetimeTest`, `NestedIdentityTest` and `OptionsTest`.
- [x] 6.2 Run `./gradlew publishToMavenLocal`. Examine the jar: the services file names `DeepMockTransformation`, and the extension module descriptor is in `META-INF/groovy/`.
- [x] 6.3 Run `./gradlew check`. If there is a violation, stop and fix it. Do not continue until `./gradlew check` passes with no violations.
