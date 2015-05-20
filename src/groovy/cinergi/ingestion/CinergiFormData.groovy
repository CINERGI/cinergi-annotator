package cinergi.ingestion

import org.neuinfo.foundry.common.model.ScicrunchResourceRec.UserKeyword

/**
 * Created by bozyurt on 5/18/15.
 */
class CinergiFormData {
    String email
    String title
    String description
    List<UserKeyword> userKeywords = new ArrayList<UserKeyword>()
}
