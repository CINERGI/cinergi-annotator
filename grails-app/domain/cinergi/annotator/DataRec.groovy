package cinergi.annotator

import groovy.transform.ToString

@ToString
class DataRec {
    Map metaData
    Spatial spatial
    List<KeywordRec> keywords
    List<EnhancedKeywordInfo> enhancedKeywords
    List<AnnotatedKeywordRec> annotatedKeywords
    static embedded = ['keywords','spatial','enhancedKeywords','annotatedKeywords']
    static mapping = {
        version false
    }

}
