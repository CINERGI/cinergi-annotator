package cinergi.annotator

import org.bson.types.ObjectId

class User {
    static mapWith = "mongo"
    ObjectId id
    String username
    String password
    String role
    String email
    String allow
    Date dateCreated

    static constraints = {
        username blank:false
        password blank: false
        email nullable: true
        allow nullable: true
    }
    static mapping = {
        collection "users"
        username index:true
        version false
    }


}
