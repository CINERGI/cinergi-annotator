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

    static String toMsg(String prefix, String rest) {
        if (prefix) {
            if (Character.isUpperCase(rest.charAt(0))) {
                return "$prefix" + rest.substring(0, 1).toLowerCase() + rest.substring(1)
            } else {
                return "$prefix$rest"
            }
        } else {
            return rest
        }
    }

    def prepHowMessage(String username) {
        StringBuilder sb = new StringBuilder(200)
        String prefix = username ? "$username " : ''
        if (newKeywords) {
            String s = prepKeywordHowMessage(toMsg(prefix, 'Added keyword'), newKeywords)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (deletedKeywords) {
            String s = prepKeywordHowMessage(toMsg(prefix, 'Removed keyword'), deletedKeywords)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (updatedKeywords) {
            String s = prepKeywordHowMessage(toMsg(prefix, 'Updated keywords'), updatedKeywords)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (updatedBBs) {
            String s = prepBoundingBoxHowMessage(toMsg(prefix, "Updated spatial extent"), updatedBBs)
            if (s) {
                sb.append(s).append(' ')
            }
        }
        if (deletedBBs) {
            String s = prepBoundingBoxHowMessage(toMsg(prefix, "Removed spatial extent"), deletedBBs)
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
                sb.append(' for ').append(bb.text)
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

    def prepKeywordHowMessage(String singularPrefix, List<KeywordInfo> kiList) {
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
                sb.append(singularPrefix).append('s ').append(list.join(','))
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
