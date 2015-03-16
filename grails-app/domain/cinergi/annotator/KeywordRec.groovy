package cinergi.annotator

class KeywordRec {
    String term
    List<EntityInfoRec> entityInfos


    def toMap() {
        def map = ['term'       : term,
                   'entityInfos': entityInfos.collect { EntityInfoRec e -> e.toMap() }]
        map
    }

    static embedded = ['entityInfos']
    static mapping = {
        version false
    }
}
