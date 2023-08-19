package com.hawolt.ui.login;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.impl.JHintTextField;
import com.hawolt.util.panel.MainUIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created: 06/08/2023 13:10
 * Author: Twitter @hawolt
 **/

public class LoginUI extends MainUIComponent implements ActionListener {
    private final JHintTextField username;
    private final JPasswordField password;
    private final ILoginCallback callback;
    private final JButton button;

    public static LoginUI show(LeagueClientUI leagueClientUI) {
        return new LoginUI(leagueClientUI);
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

        button = new JButton("Login");
        button.addActionListener(this);
        this.add(button);

        this.setPreferredSize(new Dimension(300, 150));
        this.container.add(this);

        // Using .setLabelFor() to bind labels to corresponding input fields
        usernameLabel.setLabelFor(username);
        passwordLabel.setLabelFor(password);

        // Enter hook so users can login with enter
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
                actionPerformed(null);
            }
        };

        username.addKeyListener(enterKeyAdapter);
        password.addKeyListener(enterKeyAdapter);

        this.callback = clientUI;
        this.init();
    }

    public void toggle(boolean state) {
        username.setEnabled(state);
        password.setEnabled(state);
        button.setEnabled(state);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        toggle(false);
        String username = this.username.getText();
        String password = new String(this.password.getPassword());
        LeagueClientUI.service.execute(() -> {
            callback.onLogin(username, password);
        });
    }
}
