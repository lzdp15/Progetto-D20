package it.unipv.ingsw.d20.vendingmachine.model.beverage;

import it.unipv.ingsw.d20.vendingmachine.model.beverage.exceptions.DeliveryFailedException;

public class Beverage implements Runnable{
	
	@SuppressWarnings("unused")
	private BeverageDescription beverageDescription;
	
	public Beverage(BeverageDescription b) throws DeliveryFailedException {
		this.beverageDescription=b;
		//INVECE DEL METODO hasBeenDelivered ABBIAMO PREFERITO USARE UN'ECCEZIONE CHE VIENE GESTITA NELLA SALE (IN CASO NON FOSSE POSSIBILE EROGARE LA BEVANDA. 
		//L'ECCEZIONE DEVE ESSERE LANCIATA DAL COSTRUTTORE
	}

	@Override
	public void run() {
		//a questo punto come si modifica lo status della macchina? non abbiamo accesso ai dati
		
	}
	
}
