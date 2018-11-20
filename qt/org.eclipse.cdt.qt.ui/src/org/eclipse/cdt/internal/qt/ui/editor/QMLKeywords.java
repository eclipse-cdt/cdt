/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

/**
 * Constants for the various keywords used by QML and ECMA Script
 */
public class QMLKeywords {
	// QML reserved words
	public static final String IMPORT = "import"; //$NON-NLS-1$
	public static final String CONST = "const"; //$NON-NLS-1$
	public static final String PROPERTY = "property"; //$NON-NLS-1$
	public static final String READONLY = "readonly"; //$NON-NLS-1$
	public static final String SIGNAL = "signal"; //$NON-NLS-1$
	public static final String AS = "as"; //$NON-NLS-1$
	public static final String ON = "on"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String FALSE = "false"; //$NON-NLS-1$

	// QML future reserved words
	public static final String TRANSIENT = "transient"; //$NON-NLS-1$
	public static final String SYNCHRONIZED = "synchronized"; //$NON-NLS-1$
	public static final String ABSTRACT = "abstract"; //$NON-NLS-1$
	public static final String VOLATILE = "volatile"; //$NON-NLS-1$
	public static final String NATIVE = "native"; //$NON-NLS-1$
	public static final String GOTO = "goto"; //$NON-NLS-1$
	public static final String BYTE = "byte"; //$NON-NLS-1$
	public static final String LONG = "long"; //$NON-NLS-1$
	public static final String CHAR = "char"; //$NON-NLS-1$
	public static final String SHORT = "short"; //$NON-NLS-1$
	public static final String FLOAT = "float"; //$NON-NLS-1$

	// QML basic types
	public static final String BOOLEAN = "boolean"; //$NON-NLS-1$
	public static final String DOUBLE = "double"; //$NON-NLS-1$
	public static final String INT = "int"; //$NON-NLS-1$
	public static final String LIST = "list"; //$NON-NLS-1$
	public static final String COLOR = "color"; //$NON-NLS-1$
	public static final String REAL = "real"; //$NON-NLS-1$
	public static final String STRING = "string"; //$NON-NLS-1$
	public static final String URL = "url"; //$NON-NLS-1$
	public static final String VAR = "var"; //$NON-NLS-1$

	// ECMA Script reserved words
	public static final String BREAK = "break"; //$NON-NLS-1$
	public static final String DO = "do"; //$NON-NLS-1$
	public static final String INSTANCEOF = "instanceof"; //$NON-NLS-1$
	public static final String TYPEOF = "typeof"; //$NON-NLS-1$
	public static final String CASE = "case"; //$NON-NLS-1$
	public static final String ELSE = "else"; //$NON-NLS-1$
	public static final String NEW = "new"; //$NON-NLS-1$
	public static final String CATCH = "catch"; //$NON-NLS-1$
	public static final String FINALLY = "finally"; //$NON-NLS-1$
	public static final String RETURN = "return"; //$NON-NLS-1$
	public static final String VOID = "void"; //$NON-NLS-1$
	public static final String CONTINUE = "continue"; //$NON-NLS-1$
	public static final String FOR = "for"; //$NON-NLS-1$
	public static final String SWITCH = "switch"; //$NON-NLS-1$
	public static final String WHILE = "while"; //$NON-NLS-1$
	public static final String DEBUGGER = "debugger"; //$NON-NLS-1$
	public static final String FUNCTION = "function"; //$NON-NLS-1$
	public static final String THIS = "this"; //$NON-NLS-1$
	public static final String WITH = "with"; //$NON-NLS-1$
	public static final String DEFAULT = "default"; //$NON-NLS-1$
	public static final String IF = "if"; //$NON-NLS-1$
	public static final String THROW = "throw"; //$NON-NLS-1$
	public static final String DELETE = "delete"; //$NON-NLS-1$
	public static final String IN = "in"; //$NON-NLS-1$
	public static final String TRY = "try"; //$NON-NLS-1$

	// ECMAScript future reserved words
	public static final String CLASS = "class"; //$NON-NLS-1$
	public static final String ENUM = "enum"; //$NON-NLS-1$
	public static final String EXTENDS = "extends"; //$NON-NLS-1$
	public static final String SUPER = "super"; //$NON-NLS-1$
	public static final String EXPORT = "export"; //$NON-NLS-1$

	// ECMA Script strict-mode future reserved words
	public static final String IMPLEMENTS = "implements"; //$NON-NLS-1$
	public static final String LET = "let"; //$NON-NLS-1$
	public static final String PRIVATE = "private"; //$NON-NLS-1$
	public static final String PUBLIC = "public"; //$NON-NLS-1$
	public static final String YIELD = "yield"; //$NON-NLS-1$
	public static final String INTERFACE = "interface"; //$NON-NLS-1$
	public static final String PACKAGE = "package"; //$NON-NLS-1$
	public static final String PROTECTED = "protected"; //$NON-NLS-1$
	public static final String STATIC = "static"; //$NON-NLS-1$

	/**
	 * Gets an array containing all of the QML and ECMA Script keywords.
	 *
	 * @param strictMode
	 *            Whether or not ECMA Script strict mode is enabled. If <code>true</code>, this adds several reserved keywords to
	 *            the list.
	 * @return An array of keywords
	 */
	public static final String[] getKeywords(boolean strictMode) {
		if (!strictMode) {
			return new String[] { IMPORT, CONST, PROPERTY, READONLY, SIGNAL, AS, ON, TRUE, FALSE, TRANSIENT,
					SYNCHRONIZED, ABSTRACT, VOLATILE, NATIVE, GOTO, BYTE, LONG, CHAR, SHORT, FLOAT, BOOLEAN, DOUBLE,
					INT, LIST, COLOR, REAL, STRING, URL, VAR, BREAK, DO, INSTANCEOF, TYPEOF, CASE, ELSE, NEW, CATCH,
					FINALLY, RETURN, VOID, CONTINUE, FOR, SWITCH, WHILE, DEBUGGER, FUNCTION, THIS, WITH, DEFAULT, IF,
					THROW, DELETE, IN, TRY, CLASS, ENUM, EXTENDS, SUPER, EXPORT };
		} else {
			return new String[] { IMPORT, CONST, PROPERTY, READONLY, SIGNAL, AS, ON, TRUE, FALSE, TRANSIENT,
					SYNCHRONIZED, ABSTRACT, VOLATILE, NATIVE, GOTO, BYTE, LONG, CHAR, SHORT, FLOAT, BOOLEAN, DOUBLE,
					INT, LIST, COLOR, REAL, STRING, URL, VAR, BREAK, DO, INSTANCEOF, TYPEOF, CASE, ELSE, NEW, CATCH,
					FINALLY, RETURN, VOID, CONTINUE, FOR, SWITCH, WHILE, DEBUGGER, FUNCTION, THIS, WITH, DEFAULT, IF,
					THROW, DELETE, IN, TRY, CLASS, ENUM, EXTENDS, SUPER, EXPORT, IMPLEMENTS, LET, PRIVATE, PUBLIC,
					YIELD, INTERFACE, PACKAGE, PROTECTED, STATIC };
		}
	}
}
