package com.icodici.universa.utnp;

import com.icodici.universa.utnp.connectors.BulkSenderConnector;
import com.icodici.universa.utnp.ethereum.EthereumUtils;
import org.apache.commons.cli.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BulkSendCLI {

    public static BigDecimal DEFAULT_GAS_PRICE_GWEI = new BigDecimal("21");

    public static long BULK_SIZE = 100;

    private static final Options options = new Options();

    static {
        options.addOption("h", "help", false, "display help");
        options.addOption("i", "input", true, "JSON input file");
        options.addOption("s", "skip", true, "how many records to skip from the JSON file (default: 0)");
        options.addOption("n", "number", true, "how many records to take from the JSON file (default: all records)");
        options.addOption("r", "rpc", true, "geth RPC URL (e.g. “http://localhost:8548”)");
        options.addOption("pk", "privatekey", true, "the file with private key");
        options.addOption("bs", "bulksender", true, "BulkSender contract address");
        options.addOption("e", "erc20", true, "ERC20 contract address");
        options.addOption("gp", "gasprice", true, "gas price (in Gwei; default: 1.1)");
    }

    /**
     * Display automatically generated help.
     */
    private void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar BulkSendCLI", BulkSendCLI.options);
    }

    private void executeBulkSend(@NonNull BulkSenderConnector utnpConnector,
                                 @NonNull final String erc20Address,
                                 @NonNull List<BulkSenderConnector.SingleTransfer> bulkRequests) {
        System.out.printf(" >>> Sending bulk %s\n", bulkRequests.size());
        final String txid = utnpConnector.bulkTransfer(
                erc20Address, BulkSenderConnector.UTNP_DECIMALS, bulkRequests);
        System.out.printf(" <<< Done\n");
//        System.out.printf(" <<< Result transaction: %s\n", txid);
    }

    private void executeTotalSend(@NonNull final String rpcUrl,
                                  @NonNull final String bulkSenderAddress,
                                  @NonNull final String erc20Address,
                                  @NonNull final byte[] privateKey,
                                  @NonNull final JSONArray ordersPayload,
                                  long skipOrders,
                                  long numberOrders,
                                  @NonNull final BigDecimal gasPriceGwei) {
        assert rpcUrl != null;
        assert bulkSenderAddress != null;
        assert bulkSenderAddress.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : bulkSenderAddress;
        assert erc20Address != null;
        assert erc20Address.length() == EthereumUtils.ETHEREUM_ADDRESS_LENGTH : erc20Address;
        assert privateKey != null;
        assert privateKey.length == EthereumUtils.PRIVATE_KEY_SIZE : privateKey.length;
        assert ordersPayload != null;
        assert ordersPayload.length() > 0 : ordersPayload;
        assert gasPriceGwei != null;
        assert gasPriceGwei.compareTo(BigDecimal.ZERO) > 0 : gasPriceGwei;

        System.out.printf("Started with:\n    --skip %s --number %s\n", skipOrders, numberOrders);
        System.out.printf("Next launch should use:\n    --skip %s \n", skipOrders + numberOrders);

        final List<BulkSenderConnector.SingleTransfer> requests = new LinkedList<>();

        final List<Map> objectToDoubleCheck = new LinkedList<>();

        long currentSkip = skipOrders;

        BigDecimal total = BigDecimal.ZERO;
        for (final Object o : ordersPayload) {
            if (skipOrders > 0) {
                skipOrders--;
                continue;
            }
            if (numberOrders == 0) {
                continue;
            }
            numberOrders--;

            final JSONObject jo = (JSONObject) o;
            final String uuid = jo.getString("uuid");
            final BigDecimal utnp_amount = jo.getBigDecimal("utnp_amount");
            final String address = jo.getString("utnp_address");

            total = total.add(utnp_amount);

            requests.add(new BulkSenderConnector.SingleTransfer(address, utnp_amount));
            final long finalCurrentSkip = currentSkip;
            objectToDoubleCheck.add(
                    new HashMap() {{
                        put("skip", finalCurrentSkip);
                        put("uuid", uuid);
                        put("utnp_amount", utnp_amount.toPlainString());
                        put("utnp_address", address);
                    }}
            );

            currentSkip++;
        }
        System.out.printf("Total amount: %s\n", total);

        final Map overallStructureToDoubleCheck = new HashMap() {{
            put("orders", objectToDoubleCheck);
        }};
        final JSONObject overallJSONToDoubleCheck = (JSONObject) JSONObject.wrap(overallStructureToDoubleCheck);

//        try {
//            Files.write(
//                    Paths.get("./doublecheck-production.json"),
//                    Collections.singleton(overallJSONToDoubleCheck.toString(2)),
//                    StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Now we have `requests` calculated

        final BigDecimal
                gwei = new BigDecimal("0.000000001"),
                gasPrice = gasPriceGwei.multiply(gwei);

        final BulkSenderConnector.EthereumConnection ethereumConnection = new BulkSenderConnector.EthereumConnection(
                BulkSenderConnector.EthereumConnection.Type.HTTP, rpcUrl);
        final BulkSenderConnector utnpConnector = new BulkSenderConnector(
                ethereumConnection,
                bulkSenderAddress,
                privateKey,
                EthereumUtils.valueInWei(gasPrice),
                BigInteger.valueOf(5000000));

        long left_in_bulk = BULK_SIZE;
        final List<BulkSenderConnector.SingleTransfer> bulk = new LinkedList<>();
        while (!requests.isEmpty()) {
            final BulkSenderConnector.SingleTransfer request = requests.remove(0);
            bulk.add(request);

            left_in_bulk--;

            if (left_in_bulk == 0) {
                left_in_bulk = BULK_SIZE;
                System.out.printf("Remaining: %s\n", requests.size());
                executeBulkSend(utnpConnector, erc20Address, bulk);
                bulk.clear();
            }
        }
        if (!bulk.isEmpty()) {
//            System.out.printf("Sending leftovers\n");
            executeBulkSend(utnpConnector, erc20Address, bulk);
        }
        System.out.println("Done!");
    }

    /**
     * Process the CLI flags and options.
     */
    private void processCLIArguments(@NonNull String[] args) {
        assert args != null;
        System.setProperty("log4j2.debug", "1");

        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                printHelp();
            } else {

                final String
                        argInputStr = line.getOptionValue("input"),
                        argSkipStr = line.getOptionValue("skip"),
                        argNumberStr = line.getOptionValue("number"),
                        argRPCStr = line.getOptionValue("rpc"),
                        argPrivateKeyStr = line.getOptionValue("privatekey"),
                        argBulkSenderStr = line.getOptionValue("bulksender"),
                        argErc20Str = line.getOptionValue("erc20"),
                        argGasPriceStr = line.getOptionValue("gasprice");

                // Validate for errors

                if (argInputStr == null || argRPCStr == null || argPrivateKeyStr == null || argBulkSenderStr == null || argErc20Str == null) {
                    System.err.printf("\"input\", \"rpc\", \"privatekey\", \"bulksender\" and \"erc20\" are the mandatory options!\n");
                    return;
                }

                final JSONArray ordersPayload;
                try {
                    final Path inputFilePath = Paths.get(argInputStr.replace("~", System.getProperty("user.home")));
                    final String inputFileContents = Files.readAllLines(inputFilePath)
                            .stream()
                            .collect(Collectors.joining());
                    final JSONObject jsonPayload = new JSONObject(inputFileContents);

                    ordersPayload = jsonPayload.getJSONArray("orders");
                } catch (Exception e) {
                    System.err.printf("Cannot read JSON file %s!\n", argInputStr);
                    return;
                }
                final int howManyOrders = ordersPayload.length();

                final byte[] privateKey;
                try {
                    final Path privateKeyPath = Paths.get(argPrivateKeyStr.replace("~", System.getProperty("user.home")));
                    final String privateKeyFileContents = Files.readAllLines(privateKeyPath)
                            .stream()
                            .collect(Collectors.joining()).trim();
                    privateKey = Hex.decode(privateKeyFileContents);
                } catch (Exception e) {
                    System.err.printf("Failed to read the private key file at %s!\n", argPrivateKeyStr);
                    return;
                }

                final long argSkip;
                if (argSkipStr == null) {
                    argSkip = 0;
                } else {
                    try {
                        argSkip = Long.parseUnsignedLong(argSkipStr);
                    } catch (NumberFormatException ex) {
                        System.err.printf("\"skip\" argument must contain a valid integer number!\n");
                        return;
                    }
                }

                final long argNumber;
                if (argNumberStr == null) {
                    argNumber = howManyOrders - argSkip;
                } else {
                    try {
                        argNumber = Long.parseUnsignedLong(argNumberStr);
                    } catch (NumberFormatException ex) {
                        System.err.printf("\"number\" argument must contain a valid integer number!\n");
                        return;
                    }
                }
                if (argSkip + argNumber > howManyOrders) {
                    System.err.printf("\"skip\"+\"number\" arguments must be not more than %s!\n", howManyOrders);
                    return;
                }

                final BigDecimal gasPrice;
                if (argGasPriceStr == null) {
                    gasPrice = DEFAULT_GAS_PRICE_GWEI;
                } else {
                    try {
                        gasPrice = new BigDecimal(argGasPriceStr);
                    } catch (Exception ex) {
                        System.err.printf("\"gasprice\" argument must contain a valid gas price in Gwei!\n");
                        return;
                    }
                }

                // Ready

                executeTotalSend(
                        argRPCStr, argBulkSenderStr,
                        argErc20Str, privateKey, ordersPayload,
                        argSkip, argNumber, gasPrice);
            }
        } catch (ParseException exp) {
            System.err.printf("Parsing failed. Reason: %s\n", exp.getMessage());
            printHelp();
        }
    }

    public static void main(String[] args) {
        new BulkSendCLI().processCLIArguments(args);
    }
}
