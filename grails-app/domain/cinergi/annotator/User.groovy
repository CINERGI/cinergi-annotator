package cinergi.annotator

import org.bson.types.ObjectId

class User {
    static mapWith = "mongo"
    ObjectId id
    String username
    String password
    String role
    String email
    Date dateCreated

    static constraints = {
        username blank:false
        password blank: false
        email nullable: true
    }
    static mapping = {
        collection "users"
        database "discotest"
        username index:true
        version false
    }
}
