package cinergi.annotator

class UserController {
    def userService
    def beforeInterceptor = [action: this.&auth, except:['authenticate','login','home']]

    def auth() {
        if (!session.user) {
            redirect(controller: 'User', action:'home')
            return false
        }
    }
    def home() {
        if (!session.user) {
            redirect(controller: 'User', action:'login', params: params)
            return false
        }
        if (params.docId) {
            redirect(controller: 'Annotation', action: 'index')
        } else {
            redirect(controller: 'Source', action: 'showSources')
        }
    }

    def logout() {
        flash.message = "Goodbye ${session.user.loginId}"
        session.user = null
        redirect(action:'home')
    }

    def authenticate() {
        def userInstance = userService.authenticateUser(params.loginId, params.password)
        if (userInstance) {
            userInstance.password = null
            session.user = userInstance
            flash.message = "Hello ${userInstance.loginId}!"
            redirect(action:'home', params: params)
        } else {
            flash.message = "Could not authenticate '${params.loginId}'. Please Try again"
            redirect(action:'login')
        }
    }

    def login() {
        if (params.docId) {
            render(view: 'login', model:[docId: params.docId])
        }
    }

}
