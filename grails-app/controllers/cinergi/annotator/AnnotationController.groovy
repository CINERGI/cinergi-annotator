package cinergi.annotator

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import org.neuinfo.foundry.common.model.EntityInfo
import org.neuinfo.foundry.common.model.Keyword
import org.neuinfo.foundry.common.util.FacetHierarchyHandler
import org.neuinfo.foundry.common.util.IHierarchyHandler

class AnnotationController {
    def annotationService
    // def jmsService
    def beforeInterceptor = [action: this.&auth]
    // static allowedMethods = [resourceEntered2Scicrunch: 'GET']

    def auth() {
        if (!session.user) {
            redirect(controller: 'User', action: 'home', params: params)
            return false
        }
    }

    def serveXmlSchema(String schemaName) {
        File f = new File('/home/bozyurt/dev/js/jquery.xmleditor/demo/examples/MD_Metadata.xsd')
        assert f.isFile()
        render(contentType: 'application/xml', text: f.text, encoding: 'UTF-8')
    }

    /*
    def resourceEntered2Scicrunch() {
        jmsService.send('foundry.scicrunchIn', '', null)
        render(status: 200)
    }
    */

    def saveAnnotations() {
        println params
        String primaryKey = params.docId
        DocWrapper dw = annotationService.findDocument(primaryKey)
        assert dw
        def curModel = prepView(dw, primaryKey)
        def newKeywords = [:]
        def updatedKeywords = [:]
        def deletedKeywords = [:]
        def newBBs = [:]
        def updatedBBs = [:]
        def deletedBBs = [:]

        def curKeywords = [:]
        def curBBs = [:]
        curModel.keywords.each { KeywordInfo kw ->
            curKeywords[(kw.id)] = kw
        }
        curModel.bbList.each { BoundingBox bb ->
            curBBs[(bb.id)] = bb
        }
        Set<Integer> seenKeywordIdSet = new HashSet<Integer>(17)
        Set<Integer> seenBBIdSet = new HashSet<Integer>(7)
        Set<Integer> unassignedIdSet = new HashSet<Integer>(17)
        params.each { String k, String v ->
            if (k.startsWith('keyword_') && v) {
                v = v.trim()
                if (!Utils.isNewId(k)) {
                    int id = Utils.extractIdFromName(k)
                    seenKeywordIdSet.add(id)
                    KeywordInfo curKW = curKeywords[(id)]
                    assert curKW
                    if (curKW.keyword != v) {
                        KeywordInfo ukw = updatedKeywords[(id)]
                        if (!ukw) {
                            ukw = curKW
                            updatedKeywords[(id)] = ukw
                        }
                        ukw.keyword = v
                    }
                } else {
                    int id = Utils.extractNewIdFromName(k)
                    KeywordInfo nkw = new KeywordInfo(keyword: v, id: id)
                    newKeywords[(id)] = nkw
                }
            } else if (k.startsWith("nl_") || k.startsWith("el_") || k.startsWith("sl_") || k.startsWith("wl_")) {
                v = v.trim()
                if (!Utils.isNewId(k)) {
                    int id = Utils.extractIdFromName(k)
                    seenBBIdSet.add(id)
                    BoundingBox curBB = curBBs[(id)]
                    assert curBB
                    if (Utils.isChanged(curBB, k, v)) {
                        BoundingBox ubb = updatedBBs[(id)]
                        if (!ubb) {
                            ubb = curBB
                            updatedBBs[(id)] = ubb
                        }
                        Utils.updateBBParam(ubb, k, v)
                    }
                } else {
                    int id = Utils.extractNewIdFromName(k)
                    BoundingBox nbb = new BoundingBox(id: id)
                    newBBs[(id)] = nbb
                    Utils.updateBBParam(nbb, k, v)
                }
            }
        }

        params.each { String k, String v ->
            if (k.startsWith('category_')) {
                v = v.trim()
                if (!Utils.isNewId(k)) {
                    int id = Utils.extractIdFromName(k)
                    seenKeywordIdSet.add(id)
                    KeywordInfo curKW = curKeywords[(id)]
                    assert curKW
                    if (curKW.category != v) {
                        if (v.equalsIgnoreCase("unassigned")) {
                            updatedKeywords.remove(id)
                        } else {
                            KeywordInfo ukw = updatedKeywords[(id)]
                            if (!ukw) {
                                ukw = curKW
                                updatedKeywords[(id)] = ukw
                            }
                            ukw.category = v
                        }
                    }
                } else {
                    int id = Utils.extractNewIdFromName(k)
                    if (v.equalsIgnoreCase("unassigned")) {
                        newKeywords.remove(id)
                    } else {
                        KeywordInfo nkw = newKeywords[(id)]
                        assert nkw
                        nkw.category = v
                    }
                }
            }
        }
        curModel.keywords.each { KeywordInfo kw ->
            if (!seenKeywordIdSet.contains(kw.id)) {
                deletedKeywords[(kw.id)] = kw
            }
        }
        curModel.bbList.each { BoundingBox bb ->
            if (!seenBBIdSet.contains(bb.id)) {
                deletedBBs[(bb.id)] = bb
            }
        }
        ProvenanceInfo pi = new ProvenanceInfo()
        if (newKeywords) {
            pi.newKeywords.addAll(newKeywords.values())
        }
        if (newBBs) {
            pi.newBBs.addAll(newBBs.values())
        }
        if (updatedBBs) {
            pi.updatedBBs.addAll(updatedBBs.values())
        }
        if (deletedBBs) {
            pi.deletedBBs.addAll(deletedBBs.values())
        }
        if (updatedKeywords) {
            pi.updatedKeywords.addAll(updatedKeywords.values())
        }
        if (deletedKeywords) {
            pi.deletedKeywords.addAll(deletedKeywords.values())
        }

        println deletedKeywords
        println newKeywords

        def map = ['newKeywords': newKeywords, 'newBBs': newBBs, 'updatedKeywords': updatedKeywords,
                   'updatedBBs' : updatedBBs, 'deletedKeywords': deletedKeywords, 'deletedBBs': deletedBBs]
        def updateDW = annotationService.updateDocumentEnhancements(dw, pi, map, session.user.username)
        def model = prepView(updateDW, primaryKey)

        render(view: "view", model: model)
    }

    def index() {
        String primaryKey = '5a8c8834-b09e-47df-a5d5-053b9864229c'
        primaryKey = 'f66c287b-7e22-4680-8020-15525aebbc7d'
        String bbTestPrimaryKey = '505b9142e4b08c986b3197e9'
        primaryKey = 'OT.092012.26913.2'
        primaryKey = bbTestPrimaryKey
        if (params.docId) {
            primaryKey = params.docId
        }

        DocWrapper dw = annotationService.findDocument(primaryKey)
        assert dw

        //String abstractTxt = result.OriginalDoc.'gmd:MD_Metadata'.'gmd:identificationInfo'
        //        .'gmd:MD_DataIdentification'.'gmd:abstract'.'gco:CharacterString'.'_$'
        def model = prepView(dw, primaryKey)

        render(view: "view", model: model)

    }

    private def depthFirst(parent, String label, list) {
        if (!parent) return
        if (parent instanceof BasicDBObject) {
            BasicDBObject p = (BasicDBObject) parent
            for (String key : p.keySet()) {
                if (key == label) {
                    list << p[key]
                } else {
                    depthFirst(p[key], label, list)
                }
            }
        } else if (parent instanceof BasicDBList) {
            BasicDBList plist = (BasicDBList) parent
            for (p in plist) {
                depthFirst(p, label, list)
            }
        } else {
            if (parent instanceof Map.Entry) {
                if (parent.key == label) {
                    list << parent.value
                } else {
                    depthFirst(parent.value, label, list)
                }
            }
        }
    }

    private String getText(node) {
        def list = []
        depthFirst(node, '_$', list)
        return list ? list[0] : null
    }

    private def prepView(DocWrapper dw, String primaryKey) {
        if (dw.history.prov) {
            //println "prov:" + dw.history.prov
            //ProvenanceHelper.prepAnnotationProvenance(dw, null)
        }

        String title = dw.originalDoc.'gmd:MD_Metadata'?.'gmd:identificationInfo'?.'gmd:MD_DataIdentification'?.'gmd:citation'?.'gmd:CI_Citation'?.'gmd:title'?.'gco:CharacterString'?.'_$'
        String abstractTxt = dw.originalDoc.'gmd:MD_Metadata'?.'gmd:identificationInfo'?.'gmd:MD_DataIdentification'?.'gmd:abstract'?.'gco:CharacterString'?.'_$'
        if (!abstractTxt) {
            abstractTxt = dw.originalDoc.'MD_Metadata'?.'identificationInfo'?.'MD_DataIdentification'?.'abstract'?.'gco:CharacterString'?.'_$'
        }
        if (!title) {
            title = dw.originalDoc.'MD_Metadata'?.'identificationInfo'?.'MD_DataIdentification'?.'citation'?.'CI_Citation'?.'title'?.'gco:CharacterString'?.'_$'
        }
        if (!title) {
            title = dw.originalDoc.'gmi:MD_Metadata'?.'gmd:identificationInfo'?.'gmd:MD_DataIdentification'?.'gmd:citation'?.'gmd:CI_Citation'?.'gmd:title'?.'gco:CharacterString'?.'_$'
        }
        if (!abstractTxt) {
            abstractTxt = dw.originalDoc.'gmi:MD_Metadata'?.'gmd:identificationInfo'?.'gmd:MD_DataIdentification'?.'gmd:abstract'?.'gco:CharacterString'?.'_$'
        }

        println "abstract:" + abstractTxt
        println "title:" + title

        String keywordTag = 'gmd:keyword'
        String keywordTypeCodeTag = 'gmd:MD_KeywordTypeCode'
        def dkList = dw.originalDoc.'gmd:MD_Metadata'?.'gmd:identificationInfo'?.'gmd:MD_DataIdentification'?.'gmd:descriptiveKeywords'
        if (!dkList) {
            dkList = dw.originalDoc.'gmi:MD_Metadata'?.'gmd:identificationInfo'?.'gmd:MD_DataIdentification'?.'gmd:descriptiveKeywords'
        }
        if (!dkList) {
            dkList = dw.originalDoc.'MD_Metadata'?.'identificationInfo'?.'MD_DataIdentification'?.'descriptiveKeywords'
            keywordTag = 'keyword'
            keywordTypeCodeTag = 'MD_KeywordTypeCode'
        }
        def allKeywordMap = [:]
        int allIdx = 1000
        if (dkList) {
            for (dk in dkList) {
                def kList = []
                depthFirst(dk, keywordTag, kList)
                def typeList = []
                depthFirst(dk, keywordTypeCodeTag, typeList)
                if (kList) {
                    String type = getText(typeList[0])
                    if (type) {
                        println "type:$type"
                        kList.each { kn ->
                            String k = getText(kn);
                            println k
                            KeywordInfo ki = new KeywordInfo(keyword: k, category: type, id: allIdx++)
                            allKeywordMap["$k:$type"] = ki
                        }
                    }
                }
            }
        }
        def boundingBoxes = dw.data.spatial?.boundingBoxes
        List<BoundingBox> bbList = new ArrayList<>(5)
        int idx = 1
        if (boundingBoxes) {
            boundingBoxes.each { BoundingBoxRec b ->
                String sl = b.southwest.lat
                String wl = b.southwest.lng
                String nl = b.northeast.lat
                String el = b.northeast.lng
                bbList << new BoundingBox(latSouth: sl, lngWest: wl, latNorth: nl, lngEast: el, id: idx, type: 'bb')
                idx++
            }
        }
        List<BoundingBox> derivedBoundingBoxes = dw.data.spatial?.derivedBoundingBoxesFromPlaces
        if (derivedBoundingBoxes) {
            derivedBoundingBoxes.each { BoundingBoxRec b ->
                String sl = b.southwest.lat
                String wl = b.southwest.lng
                String nl = b.northeast.lat
                String el = b.northeast.lng
                bbList << new BoundingBox(latSouth: sl, lngWest: wl, latNorth: nl,
                        lngEast: el, id: idx, type: 'dbb')
                idx++
            }
        }
        Map<String, BoundingBox> derivedPlaceBoundingBoxes = dw.data.spatial?.derivedBoundingBoxesFromDerivedPlace
        if (derivedPlaceBoundingBoxes) {
            derivedPlaceBoundingBoxes.each { String place, BoundingBoxRec b ->
                String sl = b.southwest.lat
                String wl = b.southwest.lng
                String nl = b.northeast.lat
                String el = b.northeast.lng
                bbList << new BoundingBox(latSouth: sl, lngWest: wl, latNorth: nl,
                        lngEast: el, id: idx, type: 'dpbb', text: place)
                idx++
            }
        }
        //def keywords = result.Data.keywords
        def keywords = dw.data.keywords
        def categoryKwMap = [:]
        IHierarchyHandler chh = FacetHierarchyHandler.getInstance();
        // CategoryHierarchyHandler chh = CategoryHierarchyHandler.getInstance()
        idx = 1
        if (keywords) {
            println keywords
            List<Keyword> keywordList = new ArrayList<Keyword>(keywords.size())
            keywords.each { k ->
                Keyword keyword = new Keyword(k.term)
                for (eiRec in k.entityInfos) {
                    EntityInfo ei = new EntityInfo(eiRec.contentLocation, eiRec.id, eiRec.start, eiRec.end, eiRec.category)
                    keyword.addEntityInfo(ei)
                }
                keywordList << keyword
            }
            keywordList.each { Keyword kw ->
                Set<String> categories = kw.getCategories()
                if (categories.size() > 0) {
                    String category = kw.getTheCategory(chh)
                    String cinergiCategory = chh.getCinergiCategory(category.toLowerCase())
                    if (cinergiCategory) {
                        category = cinergiCategory;
                    }
                    List<KeywordInfo> kiList = categoryKwMap[category]
                    if (!kiList) {
                        kiList = new ArrayList<KeywordInfo>(5)
                        categoryKwMap[category] = kiList
                    }
                    kiList << new KeywordInfo(keyword: kw.getTerm(), category: category, id: idx)
                    idx++
                }
            }
            /*
            keywords.each { k ->
                println "EntityInfo:>> " + k.entityInfos[0]

                String category = k.entityInfos[0].category
                String term = k.term
                List<KeywordInfo> kiList = categoryKwMap[category]
                if (!kiList) {
                    kiList = new ArrayList<KeywordInfo>(5)
                    categoryKwMap[category] = kiList
                }
                kiList << new KeywordInfo(keyword: term, category: category, id: idx)
                idx++
            }
            */
        }
        List<KeywordInfo> keywordList = new ArrayList<KeywordInfo>()
        for (String key : categoryKwMap.keySet()) {
            keywordList.addAll(categoryKwMap[key])
        }

        List<KeywordInfo> existingKeywords = new ArrayList<KeywordInfo>()
        def kmap = [:]
        keywordList.each { KeywordInfo ki -> kmap["${ki.keyword}:${ki.category}"] = ki }
        allKeywordMap.values().each { KeywordInfo ki ->
            if (!kmap["${ki.keyword}:${ki.category}"]) {
                existingKeywords << ki
            }
        }

        List<String> categories4DD = new ArrayList<String>(chh.getSortedCinergiCategories())
        if (categories4DD[0] != 'Unassigned') {
            categories4DD.add(0, 'Unassigned')
        }

        String sourceName = dw.sourceInfo.name
        String sourceID = dw.sourceInfo.sourceID
        return ["bbList"          : bbList, "keywords": keywordList, 'abstractTxt': abstractTxt,
                'docId'           : primaryKey, 'sourceName': sourceName, 'titleTxt': title,
                "existingKeywords": existingKeywords, "sourceID": sourceID,
                "categories"      : categories4DD]

    }
}
