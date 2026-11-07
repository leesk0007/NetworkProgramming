public final class Protocol {
    private Protocol() {
    }

    // 송신용 구분자
    public static final String DELIM = "|";

    //  수신용 구분자
    public static final String SEP = "\\|";

    // 명령어
    // 1. 닉네임 설정
    public static final String SET_NAME = "SET_NAME"; // SET_NAME|닉네임
    public static final String NAME_OK = "NAME_OK"; // NAME_OK
    public static final String NAME_NOT_OK = "NAME_NOT_OK"; // NAME_NOT_OK|실패사유

    // 2. 방
    public static final String REQ_ROOM_LIST = "REQ_ROOM_LIST"; // REQ_ROOM_LIST
    public static final String RES_ROOM_LIST = "RES_ROOM_LIST"; // RES_ROOM_LIST|방번호,방제목,현재인원/최대인원;또다른방번호,방제목... (방들은 ;으로 구분합니다)
    public static final String CREATE_ROOM = "CREATE_ROOM"; // CREATE_ROOM|방제목
    public static final String ENTER_ROOM = "ENTER_ROOM"; // ENTER_ROOM|방번호
    public static final String LEAVE_ROOM = "LEAVE_ROOM"; // LEAVE_ROOM|방번호
    public static final String ROOM_USER_LIST = "ROOM_USER_LIST"; // ROOM_USER_LIST|닉네임1,닉네임2,닉네임3

    // 3. 퀴즈
    public static final String GAME_START = "GAME_START"; // GAME_START|방번호
    public static final String SHOW_QUIZ = "SHOW_QUIZ"; // SHOW_QUIZ|라운드번호|초성
    public static final String SUBMIT_ANSWER = "SUBMIT_ANSWER"; // SUBMIT_ANSWER|방번호|입력단어
    public static final String ROUND_CLEAR = "ROUND_CLEAR"; // ROUND_CLEAR|정답자닉네임|정답단어|획득점수
    public static final String SCORE_SYNC = "SCORE_SYNC"; // SCORE_SYNC|닉네임,점수;닉네임,점수
    public static final String WRONG_GUESS = "WRONG_GUESS"; // WRONG_GUESS|틀린유저닉네임|오답단어

    // 4. 기타
    public static final String TIMER_TICK = "TIMER_TICK"; // TIMER_TICK|남은초
    public static final String HINT_OPEN = "HINT_OPEN"; //  HINT_OPEN|글자위치(1부터)|공개글자
    public static final String CHAT = "CHAT"; // 클라 -> 서버: CHAT|방번호|안녕하세요 , 서버 -> 방 전원: CHAT|성규|안녕하세요
    public static final String USE_ITEM = "USE_ITEM"; // USE_ITEM|방번호|아이템종류|대상유저
}
