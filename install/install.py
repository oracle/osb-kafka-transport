import os

libraryName = 'transport-kafka'
libraryFile = 'kafka-transport.jar'
appName = 'Service Bus Kafka Transport Provider'
appFile = 'kafka-transport.ear'

print '***** Service Bus Kafka Transport Install *****'
print ''

############################## Connect and Undeploy ###############################

connect()

appDeployment = cmo.lookupAppDeployment(appName)

if (appDeployment != None):

	stopApplication(appName)
	undeploy(appName)

library = cmo.lookupLibrary(libraryName)

if (library != None):

	undeploy(libraryName)

########################## Retrieve Targets Information ###########################

targets = cmo.getAdminServerName()
clusters = cmo.getClusters()

for cluster in clusters:

	targets = targets + ',' + cluster.getName()

servers = cmo.getServers()

for server in servers:

	if (server.getCluster == None):

		targets = targets + ',' + server.getName()

######################### Install and Start the Transport #########################

if (not os.path.exists(libraryFile)):

	tmp = raw_input('Enter the location of the "kafka-transport.jar" file: ')

	if (tmp.endswith(libraryFile)):

		libraryFile = tmp

	else:

		libraryFile = os.path.join(tmp, libraryFile)

if (not os.path.exists(appFile)):

        tmp = raw_input('Enter the location of the "kafka-transport.ear" file: ')

        if (tmp.endswith(appFile)):

                appFile = tmp

        else:

                appFile = os.path.join(tmp, appFile)

deploy(libraryName, libraryFile, targets=targets, libraryModule='true')
deploy(appName, appFile, targets=targets)

################################## Disconnect ####################################

disconnect()
