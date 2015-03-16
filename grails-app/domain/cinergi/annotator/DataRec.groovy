package cinergi.annotator

import groovy.transform.ToString

@ToString
class DataRec {
    Map metaData
    Spatial spatial
    List<KeywordRec> keywords
    static embedded = ['keywords','spatial']
    static mapping = {
        version false
    }

}
