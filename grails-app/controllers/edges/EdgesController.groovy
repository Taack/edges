package edges


import crew.User
import crew.config.SupportedLanguage
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.domain.TaackSaveService
import taack.render.TaackUiService
import taack.ui.EnumOption
import taack.ui.IEnumOption
import taack.ui.IEnumOptions
import taack.ui.dsl.UiBlockSpecifier
import taack.ui.dsl.UiMenuSpecifier
import taack.ui.dsl.UiShowSpecifier
import taack.ui.dsl.common.ActionIcon
import taack.ui.dsl.common.IconStyle
import taack.ui.dsl.common.Style

import static taack.render.TaackUiService.tr

@GrailsCompileStatic
@Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN', 'ROLE_EDGES_USER'])
class EdgesController implements WebAttributes {

    static scope = "session"

    TaackUiService taackUiService
    EdgesUiService edgesUiService
    EdgesManageKeyStoreService edgesManageKeyStoreService
    TaackSaveService taackSaveService
    SpringSecurityService springSecurityService

    private final static String myComputerId = 'myComputerId'
    Long currentComputerId = null

    private static IEnumOption createIEnumOption(EdgeComputer computer) {
        if (computer)
            return new EnumOption(computer.id.toString(), computer.name)
        else
            return new EnumOption("", tr('select.a.computer'))

    }

    private UiMenuSpecifier buildMenu(String q = null) {
        if (params.containsKey(myComputerId))
            currentComputerId = params.long(myComputerId)

        EdgeUser current = EdgeUser.findByBaseUser springSecurityService.currentUser as User
        List<EdgeComputer> myComputers = EdgeComputer.findAllByComputerOwner current

        new UiMenuSpecifier().ui {

            menu EdgesController.&listEdgeUser as MC
            menu EdgesController.&listEdgeComputer as MC

            if (this.currentComputerId) {
                menu EdgesController.&listEdgeComputerMatcher as MC, this.currentComputerId
            }

            menuIcon ActionIcon.DOWNLOAD, EdgesController.&downloadBinGlobalTrustStore as MC
            menuOptions(new IEnumOptions() {
                @Override
                IEnumOption[] getOptions() {
                    IEnumOption[] res = new IEnumOption[myComputers.size()]
                    EdgeComputer.findAllByComputerOwner(current).eachWithIndex { c, i ->
                        res[i] = createIEnumOption c
                    }
                    res
                }

                @Override
                IEnumOption[] getCurrents() {
                    EdgeComputer c = EdgeComputer.read(currentComputerId)
                    return [createIEnumOption(c)] as IEnumOption[]
                }

                @Override
                String getParamKey() {
                    return EdgesController.myComputerId
                }
            })
            menuSearch EdgesController.&search as MC, q
            menuOptions(SupportedLanguage.fromContext())
        }
    }

    def index() {
        redirect action: 'listEdgeUser'
    }

    def listEdgeComputer() {
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter(edgesUiService.computerFilter(), edgesUiService.computerTable()) {
                menu this.&listEdgeComputer as MC
                menuIcon ActionIcon.CREATE, this.&editEdgeComputer as MC
            }
        }, buildMenu())
    }

    def listEdgeUser() {
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter(edgesUiService.edgeUserFilter(), edgesUiService.edgeUserTable()) {
                menu this.&listEdgeUser as MC
                menuIcon ActionIcon.CREATE, this.&editEdgeUser as MC
            }
        }, buildMenu())
    }

    def search(String q) {
        taackUiService.show(edgesUiService.buildSearchBlock(q), buildMenu(q))
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

    def selectEdgeUser() {
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                tableFilter edgesUiService.edgeUserFilter(), edgesUiService.edgeUserTable(true), {
                    label(tr('default.select.label'))
                    menuIcon ActionIcon.ADD, EdgesController.&editEdgeUser as MC
                }
            }
        })
    }

    def downloadBinKeyStore(EdgeComputer computer) {
        edgesManageKeyStoreService.createAll()
        response.setHeader("Content-disposition", "attachment;filename=${computer.keyStoreFileName}")
        response.outputStream << edgesManageKeyStoreService.ksPath(computer).toFile().bytes
        try {
            response.outputStream.flush()
            response.outputStream.close()
        } catch (e) {
            log.error "${e.message}"
        }
    }

    def downloadBinGlobalTrustStore() {
        edgesManageKeyStoreService.createAll()
        File ts = edgesUiService.globalTrustStorePath.toFile()
        response.setHeader("Content-disposition", "attachment;filename=${ts.name}")
        response.outputStream << ts.bytes
        try {
            response.outputStream.flush()
            response.outputStream.close()
        } catch (e) {
            log.error "${e.message}"
        }
    }

    @Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN'])
    def editEdgeUser(EdgeUser user) {
        user ?= new EdgeUser()
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form edgesUiService.editUser(user)
            }
        })
    }

    @Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN'])
    def editEdgeComputer(EdgeComputer edgeComputer) {
        edgeComputer ?= new EdgeComputer()
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form edgesUiService.editComputer(edgeComputer)
            }
        })
    }

    def editEdgeComputerMatcher(EdgeComputerMatcher edgeComputerMatcher) {
        edgeComputerMatcher ?= new EdgeComputerMatcher(computer: EdgeComputer.get(params.long('computerId')))
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form edgesUiService.editEdgeComputerMatcher(edgeComputerMatcher)
            }
        })
    }

    def listEdgeComputerMatcher(EdgeComputer computer) {
        taackUiService.show(new UiBlockSpecifier().ui {
                table edgesUiService.listEdgeComputerMatcher(computer), {
                    menuIcon ActionIcon.ADD * IconStyle.SCALE_DOWN, EdgesController.&editEdgeComputerMatcher as MC, [computerId: computer.id]
                }
        }, buildMenu())
    }

    @Transactional
    @Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN'])
    def saveEdgeComputer() {
        taackSaveService.saveThenReloadOrRenderErrors(EdgeComputer)
    }

    @Transactional
    @Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN'])
    def saveEdgeUser() {
        taackSaveService.saveThenReloadOrRenderErrors(EdgeUser)
    }

    @Transactional
    @Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN'])
    def saveEdgeComputerMatcher() {
        taackSaveService.saveThenReloadOrRenderErrors(EdgeComputerMatcher)
    }

}

