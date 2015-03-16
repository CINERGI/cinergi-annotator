package cinergi.annotator

import grails.transaction.Transactional

@Transactional
class SourceService {

    def findAllSources() {
        return SourceRec.list()
    }
}
