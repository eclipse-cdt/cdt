/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;


/**
 * @author jcamelon
 *
 */
public interface IToken {
	
	// getters
	public int getType();
	public String getImage();
	public char [] getCharImage();
	public int getOffset();
	public int getLength();
	public int getEndOffset();
	// NOTE:if the token spans lines due to escaped newlines then 
	// the line number returned is the last one 
	public int getLineNumber();
	public IToken getNext();
	
	// setters
	public void setImage( String i );
	public void setImage( char [] i );
	public void setNext(IToken t);
	public void setType(int i);	

	// queries
	public boolean looksLikeExpression();
	public boolean isPointer();
	public boolean isOperator();
	public boolean canBeAPrefix();
	
	
	// Token types
	static public final int tIDENTIFIER = 1;

	static public final int tINTEGER = 2;

	static public final int tCOLONCOLON = 3;

	static public final int tCOLON = 4;

	static public final int tSEMI = 5;

	static public final int tCOMMA = 6;

	static public final int tQUESTION = 7;

	static public final int tLPAREN = 8;

	static public final int tRPAREN = 9;

	static public final int tLBRACKET = 10;

	static public final int tRBRACKET = 11;

	static public final int tLBRACE = 12;

	static public final int tRBRACE = 13;

	static public final int tPLUSASSIGN = 14;

	static public final int tINCR = 15;

	static public final int tPLUS = 16;

	static public final int tMINUSASSIGN = 17;

	static public final int tDECR = 18;

	static public final int tARROWSTAR = 19;

	static public final int tARROW = 20;

	static public final int tMINUS = 21;

	static public final int tSTARASSIGN = 22;

	static public final int tSTAR = 23;

	static public final int tMODASSIGN = 24;

	static public final int tMOD = 25;

	static public final int tXORASSIGN = 26;

	static public final int tXOR = 27;

	static public final int tAMPERASSIGN = 28;

	static public final int tAND = 29;

	static public final int tAMPER = 30;

	static public final int tBITORASSIGN = 31;

	static public final int tOR = 32;

	static public final int tBITOR = 33;

	static public final int tCOMPL = 34;

	static public final int tNOTEQUAL = 35;

	static public final int tNOT = 36;

	static public final int tEQUAL = 37;

	static public final int tASSIGN = 38;

	static public final int tSHIFTL = 40;

	static public final int tLTEQUAL = 41;

	static public final int tLT = 42;

	static public final int tSHIFTRASSIGN = 43;

	static public final int tSHIFTR = 44;

	static public final int tGTEQUAL = 45;

	static public final int tGT = 46;

	static public final int tSHIFTLASSIGN = 47;

	static public final int tELLIPSIS = 48;

	static public final int tDOTSTAR = 49;

	static public final int tDOT = 50;

	static public final int tDIVASSIGN = 51;

	static public final int tDIV = 52;

	static public final int t_and = 54;

	static public final int t_and_eq = 55;

	static public final int t_asm = 56;

	static public final int t_auto = 57;

	static public final int t_bitand = 58;

	static public final int t_bitor = 59;

	static public final int t_bool = 60;

	static public final int t_break = 61;

	static public final int t_case = 62;

	static public final int t_catch = 63;

	static public final int t_char = 64;

	static public final int t_class = 65;

	static public final int t_compl = 66;

	static public final int t_const = 67;

	static public final int t_const_cast = 69;

	static public final int t_continue = 70;

	static public final int t_default = 71;

	static public final int t_delete = 72;

	static public final int t_do = 73;

	static public final int t_double = 74;

	static public final int t_dynamic_cast = 75;

	static public final int t_else = 76;

	static public final int t_enum = 77;

	static public final int t_explicit = 78;

	static public final int t_export = 79;

	static public final int t_extern = 80;

	static public final int t_false = 81;

	static public final int t_float = 82;

	static public final int t_for = 83;

	static public final int t_friend = 84;

	static public final int t_goto = 85;

	static public final int t_if = 86;

	static public final int t_inline = 87;

	static public final int t_int = 88;

	static public final int t_long = 89;

	static public final int t_mutable = 90;

	static public final int t_namespace = 91;

	static public final int t_new = 92;

	static public final int t_not = 93;

	static public final int t_not_eq = 94;

	static public final int t_operator = 95;

	static public final int t_or = 96;

	static public final int t_or_eq = 97;

	static public final int t_private = 98;

	static public final int t_protected = 99;

	static public final int t_public = 100;

	static public final int t_register = 101;

	static public final int t_reinterpret_cast = 102;

	static public final int t_return = 103;

	static public final int t_short = 104;

	static public final int t_sizeof = 105;

	static public final int t_static = 106;

	static public final int t_static_cast = 107;

	static public final int t_signed = 108;

	static public final int t_struct = 109;

	static public final int t_switch = 110;

	static public final int t_template = 111;

	static public final int t_this = 112;

	static public final int t_throw = 113;

	static public final int t_true = 114;

	static public final int t_try = 115;

	static public final int t_typedef = 116;

	static public final int t_typeid = 117;

	static public final int t_typename = 118;

	static public final int t_union = 119;

	static public final int t_unsigned = 120;

	static public final int t_using = 121;

	static public final int t_virtual = 122;

	static public final int t_void = 123;

	static public final int t_volatile = 124;

	static public final int t_wchar_t = 125;

	static public final int t_while = 126;

	static public final int t_xor = 127;

	static public final int t_xor_eq = 128;
	
	static public final int tFLOATINGPT = 129;

	static public final int tSTRING = 130;
	
	static public final int tLSTRING = 131;

	static public final int tCHAR = 132;
	
	static public final int tLCHAR = 133;

	static public final int t__Bool = 134;

	static public final int t__Complex = 135;

	static public final int t__Imaginary = 136;

	static public final int t_restrict = 137;

	static public final int tMACROEXP = 138;
	
	static public final int tPOUNDPOUND = 139;

	static public final int tLAST = 139;
}