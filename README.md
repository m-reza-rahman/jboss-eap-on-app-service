# Deploying a Java/Jakarta EE Application to JBoss EAP on Azure App Service
This demo shows how you can deploy a Java/Jakarta EE application to Azure using managed JBoss EAP on App Service (the premier PaaS platform on Azure). It is the demo for [this](https://www.papercall.io/speakers/reza/speaker_talks/260453-hyperscale-jakarta-ee-paas-on-azure) talk.

We use Visual Studio Code but you can use any Maven capable IDE such as Eclipse or IntelliJ. We use Azure PostgreSQL but you can use Azure SQL, Azure MySQL or Oracle DB@Azure.

## Setup
* Install JDK 11 (we used [Eclipse Temurin OpenJDK 11 LTS](https://adoptium.net/?variant=openjdk11)).
* Install VS Code for Java from [here](https://code.visualstudio.com/docs/languages/java). Make sure the Java Extension Pack is installed.
* Download this repository somewhere in your file system (easiest way might be to download as a zip and extract).
* You will need an Azure subscription. If you don't have one, you can get one for free for one year [here](https://azure.microsoft.com/en-us/free).

## Start Managed PostgreSQL on Azure
* Go to the [Azure portal](http://portal.azure.com).
* Select 'Create a resource'. In the search box, enter and select 'Azure Database for PostgreSQL'. Hit create.
* Create a new resource group named jakartaee-cafe-group-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the Server name to be jakartaee-cafe-db-`<your suffix>` (the suffix could be your first name such as "reza"). Specify the login name to be postgres. Specify the password to be Secret123!. Click Next to go to the Networking tab.
* Enable access to Azure services and add the current client IP address.
* Create the resource. It will take a moment for the database to deploy and be ready for use.
* In the portal home, go to 'All resources'. Find and click on jakartaee-cafe-db-`<your suffix>`. Open the server parameters panel. Set the 'require_secure_transport' parameter to 'OFF', and then hit 'Save'.

Once you are done exploring the demo, you should delete the jakartaee-cafe-group-`<your suffix>` resource group. You can do this by going to the portal, going to resource groups, finding and clicking on jakartaee-cafe-group-`<your suffix>` and hitting delete. This is especially important if you are not using a free subscription! If you do keep these resources around (for example to begin your own prototype), you should in the least use your own passwords and make the corresponding changes in the demo code.

## Setup Managed JBoss EAP
* Go to the [Azure portal](http://portal.azure.com).
* Select 'Create a resource'. In the search box, enter and select 'Web App'. Hit create.
* Enter jakartaee-cafe-web-`<your suffix>` (the suffix could be your first name such as "reza") as application name and select jakartaee-cafe-group-`<your suffix>` as the resource group. Choose Java 11 as your runtime stack and JBoss EAP 7 as the Java web server stack. Hit create.

## Install the Azure CLI
* In order to deploy the application, we will need to [install the Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest).
* Open a console and execute the following to log onto Azure.

	```
	az login
	```
## Start the Application on Managed JBoss EAP
The next step is to get the application up and running on managed JBoss EAP. Follow the steps below to do so.

* Start VS Code.
* Get the jakartaee-cafe application into the IDE. In order to do that, go to File -> Open. Then browse to where you have this repository code in your file system and select jakartaee-cafe. Let VS Code set the folder up as a Java project when prompted.
* Once the application loads, open the [pom.xml](jakartaee-cafe/pom.xml) file and replace occurrences of `reza` with `<your suffix>`.
* You should do a full Maven build by going to the Maven panel, right clicking jakartaee-cafe and selecting "clean" and then "package".
* You should note the pom.xml. In particular, we have included the configuration for the Azure Maven plugin we are going to use to deploy the application to managed JBoss EAP:

```xml
<plugin>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-webapp-maven-plugin</artifactId>
    <version>2.12.0</version>
    <configuration>
        <appName>jakartaee-cafe-web-reza</appName>
        <resourceGroup>jakartaee-cafe-group-reza</resourceGroup>
        <javaVersion>Java 11</javaVersion>
        <webContainer>JBossEAP 7</webContainer>
        <appSettings>
            <!-- Increase the timeout -->
            <property>
                <name>WEBSITES_CONTAINER_START_TIME_LIMIT</name>
                <value>500</value>
            </property>
        </appSettings>
        <deployment>
            <resources>
                <resource>
                    <type>lib</type>
                    <directory>${project.basedir}/src/main/jboss/config</directory>
                    <includes>
                        <include>postgresql-42.7.1.jar</include>
                    </includes>
                </resource>
                <resource>
                    <type>script</type>
                    <directory>${project.basedir}/src/main/jboss/config</directory>
                    <includes>
                        <include>postgresql-module.xml</include>
                        <include>jboss_cli_commands.cli</include>
                    </includes>
                </resource>
                <resource>
                    <type>startup</type>
                    <directory>${project.basedir}/src/main/jboss/config</directory>
                    <includes>
                        <include>startup.sh</include>
                    </includes>
                </resource>
                <resource>
                    <directory>${project.basedir}/target</directory>
                    <includes>
                        <include>jakartaee-cafe.war</include>
                    </includes>
                </resource>
            </resources>
        </deployment>
    </configuration>
</plugin>
```

* It is now time to deploy and run the application on Azure. Go to the Maven panel and then jakartaee-cafe -> Plugins -> azure-webapp -> deploy. Right click and hit 'Run'.
* Keep an eye on the console output. You will see the application deployment progress. It may take a while for the deployment to complete. The application will be available at https://jakartaee-cafe-web-your-suffix.azurewebsites.net when it is successfully deployed.
* Once the application starts, you can test the REST service at the URL: https://jakartaee-cafe-web-your-suffix.azurewebsites.net/rest/coffees or via the JSF client at https://jakartaee-cafe-web-your-suffix.azurewebsites.net/index.xhtml.
