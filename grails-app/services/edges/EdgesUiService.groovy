package edges

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure as MC
import org.springframework.beans.factory.annotation.Value
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.domain.TaackFilterService
import taack.ui.dsl.UiFilterSpecifier
import taack.ui.dsl.UiMenuSpecifier
import taack.ui.dsl.UiTableSpecifier
import taack.ui.dsl.common.Style

import javax.annotation.PostConstruct
import java.nio.file.Path

import static taack.render.TaackUiService.tr

@GrailsCompileStatic
class EdgesUiService implements WebAttributes {

    TaackFilterService taackFilterService

    static lazyInit = false

    @Value('${intranet.root}')
    String rootPath

    Path getEdgesKeystorePath() {
        Path.of(rootPath).resolve EdgesConf.keyStorePath
    }

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
        edgesKeystorePath.toFile().mkdirs()
    }

    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu EdgesController.&index as MC
        }
        m
    }

    UiFilterSpecifier computerFilter() {
        EdgeUser eu = new EdgeUser()
        EdgeComputer ec = new EdgeComputer()
        User u = new User()

        new UiFilterSpecifier().ui EdgeComputer, {
            section {
                filterField ec.computerOwner_, eu.baseUser_, u.username_
                filterField ec.name_
            }
        }
    }

    UiTableSpecifier computerTable() {
        EdgeComputer ec = new EdgeComputer()

        new UiTableSpecifier().ui {
            header {
                sortableFieldHeader ec.lastUpdated_
                sortableFieldHeader ec.userCreated_
                sortableFieldHeader ec.name_
                sortableFieldHeader ec.computerOwner_
            }

            iterate(taackFilterService.getBuilder(EdgeComputer).build()) { EdgeComputer computer ->
                rowField computer.lastUpdated_
                rowField computer.userCreated_
                rowField computer.name, Style.BOLD
                rowField computer.computerOwner_
            }
        }
    }
}

