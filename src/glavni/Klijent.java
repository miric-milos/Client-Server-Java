package glavni;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Klijent extends Application {

    private static final int TCP_IP = 9000;
    private Label prviOdgovor;
    private TextArea area;
    private Kviz kviz;
    private static boolean kvizTraje = false;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Prozor klijenta");
        stage.setHeight(500);
        stage.setWidth(500);

        Parent rootNode = new GridPane();
        dodajAnchorPane(rootNode);
        Scene scena = new Scene(rootNode, 400, 400);
        stage.setScene(scena);
        stage.show();
    }

    private void dodajAnchorPane(Parent korenskiCvor) {
        AnchorPane ap = new AnchorPane();
        ap.setPrefSize(500, 500);
        Label prviZadatakNaslov = new Label("PROVERA PALINDROMA\nUnesite rec za proveru");
        AnchorPane.setTopAnchor(prviZadatakNaslov, 30.0);
        AnchorPane.setLeftAnchor(prviZadatakNaslov, 50.0);

        TextField rec = new TextField();
        AnchorPane.setLeftAnchor(rec, 50.0);
        AnchorPane.setTopAnchor(rec, 80.0);

        Button btn = new Button("Da li je rec palindrom?");
        AnchorPane.setLeftAnchor(btn, 50.0);
        AnchorPane.setTopAnchor(btn, 120.0);

        btn.setOnAction(e -> {
            try {
                btnPalindrom_Click(rec.getText());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        prviOdgovor = new Label("Odgovor: ");
        AnchorPane.setTopAnchor(prviOdgovor, 160.0);
        AnchorPane.setLeftAnchor(prviOdgovor, 50.0);

        Button btnFajl = new Button("Zahtevaj fajl od servera");
        AnchorPane.setLeftAnchor(btnFajl, 300.0);
        AnchorPane.setTopAnchor(btnFajl, 80.0);
        btnFajl.setOnAction(e -> {
            try {
                zahtevajFajl();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        area = new TextArea();
        area.setEditable(false);
        AnchorPane.setBottomAnchor(area, 10.0);
        AnchorPane.setLeftAnchor(area, 10.0);
        AnchorPane.setRightAnchor(area, 10.0);

        Button btnKviz = new Button("Zapocni kviz");
        AnchorPane.setLeftAnchor(btnKviz, 50.0);
        AnchorPane.setTopAnchor(btnKviz, 200.0);
        btnKviz.setOnAction(e -> {
            try {
                zapocniKviz();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        Button btnPosalji = new Button("Dodaj ucesnika");
        AnchorPane.setLeftAnchor(btnPosalji, 50.0);
        AnchorPane.setTopAnchor(btnPosalji, 230.0);

        btnPosalji.setOnAction(e -> {
            try {
                dodajUcesnika();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        ObservableList<Node> sviElementi = ap.getChildren();
        sviElementi.addAll(btn, prviZadatakNaslov, prviOdgovor, rec, btnFajl, area, btnKviz, btnPosalji);
        ((GridPane) korenskiCvor).add(ap, 0, 0);
    }

    private void dodajUcesnika() throws IOException {
        if (!kvizTraje) {
            area.appendText("Kviz nije u toku!\n");
            return;
        }
        String odgovor = kviz.dodajUcesnika();
        if(odgovor.equalsIgnoreCase("end")) {
            area.appendText("Kraj kviza!\n");
            kvizTraje = false;
        } else {
            area.appendText(odgovor + "\n");
        }
    }

    private void zapocniKviz() throws IOException {
        if (kvizTraje) {
            area.appendText("Kviz je idalje u toku!\n");
            return;
        }
        kviz = new Kviz();
        kvizTraje = true;
        kviz.startujKviz();
    }

    private void zahtevajFajl() throws IOException {

        area.appendText("Zahtev za fajl poslat\n");

        Socket socket = new Socket("127.0.0.1", TCP_IP);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        out.println("Z#DRUGI");
        byte[] mybytearray = new byte[1000000];
        InputStream is = socket.getInputStream();
        FileOutputStream fos = new FileOutputStream("zaklijenta.txt");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int bytesRead = is.read(mybytearray, 0, mybytearray.length);
        int current = bytesRead;
        do {
            bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
            if (bytesRead > 0) current += bytesRead;
        } while (bytesRead > -1);
        bos.write(mybytearray, 0, current);
        area.appendText("Fajl primljen\n\tVelicina: " + current + " bytes\n");
        bos.flush();
        fos.close();
        bos.close();
        out.close();
        is.close();
        socket.close();
    }

    private void btnPalindrom_Click(String rec) throws IOException {
        if (rec.equals("")) {
            prviOdgovor.setText("Odgovor: POLJE PRAZNO!");
            return;
        }
        Socket socket = new Socket("127.0.0.1", TCP_IP);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        out.println(rec + "#" + Zadatak.PRVI);

        String odgovor = in.readLine();

        prviOdgovor.setText("Odgovor: " + odgovor);
        in.close();
        out.close();
        socket.close();
    }
}