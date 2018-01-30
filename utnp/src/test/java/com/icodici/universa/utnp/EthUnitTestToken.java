package com.icodici.universa.utnp;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * The class to store the information about a single ERC20-like Ethereum contract.
 */
class EthUnitTestToken {

    /**
     * Base token address.
     */
    @NonNull
    public final String address;
    /**
     * Number of decimals. Can be dynamically found for HST (HumanStandardToken) tokens; but stored here for tests.
     */
    public final int decimals;
    /**
     * Does this token support HumanStandardToken fields?
     */
    public final boolean isHST;

    EthUnitTestToken(@NonNull String address, int decimals, boolean isHST) {
        this.address = address;
        this.decimals = decimals;
        this.isHST = isHST;
    }
}
