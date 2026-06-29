package shared;

public enum MessageType {

    CHAT,
    SYSTEM,

    CREATE_ROOM,
    JOIN_ROOM,
    ROOM_CREATED,
    ROOM_JOINED,
    ROOM_ERROR,

    START_GAME,

    SKIP_PHASE,
    PHASE_CHANGE,
    ROLE_ASSIGN,
    PLAYER_LIST,
    NIGHT_ACTION,
    INVESTIGATION_RESULT,
    VOTE,
    DEATH,
    VOTE_RESULT,
    GAME_OVER,
    
    UPDATE_SETTINGS,
    SETTINGS_INFO,
    
    ROLE_REVEAL,
    LEAVE_ROOM
}