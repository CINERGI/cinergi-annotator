import org.neuinfo.foundry.common.util.OntologyHandler
import org.neuinfo.foundry.common.util.ScigraphMappingsHandler
import org.neuinfo.foundry.common.util.ScigraphUtils

class BootStrap {

    def init = { servletContext ->
        ScigraphMappingsHandler handler = ScigraphMappingsHandler.getInstance()
        ScigraphUtils.setHandler(handler)
        OntologyHandler.getInstance()
    }
    def destroy = {
    }
}
