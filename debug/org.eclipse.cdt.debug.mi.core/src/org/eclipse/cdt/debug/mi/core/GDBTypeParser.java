package org.eclipse.cdt.debug.mi.core;
/*
 * Created on May 26, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author alain
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GDBTypeParser {

	// GDB type parsing from whatis command
	// declarator: type dcl
	// type: (name)+
	// dcl: ('*' | '&')* direct-decl
	// direct-dcl: '(' dcl ')'
	//             direct-dcl '(' ')'
	//             direct-dcl '[' integer ']'
	// name: ([a-zA-z][0-9])+
	// integer ([0-9)+ 

	final static int EOF = -1;
	final static int NAME = 0;
	final static int PARENS = 1;
	final static int BRACKETS = 2;

	String line;
	int index;
	int tokenType;
	String token;
	String dataType;
	String name;
	String out;
	GDBType gdbType;

	public class GDBType {
		public final static int GENERIC = 0;
		public final static int POINTER = 1;
		public final static int REFERENCE= 2;
		public final static int ARRAY = 3;
		public final static  int FUNCTION = 4;

		String name;
		int type;

		public GDBType(String n) {
			this(n, 0);
		}

		public GDBType(int t) {
			this ("", t);			
		}

		GDBType (String n, int t) {
			name = n;
			type = t;
		}

		public String toString() {
			return name;
		}

		public int getType() {
			return type;
		}
	}

	public class GDBDerivedType extends GDBType {
		GDBType child;
		int dimension;
		public GDBDerivedType(GDBType c, int i) {
			this(c, i, 0);
		}

		public GDBDerivedType(GDBType c, int t, int dim) {
			super(t);
			child = c;
			dimension = dim;
		}

		public GDBType getChild() {
			return child; 
		}

		public boolean hasChild() {
			return child != null;
		}

		public int getDimension() {
			return dimension;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			switch (getType()) {
				case FUNCTION:
					sb.append(" Function returning " + (hasChild() ? child.toString() : ""));
				break;
				case ARRAY:
					sb.append(" Array[" + dimension + "]" + " to " + (hasChild() ? child.toString() : ""));
				break;
				case REFERENCE:
					sb.append(" Reference to " + (hasChild() ? child.toString() : ""));
				break;
				case POINTER:
					sb.append(" Pointer to " + (hasChild() ? child.toString() : ""));
				break;
			}
			return sb.toString();
		}
	}

	public GDBTypeParser() {
	}

	public GDBType getGDBType() {
		return gdbType;
	}

	public void verbose() {
		System.out.println(name + " --> " + out + dataType);
	}

	public void parse(String s) {
		// Sanity.
		if (s == null) {
			s = new String();
		}
		s = s.trim();
		
		// Initialize.
		line = s;
		index = 0;
		token = "";
		dataType = "";
		
		out = "";
		name = "";

		// Fetch the datatype.
		while (getToken() == NAME) {
			dataType += " " + token;
		}
		
		gdbType = new GDBType(dataType);

		// After getting the type move back 
		//ungetch();
		dcl(tokenType);
	}

	int getch() {
		if (index >= line.length() || index < 0) {
			return EOF;
		}
		return line.charAt(index++);
	}

	void ungetch() {
		if (index > 0) {
			index--;
		}
	}

	// check if the character is an alphabet
	boolean isCIdentifierStart(int c) {
		if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
			return true;
		}
		return false;
	}

	// check is the character is alpha numeric
	// [a-zA-Z0-9]
	boolean isCIdentifierPart(int c) {
		if ((c >= '0' && c <= 9) || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
			return true;
		}
		return false;
	}

	boolean isCSpace(int c) {
		if (c == ' ' || c == '\t' || c == '\f' || c == '\n') {
			return true;
		}
		return false;
	}

	// method returns the next token
	int getToken() {
		token = "";

		int c = getch();
		char character = (char)c;

		// Skip over any space
		while (isCSpace(c)) {
			c = getch();
		}

		if (c == '(') {
			if ((c = getch()) == ')') {
				token = "()";
				tokenType = PARENS;
			} else {
				ungetch();
				tokenType = '(';
			}
		} else if (c == '[') {
			while ((c = getch()) != ']' && c != EOF) {
				token += (char)c;
			}
			tokenType = BRACKETS;
		} else if (isCIdentifierStart(c)) {
			token = "" + (char)c;
			while (isCIdentifierPart((c = getch())) && c != EOF) {
				token += (char)c;
			}
			ungetch();
			tokenType = NAME;
		} else if (c == '{') {
			// Swallow gdb sends things like "struct foobar {..} *"
			// FIXME: if the bracket is not terminate do we throw exception?
			int count = 1;
			do {
				c = getch();
				if (c == '{') {
					count++;
				} else if (c == '}') {
					count--;
				}
			} while (count > 0 && c != EOF);
		} else {
			tokenType = c;
		}
		return tokenType;
	}

	void dcl() {
		dcl(getToken());
	}
	
	// parse a declarator
	void dcl(int c) {
		int nstar = 0;
		int namp = 0;
		if (c == '*') {
			nstar++;
			for (; getToken() == '*'; nstar++) {
			}
		} else if (c == '&') {
			namp++;
			for (; getToken() == '&'; namp++) {
			}
		}
		dirdcl();
		while (nstar-- > 0) {
			out += " pointer to ";
			gdbType = new GDBDerivedType(gdbType, GDBDerivedType.POINTER);
		}
		while (namp-- > 0) {
			out += " reference to";
			gdbType = new GDBDerivedType(gdbType, GDBDerivedType.REFERENCE);			
		}
	}

	// parse a direct declarator
	void dirdcl() {
		int type;

		if (tokenType == '(') {
			dcl();
			if (tokenType != ')') {
				// FIXME: Do we throw an exception ? not terminate parenthese
				return;
			}
		} else if (tokenType == NAME) {
			// Useless we do not need the name of the variable
			name = " " + token;
		} else {
			// FIXME: another oops bad declaration
			return;
		}

		while ((type = getToken()) == PARENS || type == BRACKETS) {
			if (type == EOF) {
				return;
			}
			if (type == PARENS) {
				out += " function returning ";
				gdbType = new GDBDerivedType(gdbType, GDBType.FUNCTION);
			} else {
				int len = 0;
				if (token.length() > 0) {
					try {
						out += "" + " array[";
						len = Integer.parseInt(token);
						out += len + "]";
						out += " of ";
					} catch (NumberFormatException e) {
						out += " array[0] of ";
					}
				} else {
					out += " array of ";
				}
				gdbType = new GDBDerivedType(gdbType, GDBType.ARRAY, len);
			}
		}
	}


	public static void main(String[] args) {
		GDBTypeParser parser = new GDBTypeParser();
		System.out.println("char **argv");
		parser.parse("unsigned long long int **argv");
		System.out.println(parser.getGDBType());
		
		System.out.println("int (*daytab)[13]");
		parser.parse("int (*daytab)[13]");
		parser.verbose();
		System.out.println(parser.getGDBType());

		System.out.println("int *daytab[13]");
		parser.parse("int *daytab[13]");
		System.out.println(parser.getGDBType());
	
		System.out.println("void *comp()");
		parser.parse("void *comp()");
		System.out.println(parser.getGDBType());
	
		System.out.println("void (*comp)()");
		parser.parse("void (*comp)()");
		System.out.println(parser.getGDBType());

		System.out.println("int (*func[15])()");
		parser.parse("int (*func[15])()");
		System.out.println(parser.getGDBType());

		System.out.println("char (*(*x())[])()");
		parser.parse("char (*(*x())[])()");
		System.out.println(parser.getGDBType());
	
		System.out.println("char (*(*x[3])())[5]");
		parser.parse("char (*(*x[3])())[5]");
		System.out.println(parser.getGDBType());
	}
}
