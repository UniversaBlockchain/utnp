package com.icodici.universa.utnp.ethereum;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Transaction;
import rx.Subscription;
import rx.internal.schedulers.ExecutorScheduler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Misc tools, utils and constants related to Ethereum.
 */
public class EthereumUtils {

    static final int WEI_IN_ETHER_SCALE = 18;
    public static final BigDecimal WEI_IN_ETHER = BigDecimal.TEN.pow(WEI_IN_ETHER_SCALE);

    public static final int ETHEREUM_ADDRESS_LENGTH = 42;
    public static final int ADDRESS_ENGINE_LENGTH = ETHEREUM_ADDRESS_LENGTH;
    public static final int ADDRESS_DB_LENGTH = ETHEREUM_ADDRESS_LENGTH; // let's store it with 0x prefix, for simplicity
    public static final int ETHEREUM_TR_TXHASH_LENGTH = 66;
    public static final int TR_TXHASH_ENGINE_LENGTH = ETHEREUM_TR_TXHASH_LENGTH;
    public static final int TR_TXHASH_DB_LENGTH = ETHEREUM_TR_TXHASH_LENGTH; // let's store it with 0x prefix, for simplicity

    /**
     * Size of Ethereum private key, in bytes.
     */
    public static final int PRIVATE_KEY_SIZE = 32;
    /**
     * Size of Ethereum public key, in bytes.
     */
    public static final int PUBLIC_KEY_SIZE = 64;

    @NonNull
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Convert the web3j-typical address
     * to the DB-compatible address.
     */
    public static final String addressToDBFormat(@NonNull String web3jAddress) {
        assert web3jAddress != null;
        assert web3jAddress.toLowerCase().equals(web3jAddress) : web3jAddress;
        assert web3jAddress.toLowerCase().startsWith("0x") : web3jAddress;
        assert web3jAddress.length() == ADDRESS_ENGINE_LENGTH;

        return web3jAddress.toLowerCase();
    }

    /**
     * Convert the DB-compatible address
     * to the web3j-typical address.
     */
    public static final String addressFromDBFormat(@NonNull String dbAddress) {
        assert dbAddress.toLowerCase().equals(dbAddress) : dbAddress;
        assert dbAddress.toLowerCase().startsWith("0x") : dbAddress;
        assert dbAddress.length() == ADDRESS_DB_LENGTH : dbAddress;

        return dbAddress.toLowerCase();
    }

    /**
     * Convert the web3j-typical transaction hash
     * to the DB-compatible transaction hash.
     */
    public static final String trTxhashToDBFormat(@NonNull String web3jTrHash) {
        assert web3jTrHash.toLowerCase().equals(web3jTrHash) : web3jTrHash;
        assert web3jTrHash.toLowerCase().startsWith("0x") : web3jTrHash;
        assert web3jTrHash.length() == TR_TXHASH_ENGINE_LENGTH : web3jTrHash;

        return web3jTrHash.toLowerCase();
    }

    /**
     * Convert the DB-compatible transaction hash
     * to the web3j-typical transaction hash.
     */
    public static final String trTxhashFromDBFormat(@NonNull String dbTrHash) {
        assert dbTrHash.toLowerCase().equals(dbTrHash) : dbTrHash;
        assert dbTrHash.toLowerCase().startsWith("0x") : dbTrHash;
        assert dbTrHash.length() == TR_TXHASH_DB_LENGTH : dbTrHash;

        return dbTrHash.toLowerCase();
    }

    /**
     * Convert the amount in WEI to regular {@link BigDecimal} value of Ethers.
     */
    @SuppressWarnings("unused")
    public static final BigDecimal valueOfWei(@NonNull BigInteger weis) {
        return new BigDecimal(weis).divide(WEI_IN_ETHER);
    }

    /**
     * Convert the amount of Ethers to WEI.
     */
    @SuppressWarnings("unused")
    public static final BigInteger valueInWei(@NonNull BigDecimal ethers) {
        return ethers.multiply(WEI_IN_ETHER).toBigInteger();
    }

    /**
     * Given some value with decimal point stored separately, get its {@link BigDecimal} representation.
     */
    @SuppressWarnings("unused")
    public static final BigDecimal valueWithDecimal(@NonNull BigInteger value, @NonNull BigInteger decimals) {
        return new BigDecimal(value).divide((BigDecimal.TEN.pow(decimals.intValue())));
    }

    /**
     * Given some value with decimal point stored separately, get its {@link BigInteger} representation.
     */
    @SuppressWarnings("unused")
    public static final BigInteger intFromDecimal(@NonNull BigDecimal bd, @NonNull BigInteger decimals) {
        return bd.multiply((BigDecimal.TEN.pow(decimals.intValue()))).toBigInteger();
    }

    /**
     * Wait until a transaction is fully mined.
     *
     * @return the {@link CompletableFuture} with the transaction txhash.
     */
    @SuppressWarnings("unused")
    @NonNull
    protected static CompletableFuture<String> waitForTransactionMined(
            @NonNull Web3j web3, @NonNull String txhash
    ) throws IOException, ExecutionException, InterruptedException {
        // TODO: the implementation can be more reactive
        // (though this is not very important, the first call should release the thread rather fast).

        final BigInteger latestBlockNumber = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                .sendAsync()
                .thenApply(latestBlock -> latestBlock.getBlock().getNumber())
                .get();
        final DefaultBlockParameter
                latestBlockParam = DefaultBlockParameter.valueOf(latestBlockNumber),
                penultimateBlockParam = DefaultBlockParameter.valueOf(latestBlockNumber.subtract(BigInteger.ONE));

        // Actually we could have found the transaction status at penultimateBlockParam;
        // but it is not that important for race conditions, cause we can complete the CompletableFuture as soon
        // as the transaction is found mined *at any moment*.

        final Optional<Transaction> optTransaction = web3.ethGetTransactionByHash(txhash).send().getTransaction();
        if (optTransaction.isPresent()) {
            final String blockNumberRaw = optTransaction.get().getBlockNumberRaw();
            if (blockNumberRaw != null) {
                // Bail out! we are done
                final CompletableFuture<String> result = new CompletableFuture<>();
                result.complete(txhash);
                return result;
            }
        }

        final CompletableFuture<String> result = new CompletableFuture<>();

        // Subscription[1] is a workaround to deal with "effectively final" requirement of lambda-functions.
        final Subscription[] subscriptionContainer = new Subscription[1];
        subscriptionContainer[0] = web3.catchUpToLatestAndSubscribeToNewTransactionsObservable(latestBlockParam)
                .observeOn(new ExecutorScheduler(executor))
                .subscribe(transaction -> {
                    if (transaction.getHash().equals(txhash)) {
                        subscriptionContainer[0].unsubscribe();
                        result.complete(txhash);
                    }
                });
        return result;
    }
}
