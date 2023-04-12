package io.nosqlbench.adapter.s4r.util;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class S4RMessageHandler extends DefaultConsumer {
    private final static Logger logger = LogManager.getLogger(S4RMessageHandler.class);
    public S4RMessageHandler(Channel channel) {
        super(channel);
        //TODO Auto-generated constructor stub
    }
    @Override
    public void handleDelivery(
        String consumerTag,
        Envelope envelope,
        AMQP.BasicProperties properties,
        byte[] body) throws IOException {

    String message = new String(body, "UTF-8");
    logger.info("RabbitMQ S4R Message Consumed: " + message);
    }

}
