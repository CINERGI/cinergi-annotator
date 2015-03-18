package cinergi.annotator

/**
 * Created by bozyurt on 3/11/15.
 */
class ProvenanceInfo {
    List<KeywordInfo> updatedKeywords = []
    List<KeywordInfo> deletedKeywords = []
    List<KeywordInfo> newKeywords = []
    List<BoundingBox> updatedBBs = []
    List<BoundingBox> deletedBBs = []
    List<BoundingBox> newBBs = []

    def prepHowMessage() {
        StringBuilder sb = new StringBuilder(200)
        if (newKeywords) {
            String s = prepKeywordHowMessage('Added keywords', 'Added keyword', newKeywords)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (deletedKeywords) {
            String s = prepKeywordHowMessage('Removed keywords', 'Removed keyword', deletedKeywords)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (updatedKeywords) {
            String s = prepKeywordHowMessage('Updated keywords', 'Updated keyword', updatedKeywords)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (updatedBBs) {
            String s = prepBoundingBoxHowMessage("Updated spatial extent", updatedBBs)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (deletedBBs) {
            String s = prepBoundingBoxHowMessage("Removed spatial extent", deletedBBs)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        return sb.toString()
    }

    def prepBoundingBoxHowMessage(String prefix, List<BoundingBox> bbList) {
        StringBuilder sb = new StringBuilder(200)
        for (BoundingBox bb : bbList) {
            sb.append(prefix)
            if (bb.text) {
                sb.append(' for ').append(place)
            }
            sb.append(' with value ')
            sb.append('south lat:').append(bb.latSouth)
            sb.append(', west lng:').append(bb.lngWest)
            sb.append(', north lat:').append(bb.latNorth)
            sb.append(', east lng:').append(bb.lngEast)
            sb.append('. ')
        }
        sb.toString().trim()
    }

    def prepKeywordHowMessage(String pluralPrefix, String singularPrefix, List<KeywordInfo> kiList) {
        def map = [:]
        kiList.each { KeywordInfo ki ->
            Set<String> kwSet = map[ki.category]
            if (!kwSet) {
                kwSet = new HashSet<>(11)
                map[ki.category] = kwSet
            }
            kwSet << ki.keyword
        }
        StringBuilder sb = new StringBuilder(200)
        map.each { String category, Set<String> list ->
            if (list.size() > 1) {
                sb.append(pluralPrefix).append(' ').append(list.join(','))
                        .append(" for category ").append(category)
                        .append(". ")
            } else {
                sb.append(singularPrefix).append(' ').append(list[0]).
                        append(" for category ").append(category)
                        .append(". ")
            }
        }
        return sb.toString().trim()
    }

}
