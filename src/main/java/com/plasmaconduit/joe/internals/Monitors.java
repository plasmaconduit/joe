package com.plasmaconduit.joe.internals;

import com.plasmaconduit.joe.IO;
import com.plasmaconduit.joe.MonitorId;
import com.plasmaconduit.joe.ProcessId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Monitors {

    final record Monitor(MonitorId monitorId, ProcessId processId) { }

    IO<MonitorId> monitor(ProcessId watchingProcessId, ProcessId watchedProcessId);

    IO<Void> demonitor(MonitorId monitorId);

    IO<List<Monitor>> activate(ProcessId processId);

    static Monitors unsafe() {
        return new Impl(new HashMap<>(), new HashMap<>(), 0);
    }

    final class Impl implements Monitors {

        final Map<ProcessId, List<Monitor>> registry;

        final Map<MonitorId, ProcessId> index;

        int nextId;

        public Impl(
            Map<ProcessId, List<Monitor>> registry,
            Map<MonitorId, ProcessId> index,
            int nextId
        ) {
            this.registry = registry;
            this.index = index;
            this.nextId = nextId;
        }

        @Override
        public IO<MonitorId> monitor(ProcessId watchingProcessId, ProcessId watchedProcessId) {
            return IO.sync(() -> {
                synchronized (this) {
                    var monitorId = MonitorId.unsafe(nextId++);
                    var monitor = new Monitor(monitorId, watchingProcessId);
                    index.put(monitorId, watchedProcessId);
                    var monitors = registry.get(watchedProcessId);
                    if (monitors == null) {
                        var newMonitors = new ArrayList<Monitor>();
                        newMonitors.add(monitor);
                        registry.put(watchedProcessId, newMonitors);
                    } else {
                        monitors.add(monitor);
                    }
                    return monitorId;
                }
            });
        }

        @Override
        public IO<Void> demonitor(MonitorId monitorId) {
            return IO.sync_(() -> {
                synchronized (this) {
                    index.computeIfPresent(monitorId, (k, watchedProcessId) -> {
                        var monitors = registry.get(watchedProcessId);
                        if (monitors != null) {
                            monitors.removeIf((m) -> m.monitorId == monitorId);
                        }
                        return null;
                    });
                }
            });
        }

        @Override
        public IO<List<Monitor>> activate(ProcessId processId) {
            return IO.sync(() -> {
                synchronized (this) {
                    var monitors = registry.get(processId);
                    registry.remove(processId);
                    for (var monitor : monitors) {
                        index.remove(monitor.monitorId);
                    }
                    return monitors;
                }
            });
        }

    }

}
