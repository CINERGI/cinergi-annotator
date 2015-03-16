package cinergi.annotator

/**
 * Created by bozyurt on 3/11/15.
 */
class Utils {

    static boolean isChanged(BoundingBox refBB, String name, String value) {
        int idx = name.lastIndexOf('_')
        assert idx != -1
        String prefix = name.substring(0, idx)
        switch (prefix) {
            case "nl":
                return refBB.latNorth != value
            case "el":
                return refBB.lngEast != value
            case "sl":
                return refBB.latSouth != value
            case "wl":
                return refBB.lngWest != value
        }
        return false
    }

    static void updateBBParam(BoundingBox refBB, String name, String value) {
        int idx = name.lastIndexOf('_')
        assert idx != -1
        String prefix = name.substring(0, idx)
        switch (prefix) {
            case "nl":
                refBB.latNorth = value
            case "el":
                refBB.lngEast = value
            case "sl":
                refBB.latSouth = value
            case "wl":
                refBB.lngWest = value
        }
    }

    static boolean isNewId(name) {
        int idx = name.lastIndexOf('_')
        assert idx != -1
        String suffix = name.substring(idx + 1)
        return suffix.startsWith('n')
    }

    static int extractNewIdFromName(String name) {
        int idx = name.lastIndexOf('_')
        assert idx != -1
        String suffix = name.substring(idx + 1)
        if (suffix.startsWith('n')) {
            return suffix.substring(1) as int
        }
        return -1
    }

    static int extractIdFromName(String name) {
        int idx = name.lastIndexOf('_')
        assert idx != -1
        return name.substring(idx + 1) as int
    }
}
