# boot-less
[![Clojars Project](http://clojars.org/deraen/boot-less/latest-version.svg)](http://clojars.org/deraen/boot-less)

[Boot](https://github.com/boot-clj/boot) task to compile Less.

* Provides the `less` task
* Reads `.less` files from fileset...
* For each `.main.less` creates equivalent `.css` file.
* Uses Less.js through Java 1.8 Nashorn JS engine.
  * No Rhino support for now, thus 1.8 is required

## Features

- Load imports from classpath
  - Loading order. `@import "{name}";` at `{path}`.
    1. check if file `{path}/{name}.less` exists
    2. try `(io/resource "{name}.less")`
    3. try `(io/resource "{path}/{name}.less")`
    4. check if webjars asset map contains `{name}`
  - You should be able to depend on `[org.webjars/bootstrap "3.3.1"]`
    and use `@import "bootstrap/less/bootstrap";`

## TODO

- [ ] Update to latest LESS
  - Blocked. [less/less.js#2316](https://github.com/less/less.js/issues/2316)
- [x] Handle errors
  - [x] Trying to import non-existant file
  - [x] Missing closing `}`
  - [x] Missing `;` between declarations
  - [ ] Referencing non-existant variable
- [ ] Separate JS engine stuff to small library

## License

Copyright © 2014 Juho Teperi<br>
Copyright © 2013 Montoux Ltd. ([Lein-less](https://github.com/montoux/lein-less))

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
