package edges

import crew.User
import crew.config.SupportedLanguage
import grails.compiler.GrailsCompileStatic
import org.codehaus.groovy.runtime.MethodClosure as MC
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Value
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.domain.TaackFilterService
import taack.domain.TaackSearchService
import taack.solr.SolrFieldType
import taack.solr.SolrSpecifier
import taack.ui.dsl.UiBlockSpecifier
import taack.ui.dsl.UiFilterSpecifier
import taack.ui.dsl.UiMenuSpecifier
import taack.ui.dsl.UiTableSpecifier
import taack.ui.dsl.common.Style

import javax.annotation.PostConstruct
import java.nio.file.Path

@GrailsCompileStatic
class EdgesUiService implements TaackSearchService.IIndexService {

    TaackFilterService taackFilterService
    TaackSearchService taackSearchService

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
        taackSearchService.registerSolrSpecifier(this,
                new SolrSpecifier(EdgeComputer,
                        EdgesController.&showComputer as MC,
                        this.&labeling as MC, { EdgeComputer ec ->
                    indexField SolrFieldType.TXT_NO_ACCENT, ec.name_
                    indexField SolrFieldType.TXT_GENERAL, ec.name_
                    indexField SolrFieldType.POINT_STRING, "mainSubsidiary", true, ec.computerOwner.baseUser.subsidiary?.toString()
                    indexField SolrFieldType.POINT_STRING, "businessUnit", true, ec.computerOwner.baseUser.businessUnit?.toString()
                    indexField SolrFieldType.DATE, 0.5f, true,
                            ec.dateCreated_
                    indexField SolrFieldType.POINT_STRING,
                            "userCreated",  // Faceting String
                            0.5f,           // Boost factor
                            true, ec.userCreated?.username

                })
        )
    }

    UiMenuSpecifier buildMenu(String q = null) {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu EdgesController.&index as MC
            menuSearch EdgesController.&search as MC, q
            menuOptions(SupportedLanguage.fromContext())
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

    String labeling(Long id) {
        def ec = EdgeComputer.read(id)
        "Computer: ${ec.name} owner ${ec.computerOwner.baseUser.username} ($id)"
    }

    @Override
    List<? extends GormEntity> indexThose(Class<? extends GormEntity> toIndex) {
        if (toIndex.isAssignableFrom(User)) return User.findAllByEnabled(true)
        else null
    }

    UiBlockSpecifier buildSearchBlock(String q) {
        taackSearchService.search(q, EdgesController.&search as MC, User)
    }
}

