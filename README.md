# Lein-Frodo

A Leiningen plugin to start a Ring server easily via configuration in
Nomad.

## Dependency

Include `lein-frodo` as a plugin in your `project.clj`:

    :plugins [[jarohen/lein-frodo "0.1.0"]]

## Why?

Well, I already use **Nomad** for most of my configuration. I
configure various environments using Nomad's environment
functionality, and wanted the Ring server to be configured in the same
way.

In each project, I found myself writing the same boilerplate startup
code - reading the port details from my configuration in order to
start nREPL and Ring on the relevant ports for the environment.

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

    {:frodo/config {:nrepl {:port 7888}
                    :web {:port 3000}
                    :handler myapp.web/handler}}
	 
Then, add an entry in your `project.clj` to tell Frodo where your
Nomad file is:

    :frodo/config-resource "config/nomad-config.edn"

To run the Ring server, run:

    lein frodo
	
## "You say you use multiple environments?"

Yes - you can do this in the traditional Nomad way:

*project-root*/resources/config/nomad-config.edn:

    {:nomad/environments {"dev"
	                      {:frodo/config {:nrepl {:port 7888}
						                  :web {:port 3000}}}
						   
					      "prod"
	                      {:frodo/config {:nrepl {:port nil}
						                  :web {:port 4462}}}}}

Then, start your application with either:

    NOMAD_ENV=dev lein frodo
	
or:
	
	NOMAD_ENV=prod lein frodo

This is just the simplest multiple environment configuration - there
are many more possibilities on the [Nomad project page][1].

## Future features?

* **SSL**? I'm not sure how many people use SSL within Ring - from
  what I can tell most people sit it behind an nginx/httpd proxy. If
  you want to include SSL support, please feel free to submit a pull
  request.
* **uberjar/uberwar**? Again, I don't use these, but if you do and you
  care enough to write a patch, it'll be gratefully received!

## Changes

### 0.1.0

Initial release.

## Pull requests/bug reports/feedback etc?

Yes please, much appreciated! Please submit via GitHub in the
traditional manner. (Or, if it fits into 140 chars,
tweet [@jarohen](https://twitter.com/jarohen))

## License

Copyright © 2013 James Henderson

Distributed under the Eclipse Public License, the same as Clojure.
