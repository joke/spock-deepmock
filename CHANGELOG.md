# Changelog

## [5.0.0](https://github.com/joke/spock-deepmock/compare/v4.0.0...v5.0.0) (2026-10-16)


### ⚠ BREAKING CHANGES

* evaluating a chain in an interaction declaration no longer counts invocations. Specifications that compensated for this count must use the real count.
* an unstubbed call whose return type cannot be mocked (void, primitives, arrays, String, Integer and similar) answers like a Spock Mock(): null, 0 or false. Before, it threw InvalidSpecException or CannotCreateMockException. The mock makers decide whether a type can be mocked, so final classes that a mock maker can mock now return deep mocks.
* GroovyDeepMock() now creates a Groovy mock. Before, every GroovyDeepMock overload created a Java mock.

### Features

* fall back to Mock() answers and inherit deep mock options ([54d7149](https://github.com/joke/spock-deepmock/commit/54d714974ecf86e33a259569e8fba1ac36bf3320))
* move deep mocks to Java and fix nested mock identity ([ef6a12f](https://github.com/joke/spock-deepmock/commit/ef6a12fb0821b35e520a64ffdaa557b406323cb3))
* rewrite DeepMock to Mock at compile time and walk chains quietly ([6f13a79](https://github.com/joke/spock-deepmock/commit/6f13a791f6d8d44acb9781b48e637e22fdd0c901))


### Bug Fixes

* **ci:** use the nmcp task names for Central publishing ([abff9cf](https://github.com/joke/spock-deepmock/commit/abff9cfe5e275fdb9a115ad4768a17cae5f79bb7))

## [4.0.0](https://github.com/joke/spock-deepmock/compare/v3.0.0...v4.0.0) (2022-11-20)


### ⚠ BREAKING CHANGES

* automatically verify deep mock invocations
* drop support for spock 1.3

### Features

* automatically verify deep mock invocations ([906b89f](https://github.com/joke/spock-deepmock/commit/906b89fe6fe6eedc7914ebdb45dfd1d53e0b5ca7))
* include dsl ([be91f3e](https://github.com/joke/spock-deepmock/commit/be91f3e3416c6efe09da909738b858be11a8aaa7))


### Code Refactoring

* drop support for spock 1.3 ([a1c8add](https://github.com/joke/spock-deepmock/commit/a1c8add54cb8edae8fca9c41706691abf56e7855))

## [3.0.0](https://github.com/joke/spock-deepmock/compare/2.0.1...v3.0.0) (2022-06-12)


### ⚠ BREAKING CHANGES

* remove trasitive dependencies on spock

### Bug Fixes

* remove trasitive dependencies on spock ([5e69747](https://github.com/joke/spock-deepmock/commit/5e697472605a6b127e4c379deb3be3f87d459a6c))
