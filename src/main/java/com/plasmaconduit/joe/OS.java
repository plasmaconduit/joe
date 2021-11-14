package com.plasmaconduit.joe;

import com.plasmaconduit.joe.internals.Mailbox;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface OS {

    ProcessId self();

    <T> IO<T> receive(Function<Object, IO<T>> handler);

    IO<Void> register(String name, ProcessId process);

    IO<Void> unregister(String name);

    IO<Optional<ProcessId>> whereis(String name);

    IO<List<String>> registered();

    IO<Void> link(ProcessId a, ProcessId b);

    IO<Void> unlink(ProcessId a, ProcessId b);

    IO<MonitorId> monitor(ProcessId watchingProcessId, ProcessId watchedProcessId);

    IO<Void> demonitor(MonitorId monitorId);

    IO<ProcessId> spawn(Program program);

    IO<Void> send(ProcessId to, Object message);

    IO<Void> exit(ProcessId target, Object reason);

    IO<Void> setExitTrapStatus(ProcessId target, boolean status);

    IO<Void> println(String message);

    record Impl(Kernel kernel, ProcessId processId, Mailbox mailbox) implements OS {

        @Override
        public ProcessId self() {
            return processId;
        }

        @Override
        public <T> IO<T> receive(Function<Object, IO<T>> handler) {
            return mailbox.scan(handler);
        }

        @Override
        public IO<Void> register(String name, ProcessId process) {
            return kernel.register(name, processId);
        }

        @Override
        public IO<Void> unregister(String name) {
            return kernel.unregister(name);
        }

        @Override
        public IO<Optional<ProcessId>> whereis(String name) {
            return kernel.whereis(name);
        }

        @Override
        public IO<List<String>> registered() {
            return kernel.registered();
        }

        @Override
        public IO<Void> link(ProcessId a, ProcessId b) {
            return kernel.link(a, b);
        }

        @Override
        public IO<Void> unlink(ProcessId a, ProcessId b) {
            return kernel.unlink(a, b);
        }

        @Override
        public IO<MonitorId> monitor(ProcessId watchingProcessId, ProcessId watchedProcessId) {
            return kernel.monitor(watchingProcessId, watchedProcessId);
        }

        @Override
        public IO<Void> demonitor(MonitorId monitorId) {
            return kernel.demonitor(monitorId);
        }

        @Override
        public IO<ProcessId> spawn(Program program) {
            return kernel.spawn(program);
        }

        @Override
        public IO<Void> send(ProcessId to, Object message) {
            return kernel.send(to, message);
        }

        @Override
        public IO<Void> exit(ProcessId target, Object reason) {
            return kernel.exit(target, self(), reason);
        }

        @Override
        public IO<Void> setExitTrapStatus(ProcessId target, boolean status) {
            return kernel.setExitTrapStatus(target, status);
        }

        @Override
        public IO<Void> println(String message) {
            return kernel.println(message);
        }

    }

}
