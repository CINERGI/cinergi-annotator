package cinergi.annotator

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
                        KeywordInfo ukw = updatedKeywords[(id)]
                        if (!ukw) {
                            ukw = curKW
                            updatedKeywords[(id)] = ukw
                        }
                        ukw.category = v
                    }
                } else {
                    int id = Utils.extractNewIdFromName(k)
                    KeywordInfo nkw = newKeywords[(id)]
                    assert nkw
                    nkw.category = v
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
//        String primaryKey = 'OT.092012.26913.2'
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
        //println dw

        /*
        String homeDir = System.getProperty('user.home')
        File f = new File(homeDir,'dev/java/cinergi-annotator/cinergi_test.json')
        assert f.isFile()
        def slurper = new JsonSlurper()
        def result = slurper.parseText(f.text)
        */

        //String abstractTxt = result.OriginalDoc.'gmd:MD_Metadata'.'gmd:identificationInfo'
        //        .'gmd:MD_DataIdentification'.'gmd:abstract'.'gco:CharacterString'.'_$'
        def model = prepView(dw, primaryKey)

        render(view: "view", model: model)

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
        println "abstract:" + abstractTxt
        println "title:" + title
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
        idx = 1
        if (keywords) {
            println keywords
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
        }
        List<KeywordInfo> keywordList = new ArrayList<KeywordInfo>()
        // FIXME
        for (String key : categoryKwMap.keySet()) {
            keywordList.addAll(categoryKwMap[key])
        }
        /*
        if (categoryKwMap) {
            if (categoryKwMap.containsKey('theme')) {
                keywordList.addAll(categoryKwMap['theme'])
            }
            if (categoryKwMap.containsKey('instrument')) {
                keywordList.addAll(categoryKwMap['instrument'])
            }
            if (categoryKwMap.containsKey('location')) {
                keywordList.addAll(categoryKwMap['location'])
            }
        }
        */
        String sourceName = dw.sourceInfo.name
        return ["bbList": bbList, "keywords": keywordList, 'abstractTxt': abstractTxt,
                'docId' : primaryKey, 'sourceName': sourceName, 'titleTxt': title]

    }
}
