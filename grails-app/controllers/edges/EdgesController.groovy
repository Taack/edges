package edges

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.render.TaackUiService
import taack.ui.dsl.UiBlockSpecifier
import taack.ui.dsl.UiShowSpecifier
import taack.ui.dsl.common.Style

@GrailsCompileStatic
@Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN', 'ROLE_EDGES_USER'])
class EdgesController implements WebAttributes {

    TaackUiService taackUiService
    EdgesUiService edgesUiService

    def index() {
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter(edgesUiService.computerFilter(), edgesUiService.computerTable()) {
                menu this.&index as MC
            }
        }, edgesUiService.buildMenu())
    }

    def search(String q) {
        taackUiService.show(edgesUiService.buildSearchBlock(q), edgesUiService.buildMenu(q))
    }

    def showComputer(EdgeComputer computer) {
        User u = new User()
        EdgeUser eu = new EdgeUser()
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show(new UiShowSpecifier().ui {
                    field computer.computerOwner.baseUser.rawImg
                    fieldLabeled Style.BOLD + Style.BLUE, computer.name_
                    fieldLabeled computer.userCreated_
                    fieldLabeled computer.dateCreated_
                    fieldLabeled computer.computerOwner_, eu.baseUser_, u.username_
                })
            }
        })
    }
}

