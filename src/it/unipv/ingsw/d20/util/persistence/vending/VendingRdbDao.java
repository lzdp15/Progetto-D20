package it.unipv.ingsw.d20.util.persistence.vending;

import java.util.ArrayList;

import it.unipv.ingsw.d20.util.persistence.RdbOperations;

/**
 * Implementazione dell'interfaccia IVendingDao. Implementa il DAO relativo al database relazionale.
 *
 */
public class VendingRdbDao implements IVendingDao{

	private RdbOperations op;
	
	public VendingRdbDao(RdbOperations op) {
		this.op = op;
	}
	
	@Override
	public void addVending(VendingPOJO vending) {
		op.addVending(vending);
	}

	@Override
	public ArrayList<VendingPOJO> getAllVendings() {
		return op.getAllVendings();
	}

	@Override
	public VendingPOJO getVending(String idVending) {
		return op.getVending(idVending);
	}

}
