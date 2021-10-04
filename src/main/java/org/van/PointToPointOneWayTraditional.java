package org.van;

/*
                        not working on this file
 */


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;

import javax.jms.*;
import java.util.UUID;

public class PointToPointOneWayTraditional {

    public static void main(String... args) throws Exception {
        ActiveMQConnectionFactory connFact = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connFact.setConnectResponseTimeout(10000);
        Connection conn = connFact.createConnection("user", "password");
        conn.setClientID("Audit");
        conn.start();

        new Thread(new Receiver(conn.createSession(false, Session.CLIENT_ACKNOWLEDGE), "Audit")).start();
        new Thread(new Sender(conn.createSession(false, Session.CLIENT_ACKNOWLEDGE), "Audit")).start();
    }

    public static class Sender implements Runnable {

        private Session session;
        private String destination;

        public Sender(Session session, String destination) {
            this.session = session;
            this.destination = destination;
        }

        public void run() {
            try {
                MessageProducer messageProducer = session.createProducer(session.createQueue(destination));
                long counter = 0;

                while (true) {
                    TextMessage message = session.createTextMessage("audit " + ++counter);
                    message.setJMSMessageID(UUID.randomUUID().toString());
                    messageProducer.send(message);
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Receiver implements Runnable, MessageListener {

        private Session session;
        private String destination;

        public Receiver(Session session, String destination) {
            this.session = session;
            this.destination = destination;
        }

        public void run() {
            try {
                MessageConsumer consumer = session.createConsumer(session.createQueue(destination));
                consumer.setMessageListener(this);
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        public void onMessage(Message message) {
            try {
                System.out.println(String.format("received message '%s' with message id '%s'", ((TextMessage) message).getText(), message.getJMSMessageID()));
                message.acknowledge();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
