package cinergi.annotator

import groovy.transform.ToString

@ToString
class DataRec {
    Map metaData
    Spatial spatial
    List<KeywordRec> keywords
    List<EnhancedKeywordInfo> enhancedKeywords
    static embedded = ['keywords','spatial','enhancedKeywords']
    static mapping = {
        version false
    }

}
