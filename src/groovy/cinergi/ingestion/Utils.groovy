package cinergi.ingestion

import org.json.JSONArray
import org.json.JSONObject
import org.neuinfo.foundry.common.model.PrimaryKeyDef
import org.neuinfo.foundry.common.model.Source

/**
 * Created by bozyurt on 5/18/15.
 */
class Utils {


    public static Source prepareSource(String sourceName) {
        Source.Builder builder = new Source.Builder("", sourceName)
        builder.dataSource(sourceName)
        JSONObject icJson = new JSONObject()
        icJson.put("ingestMethod", "Scicrunch")
        icJson.put("ingestURL", "")
        JSONObject crawlFreqJson = new JSONObject()
        crawlFreqJson.put("crawlType", "Frequency")
        crawlFreqJson.put("hours", "48")
        crawlFreqJson.put("minutes", "0")

        JSONArray jsArr = new JSONArray()
        def startDays = ["Sunday", "Monday", "Tuesday", "Wednesday",
                         "Thursday", "Friday", "Saturday"]
        for (String startDay : startDays) {
            jsArr.put(startDay)
        }
        crawlFreqJson.put("startDays", jsArr)
        crawlFreqJson.put("startTime", "0:00")
        crawlFreqJson.put("operationEndTime", "24:00")
        icJson.put("crawlFrequency", crawlFreqJson)
        builder.ingestConfiguration(icJson)
        List<String> workflowSteps = new ArrayList<String>(3)
        workflowSteps.add("UUID Generation")
        workflowSteps.add("XML2Cinergi")
        workflowSteps.add("Index")

        PrimaryKeyDef pkDef = new PrimaryKeyDef(
                Arrays.asList("\$.'gmd:MD_Metadata'.'gmd:fileIdentifier'.'gco:CharacterString'.'_\$'"),
                Arrays.asList(":"), "Value")

        builder.workflowSteps(workflowSteps).primaryKey(pkDef)
                .contentSpecification(new JSONObject())

        return builder.build()
    }
}
