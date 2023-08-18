package com.hawolt;

import com.hawolt.async.ExecutorManager;
import com.hawolt.async.rms.GameStartListener;
import com.hawolt.client.ClientConfiguration;
import com.hawolt.client.IClientCallback;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.RiotClient;
import com.hawolt.logger.Logger;
import com.hawolt.rms.data.subject.service.MessageService;
import com.hawolt.shutdown.ShutdownHook;
import com.hawolt.ui.MainUI;
import com.hawolt.ui.chat.ChatSidebar;
import com.hawolt.ui.chat.friendlist.ChatSidebarFriendlist;
import com.hawolt.ui.chat.window.ChatWindow;
import com.hawolt.ui.layout.LayoutManager;
import com.hawolt.ui.login.ILoginCallback;
import com.hawolt.ui.login.LoginUI;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.virtual.leagueclient.exception.LeagueException;
import com.hawolt.virtual.leagueclient.userinfo.UserInformation;
import com.hawolt.virtual.riotclient.instance.MultiFactorSupplier;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.EventListener;
import com.hawolt.xmpp.event.EventType;
import com.hawolt.xmpp.event.objects.other.PlainData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 11/08/2023 18:10
 * Author: Twitter @hawolt
 **/

public class LeagueClientUI extends JFrame implements IClientCallback, ILoginCallback, WindowStateListener {
    public static ExecutorService service = ExecutorManager.registerService("pool", Executors.newCachedThreadPool());

    private LeagueClient leagueClient;
    private RiotClient riotClient;

    public LeagueClientUI(String title) {
        super(title);
        this.addWindowStateListener(this);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private ChatSidebar chatSidebar;
    private MainUI mainUI;

    @Override
    public void onClient(LeagueClient client) {
        client.getRMSClient().getHandler().addMessageServiceListener(MessageService.GSM, new GameStartListener(client.getPlayerPlatform()));
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(client)));
        buildUI(leagueClient = client);
    }

    private void buildUI(LeagueClient client) {
        VirtualRiotXMPPClient xmppClient = client.getXMPPClient();
        mainUI = new MainUI(this);
        ChildUIComponent temporary = new ChildUIComponent(new BorderLayout());
        ChatWindow chatWindow = new ChatWindow();
        chatWindow.setSupplier(xmppClient);
        chatWindow.setVisible(false);
        mainUI.addChatComponent(chatWindow);
        UserInformation userInformation = client.getVirtualLeagueClient()
                .getVirtualLeagueClientInstance()
                .getUserInformation();
        chatSidebar = new ChatSidebar(userInformation, chatWindow);
        LayoutManager manager = new LayoutManager(this);
        manager.setBackground(Color.MAGENTA);
        temporary.add(manager, BorderLayout.CENTER);
        temporary.add(chatSidebar, BorderLayout.EAST);
        chatSidebar.configure(userInformation);
        if (leagueClient.getXMPP().getTimestamp() > 0) {
            buildSidebarUI(xmppClient, chatWindow);
        } else {
            xmppClient.addHandler(
                    EventType.ON_READY,
                    (EventListener<PlainData>) event -> buildSidebarUI(xmppClient, chatWindow)
            );
        }
        mainUI.setMainComponent(temporary);
        mainUI.revalidate();
    }

    private void buildSidebarUI(VirtualRiotXMPPClient xmppClient, ChatWindow chatWindow) {
        chatSidebar.getProfile().getSummoner().getStatus().setXMPPClient(xmppClient);
        ChatSidebarFriendlist friendlist = chatSidebar.getChatSidebarFriendlist();
        friendlist.onEvent(xmppClient.getFriendList());
        xmppClient.addPresenceListener(friendlist);
        xmppClient.addFriendListener(friendlist);
        xmppClient.addMessageListener(chatWindow);
        friendlist.revalidate();
    }

    public ChatSidebar getChatSidebar() {
        return chatSidebar;
    }

    public RiotClient getRiotClient() {
        return riotClient;
    }

    public LeagueClient getLeagueClient() {
        return leagueClient;
    }

    @Override
    public void onError(Throwable throwable) {
        Logger.fatal(throwable);
        Logger.error("Failed to initialize Client");
    }

    public static void main(String[] args) {
        LeagueClientUI leagueClientUI = new LeagueClientUI("Swift Rift");
        leagueClientUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        leagueClientUI.setVisible(true);
        LoginUI.show(leagueClientUI);
    }

    @Override
    public void onLogin(String username, String password) throws LeagueException, IOException {
        JFrame parent = this;
        ClientConfiguration configuration = ClientConfiguration.getDefault(username, password, new MultiFactorSupplier() {
            @Override
            public String get() {
                return (String) JOptionPane.showInputDialog(
                        parent,
                        "Enter 2FA Code",
                        "Multifactor",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "");
            }
        });
        this.riotClient = new RiotClient(configuration, this);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == JFrame.MAXIMIZED_BOTH) {
            mainUI.adjust();
        }
    }
}
