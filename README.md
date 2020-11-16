# Purpose
Stock Watcher Program for Study GWT.


# How to run the application
Run in Dev Mode 
	-  ant clean build devmode
	- Open browser:  http://127.0.0.1:8888/StockWatcher.html
	                 http://127.0.0.1:8888/StockWatcher.html&locale=de

Run in Production 
	- ant war
    - then copy StockWatcher.war to {tomcat}/webapp/
	- start tomcat
	- access browser {}:9008/StockWatcher

# How to test
Right click on the Test Class and 'Run As -> GWT Junit Test'

