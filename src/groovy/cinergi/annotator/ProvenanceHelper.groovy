package cinergi.annotator

import com.mongodb.DBObject
import org.json.JSONObject
import org.neuinfo.foundry.common.provenance.ProvenanceRec
import org.neuinfo.foundry.common.util.JSONUtils

import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Created by bozyurt on 3/13/15.
 */
class ProvenanceHelper {

    public static ProvenanceData prepAnnotationProvenance(DocWrapper dw, ProvenanceInfo pi) {
        String currentVersion = dw.history.prov.curVersion
        Date lastProcessedDate = null
        if (dw.history.prov.lastProcessedDate instanceof Date) {
            lastProcessedDate = dw.history.prov.lastProcessedDate
        } else {
           lastProcessedDate = parseDate(dw.history.prov.lastProcessedDate)
        }
        assert currentVersion
        ProvenanceRec.Builder builder = new ProvenanceRec.Builder('http://example.org', 'foundry')

        String startTime = getTimeInProvenanceFormat(lastProcessedDate)
        Date now = new Date()
        String docCreationTime = getTimeInProvenanceFormat(now)
        String label = dw.primaryKey + ':' + dw.sourceInfo.name
        String howLabel = "User annotation to keywords and/or spatial extents"
        howLabel = pi.prepHowMessage()
        String version = org.neuinfo.foundry.common.util.Utils.nextVersion(currentVersion)
        String inDocId = builder.entityWithAttr("UUID=" + dw.primaryKey, "creationTime=" + startTime,
                "sourceId=" + dw.sourceInfo.sourceID,
                "label=" + label, "version=" + version).getLastGeneratedId()

        String nextVersion = org.neuinfo.foundry.common.util.Utils.nextVersion(version)
        String outDocId = builder.entityWithAttr("UUID=" + dw.primaryKey,
                "creationTime=" + docCreationTime,
                "label=" + label,
                "version=" + nextVersion).getLastGeneratedId()
        String activityId = builder.activityWithAttr('annotator', docCreationTime,
                getTimeInProvenanceFormat(), "prov:how=" + howLabel).getLastGeneratedId();
        ProvenanceRec provenanceRec = builder.used(activityId, inDocId)
                .wasDerivedFrom(outDocId, inDocId, activityId)
                .wasGeneratedBy(outDocId, activityId).build()

        String provJSON = provenanceRec.asJSON()
        println(provJSON)
        JSONObject js = new JSONObject(provJSON)
        DBObject provDBO = JSONUtils.encode(js, true)
        ProvenanceData pd = new ProvenanceData(currentVersion: nextVersion,
                processedDate: now, provDBO: provDBO)
        return pd
    }

    public static String getTimeInProvenanceFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        sdf.setTimeZone(TimeZone.getDefault())
        return sdf.format(new Date())
    }

    public static String getTimeInProvenanceFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        sdf.setTimeZone(TimeZone.getDefault())
        return sdf.format(date)
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.format(date);
    }

    public static Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.parse(dateStr);
    }

}
