# Nexus Replication Plugin

[https://github.com/griddynamics/nexus-replication-plugin](https://github.com/griddynamics/nexus-replication-plugin)

This is Nexus plugin that provides replication logic when new (Maven) artifacts are stored in a hosted repository. 
Basically it POST an HTTP requests from master to peers instance when new artifact is saved then peers invoke artifact pooling into proxy repository.

## Setting up plugin

Plugin should be installed into master and peer Nexus servers both.

### Master instance

1. Download source from [https://github.com/griddynamics/nexus-replication-plugin](https://github.com/griddynamics/nexus-replication-plugin).
2. Run `mvm package` in root directory (project requires Apache Maven 3.0.4 -- 3.1 and JDK 1.7+).
3. Unzip the `target/nexus-replication-plugin-1.0-SNAPSHOT-bundle.zip` file into the plugin-repository directory (located in `$NEXUS_HOME/sonatype-work/nexus/plugin-repository`).
4. Download a sample of the configuration file from [https://github.com/griddynamics/nexus-replication-plugin/blob/dev/replication-plugin.xml](https://github.com/griddynamics/nexus-replication-plugin/blob/dev/replication-plugin.xml).
5. Copy the `replication-plugin.xml` file in the conf  directory (located in `$NEXUS_HOME/sonatype-work/nexus/conf`).
6. Edit the config file to suit your needs (read instructions inside the file).
7. Restart Nexus.

### Peer instance

You also should configure each Nexus peer instance. 

1. Reproduce points 1 -- 3 from the previous list.
2. Create proxy repository for the each hosted repository (repositories that should be replicated and located in the master Nexus). 
4. Restart Nexus.

## Plugin REST API

Plugin REST API is available from `service/local/artifact/maven/update`. This resource receives POST request with a body like this:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<artifact-meta-info>
    <groupId>com.griddynamics.cd</groupId>
    <artifactId>nexus-replication-plugin</artifactId>
    <version>1.0-20150519.140619-2</version>
    <repositoryId>snapshots</repositoryId>
    <extension>jar</extension>
    <nexusUrl>http://localhost:8081/nexus</nexusUrl>
</artifact-meta-info>
```

This method returns XML formatted response. If artifact was resolved successfully response will be:

```xml
<rest-status>
	<isSuccess>true</isSuccess>
	<message>Artifact is resolved.</message>
</rest-status>
```

otherwise `isSuccess` will be false and `message` will contain error description.
