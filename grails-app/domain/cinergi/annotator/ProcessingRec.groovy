package cinergi.annotator

import groovy.transform.ToString

@ToString
class ProcessingRec {
    String status
    String docId
    static mapping = {
        version false
        status index:true
    }
}
