/*******************************************************************************
 * Copyright (c) 2002, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IToken {
	// getters
	public int getType();
	public String getImage();
	public char[] getCharImage();
	public int getOffset();
	public int getLength();
	public int getEndOffset();
	public IToken getNext();

	public void setNext(IToken t);
	public void setType(int i);

	// Token types
	int FIRST_RESERVED_PREPROCESSOR= -200;
	int LAST_RESERVED_PREPROCESSOR= -101;
	int FIRST_RESERVED_SCANNER= -100;
	int LAST_RESERVED_SCANNER= -1;

	/** @since 5.2 */
	int t_PRAGMA = 5200;

	int tIDENTIFIER = 1;
	int tINTEGER = 2;
	int tCOLONCOLON = 3;
	int tCOLON = 4;
	int tSEMI = 5;
	int tCOMMA = 6;
	int tQUESTION = 7;
	int tLPAREN = 8;
	int tRPAREN = 9;
	int tLBRACKET = 10;
	int tRBRACKET = 11;
	int tLBRACE = 12;
	int tRBRACE = 13;
	int tPLUSASSIGN = 14;
	int tINCR = 15;
	int tPLUS = 16;
	int tMINUSASSIGN = 17;
	int tDECR = 18;
	int tARROWSTAR = 19;
	int tARROW = 20;
	int tMINUS = 21;
	int tSTARASSIGN = 22;
	int tSTAR = 23;
	int tMODASSIGN = 24;
	int tMOD = 25;
	int tXORASSIGN = 26;
	int tXOR = 27;
	int tAMPERASSIGN = 28;
	int tAND = 29;
	int tAMPER = 30;
	int tBITORASSIGN = 31;
	int tOR = 32;
	int tBITOR = 33;
	int tBITCOMPLEMENT = 34;
	int tNOTEQUAL = 35;
	int tNOT = 36;
	int tEQUAL = 37;
	int tASSIGN = 38;
	int tUNKNOWN_CHAR= 39;
	int tSHIFTL = 40;
	int tLTEQUAL = 41;
	int tLT = 42;
	int tSHIFTRASSIGN = 43;
	int tSHIFTR = 44;
	int tGTEQUAL = 45;
	int tGT = 46;
	int tSHIFTLASSIGN = 47;
	int tELLIPSIS = 48;
	int tDOTSTAR = 49;
	int tDOT = 50;
	int tDIVASSIGN = 51;
	int tDIV = 52;
	/**
	 * @see IScanner#setSplitShiftROperator(boolean)
	 * @since 5.2
	 */
	int tGT_in_SHIFTR= 5201;

	/** @since 5.10*/ int t_alignas = 5900;
	/** @since 5.10*/ int t_alignof = 5901;
	/** @deprecated use {@link #tAND} */ @Deprecated int t_and = 54;
	/** @deprecated use {@link #tAMPERASSIGN} */ @Deprecated int t_and_eq = 55;
	int t_asm = 56;
	int t_auto = 57;
	/** @deprecated use {@link #tAMPER} */ @Deprecated int t_bitand = 58;
	/** @deprecated use {@link #tBITOR} */ @Deprecated int t_bitor = 59;
	int t_bool = 60;
	int t_break = 61;
	int t_case = 62;
	int t_catch = 63;
	int t_char = 64;
	/** @since 5.2 */ int t_char16_t= 5202;
	/** @since 5.2 */ int t_char32_t= 5203;
	int t_class = 65;
	/** @deprecated use {@link #tBITCOMPLEMENT} */ @Deprecated int tCOMPL= tBITCOMPLEMENT;
	/** @deprecated use {@link #tBITCOMPLEMENT} */ @Deprecated int t_compl = 66;
	int t_const = 67;

	/** @since 5.4 */ int t_constexpr = 5400;
	int t_const_cast = 69;
	int t_continue = 70;
	/** @since 5.2 */ int t_decltype= 5204;
	int t_default = 71;
	int t_delete = 72;
	int t_do = 73;
	int t_double = 74;
	int t_dynamic_cast = 75;
	int t_else = 76;
	int t_enum = 77;
	int t_explicit = 78;
	int t_export = 79;
	int t_extern = 80;
	int t_false = 81;
	int t_float = 82;
	int t_for = 83;
	int t_friend = 84;
	int t_goto = 85;
	int t_if = 86;
	int t_inline = 87;
	int t_int = 88;
	int t_long = 89;
	int t_mutable = 90;
	int t_namespace = 91;
	int t_new = 92;
	/** @since 5.4 */ int t_noexcept = 5401;
	/** @since 5.4 */ int t_nullptr = 5402;
	/** @deprecated use {@link #tNOT} */ @Deprecated int t_not = 93;
	/** @deprecated use {@link #tNOTEQUAL} */ @Deprecated int t_not_eq = 94;
	int t_operator = 95;
	/** @deprecated use {@link #tOR} */ @Deprecated int t_or = 96;
	/** @deprecated use {@link #tBITORASSIGN} */ @Deprecated int t_or_eq = 97;
	int t_private = 98;
	int t_protected = 99;
	int t_public = 100;
	int t_register = 101;
	int t_reinterpret_cast = 102;
	int t_return = 103;
	int t_short = 104;
	int t_sizeof = 105;
	int t_static = 106;
	/** @since 5.2 */ int t_static_assert = 5205;
	int t_static_cast = 107;
	int t_signed = 108;
	int t_struct = 109;
	int t_switch = 110;
	int t_template = 111;
	int t_this = 112;
	/** @since 5.4 */ int t_thread_local = 5403;
	int t_throw = 113;
	int t_true = 114;
	int t_try = 115;
	int t_typedef = 116;
	int t_typeid = 117;
	int t_typename = 118;
	int t_union = 119;
	int t_unsigned = 120;
	int t_using = 121;
	int t_virtual = 122;
	int t_void = 123;
	int t_volatile = 124;
	int t_wchar_t = 125;
	int t_while = 126;
	/** @deprecated use {@link #tXOR} */ @Deprecated int t_xor = 127;
	/** @deprecated use {@link #tXORASSIGN} */ @Deprecated int t_xor_eq = 128;
	int tFLOATINGPT = 129;

	int tSTRING = 130;
	int tLSTRING = 131;
	/** @since 5.1 */ int tUTF16STRING = 5000;
	/** @since 5.1 */ int tUTF32STRING = 5001;
	/** @since 5.11 */ int tUSER_DEFINED_STRING_LITERAL = 51002;

	int tCHAR = 132;
	int tLCHAR = 133;
	/** @since 5.1 */ int tUTF16CHAR = 5002;
	/** @since 5.1 */ int tUTF32CHAR = 5003;
	/** @since 5.11 */ int tUSER_DEFINED_CHAR_LITERAL = 51003;

	/** @since 5.10 */ int t__Alignas = 51000;
	/** @since 5.10 */ int t__Alignof = 51001;
	int t__Bool = 134;
	int t__Complex = 135;
	int t__Imaginary = 136;
	int t_restrict = 137;
	/** @deprecated don't use it */ @Deprecated int tMACROEXP = 138;
	int tPOUND= 	  138;
	int tPOUNDPOUND = 139;
	int tCOMPLETION = 140;
	int tEOC = 141; // End of Completion
	/** @deprecated don't use it */ @Deprecated int tCOMMENT = 142;
	/** @deprecated don't use it */ @Deprecated int tBLOCKCOMMENT = 143;
	int tEND_OF_INPUT= 144;
	/** @since 5.1 */ int tINACTIVE_CODE_START= 145;
	/** @since 5.1 */ int tINACTIVE_CODE_SEPARATOR= 146;
	/** @since 5.1 */ int tINACTIVE_CODE_END  = 147;

	int FIRST_RESERVED_IGCCToken		= 150;
	int LAST_RESERVED_IGCCToken			= 199;

	int FIRST_RESERVED_IExtensionToken	= 243;
	int LAST_RESERVED_IExtensionToken	= 299;
	
	/**
	 * Token types for context-sensitive tokens.
	 * @since 5.9
	 */
	enum ContextSensitiveTokenType {
		OVERRIDE,
		FINAL
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public boolean isOperator();
}
