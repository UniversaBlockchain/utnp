#!/bin/bash

solidity_flattener utn-p.sol --output build/utn-p-combined.sol
solidity_flattener BulkSender.sol --output build/BulkSender-combined.sol
solidity_flattener zeppelin-solidity/contracts/token/TokenTimelock.sol --output build/TokenTimelock-combined.sol
solc --optimize --output-dir build --overwrite --gas --bin --abi build/utn-p-combined.sol
solc --optimize --output-dir build --overwrite --gas --bin --abi build/BulkSender-combined.sol
solc --optimize --output-dir build --overwrite --gas --bin --abi zeppelin-solidity/contracts/token/TokenTimelock.sol
