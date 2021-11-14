package com.plasmaconduit.joe.internals;

import com.plasmaconduit.joe.IO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Mailbox {

    IO<Void> enqueue(Object message);

    <T> IO<T> scan(Function<Object, IO<T>> handler);

    static Mailbox fresh() {
        return new Impl(new ArrayList<>());
    }

    final class Impl implements Mailbox {

        final List<Object> messages;

        Function<Object, Runnable> waiter;

        public Impl(List<Object> messages) {
            this.messages = messages;
            this.waiter = null;
        }

        @Override
        public IO<Void> enqueue(Object message) {
            return IO.sync_(() -> {
                var result = synchronizedEnqueue(message);
                if (result != null) {
                    result.run();
                }
            });
        }

        private Runnable synchronizedEnqueue(Object message) {
            synchronized (this) {
                if (waiter == null) {
                    messages.add(message);
                    return null;
                } else {
                    var result = waiter.apply(message);
                    if (result != null) {
                        waiter = null;
                        return result;
                    } else {
                        messages.add(message);
                        return null;
                    }
                }
            }
        }

        @Override
        public <T> IO<T> scan(Function<Object, IO<T>> handler) {
            return new IO<>((next) -> {
                var result = synchronizedScan(handler, next);
                if (result != null) {
                    result.unsafe().accept(next);
                }
            });
        }

        private <T> IO<T> synchronizedScan(Function<Object, IO<T>> handler, Consumer<IO.Result<T>> next) {
            synchronized (this) {
                for (var i = 0; i < messages.size(); i++) {
                    var message = messages.get(i);
                    var result = handler.apply(message);
                    if (result != null) {
                        messages.remove(i);
                        return result;
                    }
                }
                waiter = (message) -> {
                    var result = handler.apply(message);
                    if (result != null) {
                        return () -> result.unsafe().accept(next);
                    } else {
                        return null;
                    }
                };
                return null;
            }
        }

    }

}
