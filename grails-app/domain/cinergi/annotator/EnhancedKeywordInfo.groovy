package cinergi.annotator

class EnhancedKeywordInfo {
    String ontologyId
    String term
    String category
    String type
    // BindingFormat('yyyy-MM-dd\'T\'HH:mm:ss\'Z\'')
    String lastChangedDate

    def toMap() {
        def map = ['term'           : term, 'category': category, 'type': type, 'ontologyId': ontologyId,
                   'lastChangedDate': lastChangedDate]
        map
    }
    static constraints = {
    }

    static mapping = {
        version false
        ontologyId attr: "id"

    }
}
