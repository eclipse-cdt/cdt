/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import org.eclipse.cdt.core.parser.util.CharArrayIntMap;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("nls")
public class Keywords {

	public static final String CAST = "cast"; 
	public static final String ALIGNOF = "alignof"; 
	public static final String TYPEOF = "typeof"; 
	
	public static final String _BOOL = "_Bool"; 
	public static final String _COMPLEX = "_Complex"; 
	public static final String _IMAGINARY = "_Imaginary"; 
	public static final String AND = "and"; 
	public static final String AND_EQ = "and_eq"; 
	public static final String ASM = "asm"; 
	public static final String AUTO = "auto"; 
	public static final String BITAND = "bitand"; 
	public static final String BITOR = "bitor"; 
	public static final String BOOL = "bool"; 
	public static final String BREAK = "break"; 
	public static final String CASE = "case"; 
	public static final String CATCH = "catch"; 
	public static final String CHAR = "char"; 
	/** @since 5.2 */
	public static final String CHAR16_T = "char16_t"; 
	/** @since 5.2 */
	public static final String CHAR32_T = "char32_t"; 
	public static final String CLASS = "class"; 
	public static final String COMPL = "compl"; 
	public static final String CONST = "const"; 
	public static final String CONST_CAST = "const_cast"; 
	public static final String CONTINUE = "continue"; 
	/** @since 5.2 */
	public static final String DECLTYPE = "decltype"; 
	public static final String DEFAULT = "default"; 
	public static final String DELETE = "delete"; 
	public static final String DO = "do"; 
	public static final String DOUBLE = "double"; 
	public static final String DYNAMIC_CAST = "dynamic_cast"; 
	public static final String ELSE = "else"; 
	public static final String ENUM = "enum"; 
	public static final String EXPLICIT = "explicit"; 
	public static final String EXPORT = "export"; 
	public static final String EXTERN = "extern"; 
	public static final String FALSE = "false"; 
	public static final String FLOAT = "float"; 
	public static final String FOR = "for"; 
	public static final String FRIEND = "friend"; 
	public static final String GOTO = "goto"; 
	public static final String IF = "if"; 
	public static final String INLINE = "inline"; 
	public static final String INT = "int"; 
	public static final String LONG = "long"; 
	public static final String LONG_LONG = "long long"; 
	public static final String MUTABLE = "mutable"; 
	public static final String NAMESPACE = "namespace"; 
	public static final String NEW = "new"; 
	public static final String NOT = "not"; 
	public static final String NOT_EQ = "not_eq"; 
	public static final String OPERATOR = "operator"; 
	public static final String OR = "or"; 
	public static final String OR_EQ = "or_eq"; 
	public static final String PRIVATE = "private"; 
	public static final String PROTECTED = "protected"; 
	public static final String PUBLIC = "public"; 
	public static final String REGISTER = "register"; 
	public static final String REINTERPRET_CAST = "reinterpret_cast"; 
	public static final String RESTRICT = "restrict"; 
	public static final String RETURN = "return"; 
	public static final String SHORT = "short"; 
	public static final String SIGNED = "signed"; 
	public static final String SIZEOF = "sizeof"; 
	public static final String STATIC = "static"; 
	/** @since 5.2 */
	public static final String STATIC_ASSERT = "static_assert"; 
	public static final String STATIC_CAST = "static_cast"; 
	public static final String STRUCT = "struct"; 
	public static final String SWITCH = "switch"; 
	public static final String TEMPLATE = "template"; 
	public static final String THIS = "this"; 
	public static final String THROW = "throw"; 
	public static final String TRUE = "true"; 
	public static final String TRY = "try"; 
	public static final String TYPEDEF = "typedef"; 
	public static final String TYPEID = "typeid"; 
	public static final String TYPENAME = "typename"; 
	public static final String UNION = "union"; 
	public static final String UNSIGNED = "unsigned"; 
	public static final String USING = "using"; 
	public static final String VIRTUAL = "virtual"; 
	public static final String VOID = "void"; 
	public static final String VOLATILE = "volatile"; 
	public static final String WCHAR_T = "wchar_t"; 
	public static final String WHILE = "while"; 
	public static final String XOR = "xor"; 
	public static final String XOR_EQ = "xor_eq"; 

	
	public static final char[] c_BOOL = "_Bool".toCharArray(); 
	public static final char[] c_COMPLEX = "_Complex".toCharArray(); 
	public static final char[] c_IMAGINARY = "_Imaginary".toCharArray(); 
	/** @since 5.3 */
	public static final char[] cALIGNOF = "alignof".toCharArray(); 
	public static final char[] cAND = "and".toCharArray(); 
	public static final char[] cAND_EQ = "and_eq".toCharArray(); 
	public static final char[] cASM = "asm".toCharArray(); 
	public static final char[] cAUTO = "auto".toCharArray(); 
	public static final char[] cBITAND = "bitand".toCharArray(); 
	public static final char[] cBITOR = "bitor".toCharArray(); 
	public static final char[] cBOOL = "bool".toCharArray(); 
	public static final char[] cBREAK = "break".toCharArray(); 
	public static final char[] cCASE = "case".toCharArray(); 
	public static final char[] cCATCH = "catch".toCharArray(); 
	public static final char[] cCHAR = "char".toCharArray(); 
	/** @since 5.2 */
	public static final char[] cCHAR16_T = CHAR16_T.toCharArray(); 
	/** @since 5.2 */
	public static final char[] cCHAR32_T = CHAR32_T.toCharArray(); 
	public static final char[] cCLASS = "class".toCharArray(); 
	public static final char[] cCOMPL = "compl".toCharArray(); 
	public static final char[] cCONST = "const".toCharArray(); 
	public static final char[] cCONST_CAST = "const_cast".toCharArray(); 
	public static final char[] cCONTINUE = "continue".toCharArray(); 
	public static final char[] cDEFAULT = "default".toCharArray(); 
	/** @since 5.2 */
	public static final char[] cDECLTYPE = DECLTYPE.toCharArray(); 
	public static final char[] cDELETE = "delete".toCharArray(); 
	public static final char[] cDO = "do".toCharArray(); 
	public static final char[] cDOUBLE = "double".toCharArray(); 
	public static final char[] cDYNAMIC_CAST = "dynamic_cast".toCharArray(); 
	public static final char[] cELSE = "else".toCharArray(); 
	public static final char[] cENUM = "enum".toCharArray(); 
	public static final char[] cEXPLICIT = "explicit".toCharArray(); 
	public static final char[] cEXPORT = "export".toCharArray(); 
	public static final char[] cEXTERN = "extern".toCharArray(); 
	public static final char[] cFALSE = "false".toCharArray(); 
	public static final char[] cFLOAT = "float".toCharArray(); 
	public static final char[] cFOR = "for".toCharArray(); 
	public static final char[] cFRIEND = "friend".toCharArray(); 
	public static final char[] cGOTO = "goto".toCharArray(); 
	public static final char[] cIF = "if".toCharArray(); 
	public static final char[] cINLINE = "inline".toCharArray(); 
	public static final char[] cINT = "int".toCharArray(); 
	public static final char[] cLONG = "long".toCharArray(); 
	public static final char[] cMUTABLE = "mutable".toCharArray(); 
	public static final char[] cNAMESPACE = "namespace".toCharArray(); 
	public static final char[] cNEW = "new".toCharArray(); 
	public static final char[] cNOT = "not".toCharArray(); 
	public static final char[] cNOT_EQ = "not_eq".toCharArray(); 
	public static final char[] cOPERATOR = "operator".toCharArray(); 
	public static final char[] cOR = "or".toCharArray(); 
	public static final char[] cOR_EQ = "or_eq".toCharArray(); 
	public static final char[] cPRIVATE = "private".toCharArray(); 
	public static final char[] cPROTECTED = "protected".toCharArray(); 
	public static final char[] cPUBLIC = "public".toCharArray(); 
	public static final char[] cREGISTER = "register".toCharArray(); 
	public static final char[] cREINTERPRET_CAST = "reinterpret_cast".toCharArray(); 
	public static final char[] cRESTRICT = "restrict".toCharArray(); 
	public static final char[] cRETURN = "return".toCharArray(); 
	public static final char[] cSHORT = "short".toCharArray(); 
	public static final char[] cSIGNED = "signed".toCharArray(); 
	public static final char[] cSIZEOF = "sizeof".toCharArray(); 
	/** @since 5.3 */
	public static final char[] cSIZEOFPACK= "sizeof...".toCharArray(); 
	public static final char[] cSTATIC = "static".toCharArray(); 
	/** @since 5.2 */
	public static final char[] cSTATIC_ASSERT = STATIC_ASSERT.toCharArray(); 
	public static final char[] cSTATIC_CAST = "static_cast".toCharArray(); 
	public static final char[] cSTRUCT = "struct".toCharArray(); 
	public static final char[] cSWITCH = "switch".toCharArray(); 
	public static final char[] cTEMPLATE = "template".toCharArray(); 
	public static final char[] cTHIS = "this".toCharArray(); 
	public static final char[] cTHROW = "throw".toCharArray(); 
	public static final char[] cTRUE = "true".toCharArray(); 
	public static final char[] cTRY = "try".toCharArray(); 
	public static final char[] cTYPEDEF = "typedef".toCharArray(); 
	public static final char[] cTYPEID = "typeid".toCharArray(); 
	public static final char[] cTYPENAME = "typename".toCharArray(); 
	public static final char[] cUNION = "union".toCharArray(); 
	public static final char[] cUNSIGNED = "unsigned".toCharArray(); 
	public static final char[] cUSING = "using".toCharArray(); 
	public static final char[] cVIRTUAL = "virtual".toCharArray(); 
	public static final char[] cVOID = "void".toCharArray(); 
	public static final char[] cVOLATILE = "volatile".toCharArray(); 
	public static final char[] cWCHAR_T = "wchar_t".toCharArray(); 
	public static final char[] cWHILE = "while".toCharArray(); 
	public static final char[] cXOR = "xor".toCharArray(); 
	public static final char[] cXOR_EQ = "xor_eq".toCharArray(); 
		
	public static final char[] cpCOLONCOLON = "::".toCharArray(); 
	public static final char[] cpCOLON = ":".toCharArray(); 
	public static final char[] cpSEMI = ";".toCharArray(); 
	public static final char[] cpCOMMA =	",".toCharArray(); 
	public static final char[] cpQUESTION = "?".toCharArray(); 
	public static final char[] cpLPAREN  = "(".toCharArray(); 
	public static final char[] cpRPAREN  = ")".toCharArray(); 
	public static final char[] cpLBRACKET = "[".toCharArray(); 
	public static final char[] cpRBRACKET = "]".toCharArray(); 
	public static final char[] cpLBRACE = "{".toCharArray(); 
	public static final char[] cpRBRACE = "}".toCharArray(); 
	public static final char[] cpPLUSASSIGN =	"+=".toCharArray(); 
	public static final char[] cpINCR = 	"++".toCharArray(); 
	public static final char[] cpPLUS = 	"+".toCharArray(); 
	public static final char[] cpMINUSASSIGN =	"-=".toCharArray(); 
	public static final char[] cpDECR = 	"--".toCharArray(); 
	public static final char[] cpARROWSTAR =	"->*".toCharArray(); 
	public static final char[] cpARROW = 	"->".toCharArray(); 
	public static final char[] cpMINUS = 	"-".toCharArray(); 
	public static final char[] cpSTARASSIGN =	"*=".toCharArray(); 
	public static final char[] cpSTAR = 	"*".toCharArray(); 
	public static final char[] cpMODASSIGN =	"%=".toCharArray(); 
	public static final char[] cpMOD = 	"%".toCharArray(); 
	public static final char[] cpXORASSIGN =	"^=".toCharArray(); 
	public static final char[] cpXOR = 	"^".toCharArray(); 
	public static final char[] cpAMPERASSIGN =	"&=".toCharArray(); 
	public static final char[] cpAND = 	"&&".toCharArray(); 
	public static final char[] cpAMPER =	"&".toCharArray(); 
	public static final char[] cpBITORASSIGN =	"|=".toCharArray(); 
	public static final char[] cpOR = 	"||".toCharArray(); 
	public static final char[] cpBITOR =	"|".toCharArray(); 
	public static final char[] cpCOMPL =	"~".toCharArray(); 
	public static final char[] cpNOTEQUAL =	"!=".toCharArray(); 
	public static final char[] cpNOT = 	"!".toCharArray(); 
	public static final char[] cpEQUAL =	"==".toCharArray(); 
	public static final char[] cpASSIGN ="=".toCharArray(); 
	public static final char[] cpSHIFTL =	"<<".toCharArray(); 
	public static final char[] cpLTEQUAL =	"<=".toCharArray(); 
	public static final char[] cpLT = 	"<".toCharArray(); 
	public static final char[] cpSHIFTRASSIGN =	">>=".toCharArray(); 
	public static final char[] cpSHIFTR = 	">>".toCharArray(); 
	public static final char[] cpGTEQUAL = 	">=".toCharArray(); 
	public static final char[] cpGT = 	">".toCharArray(); 
	public static final char[] cpSHIFTLASSIGN =	"<<=".toCharArray(); 
	public static final char[] cpELLIPSIS = 	"...".toCharArray(); 
	public static final char[] cpDOTSTAR = 	".*".toCharArray(); 
	public static final char[] cpDOT = 	".".toCharArray(); 
	public static final char[] cpDIVASSIGN =	"/=".toCharArray(); 
	public static final char[] cpDIV = 	"/".toCharArray(); 
	public static final char[] cpPOUND = "#".toCharArray(); 
	public static final char[] cpPOUNDPOUND = "##".toCharArray(); 
	
	// gcc extensions
	public static final char[] cpMIN = "<?".toCharArray(); 
	public static final char[] cpMAX = ">?".toCharArray(); 
	
	// preprocessor keywords
	public static final char[] cIFDEF = "ifdef".toCharArray(); 
	public static final char[] cIFNDEF = "ifndef".toCharArray(); 
	public static final char[] cELIF = "elif".toCharArray(); 
	public static final char[] cENDIF = "endif".toCharArray(); 
	public static final char[] cINCLUDE = "include".toCharArray(); 
	public static final char[] cDEFINE = "define".toCharArray(); 
	public static final char[] cUNDEF = "undef".toCharArray(); 
	public static final char[] cERROR = "error".toCharArray(); 
	public static final char[] cPRAGMA = "pragma".toCharArray(); 
	public static final char[] cLINE = "line".toCharArray(); 
	public static final char[] cDEFINED= "defined".toCharArray(); 
	/** @since 5.2*/
	public static final char[] _Pragma= "_Pragma".toCharArray(); 
	public static final char[] cVA_ARGS= "__VA_ARGS__".toCharArray(); 

	
	// preprocessor extensions (supported by GCC)
	public static final char[] cINCLUDE_NEXT = "include_next".toCharArray(); 
	public static final char[] cIMPORT = "import".toCharArray(); 
	public static final char[] cIDENT = "ident".toCharArray(); 
	public static final char[] cSCCS = "sccs".toCharArray(); 
	public static final char[] cWARNING = "warning".toCharArray(); 
	public static final char[] cASSERT = "assert".toCharArray(); 
	public static final char[] cUNASSERT = "unassert".toCharArray(); 
	
	public static void addKeywordsC(CharArrayIntMap kw) {
		addCommon(kw);
		addC(kw);
	}

	public static void addKeywordsCpp(CharArrayIntMap kw) {
		addCommon(kw);
		addCpp(kw);
	}


	private static void addCommon(CharArrayIntMap words) {
		words.put(Keywords._Pragma, IToken.t_PRAGMA);
        words.put(Keywords.cAUTO, IToken.t_auto);
        words.put(Keywords.cBREAK, IToken.t_break);
        words.put(Keywords.cCASE, IToken.t_case); 
        words.put(Keywords.cCHAR, IToken.t_char); 
        words.put(Keywords.cCONST, IToken.t_const); 
        words.put(Keywords.cCONTINUE, IToken.t_continue); 
        words.put(Keywords.cDEFAULT, IToken.t_default); 
        words.put(Keywords.cDO, IToken.t_do); 
        words.put(Keywords.cDOUBLE, IToken.t_double); 
        words.put(Keywords.cELSE, IToken.t_else); 
        words.put(Keywords.cENUM, IToken.t_enum); 
        words.put(Keywords.cEXTERN, IToken.t_extern); 
        words.put(Keywords.cFLOAT, IToken.t_float); 
        words.put(Keywords.cFOR, IToken.t_for); 
        words.put(Keywords.cGOTO, IToken.t_goto); 
        words.put(Keywords.cIF, IToken.t_if); 
        words.put(Keywords.cINLINE, IToken.t_inline); 
        words.put(Keywords.cINT, IToken.t_int); 
        words.put(Keywords.cLONG, IToken.t_long); 
        words.put(Keywords.cREGISTER, IToken.t_register); 
        words.put(Keywords.cRETURN, IToken.t_return); 
        words.put(Keywords.cSHORT, IToken.t_short); 
        words.put(Keywords.cSIGNED, IToken.t_signed); 
        words.put(Keywords.cSIZEOF, IToken.t_sizeof); 
        words.put(Keywords.cSTATIC, IToken.t_static); 
        words.put(Keywords.cSTRUCT, IToken.t_struct); 
        words.put(Keywords.cSWITCH, IToken.t_switch); 
        words.put(Keywords.cTYPEDEF, IToken.t_typedef); 
        words.put(Keywords.cUNION, IToken.t_union); 
        words.put(Keywords.cUNSIGNED, IToken.t_unsigned); 
        words.put(Keywords.cVOID, IToken.t_void); 
        words.put(Keywords.cVOLATILE, IToken.t_volatile); 
        words.put(Keywords.cWHILE, IToken.t_while); 
        words.put(Keywords.cASM, IToken.t_asm); 
	}
	
        // ANSI C keywords
	private static void addC(CharArrayIntMap ckeywords) {
        ckeywords.put(Keywords.cRESTRICT, IToken.t_restrict); 
        ckeywords.put(Keywords.c_BOOL, IToken.t__Bool); 
        ckeywords.put(Keywords.c_COMPLEX, IToken.t__Complex); 
        ckeywords.put(Keywords.c_IMAGINARY, IToken.t__Imaginary); 
	}
	
	private static void addCpp(CharArrayIntMap cppkeywords) {
        cppkeywords.put(Keywords.cBOOL, IToken.t_bool); 
        cppkeywords.put(Keywords.cCATCH, IToken.t_catch); 
        cppkeywords.put(Keywords.cCHAR16_T, IToken.t_char16_t); 
        cppkeywords.put(Keywords.cCHAR32_T, IToken.t_char32_t); 
        cppkeywords.put(Keywords.cCLASS, IToken.t_class); 
        cppkeywords.put(Keywords.cCONST_CAST, IToken.t_const_cast); 
        cppkeywords.put(Keywords.cDECLTYPE, IToken.t_decltype);
        cppkeywords.put(Keywords.cDELETE, IToken.t_delete); 
        cppkeywords.put(Keywords.cDYNAMIC_CAST, IToken.t_dynamic_cast); 
        cppkeywords.put(Keywords.cEXPLICIT, IToken.t_explicit); 
        cppkeywords.put(Keywords.cEXPORT, IToken.t_export); 
        cppkeywords.put(Keywords.cFALSE, IToken.t_false); 
        cppkeywords.put(Keywords.cFRIEND, IToken.t_friend); 
        cppkeywords.put(Keywords.cMUTABLE, IToken.t_mutable); 
        cppkeywords.put(Keywords.cNAMESPACE, IToken.t_namespace); 
        cppkeywords.put(Keywords.cNEW, IToken.t_new); 
        cppkeywords.put(Keywords.cOPERATOR, IToken.t_operator); 
        cppkeywords.put(Keywords.cPRIVATE, IToken.t_private); 
        cppkeywords.put(Keywords.cPROTECTED, IToken.t_protected); 
        cppkeywords.put(Keywords.cPUBLIC, IToken.t_public); 
        cppkeywords.put(Keywords.cREINTERPRET_CAST, IToken.t_reinterpret_cast); 
        cppkeywords.put(Keywords.cSTATIC_ASSERT, IToken.t_static_assert);
        cppkeywords.put(Keywords.cSTATIC_CAST, IToken.t_static_cast); 
        cppkeywords.put(Keywords.cTEMPLATE, IToken.t_template); 
        cppkeywords.put(Keywords.cTHIS, IToken.t_this); 
        cppkeywords.put(Keywords.cTHROW, IToken.t_throw); 
        cppkeywords.put(Keywords.cTRUE, IToken.t_true); 
        cppkeywords.put(Keywords.cTRY, IToken.t_try); 
        cppkeywords.put(Keywords.cTYPEID, IToken.t_typeid); 
        cppkeywords.put(Keywords.cTYPENAME, IToken.t_typename); 
        cppkeywords.put(Keywords.cUSING, IToken.t_using); 
        cppkeywords.put(Keywords.cVIRTUAL, IToken.t_virtual); 
        cppkeywords.put(Keywords.cWCHAR_T, IToken.t_wchar_t); 

        // C++ operator alternative
        cppkeywords.put(Keywords.cAND, IToken.tAND); 
        cppkeywords.put(Keywords.cAND_EQ, IToken.tAMPERASSIGN); 
        cppkeywords.put(Keywords.cBITAND, IToken.tAMPER); 
        cppkeywords.put(Keywords.cBITOR, IToken.tBITOR); 
        cppkeywords.put(Keywords.cCOMPL, IToken.tBITCOMPLEMENT); 
        cppkeywords.put(Keywords.cNOT, IToken.tNOT); 
        cppkeywords.put(Keywords.cNOT_EQ, IToken.tNOTEQUAL); 
        cppkeywords.put(Keywords.cOR, IToken.tOR); 
        cppkeywords.put(Keywords.cOR_EQ, IToken.tBITORASSIGN); 
        cppkeywords.put(Keywords.cXOR, IToken.tXOR); 
        cppkeywords.put(Keywords.cXOR_EQ, IToken.tXORASSIGN); 
	}
	
	public static void addKeywordsPreprocessor(CharArrayIntMap ppKeywords) {
        // Preprocessor keywords
        ppKeywords.put(Keywords.cIF, IPreprocessorDirective.ppIf); 
        ppKeywords.put(Keywords.cIFDEF, IPreprocessorDirective.ppIfdef); 
        ppKeywords.put(Keywords.cIFNDEF, IPreprocessorDirective.ppIfndef); 
        ppKeywords.put(Keywords.cELIF, IPreprocessorDirective.ppElif); 
        ppKeywords.put(Keywords.cELSE, IPreprocessorDirective.ppElse); 
        ppKeywords.put(Keywords.cENDIF, IPreprocessorDirective.ppEndif); 
        ppKeywords.put(Keywords.cINCLUDE, IPreprocessorDirective.ppInclude); 
        ppKeywords.put(Keywords.cDEFINE, IPreprocessorDirective.ppDefine); 
        ppKeywords.put(Keywords.cUNDEF, IPreprocessorDirective.ppUndef); 
        ppKeywords.put(Keywords.cERROR, IPreprocessorDirective.ppError); 
        ppKeywords.put(Keywords.cPRAGMA, IPreprocessorDirective.ppPragma); 
        ppKeywords.put(Keywords.cLINE, IPreprocessorDirective.ppIgnore);
    }
}
