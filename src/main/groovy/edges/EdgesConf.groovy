package edges

import java.nio.file.Path

final class EdgesConf {
    static final Path keyStorePath = Path.of('edges', 'ks')

    long pingAllToAllDelay = 1_000 * 60 * 60 * 3
    long checkIsConnectedDelay = 1_000 * 60 * 5
    long bucketChunksCount = 2 >> 8

    enum SecondaryServer {
        S1('url1', 8811),
        S2('url2', 8812)

        SecondaryServer(String symbolicAddress, int port) {
            this.symbolicAddress = symbolicAddress
            this.port = port
        }

        final String symbolicAddress
        final int port
    }
}
