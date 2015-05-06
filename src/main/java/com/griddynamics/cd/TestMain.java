package com.griddynamics.cd;

import com.griddynamics.cd.internal.model.config.ReplicationPluginConfiguration;
import com.griddynamics.cd.internal.model.config.NexusServer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class TestMain {
    /**
     * Util method used to generate plugin XML configuration file
     */
    public static void main(String[] args) throws JAXBException {
        ReplicationPluginConfiguration config = new ReplicationPluginConfiguration("http://localhost:8081/nexus");
        config.addServer(new NexusServer("http://localhost:8083/nexus", "admin", "admin123"));

        JAXBContext jaxbContext = JAXBContext.newInstance(ReplicationPluginConfiguration.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(config, System.out);
    }
}
