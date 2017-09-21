# Stock-Market-Comparator
Programmed a project in java to mine data from a remote database using MySQL. The program compares the stock values of companies, sorting them by industry, dividing them into 60 common day increments, and writing the data to a new database.

How to run: run Driver.java. Needs new database permissions to run successfully :(

Compares stocks in the johnson330 database against other stocks in the same industry. The comparison data is then stored in a new database. The johnson database has 10 industry groups, the companies in these groups have daily buy and sell rates for a range of dates. We divide the stocks into 60 trading day intervals and adjust for stock splits, excluding compananies with insufficient data. We also shorten the range, so all companies of the same industry group have the same amount of trading data to work with. We now go through the intervals, comparing the ticker return of one stock(closePrice/openPrice - 1) vs the industry return. We do this for every company. The output is written to a new database.


Here is what our output might look like:

Reader connection established.
Writer connection established.
10 industries found
Consumer Discretionary
Consumer Staples
Energy
Financials
Health Care
Industrials
Information Technology
Materials
Telecommunications Services
Utilities
Processing Consumer Discretionary
78 accepted tickers for Consumer Discretionary(2013.06.19 - 2014.08.18), 294 common dates
Processing Consumer Staples
38 accepted tickers for Consumer Staples(2009.02.11 - 2013.06.07), 1088 common dates
Processing Energy
39 accepted tickers for Energy(2011.12.12 - 2014.08.18), 674 common dates
Processing Financials
80 accepted tickers for Financials(2007.06.14 - 2014.08.18), 1808 common dates

Processing Health Care
48 accepted tickers for Health Care(2009.08.21 - 2014.06.30), 1222 common dates
Processing Industrials
60 accepted tickers for Industrials(2011.10.13 - 2014.08.18), 715 common dates
Processing Information Technology
Insufficient data for Information Technology => no analysis
Processing Materials
29 accepted tickers for Materials(2005.08.11 - 2014.08.18), 2270 common dates
Processing Telecommunications Services
7 accepted tickers for Telecommunications Services(2005.02.09 - 2014.08.18), 2397 common dates
Processing Utilities
33 accepted tickers for Utilities(2010.06.16 - 2014.08.18), 1051 common dates
Database connections closed
