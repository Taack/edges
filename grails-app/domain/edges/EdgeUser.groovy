package edges

import crew.User
import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

@TaackFieldEnum
@GrailsCompileStatic
class EdgeUser {

    Date dateCreated
    Date lastUpdated
    User userCreated
    User userUpdated

    User baseUser

    Set<EdgeComputer> computers

    static constraints = {
    }

    static hasMany = [
            computers: EdgeComputer
    ]
}