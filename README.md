# Sapphire (Querying RDF Data Made Simple)

RDF data in the linked open data (LOD) cloud is very valuable for many different applications. In order to unlock the full value of this data, users should be able to issue complex queries on the  RDF datasets in the LOD cloud. SPARQL can express such complex queries, but constructing SPARQL queries can be a challenge to users since it requires knowing the structure and vocabulary of the datasets being queried. In this paper, we introduce Sapphire, a tool that helps users write syntactically and semantically correct SPARQL queries without prior knowledge of the queried datasets. Sapphire interactively helps the user while typing the query by providing auto-complete suggestions based on the queried data. After a query is issued, Sapphire provides suggestions on ways to change the query to better match the needs of the user. Sapphire has been evaluated based on a user study and shown to be superior to other approaches for querying RDF data.


# Installation
## Requirements
- Install a web application server, such as Tomcat.

## Procedure
1. Create a WAR file for Sapphire (Sapphire.war)
2. Copy Sapphire.war to `\var\lib\tomcat\webapps\`
3. Restart the tomcat server: `sudo service tomcat7 stop`
4. Add any required data files to `/var/lib/tomcat7/webapps/Sapphire/WEB-INF/metadata/`
5. Change the log directory in `/var/lib/tomcat7/webapps/Sapphire/WEB-INF/classes/logging.properties`:
	- java.util.logging.FileHandler.pattern= /Sapphire_log_directory/
6. Restart the tomcat server: `sudo service tomcat7 stop`