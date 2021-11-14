package com.plasmaconduit.joe;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record IO<T>(Consumer<Consumer<Result<T>>> unsafe) {

    public sealed interface Result<T> {
        record Ok<T>(T value) implements Result<T> {}
        record Error<T>(Exception e) implements Result<T> {}
    }

    public static <T> IO<T> of(T value) {
        return new IO<>((next) -> next.accept(new Result.Ok<>(value)));
    }

    public static IO<Void> sync_(Runnable op) {
        return new IO<>((next) -> {
            op.run();
            next.accept(new Result.Ok<>(null));
        });
    }

    public static <T> IO<T> sync(Supplier<T> op) {
        return new IO<>((next) -> next.accept(new Result.Ok<>(op.get())));
    }

    public <R> IO<R> map(Function<T, R> f) {
        return new IO<>((next) ->
            unsafe.accept((value) -> {
                switch (value) {
                    case Result.Ok<T> ok -> next.accept(new Result.Ok<>(f.apply(ok.value)));
                    case Result.Error<T> error -> next.accept((Result<R>) error);
                };
            })
        );
    }

    public <R> IO<R> flatMap(Function<T, IO<R>> f) {
        return new IO<>((next) ->
            unsafe.accept((value) -> {
                switch (value) {
                    case Result.Ok<T> ok -> f.apply(ok.value).unsafe().accept(next);
                    case Result.Error<T> error -> next.accept((Result<R>) error);
                }
            })
        );
    }

    public IO<T> flatTap(Function<T, IO<Void>> f) {
        return new IO<>((next) ->
            unsafe.accept((value) -> {
                switch (value) {
                    case Result.Ok<T> ok -> f.apply(ok.value).unsafe().accept((ignored) -> next.accept(value));
                    case Result.Error<T> error -> next.accept(error);
                }
            })
        );
    }

    public <R> IO<R> then(IO<R> next) {
        return flatMap((ignored) -> next);
    }

    public <R> IO<R> as(R r) {
        return map((o) -> r);
    }

}
