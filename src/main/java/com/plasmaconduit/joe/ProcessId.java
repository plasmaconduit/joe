package com.plasmaconduit.joe;

public interface ProcessId {

    final record Impl(int value) implements ProcessId {

        @Override
        public String toString() {
            return String.format("ProcessId(%s)", value);
        }

        @Override
        public boolean equals(Object other) {
            return switch (other) {
                case Impl o -> value == o.value;
                default -> false;
            };
        }

        @Override
        public int hashCode() {
            return value;
        }

    }

    static ProcessId unsafe(int value) {
        return new Impl(value);
    }

}
