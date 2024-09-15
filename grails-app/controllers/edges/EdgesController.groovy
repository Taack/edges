package edges

import grails.web.api.WebAttributes
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured
import taack.render.TaackUiService
import taack.ui.dsl.UiBlockSpecifier

@GrailsCompileStatic
@Secured(['ROLE_ADMIN'])
class EdgesController implements WebAttributes {
    TaackUiService taackUiService
    EdgesUiService edgesUiService

    def index() {
        UiBlockSpecifier b = new UiBlockSpecifier().ui {
          custom "Hello World!"
        }
        taackUiService.show(b, edgesUiService.buildMenu())
    }

    def upload
}

