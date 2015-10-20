package cinergi.annotator

import com.mongodb.DBObject
import grails.transaction.Transactional
import org.jdom2.Element
import org.neuinfo.foundry.common.util.ISOXMLGenerator

@Transactional
class AnnotationService {
    def mongo

    def getKeywords(String primaryKey) {

    }

    def findDocument(String primaryKey) {
        return DocWrapper.findByPrimaryKey(primaryKey)
    }

    def generateIsoXml(String primaryKey) {
        DBObject docWrapper = DocWrapper.collection.findOne(['primaryKey': primaryKey])
        ISOXMLGenerator generator = new ISOXMLGenerator()
        Element docEl = generator.generate(docWrapper)
        String outFile = "/tmp/${primaryKey}.xml"
        org.neuinfo.foundry.common.util.Utils.saveXML(docEl, outFile)
        return outFile
    }

    def updateDocumentEnhancements(DocWrapper dw, ProvenanceInfo pi, dataMap, String username) {
        boolean keywordsUpdated = false
        if (dataMap.deletedKeywords) {
            dataMap.deletedKeywords.values().each { KeywordInfo ki ->
                KeywordRec kw2Del = null
                dw.data.keywords.each { KeywordRec kw ->
                    if (kw.term.equals(ki.keyword)) {
                        kw2Del = kw
                        pi.deletedKeywords << ki
                    }
                }
                if (kw2Del) {
                    keywordsUpdated = true
                    kw2Del.removeByOntologyId(ki.ontologyId)
                    if (!kw2Del.entityInfos) {
                        dw.data.keywords.remove(kw2Del)
                    }
                }
            }
        }
        if (dataMap.newKeywords) {
            dataMap.newKeywords.values().each { KeywordInfo ki ->
                pi.newKeywords << ki
                KeywordRec kw = new KeywordRec(term: ki.keyword)
                EntityInfoRec eir = new EntityInfoRec(category: ki.category, ontologyId: 'user-annotation',
                        start: -1, end: -1, contentLocation: 'N/A')
                kw.entityInfos = []
                kw.entityInfos.add(eir)
                dw.data.keywords.add(kw)
                keywordsUpdated = true
            }
        }
        /* update does not make sense anymore since ontologyId of the updated category/facet is not known.

        if (dataMap.updatedKeywords) {
            dataMap.updatedKeywords.values().each { KeywordInfo ki ->
                pi.updatedKeywords << ki
                KeywordRec oldKW = dw.data.keywords[ki.id - 1]
                assert oldKW
                oldKW.term = ki.keyword
                oldKW.entityInfos[0].category = ki.category
                keywordsUpdated = true
            }
        }
        */
        def order2BBRMap = [:]
        if (dw.data.spatial) {
            int idx = 1
            dw.data.spatial.boundingBoxes.each { BoundingBoxRec bb ->
                order2BBRMap[(idx)] = bb
                ++idx
            }
            dw.data.spatial.derivedBoundingBoxesFromPlaces.each { BoundingBoxRec bb ->
                order2BBRMap[(idx)] = bb
                ++idx
            }
            dw.data.spatial.derivedBoundingBoxesFromDerivedPlace.values().each { BoundingBoxRec bb ->
                order2BBRMap[(idx)] = bb
                ++idx
            }
        }

        boolean bbUpdated = false
        if (dataMap.deletedBBs) {
            dataMap.deletedBBs.values().each { BoundingBox bb ->
                BoundingBoxRec bb2Del = AnnotationService.find(bb, dw.data.spatial.boundingBoxes)
                if (bb2Del) {
                    pi.deletedBBs << bb
                    dw.data.spatial.boundingBoxes.remove(bb2Del)
                    bbUpdated = true
                } else {
                    bb2Del = AnnotationService.find(bb, dw.data.spatial.derivedBoundingBoxesFromPlaces)
                    if (bb2Del) {
                        pi.deletedBBs << bb
                        dw.data.spatial.derivedBoundingBoxesFromPlaces.remove(bb2Del)
                        bbUpdated = true
                    } else {
                        String key2Del = AnnotationService.findKey(bb, dw.data.spatial.derivedBoundingBoxesFromDerivedPlace)
                        if (key2Del) {
                            pi.deletedBBs << bb
                            dw.data.spatial.derivedBoundingBoxesFromDerivedPlace.remove(key2Del)
                            bbUpdated = true
                        }
                    }
                }
            }
        }
        if (dataMap.updatedBBs) {
            dataMap.updatedBBs.values().each { BoundingBox bb ->
                BoundingBoxRec bb2Update = order2BBRMap[(bb.id)]
                if (bb2Update) {
                    pi.updatedBBs << bb
                    bb2Update.southwest.lat = bb.latSouth
                    bb2Update.southwest.lng = bb.lngWest
                    bb2Update.northeast.lat = bb.latNorth
                    bb2Update.northeast.lng = bb.lngEast
                    bbUpdated = true
                }
            }
        }

        if (keywordsUpdated || bbUpdated) {
            def updateMap = [:]
            if (keywordsUpdated) {
                def keywordsCol = dw.data.keywords.collect { KeywordRec kw -> kw.toMap() }
                updateMap['Data.keywords'] = keywordsCol
                //DocWrapper.collection.update(['_id': dw.id], [$set: ['Data.keywords': keywordsCol]])
            }
            if (bbUpdated) {
                updateMap['Data.spatial'] = dw.data.spatial.toMap()
                // DocWrapper.collection.update(['_id': dw.id],
                //         [$set: ['Data.spatial': dw.data.spatial.toMap()]])
            }
            updateMap['Processing.status'] = 'annotated.1'
            DocWrapper.collection.update(['_id': dw.id], [$set: updateMap])

            if (dw.history.prov) {
                ProvenanceData pd = ProvenanceHelper.prepAnnotationProvenance(dw, pi, username)
                DocWrapper.collection.update(['_id': dw.id],
                        [$set : ['History.prov.curVersion'       : pd.currentVersion,
                                 'History.prov.lastProcessedDate': pd.processedDate],
                         $push: ['History.prov.events': pd.provDBO]])
                //   DocWrapper.collection.update(['_id': dw.id],
                //           [$push: ['History.prov.events': pd.provDBO]])


            }
            dw = DocWrapper.findByPrimaryKey(dw.primaryKey)
        }

        return dw
    }

    private static BoundingBox find(BoundingBox bbRef, List<BoundingBoxRec> bbrList) {
        for (BoundingBoxRec bbRec : bbrList) {
            if (bbRec.southwest.lat == bbRef.latSouth &&
                    bbRec.southwest.lng == bbRef.lngWest &&
                    bbRec.northeast.lat == bbRef.latNorth &&
                    bbRec.northeast.lng == bbRef.lngEast) {
                return bbRec
            }
        }
        return null
    }

    private static String findKey(BoundingBox bbRef, Map<String, BoundingBoxRec> bbrMap) {
        String foundKey = null
        bbrMap.each { String k, BoundingBoxRec bbRec ->
            if (bbRec.southwest.lat == bbRef.latSouth &&
                    bbRec.southwest.lng == bbRef.lngWest &&
                    bbRec.northeast.lat == bbRef.latNorth &&
                    bbRec.northeast.lng == bbRef.lngEast) {
                foundKey = k
            }
        }
        return foundKey
    }
}
