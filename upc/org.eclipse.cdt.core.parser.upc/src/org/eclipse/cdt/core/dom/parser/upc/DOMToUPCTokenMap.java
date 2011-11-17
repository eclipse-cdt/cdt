/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.upc;


import static org.eclipse.cdt.core.parser.IToken.*;
import static org.eclipse.cdt.internal.core.dom.parser.upc.UPCParsersym.*;

import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.parser.IToken;


/**
 * Maps token kinds from CPreprocessor to the tokens kinds
 * created by LPG.
 *
 * TODO UPC keywords need to be syntax highlighted
 *
 * @author Mike Kucera
 */
@SuppressWarnings("nls")
public class DOMToUPCTokenMap implements IDOMTokenMap {


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

		switch(token.getType()) {
			case tIDENTIFIER :
				Integer keywordKind = UPCKeyword.getTokenKind(token.getCharImage());
				return keywordKind == null ? TK_identifier : keywordKind;

			case tINTEGER      : return TK_integer;
			case tCOLON        : return TK_Colon;
			case tSEMI         : return TK_SemiColon;
			case tCOMMA        : return TK_Comma;
			case tQUESTION     : return TK_Question;
			case tLPAREN       : return TK_LeftParen;
			case tRPAREN       : return TK_RightParen;
			case tLBRACKET     : return TK_LeftBracket;
			case tRBRACKET     : return TK_RightBracket;
			case tLBRACE       : return TK_LeftBrace;
			case tRBRACE       : return TK_RightBrace;
			case tPLUSASSIGN   : return TK_PlusAssign;
			case tINCR         : return TK_PlusPlus;
			case tPLUS         : return TK_Plus;
			case tMINUSASSIGN  : return TK_MinusAssign;
			case tDECR         : return TK_MinusMinus;
			case tARROW        : return TK_Arrow;
			case tMINUS        : return TK_Minus;
			case tSTARASSIGN   : return TK_StarAssign;
			case tSTAR         : return TK_Star;
			case tMODASSIGN    : return TK_PercentAssign;
			case tMOD          : return TK_Percent;
			case tXORASSIGN    : return TK_CaretAssign;
			case tXOR          : return TK_Caret;
			case tAMPERASSIGN  : return TK_AndAssign;
			case tAND          : return TK_AndAnd;
			case tAMPER        : return TK_And;
			case tBITORASSIGN  : return TK_OrAssign;
			case tOR           : return TK_OrOr;
			case tBITOR        : return TK_Or;
			case tBITCOMPLEMENT: return TK_Tilde;
			case tNOTEQUAL     : return TK_NE;
			case tNOT          : return TK_Bang;
			case tEQUAL        : return TK_EQ;
			case tASSIGN       : return TK_Assign;
			case tUNKNOWN_CHAR : return TK_Invalid;
			case tSHIFTL       : return TK_LeftShift;
			case tLTEQUAL      : return TK_LE;
			case tLT           : return TK_LT;
			case tSHIFTRASSIGN : return TK_RightShiftAssign;
			case tSHIFTR       : return TK_RightShift;
			case tGTEQUAL      : return TK_GE;
			case tGT           : return TK_GT;
			case tSHIFTLASSIGN : return TK_LeftShiftAssign;
			case tELLIPSIS     : return TK_DotDotDot;
			case tDOT          : return TK_Dot;
			case tDIVASSIGN    : return TK_SlashAssign;
			case tDIV          : return TK_Slash;

			case t_auto        : return TK_auto;
			case t_break       : return TK_break;
			case t_case        : return TK_case;
			case t_char        : return TK_char;
			case t_const       : return TK_const;
			case t_continue    : return TK_continue;
			case t_default     : return TK_default;
			case t_do          : return TK_do;
			case t_double      : return TK_double;
			case t_else        : return TK_else;
			case t_enum        : return TK_enum;
			case t_extern      : return TK_extern;
			case t_float       : return TK_float;
			case t_for         : return TK_for;
			case t_goto        : return TK_goto;
			case t_if          : return TK_if;
			case t_inline      : return TK_inline;
			case t_int         : return TK_int;
			case t_long        : return TK_long;
			case t_register    : return TK_register;
			case t_return      : return TK_return;
			case t_short       : return TK_short;
			case t_sizeof      : return TK_sizeof;
			case t_static      : return TK_static;
			case t_signed      : return TK_signed;
			case t_struct      : return TK_struct;
			case t_switch      : return TK_switch;
			case t_typedef     : return TK_typedef;
			case t_union       : return TK_union;
			case t_unsigned    : return TK_unsigned;
			case t_void        : return TK_void;
			case t_volatile    : return TK_volatile;
			case t_while       : return TK_while;
			case tFLOATINGPT   : return TK_floating;
			case tSTRING       : return TK_stringlit;
			case tLSTRING      : return TK_stringlit;
			case tUTF16STRING  : return TK_stringlit;
			case tUTF32STRING  : return TK_stringlit;
			case tCHAR         : return TK_charconst;
			case tLCHAR        : return TK_charconst;
	        case tUTF16CHAR    : return TK_charconst;
	        case tUTF32CHAR    : return TK_charconst;
			case t__Bool       : return TK__Bool;
			case t__Complex    : return TK__Complex;
			case t__Imaginary  : return TK__Imaginary;
			case t_restrict    : return TK_restrict;
			case tCOMPLETION   : return TK_Completion;
			case tEOC          : return TK_EndOfCompletion;
			case tEND_OF_INPUT : return TK_EOF_TOKEN;

			default:
				assert false : "token not recognized by the UPC parser: " + token.getType();
				return TK_Invalid;
		}
	}



}
