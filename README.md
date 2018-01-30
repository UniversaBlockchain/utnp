# UTN-P (“Universa TokeN, Placeholder”)

This repository contains the UTN-P smart contract source code itself (`utn-p.sol`); the related smart contracts (such as `BulkSender.sol` or `TokenTimelock.sol`); and also the Java accessor module and CLI tool to control these smart contracts from Java code.

## Source code map

* `/utn-p.sol` – the primary source code of Universa UTN-P ERC20 token.
* `/BulkSender.sol` – the smart contract capable of owning and mass sending of the same ERC20-compatible token (no matter what one, it is not fixed in the code of BulkSender) to multiple addresses at once. It significantly saves the transaction fees.
* `/utnp` (directory) – the Java accessor to the smart contracts; also, the CLI tool(s) to simplify it. See `/utnp/README.md` for details. Use them at your own risk!

## Smart contract dependencies

To compile this, you need `solidity` and `web3j` command line tools installed.

### Installing `solidity`

The details are available at <https://solidity.readthedocs.io/en/develop/installing-solidity.html>.

For macOS, do the following:

    brew update
    brew upgrade
    brew tap ethereum/ethereum
    brew install solidity
    brew linkapps solidity

### Installing `web3j`

The details are available at <https://docs.web3j.io/command_line.html>.

For macOS, do the following:

    brew tap web3j/web3j
    brew install web3j

## Building

    build.sh

This compiles the smart contracts and puts the `abi`/`bin` files in the `/build` directory. Also it puts there the `*-combined.sol` version of the contracts, ready for publishing at [Etherscan.io](https://etherscan.io).

Building the smart contracts is also an essential prerequirement to use the Java source code. For details on building the Java source code, see `/utnp/README.md`.

## QA

### What else does the code base of Universa smart contracts uses?

The Universa Ethereum smart contracts are based on [OpenZeppelin](https://github.com/OpenZeppelin/zeppelin-solidity) framework. To simplify the deployment, the version used for the launch is copied to this repository.

### How to setup the TimeLock?

For Token Timelock, find date:

    python3 -c "from datetime import datetime; print(int(datetime(2018, 4, 28).timestamp()))"
    python3 -c "from datetime import datetime; print(datetime.fromtimestamp(1524862800))"

Result: `1524862800`

This will be used when deploying the TokenTimelock smart contract to the Ethereum blockchain.