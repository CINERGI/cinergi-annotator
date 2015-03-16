package cinergi.annotator

import groovy.transform.ToString

@ToString
class EntityInfoRec {
    String contentLocation
    String ontologyId
    int start
    int end
    String category

    def toMap() {
        return ['contentLocation': contentLocation, 'id': ontologyId, 'start': start,
                'end'            : end, 'category': category]
    }

    static mapping = {
        ontologyId attr: 'id'
        version false
    }
}
