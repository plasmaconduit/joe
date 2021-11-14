package com.plasmaconduit.joe.internals;

import com.plasmaconduit.joe.BadArg;
import com.plasmaconduit.joe.IO;
import com.plasmaconduit.joe.ProcessId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Names {

    IO<Void> register(String name, ProcessId process);

    IO<Void> unregister(String name);

    IO<Optional<ProcessId>> whereis(String name);

    IO<List<String>> registered();

    static Names unsafe() {
        return new Impl(new HashMap<>());
    }

    final class Impl implements Names {

        final private  Map<String, ProcessId> registry;

        public Impl(Map<String, ProcessId> registry) {
            this.registry = registry;
        }

        @Override
        public IO<Void> register(String name, ProcessId process) {
            return IO.sync_(() -> {
                synchronized (this) {
                    registry.compute(name, (k, v) -> {
                        if (v == null) {
                            return process;
                        } else {
                            throw new BadArg();
                        }
                    });
                }
            });
        }

        @Override
        public IO<Void> unregister(String name) {
            return IO.sync_(() -> {
                synchronized (this) {
                    registry.compute(name, (k, v) -> {
                        if (v != null) {
                            return null;
                        } else {
                            throw new BadArg();
                        }
                    });
                }
            });
        }

        @Override
        public IO<Optional<ProcessId>> whereis(String name) {
            return IO.sync(() -> Optional.ofNullable(registry.get(name)));
        }

        @Override
        public IO<List<String>> registered() {
            return IO.sync(() -> {
                synchronized (this) {
                    return List.copyOf(registry.keySet());
                }
            });

        }

    }

}
