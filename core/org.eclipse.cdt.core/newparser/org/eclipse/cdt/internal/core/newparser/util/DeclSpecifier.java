package org.eclipse.cdt.internal.core.newparser.util;

import org.eclipse.cdt.internal.core.newparser.Token;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DeclSpecifier {
	
	// DeclSpecifier layed out as bit array
	// leftmost 5 bits are type
	public static final int typeMask = 0x001f;
	public static final int isAuto = 0x0020;
	public static final int isRegister = 0x0040;
	public static final int isStatic = 0x0080;
	public static final int isExtern = 0x0100;
	public static final int isMutable = 0x0200;
	public static final int isInline = 0x0400;
	public static final int isVirtual = 0x0800;
	public static final int isExplicit = 0x1000;
	public static final int isTypedef = 0x2000;
	public static final int isFriend = 0x4000;
	public static final int isConst = 0x8000;
	public static final int isVolatile = 0x10000;
	public static final int isUnsigned = 0x20000;
	public static final int isShort = 0x40000;
	public static final int isLong = 0x80000;

	private int declSpecifierSeq = 0;
	public int getDeclSpecifierSeq() { return declSpecifierSeq; }

	// Convenience methods
	private void setBit(boolean b, int mask) {
		if (b)
			declSpecifierSeq = declSpecifierSeq | mask;
		else
			declSpecifierSeq = declSpecifierSeq & ~mask;
	}
	
	private boolean checkBit(int mask) {
		return (declSpecifierSeq & mask) == 1;
	}
	
	public void setAuto(boolean b) { setBit(b, isAuto); }
	public boolean isAuto() { return checkBit(isAuto); }
	
	public void setRegister(boolean b) { setBit(b, isRegister); }
	public boolean isRegister() { return checkBit(isRegister); } 
	
	public void setStatic(boolean b) { setBit(b, isStatic); }
	public boolean isStatic() { return checkBit(isStatic); }
	
	public void setExtern(boolean b) { setBit(b, isExtern); }
	public boolean isExtern() { return checkBit(isExtern); }
	
	public void setMutable(boolean b) { setBit(b, isMutable); }
	public boolean isMutable() { return checkBit(isMutable); }
	
	public void setInline(boolean b) { setBit(b, isInline); }
	public boolean isInline() { return checkBit(isInline); }
	
	public void setVirtual(boolean b) { setBit(b, isVirtual); }
	public boolean isVirtual() { return checkBit(isVirtual); }
	
	public void setExplicit(boolean b) { setBit(b, isExplicit); }
	public boolean isExplicit() { return checkBit(isExplicit); }
	
	public void setTypedef(boolean b) { setBit(b, isTypedef); }
	public boolean isTypedef() { return checkBit(isTypedef); }
	
	public void setFriend(boolean b) { setBit(b, isFriend); }
	public boolean isFriend() { return checkBit(isFriend); }
	
	public void setConst(boolean b) { setBit(b, isConst); }
	public boolean isConst() { return checkBit(isConst); }
	
	public void setVolatile(boolean b) { setBit(b, isVolatile); }
	public boolean isVolatile() { return checkBit(isVolatile); }

	public void setUnsigned(boolean b) { setBit(b, isUnsigned); }
	public boolean isUnsigned() {	return checkBit(isUnsigned); }
	
	public void setShort(boolean b) { setBit(b, isShort); }
	public boolean isShort() { return checkBit(isShort); }

	public void setLong(boolean b) { setBit(b, isLong); }
	public boolean isLong() {	return checkBit(isLong); }

	// Simple Types
	public static final int t_type = 0; // Type Specifier
	public static final int t_char = 1;
	public static final int t_wchar_t = 2;
	public static final int t_bool = 3;
	public static final int t_int = 4;
	public static final int t_float = 5;
	public static final int t_double = 6;
	public static final int t_void = 7;
	
	public void setType( Token token )
	{
		switch (token.getType()) {
			case Token.t_auto:
				setAuto(true);
				break;
			case Token.t_register:
				setRegister(true);
				break;
			case Token.t_static:
				setStatic(true);
				break;
			case Token.t_extern:
				setExtern(true);
				break;
			case Token.t_mutable:
				setMutable(true);
				break;
			case Token.t_inline:
				setInline(true);
				break;
			case Token.t_virtual:
				setVirtual(true);
				break;
			case Token.t_explicit:
				setExplicit(true);
				break;
			case Token.t_typedef:
				setTypedef(true);
				break;
			case Token.t_friend:
				setFriend(true);
				break;
			case Token.t_const:
				setConst(true);
				break;
			case Token.t_volatile:
				setVolatile(true);
				break;
			case Token.t_char:
				setType(DeclarationSpecifier.t_char);
				break;
			case Token.t_wchar_t:
				setType(DeclarationSpecifier.t_wchar_t);
				break;
			case Token.t_bool:
				setType(DeclarationSpecifier.t_bool);
				break;
			case Token.t_short:
				setShort(true);
				break;
			case Token.t_int:
				setType(DeclarationSpecifier.t_int);
				break;
			case Token.t_long:
				setLong(true);
				break;
			case Token.t_signed:
				setUnsigned(false);
				break;
			case Token.t_unsigned:
				setUnsigned(true);
				break;
			case Token.t_float:
				setType(DeclarationSpecifier.t_float);
				break;
			case Token.t_double:
				setType(DeclarationSpecifier.t_double);
				break;
			case Token.t_void:
				setType(DeclarationSpecifier.t_void);
				break;
			case Token.tIDENTIFIER:
				setType(DeclarationSpecifier.t_type);
				break;
		}

	}
	
	public void setType(int t) {
		declSpecifierSeq = declSpecifierSeq & ~typeMask | t;
	}
	
	public int getType() {
		return declSpecifierSeq & typeMask;
	}

}
