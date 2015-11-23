package io.apptik.comm.jus;

import io.apptik.comm.jus.toolbox.Utils;

public class Marker {
    public final String name;
    public final long threadId;
    public final String threadName;
    public final long time;

    public Marker(String name, long threadId, String threadName, long time) {
        Utils.checkNotNull(name, "name==null");
        this.name = name;
        this.threadId = threadId;
        this.threadName = threadName;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Marker{" +
                "name='" + name + '\'' +
                ", threadId=" + threadId +
                ", threadName='" + threadName + '\'' +
                ", time=" + time +
                '}';
    }

    public interface MarkerFilter {
        boolean apply(Marker marker);
    }

    public static class SimpleMarkerFilter implements MarkerFilter {
        final String val;
        final int type;
        int exact = 1;
        int contains = 2;
        int matches = 3;


        private SimpleMarkerFilter(String val, int type) {
            this.val = val;
            this.type = type;
        }

        public static SimpleMarkerFilter withName(String name) {
            return new SimpleMarkerFilter(name, 1);
        }

        public SimpleMarkerFilter containsName(String name) {
            return new SimpleMarkerFilter(name, 2);
        }

        public SimpleMarkerFilter matchesName(String name) {
            return new SimpleMarkerFilter(name, 3);
        }

        @Override
        public boolean apply(Marker marker) {
            if (type == exact) {
                return marker.name.equals(val);
            } else if (type == contains) {
                return marker.name.contains(val);
            } else if (type == matches) {
                return marker.name.matches(val);
            }
            return false;
        }
    }
}
