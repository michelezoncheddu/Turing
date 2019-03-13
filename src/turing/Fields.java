package turing;

/**
 * Message fields
 */
public final class Fields {

	// operations
	public static final String OP              = "o";
	public static final String OP_LOGIN        = "o_0";
	public static final String OP_LOGOUT       = "o_1";
	public static final String OP_CREATE_DOC   = "o_2";
	public static final String OP_SHOW_DOC     = "o_3";
	public static final String OP_SHOW_SEC     = "o_4";
	public static final String OP_EDIT_SEC     = "o_5";
	public static final String OP_END_EDIT     = "o_6";
	public static final String OP_LIST         = "o_7";
	public static final String OP_CHAT_MSG     = "o_8";
	public static final String OP_INVITE       = "o_9";

	// status
	public static final String STATUS          = "s";
	public static final String STATUS_OK       = "s_0";
	public static final String STATUS_ERR      = "s_1";

	// fields
	public static final String USERNAME        = "f_0";
	public static final String PASSWORD        = "f_1";
	public static final String DOCS            = "f_2";
 	public static final String DOC_NAME        = "f_3";
	public static final String DOC_CREATOR     = "f_4";
	public static final String DOC_SECTION     = "f_5";
	public static final String SECTIONS        = "f_6";
	public static final String SEC_CONTENT     = "f_7";
	public static final String DOC_CONTENT     = "f_8";
	public static final String IS_SHARED       = "f_9";
	public static final String CHAT_ADDR       = "f_10";
	public static final String CHAT_MSG        = "f_11";
	public static final String ERR_MSG         = "f_12";
}
