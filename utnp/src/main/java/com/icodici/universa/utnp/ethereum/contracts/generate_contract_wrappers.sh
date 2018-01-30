#!/bin/sh

# To execute, you must link a build directory with *.bin and *abi files to ./build

# Turn *.bin and *.abi into *.java.

for f in UTNP TokenTimelock BulkSender
do
    echo "Generating wrappers for $f.sol"
    web3j solidity generate \
        ./build/$f.bin ./build/$f.abi \
        --package com.icodici.universa.utnp.ethereum.contracts.generated \
        -o ../../../../../../
done
