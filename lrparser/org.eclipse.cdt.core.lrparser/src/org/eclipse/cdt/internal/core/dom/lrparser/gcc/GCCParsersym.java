/*******************************************************************************
* Copyright (c) 2006, 2010 IBM Corporation and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG

package org.eclipse.cdt.internal.core.dom.lrparser.gcc;

public interface GCCParsersym {
	public final static int TK_auto = 29, TK_break = 38, TK_case = 39, TK_char = 49, TK_const = 24, TK_continue = 40,
			TK_default = 41, TK_do = 42, TK_double = 50, TK_else = 99, TK_enum = 62, TK_extern = 30, TK_float = 51,
			TK_for = 43, TK_goto = 44, TK_if = 45, TK_inline = 31, TK_int = 52, TK_long = 53, TK_register = 32,
			TK_restrict = 27, TK_return = 46, TK_short = 54, TK_signed = 55, TK_sizeof = 17, TK_static = 28,
			TK_struct = 63, TK_switch = 47, TK_typedef = 33, TK_union = 64, TK_unsigned = 56, TK_void = 57,
			TK_volatile = 25, TK_while = 37, TK__Bool = 58, TK__Complex = 59, TK__Imaginary = 60, TK_integer = 18,
			TK_floating = 19, TK_charconst = 20, TK_stringlit = 13, TK_identifier = 2, TK_Completion = 5,
			TK_EndOfCompletion = 3, TK_Invalid = 100, TK_LeftBracket = 35, TK_LeftParen = 1, TK_LeftBrace = 10,
			TK_Dot = 70, TK_Arrow = 85, TK_PlusPlus = 15, TK_MinusMinus = 16, TK_And = 14, TK_Star = 6, TK_Plus = 11,
			TK_Minus = 12, TK_Tilde = 21, TK_Bang = 22, TK_Slash = 71, TK_Percent = 72, TK_RightShift = 66,
			TK_LeftShift = 67, TK_LT = 73, TK_GT = 74, TK_LE = 75, TK_GE = 76, TK_EQ = 80, TK_NE = 81, TK_Caret = 82,
			TK_Or = 83, TK_AndAnd = 84, TK_OrOr = 86, TK_Question = 87, TK_Colon = 61, TK_DotDotDot = 69,
			TK_Assign = 68, TK_StarAssign = 88, TK_SlashAssign = 89, TK_PercentAssign = 90, TK_PlusAssign = 91,
			TK_MinusAssign = 92, TK_RightShiftAssign = 93, TK_LeftShiftAssign = 94, TK_AndAssign = 95,
			TK_CaretAssign = 96, TK_OrAssign = 97, TK_Comma = 48, TK_RightBracket = 77, TK_RightParen = 36,
			TK_RightBrace = 65, TK_SemiColon = 26, TK_typeof = 9, TK___alignof__ = 23, TK___attribute__ = 7,
			TK___declspec = 8, TK_MAX = 78, TK_MIN = 79, TK_asm = 4, TK_ERROR_TOKEN = 34, TK_EOF_TOKEN = 98;

	public final static String orderedTerminalSymbols[] = { "", "LeftParen", "identifier", "EndOfCompletion", "asm",
			"Completion", "Star", "__attribute__", "__declspec", "typeof", "LeftBrace", "Plus", "Minus", "stringlit",
			"And", "PlusPlus", "MinusMinus", "sizeof", "integer", "floating", "charconst", "Tilde", "Bang",
			"__alignof__", "const", "volatile", "SemiColon", "restrict", "static", "auto", "extern", "inline",
			"register", "typedef", "ERROR_TOKEN", "LeftBracket", "RightParen", "while", "break", "case", "continue",
			"default", "do", "for", "goto", "if", "return", "switch", "Comma", "char", "double", "float", "int", "long",
			"short", "signed", "unsigned", "void", "_Bool", "_Complex", "_Imaginary", "Colon", "enum", "struct",
			"union", "RightBrace", "RightShift", "LeftShift", "Assign", "DotDotDot", "Dot", "Slash", "Percent", "LT",
			"GT", "LE", "GE", "RightBracket", "MAX", "MIN", "EQ", "NE", "Caret", "Or", "AndAnd", "Arrow", "OrOr",
			"Question", "StarAssign", "SlashAssign", "PercentAssign", "PlusAssign", "MinusAssign", "RightShiftAssign",
			"LeftShiftAssign", "AndAssign", "CaretAssign", "OrAssign", "EOF_TOKEN", "else", "Invalid" };

	public final static boolean isValidForParser = true;
}
