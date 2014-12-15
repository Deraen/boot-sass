# boot-less
[![Clojars Project](http://clojars.org/deraen/boot-less/latest-version.svg)](http://clojars.org/deraen/boot-less)

[Boot](https://github.com/boot-clj/boot) task to compile Less.

* Provides the `less` task
* Reads `.less` files from fileset...
* For each `.less` file not starting with \_ (underscore), creates
equivalent `.css` file.
* Uses Less.js through Java 1.8 Nashorn JS engine.

## TODO

- [ ] Update to latest LESS
- [ ] Handle errors
- [ ] Use non-bundled Less files
  - Webjars has less-node, but the files are not concatenated
  - Parhaps we'll need to package ourselves
- [ ] Separate JS engine stuff to small library
- [ ] How to read files from classpath
  - E.g. Bootstrap as maven dependency from Webjars
