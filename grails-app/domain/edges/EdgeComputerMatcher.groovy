package edges

import attachment.Attachment
import attachment.DocumentAccess
import attachment.TaackDocument
import attachment.Term
import attachment.config.AttachmentContentType
import attachment.config.AttachmentContentTypeCategory
import crew.User
import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

@TaackFieldEnum
@GrailsCompileStatic
class EdgeComputerMatcher {

    Date dateCreated
    Date lastUpdated
    User userCreated
    User userUpdated

    String fileExt
    String filePattern
    String rootPath
    boolean descend = false

    Set<AttachmentContentType> contentTypeEnumSet
    AttachmentContentTypeCategory contentTypeCategoryEnum

    DocumentAccess documentAccess
    EdgeComputer computer
    Set<Attachment> attachmentSet

    static constraints = {
        fileExt nullable: true
        filePattern nullable: true
        contentTypeCategoryEnum nullable: true
    }

    static hasMany = [
            attachmentSet     : Attachment,
            contentTypeEnumSet: AttachmentContentType
    ]
}
