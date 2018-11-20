/*******************************************************************************
 * Copyright (c) 2010, 2016 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial implementation
 *     Anders Dahlberg (Ericsson)  - Need additional API to extend support for memory spaces (Bug 431627)
 *     Alvaro Sanchez-Leon (Ericsson)  - Need additional API to extend support for memory spaces (Bug 431627)
 *     Matthew Khouzam (Ericsson) - Minor code cleanup and privatization of variables
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb;

import java.util.regex.Pattern;

/**
 * GDB Type Parser (duplicate of org.eclipse.cdt.debug.mi.core.GDBTypeParser)
 * The code was lifted from: The C Programming Language
 * B. W. Kernighan and D. Ritchie
 *
 * @since 3.0
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

	private static final int EOF = -1;
	private static final int NAME = 0;
	private static final int PARENS = 1;
	private static final int BRACKETS = 2;

	private String line;
	private int index;
	private int tokenType;
	private String token;
	private String dataType;
	private String name;
	private GDBDerivedType gdbDerivedType;
	private GDBType genericType;

	public GDBType getGDBType() {
		if (gdbDerivedType != null) {
			return gdbDerivedType;
		}
		return genericType;
	}

	public String getVariableName() {
		return name;
	}

	public GDBType parse(String gdbTypeString) {
		// Sanity.
		String s = (gdbTypeString == null) ? "" : gdbTypeString; //$NON-NLS-1$

		s = Pattern.compile("\\bconst\\b").matcher(s).replaceAll(""); //$NON-NLS-1$//$NON-NLS-2$
		s = Pattern.compile("\\bvolatile\\b").matcher(s).replaceAll(""); //$NON-NLS-1$//$NON-NLS-2$
		s = s.trim();

		// Initialize.
		line = s;
		index = 0;
		tokenType = -1;
		token = ""; //$NON-NLS-1$
		dataType = ""; //$NON-NLS-1$
		name = ""; //$NON-NLS-1$
		gdbDerivedType = null;
		genericType = null;

		// Fetch the datatype.
		while (getToken() == NAME) {
			dataType += " " + token; //$NON-NLS-1$
		}

		// Hack for GDB, the typename can be something like
		// class A : public B, C { ... } *
		// We are only interested in "class A"
		// Carefull for class A::data or class ns::A<ns::data>
		int column = dataType.indexOf(':');
		while (column > 0) {
			if ((column + 2) < dataType.length() && dataType.charAt(column + 1) == ':') {
				column = dataType.indexOf(':', column + 2);
				continue;
			}
			dataType = dataType.substring(0, column);
			break;
		}
		genericType = new GDBType(dataType);

		// Start the recursive parser.
		dcl(tokenType);
		return getGDBType();
	}

	public static String unParse(GDBType gdbParentType) {
		StringBuilder sb = new StringBuilder();
		GDBType gdbType = gdbParentType;
		// Fetch the datatype.
		while (gdbType != null) {
			if (gdbType instanceof GDBDerivedType) {
				GDBDerivedType derived = (GDBDerivedType) gdbType;
				int type = derived.getType();
				gdbType = derived.getChild();
				switch (type) {
				case GDBType.FUNCTION:
					sb.append("()"); //$NON-NLS-1$
					break;
				case GDBType.ARRAY:
					sb.append('[').append(derived.getDimension()).append(']');
					break;
				case GDBType.POINTER:
					handlePointer(gdbType, sb);
					break;
				case GDBType.REFERENCE:
					handleReference(gdbType, sb);
					break;
				}
			} else {
				sb.insert(0, ' ');
				sb.insert(0, gdbType.getTypeName());
				break;
			}
		}
		return sb.toString().trim();

	}

	private static void handleReference(GDBType gdbType, StringBuilder sb) {
		handleReferenceOrPointer(gdbType, sb, '&');
	}

	private static void handlePointer(GDBType gdbType, StringBuilder sb) {
		handleReferenceOrPointer(gdbType, sb, '*');
	}

	private static void handleReferenceOrPointer(GDBType gdbType, StringBuilder sb, char prefix) {
		switch (getChildType(gdbType)) {
		case GDBType.POINTER:
		case GDBType.REFERENCE:
			sb.append(prefix);
			break;
		case GDBType.GENERIC:
			sb.insert(0, prefix);
			break;
		default:
			sb.insert(0, "(" + prefix).append(')'); //$NON-NLS-1$
			break;
		}
	}

	private static int getChildType(GDBType gdbType) {
		return (gdbType != null) ? gdbType.getType() : GDBType.GENERIC;
	}

	public class GDBType {
		public static final int GENERIC = 0;
		public static final int POINTER = 1;
		public static final int REFERENCE = 2;
		public static final int ARRAY = 3;
		public static final int FUNCTION = 4;

		String nameType;
		int type;

		public GDBType(String n) {
			this(n, 0);
		}

		public GDBType(int t) {
			this("", t); //$NON-NLS-1$
		}

		public GDBType(String n, int t) {
			nameType = n;
			type = t;
		}

		@Override
		public String toString() {
			return unParse(this);
		}

		public String verbose() {
			return nameType;
		}

		public int getType() {
			return type;
		}

		public String getTypeName() {
			return nameType;
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

		@Override
		public String verbose() {
			StringBuilder sb = new StringBuilder();
			switch (getType()) {
			case FUNCTION:
				sb.append(" function returning ").append(hasChild() ? child.verbose() : ""); //$NON-NLS-1$//$NON-NLS-2$
				break;
			case ARRAY:
				sb.append(" array[").append(dimension).append("] of ").append(hasChild() ? child.verbose() : ""); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				break;
			case REFERENCE:
				sb.append(" reference to ").append(hasChild() ? child.verbose() : ""); //$NON-NLS-1$//$NON-NLS-2$
				break;
			case POINTER:
				sb.append(" pointer to ").append(hasChild() ? child.verbose() : ""); //$NON-NLS-1$//$NON-NLS-2$
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
		if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || c == ':' || c == ',') {
			return true;
		}
		return false;
	}

	// check is the character is alpha numeric
	// [a-zA-Z0-9]
	// GDB hack accept ':' ',' part of the GDB hacks
	// when doing ptype gdb returns "class A : public C { ..}"
	boolean isCIdentifierPart(int c) {
		if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || c == ':') {
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

	void insertingChild(int kind) {
		insertingChild(kind, 0);
	}

	/**
	 * @since 4.4
	 */
	protected void insertingChild(int kind, int d) {
		if (gdbDerivedType == null) {
			gdbDerivedType = createGDBDerivedType(genericType, kind, d);
		} else {
			GDBDerivedType dType = gdbDerivedType;
			GDBType gdbType = gdbDerivedType.getChild();
			while (gdbType instanceof GDBDerivedType) {
				dType = (GDBDerivedType) gdbType;
				gdbType = dType.getChild();
			}
			gdbType = createGDBDerivedType(gdbType, kind, d);
			dType.setChild(gdbType);
		}
	}

	// method returns the next token
	int getToken() {
		token = ""; //$NON-NLS-1$

		int c = getch();

		// Skip over any space
		while (isCSpace(c)) {
			c = getch();
		}

		//char character = (char) c;

		if (c == '(') {
			c = getch();
			if (c == ')') {
				token = "()"; //$NON-NLS-1$
				tokenType = PARENS;
			} else if (isCIdentifierStart(c)) {
				int i = 0;
				token += (char) c;
				while (i == 0 && c != ')') {
					if (c == EOF) {
						// Unbalanced parantheses.
						break;
					}
					c = getch();
					token += (char) c;
					if (c == '(') {
						++i;
					} else if (c == ')') {
						--i;
					}
				}
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
			StringBuilder sb = new StringBuilder();
			sb.append((char) c);
			while (isCIdentifierPart((c = getch())) && c != EOF) {
				sb.append((char) c);
			}
			if (c == '<') {
				// Swallow template args in types like "class foobar<A,B> : public C {..} *"
				// FIXME: if the bracket is not terminate do we throw exception?
				sb.append((char) c);
				int count = 1;
				do {
					c = getch();
					if (c == '<') {
						count++;
					} else if (c == '>') {
						count--;
					}
					if (c != ' ') {
						sb.append((char) c);
					}
				} while (count > 0 && c != EOF);
			} else if (c != EOF) {
				ungetch();
			}
			token = sb.toString();
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
			insertingChild(GDBType.POINTER);
		}
		while (namp-- > 0) {
			insertingChild(GDBType.REFERENCE);
		}
	}

	// parse a direct declarator
	void dirdcl() {
		int type;

		if (tokenType == '(') {
			dcl();
			if (tokenType != ')' /*&& !name.isEmpty()*/) {
				// Do we throw an exception on unterminated parentheses
				// It should have been handle by getToken()
				return;
			}
		} else if (tokenType == NAME) {
			// Useless we do not need the name of the variable
			name = " " + token; //$NON-NLS-1$
		} else if (tokenType == PARENS) {
			insertingChild(GDBType.FUNCTION);
		} else if (tokenType == BRACKETS) {
			int len = 0;
			if (!token.isEmpty()) {
				try {
					len = Integer.parseInt(token);
				} catch (NumberFormatException e) {
				}
			}
			insertingChild(GDBType.ARRAY, len);
		} else if (tokenType == '&') {
			insertingChild(GDBType.REFERENCE);
		} else {
			// oops bad declaration ?
			return;
		}

		while ((type = getToken()) == PARENS || type == BRACKETS) {
			if (type == PARENS) {
				insertingChild(GDBType.FUNCTION);
			} else { /* BRACKETS */
				int len = 0;
				if (!token.isEmpty()) {
					try {
						len = Integer.parseInt(token);
					} catch (NumberFormatException e) {
					}
				}
				insertingChild(GDBType.ARRAY, len);
			}
		}
	}

	/**
	 * @since 4.4
	 */
	protected GDBDerivedType createGDBDerivedType(GDBType c, int t, int dim) {
		return new GDBDerivedType(c, t, dim);
	}

	public static void main(String[] args) {

		GDBTypeParser parser = new GDBTypeParser();

		System.out.println("int *&"); //$NON-NLS-1$
		parser.parse("int *&"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (&rg)(int)"); //$NON-NLS-1$
		parser.parse("int (&rg)(int)"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (&ra)[3]"); //$NON-NLS-1$
		parser.parse("int (&rg)[3]"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("struct link { int i; int j; struct link * next;} *"); //$NON-NLS-1$
		parser.parse("struct link { int i; int j; struct link * next} *"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("class ns::link<8, ns::A> : public ns::B { int i; int j; struct link * next;} *"); //$NON-NLS-1$
		parser.parse("class ns::link<8, ns::A> : public ns::B { int i; int j; struct link * next;} *"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("char **argv"); //$NON-NLS-1$
		parser.parse("char **argv"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (*daytab)[13]"); //$NON-NLS-1$
		parser.parse("int (*daytab)[13]"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int *daytab[13]"); //$NON-NLS-1$
		parser.parse("int *daytab[13]"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("void *comp()"); //$NON-NLS-1$
		parser.parse("void *comp()"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("void (*comp)()"); //$NON-NLS-1$
		parser.parse("void (*comp)()"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (*func[15])()"); //$NON-NLS-1$
		parser.parse("int (*func[15])()"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("char (*(*x())[])()"); //$NON-NLS-1$
		parser.parse("char (*(*x())[])()"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("char (*(*x[3])())[5]"); //$NON-NLS-1$
		parser.parse("char (*(*x[3])())[5]"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("char *[5]"); //$NON-NLS-1$
		parser.parse("char *[5]"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int [2][3]"); //$NON-NLS-1$
		parser.parse("int [2][3]"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (int, char **)"); //$NON-NLS-1$
		parser.parse("int (int, char **)"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (int)"); //$NON-NLS-1$
		parser.parse("int (int)"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int (void)"); //$NON-NLS-1$
		parser.parse("int (void)"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

		System.out.println("int ()"); //$NON-NLS-1$
		parser.parse("int ()"); //$NON-NLS-1$
		System.out.println(GDBTypeParser.unParse(parser.getGDBType()));
		System.out.println(parser.getGDBType().verbose());
		System.out.println();

	}
}
