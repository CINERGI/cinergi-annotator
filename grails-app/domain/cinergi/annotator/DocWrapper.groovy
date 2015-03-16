package cinergi.annotator

import com.mongodb.WriteConcern
import groovy.transform.ToString
import org.bson.types.ObjectId

@ToString
class DocWrapper {
    static mapWith = "mongo"
    ObjectId id
    String primaryKey
    String crawlDate
    Map originalDoc
    SourceInformation sourceInfo
    DataRec data
    ProcessingRec processing
    HistoryRec history

    static embedded = ['SourceInfo', 'Data', 'Processing', 'History']
    static constraints = {

    }
    static mapping = {
        collection "records"
        database "discotest"
        primaryKey index: true
        version false
        crawlDate attr: "CrawlDate"
        originalDoc attr: "OriginalDoc"
        sourceInfo attr: "SourceInfo"
        data attr: "Data"
        processing attr: "Processing"
        history attr: "History"
        writeConcern WriteConcern.JOURNAL_SAFE
    }

}
