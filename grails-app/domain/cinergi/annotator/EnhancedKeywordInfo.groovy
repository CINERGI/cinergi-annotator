package cinergi.annotator

class EnhancedKeywordInfo {
    String ontologyId
    String term
    String category
    String type
    // BindingFormat('yyyy-MM-dd\'T\'HH:mm:ss\'Z\'')
    String lastChangedDate

    static constraints = {
    }

    static mapping = {
        version false
        ontologyId attr: "id"

    }
}
