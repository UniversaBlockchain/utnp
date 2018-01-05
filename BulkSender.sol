pragma solidity ^0.4.18;

import './zeppelin-solidity/contracts/ownership/Ownable.sol';
import './zeppelin-solidity/contracts/token/SafeERC20.sol';

/**
 * @dev A contract that can send multiple contracts to multiple addresses, at once.
 *
 * Based on OpenZeppelin framework. Uses the code of TokenTimelock as an example.
 *
 */
contract BulkSender is Ownable {
    using SafeERC20 for ERC20Basic;

    /**
     * @dev Transfer multiple batches for the same token to multiple addresses accordingly,
     * from the ownership of the sender contract.
     * Note: only the owner (creator) of this contract may call this.
     */
    function bulkTransfer(ERC20Basic token, address[] toAddresses, uint256[] values) public onlyOwner returns (bool) {
        require((toAddresses.length > 0) && (toAddresses.length == values.length));
        for (uint i = 0; i < toAddresses.length; i++) {
            token.safeTransfer(toAddresses[i], values[i]);
        }
        return true;
    }
}
