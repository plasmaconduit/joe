# Joe

Joe is a purely functional actor system written in java17

> Make it work, then make it beautiful, then if you really, really have to,
> make it fast. 90 percent of the time, if you make it beautiful, it will
> already be fast. So really, just make it beautiful!
> -- Joe Armstrong

## What?

Joe is purely functional port of the most practical actor system ever made,
Elang/OTP. Like Erlang, Joe lets you spin up millions of lightweight processes
that can coordinate with each other via message passing.

## Why pure FP?

Unlike BeamVM, Java's JVM doesn't support lightweight processes yet. Java will
get lightweight processes as part of the Java Loom project but until Loom gets
released we need a way to efficiently suspend, resume and shift running
computations to different execution threads and having a purely functional
effect system allows us to do all of those.