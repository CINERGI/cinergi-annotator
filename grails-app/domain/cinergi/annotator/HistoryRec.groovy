package cinergi.annotator

import groovy.transform.ToString

@ToString
class HistoryRec {
    String batchId
    Map prov
    static mapping = {
        version false
    }
}
