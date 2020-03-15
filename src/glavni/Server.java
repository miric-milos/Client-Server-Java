package glavni;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server extends Application implements Runnable {
    private static final int TCP_IP = 9000;
    private ServerSocket ss;
    //Kontrole
    private Stage mainStage;
    private TextArea area;
    private TextField txtImeFajla;
    private static boolean kvizTraje;

    @Override
    public void start(Stage stage) throws Exception {
        Thread serverNit = new Thread(this);
        serverNit.setDaemon(true);
        serverNit.start();
        mainStage = stage;
        stage.setTitle("Prozor servera");
        stage.setHeight(500);
        stage.setWidth(500);

        Parent rootNode = new GridPane();
        dodajAnchorPane(rootNode);
        Scene scena = new Scene(rootNode, 400, 400);
        stage.setScene(scena);
        stage.show();
    }

    private void dodajAnchorPane(Parent rootNode) {
        AnchorPane ap = new AnchorPane();
        ap.setPrefSize(500, 500);
        area = new TextArea();
        area.setEditable(false);
        AnchorPane.setBottomAnchor(area, 10.0);
        AnchorPane.setLeftAnchor(area, 10.0);
        AnchorPane.setRightAnchor(area, 10.0);

        ObservableList<Node> sviElementi = ap.getChildren();
        sviElementi.addAll(area);
        ((GridPane) rootNode).add(ap, 0, 0);
    }


    private void posaljiFajlKlijentu(Socket socket) throws IOException {
        File myFile = new File("../OP2_Domaci/src/DATA_SERVER/zaklijenta.txt");
        byte[] mybytearray = new byte[(int) myFile.length()];
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        OutputStream os = socket.getOutputStream();
        bis.read(mybytearray, 0, mybytearray.length);
        area.appendText("Slanje fajla zapoceto...\n");
        os.write(mybytearray, 0, mybytearray.length);
        os.flush();
        area.appendText("Fajl poslat klijentu.\n\n");
        fis.close();
        bis.close();
        os.close();
        out.close();
        socket.close();
    }

    private void pokreniServer() throws IOException {
        ss = new ServerSocket(TCP_IP);
        System.out.println("Server pokrenut...");

        while (true) {
            Socket socket = ss.accept();
            area.appendText("Klijent prihvacen\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            String[] niz = in.readLine().split("#");
            if (niz[niz.length - 1].equalsIgnoreCase("PRVI")) {
                area.appendText("Klijent trazi palindrom.\n");
                if (jePalindrom(niz[0])) {
                    out.println("REC JESTE PALINDROM");
                } else {
                    out.println("REC NIJE PALINDROM");
                }
                area.appendText("Provera palindrom zavrsena.\n\n");
                in.close();
                out.close();
                socket.close();
            } else if(niz[niz.length - 1].equalsIgnoreCase("DRUGI")) {
                area.appendText("Klijent trazi fajl\n");
                posaljiFajlKlijentu(socket);
            } else if (niz[niz.length - 1].equalsIgnoreCase("TRECI")) {
                area.appendText("Klijent startovao kviz\n");
                startujKviz(socket);
            }
        }
    }

    private void startujKviz(Socket socket) throws IOException {
        kvizTraje = true;
        ArrayList<Integer> brojevi = new ArrayList<>();
        zapocniOdbrojavanje();
        while(true) {
            socket = ss.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            String[] ucesnik = in.readLine().split("#");
            if(!kvizTraje) {
                area.appendText("Kviz je gotov!\n");
                out.println("end");
                return;
            }

            String id = ucesnik[0];
            int broj = Integer.parseInt(ucesnik[1]);
            brojevi.add(broj);
            if(jeNajmanji(brojevi, broj)) {
                out.println("Vas broj jeste najmanji i jednistven");
            } else {
                out.println("Vas broj nije najmanji i jedinstven");
            }
            area.appendText("Ucesnik dodat, ID: " + id + "\n");

            socket.close();
            in.close();
            out.close();
        }
    }

    private void zapocniOdbrojavanje() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 5;i>=0;--i) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                kvizTraje = false;
            }
        }).start();
    }

    private boolean jeNajmanji(ArrayList<Integer> brojevi, int broj) {
        int min = brojevi.get(0);
        for(int i = 1;i<brojevi.size();++i) {
            if(brojevi.get(i) < min) min = brojevi.get(i);
        }
        return min == broj;
    }


    @Override
    public void run() {
        try {
            pokreniServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        launch(args);
    }
    private static boolean jePalindrom(String rec) {
        rec = trim(rec);
        StringBuilder recObrnuto = new StringBuilder();
        for (int i = rec.length() - 1; i >= 0; --i) {
            recObrnuto.append(rec.charAt(i));
        }
        return rec.equalsIgnoreCase(recObrnuto.toString());
    }
    private static String trim(String rec) {
        StringBuilder rezultat = new StringBuilder();
        for (int i = 0; i < rec.length(); ++i) {
            if (rec.charAt(i) == ' ' || rec.charAt(i) == '\n' || rec.charAt(i) == '\t') {
                continue;
            }
            rezultat.append(rec.charAt(i));
        }
        return rezultat.toString();
    }
}