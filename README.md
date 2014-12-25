# boot-less
[![Clojars Project](http://clojars.org/deraen/boot-less/latest-version.svg)](http://clojars.org/deraen/boot-less)

[Boot](https://github.com/boot-clj/boot) task to compile Less.

* Provides the `less` task
* Reads `.less` files from fileset...
* For each `.less` file not starting with \_ (underscore), creates
equivalent `.css` file.
* Uses Less.js through Java 1.8 Nashorn JS engine.

## Features

- Load imports from classpath
  - Loading order. `@import "foo";`
    1. check if `src/less/foo.less` exists
    2. try `(io/resource "foo.less")`
    3. try `(io/resource "src/less/foo.less")`
    4. check if webjars asset map contains `foo`
  - You should be able to depend on `[org.webjars/bootstrap "3.3.1"]`
    and use `@import "bootstrap/less/bootstrap";`

## TODO

- [ ] Update to latest LESS
- [ ] Handle errors
- [ ] Use non-bundled Less files
  - Webjars has less-node, but the files are not concatenated
  - Parhaps we'll need to package ourselves
  - https://github.com/nodyn/jvm-npm allows us to use Node modules
- [ ] Separate JS engine stuff to small library
- [x] How to read files from classpath
  - E.g. Bootstrap as maven dependency from Webjars
  - Create custom "less-nashorn" implementing fileManager.loadFile which tries to locate files in classpath (https://github.com/less/less.js/blob/master/lib/less-node/file-manager.js)
- [ ] Set main file manually
  - If no main file set, other files should start with \_
  - if set, other files can be named in any way
