# UTN-P (“Universa TokeN, Placeholder”)

Based on [OpenZeppelin](https://github.com/OpenZeppelin/zeppelin-solidity) framework.

## Build

    build.sh

## Timelock preparations

For Token Timelock, find date:

    python3 -c "from datetime import datetime; print(int(datetime(2018, 4, 28).timestamp()))"
    python3 -c "from datetime import datetime; print(datetime.fromtimestamp(1524862800))"

Result: `1524862800`

This will be used for smart contract deployment.
