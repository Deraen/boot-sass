# boot-less
[![Clojars Project](http://clojars.org/deraen/boot-less/latest-version.svg)](http://clojars.org/deraen/boot-less)

[Boot](https://github.com/boot-clj/boot) task to compile Less.

* Provides the `less` task
* For each `.main.less` in fileset creates equivalent `.css` file.
* Uses [Less4j](https://github.com/SomMeri/less4j) Java implementation of Less compiler

## Usage

```clj
[s source-map  bool "Create source-map for compiled CSS."
 c compression bool "Compress compiled CSS using simple compression."]
```

## Features

- Load imports from classpath
  - Loading order. `@import "{name}";` at `{path}`.
    1. check if file `{path}/{name}.less` exists
    2. try `(io/resource "{name}.less")`
    3. try `(io/resource "{path}/{name}.less")`
    4. check if webjars asset map contains `{name}`
  - You should be able to depend on `[org.webjars/bootstrap "3.3.1"]`
    and use `@import "bootstrap/less/bootstrap";`
  - Use boot debug to find what is being loaded:
    `boot -vvv less`

## License

Copyright © 2014 Juho Teperi

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
