package cinergi.annotator

import grails.transaction.Transactional

@Transactional
class UserService {

    def createUser(String username, String pwd, String role = "curator", String email = null) {
        def users = User.findByUsername(username)
        if (!users) {
            User user = new User(username: username, password: pwd, role: role, email: email)
            user.save(failOnError: true)
            return user
        }
        return null
    }

    def authenticateUser(String username, String pwd) {
        def user = User.findByUsername(username)
        if (!user) {
            return null
        } else {
            if (user.password == pwd) {
                return user
            }
        }
        return null
    }

    def getAllUsers() {
        return User.list()
    }
}
