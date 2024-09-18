package edges

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.domain.TaackSaveService
import taack.render.TaackUiService
import taack.ui.dsl.UiBlockSpecifier
import taack.ui.dsl.UiShowSpecifier
import taack.ui.dsl.common.ActionIcon
import taack.ui.dsl.common.Style

import static taack.render.TaackUiService.tr

@GrailsCompileStatic
@Secured(['ROLE_ADMIN', 'ROLE_EDGES_ADMIN', 'ROLE_EDGES_USER'])
class EdgesController implements WebAttributes {

    TaackUiService taackUiService
    EdgesUiService edgesUiService
    EdgesManageKeyStoreService edgesManageKeyStoreService
    TaackSaveService taackSaveService

    def index() {
        redirect action: 'listEdgeUser'
    }

    def listEdgeComputer() {
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter(edgesUiService.computerFilter(), edgesUiService.computerTable()) {
                menu this.&listEdgeComputer as MC
                menuIcon ActionIcon.CREATE, this.&editEdgeComputer as MC
            }
        }, edgesUiService.buildMenu())
    }

    def listEdgeUser() {
        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter(edgesUiService.edgeUserFilter(), edgesUiService.edgeUserTable()) {
                menu this.&listEdgeUser as MC
                menuIcon ActionIcon.CREATE, this.&editEdgeUser as MC
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
}

