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
    EdgeUser computerOwner
    boolean server = false

    String getKeyStoreFileName() {
        "${computerOwner.baseUser.username}-${name}-${id}.ks"
    }

    static constraints = {
        name unique: ['computerOwner'], validator: { String s ->
            if (!s.matches(/[A-Za-z0-9]+/))
                return 'name.alphanum.only.validator'
            else return true
        }
    }
}
