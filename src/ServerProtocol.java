// 2331311 임동섭

public class ServerProtocol {

	public static final String SEP = "\\|";
	public static final String ITEM_BAN_INPUT = "BAN_INPUT";
	public static final String ITEM_DOUBLE_SCORE = "DOUBLE_SCORE";
	public static final String SET_NAME = "SET_NAME";
	public static final String NAME_OK = "NAME_OK";
	public static final String NAME_NOT_OK = "NAME_NOT_OK";
	public static final String QUIT = "QUIT";
	public static final String REQ_ROOM_LIST = "REQ_ROOM_LIST";
	public static final String RES_ROOM_LIST = "RES_ROOM_LIST";
	public static final String CREATE_ROOM = "CREATE_ROOM";
	public static final String ENTER_ROOM = "ENTER_ROOM";
	public static final String ROOM_OK = "ROOM_OK";
	public static final String ROOM_FAIL = "ROOM_FAIL";
	public static final String LEAVE_ROOM = "LEAVE_ROOM";
	public static final String USER_LIST = "USER_LIST";
	public static final String LEAVE_OK = "LEAVE_OK";
	public static final String HOST_CHANGE = "HOST_CHANGE";
	public static final String GAME_START = "GAME_START";
	public static final String START_OK = "START_OK";
	public static final String START_FAIL = "START_FAIL";
	public static final String SHOW_QUIZ = "SHOW_QUIZ";
	public static final String SUBMIT_ANSWER = "SUBMIT_ANSWER";
	public static final String ROUND_END = "ROUND_END";
	public static final String GAME_OVER = "GAME_OVER";
	public static final String ANSWER_WRONG = "ANSWER_WRONG";
	public static final String WRONG_BROADCAST = "WRONG_BROADCAST";
	public static final String SCORE_SYNC = "SCORE_SYNC";
	public static final String SKIP_ROUND = "SKIP_ROUND";
	public static final String SKIP_ENABLE = "SKIP_ENABLE";
	public static final String SKIP_FAIL = "SKIP_FAIL";
	public static final String TIMER_TICK = "TIMER_TICK";
	public static final String HINT_OPEN = "HINT_OPEN";
	public static final String CHAT = "CHAT";
	public static final String USE_ITEM = "USE_ITEM";
	public static final String ITEM_APPLIED = "ITEM_APPLIED";
	public static final String ITEM_FAIL = "ITEM_FAIL";
	public static final String INPUT_LOCK = "INPUT_LOCK";
	public static final String INPUT_UNLOCK = "INPUT_UNLOCK";
	public static final String TYPING = "TYPING";
	public static final String TYPING_SHARE = "TYPING_SHARE";
	public static final String ERROR = "ERROR";

	public static final int PORT = 54321;
	public static final int MAX_USERS = 4;
	public static final int MIN_START_USERS = 2;
	public static final int TOTAL_ROUNDS = 5;
	public static final int ROUND_SECONDS = 30;
	public static final int HINT_INTERVAL = 10;
	public static final int SKIP_AFTER = 15;
	public static final int NEXT_ROUND_WAIT = 3;
	public static final int SCORE_CORRECT = 10;
	public static final int LOCK_SECONDS = 1;
}
