package com.bah.cdh.beer30;

import com.bah.cdh.beer30.dtos.Beer30;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    public void run() {

        TrayIcon trayIcon = this.createTray();

        try {
            SSLUtil.turnOffSslChecking();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);

        } catch (KeyManagementException e) {
            e.printStackTrace();
            System.exit(2);
        }


        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            String responseMessage = null;

            try {
                URL url = new URL("https://beer30.charlestondigitalhub.com/lights/beer30.json");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();
                responseMessage = content.toString();

            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                Beer30 beer30 = objectMapper.readValue(responseMessage, Beer30.class);
                setTrayIconFromBeer30State(trayIcon, beer30);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    private TrayIcon createTray() {
        // Add -Dapple.awt.UIElement=true to JVM options to hide the Java icon in the Dock
        TrayIcon trayIcon = null;
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Red_pog.png"));

            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Quit");
            defaultItem.addActionListener((ActionEvent) -> System.exit(0));
            popup.add(defaultItem);

            trayIcon = new TrayIcon(image, "Beer 30", popup);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        } else {
            System.exit(3);
        }

        return trayIcon;
    }

    private void setTrayIconFromBeer30State(TrayIcon trayIcon, Beer30 beer30) {
        Image image;

        switch (beer30.getState()) {
            case "red":
                image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Red_pog.png"));
                break;
            case "yellow":
                image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Yellow_pog.png"));
                break;
            case "green":
                image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Green_pog.png"));
                break;
            default:
                image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/Red_pog.png"));
                break;
        }
        trayIcon.setImage(image);
        trayIcon.setToolTip(beer30.getText() + "\n" + beer30.getUpdated_at());
    }

}
