import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;
import java.util.Locale;
import java.util.Scanner;

public class AsynchMain {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter the queue name: ");
        String queue = sc.nextLine();
        System.out.print("Please enter client ID: ");
        String clientId = sc.nextLine();
        System.out.print("Please enter the text message: ");
        String message = sc.nextLine();
        System.out.print("Receive messages? (y/ n): ");
        String toReceive = sc.nextLine();
        System.out.print("Please set the priority of the message: ");
        int priority = sc.nextInt();
        System.out.print("Please enter how many messages to send: ");
        int howManyToSend = sc.nextInt();


        boolean receive = false;
        if (toReceive.toLowerCase(Locale.ROOT).equals("y")) {
            receive = true;
        }

        if (args.length > 0){
            clientId = args[0];
        }
        System.out.printf("ClientId %s%n", clientId);
        ActiveMQConnectionFactory connFact = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connFact.setConnectResponseTimeout(10000);
        Connection conn = connFact.createConnection("admin", "admin");
        conn.setClientID(clientId);
        conn.start();




//        if (args.length < 2) {
//            howManyToSend = 5;
//            receive = true;
//        }
        if (args.length >= 2) {
            try {
                howManyToSend = Integer.valueOf(args[1]);
            } catch (NumberFormatException nfe) {
                usageExit("Number of messages to send, not a valid number");
            }
        }
        if (args.length >= 3) {
            receive = ("receive".equalsIgnoreCase(args[2]));
        }

        if (howManyToSend > 0) {
            System.out.printf("Sending %d messages%n", howManyToSend);
            new Thread(new Sender(
                    conn.createSession(false, Session.CLIENT_ACKNOWLEDGE),
                    queue,
                    howManyToSend,
                    message,
                    priority)
            ).start();

        }
        if ( receive) {
            System.out.printf("Receiving messages%n");
            new Thread(new Receiver(conn.createSession(false, Session.CLIENT_ACKNOWLEDGE), queue)).start();
        }
    }

    private static void usageExit(String message) {
        System.out.println(message);
        System.out.println("Argument 1: a number of messages to send, 0 for none, default 5");
        System.out.println("Optional second argument: 'receive' will start a receiver, other values will not, default to receive");

    }

}
