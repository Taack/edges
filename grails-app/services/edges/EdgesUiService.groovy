package edges

import grails.compiler.GrailsCompileStatic
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.ui.dsl.UiMenuSpecifier

import javax.annotation.PostConstruct

@GrailsCompileStatic
class EdgesUiService implements WebAttributes {

    static lazyInit = false

    @PostConstruct
    void init() {
        TaackAppRegisterService.register(
                new TaackApp(
                        EdgesController.&index as MC,
                        new String(
                                this.class
                                        .getResourceAsStream("/edges/edges.svg")
                                        .readAllBytes()
                        )
                )
        )

    }

    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu EdgesController.&index as MC
        }
        m
    }
}

