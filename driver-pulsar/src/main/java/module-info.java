module driver.pulsar {
    requires engine.api;
    requires org.apache.logging.log4j;
    requires com.codahale.metrics;
    requires pulsar.client.admin.api;
    requires pulsar.client.api;
    requires adapters.api;
    requires nb.api;
    requires org.apache.commons.lang3;
    requires nb.annotations;
    requires commons.collections;
    requires org.apache.avro;
    requires pulsar.client;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.configuration2;
}
