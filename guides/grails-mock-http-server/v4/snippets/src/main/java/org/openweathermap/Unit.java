package org.openweathermap;

public enum Unit {
    Standard, Imperial, Metric;

    public static Unit unitWithString(String str) {
        if ( str != null) {
            if ( str.toLowerCase().equals("metric") ) {
                return Unit.Metric;
            } else if ( str.toLowerCase().equals("imperial") ) {
                return Unit.Imperial;
            }
        }
        return Unit.Standard;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
