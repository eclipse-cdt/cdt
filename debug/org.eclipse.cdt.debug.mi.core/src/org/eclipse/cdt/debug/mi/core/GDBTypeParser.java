/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core;

/**
 * GDB Type Parser.
 * The code was lifted from: The C Programming Language
 * B. W. Kernighan and D. Ritchie
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
	GDBDerivedType gdbDerivedType;
	GDBType genericType;

	public GDBType getGDBType() {
		if (gdbDerivedType != null) {
			return gdbDerivedType;
		}
		return genericType;
	}

	public String getVariableName() {
		return name;
	}

	public GDBType parse(String s) {
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
		name = "";
		gdbDerivedType = null;

		// Fetch the datatype.
		while (getToken() == NAME) {
			dataType += " " + token;
		}

		genericType = new GDBType(dataType);

		// Start the recursive parser.
		dcl(tokenType);
		return getGDBType();
	}

	public class GDBType {
		public final static int GENERIC = 0;
		public final static int POINTER = 1;
		public final static int REFERENCE = 2;
		public final static int ARRAY = 3;
		public final static int FUNCTION = 4;

		String name;
		int type;

		public GDBType(String n) {
			this(n, 0);
		}

		public GDBType(int t) {
			this("", t);
		}

		GDBType(String n, int t) {
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
		int dimension;
		GDBType child;

		public GDBDerivedType(GDBType c, int i) {
			this(c, i, 0);
		}

		public GDBDerivedType(GDBType c, int t, int dim) {
			super(t);
			setChild(c);
			dimension = dim;
		}

		public int getDimension() {
			return dimension;
		}

		public void setChild(GDBType c) {
			child = c;
		}

		public GDBType getChild() {
			return child;
		}

		public boolean hasChild() {
			return child != null;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			switch (getType()) {
				case FUNCTION :
					sb.append(" function returning " + (hasChild() ? child.toString() : ""));
					break;
				case ARRAY :
					sb.append(" array[" + dimension + "]" + " of " + (hasChild() ? child.toString() : ""));
					break;
				case REFERENCE :
					sb.append(" reference to " + (hasChild() ? child.toString() : ""));
					break;
				case POINTER :
					sb.append(" pointer to " + (hasChild() ? child.toString() : ""));
					break;
			}
			return sb.toString();
		}
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

	void prependChild(int kind) {
		prependChild(kind, 0);
	}

	void prependChild(int kind, int d) {
		GDBDerivedType dType = new GDBDerivedType(genericType, kind, d);
		if (gdbDerivedType != null) {
			// get to the last node in the list and add the new to it
			GDBType leaf = genericType;
			GDBDerivedType node;
			boolean keepGoing = true;
			for (node = gdbDerivedType; keepGoing;) {
				leaf = node.getChild();
				if (leaf instanceof GDBDerivedType) {
					node = (GDBDerivedType) leaf;
				} else {
					keepGoing = false;
				}
			}
			node.setChild(dType);
		} else {
			gdbDerivedType = dType;
		}
	}

	// method returns the next token
	int getToken() {
		token = "";

		int c = getch();

		// Skip over any space
		while (isCSpace(c)) {
			c = getch();
		}

		char character = (char) c;

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
				token += (char) c;
			}
			tokenType = BRACKETS;
		} else if (isCIdentifierStart(c)) {
			token = "" + (char) c;
			while (isCIdentifierPart((c = getch())) && c != EOF) {
				token += (char) c;
			}
			if (c != EOF) {
				ungetch();
			}
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
			prependChild(GDBType.POINTER);
		}
		while (namp-- > 0) {
			prependChild(GDBType.REFERENCE);
			GDBDerivedType referenceType = new GDBDerivedType(genericType, GDBDerivedType.REFERENCE);
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
				prependChild(GDBType.FUNCTION);
			} else {
				int len = 0;
				if (token.length() > 0) {
					try {
						len = Integer.parseInt(token);
					} catch (NumberFormatException e) {
					}
				}
				prependChild(GDBType.ARRAY, len);
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
