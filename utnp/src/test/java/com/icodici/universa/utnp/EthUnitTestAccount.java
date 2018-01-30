package com.icodici.universa.utnp;

import com.icodici.universa.utnp.ethereum.EthereumUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * The class to store the credentials and the private key (sic!)
 * for the single Ethereum account for unit tests.
 */
class EthUnitTestAccount {

    @NonNull
    public final String address;
    @NonNull
    public final byte[] privateKey;

    EthUnitTestAccount(@NonNull String address, @NonNull byte[] privateKey) {
        assert address != null;
        assert privateKey != null;

        assert address.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : address;
        assert privateKey.length == EthereumUtils.PRIVATE_KEY_SIZE : privateKey.length;

        this.address = address;
        this.privateKey = privateKey;
    }

    EthUnitTestAccount(@NonNull String address, @NonNull String privateKeyStr) {
        this(address, Hex.decode(privateKeyStr));
        assert address != null;
        assert privateKeyStr != null;
    }

    EthUnitTestAccount(@NonNull ECKeyPair ecKeyPair) {
        this(
                "0x" + Keys.getAddress(ecKeyPair),
                Numeric.toBytesPadded(ecKeyPair.getPrivateKey(), EthereumUtils.PRIVATE_KEY_SIZE)
        );
        assert ecKeyPair != null;
    }

    public static final EthUnitTestAccount makeRandom()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        return new EthUnitTestAccount(Keys.createEcKeyPair());
    }

    final Credentials getCredentials() {
        return Credentials.create(ECKeyPair.create(privateKey));
    }
}
