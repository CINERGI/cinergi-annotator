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
    static constraints = {
        annotationAction inList: ['new','deleted','updated']
    }
    static mapping = {
        version false
    }
}
