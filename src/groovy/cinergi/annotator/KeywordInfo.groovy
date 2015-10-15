package cinergi.annotator

import groovy.transform.ToString

/**
 * Created by bozyurt on 3/9/15.
 */
@ToString
class KeywordInfo {
    int id
    String keyword
    String category

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        KeywordInfo that = (KeywordInfo) o

        if (category != that.category) return false
        if (keyword != that.keyword) return false

        return true
    }

    int hashCode() {
        int result
        result = keyword.hashCode()
        result = 31 * result + category.hashCode()
        return result
    }

}
