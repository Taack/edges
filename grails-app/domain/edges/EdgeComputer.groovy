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

    String getKeyStoreFileName() {
        "${computerOwner.baseUser.username}-${name}-${id}.ks"
    }

    static constraints = {
        name(unique: ['computerOwner'])
    }
}
