import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Node {

    public class RemoteNode {
        public String ip;
        public int port;
    }

    public class NetworkListener extends Thread {
        Node node = null;
        public NetworkListener(Node n) { node = n; }
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(node.port);
                byte[] receive = new byte[65535];
                DatagramPacket packet = null;
                while(true)
                {
                    packet = new DatagramPacket(receive, receive.length);
                    socket.receive(packet);
                    String data = ByteToString(receive);
                    String remoteip = packet.getAddress().toString().substring(1);
                    node.receive(data, remoteip);
                    receive = new byte[65535];
                }
            } catch(Exception e) { e.printStackTrace(); }
        }

        public String ByteToString(byte[] a)
        {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret.toString();
        }
    }

    public class KeyboardListener extends Thread {
        Node node = null;
        public KeyboardListener(Node n) { node = n; }
        public void run() {
            Scanner sc = new Scanner(System.in);
            while(true)
            {
                String line = sc.nextLine();
                node.keyboard(line);
            }
        }
    }

    public NetworkListener nlistener = null;
    public KeyboardListener klistener = null;
    public int port = 5000;
    public List<RemoteNode> nodes = new ArrayList<RemoteNode>();

    public static void main(String[] args)
    {
        int tport = 5000;
        if (args.length>0) tport = Integer.parseInt(args[0]);
        System.out.println("Starting on port "+tport);
        Node n = new Node();
        n.port = tport;
        n.go();
    }

    public void go()
    {
        klistener = new KeyboardListener(this);
        klistener.start();
        nlistener = new NetworkListener(this);
        nlistener.start();
    }

    public void send(String msg, String ip, int remoteport) {
        try {
            byte[] bytes = msg.getBytes();
            InetAddress inet = InetAddress.getByName(ip);
            DatagramSocket ds = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, inet, remoteport);
            ds.send(packet);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void keyboard(String line) // keyboard line entered
    {
        System.out.println("Keyboard: "+line);
        String[] cmd = line.split(" ");
        if (cmd[0].equalsIgnoreCase("quit"))
            System.exit(0);
        else if(cmd[0].equalsIgnoreCase("ping"))
        {
            if(cmd.length != 3) System.out.println("ping IP PORT");
            else {
                String remoteip = cmd[1];
                int remoteport = Integer.parseInt(cmd[2]);
                System.out.println("Sending a PING to "+remoteip+":"+remoteport);
                send("PING "+port,remoteip,remoteport);
            }
        }
        else if (cmd[0].equalsIgnoreCase("add")) {
            RemoteNode rn = new RemoteNode();
            rn.ip = cmd[1];
            rn.port = Integer.parseInt(cmd[2]);
            nodes.add(rn);
        }
        else if (cmd[0].equalsIgnoreCase("list")) {
            for(RemoteNode n: nodes)
            {
                System.out.println(n.ip+":"+n.port);
            }
        }
        else if (cmd[0].equalsIgnoreCase("broadcast")) {
            for(RemoteNode n: nodes)
            {
                String remoteip = n.ip;
                int remoteport = n.port;
                System.out.println("Sending a PING to "+remoteip+":"+remoteport);
                send("PING "+port,remoteip,remoteport);
            }
        }
    }

    public void receive(String line, String remoteip) // received data
    {
        System.out.println("Rx "+line+" from "+remoteip);
        String[] parts = line.split(" ");
        if (parts[0].equalsIgnoreCase("PING")) {
            int remoteport = Integer.parseInt(parts[1]);
            send("PONG",remoteip,remoteport);
        }
    }

}
