package it.unipv.ingsw.d20.persistence;

import it.unipv.ingsw.d20.persistence.vending.IVendingDao;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PersistenceFacade pf = PersistenceFacade.getInstance();
		
		IVendingDao a = pf.getVendingDao();
		a.addVending("id4", "Roma");
		System.out.println(a.getAddressById("id3"));


	}

}