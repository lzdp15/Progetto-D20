package it.unipv.ingsw.d20.util.persistence.sale;

import java.sql.SQLException;
import java.util.ArrayList;
import it.unipv.ingsw.d20.util.persistence.RdbOperations;

public class SaleRdbDao implements ISaleDao{
	
	private RdbOperations op;
	
	public SaleRdbDao() {
		op = new RdbOperations();
	}

	@Override
	public void addSale(SalePOJO sale) throws SQLException {
		op.addSale(sale);
	}

	@Override
	public SalePOJO getSaleByKey(String id,String date) {
		return op.getSaleByKey(id, date);
	}

	@Override
	public ArrayList<SalePOJO> getAllSalesByIdVending(String idVending) {
		return op.getAllSalesByIdVending(idVending);
	}

}
