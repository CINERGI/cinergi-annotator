package cinergi.annotator

class LatLong {
    String lat
    String lng

    def toMap() {
        return ['lat': lat, 'lng': lng]
    }
    static mapping = {
        version false
    }
}
