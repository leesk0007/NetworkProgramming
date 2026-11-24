// 2331311 임동섭
// 초성 퀴즈 메시지 규격

public class Protocol {

	// C→S : 클라이언트가 보내는 요청
	public static final String SET_NAME = "SET_NAME";           // SET_NAME|닉네임
	public static final String REQ_ROOM_LIST = "REQ_ROOM_LIST"; // REQ_ROOM_LIST
	public static final String CREATE_ROOM = "CREATE_ROOM";     // CREATE_ROOM|방제목
	public static final String ENTER_ROOM = "ENTER_ROOM";       // ENTER_ROOM|방번호
	public static final String LEAVE_ROOM = "LEAVE_ROOM";       // LEAVE_ROOM|방번호
	public static final String GAME_START = "GAME_START";       // GAME_START|방번호
	public static final String SUBMIT_ANSWER = "SUBMIT_ANSWER"; // SUBMIT_ANSWER|방번호|단어
	public static final String SKIP_ROUND = "SKIP_ROUND";       // SKIP_ROUND|방번호
	public static final String TYPING = "TYPING";               // TYPING|방번호|입력중텍스트
	public static final String CHAT = "CHAT";                   // C→S: CHAT|방번호|내용   S→C: CHAT|발신자|내용
	public static final String USE_ITEM = "USE_ITEM";           // USE_ITEM|방번호|아이템|대상
	public static final String QUIT = "QUIT";                   // QUIT

	// S→C : 서버가 보내는 응답·알림
	public static final String NAME_OK = "NAME_OK";                 // NAME_OK|닉네임
	public static final String NAME_NOT_OK = "NAME_NOT_OK";         // NAME_NOT_OK|사유
	public static final String RES_ROOM_LIST = "RES_ROOM_LIST";     // RES_ROOM_LIST|번호,제목,인원,최대,상태;...
	public static final String ROOM_OK = "ROOM_OK";                 // ROOM_OK|방번호|방제목
	public static final String ROOM_FAIL = "ROOM_FAIL";             // ROOM_FAIL|사유
	public static final String LEAVE_OK = "LEAVE_OK";               // LEAVE_OK
	public static final String USER_LIST = "USER_LIST";             // USER_LIST|닉네임,방장여부;...
	public static final String HOST_CHANGE = "HOST_CHANGE";         // HOST_CHANGE|닉네임
	public static final String START_OK = "START_OK";               // START_OK|총라운드
	public static final String START_FAIL = "START_FAIL";           // START_FAIL|사유
	public static final String SHOW_QUIZ = "SHOW_QUIZ";             // SHOW_QUIZ|라운드|초성|설명
	public static final String TIMER_TICK = "TIMER_TICK";           // TIMER_TICK|남은초
	public static final String HINT_OPEN = "HINT_OPEN";             // HINT_OPEN|단계|힌트내용
	public static final String ANSWER_WRONG = "ANSWER_WRONG";       // ANSWER_WRONG|단어|사유
	public static final String WRONG_BROADCAST = "WRONG_BROADCAST"; // WRONG_BROADCAST|닉네임|단어
	public static final String SKIP_ENABLE = "SKIP_ENABLE";         // SKIP_ENABLE
	public static final String SKIP_FAIL = "SKIP_FAIL";             // SKIP_FAIL|사유
	public static final String TYPING_SHARE = "TYPING_SHARE";       // TYPING_SHARE|닉네임|입력중텍스트
	public static final String ROUND_END = "ROUND_END";             // ROUND_END|정답자|정답단어|획득점수 (정답자 없으면 -)
	public static final String SCORE_SYNC = "SCORE_SYNC";           // SCORE_SYNC|닉네임,점수;...
	public static final String GAME_OVER = "GAME_OVER";             // GAME_OVER|닉네임,점수;...
	public static final String ITEM_APPLIED = "ITEM_APPLIED";       // ITEM_APPLIED|사용자|아이템|대상
	public static final String ITEM_FAIL = "ITEM_FAIL";             // ITEM_FAIL|사유
	public static final String INPUT_LOCK = "INPUT_LOCK";           // INPUT_LOCK|초
	public static final String INPUT_UNLOCK = "INPUT_UNLOCK";       // INPUT_UNLOCK
	public static final String ERROR = "ERROR";                     // ERROR|사유

	// 아이템 이름
	public static final String ITEM_LOCK = "LOCK";     // 입력 금지
	public static final String ITEM_DOUBLE = "DOUBLE"; // 점수 2배

	// 게임 설정값
	public static final int MAX_USERS = 4;        // 방 최대 인원
	public static final int MIN_START_USERS = 2;  // 게임 시작 최소 인원
	public static final int TOTAL_ROUNDS = 5;     // 총 라운드
	public static final int ROUND_SECONDS = 30;   // 라운드 제한시간(초)
	public static final int HINT_INTERVAL = 10;   // 힌트 공개 간격(초)
	public static final int SKIP_AFTER = 15;      // 스킵 버튼 활성화 기준(초)
	public static final int NEXT_ROUND_WAIT = 3;  // 다음 문제까지 대기(초)
	public static final int SCORE_CORRECT = 10;   // 정답 점수 (점수 2배 시 20)
	public static final int LOCK_SECONDS = 1;     // 입력 금지 시간(초)
}
