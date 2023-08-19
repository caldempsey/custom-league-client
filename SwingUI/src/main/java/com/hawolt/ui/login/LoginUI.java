package com.hawolt.ui.login;

import com.hawolt.LeagueClientUI;
import com.hawolt.logger.Logger;
import com.hawolt.ui.impl.JHintTextField;
import com.hawolt.util.panel.MainUIComponent;
import com.hawolt.virtual.leagueclient.exception.LeagueException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Created: 06/08/2023 13:10
 * Author: Twitter @hawolt
 **/

public class LoginUI extends MainUIComponent implements ActionListener {
    private final JHintTextField username;
    private final JPasswordField password;
    private final JButton login;
    private final ILoginCallback callback;

    public static void show(LeagueClientUI leagueClientUI) {
        new LoginUI(leagueClientUI);
    }

    private LoginUI(LeagueClientUI clientUI) {
        super(clientUI);
        this.setLayout(new GridLayout(0, 1, 0, 5));
        this.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel usernameLabel = new JLabel("Username");
        this.add(usernameLabel);
        this.add(username = new JHintTextField(""));

        JLabel passwordLabel = new JLabel("Password");
        this.add(passwordLabel);
        this.add(password = new JPasswordField());

        this.add(login = new JButton("Login"));
        login.addActionListener(this);

        this.setPreferredSize(new Dimension(300, 150));
        this.container.add(this);

        // Using .setLabelFor() to bind labels to corresponding input fields
        usernameLabel.setLabelFor(username);
        passwordLabel.setLabelFor(password);

        // Enter hook so users can login with enter
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitLogin();
                }
            }
        };

        username.addKeyListener(enterKeyAdapter);
        password.addKeyListener(enterKeyAdapter);

        this.callback = clientUI;
        this.init();
    }

    private void submitLogin() {
        String user = this.username.getText();
        String pass = new String(this.password.getPassword());
        login.setText("Logging in...");
        login.getModel().setPressed(true);
        login.update(login.getGraphics());

        try {
            callback.onLogin(user, pass);
        } catch (Exception e) {
            // Check for specific message indicating rate limiting
            if (e.getMessage().contains("RATE_LIMITED")) {
                login.setText("You are being rate limited, try again later...");
            } else {
                login.setText("Login");
            }
            login.getModel().setPressed(false);
            login.update(login.getGraphics());
            Logger.error(e);
            // DevNote: Current flow is to recover the League client by calling Logger.fatal. 
            // Removing this line will cause the login flow to re-try its priot state which may-
            // result in you being rate limited.
            Logger.fatal(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == login) {
            submitLogin();
        }
    }
}
