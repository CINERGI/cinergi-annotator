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

    def asCSV() {
        StringBuilder sb = new StringBuilder(64000)
        sb.append('Source Name,Document ID,Action,Ontology ID,Old Term,Old Category, New Term, New Category, Update Date, Annotator\n' )
        DocWrapper.collection.find(['Processing.status'     : 'finished',
                                    'Data.annotatedKeywords': [$exists: 1]],
                ['primaryKey': 1, 'SourceInfo': 1, 'Data.annotatedKeywords': 1])
                .sort(['primaryKey': 1]).each { dw ->
            String resourceName = dw.SourceInfo.Name
            String pk = dw.primaryKey
            dw.Data.annotatedKeywords.each {  akr ->
                wrap(resourceName, sb)
                wrap(pk, sb)
                wrap(akr.annotationAction, sb)
                wrap(akr.ontologyId, sb)
                wrap(akr.oldTerm, sb)
                wrap(akr.oldCategory, sb)
                wrap(akr.newTerm, sb)
                wrap(akr.newCategory, sb)
                wrap(akr.lastChangedDate, sb)
                wrap(akr.annotator, sb, '\n')
            }
        }
        render(text: sb.toString(), contentType: "text/csv", encoding: "UTF-8")
    }

    private static String wrap(val, StringBuilder sb, String delim = ',') {
        if (val) {
            sb.append('"').append(val).append('"').append(delim)
        } else {
            sb.append(delim)
        }

    }

    def show() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        params.offset = Math.max(params.offset ? params.int('offset') : 0, 0)
        int totCount
        if (params.totCount) {
            totCount = params.int('totCount')
        } else {
            totCount = DocWrapper.collection.count(['Processing.status'     : 'finished',
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
