package com.plasmaconduit.joe;

import java.util.function.Function;


@FunctionalInterface
public interface Program {

    sealed interface Result {
        record Terminate() implements Result { }
        record Become(Program program) implements Result { }
    }

    IO<Result> run(OS os);

    record ForeverProgram(Function<OS, IO<Void>> program) implements Program {
        @Override
        public IO<Result> run(OS os) {
            return program.apply(os).as(become(this));
        }
    }

    static Program forever(Function<OS, IO<Void>> program) {
        return new ForeverProgram(program);
    }

    static Result terminate() {
        return new Result.Terminate();
    }

    static IO<Result> terminateAsync() {
        return IO.of(terminate());
    }

    static Result become(Program program) {
        return new Result.Become(program);
    }

    static IO<Result> becomeAsync(Program program) {
        return IO.of(become(program));
    }

}
