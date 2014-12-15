# boot-cljx
[![Clojars Project](http://clojars.org/deraen/boot-cljx/latest-version.svg)](http://clojars.org/deraen/boot-cljx)

[Boot](https://github.com/boot-clj/boot) task to compile Cljx.

* Provides the `cljx` task
* Reads `.cljx` files from `:src-paths` and creates corresponding `.clj`
and `.cljs` files. Resulting files are available to others tasks through
`src-files`.

## Use

```clojure
; All files (.clj, .cljx, .cljs) could be on the same directory,
; but I like to have separate directories per filetype.
(set-env! :src-paths #{"src/cljx" "src/clj" "src/cljs"})

; Run cljx before cljs
; $ boot cljx cljs ...
(deftask package
  "Package the app"
  []
  (comp
    (cljx)
    (cljs)
    ...))
```

## TODO

- [ ] Handle errors
  - Does cljx throw any errors?
- [ ] What options should there be?
  - No options needed for now
