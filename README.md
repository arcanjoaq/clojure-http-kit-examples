# Clojure http-kit Examples

## Configuration

Add 127.0.0.1 api.localhost into your /etc/hosts.

## Leiningen

### Running unit tests

To _run_ _unit tests_, execute:

```sh
lein test
```

### Running the application

To _run_ the _application_, type:

```sh
lein run
```

You can run a specific file by running:

```sh
lein run -m clojure-http-kit-examples.core
```

### Generating an uberjar

To _generate_ an _uberjar_, run:

```sh
lein uberjar
```

The _generated_ _Jar_ will be located in _target/uberjar_ directory.

## Executing the REPL

To _execute_ the _Lein REPL_, type:

```sh
lein repl
```

If you want to _load_ a file into the _REPL_, use _load-file_ function. Example:

```clj
(load-file "src/clojure-http-kit-examples/core.clj")
```

To invoke _main_ function you can do the following:

```clj
(clojure-http-kit-examples.core/-main)
```

## Formatting code

You can _format_ code by using the following command:

```sh
lein format # or
lein format-fix
```

## Running a Linter

You can use a _linter_ by typing:

```sh
lein lint # or
lein lint-fix
```

## Reader tools

**#spy/p**: _Pretty Print_ reader tool. Example:

```clj
#spy/p (my-form (System/getenv "USER"))
```

**#spy/d**: _Debug_ reader tool. Example:

```clj
#spy/d (my-form (System/getenv "USER"))
```

**#spy/t**: _Trace_ reader tool. Example:

```clj
#spy/t (my-form (System/getenv "USER"))
```

## Test watch mode

```sh
lein prism
```

## References

- [http-kit client](https://http-kit.github.io/client.html)
