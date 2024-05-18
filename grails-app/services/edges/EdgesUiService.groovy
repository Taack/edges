package edges

import grails.compiler.GrailsCompileStatic
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import taack.ui.base.UiMenuSpecifier

import static taack.render.TaackUiService.tr

@GrailsCompileStatic
class EdgesUiService implements WebAttributes {

    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu tr("default.home.label"), EdgesController.&index as MethodClosure
        }
        m
    }
}

