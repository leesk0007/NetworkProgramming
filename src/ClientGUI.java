import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame {

    // ===============================
    //  서버 연결 정보 및 화면 컴포넌트로 사용하는 것들
    // ===============================

    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_LOBBY = "LOBBY";
    public static final String SCREEN_ROOM = "ROOM";

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static final String LOBBY_ROOM_ID = "0";

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private String myNickname = "";

    private ClientSocket clientSocket;
    private boolean isConnected = false;

    // 대기실 컴포넌트
    private JLabel lobbyUserLabel;
    private DefaultTableModel roomTableModel;
    private JTable roomTable;
    private JTextArea lobbyChatArea;
    private JTextField lobbyChatInput;

    // ===============================
    //  프레임, 소켓 연결 준비
    // ===============================
    public ClientGUI() {
        clientSocket = new ClientSocket(this, SERVER_IP, SERVER_PORT);

        setTitle("초성 퀴즈 온라인 - ClientGUI");
        setSize(800, 650);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clientSocket.disconnect();
            }
        });

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        add(mainContainer);

        mainContainer.add(createLoginPanel(), SCREEN_LOGIN);
        mainContainer.add(createLobbyPanel(), SCREEN_LOBBY);
        mainContainer.add(createRoomPanel(), SCREEN_ROOM);

        showScreen(SCREEN_LOGIN);

        setVisible(true);
    }

    // ===============================
    // 로그인
    // ===============================
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(220, 220, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        // 타이틀 초성퀴즈
        JLabel titleLabel = new JLabel("초성 퀴즈", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 48));
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 10, 60, 10);
        panel.add(titleLabel, gbc);

        // 닉네임 입력 행
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        inputPanel.setOpaque(false);

        JLabel nicknameLabel = new JLabel("닉네임");
        nicknameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));

        JTextField nicknameField = new JTextField(20);
        nicknameField.setPreferredSize(new Dimension(220, 30));
        nicknameField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        inputPanel.add(nicknameLabel);
        inputPanel.add(nicknameField);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 25, 10);
        panel.add(inputPanel, gbc);

        // 접속/나가기 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton loginBtn = new JButton("접속");
        JButton exitBtn = new JButton("나가기");

        loginBtn.setPreferredSize(new Dimension(80, 32));
        exitBtn.setPreferredSize(new Dimension(80, 32));

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText().trim();

                if (nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "닉네임을 입력해주세요.");
                    return;
                }
                if (Protocol.hasReservedDelims(nickname)) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "닉네임에 |, ;, , 문자는 사용할 수 없습니다.");
                    return;
                }

                if (!isConnected) {
                    isConnected = clientSocket.connect();
                    if (!isConnected) {
                        JOptionPane.showMessageDialog(ClientGUI.this, "서버에 연결할 수 없습니다.");
                        return;
                    }
                }

                myNickname = nickname;
                clientSocket.sendMessage(Protocol.build(Protocol.SET_NAME, nickname));
            }
        });

        // 엔터 키 입력 로그인되도록
        nicknameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginBtn.doClick();
            }
        });

        exitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(loginBtn);
        buttonPanel.add(exitBtn);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 10, 30, 10);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    // ===============================
    // 대기실 화면 (방 목록 / 방 만들기 / 채팅) 기능 있음
    // ===============================
    private JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(new Color(230, 230, 230));

        //  새로고침, 방 만들기 버튼
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        lobbyUserLabel = new JLabel("로비");
        lobbyUserLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topButtonPanel.setOpaque(false);

        JButton refreshBtn = new JButton("새로고침");
        JButton createRoomBtn = new JButton("방 만들기");

        topButtonPanel.add(refreshBtn);
        topButtonPanel.add(createRoomBtn);

        topPanel.add(lobbyUserLabel, BorderLayout.WEST);
        topPanel.add(topButtonPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // 방목록 + 채팅창 들어갈 영역
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // 방 목록
        JPanel roomSectionPanel = new JPanel(new BorderLayout(0, 5));
        roomSectionPanel.setOpaque(false);

        JLabel roomListTitle = new JLabel("방 목록");
        roomListTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        roomSectionPanel.add(roomListTitle, BorderLayout.NORTH);

        String[] columnNames = {"방 번호", "제목", "인원", "상태"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(roomTableModel);
        roomTable.setRowHeight(26);
        roomTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getTableHeader().setReorderingAllowed(false);

        // 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScrollPane = new JScrollPane(roomTable);
        tableScrollPane.setPreferredSize(new Dimension(740, 220));
        roomSectionPanel.add(tableScrollPane, BorderLayout.CENTER);

        centerContainer.add(roomSectionPanel);
        centerContainer.add(Box.createVerticalStrut(15));

        // 채팅창
        JPanel chatSectionPanel = new JPanel(new BorderLayout(0, 5));
        chatSectionPanel.setOpaque(false);

        JLabel chatTitle = new JLabel("채팅");
        chatTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        chatSectionPanel.add(chatTitle, BorderLayout.NORTH);

        lobbyChatArea = new JTextArea(7, 40);
        lobbyChatArea.setEditable(false);
        lobbyChatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        lobbyChatArea.setLineWrap(true);

        JScrollPane chatScrollPane = new JScrollPane(lobbyChatArea);
        chatSectionPanel.add(chatScrollPane, BorderLayout.CENTER);

        lobbyChatInput = new JTextField();
        lobbyChatInput.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        lobbyChatInput.setPreferredSize(new Dimension(0, 28));

        chatSectionPanel.add(lobbyChatInput, BorderLayout.SOUTH);

        centerContainer.add(chatSectionPanel);

        panel.add(centerContainer, BorderLayout.CENTER);

        // ===============================
        // 대기실에서 사용하는 액션 모음
        // 채팅 전송 / 새로고침 / 방 입장 / 방 만들기
        // ===============================

        // 로비에서 채팅 입력하면 서버로 전송 (CHAT|방번호|내용)
        lobbyChatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = lobbyChatInput.getText().trim();
                if (message.isEmpty()) {
                    return;
                }
                if (Protocol.hasReservedDelims(message)) {
                    JOptionPane.showMessageDialog(ClientGUI.this, "채팅에 |, ;, , 문자는 사용할 수 없습니다.");
                    return;
                }
                clientSocket.sendMessage(Protocol.build(Protocol.CHAT, LOBBY_ROOM_ID, message));
                lobbyChatInput.setText("");
            }
        });

        // 새로고침 버튼 누르면 방 목록 요청 전송 (REQ_ROOM_LIST)
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientSocket.sendMessage(Protocol.REQ_ROOM_LIST);
            }
        });

        // 방 목록 더블 클릭 하면 방 입장 요청 전송 (ENTER_ROOM|방번호)
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = roomTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String roomId = String.valueOf(roomTableModel.getValueAt(selectedRow, 0));
                        clientSocket.sendMessage(Protocol.build(Protocol.ENTER_ROOM, roomId));
                    }
                }
            }
        });

        // 방 만들기 버튼 누르면 방 생성 모달 열기
        createRoomBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateRoomDialog();
            }
        });

        return panel;
    }

    // ===============================
    // 방 만들기 모달 창 (방제, 비밀번호, 방 생성 - 총 3개)
    // ===============================
    private void openCreateRoomDialog() {
        JDialog dialog = new JDialog(this, "방 만들기", true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(new Color(220, 220, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("방 제목");
        JTextField titleField = new JTextField(15);
        JLabel pwLabel = new JLabel("비밀번호");
        JPasswordField pwField = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(titleLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        dialog.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(pwLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        dialog.add(pwField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        JButton createBtn = new JButton("생성");
        JButton cancelBtn = new JButton("취소");

        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomTitle = titleField.getText().trim();
                if (roomTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "방 제목을 입력해주세요.");
                    return;
                }
                if (Protocol.hasReservedDelims(roomTitle)) {
                    JOptionPane.showMessageDialog(dialog, "방 제목에 구분자(|, ;, ,)를 사용할 수 없습니다.");
                    return;
                }
                dialog.dispose();
                // 나중에 프로토콜에 비밀번호 추가하기
                clientSocket.sendMessage(Protocol.build(Protocol.CREATE_ROOM, roomTitle));
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 10, 10);
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    // ===============================
    // 게임방 화면 임시로 파랗게
    // ===============================
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

    // ===============================
    // 화면 전환, 서버 응답 처리 (콜백)
    // ===============================
    public void showScreen(String screenName) {
        cardLayout.show(mainContainer, screenName);
    }

    // 소켓 통신 응답 처리
    public void onLoginSuccess() {
        lobbyUserLabel.setText("로비 (" + myNickname + ")");
        showScreen(SCREEN_LOBBY);
        clientSocket.sendMessage(Protocol.REQ_ROOM_LIST);
    }

    public void onLoginFail(String reason) {
        JOptionPane.showMessageDialog(this, reason);
    }

    public void updateRoomList(String roomData) {
        roomTableModel.setRowCount(0);
        if (roomData == null || roomData.trim().isEmpty()) {
            return;
        }

        String[] rooms = roomData.split(Protocol.LIST_DELIM);
        for (String room : rooms) {
            String[] info = room.split(Protocol.ITEM_DELIM);
            if (info.length >= 4) {
                roomTableModel.addRow(new Object[]{info[0], info[1], info[2], info[3]});
            } else if (info.length >= 3) {
                roomTableModel.addRow(new Object[]{info[0], info[1], info[2], "대기중"});
            }
        }
    }

    public void appendChatMessage(String sender, String message) {
        lobbyChatArea.append(sender + " : " + message + "\n");
        lobbyChatArea.setCaretPosition(lobbyChatArea.getDocument().getLength());
    }

    public void onEnterRoomSuccess(String roomId, String roomTitle) {
        showScreen(SCREEN_ROOM);
    }

    public void onRoomFail(String reason) {
        JOptionPane.showMessageDialog(this, reason);
    }

    // ===============================
    // 프로그램 시작 메인
    // ===============================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }
}
