// Ryan Wendlling
// Sunday November 8th, 2015
// Ahmed 330
// Creates an object to hold all the trading days for each stock.
// To be used with Driver.Java, assignment 3.

//package appLogic;

public class companyTradingDays {

	   // Fields for the sDay class
	   public String ticker;
	   public int tradingDays;
	   
	   companyTradingDays (String name, int total) {
		   this.ticker = name;
		   this.tradingDays = total;	   
	   }
}
