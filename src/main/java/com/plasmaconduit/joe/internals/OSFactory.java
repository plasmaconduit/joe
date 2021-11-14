package com.plasmaconduit.joe.internals;

import com.plasmaconduit.joe.Kernel;
import com.plasmaconduit.joe.OS;
import com.plasmaconduit.joe.ProcessId;

public interface OSFactory {

    OS make(ProcessId processId, Mailbox mailbox);

    final class Impl implements OSFactory {

        final private Kernel kernel;

        public Impl(Kernel kernel) {
            this.kernel = kernel;
        }

        @Override
        public OS make(ProcessId processId, Mailbox mailbox) {
            return new OS.Impl(kernel, processId, mailbox);
        }

    }

}
