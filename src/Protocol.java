public final class Protocol {
    private Protocol() {
    }

    // 송신용 구분자
    public static final String DELIM = "|";
    public static final String LIST_DELIM = ";";
    public static final String ITEM_DELIM = ",";

    // 수신용 구분자
    public static final String SEP = "\\|";

    // 아이템 종류
    public static final String ITEM_BAN_INPUT = "BAN_INPUT";
    public static final String ITEM_DOUBLE_SCORE = "DOUBLE_SCORE";

    // 명령어
    // 1. 닉네임 설정
    public static final String SET_NAME = "SET_NAME"; // SET_NAME|닉네임
    public static final String NAME_OK = "NAME_OK"; // NAME_OK
    public static final String NAME_NOT_OK = "NAME_NOT_OK"; // NAME_NOT_OK|실패사유

    // 2. 방
    public static final String REQ_ROOM_LIST = "REQ_ROOM_LIST"; // REQ_ROOM_LIST
    public static final String RES_ROOM_LIST = "RES_ROOM_LIST"; // RES_ROOM_LIST|방번호,방제목,현재인원/최대인원;방번호,방제목,현재인원/최대인원
    public static final String CREATE_ROOM = "CREATE_ROOM"; // CREATE_ROOM|방제목
    public static final String ENTER_ROOM = "ENTER_ROOM"; // ENTER_ROOM|방번호
    public static final String ROOM_OK = "ROOM_OK"; // ROOM_OK|방번호|방제목
    public static final String ROOM_FAIL = "ROOM_FAIL"; // ROOM_FAIL|실패사유
    public static final String LEAVE_ROOM = "LEAVE_ROOM"; // LEAVE_ROOM|방번호
    public static final String USER_LIST = "USER_LIST"; // USER_LIST|닉네임,점수;닉네임,점수

    // 3. 퀴즈
    public static final String GAME_START = "GAME_START"; // GAME_START|방번호
    public static final String START_OK = "START_OK"; // START_OK
    public static final String START_FAIL = "START_FAIL"; // START_FAIL|실패사유
    public static final String SHOW_QUIZ = "SHOW_QUIZ"; // SHOW_QUIZ|라운드번호|초성|문제설명
    public static final String SUBMIT_ANSWER = "SUBMIT_ANSWER"; // SUBMIT_ANSWER|방번호|입력단어
    public static final String ROUND_END = "ROUND_END"; // ROUND_END|정답자닉네임|정답단어|획득점수
    public static final String GAME_OVER = "GAME_OVER"; // GAME_OVER|닉네임,점수;닉네임,점수

    // 4. 기타
    public static final String TIMER_TICK = "TIMER_TICK"; // TIMER_TICK|남은초
    public static final String HINT_OPEN = "HINT_OPEN"; // HINT_OPEN|힌트단계|힌트내용
    public static final String CHAT = "CHAT"; // 클라 -> 서버: CHAT|방번호|내용, 서버 -> 방 전원: CHAT|발신자닉네임|내용
    public static final String USE_ITEM = "USE_ITEM"; // USE_ITEM|방번호|아이템종류|대상유저
    public static final String ITEM_APPLIED = "ITEM_APPLIED"; // ITEM_APPLIED|사용자닉네임|아이템종류|대상유저
    public static final String ITEM_FAIL = "ITEM_FAIL"; // ITEM_FAIL|실패사유

    // 유틸 메서드
    // 명령어와 필드를 구분자로 결합하여 전송 패킷 문자열 생성
    public static String build(String... parts) {
        return String.join(DELIM, parts);
    }

    // 닉네임 또는 방 제목 내 예약 구분자 포함 여부 검사
    public static boolean hasReservedDelims(String text) {
        if (text == null) {
            return false;
        }
        return text.contains(DELIM) || text.contains(LIST_DELIM) || text.contains(ITEM_DELIM);
    }

    // 메인 구분자 및 줄바꿈 문자를 치환하여 패킷 파싱 깨짐 방지
    public static String sanitizeField(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(DELIM, " ").replace("\n", " ").replace("\r", " ");
    }
}
