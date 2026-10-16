## Why

`DeepMock()` reaches the Spock compiler through Spock internals. A global AST transformation adds names to the static sets in `org.spockframework.util.Identifiers`. Spock then rewrites the call to `SpecInternals.DeepMockImpl(...)`, which resolves only through an extension method on `java.lang.Class`. `SpecInternals` is marked internal, and Spock 2.4 changed its shape. Interactions on chains also have defects: evaluating a chain in an interaction declaration counts as invocations, and implicit interactions registered during `when:` cause false `WrongInvocationOrderError`s.

## What Changes

- A global AST transformation in the `CONVERSION` phase rewrites `DeepMock(...)` and `GroovyDeepMock(...)` into `Mock(...)` and `GroovyMock(...)` with the deep mock default response in the options. Spock then handles the call as a normal mock creation, with name and type inference, interaction closures and all mock options.
- Remove `SpockTransformationInitialization` (the `Identifiers` mutation), `SpecInternalsExtension` (the extension methods on `java.lang.Class`) and the Groovy-Eclipse DSLD file `spk.dsld`.
- `MockingApiExtension` stays for IDE support. Its `@NamedParams` get the `mockMaker` option. Its methods throw an `InvalidSpecException` that tells the user that the compiler did not rewrite the call.
- Move the extension module descriptor from `META-INF/services/` to `META-INF/groovy/`, the location that Groovy 2.5 and later read first.
- The same transformation wraps the target of each interaction declaration. While Spock evaluates the target, calls on deep mocks count no invocation and are not recorded as unmatched.
- An interaction that reaches its target through calls on deep mocks behaves as if the specification declared `_ * parent.call(arguments) >> nested` at the position of the interaction, for each call on the path from the root deep mock. The path includes calls that earlier statements made, for example a nested mock saved in a variable.
- An unmatched call on a deep mock no longer registers an interaction. This removes the false `WrongInvocationOrderError` and the exception handling for `cleanup:` blocks.
- Implicit step interactions show the call and the line of the declaring interaction in Spock messages.
- Update `README.adoc`:
  - The usage section shows all argument forms and options of `Mock()`.
  - A new section explains the chain rules.
  - The migration section adds that evaluating a chain in a declaration no longer counts invocations.

Out of scope: Spock upstream changes, a Groovy 5 test set and deep stubs.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `deep-mocking`: deep mocks accept all options of `Mock()` and `GroovyMock()` except `defaultResponse`; chain interactions follow the `_ *` rule; evaluating a chain in a declaration counts no invocation; unmatched calls register no interaction; implicit step interactions have readable messages.

## Impact

- Library: new transformation class and new runtime support class for generated code. `DeepMockResponse` no longer uses `MockController`. `DeepMockInteraction` becomes the implicit step interaction with text and line. `MockCache` keeps parent links to build paths. `SpockTransformationInitialization`, `SpecInternalsExtension` and `spk.dsld` are deleted.
- Resources: `META-INF/services/org.codehaus.groovy.transform.ASTTransformation` names the new transformation. The extension module descriptor moves to `META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule`.
- Tests: new specifications in `examples` for the rewrite, the chain rules, the ordering case and the messages.
- Documentation: `README.adoc`.
- Consumers: the public `DeepMock()` and `GroovyDeepMock()` signatures do not change. Specifications that verify a step of a chain must declare that interaction before the chain, as with any Spock `_ *` interaction.
- Depends on `fix-deep-mock-responses` (path names and cache entries).
- Release: part of 5.0.0.
