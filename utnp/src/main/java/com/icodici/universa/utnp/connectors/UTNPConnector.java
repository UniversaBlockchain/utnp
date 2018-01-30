package com.icodici.universa.utnp.connectors;

import com.icodici.universa.utnp.ethereum.EthereumUtils;
import com.icodici.universa.utnp.ethereum.contracts.generated.UTNP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * The class acting as a remote control to UTNP Ethereum smart contract.
 * <p>
 * Alpha-version – use at your own risk!
 * <p>
 * The web3j backend, in its current state, doesn’t provide stable non-problematic overview
 * of whether the smart contract method call has been completed succcessfully!
 */
public class UTNPConnector extends AbstractConnector {

    private static final Logger logger = LogManager.getLogger(UTNPConnector.class);

    private final UTNP contract;


    /**
     * Constructor.
     *
     * @param utnpContractAddress the Ethereum address of the UTN-P ERC20 contract. Used for burn operations.
     * @param burnerPrivateKey    the private key that has the permission to burn UTN-P tokens.
     */
    public UTNPConnector(AbstractConnector.@NonNull EthereumConnection connection,
                         @NonNull String utnpContractAddress,
                         @NonNull byte[] burnerPrivateKey,
                         @NonNull BigInteger gasPrice,
                         @NonNull BigInteger gasLimit) {
        super(connection);

        assert connection != null;
        assert utnpContractAddress != null;
        assert burnerPrivateKey != null;
        assert gasPrice != null;
        assert gasLimit != null;

        assert utnpContractAddress.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : utnpContractAddress;
        assert burnerPrivateKey.length == EthereumUtils.PRIVATE_KEY_SIZE : burnerPrivateKey.length;
        assert gasPrice.compareTo(BigInteger.ZERO) > 0 : gasPrice;
        assert gasLimit.compareTo(BigInteger.ZERO) > 0 : gasLimit;

        final Credentials burnerCredentials = Credentials.create(ECKeyPair.create(burnerPrivateKey));

        final String operatorAddress = burnerCredentials.getAddress();
        logger.debug("Operating from {}", operatorAddress);

        contract = UTNP.load(utnpContractAddress, web3j, burnerCredentials, gasPrice, gasLimit);
        assert contract != null;
    }

    /**
     * Call the `burn` method of UTN-P ERC20 contract.
     *
     * @param amountToBurn how many (in UTN-P contracts; a number with decimal point maybe) to burn.
     * @return <code>null</code> if failed, the transaction txid, if successfully executed.
     */
    @SuppressWarnings("unused")
    @Nullable
    public String burn(@NonNull BigDecimal amountToBurn) {
        if ((amountToBurn == null) || (amountToBurn.compareTo(BigDecimal.ZERO) < 0)) {
            return null;
        } else {
            final TransactionReceipt transactionReceipt;
            try {
                transactionReceipt = contract.burn(EthereumUtils.intFromDecimal(amountToBurn, UTNP_DECIMALS)).sendAsync().get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Problem on burn", e);
                return null;
            }
            if (transactionReceipt == null) {
                return null;
            } else {
                logger.error("Problem on burn/getTransactionHash");
                return transactionReceipt.getTransactionHash();
            }
        }
    }
}
