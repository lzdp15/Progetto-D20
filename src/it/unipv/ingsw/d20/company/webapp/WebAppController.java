package it.unipv.ingsw.d20.company.webapp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.unipv.ingsw.d20.company.Company;
import it.unipv.ingsw.d20.company.VendingMachineInfo;
import it.unipv.ingsw.d20.company.webapp.exceptions.InvalidPasswordException;
import it.unipv.ingsw.d20.company.webapp.exceptions.InvalidUserException;
import it.unipv.ingsw.d20.util.persistence.PersistenceFacade;
import it.unipv.ingsw.d20.util.persistence.beveragedescription.BeverageDescriptionPOJO;
import it.unipv.ingsw.d20.util.persistence.beveragedescription.IBeverageDescriptionDao;
import it.unipv.ingsw.d20.util.persistence.ingredientrecipe.IIngredientRecipeDao;
import it.unipv.ingsw.d20.util.persistence.ingredientrecipe.IngredientRecipePOJO;
import it.unipv.ingsw.d20.util.persistence.operator.IOperatorDao;
import it.unipv.ingsw.d20.util.persistence.operator.OperatorPOJO;
import it.unipv.ingsw.d20.util.persistence.paymentKey.IKeyDao;
import it.unipv.ingsw.d20.util.persistence.paymentKey.KeyPOJO;
import it.unipv.ingsw.d20.util.persistence.vending.IVendingDao;
import it.unipv.ingsw.d20.util.persistence.vending.VendingPOJO;

/**
 * Gestisce le operazioni delle servlet, costituendo per esse un'interfaccia verso il DB e la Company.
 *
 */
public class WebAppController {
	private PersistenceFacade facade;
	private IVendingDao vendingsManager;
	private IOperatorDao operatorsManager;
	private IKeyDao keysManager;
	private IBeverageDescriptionDao beveragesManager;
	private IIngredientRecipeDao ingredientsManager;
	private OperatorPOJO loggedOperator;
	private boolean limited; //E' true quando l'operatore loggato è un operatore e non un operatore remoto
	private Map<String, VendingMachineInfo> infoList;
	public static String absenceString="None";
	private List<ReportPOJO> reportList;
	
	private enum OperatorType{
		Operator, RemoteOperator;
	}
	
	public WebAppController() {
		facade= PersistenceFacade.getInstance();
		vendingsManager=facade.getVendingDao();
		operatorsManager=facade.getOperatorDao();
		keysManager=facade.getKeyDao();
		beveragesManager=facade.getBeverageDescriptionDao();	
		ingredientsManager=facade.getIngredientRecipeDao();
		infoList =Company.vendingMachineInfoList;
		reportList=new LinkedList<>();
		reportList.add(new ReportPOJO("VEND1", "Connection Lost", "testing report", "Mr.Tester"));
	}
		
	//GESTIONE DEL LOGIN
	public OperatorPOJO getLoggedOperator() {
		return loggedOperator;
	}
	
	public void setNotLogged() {
		loggedOperator=null;
	}
	
	public void checkOperatorLogIn (String username, String password) throws InvalidPasswordException, InvalidUserException{
		OperatorPOJO operator=lookForOperator(username);
		
		if (operator.getCode().equals(username) && operator.getPassword().equals(password)) {
			loggedOperator=getOperator(username);
		}
		else {
			throw new InvalidPasswordException();
		}
		
		checkLimitation();
	}
	
	private OperatorPOJO lookForOperator(String username) throws InvalidUserException {
		OperatorPOJO operator;
		if (getOperator(username)==null) {
			throw new InvalidUserException();
		}
		else {
			operator=getOperator(username);
		}
		return operator;
	}
	
	private void checkLimitation() {
		if (loggedOperator.getType().equals(OperatorType.Operator.toString())){
			setLimited(true);
		}
		else {
			setLimited(false);
		}
	}
	private void setLimited(boolean bool) {
		this.limited=bool;
	}
	
	public boolean isLimited() {
		return limited;
	}

	//VENDING MACHINES
	public List<VendingPOJO> getAllVendingMachines() {
		return vendingsManager.getAllVendings();
	}
	
	public VendingPOJO getVendingMachine(String id) {
		return vendingsManager.getVending(id);
	}
	
	public VendingMachineInfo getVendingMachineInfo(String id) {
		return infoList.get(id);	
	}
	
	public Map<String, VendingMachineInfo> getAllVendingMachineInfo() {
		return infoList;	
	}
	
	//OPERATORS
	public List<OperatorPOJO> getAllOperators() {
		return operatorsManager.getAllOperators();
	}
	
	public OperatorPOJO getOperator(String code) {
		return operatorsManager.getOperator(code);
	}
	
	public void addOperator(String code, String name, String password, String type) {
		operatorsManager.addOperator(code, name, password, type);
	}
	
	//KEYS
	public List<KeyPOJO> getAllKeys() {
		return keysManager.getAllKeys();
	}
	
	public void addKey(int serialCode, double credit) {
		keysManager.addKey(serialCode, credit);
	}
	
	public void deactivateKey(String serialCode) {
		keysManager.deactivateKey(serialCode);
	}
	
	//BEVERAGES
	public List<BeverageDescriptionPOJO> getAllBeverageDescriptions() {
		return beveragesManager.getAllBeverageDescriptions();
	}
	
	public BeverageDescriptionPOJO getBeverageDescription(String beverageName) {
		return beveragesManager.getBeverageDescriptionByBevName(beverageName);
	}
	
	public void updateIngredients (String idRecipe, String ingredientName, double quantity) {
		ingredientsManager.updateIngredientRecipe(idRecipe, ingredientName, quantity);
	}
	
	public List<String> getIngredientsNames(String idRecipe) {
		List<String> ingredientsNames=new LinkedList<>();
		List<IngredientRecipePOJO> recipe= ingredientsManager.getAllIngredientRecipeByIdRecipe(idRecipe);
		
		for (IngredientRecipePOJO entry: recipe) {
			ingredientsNames.add(entry.getIngredientName());
		}
		
		if (ingredientsNames.size()<IngredientRecipePOJO.maxIngredients) {
			int ingredientNumber= ingredientsNames.size();
			int i;
			for (i=0; i<(IngredientRecipePOJO.maxIngredients-ingredientNumber); i++) {
				ingredientsNames.add("None");
			}
		}
		return ingredientsNames;
	}
	
	public List<Double> getIngredientsQuantities(String idRecipe) {
		List<Double> ingredientsQuantities=new LinkedList<>();
		List<IngredientRecipePOJO> recipe= ingredientsManager.getAllIngredientRecipeByIdRecipe(idRecipe);
		
		for (IngredientRecipePOJO entry: recipe) {
			ingredientsQuantities.add(entry.getQuantity());
		}
		
		if (ingredientsQuantities.size()<IngredientRecipePOJO.maxIngredients) {
			int ingredientNumber= ingredientsQuantities.size();
		
			int i;
			for (i=0; i<(IngredientRecipePOJO.maxIngredients-ingredientNumber); i++) {
				ingredientsQuantities.add(0.0);
			}
		}
		return ingredientsQuantities;
	}

	public List<ReportPOJO> getReportList() {
		return reportList;
	}
	
	public void addReport(ReportPOJO report) {
		reportList.add(report);
	}
	
	public void removeReport(String id, String problem) {
		for (ReportPOJO report: reportList) {
			if (report.getVendingID().equals(id) && report.getProblem().equals(problem)) {
				reportList.remove(report);
			}
		}
	}
	
}
