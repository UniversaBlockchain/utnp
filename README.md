# UTN-P (“Universa TokeN, Placeholder”)

Based on [OpenZeppelin](https://github.com/OpenZeppelin/zeppelin-solidity) framework.

## Build

    solidity_flattener utn-p.sol --output build/utn-p-combined.sol 
    solidity_flattener BulkSender.sol --output build/BulkSender-combined.sol
    solc --optimize --output-dir build --overwrite --gas --bin --abi build/utn-p-combined.sol
    solc --optimize --output-dir build --overwrite --gas --bin --abi build/BulkSender-combined.sol
    solc --optimize --output-dir build --overwrite --gas --bin --abi zeppelin-solidity/contracts/token/TokenTimelock.sol

## Timelock preparations

For Token Timelock, find date:

    python3 -c "from datetime import datetime; print(int(datetime(2018, 4, 1).timestamp()))"

Result: `1522530000`

This will be used for smart contract deployment.