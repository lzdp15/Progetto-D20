package it.unipv.ingsw.d20.vendingmachine.model;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

import it.unipv.ingsw.d20.util.persistence.PersistenceFacade;
import it.unipv.ingsw.d20.util.persistence.beveragecatalog.IBvCatalogDao;
import it.unipv.ingsw.d20.util.persistence.local.VendingLocalIO;
import it.unipv.ingsw.d20.util.persistence.sale.ISaleDao;
import it.unipv.ingsw.d20.util.persistence.sale.SalePOJO;
import it.unipv.ingsw.d20.vendingmachine.model.net.VendingMachineClient;

/**
 * Periodicamente questa classe aggiorna la company con le informazioni attuali
 * della macchinetta, aggiorna il catalogo nel caso sia stato modificato e 
 * carica la lista delle vendite effettuate sul database.
 *
 */
public class UpdateInfoTimerTask extends TimerTask {

	@Override
	public synchronized void run() {
		PersistenceFacade pf = PersistenceFacade.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		
		try {
			VendingMachineClient vmc = new VendingMachineClient();
			vmc.connectToServer(VendingMachine.info); //invia le informazioni al server della company
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//renizializzazione del catalogo
		int vmType = Integer.parseInt(v.getVendingTypeFromLocal());
		IBvCatalogDao bv = pf.getBvCatalogDao(); 
		v.saveCatalogIntoLocal(bv.getBeverageCatalog(vmType));
		
		List<String> saleList = v.getSaleListFromLocal();
		ISaleDao saleDao = pf.getSaleDao();
		
		if (!saleList.isEmpty()) {
			try {
				for (String s : saleList) {
					String[] split = s.split("	");
					if (split.length == 3) {
						saleDao.addSale(new SalePOJO(split[0], split[1], split[2])); //carica la sale nel database
					}
				}
				
				v.emptyLocalSale(); //svuota il file locale con le sale, sono ormai nel database
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}