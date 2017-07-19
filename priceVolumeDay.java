// Ryan Wendlling
// Sunday November 8th, 2015
// Ahmed 330
// Creates an object to hold all values associated with the current stock of one company on a given day.
// To be used with Driver.Java, assignment 3.

//package appLogic;

public class priceVolumeDay {

	   // Fields for the sDay class
	   public String ticker;
	   public String transDate;
	   public double openPrice;
	   public double highPrice;
	   public double lowPrice;
	   public double closePrice;
	   public double volume;
	   public double adjustedClose;   
	   public priceVolumeDay left;
	   public priceVolumeDay right;
	    
	   
	   public priceVolumeDay() {
		   this.ticker = null;
		   this.transDate = null;
		   this.openPrice = 0;
		   this.highPrice = 0;
		   this.lowPrice = 0;
	       this.closePrice = 0;
	       this.volume = 0;
   	       this.adjustedClose = 0;
		   this.left = null;
	       this.right = null;		   
	   }
	   
	   public priceVolumeDay(String name) {
		   this.ticker = name;
		   this.transDate = null;
		   this.openPrice = 0;
		   this.highPrice = 0;
		   this.lowPrice = 0;
	       this.closePrice = 0;
	       this.volume = 0;
   	       this.adjustedClose = 0;
		   this.left = null;
	       this.right = null;		   
	   }
	   
	   
	   //Creates instance of the sDay object
	   priceVolumeDay(String ticker, String transDate, double openPrice, double highPrice, double lowPrice, double closePrice, double volume, double adjustedClose) {
	      this.ticker = ticker;
	      this.transDate = transDate;
	      this.openPrice = openPrice;
	      this.highPrice = highPrice;
	      this.lowPrice = lowPrice;
	      this.closePrice = closePrice;
	      this.volume = volume;
	      this.adjustedClose = adjustedClose;
	      this.left = null;
	      this.right = null;
	   }   


	   //Getter methods for the next and previous objects.
	   public priceVolumeDay lefty() {
	        return left; 
	   }


	   public priceVolumeDay righty() {
	        return right; 
	   }  	
}
