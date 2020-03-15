package glavni;

import java.io.*;
import java.net.Socket;

public class Kviz {
    private static int idUcesnika = 0;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private static final int TCP_IP = 9000;

    public Kviz() throws IOException {
        poveziNaServer();
    }
    public void startujKviz() throws IOException {
        out.println("Z#TRECI");
    }

    public String dodajUcesnika() throws IOException {
        poveziNaServer();
        int broj = (int) (Math.random() * 1000);
        out.println("id_" + (++idUcesnika) + "#" + broj);
        return in.readLine();
    }
    private void poveziNaServer() throws IOException {
        socket = new Socket("127.0.0.1", TCP_IP);
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
}
