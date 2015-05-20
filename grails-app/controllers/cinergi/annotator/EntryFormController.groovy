package cinergi.annotator

import cinergi.ingestion.CinergiFormData
import org.neuinfo.foundry.common.model.ScicrunchResourceRec

class EntryFormController {
    def formEntryIngestorService
    def beforeInterceptor = [action: this.&auth]

    def auth() {
        if (!session.user) {
            redirect(controller: 'User', action: 'home', params: params)
            return false
        }
    }

    def index() {
         render(view: "view", model: [:])
    }

    def processEntry() {
        println params
        assert params.email
        assert params.description
        CinergiFormData cfd = new CinergiFormData(email: params.email, title: params.title,
                description: params.description)
        def newKeywords = [:]
        params.each { String k, String v ->
            if (k.startsWith('keyword_') && v) {
                v = v.trim()
                if (Utils.isNewId(k)) {
                    int id = Utils.extractNewIdFromName(k)
                    KeywordInfo nkw = new KeywordInfo(keyword: v, id: id)
                    newKeywords[(id)] = nkw
                }
            }
        }
        params.each { String k, String v ->
            if (k.startsWith('category_') && v) {
                if (Utils.isNewId(k)) {
                    int id = Utils.extractNewIdFromName(k)
                    KeywordInfo nkw = newKeywords[(id)]
                    assert nkw
                    nkw.category = v
                }
            }
        }
        newKeywords.values().each { KeywordInfo ki ->
            cfd.userKeywords << new ScicrunchResourceRec.UserKeyword(ki.keyword, ki.category)
        }
        formEntryIngestorService.toPipeline(cfd)
        render(view: "view", model: [:])
    }

}
