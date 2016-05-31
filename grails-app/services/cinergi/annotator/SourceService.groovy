package cinergi.annotator

import grails.transaction.Transactional

@Transactional
class SourceService {

    def findAllSources() {
        return SourceRec.list()
    }

    def findSources(String sourceID) {
        return SourceRec.findAll({ eq('sourceInformation.resourceID', sourceID) })
    }
}
