/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Matthias Spycher (matthias@coware.com) - bug 124966
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.util.regex.Pattern;

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
		s = Pattern.compile("\\bconst\\b").matcher(s).replaceAll("");  //$NON-NLS-1$//$NON-NLS-2$
		s = Pattern.compile("\\bvolatile\\b").matcher(s).replaceAll("");  //$NON-NLS-1$//$NON-NLS-2$
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
                column = dataType.indexOf(':', column+2);
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

	public static String unParse (GDBType gdbType) {

		StringBuffer sb = new StringBuffer();
		// Fetch the datatype.
		while (gdbType != null) {
			GDBDerivedType derived = null;
			int type = gdbType.getType();
			if (gdbType instanceof GDBDerivedType) {
				derived = (GDBDerivedType)gdbType;
				gdbType = derived.getChild();
				// respect the precedence of operators.
				if (type == GDBType.FUNCTION) {
					sb.append("()"); //$NON-NLS-1$
				} else if (type == GDBType.ARRAY) {
					sb.append('[').append(derived.getDimension()).append(']');
				} else if (type == GDBType.POINTER) {
					int childType = (gdbType != null) ? gdbType.getType() : GDBType.GENERIC; 
					if (childType == GDBType.POINTER || childType == GDBType.REFERENCE) {
						sb.append('*');
					} else if (childType == GDBType.GENERIC) {
						sb.insert(0, '*');
					} else {
						sb.insert(0, "(*").append(')'); //$NON-NLS-1$
					}
				} else if (type == GDBType.REFERENCE) {
					int childType = (gdbType != null) ? gdbType.getType() : GDBType.GENERIC; 
					if (childType == GDBType.POINTER || childType == GDBType.REFERENCE) {
						sb.append("&"); //$NON-NLS-1$
					} else if (childType == GDBType.GENERIC) {
						sb.insert(0, '&');
					} else {
						sb.insert(0, "(&").append(')'); //$NON-NLS-1$
					}
				}
			} else {
				sb.insert(0, ' ');
				sb.insert(0, gdbType.nameType);
				gdbType = null;
			}
		}
		return sb.toString().trim();

	}

	public class GDBType {
		public final static int GENERIC = 0;
		public final static int POINTER = 1;
		public final static int REFERENCE = 2;
		public final static int ARRAY = 3;
		public final static int FUNCTION = 4;

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
			StringBuffer sb = new StringBuffer();
			switch (getType()) {
				case FUNCTION :
					sb.append(" function returning " + (hasChild() ? child.verbose() : ""));  //$NON-NLS-1$//$NON-NLS-2$
					break;
				case ARRAY :
					sb.append(" array[" + dimension + "]" + " of " + (hasChild() ? child.verbose() : ""));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					break;
				case REFERENCE :
					sb.append(" reference to " + (hasChild() ? child.verbose() : ""));  //$NON-NLS-1$//$NON-NLS-2$
					break;
				case POINTER :
					sb.append(" pointer to " + (hasChild() ? child.verbose() : ""));  //$NON-NLS-1$//$NON-NLS-2$
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

	void insertingChild(int kind, int d) {
		if (gdbDerivedType == null) {
			gdbDerivedType = new GDBDerivedType(genericType, kind, d);
		} else {
			GDBDerivedType dType = gdbDerivedType;
			GDBType gdbType = gdbDerivedType.getChild();
			while (gdbType instanceof GDBDerivedType) {
				dType = (GDBDerivedType)gdbType;
				gdbType = dType.getChild();
			}				
			gdbType = new GDBDerivedType(gdbType, kind, d);
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
				token += (char)c;
				while (i == 0 && c != ')') {
					if (c == EOF) {
						// Unbalanced parantheses.
						break;
					}
					c = getch();
					token += (char)c;
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
            StringBuffer sb = new StringBuffer();
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
                    	sb.append((char)c);
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
			if (tokenType != ')' /*&& name.length() > 0*/) {
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
			if (token.length() > 0) {
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
				if (token.length() > 0) {
					try {
						len = Integer.parseInt(token);
					} catch (NumberFormatException e) {
					}
				}
				insertingChild(GDBType.ARRAY, len);
			}
		}
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
