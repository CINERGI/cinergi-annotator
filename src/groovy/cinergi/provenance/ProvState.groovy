package cinergi.provenance

/**
 * Created by bozyurt on 5/19/15.
 */
class ProvState {
    final String curVersion
    final Date lastProcessedDate

    public ProvState(String curVersion, Date lastProcessedDate) {
        this.curVersion = curVersion
        this.lastProcessedDate = lastProcessedDate
    }

}
