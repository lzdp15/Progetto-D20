package it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.payment.strategies;

import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.payment.exceptions.*;

public class KeyPaymentStrategy extends AbstractPaymentStrategy {
	
	public double elaboratePayment(double price, Object creditInfo) throws InsufficientCreditException, InvalidPaymentException {

		String serial=serialize(creditInfo);
		
		double amount=0;
		if (checkValidity(serial)) {
			amount=getAmount(serial);
		}
				
		double change=checkCredit(amount, price);
		
		setAmount(change);
		return change;
	}
	
	public String serialize(Object creditInfo) throws InvalidPaymentException {
		String id;
		try {
			id=(String)creditInfo;
		} catch (ClassCastException e) {
			throw new InvalidPaymentException();
		}
		return id;
	}
	
	public boolean checkValidity(String serial) {
		
		//va a controllare che il seriale della chiavetta sia presente sul DB, se tutto ok ritorna true
		
		return true;
	}
	
	public double getAmount(String serial) {
		
		//va a prendere l'ammontare dal DB
		double amount=0;
		
		return amount;
	}
	
	public boolean setAmount(double change) {
		
		//va a scrivere l'ammontare rimasto sulla chiavetta sul DB
		
		return true;
	}
	
	@Override
	public String toString() {
		return ("Key");
	}

}
