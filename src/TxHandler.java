import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.lang.NullPointerException;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	
	private UTXOPool pool;
	
    public TxHandler(UTXOPool utxoPool) {
        pool=utxoPool;
       
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
      
    	ArrayList<Transaction.Output> outputs1 = tx.getOutputs();
    	ArrayList<Transaction.Input> inputs1 = tx.getInputs();
    	double sumOfOutputs = 0;
    	double sumOfInputs = 0;
    	ArrayList<UTXO> utxos1 = new ArrayList();
    	//(2)
    	for (int i=0; i < inputs1.size(); i++) {
    		Transaction.Input x = inputs1.get(i);
    		UTXO utxo = new UTXO (x.prevTxHash,x.outputIndex);
    		if (utxos1.contains(utxo)) {
    			return false;
    		}
    		utxos1.add(utxo);
    		Transaction.Output output;
    		if (!pool.contains(utxo)) {
    			return false;
    		}
    		try {
    			output = pool.getTxOutput(utxo);
    		} catch (NullPointerException e) {
    			return false;
    		}
    		if (output != null) {
    			sumOfInputs += output.value;
    			if (Crypto.verifySignature(output.address, tx.getRawDataToSign(i), x.signature) == false) {
    				return false;
    			}
    		}
    	}
    	
    	//(4)
    	for (int i=0; i < outputs1.size(); i++) {
    		Transaction.Output x = outputs1.get(i);
    		sumOfOutputs += x.value;
    		if (x.value < 0) {
    			return false;
    		}
    	}
    	
    	if (sumOfOutputs > sumOfInputs) {
    		return false;
    	}
    	
    	return true;
    	
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	
    	ArrayList<Transaction> confirmedTransactions = new ArrayList();
    	for(int i = 0; i < possibleTxs.length; i++)
    	{
    	    if (isValidTx(possibleTxs[i])) {
    	    	confirmedTransactions.add(possibleTxs[i]);
    	    }
    	}
    	Transaction[] arrayToReturn = new Transaction[confirmedTransactions.size()];
    	return confirmedTransactions.toArray(arrayToReturn);
    }

}
