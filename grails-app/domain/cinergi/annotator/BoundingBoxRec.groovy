package cinergi.annotator

class BoundingBoxRec {
    LatLong southwest
    LatLong northeast

    def toMap() {
        return ['southwest': southwest.toMap(), 'northeast': northeast.toMap()]
    }
    static embedded = ['southwest', 'northeast']
    static mapping = {
        version false
    }
}
