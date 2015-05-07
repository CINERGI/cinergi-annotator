class UrlMappings {

    static mappings = {
        // "/"(controller: "annotation") { action = "index" }
        "/"(controller: "user") { action = "home" }
        //   "/schema/$schemaName"(controller: "annotation") { action = "serveXmlSchema" }
        "/service/resourceEntered2Scicrunch"(controller: "annotation") { action = "resourceEntered2Scicrunch" }
        "500"(view: '/error')

        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }
    }
}
