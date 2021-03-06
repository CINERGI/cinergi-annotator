package cinergi.annotator

class SourceController {
    def sourceService
    def beforeInterceptor = [action: this.&auth]

    def auth() {
        if (!session.user) {
            redirect(controller: 'User', action: 'home', params: params)
            return false
        }
    }

    def showSources() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = Math.max(params.offset ? params.int('offset') : 0, 0)
        boolean enhancedOnly = params.enhancedOnly ? Boolean.parseBoolean(params.enhancedOnly) : false
        int totCount = 0
        String allow = session.user.allow
        String selectedSource = null
        if (params.selectedSourceId) {
            // coming from source change
            selectedSource = params.selectedSourceId
        } else if (params.selectedSource) {
            selectedSource = params.selectedSource
        }


        def list = null;
        if (allow) {
            list = sourceService.findSources(allow)
        } else {
            list = sourceService.findAllSources()
        }
        List<SourceInfo> siList = new ArrayList<SourceInfo>(10)
        list.each { SourceRec sr ->
            SourceInfo si = new SourceInfo(resourceId: sr.sourceInformation.resourceID,
                    name: sr.sourceInformation.name, dataSource: sr.sourceInformation.dataSource)
            siList << si
        }
        SourceInfo sourceInfo = null
        if (!selectedSource) {
            sourceInfo = siList.get(0)
            selectedSource = sourceInfo.resourceId
        } else {
            sourceInfo = siList.find { SourceInfo si -> si.resourceId == selectedSource }
        }
        println "selectedSource:$selectedSource"
        def dwList = []
        if (params.totCount) {
            totCount = params.int('totCount')
        } else {
            if (enhancedOnly) {
                totCount = DocWrapper.collection.count(['SourceInfo.SourceID'  : sourceInfo.resourceId,
                                                        'Processing.status'    : 'finished',
                                                        'Data.enhancedKeywords': [$exists: 1]])
            } else {
                totCount = DocWrapper.collection.count(['SourceInfo.SourceID': sourceInfo.resourceId,
                                                        'Processing.status'  : 'finished'])
            }
        }
        if (enhancedOnly) {
            if (sourceInfo.resourceId in ['cinergi-0001', 'cinergi-0018']) {
                DocWrapper.collection.find(['SourceInfo.SourceID'  : sourceInfo.resourceId,
                                            'Processing.status'    : 'finished',
                                            'Data.enhancedKeywords': [$exists: 1]],
                        ['primaryKey': 1, 'CrawlDate': 1]).limit(params.max).skip(params.offset).each { dw ->
                    dwList << dw
                }
            } else {
                DocWrapper.collection.find(['SourceInfo.SourceID'  : sourceInfo.resourceId,
                                            'Processing.status'    : 'finished',
                                            'Data.enhancedKeywords': [$exists: 1]],
                        ['primaryKey': 1, 'CrawlDate': 1]).sort(['primaryKey': 1]).limit(params.max).skip(params.offset).each { dw ->
                    dwList << dw
                }
            }
        } else {
            if (sourceInfo.resourceId in ['cinergi-0001', 'cinergi-0018']) {
                // big data sets cannot sort
                DocWrapper.collection.find(['SourceInfo.SourceID': sourceInfo.resourceId,
                                            'Processing.status'  : 'finished'],
                        ['primaryKey': 1, 'CrawlDate': 1]).limit(params.max).skip(params.offset).each { dw ->
                    dwList << dw
                }
            } else {
                // .sort(['primaryKey': 1]).
                DocWrapper.collection.find(['SourceInfo.SourceID': sourceInfo.resourceId,
                                            'Processing.status'  : 'finished'],
                        ['primaryKey': 1, CrawlDate: 1]).sort(['primaryKey': 1]).limit(params.max).skip(params.offset).each { dw ->
                    dwList << dw
                }
            }
        }

        // println "dwList.size:" + dwList.size()
        // println "totalCount:" + totCount
        println "dwList" + dwList
        render(view: 'view', model: ['siList'        : siList, 'dwList': dwList, 'totCount': totCount,
                                     'selectedSource': selectedSource, enhancedOnly: enhancedOnly])

    }

}
