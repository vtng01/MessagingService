package org.van;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.UUID;

import static javax.jms.DeliveryMode.NON_PERSISTENT;


public  class Sender implements Runnable {

    private Session session;
    private String destination;
    private int howManyToSend;
    private String message;
    private int priority;

    public Sender(Session initSession, String initDestination, int initHowManyToSend, String initMessage, int initPriority) {
        this.session = initSession;
        this.destination = initDestination;
        this.howManyToSend = initHowManyToSend;
        this.message = initMessage;
        this.priority = initPriority;
    }

    public Sender(Session initSession, String initDestination) {
        this(initSession, initDestination, 5, "Hello World", 1);
    }

    public void run() {
        try {
            MessageProducer messageProducer = session.createProducer(session.createQueue(destination));
//            messageProducer.setDeliveryMode(NON_PERSISTENT);
            long counter = 0;

            while (counter < howManyToSend) {
                TextMessage message = session.createTextMessage("Message " + this.message);
                message.setJMSMessageID(UUID.randomUUID().toString());
//                message.setJMSReplyTo(session.createQueue(this.destination));
                messageProducer.setPriority(this.priority);
                messageProducer.send(message);
                System.out.printf("Sent %d: %s%n", counter, message);
                counter++;
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
