pragma solidity ^0.4.18;

import './zeppelin-solidity/contracts/ownership/Ownable.sol';
import './zeppelin-solidity/contracts/token/BasicToken.sol';
import './zeppelin-solidity/contracts/token/BurnableToken.sol';
import './zeppelin-solidity/contracts/token/ERC20.sol';

/**
 * @title UTN-P ERC20 token by Universa Blockchain.
 *
 * @dev Based on OpenZeppelin framework.
 *
 * Features:
 *
 * * ERC20 compatibility, with token details as properties.
 * * total supply: 4997891952 (initially given to the contract author).
 * * decimals: 18
 * * BurnableToken: some addresses are allowed to burn tokens.
 * * “third-party smart contract trading protection”: transferFrom/approve/allowance methods are present but do nothing.
 * * TimeLock: implemented externally (in TokenTimelock contract), some tokens are time-locked for 3 months.
 * * Bulk send: implemented externally (in BulkSender contract), some tokens are time-locked for 3 months.
 */
contract UTNP is BasicToken, BurnableToken, ERC20, Ownable {

    string public constant name = "UTN-P: Universa Token";
    string public constant symbol = "UTNP";
    uint8 public constant decimals = 18;
    string public constant version = "1.0";

    uint256 constant INITIAL_SUPPLY_UTN = 4997891952;

    /// @dev whether an address is permitted to perform burn operations.
    mapping(address => bool) public isBurner;

    /**
     * @dev Constructor that:
     * * gives all of existing tokens to the message sender;
     * * initializes the burners (also adding the message sender);
     */
    function UTNP() public {
        totalSupply = INITIAL_SUPPLY_UTN * (10 ** uint256(decimals));
        balances[msg.sender] = totalSupply;

        isBurner[msg.sender] = true;
    }

    /**
     * @dev Standard method to comply with ERC20 interface;
     * prevents some Ethereum-contract-initiated operations.
     */
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool) {
        return false;
    }

    /**
     * @dev Standard method to comply with ERC20 interface;
     * prevents some Ethereum-contract-initiated operations.
     */
    function approve(address _spender, uint256 _value) public returns (bool) {
        return false;
    }

    /**
     * @dev Standard method to comply with ERC20 interface;
     * prevents some Ethereum-contract-initiated operations.
     */
    function allowance(address _owner, address _spender) public view returns (uint256) {
        return 0;
    }

    /**
     * @dev Grant or remove burn permissions. Only owner can do that!
     */
    function grantBurner(address _burner, bool _value) public onlyOwner {
        isBurner[_burner] = _value;
    }

    /**
     * @dev Throws if called by any account other than the burner.
     */
    modifier onlyBurner() {
        require(isBurner[msg.sender]);
        _;
    }

    /**
     * @dev Burns a specific amount of tokens.
     * Only an address listed in `isBurner` can do this.
     * @param _value The amount of token to be burned.
     */
    function burn(uint256 _value) public onlyBurner {
        super.burn(_value);
    }
}
