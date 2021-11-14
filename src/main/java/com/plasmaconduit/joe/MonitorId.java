package com.plasmaconduit.joe;

public interface MonitorId {

    final record Impl(int value) implements MonitorId {

        @Override
        public String toString() {
            return String.format("MonitorId(%s)", value);
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

    static MonitorId unsafe(int value) {
        return new Impl(value);
    }

}
