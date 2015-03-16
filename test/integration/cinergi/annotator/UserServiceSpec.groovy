package cinergi.annotator

import grails.test.spock.IntegrationSpec

/**
 * Created by bozyurt on 3/10/15.
 */
class UserServiceSpec extends IntegrationSpec {
    def userService

    void "test user creation and listing"() {
        User user = null
        when: "createUser is called"
        user = userService.createUser("admin", "xxx", 'admin', '')
        then: "you should have a new user created"
        user
    }

    void "test user authentication"() {
        User authenticated = null
        when: "authenticate is called with an existing user's credentials"
        authenticated = userService.authenticateUser('admin','xxx')
        then:"you should get authenticated user record"
        authenticated
    }
}
