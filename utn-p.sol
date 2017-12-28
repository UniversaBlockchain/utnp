pragma solidity ^0.4.18;

import './zeppelin-solidity/contracts/token/BasicToken.sol';
import './zeppelin-solidity/contracts/token/BurnableToken.sol';
import './zeppelin-solidity/contracts/token/ERC20.sol';

/**
 * @dev UTN-P ERC20 token.
 *
 * Based on OpenZeppelin framework.
 *
 * Features:
 *
 * * ERC20 compatibility, with token details as properties.
 * * total supply: 4997891952 (initially given to the contract author).
 * * decimals: 18
 * * BurnableToken: some addresses are allowed to burn tokens.
 * * transferFrom/approve/allowance methods are present but do nothing.
 * * TimeLock: implemented externally (in TokenTimelock contract), some tokens are time-locked for 3 months.
 */
//TokenVesting
contract UTNP is BasicToken, BurnableToken, ERC20 {

    string public constant name = "UTN-P: Universa Token";
    string public constant symbol = "UTNP";
    uint8 public constant decimals = 18;
    string public constant version = "1.0";

    uint256 constant INITIAL_SUPPLY_UTN = 4997891952;

    mapping(address => bool) public isBurner;
    address public constant timeLockedAddress = 0xDf5963B72B2478E828Bc69693f1f47C2b2BB7948;

    /**
     * @dev Constructor that:
     * * gives all of existing tokens to the message sender;
     * * initializes the burners (also adding the message sender);
     */
    function UTNP() public {
        totalSupply = INITIAL_SUPPLY_UTN * (10 ** uint256(decimals));
        balances[msg.sender] = totalSupply;

        // Initialize the burners.
        isBurner[msg.sender] = true;
        isBurner[0xB5a7235d2D53aaDaEfe68c4477BdAF1A8046DEFC] = true;
        isBurner[0x5335266ef86655ac6764b98cD9FF1166EF542342] = true;
        isBurner[0xE1D2200354677D549782bA3eF0b782b203C1057a] = true;
    }

    /**
     * @dev Standard method to comply with ERC20 interface;
     * prevents any Ethereum-contract-initiated operations.
     */
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool) {
        return false;
    }

    /**
     * @dev Standard method to comply with ERC20 interface;
     * prevents any Ethereum-contract-initiated operations.
     */
    function approve(address _spender, uint256 _value) public returns (bool) {
        return false;
    }

    /**
     * @dev Standard method to comply with ERC20 interface;
     * prevents any Ethereum-contract-initiated operations.
     */
    function allowance(address _owner, address _spender) public view returns (uint256) {
        return 0;
    }

    /**
     * @dev Burns a specific amount of tokens.
     * Only an address listed in `isBurner` can do this.
     * @param _value The amount of token to be burned.
     */
    function burn(uint256 _value) public {
        require(isBurner[msg.sender]);
        super.burn(_value);
    }
}
