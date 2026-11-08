import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame {

    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_LOBBY = "LOBBY";
    public static final String SCREEN_ROOM = "ROOM";

    private CardLayout cardLayout;
    private JPanel mainContainer;

    public ClientGUI() {
        setTitle("초성 퀴즈 게임");
        setSize(800, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        add(mainContainer);

        mainContainer.add(createLoginPanel(), SCREEN_LOGIN);
        mainContainer.add(createLobbyPanel(), SCREEN_LOBBY);
        mainContainer.add(createRoomPanel(), SCREEN_ROOM);

        showScreen(SCREEN_LOGIN);

        setVisible(true);
    }

    private JPanel createLoginPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);

        JLabel label = new JLabel("닉네임:");
        JTextField nicknameField = new JTextField(15);
        JButton loginBtn = new JButton("접속하기");

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText().trim();

                if (nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "닉네임을 입력해주세요.");
                    return;
                }
                showScreen(SCREEN_LOBBY);
            }
        });

        formPanel.add(label);
        formPanel.add(nicknameField);
        formPanel.add(loginBtn);

        panel.add(formPanel);

        return panel;
    }

    private JPanel createLobbyPanel() {

        JPanel panel = new JPanel();
        panel.setBackground(Color.PINK);

        JLabel label = new JLabel("대기실(로비) 화면입니다.");
        JButton enterRoomBtn = new JButton("방 입장");

        enterRoomBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showScreen(SCREEN_ROOM);
            }
        });

        panel.add(label);

        panel.add(enterRoomBtn);
        return panel;
    }

    private JPanel createRoomPanel() {

        JPanel panel = new JPanel();
        panel.setBackground(Color.CYAN);

        JLabel label = new JLabel("게임방 화면입니다.");
        JButton leaveBtn = new JButton("방 나가기");

        leaveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showScreen(SCREEN_LOBBY);
            }
        });

        panel.add(label);
        panel.add(leaveBtn);

        return panel;
    }

    public void showScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }
}
