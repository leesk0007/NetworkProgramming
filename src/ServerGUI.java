// 2331311 임동섭

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerGUI extends JFrame {

	private int port;
	private ServerSocket serverSocket = null;

	private Thread acceptThread = null;
	private Vector<ClientHandler> users = new Vector<ClientHandler>();
	private Vector<Room> rooms = new Vector<Room>();
	private int roomCount = 0;

	private Vector<String> answers = new Vector<String>();
	private Vector<String> questions = new Vector<String>();
	private Random random = new Random();

	private JTextArea t_display;
	private JButton b_connect, b_disconnect, b_exit;

	// 서버 창 생성
	public ServerGUI(int port) {
		super("초성 퀴즈 Server");

		buildGUI();

		setSize(500, 400);
		setLocation(500, 0);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		this.port = port;

		loadWords();
	}

	// 화면 구성
	private void buildGUI() {
		add(createDisplayPanel(), BorderLayout.CENTER);
		add(createControlPanel(), BorderLayout.SOUTH);
	}

	// 로그 출력창 생성
	private JPanel createDisplayPanel() {
		JPanel p = new JPanel(new BorderLayout());

		t_display = new JTextArea();
		t_display.setEditable(false);

		p.add(new JScrollPane(t_display), BorderLayout.CENTER);

		return p;
	}

	// 시작·종료 버튼 생성
	private JPanel createControlPanel() {
		JPanel p = new JPanel(new GridLayout(1, 0));

		b_connect = new JButton("서버 시작");
		b_connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				acceptThread = new Thread(new Runnable() {
					@Override
					public void run() {
						startServer();
					}
				});
				acceptThread.start();

				b_connect.setEnabled(false);
				b_disconnect.setEnabled(true);
				b_exit.setEnabled(false);
			}
		});

		b_disconnect = new JButton("서버 종료");
		b_disconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				disconnect();

				b_connect.setEnabled(true);
				b_disconnect.setEnabled(false);
				b_exit.setEnabled(true);
			}
		});

		b_exit = new JButton("종료");
		b_exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		p.add(b_connect);
		p.add(b_disconnect);
		p.add(b_exit);

		b_disconnect.setEnabled(false);

		return p;
	}

	// 서버 IP 확인
	private String getLocalAddr() {
		InetAddress local = null;
		String addr = "";
		try {
			local = InetAddress.getLocalHost();
			addr = local.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return addr;
	}

	// 접속 대기·수락
	private void startServer() {
		Socket clientSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			printDisplay("서버가 시작되었습니다: " + getLocalAddr());

			while (acceptThread == Thread.currentThread()) {
				clientSocket = serverSocket.accept();

				String cAddr = clientSocket.getInetAddress().getHostAddress();
				printDisplay("클라이언트가 연결되었습니다: " + cAddr);

				ClientHandler cHandler = new ClientHandler(clientSocket);
				users.add(cHandler);
				cHandler.start();
			}
		} catch (SocketException e) {
			printDisplay("서버 소켓 종료");
		} catch (IOException e) {
			System.err.println("서버 소켓 오류> " + e.getMessage());
		}
	}

	// 서버 종료
	private void disconnect() {
		try {
			acceptThread = null;
			serverSocket.close();

			Vector<ClientHandler> list = new Vector<ClientHandler>(users);
			for (ClientHandler c : list) {
				c.clientSocket.close();
			}
		} catch (IOException e) {
			System.err.println("서버 소켓 닫기 오류> " + e.getMessage());
		}
	}

	// 로그 출력
	private void printDisplay(String msg) {
		t_display.append(msg + "\n");
		t_display.setCaretPosition(t_display.getDocument().getLength());
	}

	// 문제 파일 읽기
	private void loadWords() {
		String fileName = "words.json";
		File file = new File(fileName);
		if (!file.exists()) {
			fileName = "src/words.json";
		}

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String line;
			String question = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("\"question\"")) {
					question = getValue(line);
				} else if (line.contains("\"answer\"")) {
					answers.add(getValue(line));
					questions.add(question);
					question = "";
				}
			}
			br.close();
			printDisplay("단어 " + answers.size() + "개를 읽었습니다.");
		} catch (IOException e) {
			printDisplay("words.json 파일을 읽을 수 없습니다.");
		}
	}

	// 문제 데이터 값 추출
	private String getValue(String line) {
		String value = line.substring(line.indexOf(":") + 1).trim();
		if (value.endsWith(",")) {
			value = value.substring(0, value.length() - 1);
		}
		value = value.replace("\"", "");
		return value;
	}

	// 초성 추출
	private String getChosung(String word) {
		String cho = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";
		String result = "";
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (c >= '가' && c <= '힣') {
				int index = (c - '가') / 588;
				result = result + cho.charAt(index);
			} else {
				result = result + c;
			}
		}
		return result;
	}

	// 힌트 생성
	private String makeHint(String word, int count) {
		String hint = word.substring(0, count);
		hint = hint + getChosung(word.substring(count));
		return hint;
	}

	// 방 목록 메시지 생성
	private String getRoomList() {
		String list = "";
		for (int i = 0; i < rooms.size(); i++) {
			Room r = rooms.get(i);
			String state = "대기중";
			if (r.playing) {
				state = "게임중";
			}
			if (i > 0) {
				list = list + ";";
			}
			list = list + r.id + "," + r.title + "," + r.members.size() + "/" + ServerProtocol.MAX_USERS + "," + state;
		}
		return ServerProtocol.RES_ROOM_LIST + "|" + list;
	}

	// 로비에 방 목록 전송
	private void sendRoomList() {
		String msg = getRoomList();
		for (ClientHandler c : users) {
			if (c.uid != null && c.room == null) {
				c.sendMessage(msg);
			}
		}
	}

	// 방 번호로 방 찾기
	private Room findRoom(int id) {
		for (Room r : rooms) {
			if (r.id == id) {
				return r;
			}
		}
		return null;
	}

	// 닉네임·방제목 검사
	private boolean checkName(String name) {
		if (name.equals("") || name.contains("|") || name.contains(";") || name.contains(",")) {
			return false;
		}
		return true;
	}

	private class Room {
		int id;
		String title;
		ClientHandler host;
		Vector<ClientHandler> members = new Vector<ClientHandler>();
		boolean playing = false;

		String answer = "";
		String chosung = "";
		boolean solved = false;
		boolean skipEnabled = false;
		boolean skip = false;

		Thread gameThread = null;

		// 방 생성
		Room(int id, String title, ClientHandler host) {
			this.id = id;
			this.title = title;
			this.host = host;
		}

		// 방 전체 전송
		void broadcasting(String msg) {
			for (ClientHandler c : members) {
				c.sendMessage(msg);
			}
		}

		// 본인 제외 전송
		void broadcastingOthers(String msg, ClientHandler me) {
			for (ClientHandler c : members) {
				if (c != me) {
					c.sendMessage(msg);
				}
			}
		}

		// 참가자 목록 생성
		String getUserList() {
			String list = "";
			for (int i = 0; i < members.size(); i++) {
				if (i > 0) {
					list = list + ";";
				}
				list = list + members.get(i).uid + "," + members.get(i).score;
			}
			return ServerProtocol.USER_LIST + "|" + list;
		}

		// 점수 순위 생성
		String getScoreList() {
			Vector<ClientHandler> list = new Vector<ClientHandler>();
			for (ClientHandler c : members) {
				list.add(c);
			}

			for (int i = 0; i < list.size() - 1; i++) {
				for (int j = i + 1; j < list.size(); j++) {
					if (list.get(j).score > list.get(i).score) {
						ClientHandler temp = list.get(i);
						list.set(i, list.get(j));
						list.set(j, temp);
					}
				}
			}

			String result = "";
			for (int i = 0; i < list.size(); i++) {
				if (i > 0) {
					result = result + ";";
				}
				result = result + list.get(i).uid + "," + list.get(i).score;
			}
			return result;
		}

		// 선착순 정답 판정
		synchronized void checkAnswer(ClientHandler c, String word) {
			printDisplay("[" + id + "번 방] " + c.uid + " 정답 제출: " + word);

			if (solved) {
				c.sendMessage(ServerProtocol.ANSWER_WRONG + "|" + word + "|이미 정답 나옴");
				return;
			}

			String reason = "";
			if (!getChosung(word).equals(chosung)) {
				reason = "초성 불일치";
			} else if (!answers.contains(word)) {
				reason = "사전에 없음";
			}

			if (!reason.equals("")) {
				c.sendMessage(ServerProtocol.ANSWER_WRONG + "|" + word + "|" + reason);
				broadcastingOthers(ServerProtocol.WRONG_BROADCAST + "|" + c.uid + "|" + word, c);
				printDisplay("[" + id + "번 방] 오답(" + reason + "): " + c.uid + " - " + word);
				return;
			}

			solved = true;
			int point = ServerProtocol.SCORE_CORRECT;
			if (c.doubleScore) {
				point = point * 2;
				c.doubleScore = false;
			}
			c.score = c.score + point;

			broadcasting(ServerProtocol.ROUND_END + "|" + c.uid + "|" + word + "|" + point);
			broadcasting(getUserList());
			broadcasting(ServerProtocol.SCORE_SYNC + "|" + getScoreList());
			printDisplay("[" + id + "번 방] 정답: " + c.uid + " +" + point + "점 (" + getScoreList() + ")");
		}

		// 시간 초과·스킵 처리
		synchronized void timeOver() {
			if (solved) {
				return;
			}
			solved = true;
			broadcasting(ServerProtocol.ROUND_END + "|-|" + answer + "|0");
			if (skip) {
				printDisplay("[" + id + "번 방] 스킵, 정답: " + answer);
			} else {
				printDisplay("[" + id + "번 방] 시간 초과, 정답: " + answer);
			}
		}

		// 방 퇴장 처리
		synchronized void removeUser(ClientHandler c) {
			members.removeElement(c);
			printDisplay("[" + id + "번 방] " + c.uid + " 퇴장");

			if (members.size() == 0) {
				stop();
				rooms.removeElement(this);
				printDisplay("방 삭제: [" + id + "번 방] " + title);
			} else {
				if (host == c) {
					host = members.get(0);
					broadcasting(ServerProtocol.HOST_CHANGE + "|" + host.uid);
					printDisplay("[" + id + "번 방] 방장 변경: " + host.uid);
				}
				broadcasting(getUserList());
			}
		}

		// 게임 진행 시작
		void start() {
			gameThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Vector<Integer> used = new Vector<Integer>();

					for (int round = 1; round <= ServerProtocol.TOTAL_ROUNDS; round++) {
						if (gameThread != Thread.currentThread() || members.size() == 0) {
							break;
						}

						int index = random.nextInt(answers.size());
						while (used.contains(index) && used.size() < answers.size()) {
							index = random.nextInt(answers.size());
						}
						used.add(index);

						answer = answers.get(index);
						chosung = getChosung(answer);
						solved = false;
						skipEnabled = false;
						skip = false;

						broadcasting(ServerProtocol.SHOW_QUIZ + "|" + round + "|" + chosung + "|" + questions.get(index));
						printDisplay("[" + id + "번 방] " + round + "라운드 출제: " + answer + " (" + chosung + ")");

						int hintCount = 0;
						for (int sec = 1; sec <= ServerProtocol.ROUND_SECONDS; sec++) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								return;
							}

							if (solved || skip || members.size() == 0) {
								break;
							}

							broadcasting(ServerProtocol.TIMER_TICK + "|" + (ServerProtocol.ROUND_SECONDS - sec));

							if (sec % ServerProtocol.HINT_INTERVAL == 0 && hintCount < answer.length() - 1) {
								hintCount++;
								broadcasting(ServerProtocol.HINT_OPEN + "|" + hintCount + "|" + makeHint(answer, hintCount));
							}

							if (sec == ServerProtocol.SKIP_AFTER) {
								skipEnabled = true;
								broadcasting(ServerProtocol.SKIP_ENABLE);
							}
						}

						if (members.size() == 0) {
							break;
						}

						timeOver();

						try {
							Thread.sleep(ServerProtocol.NEXT_ROUND_WAIT * 1000);
						} catch (InterruptedException e) {
							return;
						}
					}

					playing = false;
					if (members.size() > 0) {
						broadcasting(ServerProtocol.GAME_OVER + "|" + getScoreList());
						printDisplay("[" + id + "번 방] 게임 종료: " + getScoreList());
						sendRoomList();
					}
				}
			});
			gameThread.start();
		}

		// 게임 진행 중지
		void stop() {
			gameThread = null;
		}
	}

	private class ClientHandler extends Thread {
		private Socket clientSocket;
		private BufferedWriter out;
		private String uid;

		private Room room = null;
		private int score = 0;
		private boolean hasBanItem = false;
		private boolean hasDoubleItem = false;
		private boolean doubleScore = false;
		private boolean banned = false;

		// 클라이언트 처리 객체 생성
		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		// 클라이언트 신호 수신·분기
		private void receiveMessages(Socket cs) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream(), "UTF-8"));
				out = new BufferedWriter(new OutputStreamWriter(cs.getOutputStream(), "UTF-8"));

				String message;
				while ((message = in.readLine()) != null) {
					String[] msg = message.split(ServerProtocol.SEP);
					if (msg.length == 0) {
						sendMessage(ServerProtocol.ERROR + "|알 수 없는 명령");
						continue;
					}

					if (msg[0].equals(ServerProtocol.QUIT)) {
						break;
					}

					if (uid == null && !msg[0].equals(ServerProtocol.SET_NAME)) {
						sendMessage(ServerProtocol.ERROR + "|로그인이 필요합니다");
						continue;
					}

					if (msg[0].equals(ServerProtocol.SET_NAME)) {
						setName(msg);
					} else if (msg[0].equals(ServerProtocol.REQ_ROOM_LIST)) {
						sendMessage(getRoomList());
					} else if (msg[0].equals(ServerProtocol.CREATE_ROOM)) {
						createRoom(msg);
					} else if (msg[0].equals(ServerProtocol.ENTER_ROOM)) {
						enterRoom(msg);
					} else if (msg[0].equals(ServerProtocol.LEAVE_ROOM)) {
						if (checkRoom(msg)) {
							sendMessage(ServerProtocol.LEAVE_OK);
							leaveRoom();
						}
					} else if (msg[0].equals(ServerProtocol.GAME_START)) {
						if (checkRoom(msg)) {
							gameStart();
						}
					} else if (msg[0].equals(ServerProtocol.SUBMIT_ANSWER)) {
						if (checkRoom(msg)) {
							submitAnswer(msg);
						}
					} else if (msg[0].equals(ServerProtocol.SKIP_ROUND)) {
						if (checkRoom(msg)) {
							skipRound();
						}
					} else if (msg[0].equals(ServerProtocol.TYPING)) {
						if (checkRoom(msg)) {
							String text = "";
							if (msg.length >= 3) {
								text = msg[2];
							}
							room.broadcastingOthers(ServerProtocol.TYPING_SHARE + "|" + uid + "|" + text, this);
						}
					} else if (msg[0].equals(ServerProtocol.CHAT)) {
						if (checkRoom(msg)) {
							if (msg.length < 3) {
								sendMessage(ServerProtocol.ERROR + "|필드 부족");
							} else {
								room.broadcasting(ServerProtocol.CHAT + "|" + uid + "|" + msg[2]);
								printDisplay("[" + room.id + "번 방] " + uid + ": " + msg[2]);
							}
						}
					} else if (msg[0].equals(ServerProtocol.USE_ITEM)) {
						if (checkRoom(msg)) {
							useItem(msg);
						}
					} else {
						sendMessage(ServerProtocol.ERROR + "|알 수 없는 명령");
					}
				}
			} catch (IOException e) {
				System.err.println("서버 읽기 오류> " + e.getMessage());
			} finally {
				users.removeElement(this);
				if (room != null) {
					leaveRoom();
				}
				if (uid == null) {
					printDisplay("클라이언트 연결 종료. 현재 참가자 수: " + users.size());
				} else {
					printDisplay(uid + " 퇴장. 현재 참가자 수: " + users.size());
				}

				try {
					cs.close();
				} catch (IOException e) {
					System.err.println("서버 닫기 오류> " + e.getMessage());
				}
			}
		}

		// 클라이언트에 신호 전달
		private void sendMessage(String msg) {
			try {
				out.write(msg + "\n");
				out.flush();
			} catch (IOException e) {
				System.err.println("서버 전송 오류> " + e.getMessage());
			}
		}

		// 전체 접속자 전송
		private void broadcasting(String msg) {
			for (ClientHandler c : users) {
				c.sendMessage(msg);
			}
		}

		// 방번호 확인
		private boolean checkRoom(String[] msg) {
			if (room == null || msg.length < 2 || !msg[1].equals(room.id + "")) {
				sendMessage(ServerProtocol.ERROR + "|방번호 불일치");
				return false;
			}
			return true;
		}

		// 닉네임 설정
		private void setName(String[] msg) {
			if (uid != null) {
				sendMessage(ServerProtocol.ERROR + "|이미 로그인됨");
				return;
			}
			if (msg.length < 2 || !checkName(msg[1])) {
				sendMessage(ServerProtocol.NAME_NOT_OK + "|사용할 수 없는 닉네임");
				return;
			}
			for (ClientHandler c : users) {
				if (msg[1].equals(c.uid)) {
					sendMessage(ServerProtocol.NAME_NOT_OK + "|이미 사용 중인 닉네임");
					printDisplay("닉네임 중복: " + msg[1]);
					return;
				}
			}

			uid = msg[1];
			sendMessage(ServerProtocol.NAME_OK);
			printDisplay("새 참가자: " + uid);
			printDisplay("현재 참가자 수: " + users.size());
		}

		// 방 만들기
		private void createRoom(String[] msg) {
			if (room != null) {
				sendMessage(ServerProtocol.ROOM_FAIL + "|이미 방에 있음");
				return;
			}
			if (msg.length < 2 || !checkName(msg[1])) {
				sendMessage(ServerProtocol.ROOM_FAIL + "|사용할 수 없는 방제목");
				return;
			}

			roomCount++;
			Room r = new Room(roomCount, msg[1], this);
			r.members.add(this);
			rooms.add(r);
			room = r;

			sendMessage(ServerProtocol.ROOM_OK + "|" + r.id + "|" + r.title);
			r.broadcasting(r.getUserList());
			sendRoomList();
			printDisplay("방 생성: [" + r.id + "번 방] " + r.title + " (방장: " + uid + ")");
		}

		// 방 입장
		private void enterRoom(String[] msg) {
			if (room != null) {
				sendMessage(ServerProtocol.ROOM_FAIL + "|이미 방에 있음");
				return;
			}

			Room r = null;
			try {
				r = findRoom(Integer.parseInt(msg[1]));
			} catch (Exception e) {
				r = null;
			}

			if (r == null) {
				sendMessage(ServerProtocol.ROOM_FAIL + "|없는 방");
				return;
			}
			if (r.playing) {
				sendMessage(ServerProtocol.ROOM_FAIL + "|게임 중");
				return;
			}
			if (r.members.size() >= ServerProtocol.MAX_USERS) {
				sendMessage(ServerProtocol.ROOM_FAIL + "|정원 초과");
				return;
			}

			r.members.add(this);
			room = r;

			sendMessage(ServerProtocol.ROOM_OK + "|" + r.id + "|" + r.title);
			r.broadcasting(r.getUserList());
			sendRoomList();
			printDisplay("[" + r.id + "번 방] " + uid + " 입장");
		}

		// 방 나가기
		private void leaveRoom() {
			Room r = room;
			room = null;
			r.removeUser(this);
			sendRoomList();
		}

		// 게임 시작
		private void gameStart() {
			if (room.host != this) {
				sendMessage(ServerProtocol.START_FAIL + "|방장이 아님");
				return;
			}
			if (room.members.size() < ServerProtocol.MIN_START_USERS) {
				sendMessage(ServerProtocol.START_FAIL + "|인원 부족");
				return;
			}
			if (room.playing) {
				sendMessage(ServerProtocol.START_FAIL + "|이미 게임 중");
				return;
			}
			if (answers.size() == 0) {
				sendMessage(ServerProtocol.START_FAIL + "|단어 데이터 없음");
				return;
			}

			for (ClientHandler c : room.members) {
				c.score = 0;
				c.hasBanItem = true;
				c.hasDoubleItem = true;
				c.doubleScore = false;
				c.banned = false;
			}
			room.playing = true;

			room.broadcasting(ServerProtocol.START_OK);
			sendRoomList();
			printDisplay("[" + room.id + "번 방] 게임 시작");

			room.start();
		}

		// 정답 제출 처리
		private void submitAnswer(String[] msg) {
			if (msg.length < 3) {
				sendMessage(ServerProtocol.ERROR + "|필드 부족");
				return;
			}
			if (!room.playing) {
				sendMessage(ServerProtocol.ERROR + "|게임 중이 아님");
				return;
			}
			if (banned) {
				printDisplay("[" + room.id + "번 방] " + uid + " 입력 금지 중 제출 무시: " + msg[2]);
				return;
			}

			room.checkAnswer(this, msg[2].trim());
		}

		// 라운드 스킵
		private void skipRound() {
			if (!room.playing || !room.skipEnabled || room.solved) {
				sendMessage(ServerProtocol.SKIP_FAIL + "|아직 스킵할 수 없음");
				return;
			}
			room.skip = true;
			printDisplay("[" + room.id + "번 방] " + uid + " 스킵 요청");
		}

		// 아이템 사용
		private void useItem(String[] msg) {
			if (msg.length < 3) {
				sendMessage(ServerProtocol.ERROR + "|필드 부족");
				return;
			}
			if (!room.playing) {
				sendMessage(ServerProtocol.ITEM_FAIL + "|게임 중이 아님");
				return;
			}

			if (msg[2].equals(ServerProtocol.ITEM_DOUBLE_SCORE)) {
				if (!hasDoubleItem) {
					sendMessage(ServerProtocol.ITEM_FAIL + "|보유 아이템 없음");
					return;
				}
				hasDoubleItem = false;
				doubleScore = true;
				room.broadcasting(ServerProtocol.ITEM_APPLIED + "|" + uid + "|" + msg[2] + "|" + uid);
				printDisplay("[" + room.id + "번 방] 아이템: " + uid + " 점수 2배");

			} else if (msg[2].equals(ServerProtocol.ITEM_BAN_INPUT)) {
				if (!hasBanItem) {
					sendMessage(ServerProtocol.ITEM_FAIL + "|보유 아이템 없음");
					return;
				}

				ClientHandler target = null;
				if (msg.length >= 4) {
					for (ClientHandler c : room.members) {
						if (c.uid.equals(msg[3])) {
							target = c;
						}
					}
				}
				if (target == null || target == this) {
					sendMessage(ServerProtocol.ITEM_FAIL + "|잘못된 대상");
					return;
				}

				hasBanItem = false;
				target.banned = true;
				room.broadcasting(ServerProtocol.ITEM_APPLIED + "|" + uid + "|" + msg[2] + "|" + target.uid);
				target.sendMessage(ServerProtocol.INPUT_LOCK + "|" + ServerProtocol.LOCK_SECONDS);
				printDisplay("[" + room.id + "번 방] 아이템: " + uid + " -> " + target.uid + " 입력 금지");

				final ClientHandler t = target;
				Thread unlockThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(ServerProtocol.LOCK_SECONDS * 1000);
						} catch (InterruptedException e) {
							return;
						}
						t.banned = false;
						t.sendMessage(ServerProtocol.INPUT_UNLOCK);
					}
				});
				unlockThread.start();

			} else {
				sendMessage(ServerProtocol.ITEM_FAIL + "|없는 아이템");
			}
		}

		// 수신 스레드 실행
		@Override
		public void run() {
			receiveMessages(clientSocket);
		}
	}

	// 서버 실행
	public static void main(String[] args) {
		int port = ServerProtocol.PORT;

		ServerGUI server = new ServerGUI(port);
	}
}
