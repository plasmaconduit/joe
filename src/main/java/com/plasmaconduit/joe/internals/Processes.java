package com.plasmaconduit.joe.internals;

import com.plasmaconduit.joe.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Processes {

    IO<ProcessId> spawn(Program program, OSFactory osFactory);

    IO<Void> send(ProcessId to, Object message);

    IO<Void> exit(ProcessId target, ProcessId requester, Object reason);

    IO<Void> setExitTrapStatus(ProcessId target, boolean status);

    static Processes unsafe() {
        return new Impl(Executors.newSingleThreadExecutor());
    }

    record Process(
        ExecutorService pool,
        OS os,
        Mailbox mailbox,
        AtomicBoolean trappingExits,
        Program program
    ) implements Runnable {

        @Override
        public void run() {
            program.run(os).unsafe().accept((io) -> {
                switch (io) {
                    case IO.Result.Ok<Program.Result> ok -> {
                        if (ok.value() instanceof Program.Result.Become next) {
                            pool.execute(new Process(pool, os, mailbox, trappingExits, next.program()));
                        }
                    }
                    case IO.Result.Error<Program.Result> error -> {
                        throw new RuntimeException("TODO: Notify process failed with error");
                    }
                }
            });
        }

    }

    final class Impl implements Processes {

        final ExecutorService pool;

        final Map<ProcessId, Process> registry;

        int nextId;

        public Impl(ExecutorService pool) {
            this.pool = pool;
            this.registry = new HashMap<>();
        }

        @Override
        public IO<ProcessId> spawn(Program program, OSFactory osFactory) {
            return IO.sync(() -> {
                synchronized (this) {
                    var processId = ProcessId.unsafe(nextId++);
                    var mailbox = Mailbox.fresh();
                    var trappingExits = new AtomicBoolean(false);
                    var os = osFactory.make(processId, mailbox);
                    var process = new Process(pool, os, mailbox, trappingExits, program);
                    registry.put(processId, process);
                    pool.execute(process);
                    return processId;
                }
            });
        }

        @Override
        public IO<Void> send(ProcessId to, Object message) {
            return new IO<>((next) -> {
                var process = registry.get(to);
                if (process != null) {
                    process.mailbox.enqueue(message).unsafe().accept(next);
                }
            });
        }

        @Override
        public IO<Void> exit(ProcessId target, ProcessId requester, Object reason) {
            throw new RuntimeException("TODO");
        }

        @Override
        public IO<Void> setExitTrapStatus(ProcessId target, boolean status) {
            return IO.sync_(() -> {
                var process = registry.get(target);
                if (process != null) {
                    process.trappingExits.set(status);
                }
            });
        }

    }

}
