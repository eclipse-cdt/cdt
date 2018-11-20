/*******************************************************************************
 *  Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.gnu;

import static org.eclipse.cdt.core.parser.IToken.tAMPER;
import static org.eclipse.cdt.core.parser.IToken.tAMPERASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tAND;
import static org.eclipse.cdt.core.parser.IToken.tARROW;
import static org.eclipse.cdt.core.parser.IToken.tARROWSTAR;
import static org.eclipse.cdt.core.parser.IToken.tASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tBITCOMPLEMENT;
import static org.eclipse.cdt.core.parser.IToken.tBITOR;
import static org.eclipse.cdt.core.parser.IToken.tBITORASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tCHAR;
import static org.eclipse.cdt.core.parser.IToken.tCOLON;
import static org.eclipse.cdt.core.parser.IToken.tCOLONCOLON;
import static org.eclipse.cdt.core.parser.IToken.tCOMMA;
import static org.eclipse.cdt.core.parser.IToken.tCOMPLETION;
import static org.eclipse.cdt.core.parser.IToken.tDECR;
import static org.eclipse.cdt.core.parser.IToken.tDIV;
import static org.eclipse.cdt.core.parser.IToken.tDIVASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tDOT;
import static org.eclipse.cdt.core.parser.IToken.tDOTSTAR;
import static org.eclipse.cdt.core.parser.IToken.tELLIPSIS;
import static org.eclipse.cdt.core.parser.IToken.tEND_OF_INPUT;
import static org.eclipse.cdt.core.parser.IToken.tEOC;
import static org.eclipse.cdt.core.parser.IToken.tEQUAL;
import static org.eclipse.cdt.core.parser.IToken.tFLOATINGPT;
import static org.eclipse.cdt.core.parser.IToken.tGT;
import static org.eclipse.cdt.core.parser.IToken.tGTEQUAL;
import static org.eclipse.cdt.core.parser.IToken.tIDENTIFIER;
import static org.eclipse.cdt.core.parser.IToken.tINCR;
import static org.eclipse.cdt.core.parser.IToken.tINTEGER;
import static org.eclipse.cdt.core.parser.IToken.tLBRACE;
import static org.eclipse.cdt.core.parser.IToken.tLBRACKET;
import static org.eclipse.cdt.core.parser.IToken.tLCHAR;
import static org.eclipse.cdt.core.parser.IToken.tLPAREN;
import static org.eclipse.cdt.core.parser.IToken.tLSTRING;
import static org.eclipse.cdt.core.parser.IToken.tLT;
import static org.eclipse.cdt.core.parser.IToken.tLTEQUAL;
import static org.eclipse.cdt.core.parser.IToken.tMINUS;
import static org.eclipse.cdt.core.parser.IToken.tMINUSASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tMOD;
import static org.eclipse.cdt.core.parser.IToken.tMODASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tNOT;
import static org.eclipse.cdt.core.parser.IToken.tNOTEQUAL;
import static org.eclipse.cdt.core.parser.IToken.tOR;
import static org.eclipse.cdt.core.parser.IToken.tPLUS;
import static org.eclipse.cdt.core.parser.IToken.tPLUSASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tPOUND;
import static org.eclipse.cdt.core.parser.IToken.tQUESTION;
import static org.eclipse.cdt.core.parser.IToken.tRBRACE;
import static org.eclipse.cdt.core.parser.IToken.tRBRACKET;
import static org.eclipse.cdt.core.parser.IToken.tRPAREN;
import static org.eclipse.cdt.core.parser.IToken.tSEMI;
import static org.eclipse.cdt.core.parser.IToken.tSHIFTL;
import static org.eclipse.cdt.core.parser.IToken.tSHIFTLASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tSHIFTR;
import static org.eclipse.cdt.core.parser.IToken.tSHIFTRASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tSTAR;
import static org.eclipse.cdt.core.parser.IToken.tSTARASSIGN;
import static org.eclipse.cdt.core.parser.IToken.tSTRING;
import static org.eclipse.cdt.core.parser.IToken.tUNKNOWN_CHAR;
import static org.eclipse.cdt.core.parser.IToken.tUTF16CHAR;
import static org.eclipse.cdt.core.parser.IToken.tUTF16STRING;
import static org.eclipse.cdt.core.parser.IToken.tUTF32CHAR;
import static org.eclipse.cdt.core.parser.IToken.tUTF32STRING;
import static org.eclipse.cdt.core.parser.IToken.tXOR;
import static org.eclipse.cdt.core.parser.IToken.tXORASSIGN;
import static org.eclipse.cdt.core.parser.IToken.t__Complex;
import static org.eclipse.cdt.core.parser.IToken.t__Imaginary;
import static org.eclipse.cdt.core.parser.IToken.t_asm;
import static org.eclipse.cdt.core.parser.IToken.t_auto;
import static org.eclipse.cdt.core.parser.IToken.t_bool;
import static org.eclipse.cdt.core.parser.IToken.t_break;
import static org.eclipse.cdt.core.parser.IToken.t_case;
import static org.eclipse.cdt.core.parser.IToken.t_catch;
import static org.eclipse.cdt.core.parser.IToken.t_char;
import static org.eclipse.cdt.core.parser.IToken.t_class;
import static org.eclipse.cdt.core.parser.IToken.t_const;
import static org.eclipse.cdt.core.parser.IToken.t_const_cast;
import static org.eclipse.cdt.core.parser.IToken.t_continue;
import static org.eclipse.cdt.core.parser.IToken.t_default;
import static org.eclipse.cdt.core.parser.IToken.t_delete;
import static org.eclipse.cdt.core.parser.IToken.t_do;
import static org.eclipse.cdt.core.parser.IToken.t_double;
import static org.eclipse.cdt.core.parser.IToken.t_dynamic_cast;
import static org.eclipse.cdt.core.parser.IToken.t_else;
import static org.eclipse.cdt.core.parser.IToken.t_enum;
import static org.eclipse.cdt.core.parser.IToken.t_explicit;
import static org.eclipse.cdt.core.parser.IToken.t_export;
import static org.eclipse.cdt.core.parser.IToken.t_extern;
import static org.eclipse.cdt.core.parser.IToken.t_false;
import static org.eclipse.cdt.core.parser.IToken.t_float;
import static org.eclipse.cdt.core.parser.IToken.t_for;
import static org.eclipse.cdt.core.parser.IToken.t_friend;
import static org.eclipse.cdt.core.parser.IToken.t_goto;
import static org.eclipse.cdt.core.parser.IToken.t_if;
import static org.eclipse.cdt.core.parser.IToken.t_inline;
import static org.eclipse.cdt.core.parser.IToken.t_int;
import static org.eclipse.cdt.core.parser.IToken.t_long;
import static org.eclipse.cdt.core.parser.IToken.t_mutable;
import static org.eclipse.cdt.core.parser.IToken.t_namespace;
import static org.eclipse.cdt.core.parser.IToken.t_new;
import static org.eclipse.cdt.core.parser.IToken.t_operator;
import static org.eclipse.cdt.core.parser.IToken.t_private;
import static org.eclipse.cdt.core.parser.IToken.t_protected;
import static org.eclipse.cdt.core.parser.IToken.t_public;
import static org.eclipse.cdt.core.parser.IToken.t_register;
import static org.eclipse.cdt.core.parser.IToken.t_reinterpret_cast;
import static org.eclipse.cdt.core.parser.IToken.t_restrict;
import static org.eclipse.cdt.core.parser.IToken.t_return;
import static org.eclipse.cdt.core.parser.IToken.t_short;
import static org.eclipse.cdt.core.parser.IToken.t_signed;
import static org.eclipse.cdt.core.parser.IToken.t_sizeof;
import static org.eclipse.cdt.core.parser.IToken.t_static;
import static org.eclipse.cdt.core.parser.IToken.t_static_cast;
import static org.eclipse.cdt.core.parser.IToken.t_struct;
import static org.eclipse.cdt.core.parser.IToken.t_switch;
import static org.eclipse.cdt.core.parser.IToken.t_template;
import static org.eclipse.cdt.core.parser.IToken.t_this;
import static org.eclipse.cdt.core.parser.IToken.t_throw;
import static org.eclipse.cdt.core.parser.IToken.t_true;
import static org.eclipse.cdt.core.parser.IToken.t_try;
import static org.eclipse.cdt.core.parser.IToken.t_typedef;
import static org.eclipse.cdt.core.parser.IToken.t_typeid;
import static org.eclipse.cdt.core.parser.IToken.t_typename;
import static org.eclipse.cdt.core.parser.IToken.t_union;
import static org.eclipse.cdt.core.parser.IToken.t_unsigned;
import static org.eclipse.cdt.core.parser.IToken.t_using;
import static org.eclipse.cdt.core.parser.IToken.t_virtual;
import static org.eclipse.cdt.core.parser.IToken.t_void;
import static org.eclipse.cdt.core.parser.IToken.t_volatile;
import static org.eclipse.cdt.core.parser.IToken.t_wchar_t;
import static org.eclipse.cdt.core.parser.IToken.t_while;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_And;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_AndAnd;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_AndAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Arrow;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_ArrowStar;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Assign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Bang;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Caret;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_CaretAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Colon;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_ColonColon;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Comma;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Completion;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Dot;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_DotDotDot;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_DotStar;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_EOF_TOKEN;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_EQ;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_EndOfCompletion;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_GE;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_GT;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Invalid;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LE;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LT;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LeftBrace;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LeftBracket;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LeftParen;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LeftShift;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_LeftShiftAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_MAX;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_MIN;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Minus;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_MinusAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_MinusMinus;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_NE;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Or;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_OrAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_OrOr;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Percent;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_PercentAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Plus;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_PlusAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_PlusPlus;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Question;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_RightBrace;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_RightBracket;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_RightParen;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_RightShift;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_RightShiftAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_SemiColon;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Slash;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_SlashAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Star;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_StarAssign;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_Tilde;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK__Complex;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK__Imaginary;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK___alignof__;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK___attribute__;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK___declspec;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_asm;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_auto;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_bool;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_break;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_case;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_catch;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_char;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_charconst;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_class;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_const;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_const_cast;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_continue;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_default;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_delete;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_do;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_double;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_dynamic_cast;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_else;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_enum;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_explicit;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_export;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_extern;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_false;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_float;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_floating;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_for;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_friend;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_goto;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_identifier;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_if;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_inline;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_int;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_integer;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_long;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_mutable;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_namespace;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_new;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_operator;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_private;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_protected;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_public;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_register;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_reinterpret_cast;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_restrict;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_return;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_short;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_signed;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_sizeof;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_static;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_static_cast;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_stringlit;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_struct;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_switch;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_template;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_this;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_throw;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_true;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_try;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_typedef;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_typeid;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_typename;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_typeof;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_union;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_unsigned;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_using;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_virtual;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_void;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_volatile;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_wchar_t;
import static org.eclipse.cdt.internal.core.dom.lrparser.gpp.GPPParsersym.TK_while;

import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IToken;

/**
 * Maps tokens types returned by CPreprocessor to token types
 * expected by the C++ parser.
 *
 * @author Mike Kucera
 */
public class DOMToGPPTokenMap implements IDOMTokenMap {

	public static final DOMToGPPTokenMap DEFAULT_MAP = new DOMToGPPTokenMap();

	private DOMToGPPTokenMap() {
		// just a private constructor
	}

	@Override
	public int getEOFTokenKind() {
		return TK_EOF_TOKEN;
	}

	@Override
	public int getEOCTokenKind() {
		return TK_EndOfCompletion;
	}

	@Override
	public int mapKind(IToken token) {

		switch (token.getType()) {
		case tIDENTIFIER:
			return TK_identifier;
		case tINTEGER:
			return TK_integer;
		case tCOLONCOLON:
			return TK_ColonColon;
		case tCOLON:
			return TK_Colon;
		case tSEMI:
			return TK_SemiColon;
		case tCOMMA:
			return TK_Comma;
		case tQUESTION:
			return TK_Question;
		case tLPAREN:
			return TK_LeftParen;
		case tRPAREN:
			return TK_RightParen;
		case tLBRACKET:
			return TK_LeftBracket;
		case tRBRACKET:
			return TK_RightBracket;
		case tLBRACE:
			return TK_LeftBrace;
		case tRBRACE:
			return TK_RightBrace;
		case tPLUSASSIGN:
			return TK_PlusAssign;
		case tINCR:
			return TK_PlusPlus;
		case tPLUS:
			return TK_Plus;
		case tMINUSASSIGN:
			return TK_MinusAssign;
		case tDECR:
			return TK_MinusMinus;
		case tARROWSTAR:
			return TK_ArrowStar;
		case tARROW:
			return TK_Arrow;
		case tMINUS:
			return TK_Minus;
		case tSTARASSIGN:
			return TK_StarAssign;
		case tSTAR:
			return TK_Star;
		case tMODASSIGN:
			return TK_PercentAssign;
		case tMOD:
			return TK_Percent;
		case tXORASSIGN:
			return TK_CaretAssign;
		case tXOR:
			return TK_Caret;
		case tAMPERASSIGN:
			return TK_AndAssign;
		case tAND:
			return TK_AndAnd;
		case tAMPER:
			return TK_And;
		case tBITORASSIGN:
			return TK_OrAssign;
		case tOR:
			return TK_OrOr;
		case tBITOR:
			return TK_Or;
		case tBITCOMPLEMENT:
			return TK_Tilde;
		case tNOTEQUAL:
			return TK_NE;
		case tNOT:
			return TK_Bang;
		case tEQUAL:
			return TK_EQ;
		case tASSIGN:
			return TK_Assign;
		case tUNKNOWN_CHAR:
			return TK_Invalid;
		case tSHIFTL:
			return TK_LeftShift;
		case tLTEQUAL:
			return TK_LE;
		case tLT:
			return TK_LT;
		case tSHIFTRASSIGN:
			return TK_RightShiftAssign;
		case tSHIFTR:
			return TK_RightShift;
		case tGTEQUAL:
			return TK_GE;
		case tGT:
			return TK_GT;
		case tSHIFTLASSIGN:
			return TK_LeftShiftAssign;
		case tELLIPSIS:
			return TK_DotDotDot;
		case tDOTSTAR:
			return TK_DotStar;
		case tDOT:
			return TK_Dot;
		case tDIVASSIGN:
			return TK_SlashAssign;
		case tDIV:
			return TK_Slash;

		case t_asm:
			return TK_asm;
		case t_auto:
			return TK_auto;
		case t_bool:
			return TK_bool;
		case t_break:
			return TK_break;
		case t_case:
			return TK_case;
		case t_catch:
			return TK_catch;
		case t_char:
			return TK_char;
		case t_class:
			return TK_class;
		case t_const:
			return TK_const;
		case t_const_cast:
			return TK_const_cast;
		case t_continue:
			return TK_continue;
		case t_default:
			return TK_default;
		case t_delete:
			return TK_delete;
		case t_do:
			return TK_do;
		case t_double:
			return TK_double;
		case t_dynamic_cast:
			return TK_dynamic_cast;
		case t_else:
			return TK_else;
		case t_enum:
			return TK_enum;
		case t_explicit:
			return TK_explicit;
		case t_export:
			return TK_export;
		case t_extern:
			return TK_extern;
		case t_false:
			return TK_false;
		case t_float:
			return TK_float;
		case t_for:
			return TK_for;
		case t_friend:
			return TK_friend;
		case t_goto:
			return TK_goto;
		case t_if:
			return TK_if;
		case t_inline:
			return TK_inline;
		case t_int:
			return TK_int;
		case t_long:
			return TK_long;
		case t_mutable:
			return TK_mutable;
		case t_namespace:
			return TK_namespace;
		case t_new:
			return TK_new;
		case t_operator:
			return TK_operator;
		case t_private:
			return TK_private;
		case t_protected:
			return TK_protected;
		case t_public:
			return TK_public;
		case t_register:
			return TK_register;
		case t_reinterpret_cast:
			return TK_reinterpret_cast;
		case t_return:
			return TK_return;
		case t_short:
			return TK_short;
		case t_sizeof:
			return TK_sizeof;
		case t_static:
			return TK_static;
		case t_static_cast:
			return TK_static_cast;
		case t_signed:
			return TK_signed;
		case t_struct:
			return TK_struct;
		case t_switch:
			return TK_switch;
		case t_template:
			return TK_template;
		case t_this:
			return TK_this;
		case t_throw:
			return TK_throw;
		case t_true:
			return TK_true;
		case t_try:
			return TK_try;
		case t_typedef:
			return TK_typedef;
		case t_typeid:
			return TK_typeid;
		case t_typename:
			return TK_typename;
		case t_union:
			return TK_union;
		case t_unsigned:
			return TK_unsigned;
		case t_using:
			return TK_using;
		case t_virtual:
			return TK_virtual;
		case t_void:
			return TK_void;
		case t_volatile:
			return TK_volatile;
		case t_wchar_t:
			return TK_wchar_t;
		case t_while:
			return TK_while;

		case tFLOATINGPT:
			return TK_floating;
		case tSTRING:
			return TK_stringlit;
		case tLSTRING:
			return TK_stringlit;
		case tUTF16STRING:
			return TK_stringlit;
		case tUTF32STRING:
			return TK_stringlit;
		case tUTF16CHAR:
			return TK_charconst;
		case tUTF32CHAR:
			return TK_charconst;
		case tCHAR:
			return TK_charconst;
		case tLCHAR:
			return TK_charconst;
		case tCOMPLETION:
			return TK_Completion;
		case tEOC:
			return TK_EndOfCompletion;
		case tEND_OF_INPUT:
			return TK_EOF_TOKEN;

		case IGCCToken.t_typeof:
			return TK_typeof;
		case IGCCToken.t___alignof__:
			return TK___alignof__;
		case IGCCToken.tMAX:
			return TK_MAX;
		case IGCCToken.tMIN:
			return TK_MIN;
		case IGCCToken.t__attribute__:
			return TK___attribute__;
		case IGCCToken.t__declspec:
			return TK___declspec;

		// GNU supports these but they are not in the C++ spec
		case t__Complex:
			return TK__Complex;
		case t__Imaginary:
			return TK__Imaginary;
		case t_restrict:
			return TK_restrict;

		case tPOUND:
			return TK_Invalid;

		default:
			assert false : "token not recognized by the GPP parser: " + token.getType(); //$NON-NLS-1$
			return TK_Invalid;
		}
	}

}
