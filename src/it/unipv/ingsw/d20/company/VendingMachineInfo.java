package it.unipv.ingsw.d20.company;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.unipv.ingsw.d20.vendingmachine.model.VendingMachineStatus;
import it.unipv.ingsw.d20.vendingmachine.model.beverage.Ingredients;
import it.unipv.ingsw.d20.vendingmachine.model.tanks.Tank;

/**
 * Classe che raccoglie tutte le informazioni (temporanee) relative ad un
 * determinato distributore.
 *
 */
public class VendingMachineInfo {
	
	private Date lastUpdate;
	private double currentAmount;
	private List<Tank> tankList = new ArrayList<>();
	private VendingMachineStatus status;
	private boolean tempsSetPointModified;
	
	/**
	 * Costruttore che imposta solo l'ammontare corrente a 0 e lo status su OFF.
	 */
	public VendingMachineInfo() {
		currentAmount = 0;
		status = VendingMachineStatus.OFF;
		refreshLastUpdate();
	}
	
	/**
	 * Costruttore completo che inizializza tutti gli attributi della classe.
	 * @param cashInfo ammontare corrente contenuto nella macchinetta
	 * @param tankInfo informazioni riguardanti i tank
	 * @param statusInfo status del distributore
	 */
	public VendingMachineInfo(String cashInfo, String tankInfo, String statusInfo) {
		currentAmount = Double.parseDouble(cashInfo);
		setTankList(tankInfo);
		setStatusString(statusInfo);
		refreshLastUpdate();
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	/**
	 * Reimposta l'orario dell'ultima notifica al momento attuale.
	 */
	private void refreshLastUpdate() {
		lastUpdate = new Date();
	}
	
	public double getCurrentAmount() {
		return currentAmount;
	}
	
	public List<Tank> getTankList() {
		return tankList;
	}
	
	/**
	 * Si occupa di dividere la stringa e utilizzarla
	 * per inizializzare la lista dei tank.
	 * @param tankInfo stringa che contiene le informazioni sui tank
	 */
	private void setTankList(String tankInfo) {
		String[] infoSplit = tankInfo.split(" ");

		for (int i = 0; i < infoSplit.length; i = i + 4) {
			Ingredients ingredient = Ingredients.valueOf(infoSplit[i]);
			double level = Double.parseDouble(infoSplit[i + 1]);
			double temperature = Double.parseDouble(infoSplit[i + 2]);
			double volume = Double.parseDouble(infoSplit[i + 3]);
			Tank t = new Tank(ingredient, level, temperature, volume);
			
			tankList.add(t);
		}
		
	}
	
	/**
	 * Aggiorna le temperature dei tank.
	 * @param updatedTanksTemps lista di nuove temperature
	 */
	public void updateTanksTemp(List<Double> updatedTanksTemps) {
		for (int i = 0; i < tankList.size(); i++) {
			tankList.get(i).setTemperature(updatedTanksTemps.get(i));
		}
		tempsSetPointModified=true;
	}
	
	/**
	 * Restituisce una stringa con le temperatura aggiornate.
	 * @return stringa di temperature separate da spazi
	 */
	public String getUpdatedTemps() {
		StringBuilder updatedTemps = new StringBuilder();
		for (Tank tank : tankList) {
			updatedTemps.append(tank.getTemperature() + " ");
		}
		return updatedTemps.toString();
	}
	
	public VendingMachineStatus getStatus() {
		return status;
	}
	
	public String getStatusString() {
		return status.toString();
	}
	
	public void setStatus(VendingMachineStatus status) {
		this.status = status;	
	}
	
	public void setStatusString(String statusInfo) {
		status = VendingMachineStatus.valueOf(statusInfo);
	}

	public boolean isTempsSetPointModified() {
		return tempsSetPointModified;
	}
	
	public void setTempsSetPointModified(boolean bool) {
		tempsSetPointModified=bool;
	}

}












