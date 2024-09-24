package edges

import attachment.Attachment
import org.taack.edge.proto.FileTree.UserFile

/**
 * Attachment are tagged with edgeUser, root dir
 * Matcher file pattern should be build according to Attachment type
 * First file 24 bytes should identify the file type.
 *
 * First we upload, then we create new attachment version explicitly
 */
class EdgesManageAttachmentFromMatcher {

    void displayFileBucketContent(EdgeComputerMatcher matcher) {

    }

    Attachment createAttachment(EdgeComputerMatcher matcher, UserFile file) {

    }

    void displayCurrentFiles(EdgeComputerMatcher matcher) {

    }
}
