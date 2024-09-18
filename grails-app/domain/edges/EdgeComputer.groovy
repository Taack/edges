package edges

import crew.User
import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

@TaackFieldEnum
@GrailsCompileStatic
class EdgeComputer {

    Date dateCreated
    Date lastUpdated
    User userCreated
    User userUpdated

    String name
    String keyStorePasswd
    EdgeUser computerOwner
    boolean server = false

    String getKeyStoreEntryName() {
        "${computerOwner.baseUser.username}-${name}"
    }

    String getKeyStoreFileName() {
        "${keyStoreEntryName}-${id}.jks"
    }

    static constraints = {
        name unique: ['computerOwner'], validator: { String s ->
            if (!s.matches(/[A-Za-z0-9]+/))
                return 'name.alphanum.only.validator'
            else return true
        }
    }
}
