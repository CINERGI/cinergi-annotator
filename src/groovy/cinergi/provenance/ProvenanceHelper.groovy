package cinergi.provenance

import cinergi.annotator.ProvenanceClient
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.json.JSONArray
import org.json.JSONObject
import org.neuinfo.foundry.common.model.DocWrapper
import org.neuinfo.foundry.common.provenance.ProvenanceRec
import org.neuinfo.foundry.common.util.Assertion
import org.neuinfo.foundry.common.util.JSONUtils
import org.neuinfo.foundry.common.util.Utils

import java.text.ParseException

/**
 * Created by bozyurt on 5/19/15.
 */
class ProvenanceHelper {
    public static boolean TEST_MODE = true

    public static void saveProvRec2DB(DBObject docWrapper,
                                      JSONObject provJSON, String currentVersion, Date processedDate) {
        DBObject history = (DBObject) docWrapper.get("History")
        DBObject provDO = (DBObject) history.get("prov")
        if (!provDO) {
            provDO = new BasicDBObject()
            history.put("prov", provDO)
        }
        provDO.put("curVersion", currentVersion)
        provDO.put("lastProcessedDate", DocWrapper.formatDate(processedDate))
        BasicDBList events = (BasicDBList) provDO.get("events")
        if (events == null) {
            events = new BasicDBList()
            provDO.put("events", events)
        }
        events.add(JSONUtils.encode(provJSON, true))
    }

    public static void saveIngestProvRec2DB(DocWrapper docWrapper, JSONObject provJSON,
                                            String currentVersion, Date processedDate) {
        JSONObject history = docWrapper.getHistory()
        if (!history) {
            history = new JSONObject()
            docWrapper.setHistory(history)
            history.put("batchId", docWrapper.getBatchId())
        }
        JSONObject provJs;
        if (!history.has("prov")) {
            provJs = new JSONObject()
            history.put("prov", provJs)
        } else {
            provJs = history.getJSONObject("prov")
        }
        provJs.put("curVersion", currentVersion)
        provJs.put("lastProcessedDate", DocWrapper.formatDate(processedDate))
        JSONArray events = null
        if (!provJs.has("events")) {
            events = new JSONArray()
            provJs.put("events", events)
        }
        JSONUtils.escapeJson(provJSON)
        events.put(provJSON)
    }

    public static void clearProv(DBObject docWrapper) {
        DBObject history = (DBObject) docWrapper.get("History")
        DBObject provDO = (DBObject) history.get("prov")
        if (provDO) {
            history.removeField("prov")
        }
    }

    public static ProvState getCurrentProvState(DBObject docWrapper) throws ParseException {
        DBObject history = (DBObject) docWrapper.get("History");

        BasicDBObject provDO = (BasicDBObject) history.get("prov")
        if (!provDO) {
            return null
        }
        String currentVersion = (String) provDO.get("curVersion")
        Date lpDate = DocWrapper.getDate((String) provDO.get("lastProcessedDate"))
        return new ProvState(currentVersion, lpDate)
    }

    public static String saveEnhancerProvenance(String activityName, ProvData provData, DBObject docWrapper) {
        try {
            ProvState provState = getCurrentProvState(docWrapper)
            Assertion.assertNotNull(provState)

            ProvenanceRec.Builder builder = new ProvenanceRec.Builder("http://example.org", "foundry")
            String startTime = Utils.getTimeInProvenanceFormat(provState.getLastProcessedDate())
            Date now = new Date()
            String docCreationTime = Utils.getTimeInProvenanceFormat(now)
            String label = provData.prepLabel()
            String howLabel = provData.prepLabelHow()
            String version = provState.getCurVersion()
            String inDocId = builder.entityWithAttr("UUID=" + provData.docIdentifier, "creationTime=" + startTime,
                    "sourceId=" + provData.srcId,
                    "label=" + label, "version=" + version).getLastGeneratedId()
            String nextVersion = Utils.nextVersion(version);
            String outDocId = builder.entityWithAttr("UUID=" + provData.docIdentifier,
                    "creationTime=" + docCreationTime,
                    "label=" + label,
                    "version=" + nextVersion).getLastGeneratedId()
            String activityId = builder.activityWithAttr(activityName, docCreationTime,
                    Utils.getTimeInProvenanceFormat(), "prov:how=" + howLabel).getLastGeneratedId();
            ProvenanceRec provenanceRec = builder.used(activityId, inDocId)
                    .wasDerivedFrom(outDocId, inDocId, activityId)
                    .wasGeneratedBy(outDocId, activityId).build()

            String provJSON = provenanceRec.asJSON()
            // println(provJSON);

            saveProvRec2DB(docWrapper, new JSONObject(provJSON), nextVersion, now)

            ProvenanceClient pc = new ProvenanceClient()
            String requestId = null
            if (!TEST_MODE) {
                requestId = pc.saveProvenance(provenanceRec)
                println "requestId:$requestId"
            }
            return requestId
        } catch (Throwable t) {
            t.printStackTrace()
        }
        return null
    }

    public static void removeProvenance(String uuid) throws Exception {
        if (!TEST_MODE) {
            ProvenanceClient pc = new ProvenanceClient()
            pc.deleteProvenance(uuid)
        }
    }

    public static String saveIngestionProvenance(String activityName, ProvData provData, Date startTS, DocWrapper docWrapper) {
        try {
            ProvenanceRec.Builder builder = new ProvenanceRec.Builder("http://example.org", "foundry")
            String startTime = Utils.getTimeInProvenanceFormat(startTS)
            Date now = new Date()
            String docCreationTime = Utils.getTimeInProvenanceFormat(now)
            String startUUID = UUID.randomUUID().toString()
            String label = provData.prepLabel()
            String howLabel = provData.prepLabelHow()
            String inDocId = builder.entityWithAttr("UUID=" + startUUID, "creationTime=" + startTime,
                    "sourceId=" + provData.srcId,
                    "label=" + label,
                    "version=" + provData.getVersion()).getLastGeneratedId()
            String nextVersion = org.neuinfo.foundry.common.util.Utils.nextVersion(provData.getVersion())

            String outDocId = builder.entityWithAttr("UUID=" + provData.docIdentifier,
                    "creationTime=" + docCreationTime,
                    "label=" + label,
                    "version=" + nextVersion).getLastGeneratedId()
            String activityId = builder.activityWithAttr(activityName, docCreationTime,
                    Utils.getTimeInProvenanceFormat(), "prov:how=" + howLabel).getLastGeneratedId()

            ProvenanceRec provenanceRec = builder.used(activityId, inDocId)
                    .wasGeneratedBy(outDocId, activityId).build()
            String provJSON = provenanceRec.asJSON()

            saveIngestProvRec2DB(docWrapper, new JSONObject(provJSON), nextVersion, now)

            // println provJSON
            ProvenanceClient pc = new ProvenanceClient()
            String requestId = null
            if (!TEST_MODE) {
                requestId = pc.saveProvenance(provenanceRec)
                println "requestId:$requestId"
            }
            return requestId
        } catch (Throwable t) {
            t.printStackTrace()
        }
        return null
    }

}
