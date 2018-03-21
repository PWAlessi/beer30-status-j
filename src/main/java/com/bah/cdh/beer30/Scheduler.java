package com.bah.cdh.beer30;

import com.bah.cdh.beer30.dtos.Beer30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final Logger log = LoggerFactory.getLogger(javafx.application.Application.class);

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

        RestTemplate restTemplate = new RestTemplate();

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            Beer30 beer30 = restTemplate.getForObject("https://beer30.charlestondigitalhub.com/lights/beer30.json", Beer30.class);
            setTrayIconFromBeer30State(trayIcon, beer30);
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
            defaultItem.addActionListener((ActionEvent) -> {
                log.info("quitting");
                System.exit(0);
            });
            popup.add(defaultItem);

            trayIcon = new TrayIcon(image, "Beer 30", popup);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        } else {
            log.error("SystemTray is not supported");
            System.exit(3);
        }

        return trayIcon;
    }

    private void setTrayIconFromBeer30State(TrayIcon trayIcon, Beer30 beer30) {
        Image image;

        switch (beer30.getState()){
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
        trayIcon.setToolTip(beer30.getText());
    }

}
