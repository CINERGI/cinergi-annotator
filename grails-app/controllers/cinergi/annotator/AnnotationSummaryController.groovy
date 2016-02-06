package cinergi.annotator

class AnnotationSummaryController {
    def sourceService
    def beforeInterceptor = [action: this.&auth]

    def auth() {
        if (!session.user) {
            redirect(controller: 'User', action: 'home', params: params)
            return false
        }
    }

    def show() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = Math.max(params.offset ? params.int('offset') : 0, 0)
        int totCount
        if (params.totCount) {
            totCount = params.int('totCount')
        } else {
            totCount = DocWrapper.collection.count(['Processing.status'    : 'finished',
                                                    'Data.annotatedKeywords': [$exists: 1]])
        }
        def dwList = []
        DocWrapper.collection.find(['Processing.status'     : 'finished',
                                    'Data.annotatedKeywords': [$exists: 1]],
                ['primaryKey': 1, 'SourceInfo': 1, 'Data.annotatedKeywords': 1])
                .sort(['primaryKey': 1]).limit(params.max).skip(params.offset).each { dw ->
            dwList << dw
        }
        println dwList
        render(view: 'view', model: ['dwList': dwList, 'totCount': totCount])
    }
}
