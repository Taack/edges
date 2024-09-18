package edges

import grails.compiler.GrailsCompileStatic

import java.nio.file.Path

@GrailsCompileStatic
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

    private void createEdgeComputerKeyStore(EdgeComputer computer) {
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

    private void exportCertificate(EdgeComputer computer) {
        File ksf = ksPath(computer).toFile()
        File crf = cerPath(computer).toFile()

        if (ksf.exists() && !crf.exists()) {
            String cmd = "keytool -alias ${computer.keyStoreEntryName} -exportcert " +
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

    private void importTrustCertificate(EdgeComputer fromComputer) {
        File ksf = edgesUiService.globalTrustStorePath.toFile()
        File crf = cerPath(fromComputer).toFile()

        if (crf.exists()) {
            String cmd = "keytool -noprompt -alias ${fromComputer.keyStoreEntryName} -importcert " +
                    "-trustcacerts -file ${crf.path} " +
                    "-keypass ${fromComputer.keyStorePasswd} " +
                    "-storepass globalTruststorePass " +
                    "-keystore ${ksf.path}"
            executeCmd(cmd)
        } else {
            String msg = "Cannot import certif ${crf.path} in ${ksf.path}"
            log.error(msg)
            throw new Exception(msg)
        }
    }

    void createAll() {
        EdgeComputer.all.each {
            createEdgeComputerKeyStore it
            exportCertificate it
        }

        edgesUiService.globalTrustStorePath.toFile().delete()

        EdgeComputer.all.each {
            importTrustCertificate it
        }
    }
}
