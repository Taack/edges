package edges

import crew.AttachmentController
import crew.CrewController
import crew.User
import crew.config.SupportedLanguage
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import org.codehaus.groovy.runtime.MethodClosure as MC
import org.grails.datastore.gorm.GormEntity
import org.springframework.beans.factory.annotation.Value
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.domain.TaackFilterService
import taack.domain.TaackSearchService
import taack.render.TaackUiEnablerService
import taack.solr.SolrFieldType
import taack.solr.SolrSpecifier
import taack.ui.dsl.*
import taack.ui.dsl.common.ActionIcon
import taack.ui.dsl.common.IconStyle
import taack.ui.dsl.common.Style

import javax.annotation.PostConstruct
import java.nio.file.Path

import static taack.render.TaackUiService.tr

@GrailsCompileStatic
class EdgesUiService implements TaackSearchService.IIndexService {

    TaackFilterService taackFilterService
    TaackSearchService taackSearchService
    SpringSecurityService springSecurityService

    static lazyInit = false

    @Value('${intranet.root}')
    String rootPath

    Path getEdgesKeystorePath() {
        Path.of(rootPath).resolve EdgesConf.keyStorePath
    }

    Path getGlobalTrustStorePath() {
        edgesKeystorePath.resolve('globalTruststore.jks')
    }

    @PostConstruct
    void init() {
        TaackAppRegisterService.register(
                new TaackApp(
                        EdgesController.&listEdgeUser as MC,
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

        TaackUiEnablerService.securityClosure({ Long id, Map p ->
            if (id) canDownload(EdgeComputer.read(id))
            else true
        },
                EdgesController.&downloadBinKeyStore as MC,
                EdgesController.&listEdgeComputerMatcher as MC,
                EdgesController.&editEdgeComputer as MC
        )
    }

    boolean canDownload(EdgeComputer ec) {
        ec.computerOwner.baseUser.id == springSecurityService.currentUserId
    }

    UiMenuSpecifier buildMenu(String q = null) {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu EdgesController.&listEdgeUser as MC
            menu EdgesController.&listEdgeComputer as MC
            menuIcon ActionIcon.DOWNLOAD, EdgesController.&downloadBinGlobalTrustStore as MC
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
                rowColumn {
                    rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, EdgesController.&editEdgeComputer as MC, computer.id
                    rowAction ActionIcon.DOWNLOAD * IconStyle.SCALE_DOWN, EdgesController.&downloadBinKeyStore as MC, computer.id
                    rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, EdgesController.&listEdgeComputerMatcher as MC, computer.id
                    rowField computer.name, Style.BOLD
                }
                rowField computer.computerOwner_
            }
        }
    }

    UiFilterSpecifier edgeUserFilter() {
        EdgeUser eu = new EdgeUser()
        EdgeComputer ec = new EdgeComputer()
        User u = new User()

        new UiFilterSpecifier().ui EdgeUser, {
            section {
                filterField eu.baseUser_, u.username_
                filterField eu.computers_, ec.name_
            }
        }
    }


    UiTableSpecifier edgeUserTable(boolean selectMode = false) {
        EdgeComputer ec = new EdgeComputer()
        EdgeUser eu = new EdgeUser()
        User u = new User()

        new UiTableSpecifier().ui {
            header {
                sortableFieldHeader eu.lastUpdated_
                sortableFieldHeader eu.userCreated_
                sortableFieldHeader eu.baseUser_, u.username_
                label eu.computers_
            }

            iterate(taackFilterService.getBuilder(EdgeUser).build()) { EdgeUser edgeUser ->
                rowField edgeUser.lastUpdated_
                rowField edgeUser.userCreated_
                rowColumn {
                    if (selectMode) {
                        rowAction tr('default.select.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, edgeUser.id, edgeUser.toString()
                    } else {
                        rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, EdgesController.&editEdgeUser as MC, edgeUser.id
                    }
                    rowField edgeUser.baseUser.username, Style.BOLD
                }
                rowField edgeUser.computers*.name?.join(', ')
            }
        }
    }

    String labeling(Long id) {
        def ec = EdgeComputer.read(id)
        "Computer: ${ec.name} owner ${ec.computerOwner.baseUser.username} ($id)"
    }

    UiFormSpecifier editComputer(EdgeComputer computer) {
        new UiFormSpecifier().ui computer, {
            section {
                field computer.name_
                field computer.keyStorePasswd_
                field computer.server_
                ajaxField computer.computerOwner_, EdgesController.&selectEdgeUser as MC
            }
            formAction EdgesController.&saveEdgeComputer as MC
        }
    }

    UiFormSpecifier editUser(EdgeUser user) {
        new UiFormSpecifier().ui user, {
            section {
                ajaxField user.baseUser_, CrewController.&selectUserM2O as MC
            }
            formAction EdgesController.&saveEdgeUser as MC
        }
    }

    @Override
    List<? extends GormEntity> indexThose(Class<? extends GormEntity> toIndex) {
        if (toIndex.isAssignableFrom(User)) return User.findAllByEnabled(true)
        else null
    }

    UiBlockSpecifier buildSearchBlock(String q) {
        taackSearchService.search(q, EdgesController.&search as MC, User)
    }

    UiFormSpecifier editEdgeComputerMatcher(EdgeComputerMatcher matcher) {
        new UiFormSpecifier().ui EdgeComputerMatcher, {
            hiddenField matcher.computer_
            row {
                col {
                    section('Matcher') {
                        field matcher.rootPath_
                        field matcher.contentTypeEnumSet_
                        field matcher.contentTypeCategoryEnum_
                    }
                }
                col {
                    section('Security') {
                        field matcher.descend_
                        ajaxField matcher.documentAccess_, AttachmentController.&editAttachmentDescriptor as MC
                    }
                }
            }
            formAction EdgesController.&saveEdgeComputerMatcher as MC
        }
    }

    UiTableSpecifier listEdgeComputerMatcher(EdgeComputer computer) {
        EdgeComputerMatcher ecm = new EdgeComputerMatcher()
        new UiTableSpecifier().ui {
            header {
                sortableFieldHeader ecm.fileExt_
                sortableFieldHeader ecm.filePattern_
                sortableFieldHeader ecm.rootPath_
                label ecm.contentTypeEnumSet_
                sortableFieldHeader ecm.contentTypeCategoryEnum_
            }

            EdgeComputerMatcher.findAllByComputer(computer)*.id

            iterate(taackFilterService.getBuilder(EdgeComputerMatcher)
                    .addRestrictedIds(EdgeComputerMatcher.findAllByComputer(computer)*.id as Long[])
                    .build()) { EdgeComputerMatcher ecmIt ->
                rowField ecmIt.fileExt
                rowField ecmIt.filePattern
                rowField ecmIt.rootPath
                rowField ecmIt.contentTypeEnumSet_
                rowField ecmIt.contentTypeCategoryEnum_
            }
        }
    }
}

