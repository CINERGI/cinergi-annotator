package cinergi.annotator

import org.bson.types.ObjectId

class SourceRec {
    static mapWith = "mongo"
    ObjectId id
    Map sourceInformation

    static constraints = {
    }
    static mapping = {
        collection "sources"
        database "discotest"
        version false
    }
}
