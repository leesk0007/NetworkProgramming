import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    private String serverIp;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientGUI gui;
    private boolean isRunning = false;

    // gui 객체랑 서버 정보를 받아서 init 하기
    public ClientSocket(ClientGUI gui, String serverIp, int serverPort) {
        this.gui = gui;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    // 서버랑 소켓 연결 시도
    public boolean connect() {
        try {
            socket = new Socket(serverIp, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버 메시지를 계속 받을 수 있는 백그라운드 스레드
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    listen();
                }
            });
            thread.start();

            return true;
        } catch (Exception e) {
            System.err.println("[클라이언트 소켓] 서버 연결 실패: " + e.getMessage());
            return false;
        }
    }

    // 서버로 메시지 한 라인 전송
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // 서버 메세지 받기 위한 대기 루프
    private void listen() {
        try {
            String line;
            while (isRunning && (line =
                    in.readLine()) != null) {
                System.out.println("[서버 수신] " + line);
                handleServerMessage(line);
            }
        } catch (Exception e) {
            if (isRunning) {
                System.err.println("[클라이언트 소켓] 연결 끊김: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    // 서버로 부터 받은 프로토콜 해석과 GUI 반영
    private void handleServerMessage(String message) {
        String[] tokens = message.split(Protocol.SEP);
        if (tokens.length == 0) {
            return;
        }
        String command = tokens[0];

        // EDT 스레드에서 화면 새로고침 해주기
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                switch (command) {
                    case Protocol.NAME_OK: // 닉네임 설정 성공하면 로비로 화면 전환
                        gui.onLoginSuccess();
                        break;

                    case Protocol.NAME_NOT_OK: // 실패하면 알림창 표시
                        String reason = tokens.length > 1 ? tokens[1] : "닉네임 설정 실패";
                        gui.onLoginFail(reason);
                        break;

                    case Protocol.RES_ROOM_LIST: // 방 목록 수신받으면 로비 테이블 갱신
                        String roomData = tokens.length > 1 ? tokens[1] : "";
                        gui.updateRoomList(roomData);
                        break;

                    case Protocol.CHAT: // 채팅 받으면 채팅창에 추가 (CHAT|발신자|내용)
                        if (tokens.length >= 3) {
                            gui.appendChatMessage(tokens[1], tokens[2]);
                        }
                        break;

                    case Protocol.ROOM_OK: // 방 입장/생성 성공 -> 게임방 화면으로 전환 (ROOM_OK|방번호|방제목)
                        String roomId = tokens.length > 1 ? tokens[1] : "";
                        String roomTitle = tokens.length > 2 ? tokens[2] : "";
                        gui.onEnterRoomSuccess(roomId, roomTitle);
                        break;

                    case Protocol.ROOM_FAIL: // 방 입장 실패 알림
                        String failMsg = tokens.length > 1 ? tokens[1] : "방 입장에 실패했습니다.";
                        gui.onRoomFail(failMsg);
                        break;

                    default:
                        System.out.println("[클라이언트 소켓] 미처리 명령어: " + command);
                        break;
                }
            }
        });
    }

    // 소켓 및 스트림 종료
    public void disconnect() {
        isRunning = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
    }
}
