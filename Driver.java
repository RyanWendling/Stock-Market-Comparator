// Ryan Wendlling
// Sunday November 8th, 2015
// Ahmed 330, Assignment 3
// Creates a program to read in data from a remote database using SQL. Holds and reads through the data, showing the stock
// splits. Afterwards Groups every company by their industry and compares them to each other. Dividing their common days into increments of 60
// looops through increments and invest money to figure out how each company is doing. Write new data back to the database.
//package appLogic;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
   

class Driver {


    static Connection conn = null;
    static Connection conn2 = null;
    static Statement stmt2 = null;
    
    //Create a arraylist to hold custom priceVolume item. (Note: array is reset for each looping for new query)
    static ArrayList <priceVolumeDay> priceVolumeList = new ArrayList<priceVolumeDay>();
    static ArrayList <priceVolumeDay> priceVolumeListAdj = new ArrayList<priceVolumeDay>();
    static ArrayList <ranges> intervalRanges = new ArrayList<ranges>();
    static ArrayList <String> industries = new ArrayList<String>();
    static int days = 0;
    static double dollars = 1000;
    static double openShares = 0;
    static final String createPerformanceTable = "create table Performance (Industry char(30), Ticker char(6), StartDate char(10), EndDate char(10), TickerReturn char(12), IndustryReturn char(12))";
    static final String insertPerformance = "insert into Performance (Industry, Ticker, StartDate, EndDate, TickerReturn, IndustryReturn) values(?, ?, ?, ?, ?, ?)";
    static final String dropPerformanceTable = "drop table if exists Performance;";
    
    public static void main(String[] args) throws Exception {
        // Get connection properties
        String paramsFile = "readerparams.txt";
        String paramsFile2 = "writerparams.txt";
        if (args.length >= 1) {
            paramsFile = args[0];
            paramsFile2 = args[1];
        }
        Properties connectprops = new Properties();
        connectprops.load(new FileInputStream(paramsFile));
        
        Properties connectprops2 = new Properties();
        connectprops2.load(new FileInputStream(paramsFile2));

        try {
            // Get connection
            Class.forName("com.mysql.jdbc.Driver");
            
            String dburl = connectprops.getProperty("dburl");
            String username = connectprops.getProperty("user");
            conn = DriverManager.getConnection(dburl, connectprops);
            System.out.printf("Database connection %s %s established.%n", dburl, username);
            
            String dburl2 = connectprops2.getProperty("dburl");
            String username2 = connectprops2.getProperty("user");
            conn2 = DriverManager.getConnection(dburl2, connectprops2);
            System.out.printf("Database connection %s %s established.%n", dburl2, username2);
            stmt2 = conn2.createStatement();
            stmt2.executeUpdate("drop table if exists Performance");
            stmt2.executeUpdate(dropPerformanceTable);
            int result = stmt2.executeUpdate("create table Performance (Industry char(30), Ticker char(6), StartDate char(10), EndDate char(10), TickerReturn char(12), IndustryReturn char(12))");
            if (result == 0) {
            	System.out.println("Performance table created, please wait..");
            } else {
            	System.out.println("nah");
            }

            industrySort();
            System.out.println("Amount of industries = "+industries.size());
            for (int a = 0; a < industries.size(); a++) {
            	if (a == 6) {
            		System.out.println();
            		System.out.print("no analysis for Information Technology");
            		continue;
            	}       	
                System.out.println();
            	priceVolumeList.clear();
            	priceVolumeListAdj.clear();
            	intervalRanges.clear();

                System.out.print("Beginning calculations for "+industries.get(a)+"..");
                companyRange(industries.get(a));

            }
            showCompanyName();
            conn.close();
        } catch (SQLException ex) {
            System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                                    ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
        }
    }
    
    
//  Uses a prepared statement to give the company name given the ticker
    static void showCompanyName() throws SQLException {
        // Prepare query
        PreparedStatement pstmt = conn2.prepareStatement(
                "select * " +
                "  from Performance " +
                " where Industry = 'Telecommunications Services' " +
                " order by StartDate, Ticker " +
                " limit 10 ");

        ResultSet rs = pstmt.executeQuery();

        // Did we get anything? If so, output data.
        while (rs.next()) {
            for (int i = 1; i <= 6; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = rs.getString(i);
                System.out.print(columnValue);
            }
        }
        pstmt.close();
    }
    
    
//  Uses a prepared statement to show all companies from certain industry.
    static void industrySort() throws SQLException {
        
    	// Prepare query
        PreparedStatement pstmt = conn.prepareStatement(
                "select Industry "
                + " from Company natural join PriceVolume "
               + " group by Industry ");
     
        ResultSet rs = pstmt.executeQuery();
        // Did we get anything? If so, output data.
        while (rs.next()) {
        	industries.add(rs.getString(1));
            System.out.printf("%s%n", rs.getString(1));
        }
        pstmt.close();
    }
    
    
    static void addToPerformance (String industry, String ticker, String begin, String end, String aReturn, String theReturn) throws SQLException {
		
    	try {
    	PreparedStatement pStmt = conn2.prepareStatement(insertPerformance);
   		pStmt.setString(1, industry);
   		pStmt.setString(2, ticker);
   		pStmt.setString(3, begin);
   		pStmt.setString(4, end);
   		pStmt.setString(5, aReturn);
   		pStmt.setString(6, theReturn);
   		pStmt.executeUpdate();
   		pStmt.close();
   		
    	} catch (SQLException se) {
    		throw se;
    	}
   		
    }
    
    
//  Uses a prepared statement to show all companies from certain industry.
    static void getTradingDays(ArrayList<companyTradingDays> tradingDayList, String beginning, String end, String industry) throws SQLException {
        // Prepare query
        PreparedStatement pstmt = conn.prepareStatement(
                "select Ticker, count(distinct TransDate) as TradingDays "
                + " from Company natural join PriceVolume "
                + " where Industry =  ? "
                + " and TransDate >= ? and TransDate <= ? "
                + " group by Ticker "
                + " having TradingDays >= 150 "
                + " order by Ticker ");

        pstmt.setString(1, industry);
        pstmt.setString(2, beginning);
        pstmt.setString(3, end);
        ResultSet rs = pstmt.executeQuery();
        // Did we get anything? If so, output data.
        while (rs.next()) {
            companyTradingDays aCompany = new companyTradingDays (rs.getString(1), rs.getInt(2));
            tradingDayList.add(aCompany);
        }
        pstmt.close();
    }

    
//  Uses a prepared statement to show all companies from certain industry. //write this to database and compare further?
    static void companyRange(String anIndustry) throws SQLException {
        
        String industry = anIndustry;
        String minMaxInd;
        String maxMinInd;
        int minTradingDays = 0; 
        int tradingIntervals = 0;
        ArrayList <priceVolumeDay> companiesList = new ArrayList<priceVolumeDay>();
        ArrayList <companyTradingDays> tradingDaysList = new ArrayList<companyTradingDays>();
        
    	// Prepare query
        PreparedStatement pstmt = conn.prepareStatement(
                "select Ticker, min(TransDate), max(TransDate), count(distinct TransDate) as TradingDays"
                + " from Company natural left outer join PriceVolume "
                + " where Industry = ? "
                + " group by Ticker "
                + " having TradingDays >= 150 "
                + " order by Ticker ");

        pstmt.setString(1, industry);
        ResultSet rs = pstmt.executeQuery();
        ArrayList <String> minDates = new ArrayList<String>();
        ArrayList <String> maxDates = new ArrayList<String>();
        // Did we get anything? If so, output data.
        while (rs.next()) {
        	priceVolumeDay aDay = new priceVolumeDay(rs.getString(1));
        	companiesList.add(aDay);
        	minDates.add(rs.getString(2));
        	maxDates.add(rs.getString(3));
        	minTradingDays = rs.getInt(4);
        }     
        String minMax = minDates.get(0);
        
        for (int i = 0; i < minDates.size(); i++) {
        	String string = minDates.get(i);
        	try {
        		SimpleDateFormat converter = new SimpleDateFormat("yyyy.MM.dd");
        		Date date1 = converter.parse(minMax);
        		Date date2 = converter.parse(string);
        		if (date2.after(date1)) {
        			minMax = string;
        		}
        	}catch(ParseException ex){
        		System.out.println("cant convert string date to date date");
        	}
        }
        String maxMin = maxDates.get(0);
        
        for (int i = 0; i < maxDates.size(); i++) {
        	String string = maxDates.get(i);
        	try {
        		SimpleDateFormat converter = new SimpleDateFormat("yyyy.MM.dd");
        		Date date1 = converter.parse(maxMin);
        		Date date2 = converter.parse(string);
        		if (date2.before(date1)) {
        			maxMin = string;
        		}
        	}catch(ParseException ex){
        		System.out.println("cant convert string date to date date");
        	}
        }           
        //biggest
        maxMinInd = maxMin;
        //smallest
        minMaxInd = minMax;
        pstmt.close();        

        getTradingDays(tradingDaysList, minMax, maxMin, industry);
        
        for (int j = 0; j < tradingDaysList.size(); j++) {
        	if (tradingDaysList.get(j).tradingDays < minTradingDays) {
        		minTradingDays = tradingDaysList.get(j).tradingDays;
        	}
        }
        tradingIntervals = minTradingDays/60;
               
        rangePriceVolume(companiesList.get(0).ticker, minMax, maxMin);
		Collections.reverse(priceVolumeList);
		
    	for (int k = 0; k < tradingIntervals; k++) {
			int start = 60 * k;
			int end = (60 * (k + 1)) - 1;
			String startString = priceVolumeList.get(start).transDate;
			String endString = priceVolumeList.get(end).transDate;
			ranges aDayR = new ranges(startString, endString);
			intervalRanges.add(aDayR);
    	}
    	
        //get industry average in arraylist
    	ArrayList <Double> industryReturn = new ArrayList<Double>();
    	for (int q = 0; q < tradingIntervals; q++) {
    		industryReturn.add(q, 0.0);
    	}
    	
    	// find value for entire industry average, interval by interval
        for (int i = 0; i < companiesList.size(); i++) {
        	priceVolumeList.clear();
        	rangePriceVolume(companiesList.get(i).ticker, minMax, maxMin);
    		Collections.reverse(priceVolumeList);
        	
        	
        	for (int k = 0; k < tradingIntervals; k++) {
        		openShares = 0;
        		
        		for (int l = 60 * k; l <= 60 * (k + 1);) {
        			try {
                		SimpleDateFormat converter = new SimpleDateFormat("yyyy.MM.dd");
                		Date date1 = converter.parse(intervalRanges.get(k).start);
                		Date date2 = converter.parse(priceVolumeList.get(l).transDate);
                		if ((date1.before(date2) || date1.equals(date2))) {          			                			
                			openShares += (priceVolumeList.get(l).openPrice);
                    		break;
                		} else {
                			l++;
                		}    		
                	}catch(ParseException ex){
                		System.out.println("cant convert string date to date date");
                	}
        		}	

        		for (int l = 60 * k; l <= 60 * (k + 1);) {
            		try {
                    	SimpleDateFormat converter = new SimpleDateFormat("yyyy.MM.dd");
                    	Date date2 = converter.parse(priceVolumeList.get(l).transDate);
                    	Date date3 = converter.parse(intervalRanges.get(k).end);
                    	if (date2.after(date3) || date2.equals(date3)) {	
                       		double tickerReturn = ((priceVolumeList.get(l).closePrice)/openShares);                     		
                       		double currentAvg = industryReturn.get(k);
                       		double currentAvgThis = tickerReturn;                      		
                       		currentAvg += currentAvgThis;
                       		industryReturn.set(k, currentAvg);
                       		break;
                    	} else {
                    		l++;
                    	}        		
                   	}catch(ParseException ex){
                   		System.out.println("cant convert string date to date date");
                   	}
        		}
        	}      	      	
        }


    	// finds value for specific stock, adds everything to performance table
        for (int i = 0; i < companiesList.size(); i++) {
        	priceVolumeList.clear();
        	rangePriceVolume(companiesList.get(i).ticker, minMax, maxMin);
    		Collections.reverse(priceVolumeList);
        	    	
        	for (int k = 0; k < tradingIntervals; k++) {       		
        		String startPerf = null;
        		openShares = 0;
        		for (int l = 60 * k; l <= 60 * (k + 1);) {
        			try {
                		SimpleDateFormat converter = new SimpleDateFormat("yyyy.MM.dd");
                		Date date1 = converter.parse(intervalRanges.get(k).start);
                		Date date2 = converter.parse(priceVolumeList.get(l).transDate);
                		if ((date1.before(date2) || date1.equals(date2))) {         			                			
                			startPerf = priceVolumeList.get(l).transDate;
                			openShares += (priceVolumeList.get(l).openPrice);
                    		break;
                		} else {
                			l++;
                		} 		
                	}catch(ParseException ex){
                		System.out.println("cant convert string date to date date");
                	}
        		}	
        			
        		for (int l = 60 * k; l <= 60 * (k + 1);) {
            		try {
                    	SimpleDateFormat converter = new SimpleDateFormat("yyyy.MM.dd");
                    	Date date2 = converter.parse(priceVolumeList.get(l).transDate);
                    	Date date3 = converter.parse(intervalRanges.get(k).end);
                    	if (date2.after(date3) || date2.equals(date3)) {         		
                       		double tickerReturn = ((priceVolumeList.get(l).closePrice)/openShares) - 1;
                       		String tickerReturnString = String.format("%10.7f", tickerReturn);
                       		double totalIndustry = industryReturn.get(k);
                       		double lessTotalIndustry = totalIndustry - (tickerReturn + 1);
                       		double companyD = companiesList.size();
                       		double divisor = companyD - 1.0;
                       		double divisorMinus = 1.0/divisor;
                       		double nextt = lessTotalIndustry * (divisorMinus);
                       		double finalInd = nextt - 1;
                       		String finalIndString = String.format("%10.7f", finalInd);

                       		addToPerformance(industry, priceVolumeList.get(l).ticker, startPerf, priceVolumeList.get(l).transDate, tickerReturnString, finalIndString);       		
                       		break;
                    	} else {
                    		l++;
                    	}    		
                   	}catch(ParseException ex){
                   		System.out.println("cant convert string date to date date");
                   	}
        		}
        	}      	      	
        }      
    }
     
  
//  Gets the data using SQL from the remote database and takes the values and puts them into an arraylist    
    static void rangePriceVolume(String... names) throws SQLException {
 
    	String ticker;
    	String beginDate;
    	String endDate;
    	ticker = names[0];
    	int size = names.length;
    	if (size == 1){
        	beginDate = "1900.01.01";
        	endDate = "2020.01.01";
        } else {
        	beginDate = names[1];
        	endDate = names[2];
        }
             
        // Prepare query
        PreparedStatement pstmt = conn.prepareStatement(
                "select Ticker, TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice, Volume, AdjustedClose" + 
                "  from PriceVolume " +
                "  where Ticker = ? and TransDate >= ? and TransDate <= ?" + "order by TransDate DESC");
     
        // Fill in the blanks
        pstmt.setString(1, ticker);
        pstmt.setString(2, beginDate);
        pstmt.setString(3, endDate);
        ResultSet rs = pstmt.executeQuery();
        // Did we get anything? If so, output data.
        
        while (rs.next()) {
        	priceVolumeDay aDay = new priceVolumeDay(rs.getString(1), rs.getString(2), rs.getFloat(3), rs.getFloat(4),
        			rs.getFloat(5), rs.getFloat(6), rs.getDouble(7), rs.getFloat(8));
        	priceVolumeList.add(aDay);
        }
        linker(priceVolumeList);
        splitDay(priceVolumeList);       
        pstmt.close();
    }
    
    
//  Links the priceVolumeDay objects that are in the arraylist.
    public static void linker(ArrayList<priceVolumeDay> sArray){  
    	 
        sArray.get(0).right = sArray.get(1);
        for (int i = 1; i < sArray.size() - 1; i++) {
           sArray.get(i).right = sArray.get(i + 1);
           sArray.get(i).left = sArray.get(i - 1);      
        }
        sArray.get(sArray.size() - 1).left = sArray.get(sArray.size() - 2);
     }
    
    
//  Loops through the data in reverse chronological order, printing out the stock split days and adjusting the rest of
//  the data by factors of 1.5, 2, 3 depending on the type of split that happened.    
    public static void splitDay(ArrayList<priceVolumeDay> sArray){
        int splits = 0;
        double twoOneSplit = 2.0;
        double threeOneSplit = 3.0;
        double threeTwoSplit = 1.5;
        double comparator1 = .20;
        double comparator2 = .30;
        double comparator3 = .15;
        days = 0;
        
        for (int n = 0; n < sArray.size() - 1; n++) {
        	
           double ratio1 = ((sArray.get(n).right.closePrice) / sArray.get(n).openPrice);
        	
           if ((Math.abs(ratio1 - twoOneSplit)) < comparator1) {
        	   String closer = String.format("%.2f",sArray.get(n).right.closePrice);
        	   String opener = String.format("%.2f",sArray.get(n).openPrice);
                           
           }
           if ((Math.abs(ratio1 - threeOneSplit)) < comparator2) {
        	   String closer = String.format("%.2f",sArray.get(n).right.closePrice);
        	   String opener = String.format("%.2f",sArray.get(n).openPrice);
          
           }
           if ((Math.abs(ratio1 - threeTwoSplit)) < comparator3) {
        	   String closer = String.format("%.2f",sArray.get(n).right.closePrice);
        	   String opener = String.format("%.2f",sArray.get(n).openPrice);

           }                         
        }
        
        for (int i = 0; i < sArray.size() - 1; i++) {
        	days++;
           double ratio = ((sArray.get(i).right.closePrice) / sArray.get(i).openPrice);
        	
           if ((Math.abs(ratio - twoOneSplit)) < comparator1) {
               splits++;   
               for (int p = (i+1); p <= sArray.size() - 1; p++) {
                   sArray.get(p).openPrice = ((sArray.get(p).openPrice)/2d);
                   sArray.get(p).highPrice = ((sArray.get(p).highPrice)/2d);
                  sArray.get(p).closePrice = ((sArray.get(p).closePrice)/2d);
                   sArray.get(p).lowPrice = ((sArray.get(p).lowPrice)/2d);
               }                                  
           }
           if ((Math.abs(ratio - threeOneSplit)) < comparator2) {
               splits++;
               for (int g = (i+1); g <= sArray.size() - 1; g++) {
                   sArray.get(g).openPrice = ((sArray.get(g).openPrice)/3d);
                   sArray.get(g).highPrice = ((sArray.get(g).highPrice)/3d);
                   sArray.get(g).closePrice = ((sArray.get(g).closePrice)/3d);
                   sArray.get(g).lowPrice = ((sArray.get(g).lowPrice)/3d);
               }                 
           }
           if ((Math.abs(ratio - threeTwoSplit)) < comparator3) {
               splits++;
               for (int m = (i+1); m <= sArray.size() - 1; m++) {
               sArray.get(m).openPrice = ((sArray.get(m).openPrice)/1.5d);
               sArray.get(m).highPrice = ((sArray.get(m).highPrice)/1.5d);
               sArray.get(m).closePrice = ((sArray.get(m).closePrice)/1.5d);
               sArray.get(m).lowPrice = ((sArray.get(m).lowPrice)/1.5d);
               }
           }                         
        }
        days++;
     }
}