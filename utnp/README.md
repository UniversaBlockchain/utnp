# UTN-P: Java accessors

Contains the Java backend to access the UTN-P contract and all its Ethereum infrastructure.

WARNING: this is alpha code, use at your own risk!

The web3j backend, in its current state, doesn’t provide stable non-problematic overview of whether the smart contract method call has been completed succcessfully.

## Dependencies

* JDK 8 (not tested with JDK 9).
* Gradle (tested with Gradle 4.4).
* The dependencies detailed in `/README.md` of the project (such as, `solidity`/`web3j`).

Some of the code is auto-generated from the source code of smart contracts. Please resolve the dependencies from `/README.md` of the project and build the tokens, so the `abi`/`bin`-files are available. After this, run the command `./generate_contract_wrappers.sh` in `/src/main/java/com/icodici/universa/utnp/ethereum/contracts` directory, to generate them once.

## Build

In the root directory of the project, run:

     gradle build -x compileScala
