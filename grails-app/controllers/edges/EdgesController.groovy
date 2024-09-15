package edges

import grails.web.api.WebAttributes
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.render.TaackUiService
import taack.ui.dsl.UiBlockSpecifier


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
}

