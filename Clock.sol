pragma solidity ^0.4.18;

/**
 * @title Clock: get the current Ethereum time.
 *
 * @dev Minor utilitary smart contract to get current Ethereum timestamp.
 */
contract Clock {
    function time() public view returns (uint256) {
        return now;
    }
}
