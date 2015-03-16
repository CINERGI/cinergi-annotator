package cinergi.annotator

import groovy.transform.ToString

/**
 * Created by bozyurt on 3/9/15.
 */
@ToString
class BoundingBox {
    int id
    String latSouth
    String lngWest
    String latNorth
    String lngEast
    String text
    String type

    String prepKey() {
        return "$latSouth:$lngWest:$latNorth:$lngEast"
    }
}
