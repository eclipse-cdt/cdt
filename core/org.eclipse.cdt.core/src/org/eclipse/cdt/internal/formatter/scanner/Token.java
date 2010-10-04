/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Anton Leherbauer - adding tokens for preprocessing directives
 *     Markus Schorn - classification of preprocessing directives.
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter.scanner;


public class Token {

    public int type;
    public String text;
    public int offset;

    public Token(int t, String i, ScannerContext context) {
        set(t,i,context);
    }

    public void set(int t, String i, ScannerContext context) {
        type = t;
        text = i;
        offset = context.getOffset() - text.length() - context.undoStackSize();
    }

    public Token(int t, String i) {
        type = t;
        text = i;
    }
    
    @Override
	public String toString() {
        return "Token type=" + type + "  image =" + text + " offset=" + offset; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return text.length();
    }

    public int getDelta(Token other) {
        return other.getOffset() + other.getLength() - getOffset();
    }

    public boolean looksLikeExpressionStart() {
        switch (type) {
            case tINTEGER:
            case t_false:
            case t_true:
            case tSTRING:
            case tLSTRING:
            case tFLOATINGPT:
            case tCHAR:
            case tAMPER:
            case tDOT:
            case tLPAREN:
                return true;
            default:
                break;
        }
        return false;
    }

    public boolean looksLikeExpressionEnd() {
        switch (type) {
            case tINTEGER:
            case tSTRING:
            case tLSTRING:
            case tFLOATINGPT:
            case tCHAR:
            case tRPAREN:
            case tIDENTIFIER:
                return true;
            default:
                break;
        }
        return false;
    }

    public boolean isPointer() {
        return (type == tAMPER || type == tSTAR);
    }

    public boolean isOperator() {
        switch (type) {
            case t_new:
            case t_delete:
            case tPLUS:
            case tMINUS:
            case tSTAR:
            case tDIV:
            case tXOR:
            case tMOD:
            case tAMPER:
            case tBITOR:
            case tCOMPL:
            case tNOT:
            case tASSIGN:
            case tLT:
            case tGT:
            case tPLUSASSIGN:
            case tMINUSASSIGN:
            case tSTARASSIGN:
            case tDIVASSIGN:
            case tMODASSIGN:
            case tBITORASSIGN:
            case tAMPERASSIGN:
            case tXORASSIGN:
            case tSHIFTL:
            case tSHIFTR:
            case tSHIFTLASSIGN:
            case tSHIFTRASSIGN:
            case tEQUAL:
            case tNOTEQUAL:
            case tLTEQUAL:
            case tGTEQUAL:
            case tAND:
            case tOR:
            case tINCR:
            case tDECR:
            case tCOMMA:
            case tDOT:
            case tDOTSTAR:
            case tARROW:
            case tARROWSTAR:
                return true;
            default:
                return false;
        }
    }

    public boolean isInfixOperator() {
        switch (type) {
            case tPLUS:
            case tMINUS:
            case tSTAR:
            case tDIV:
            case tXOR:
            case tMOD:
            case tAMPER:
            case tBITOR:
            case tASSIGN:
            case tLT:
            case tGT:
            case tPLUSASSIGN:
            case tMINUSASSIGN:
            case tSTARASSIGN:
            case tDIVASSIGN:
            case tMODASSIGN:
            case tBITORASSIGN:
            case tAMPERASSIGN:
            case tXORASSIGN:
            case tSHIFTL:
            case tSHIFTR:
            case tSHIFTLASSIGN:
            case tSHIFTRASSIGN:
            case tEQUAL:
            case tNOTEQUAL:
            case tLTEQUAL:
            case tGTEQUAL:
            case tAND:
            case tOR:
            case tCOLON:
            case tQUESTION:
                return true;
            default:
                return false;
        }
    }

    public boolean isPrefixOperator() {
        switch (type) {
            case tPLUS:
            case tMINUS:
            case tSTAR:
            case tAMPER:
            case tCOMPL:
            case tNOT:
            case tINCR:
            case tDECR:
                return true;
            default:
                return false;
        }
    }

    public boolean isPostfixOperator() {
        switch (type) {
            case tINCR:
            case tDECR:
                return true;
            default:
                return false;
        }
    }

    public boolean isAssignmentOperator() {
        return isAssignmentOperator(type);
    }
    public static boolean isAssignmentOperator(int type) {
        switch (type) {
            case tASSIGN:
            case tPLUSASSIGN:
            case tMINUSASSIGN:
            case tSTARASSIGN:
            case tDIVASSIGN:
            case tAMPERASSIGN:
            case tBITORASSIGN:
            case tXORASSIGN:
            case tMODASSIGN:
            case tSHIFTLASSIGN:
            case tSHIFTRASSIGN:
                return true;
            default:
                return false;
        }
    }

    public boolean isControlStmt() {
        switch (type) {
            case t_if:
            case t_else:
            case t_for:
            case t_do:
            case t_while:
            case t_switch:
            case t_try:
            case t_catch:
            case t_finally:
                return true;
            default:
                return false;
        }
    }

    public boolean isWhiteSpace() {
        return type == tWHITESPACE;
    }

    public boolean isComment() {
        return isLineComment() || isBlockComment();
    }

    public boolean isLineComment() {
        return type == tLINECOMMENT;
    }

    public boolean isBlockComment() {
        return type == tBLOCKCOMMENT;
    }

    public boolean isCaseLabel() {
        return type == t_case || type == t_default;
    }

    public boolean isStructType() {
        return isStructType(type);
    }

    public static boolean isStructType(int type) {
        return type == t_struct || type == t_union || type == t_class;
    }

    public boolean isVisibilityModifier() {
        return isVisibilityModifier(type);
    }

	public static boolean isVisibilityModifier(int type) {
        return type == t_public || type == t_protected || type == t_private;
	}

	public boolean isEndOfStatement() {
        return type == tSEMI || type == tRBRACE;
    }
    
    public boolean isCPPToken() {
        switch (type) {
        case tCOLONCOLON:
        case t_class:
        case t_namespace:
        case t_using:
        case t_template:
        case t_public:
        case t_protected:
        case t_private:
        case t_operator:
        case t_virtual:
        case t_inline:
        case t_friend:
        case t_mutable:
        case t_new:
        case t_delete:
        case t_reinterpret_cast:
        case t_dynamic_cast:
        case t_static_cast:
        case t_finally:
            return true;
        default:
            return false;
        }
    }

    // overrider
    public boolean isStringLiteral() {
        return type == tSTRING || type == tLSTRING;
    }

    // overrider
    public boolean isCharacterLiteral() {
        return type == tCHAR;
    }

    // overrider
    public boolean isPreprocessor() {
        switch (type) {
        case tPREPROCESSOR:
        case tPREPROCESSOR_DEFINE:
        case tPREPROCESSOR_INCLUDE:
            return true;
        }
        return false;
    }
    
    // overrider
    public boolean isIncludeDirective() {
        return type == tPREPROCESSOR_INCLUDE;
    }
    // overrider
    public boolean isMacroDefinition() {
        return type == tPREPROCESSOR_DEFINE;
    }
    
    // Special Token types (non-grammar tokens)
    public static final int tWHITESPACE = 1000;
    public static final int tLINECOMMENT = 1001;
    public static final int tBLOCKCOMMENT = 1002;
    public static final int tPREPROCESSOR = 1003;
    public static final int tPREPROCESSOR_INCLUDE = 1004;
    public static final int tPREPROCESSOR_DEFINE  = 1005;
    public static final int tBADCHAR = 1006;

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
    static public final int tELIPSE = 48;
    static public final int tDOTSTAR = 49;
    static public final int tDOT = 50;
    static public final int tDIVASSIGN = 51;
    static public final int tDIV = 52;
    static public final int tCLASSNAME = 53;
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
    static public final int tSTRING = 129;
    static public final int tFLOATINGPT = 130;
    static public final int tLSTRING = 131;
    static public final int tCHAR = 132;
    static public final int tRSTRING = 133;
    static public final int t_restrict = 136;
    static public final int t_interface = 200;
    static public final int t_import = 201;
    static public final int t_instanceof = 202;
    static public final int t_extends = 203;
    static public final int t_implements = 204;
    static public final int t_final = 205;
    static public final int t_super = 206;
    static public final int t_package = 207;
    static public final int t_boolean = 208;
    static public final int t_abstract = 209;
    static public final int t_finally = 210;
    static public final int t_null = 211;
    static public final int t_synchronized = 212;
    static public final int t_throws = 213;
    static public final int t_byte = 214;
    static public final int t_transient = 215;
    static public final int t_native = 216;
}
