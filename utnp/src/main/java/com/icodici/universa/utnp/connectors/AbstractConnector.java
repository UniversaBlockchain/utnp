package com.icodici.universa.utnp.connectors;

import com.icodici.universa.utnp.ethereum.EthereumUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Any remote-control-class to some Ethereum smart contract.
 */
public abstract class AbstractConnector {
    public static final String UTNP_MAINNET_CONTRACT_ADDRESS = "0x9e3319636e2126e3c0bc9e3134AEC5e1508A46c7";
    public static final BigInteger UTNP_DECIMALS = BigInteger.valueOf(18);

    protected final Web3j web3j;

    /**
     * Accessory class used to describe the connection to the Ethereum node (geth).
     */
    public static class EthereumConnection {
        @NonNull
        public final Type type;
        @NonNull
        public final String address;

        /**
         * @param type    HTTP or IPC that is used to connect to geth node
         * @param address URL of geth node
         */
        public EthereumConnection(@NonNull Type type, @NonNull String address) {
            assert type != null;
            assert address != null;

            this.type = type;
            this.address = address;
        }

        public String toString() {
            return String.format("EthereumConnection(%s, %s)", type, address);
        }

        public enum Type {HTTP, IPC}
    }

    /**
     * A class containing the information about a single ERC20 transfer.
     */
    public static class SingleTransfer {

        @NonNull
        public final String toAddress;

        @NonNull
        public final BigDecimal amount;


        @SuppressWarnings("unused")
        public SingleTransfer(@NonNull String toAddress, @NonNull BigDecimal amount) {
            assert toAddress != null;
            assert toAddress.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : toAddress;
            assert amount != null;
            assert amount.compareTo(BigDecimal.ZERO) > 0 : amount;

            this.toAddress = toAddress;
            this.amount = amount;
        }

        public String toString() {
            return String.format("SingleTransfer(toAddress=%s, amount=%s)", toAddress, amount);
        }
    }

    /**
     * Constructor.
     */
    protected AbstractConnector(@NonNull EthereumConnection connection) {
        web3j = createWeb3j(connection);
    }


    private static Web3j createWeb3j(@NonNull EthereumConnection connection) {
        assert connection != null;

        switch (connection.type) {
            case HTTP:
                return Web3j.build(new HttpService(connection.address));
            case IPC:
                return Web3j.build(new UnixIpcService(connection.address));
            default:
                throw new RuntimeException("Cannot initialize Web3j with " + connection.toString());
        }
    }
}
