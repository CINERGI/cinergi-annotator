package cinergi.provenance

/**
 * Created by bozyurt on 5/18/15.
 */
class ProvData {
    String sourceName
    String srcId
    String batchId
    String version = "1.0"
    String docTitle
    String docIdentifier
    List<String> modifiedList = new ArrayList<String>(5)
    ModificationType modType

    public ProvData(String docIdentifier, ModificationType modType) {
        this.modType = modType
        this.docIdentifier = docIdentifier
    }

    public ProvData setDocTitle(String docTitle) {
        this.docTitle = docTitle
        return this
    }

    public ProvData setSourceName(String sourceName) {
        this.sourceName = sourceName
        return this
    }

    public void addModifiedFieldProv(String fieldProv) {
        this.modifiedList.add(fieldProv)
    }

    public ProvData setSrcId(String srcId) {
        this.srcId = srcId;
        return this;
    }

    public ProvData setVersion(String version) {
        this.version = version
        return this
    }

    public String prepLabel() {
        StringBuilder sb = new StringBuilder(80)
        sb.append(sourceName).append(":").append(docIdentifier)
        if (docTitle != null && docTitle.isEmpty()) {
            sb.append("::").append(docTitle)
        }
        return sb.toString()
    }

    public String prepLabelHow() {
        StringBuilder sb = new StringBuilder(128);
        if (!this.modifiedList.isEmpty()) {
            for (Iterator<String> iter = modifiedList.iterator(); iter.hasNext();) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(". ")
                } else {
                    sb.append('.')
                }
            }
        } else {
            if (this.modType == ModificationType.Ingested) {
                sb.append("Added document with uuid ").append(docIdentifier)
            }
        }
        return sb.toString();
    }
}


public enum ModificationType {
    Added, Modified, Deleted, Ingested, None
}
