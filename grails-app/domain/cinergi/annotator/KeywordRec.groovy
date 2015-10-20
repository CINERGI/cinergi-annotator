package cinergi.annotator

class KeywordRec {
    String term
    List<EntityInfoRec> entityInfos


    def toMap() {
        def map = ['term'       : term,
                   'entityInfos': entityInfos.collect { EntityInfoRec e -> e.toMap() }]
        map
    }

    def removeByOntologyId(String ontologyId) {
        def toBeRemoved = []
        entityInfos.each { EntityInfoRec ei ->
            if (ei.ontologyId == ontologyId) {
                toBeRemoved << ei
            }
        }
        toBeRemoved.each { EntityInfoRec ei -> entityInfos.remove(ei) }
    }

    static embedded = ['entityInfos']
    static mapping = {
        version false
    }
}
