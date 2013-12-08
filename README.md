# Lein-Frodo

A Leiningen plugin to start a web server (backed by [http-kit][1])
easily via configuration in Nomad, and also to start and connect to a
ClojureScript REPL.

[1]:http://http-kit.org/index.html

## Dependency

Include `lein-frodo` as a plugin in your `project.clj`:

    :plugins [[jarohen/lein-frodo "0.2.4"]]

## Why?

Well, I already use **Nomad** for most of my configuration. I
configure various environments using Nomad's environment
functionality, and wanted the web server to be configured in the same
way.

In each project, I found myself writing the same boilerplate startup
code - reading the port details from my configuration in order to
start nREPL and a web server on the relevant ports for the
environment.

With Frodo, it's possible to start web applications with:

    NOMAD_ENV=<<environment>> lein frodo

and have the ports vary by environment.

For more details about what's possible with Nomad, please see
[its project page][1].

(I did use [lein-ring][2] for a bit but, while it is a great plugin,
I'd much prefer all my configuration in one place - hence taking the
time to write this!)

[1]: https://github.com/james-henderson/nomad
[2]: https://github.com/weavejester/lein-ring

### "About the name...?"

Yes, it's corny, I'm sorry! I did toy with *lein-nomad-ring*, and
various permutations, but none of them really seemed to bring together
Ring and Nomad in the way *lein-frodo* did. Alternatives gratefully
received!

## Getting started

First, create a Nomad configuration file somewhere on your
classpath, and add a `:frodo/config` key, as follows:

*project-root*/resources/config/nomad-config.edn:

```clojure
{:frodo/config {:nrepl {:port 7888}
                :web {:port 3000
                      :handler myapp.web/handler}}}
```
	 
Then, add an entry in your `project.clj` to tell Frodo where your
Nomad file is:

```clojure
:frodo/config-resource "config/nomad-config.edn"
```

To run the Ring server, run:

    lein frodo
	
## "You say you use multiple environments?"

Yes - you can do this in the traditional Nomad way:

*project-root*/resources/config/nomad-config.edn:

```clojure
{:nomad/environments {"dev"
	                  {:frodo/config {:nrepl {:port 7888}
                                      :web {:port 3000}}}

                      "prod"
                      {:frodo/config {:nrepl {:port nil}
                                      :web {:port 4462}}}}}
```										

Then, start your application with either:

    NOMAD_ENV=dev lein frodo
	
or:
	
	NOMAD_ENV=prod lein frodo

This is just the simplest multiple environment configuration - there
are many more possibilities on the [Nomad project page][1].

## ClojureScript REPL

Frodo also allows you to start and connect to a ClojureScript
REPL. Frodo's CLJS support is a lightweight wrapper around Chas
Emerick's excellent [Austin](https://github.com/cemerick/austin)
library.

Setting this up in Frodo is achieved with 4 easy steps:

1. Include `:cljs-repl? true` in your nREPL configuration, as follows:

   ```clojure
   {:nomad/environments {"dev"
                         {:frodo/config {:nrepl {:port 7888
         				                         :cljs-repl? true}
                                         :web {:port 3000}}}}}
   ```
										  
2. Include a snippet of JS in your web page to connect your browser to
   the REPL. The
   `(frodo/repl-connect-js)` function
   provides the JS - you just have to include it in the &lt;body&gt;
   tag.
   
   You can do this with Hiccup:
   ```clojure
   (:require [frodo :refer [repl-connect-js])
   ...
   [:script (repl-connect-js)]
   ```

   (If the CLJS REPL is disabled, `repl-connect-js` returns `nil`, so
   you can leave this in even when the CLJS REPL is disabled - e.g. in
   production)

3. Connect to your usual Clojure REPL, and run `(require 'frodo)`,
   then `(frodo/cljs-repl)` to turn it into a CLJS REPL. (Type
   `:cljs/quit` to exit back to the Clojure REPL)
   
4. Refresh your browser window to connect it to your REPL.

You should then be able to run commands in the CLJS REPL as you would
do with any other Clojure REPL. A good smoke test is any one of the
following:

```clojure
(js/alert "Hello world!")
(js/console.log "Hello world!")
(-> js/document .-body (.setAttribute "style" "background:green"))
```

I have also tested this in Emacs - most of the usual nREPL keybindings
work fine with CLJS REPLs. The only exception I've found so far (as of
2013-09-14) is `M-.` and `M-,` - jump to (and back from) a
declaration.

For more information about Austin and CLJS REPLs in general, Chas has
written a [great tutorial][1], a [sample project][2] and a
[screencast][3].

[1]: https://github.com/cemerick/austin/blob/master/README.md
[2]: https://github.com/cemerick/austin/tree/master/browser-connected-repl-sample
[3]: http://www.youtube.com/watch?v=a1Bs0pXIVXc&feature=youtu.be

## CLJX support

As of 0.2.4, Frodo transparently supports
[CLJX](https://github.com/lynaghk/cljx) - if you have a `:cljx` key in
your `project.clj`, Frodo will ensure the necessary middleware is in
place.

See [CLJX](https://github.com/lynaghk/cljx)'s project page for more
details on how to use it.

## Future features?

* **SSL**? I'm not sure how many people use SSL within Clojure - from
  what I can tell most people sit it behind an nginx/httpd proxy. If
  you want to include SSL support, please feel free to submit a pull
  request.
* **uberjar/uberwar**? Again, I don't use these, but if you do and you
  care enough to write a patch, it'll be gratefully received!

## Changes

### 0.2.4

No breaking changes - CLJX support

### 0.2.3

Upstream dependency updates.

### 0.2.2

Minor bugfix - creating 'target/classes' directory if it doesn't exist

### 0.2.1

Fixed a bug whereby requiring `cemerick.austin.repls` when it wasn't
linked threw exceptions. Now use `(frodo/repl-connect-js)` which will
work if CLJS REPLs are turned on but won't error if they're turned off
(e.g. in prod)

### 0.2.0

No breaking changes. Frodo now uses [**http-kit**][1] to provide the
web server. http-kit is compatible with ring and ring-jetty, so you
shouldn't have any trouble.

The `:handler` key has been moved inside the `:web` map in the config
file. The original location still works, but it has been deprecated,
and will be removed in 0.3.0.

Also, the nREPL port is now saved to `target/repl-port` to be
consistent with `lein repl`.

### 0.1.2

No breaking changes. Added CLJS REPL functionality.

### 0.1.1

No breaking changes. Better error handling if the user doesn't specify
a handler.

### 0.1.0

Initial release.

## Pull requests/bug reports/feedback etc?

Yes please, much appreciated! Please submit via GitHub in the
traditional manner. (Or, if it fits into 140 chars, you can tweet
[@jarohen](https://twitter.com/jarohen))

## Thanks

* Big thanks to [James Reeves](https://github.com/weavejester) for his
  **lein-ring** project (amongst everything else!) from which I have
  plundered a couple of ideas and snippets of code. Also, thanks for the
  general help and advice.
* Also, thanks to [Chas Emerick](https://github.com/cemerick) for his
  **Austin** CLJS REPL library.
* Thanks to [Kevin Lynagh](https://github.com/lynaghk) for his
  **CLJX** CLJ/CLJS crossovers library

## License

Copyright © 2013 James Henderson

Distributed under the Eclipse Public License, the same as Clojure.
