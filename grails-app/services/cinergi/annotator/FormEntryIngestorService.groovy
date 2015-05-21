package cinergi.annotator

import cinergi.ingestion.CinergiFormData
import cinergi.provenance.ModificationType
import cinergi.provenance.ProvData
import cinergi.provenance.ProvenanceHelper
import com.mongodb.*
import grails.transaction.Transactional
import org.jdom2.Element
import org.json.JSONObject
import org.neuinfo.foundry.common.ingestion.DocumentIngestionService
import org.neuinfo.foundry.common.ingestion.SourceIngestionService
import org.neuinfo.foundry.common.model.DocWrapper
import org.neuinfo.foundry.common.model.PrimaryKeyDef
import org.neuinfo.foundry.common.model.ScicrunchResourceRec
import org.neuinfo.foundry.common.model.Source
import org.neuinfo.foundry.common.util.JSONUtils
import org.neuinfo.foundry.common.util.TemplateISOXMLGenerator
import org.neuinfo.foundry.common.util.Utils
import org.neuinfo.foundry.common.util.XML2JSONConverter

@Transactional
class FormEntryIngestorService {
    def mongo

    def toPipeline(CinergiFormData cfd) {
        ScicrunchResourceRec srRec = new ScicrunchResourceRec()
        srRec.email = cfd.email
        srRec.dataSetName = cfd.title
        srRec.description = cfd.description
        cfd.userKeywords.each { ScicrunchResourceRec.UserKeyword uk -> srRec.addKeyword(uk) }
        Source source = cinergi.ingestion.Utils.prepareSource("Entry Forms")
        Date startDate = new Date()
        String batchId = Utils.prepBatchId(startDate)
        String collectionName = "records"
        String outStatus = "new.1"
        DB db = mongo.getDB("discotest");
        source = findOrAssignIDAndSaveSource(db, source)
        println source
        TemplateISOXMLGenerator generator = new TemplateISOXMLGenerator()

        Element docEl = generator.createISOXMLDoc(srRec)
        XML2JSONConverter converter = new XML2JSONConverter()
        JSONObject json = converter.toJSON(docEl)
        BasicDBObject docWrapper = findDocument(json, collectionName, db, source)
        if (!docWrapper) {
            DocWrapper dw = prepareDocWrapper(json, batchId, source, outStatus)
            // save provenance
            ProvData provData = new ProvData(dw.getPrimaryKey(),
                    ModificationType.Ingested)
            provData.setSourceName(dw.getSourceName()).setSrcId(dw.getSourceId())
            // first cleanup any previous provenance data
            ProvenanceHelper.removeProvenance(dw.getPrimaryKey())
            ProvenanceHelper.saveIngestionProvenance("ingestion",
                    provData, startDate, dw)
            saveDocument(dw, collectionName, db)
        }
    }

    private Source findOrAssignIDAndSaveSource(DB db, Source source) {
        DBCollection sources = db.getCollection("sources");
        BasicDBObject query = new BasicDBObject("sourceInformation.name", source.getName());
        BasicDBObject srcDBO = (BasicDBObject) sources.findOne(query);
        if (srcDBO != null) {
            source = Source.fromDBObject(srcDBO)
        } else {
            SourceIngestionService.ResourceID rid = getLatestResourceID(sources)
            String newResourceID = "cinergi-" + String.format("%04d", rid.id + 1)
            source.setResourceID(newResourceID)
            JSONObject json = source.toJSON()
            DBObject sourceDbObj = JSONUtils.encode(json, true)
            try {
                WriteResult wr = sources.insert(sourceDbObj, WriteConcern.JOURNAL_SAFE);
            } catch (MongoException me) {
                me.printStackTrace();
                return null
            }
        }
        return source
    }


    SourceIngestionService.ResourceID getLatestResourceID(DBCollection sources) {
        BasicDBObject keys = new BasicDBObject("sourceInformation.resourceID", 1);
        DBCursor cursor = sources.find(new BasicDBObject(), keys);
        SourceIngestionService.ResourceID latestResourceID = null;
        try {
            while (cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                BasicDBObject dbo = (BasicDBObject) dbObject.get("sourceInformation");
                String resourceID = dbo.getString("resourceID");
                if (resourceID.startsWith("cinergi")) {
                    if (latestResourceID == null) {
                        latestResourceID = new SourceIngestionService.ResourceID(resourceID);
                    } else {
                        SourceIngestionService.ResourceID rid = new SourceIngestionService.ResourceID(resourceID);
                        if (rid.compareTo(latestResourceID) > 0) {
                            latestResourceID = rid;
                        }
                    }
                }
            }
        } finally {
            cursor.close()
        }
        return latestResourceID
    }

    private BasicDBObject findDocument(JSONObject payload, String collectionName, DB db, Source source) throws Exception {
        DBCollection collection = db.getCollection(collectionName)
        PrimaryKeyDef pkDef = source.getPrimaryKeyDef()

        String primaryKey = pkDef.prepPrimaryKey(payload);
        BasicDBObject query = new BasicDBObject("primaryKey", primaryKey)
                .append("SourceInfo.SourceID", source.getResourceID())
                .append("SourceInfo.DataSource", source.getDataSource())
        DBCursor cursor = collection.find(query)

        BasicDBObject docWrapper = null
        try {
            if (cursor.hasNext()) {
                docWrapper = (BasicDBObject) cursor.next()
            }
        } finally {
            cursor.close()
        }
        return docWrapper
    }

    private DocWrapper prepareDocWrapper(JSONObject payload, String batchId, Source source, String outStatus) throws Exception {
        PrimaryKeyDef pkDef = source.getPrimaryKeyDef()
        String primaryKey = pkDef.prepPrimaryKey(payload)

        DocWrapper.Builder builder = new DocWrapper.Builder(outStatus)
        final DocWrapper docWrapper = builder.batchId(batchId).payload(payload).version(1)
                .crawlDate(new Date()).sourceId(source.getResourceID())
                .sourceName(source.getName()).primaryKey(primaryKey)
                .dataSource(source.getDataSource()).build()
        return docWrapper
    }

    private DocWrapper saveDocument(DocWrapper docWrapper, String collectionName, DB db) throws Exception {
        DBCollection collection = db.getCollection(collectionName)
        // now save the doc
        JSONObject json = docWrapper.toJSON()
        DBObject dbObject = JSONUtils.encode(json, true)

        collection.insert(dbObject, WriteConcern.SAFE)
        return docWrapper
    }

    def submit2Pipeline(CinergiFormData cfd) {
        Date startDate = new Date()
        String batchId = Utils.prepBatchId(startDate)
        String collectionName = "records"
        String outStatus = "new.1"


        DocumentIngestionService dis = new DocumentIngestionService()
        SourceIngestionService sis = new SourceIngestionService()

        try {

            // sis.start()
            ScicrunchResourceRec srRec = new ScicrunchResourceRec()
            srRec.email = cfd.email
            srRec.dataSetName = cfd.title
            srRec.description = cfd.description
            cfd.userKeywords.each { ScicrunchResourceRec.UserKeyword uk -> srRec.addKeyword(uk) }
            Source source = cinergi.ingestion.Utils.prepareSource("Entry Forms")
            source = sis.findOrAssignIDandSaveSource(source)
            dis.setSource(source)
            TemplateISOXMLGenerator generator = new TemplateISOXMLGenerator()

            Element docEl = generator.createISOXMLDoc(srRec)
            XML2JSONConverter converter = new XML2JSONConverter()
            JSONObject json = converter.toJSON(docEl)
            BasicDBObject docWrapper = dis.findDocument(json, collectionName)
            if (!docWrapper) {
                DocWrapper dw = dis.prepareDocWrapper(json, batchId, source, outStatus)
                // save provenance
                ProvData provData = new ProvData(dw.getPrimaryKey(),
                        ModificationType.Ingested)
                provData.setSourceName(dw.getSourceName()).setSrcId(dw.getSourceId())
                // first cleanup any previous provenance data
                ProvenanceHelper.removeProvenance(dw.getPrimaryKey());
                ProvenanceHelper.saveIngestionProvenance("ingestion",
                        provData, startDate, dw);
                dis.saveDocument(dw, collectionName)
            }
        } finally {
            dis.shutdown()
            sis.shutdown()
        }
    }


}
