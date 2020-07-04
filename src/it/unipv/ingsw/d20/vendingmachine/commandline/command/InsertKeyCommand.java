package it.unipv.ingsw.d20.vendingmachine.commandline.command;

import it.unipv.ingsw.d20.vendingmachine.commandline.exception.CommandFormatException;
import it.unipv.ingsw.d20.vendingmachine.model.VendingMachine;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.exceptions.UnrecognisedKeyException;

/**
 * Comando che permette di inserire una chiavetta, recuperandola
 * dal database.
 *
 */
public class InsertKeyCommand implements ICommand {

	@Override
	public String execute(VendingMachine vm, String args) {
		try {
			if (args != null)
				throw new CommandFormatException("Argomento non valido per il comando 'insertkey'");
			
			vm.insertKey();
		} catch (CommandFormatException | UnrecognisedKeyException e) {
			return e.getMessage();
		}
		
		return "Chiavetta inserita correttamente. Credito attuale: €" + String.format("%.2f", vm.getCredit());
	}

}
