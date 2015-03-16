package cinergi.annotator

import groovy.transform.ToString

@ToString
class SourceInformation {
    String sourceID
    String viewID
    String name
    String dataSource

    static mapping = {
        name attr:"Name"
        dataSource attr:"DataSource"
        viewID attr:"ViewID"
        sourceID attr:"SourceID", index: true
        version false

    }
}
