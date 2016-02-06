package cinergi.annotator

class AnnotatedKeywordRec {
    String oldTerm
    String oldCategory
    String ontologyId
    String annotationAction
    String newTerm
    String newCategory
    Date lastChangedDate
    String annotator

    def toMap() {
        def map = ['oldTerm'         : oldTerm, 'oldCategory': oldCategory, 'ontologyId': ontologyId,
                   'annotationAction': annotationAction, 'newTerm': newTerm, 'newCategory': newCategory,
                   'lastChangedDate' : lastChangedDate, 'annotator': annotator]
        map
    }
    static constraints = {
        annotationAction inList: ['new', 'deleted', 'updated']
    }
    static mapping = {
        version false
    }
}
