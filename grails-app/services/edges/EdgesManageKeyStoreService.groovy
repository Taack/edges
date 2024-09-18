package edges

import java.nio.file.Path

class EdgesManageKeyStoreService {

    final private Object cmdOnKeyStore = new Object()
    EdgesUiService edgesUiService

    private void executeCmd(String cmd) {
        synchronized (cmdOnKeyStore) {
            log.info "AUO EdgesManageKeyStoreService executing $cmd"
            Process p = cmd.execute()
            p.consumeProcessOutput()
            p.waitForOrKill(30 * 1000)
            int ev = p.exitValue()
            if (ev != 0) {
                log.info "AUO exit = ${ev}"
                log.error "${p.errorStream.text}"
            }
        }
    }

    Path ksPath(EdgeComputer computer) {
        edgesUiService.edgesKeystorePath.resolve(computer.keyStoreFileName)
    }

    private Path cerPath(EdgeComputer computer) {
        edgesUiService.edgesKeystorePath.resolve(computer.keyStoreEntryName + '.cer')
    }

    void createEdgeComputerKeyStore(EdgeComputer computer) {
        File ksf = ksPath(computer).toFile()

        if (!ksf.exists()) {
            String cmd = "keytool -alias ${computer.keyStoreEntryName} -dname CN=${computer.keyStoreEntryName} -genkeypair " +
                    "-keypass ${computer.keyStorePasswd} " +
                    "-storepass ${computer.keyStorePasswd} " +
                    "-keyalg RSA -keystore ${ksf.path}"
            executeCmd(cmd)
        }

        if (!ksf.exists()) {
            String msg = "Cannot create keyStore file ${ksf.path}"
            log.error(msg)
            throw new Exception(msg)
        }
    }

    void exportCertificate(EdgeComputer computer) {
        File ksf = ksPath(computer).toFile()
        File crf = cerPath(computer).toFile()

        if (ksf.exists() && !crf.exists()) {
            String cmd = "keytool  -alias ${computer.keyStoreEntryName} -exportcert " +
                    "-storepass ${computer.keyStorePasswd} " +
                    "-file ${crf.path} -keystore ${ksf.path}"
            executeCmd(cmd)
        }

        if (!crf.exists()) {
            String msg = "Cannot create certificate file ${crf.path}"
            log.error(msg)
            throw new Exception(msg)
        }
    }

    void importTrustCertificate(EdgeComputer fromComputer, EdgeComputer toComputer) {
        File ksf = ksPath(toComputer).toFile()
        File crf = cerPath(fromComputer).toFile()

        if (!ksf.exists()) createEdgeComputerKeyStore(toComputer)
        if (!crf.exists()) exportCertificate(fromComputer)

        if (ksf.exists() && crf.exists()) {
            String cmd = "keytool -noprompt -alias ${fromComputer.keyStoreEntryName} -importcert " +
                    "-trustcacerts -file ${crf.path} " +
                    "-keypass ${fromComputer.keyStorePasswd} " +
                    "-storepass ${toComputer.keyStorePasswd} " +
                    "-keystore ${ksf.path}"
            executeCmd(cmd)
        } else {
            String msg = "Cannot import certif ${crf.path} in ${ksf.path}"
            log.error(msg)
            throw new Exception(msg)
        }
    }

}
