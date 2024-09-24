package edges

import edges.EdgesConf.SecondaryServer
import grails.compiler.GrailsCompileStatic
import org.taack.edge.proto.FileTree

@GrailsCompileStatic
final class EdgesServerSocketService {

    private final class ComputerLatency {
        long lastPingReply
        long lastPingAll
        SecondaryServer associatedServer
    }

    final private Map<Long, ComputerLatency> latencyMap = [:]

    private static EdgeComputerMatcher createPingMatcher(EdgeComputer computer) {

    }

    private FileTree.UserFileBucket matcherToBucket(EdgeComputerMatcher matcher) {

    }

    private FileTree.UserFileBucket sendBucket(FileTree.UserFileBucket bucket) {

    }

    void setLatency(EdgeComputer computer) {

    }

    void refreshMatcher(EdgeComputerMatcher matcher) {
        // TODO get files meta data, compare with previous, ask new content
    }

}
