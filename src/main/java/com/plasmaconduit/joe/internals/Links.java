package com.plasmaconduit.joe.internals;

import com.plasmaconduit.joe.IO;
import com.plasmaconduit.joe.ProcessId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Links {

    IO<Void> link(ProcessId a, ProcessId b);

    IO<Void> unlink(ProcessId a, ProcessId b);

    IO<List<ProcessId>> activate(ProcessId processId);

    static Links unsafe() {
        return new Impl(new HashMap<>());
    }

    final class Impl implements Links {

        final private Map<ProcessId, List<ProcessId>> registry;

        public Impl(Map<ProcessId, List<ProcessId>> registry) {
            this.registry = registry;
        }

        @Override
        public IO<Void> link(ProcessId a, ProcessId b) {
            return IO.sync_(() -> {
                synchronized (this) {
                    setDirectionalLink(a, b);
                    setDirectionalLink(b, a);
                }
            });
        }

        @Override
        public IO<Void> unlink(ProcessId a, ProcessId b) {
            return IO.sync_(() -> {
                synchronized (this) {
                    removeDirectionalLink(a, b);
                    removeDirectionalLink(b, a);
                }
            });
        }

        @Override
        public IO<List<ProcessId>> activate(ProcessId processId) {
            return IO.sync(() -> {
                synchronized (this) {
                    var links = registry.get(processId);
                    if (links.isEmpty()) {
                        return links;
                    } else {
                        for (var link : links) {
                            removeDirectionalLink(link, processId);
                        }
                        registry.remove(processId);
                        return List.copyOf(links);
                    }
                }
            });
        }

        private void setDirectionalLink(ProcessId a, ProcessId b) {
            var v = registry.get(a);
            if (v == null) {
                var list = new ArrayList<ProcessId>();
                list.add(b);
                registry.put(a, list);
            } else {
                v.add(b);
            }
        }

        private void removeDirectionalLink(ProcessId a, ProcessId b) {
            var v = registry.get(a);
            if (v != null) {
                v.remove(b);
            }
        }

    }

}
