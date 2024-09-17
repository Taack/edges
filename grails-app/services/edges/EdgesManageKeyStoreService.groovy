package edges

class EdgesManageKeyStoreService {

    void createEdgeComputerKeyStore(EdgeComputer computer) {

    }

    private void exportCertificate(EdgeComputer computer) {

    }

    void importTrustCertificate(EdgeComputer fromComputer, EdgeComputer toComputer) {

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
