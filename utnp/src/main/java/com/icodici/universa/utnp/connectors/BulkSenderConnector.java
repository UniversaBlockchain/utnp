package com.icodici.universa.utnp.connectors;

import com.icodici.universa.utnp.ethereum.EthereumUtils;
import com.icodici.universa.utnp.ethereum.contracts.generated.BulkSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.NoOpProcessor;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class acting as a remote control to BulkSender Ethereum smart contract.
 * <p>
 * Alpha-version – use at your own risk!
 * <p>
 * The web3j backend, in its current state, doesn’t provide stable non-problematic overview
 * of whether the smart contract method call has been completed succcessfully!
 */
public class BulkSenderConnector extends AbstractConnector {

    private static final Logger logger = LogManager.getLogger(BulkSenderConnector.class);

    private final RawTransactionManager txManager;
    private final BulkSender contract;


    /**
     * Constructor.
     *
     * @param bulkSenderContractAddress the Ethereum address of the UTN-P BulkSender contract.
     * @param bulkSenderPrivateKey      the private key that has the permission to use BulkSender contract.
     */
    public BulkSenderConnector(@NonNull EthereumConnection connection,
                               @NonNull String bulkSenderContractAddress,
                               @NonNull byte[] bulkSenderPrivateKey,
                               @NonNull BigInteger gasPrice,
                               @NonNull BigInteger gasLimit) {
        super(connection);

        assert connection != null;
        assert bulkSenderContractAddress != null;
        assert bulkSenderPrivateKey != null;
        assert gasPrice != null;
        assert gasLimit != null;

        assert bulkSenderContractAddress.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : bulkSenderContractAddress;
        assert bulkSenderPrivateKey.length == EthereumUtils.PRIVATE_KEY_SIZE : bulkSenderPrivateKey.length;
        assert gasPrice.compareTo(BigInteger.ZERO) > 0 : gasPrice;
        assert gasLimit.compareTo(BigInteger.ZERO) > 0 : gasLimit;

        final Credentials bulkSenderCredentials = Credentials.create(ECKeyPair.create(bulkSenderPrivateKey));

        final String operatorAddress = bulkSenderCredentials.getAddress();
        logger.debug("Operating from {}", operatorAddress);
        logger.debug("Bulk sender: {}", bulkSenderContractAddress);

        txManager = new RawTransactionManager(web3j, bulkSenderCredentials, ChainId.MAINNET, new NoOpProcessor(web3j));
        contract = BulkSender.load(bulkSenderContractAddress, web3j, txManager, gasPrice, gasLimit);
        assert contract != null;
    }

    /**
     * Send multiple transfers at once, using the BulkTransfer contract.
     *
     * @return <code>null</code> if failed, the transaction txid, if successfully executed.
     */
    @SuppressWarnings("unused")
    @Nullable
    public String bulkTransfer(
            @NonNull String contractAddressToTransfer,
            @NonNull BigInteger decimals,
            @NonNull Collection<SingleTransfer> transfers
    ) {
        assert contractAddressToTransfer != null;
        assert contractAddressToTransfer.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : contractAddressToTransfer;
        assert decimals != null;
        assert decimals.compareTo(BigInteger.ZERO) > 0 : decimals;
        assert !transfers.isEmpty();

        final List<String> addresses = transfers.stream()
                .map(singleTransfer -> singleTransfer.toAddress)
                .collect(Collectors.toList());

        final List<BigInteger> amounts = transfers.stream()
                .map(singleTransfer -> EthereumUtils.intFromDecimal(singleTransfer.amount, UTNP_DECIMALS))
                .collect(Collectors.toList());

        assert addresses.size() == amounts.size() : String.format("%s vs %s", addresses.size(), amounts.size());

        try {
            return contract.bulkTransfer(contractAddressToTransfer, addresses, amounts).send().getTransactionHash();
        } catch (Exception e) {
            logger.error("Problem on bulkTransfer in noop send", e);
            logger.error("Bulk transfer cannot get the receipt yet");
            return null;
        }
    }

    @SuppressWarnings("unused")
    @Nullable
    public String bulkTransferMainnetUTNP(@NonNull Collection<SingleTransfer> transfers) {
        return bulkTransfer(UTNP_MAINNET_CONTRACT_ADDRESS, UTNP_DECIMALS, transfers);
    }
}
