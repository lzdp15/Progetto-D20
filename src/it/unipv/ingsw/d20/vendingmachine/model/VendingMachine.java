package it.unipv.ingsw.d20.vendingmachine.model;


import it.unipv.ingsw.d20.util.persistence.PersistenceFactory;
import it.unipv.ingsw.d20.util.persistence.local.VendingLocalIO;
import it.unipv.ingsw.d20.vendingmachine.model.beverage.BeverageCatalog;
import it.unipv.ingsw.d20.vendingmachine.model.beverage.BeverageDescription;
import it.unipv.ingsw.d20.vendingmachine.model.beverage.Ingredients;
import it.unipv.ingsw.d20.vendingmachine.model.exceptions.InsufficientIngredientsException;
import it.unipv.ingsw.d20.vendingmachine.model.exceptions.InsufficientPermissionsException;
import it.unipv.ingsw.d20.vendingmachine.model.exceptions.KeyRestException;
import it.unipv.ingsw.d20.vendingmachine.model.exceptions.NonExistentCodeException;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.CashContainer;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.KeyHandler;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.Sale;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.exceptions.InsufficientCashForRestException;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.exceptions.InsufficientCreditException;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.exceptions.InvalidCoinException;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.exceptions.KeyNotInsertedException;
import it.unipv.ingsw.d20.vendingmachine.model.paymentsystem.exceptions.UnrecognisedKeyException;
import it.unipv.ingsw.d20.vendingmachine.model.tanks.Tank;
import it.unipv.ingsw.d20.vendingmachine.model.tanks.TankHandler;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Rappresenta il modello del distributore automatico di bevande.
 * 
 */
public class VendingMachine {

	private String id;
	private VendingMachineStatus status;
	private double credit; //soldi attualmente inseriti
	private TankHandler tankHandler;
	private BeverageCatalog bvCatalog;	//catalogo delle bevande
	private CashContainer cashContainer;
	private KeyHandler keyHandler;
	private String info = "";
	private boolean operatorMode;
	
	/**
	 * Costruttore della classe VendingMachine. Istanzia tutte le componenti che servono per far funzionare un distributore.
	 * @param id Stringa che rappresenta l'ID univoco della macchinetta
	 * 
	 */
	public VendingMachine(String id) {	
		this.id = id;
		credit = 0.0;
		status = VendingMachineStatus.ONLINE;
		keyHandler = new KeyHandler();
		bvCatalog = getCatalogFromLocal();//la vending istanzia il catalogo delle bevande prendedolo dal file locale
		tankHandler = new TankHandler(getTanksFromLocal());
		cashContainer = getCashContainerFromLocal(); 
		operatorMode = false;

		rebuildInfo();
		Timer timer = new Timer();
		timer.schedule(new UpdateInfoTimerTask(this), new Date(), TimeUnit.SECONDS.toMillis(20)); //ogni 20 secondi viene notificata la company
	}
	
	/**
	 * Metodo che permette di inserire una moneta.
	 * @param coinValue è il valore della moneta
	 * @throws InvalidCoinException 
	 */
	public void insertCoin(double coinValue) throws InvalidCoinException {
		cashContainer.addCoin(coinValue); 
		saveCashContainerIntoLocal();
		rebuildInfo();
		credit += coinValue;			
	}
	
	/**
	 * Metodo per inserire una chiavetta.
	 * @throws UnrecognisedKeyException 
	 */
	public void insertKey() throws UnrecognisedKeyException { 
		keyHandler.insertKey(credit);
		credit = keyHandler.getCreditOnKey();
	}
	
	/**
	 * Metodo per espellere una chiavetta.
	 * @throws KeyNotInsertedException 
	 */
	public void ejectKey() throws KeyNotInsertedException { 
		keyHandler.ejectKey(credit);
		credit = 0.0;
	}
	
	/**
	 * Metodo che restituisce il resto al cliente.
	 * @throws InsufficientCashForRestException
	 * @throws KeyRestException 
	 */
	public void dispenseCash() throws InsufficientCashForRestException, KeyRestException { 
		if (keyHandler.keyIsInserted()) { 
			throw new KeyRestException("Impossibile erogare resto, togliere la chiavetta");
		}
		cashContainer.dispenseRest(credit);
		saveCashContainerIntoLocal();
		rebuildInfo();
		credit = 0.0;
	}
	
	/**
	 * Metodo che permette al cliente di inserire il codice della bevanda e dopo gli oppurtuni controlli fa partire la transazione economica
	 * @param code Codice della bevanda inserita
	 * @throws InsufficientCreditException
	 * @throws NonExistentCodeException
	 * @throws InsufficientIngredientsException 
	 */
	public String insertCode(String code) throws InsufficientCreditException, NonExistentCodeException, InsufficientIngredientsException { 
		BeverageDescription bvDesc = bvCatalog.getBeverageDesc(code);
		
		if (bvDesc == null) {
			throw new NonExistentCodeException("Codice della bevanda inesistente"); 
		} else if (tankHandler.isAvailable(bvDesc)) {
			startTransaction(bvDesc);
		} else {
			throw new InsufficientIngredientsException("Spiacente, bevanda terminata");
		}
		
		return bvDesc.getName();
	}
	
	/**
	 * Metodo che esegue la transazione economica.
	 * @param bvDesc Descrizione delle bevanda
	 * @throws InsufficientCreditException 
	 */
	public void startTransaction(BeverageDescription bvDesc) throws InsufficientCreditException { 
		Sale sale = new Sale(id, bvDesc, credit);
		saveCashContainerIntoLocal();
		tankHandler.scaleTanksLevel(bvDesc);
		saveTanksIntoLocal();
		rebuildInfo();
		
		credit = sale.getRest();
		
		saveSaleIntoLocal(sale);
	}
	
	public HashMap<Ingredients, Double> getTanksLevels() {
		return tankHandler.getTanksLevel();
	}
	
	/**
	 * Riempie il tank indicato dal parametro.
	 * @param id Id del Tank da riempire  
	 * @throws InsufficientPermissionsException 
	 */
	public void refillTank(String id) throws InsufficientPermissionsException {
		if (!operatorMode) 
			throw new InsufficientPermissionsException("Permessi insufficienti: devi essere un operatore per poter riempire i tank");
		
		tankHandler.refillTank(id); 
		saveTanksIntoLocal();
		rebuildInfo();
	}
	
	/**
	 * Metodo che gestisce il ritiro del credito dalla VendingMachine.
	 *
	 */
	public double withdrawAmount() throws InsufficientPermissionsException { 
		if (!operatorMode) 
			throw new InsufficientPermissionsException("Permessi insufficienti: devi essere un operatore per poter ritirare i soldi");
		
		double withdrawnAmount = cashContainer.withdrawAmount();
		saveCashContainerIntoLocal();
		rebuildInfo();
		return withdrawnAmount;
	}

	/**
	 * Modifica la temperatura dei tank.
	 * @param setpointList lista delle nuove temperature
	 */
	public void modifyTankSettings(String setpointList) { 
		tankHandler.modifyTankSettings(setpointList);
		saveTanksIntoLocal();
		rebuildInfo();
	}
	
	/**
	 * Metodo che permette di ottenere il catalogo delle bevande dalla persistenza locale.
	 */
	private BeverageCatalog getCatalogFromLocal() {
		PersistenceFactory pf = PersistenceFactory.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		return v.getCatalogFromLocal();
	}
	
	/**
	 * Metodo che permette di ottenere i serbatoi dalla persistenza locale.
	 */
	private HashMap<Ingredients,Tank> getTanksFromLocal() {
		PersistenceFactory pf = PersistenceFactory.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		return v.getTanksFromLocal();
	}
	
	/**
	 * Metodo che permette di salvare nella persistenza locale i serbatoi.
	 */
	private void saveTanksIntoLocal() {
		PersistenceFactory pf = PersistenceFactory.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		v.saveTankIntoLocal(tankHandler.getTankList());
	}
	
	/**
	 * Metodo che permette di ottenere la classe di gestione del denaro dalla persistenza locale.
	 */
	private CashContainer getCashContainerFromLocal() {
		PersistenceFactory pf = PersistenceFactory.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		return v.getCashContainerFromLocal();
	}
	
	/**
	 * Metodo che permette di salvare nella persistenza locale la classe di gestione del denaro.
	 */
	private void saveCashContainerIntoLocal() {
		PersistenceFactory pf = PersistenceFactory.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		v.saveCashContainerIntoLocal(cashContainer);
	}
	
	public void setStatus(VendingMachineStatus status) {
		this.status = status;
	}
	
	/**
	 * Verifica che la stringa ricevuta come parametro corrisponda all'ID, se la condizione
	 * è verificata entra in modalità operatore.
	 * @param insertedId Id inserito
	 */
	public boolean enterOperatorMode(String insertedId) {
		if (id.equals(insertedId)) 
			operatorMode = true;
		return operatorMode;
	}
	
	/**
	 * Esce dalla modalità operatore.
	 */
	public void exitOperatorMode() {
		operatorMode = false;
	}

	public String getId() {
		return id;
	}
	
	public Double getCredit() {
		return credit;
	}
	
	public void resetCredit() {
		credit = 0;
	}

	public VendingMachineStatus getStatus() {
		return status;
	}
	
	public BeverageCatalog getCatalog() {
		return bvCatalog;
	}
	
	public int getTankNumber() {
		return tankHandler.getTankNumber();
	}

	public HashMap<Ingredients, Tank> getTankList() {
		return tankHandler.getTankList();
	}
	
	/**
	 * Metodo che permette di salvare nella persistenza locale gli oggetti Sale
	 */
	public void saveSaleIntoLocal(Sale sale) {
		PersistenceFactory pf = PersistenceFactory.getInstance();
		VendingLocalIO v = pf.getVendingLocalIO();
		v.saveSaleIntoLocal(sale);
	}
	
	public double getTotalAmount() {
		return cashContainer.getTotalAmount();
	}
	
	/**
	 * Aggiorna l'attributo info con le informazioni attuali della macchinetta.
	 */
	private void rebuildInfo() {
		StringBuilder infoBuilder = new StringBuilder();
		
		infoBuilder.append(id); infoBuilder.append("/");
		infoBuilder.append(cashContainer.getTotalAmount()); infoBuilder.append("/");
		for (Tank t : tankHandler.getTankList().values()) {
			infoBuilder.append(t.getId()); infoBuilder.append(" ");
			infoBuilder.append(t.getLevel()); infoBuilder.append(" ");
			infoBuilder.append(t.getTemperature()); infoBuilder.append(" ");
			infoBuilder.append(t.getVolume()); infoBuilder.append(" ");
		}
		infoBuilder.append("/"); infoBuilder.append(status);
		
		info = infoBuilder.toString();
	}
	
	public String getInfo() {
		return info;
	}
	
	public void updateCatalog() {
		bvCatalog = getCatalogFromLocal();
	}
	
}
