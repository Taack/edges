package edges

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value

import javax.annotation.PostConstruct
import javax.crypto.SecretKey
import java.security.KeyStore
import java.security.KeyStore.TrustedCertificateEntry
import java.security.PrivateKey
import java.security.cert.Certificate


@CompileStatic
final class EdgesManageCertificatesService {

    @Value('${edges.storePasswd}')
    String storePasswd

    private KeyStore ks

    @PostConstruct
    void init() {
        ks = KeyStore.getInstance(KeyStore.getDefaultType())
        load()
    }

    void load() {
        char[] password = storePasswd.toCharArray()
        try (FileInputStream fis = new FileInputStream("edgesKS")) {
            ks.load(fis, password)
        }
    }

    void keystoreToFile(File otherKeyStore) {
        try (FileOutputStream fos = new FileOutputStream(otherKeyStore)) {
            char[] password = storePasswd.toCharArray()
            ks.store(fos, password)
        }
    }

    private void checkAlias(final String alias) {
        final String upperAlias = alias.toUpperCase()
        Iterator<String> itAliases = ks.aliases().asIterator()
        while (itAliases.hasNext()) {
            final String aliasKey = itAliases.next()
            if (aliasKey == alias)
                throw new IllegalArgumentException("Alias already existing in keystore: ${alias}")
            final String upperAliasKey = aliasKey.toUpperCase()
            if (upperAliasKey == upperAlias)
                throw new IllegalArgumentException("Alias already existing in keystore with different case: ${alias} match ${aliasKey} keystore entry")
        }
    }

    void saveSecretKey(SecretKey secretKey, String alias) {
        checkAlias(alias)
        KeyStore.SecretKeyEntry skEntry =
                new KeyStore.SecretKeyEntry(secretKey)
        KeyStore.ProtectionParameter protParam =
                new KeyStore.PasswordProtection(storePasswd.toCharArray())
        ks.setEntry(alias, skEntry, protParam)
    }

    void savePrivateKey(PrivateKey privateKey, String alias) {
        checkAlias(alias)
        KeyStore.PrivateKeyEntry pkEntry =
                new KeyStore.PrivateKeyEntry(privateKey)
        KeyStore.ProtectionParameter protParam =
                new KeyStore.PasswordProtection(storePasswd.toCharArray())
        ks.setEntry(alias, pkEntry, protParam)
    }

    void saveTrustedCertificateEntry(Certificate certificate, String alias) {
        checkAlias(alias)
        TrustedCertificateEntry tcEntry =
                new TrustedCertificateEntry(certificate)
        KeyStore.ProtectionParameter protParam =
                new KeyStore.PasswordProtection(storePasswd.toCharArray())
        ks.setEntry(alias, tcEntry, protParam)
    }

    void deleteEntryFromAlias(String alias) {
        ks.deleteEntry(alias)
    }
}
