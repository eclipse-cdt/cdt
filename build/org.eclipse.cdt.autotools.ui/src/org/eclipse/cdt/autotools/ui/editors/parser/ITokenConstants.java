package org.eclipse.cdt.autotools.ui.editors.parser;

public interface ITokenConstants {

	/** end of file */
	public static final int EOF = 0;
	/** end of line */
	public static final int EOL = 1;
	
	/** an open parenthesis */
	public static final int LPAREN = 2;
	/** a close parenthesis */
	public static final int RPAREN = 3;
	/** a comma */
	public static final int COMMA = 4;
	/** a semicolon */
	public static final int SEMI = 5;

	/** a word (either m4 word or shell identifier-looking word) */
	public static final int WORD = 6;

	/** other text (usually punctuation or number, one char at a time) */
	public static final int TEXT = 7;

	/** an m4 string (the text does not contain the outermost quotes) */
	public static final int M4_STRING = 21;
	/** an m4 comment (as determined by changecomment, NOT dnl) */
	public static final int M4_COMMENT = 22;
	
	/** the sh 'if' token */
	public static final int SH_IF = 40;
	/** the sh 'then' token */
	public static final int SH_THEN = 41;
	/** the sh 'else' token */
	public static final int SH_ELSE = 42;
	/** the sh 'elif' token */
	public static final int SH_ELIF = 43;
	/** the sh 'fi' token */
	public static final int SH_FI = 44;
	
	/** the sh 'while' token */
	public static final int SH_WHILE = 45;
	/** the sh 'for' token */
	public static final int SH_FOR = 46;
	/** the sh 'select' token */
	public static final int SH_SELECT = 47;
	/** the sh 'until' token */
	public static final int SH_UNTIL = 48;
	/** the sh 'do' token */
	public static final int SH_DO = 49;
	/** the sh 'done' token */
	public static final int SH_DONE = 50;
	/** the sh 'case' token */
	
	public static final int SH_CASE = 51;
	/** the sh 'in' token */
	public static final int SH_IN = 52;
	/** the sh ';;' token */
	public static final int SH_CASE_CONDITION_END = 53;
	/** the sh 'esac' token */
	public static final int SH_ESAC = 54;

	/** the sh '$' token */
	public static final int SH_DOLLAR = 60;
	
	/** the sh '{' token */
	public static final int SH_LBRACE = 61;
	/** the sh '}' token */
	public static final int SH_RBRACE = 62;
	/** the sh '[' token */
	public static final int SH_LBRACKET = 63;
	/** the sh ']' token */
	public static final int SH_RBRACKET = 64;
	
	/** the sh '<<' token */
	public static final int SH_HERE = 65;
	/** the sh '<<-' token */
	public static final int SH_HERE_DASH = 66;
	/** an sh double-quoted string */
	public static final int SH_STRING_DOUBLE = 67;
	/** an sh single-quoted string */
	public static final int SH_STRING_SINGLE = 68;
	/** an sh backtick-quoted string */
	public static final int SH_STRING_BACKTICK = 69;


}