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
            String cmd = "keytool -dname CN=${computer.keyStoreEntryName} -genkey " +
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
            String cmd = "keytool -export " +
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

        if (ksf.exists() && crf.exists()) {
            String cmd = "keytool -import " +
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


/*
    1.
    ~/.jdks/jbrsdk-17.0.9-1/bin/keytool -genkey -keypass password -storepass password -keyalg RSA -keystore serverkeystore.jks
    Quels sont vos nom et prénom ?
    [Unknown]:  localhost
    Quel est le nom de votre unité organisationnelle ?
    [Unknown]:  CiO

    2.
    ~/.jdks/jbrsdk-17.0.9-1/bin/keytool -export -storepass password -file server.cer -keystore serverkeystore.jks

    3.
    ~/.jdks/jbrsdk-17.0.9-1/bin/keytool -import -v -trustcacerts -file server.cer -keypass password -storepass password -keystore clienttruststore.jks
    */
}
