package cinergi.annotator

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import org.neuinfo.foundry.common.util.JSONUtils

/**
 * Created by bozyurt on 3/19/15.
 */
class ProvenanceClient {
    private String serverURL = "http://geoprovdb.webfactional.com/"
    private String user = "cinergi"
    private String pwd = "4cinergi_prov"


    def saveProvenance(DBObject provData) {
        JSONObject json = JSONUtils.toJSON((BasicDBObject) provData, true)
        DefaultHttpClient client = new DefaultHttpClient()
        client.getCredentialsProvider().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user, pwd))
        URIBuilder builder = new URIBuilder(serverURL).setPath("/provdb/api/provenance/")
        URI uri = builder.build()
        println "uri:$uri"
        HttpPost httpPost = new HttpPost(uri)
        try {
            String jsonStr = json.toString(2)
            httpPost.addHeader("Accept", "application/json")
            httpPost.addHeader("Content-Type", "application/json")
            StringEntity entity = new StringEntity(jsonStr, "UTF-8")
            httpPost.setEntity(entity)
            final HttpResponse response = client.execute(httpPost)
            int statusCode = response.getStatusLine().getStatusCode()
            if (statusCode == 200 || statusCode == 201) {
                HttpEntity respEntity = response.getEntity()
                String resultJsonStr = EntityUtils.toString(respEntity)
                try {
                    JSONObject js = new JSONObject(resultJsonStr)
                    if (js.has("request id: ")) {
                        String requestId = js.getString("request id: ")
                        return requestId
                    }
                } catch (Throwable t) {
                    t.printStackTrace()
                }
                println resultJsonStr
            } else {
                println response
                println EntityUtils.toString(response.getEntity())
            }
            return null
        } finally {
            httpPost?.releaseConnection()
        }
    }
}
