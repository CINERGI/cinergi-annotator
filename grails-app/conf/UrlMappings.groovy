class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }

        // "/"(controller: "annotation") { action = "index" }
        "/"(controller: "user") { action = "home" }
     //   "/schema/$schemaName"(controller: "annotation") { action = "serveXmlSchema" }
        "500"(view: '/error')
    }
}
