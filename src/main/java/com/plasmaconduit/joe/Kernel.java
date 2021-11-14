package com.plasmaconduit.joe;

import com.plasmaconduit.joe.internals.*;

import java.util.List;
import java.util.Optional;

public interface Kernel {

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

    IO<Void> exit(ProcessId target, ProcessId requester, Object reason);

    IO<Void> setExitTrapStatus(ProcessId target, boolean status);

    IO<Void> println(String message);

    static Kernel makeDefault() {
        var names = Names.unsafe();
        var links = Links.unsafe();
        var monitors = Monitors.unsafe();
        var processes = Processes.unsafe();
        return new Kernel.Impl(names, links, monitors, processes);
    }

    final class Impl implements Kernel {

        final private Names names;

        final private Links links;

        final private Monitors monitors;

        final private Processes processes;

        public Impl(Names names, Links links, Monitors monitors, Processes processes) {
            this.names = names;
            this.links = links;
            this.monitors = monitors;
            this.processes = processes;
        }

        @Override
        public IO<Void> register(String name, ProcessId process) {
            return names.register(name, process);
        }

        @Override
        public IO<Void> unregister(String name) {
            return names.unregister(name);
        }

        @Override
        public IO<Optional<ProcessId>> whereis(String name) {
            return names.whereis(name);
        }

        @Override
        public IO<List<String>> registered() {
            return names.registered();
        }

        @Override
        public IO<Void> link(ProcessId a, ProcessId b) {
            return links.link(a, b);
        }

        @Override
        public IO<Void> unlink(ProcessId a, ProcessId b) {
            return links.unlink(a, b);
        }

        @Override
        public IO<MonitorId> monitor(ProcessId watchingProcessId, ProcessId watchedProcessId) {
            return monitors.monitor(watchingProcessId, watchedProcessId);
        }

        @Override
        public IO<Void> demonitor(MonitorId monitorId) {
            return monitors.demonitor(monitorId);
        }

        @Override
        public IO<ProcessId> spawn(Program program) {
            return processes.spawn(program, new OSFactory.Impl(this));
        }

        @Override
        public IO<Void> send(ProcessId to, Object message) {
            return processes.send(to, message);
        }

        @Override
        public IO<Void> exit(ProcessId target, ProcessId requester, Object reason) {
            return processes.exit(target, requester, reason);
        }

        @Override
        public IO<Void> setExitTrapStatus(ProcessId target, boolean status) {
            return processes.setExitTrapStatus(target, status);
        }

        @Override
        public IO<Void> println(String message) {
            return IO.sync_(() -> System.out.println(message));
        }

    }

}
