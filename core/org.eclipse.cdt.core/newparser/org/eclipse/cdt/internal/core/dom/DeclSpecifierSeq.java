package org.eclipse.cdt.internal.core.dom;

/**
 */
public class DeclSpecifierSeq {

	private boolean isAuto = false;
	public void setAuto(boolean b) { isAuto = b; }
	public boolean isAuto() { return isAuto; }
	
	private boolean isRegister = false;
	public void setRegister(boolean b) { isRegister = b; }
	public boolean isRegister() { return isRegister; }
	
	private boolean isStatic = false;
	public void setStatic(boolean b) { isStatic = b; }
	public boolean isStatic() { return isStatic; }
	
	private boolean isExtern = false;
	public void setExtern(boolean b) { isExtern = b; }
	public boolean isExtern() { return isExtern; }
	
	private boolean isMutable = false;
	public void setMutable(boolean b) { isMutable = b; }
	public boolean isMutable() { return isMutable; }
	
	private boolean isInline = false;
	public void setInline(boolean b) { isInline = b; }
	public boolean isInline() { return isInline; }
	
	private boolean isVirtual = false;
	public void setVirtual(boolean b) { isVirtual = b; }
	public boolean isVirtual() { return isVirtual; }
	
	private boolean isExplicit = false;
	public void setExplicit(boolean b) { isExplicit = b; }
	public boolean isExplicit() { return isExplicit; }
	
	private boolean isTypedef = false;
	public void setTypedef(boolean b) { isTypedef = b; }
	public boolean isTypedef() { return isTypedef; }
	
	private boolean isFriend = false;
	public void setFriend(boolean b) { isFriend = b; }
	public boolean isFriend() { return isFriend; }
	
	private boolean isConst = false;
	public void setConst(boolean b) { isConst = b; }
	public boolean isConst() { return isConst; }
	
	private boolean isVolatile = false;
	public void setVolatile(boolean b) { isVolatile = b; }
	public boolean isVolatile() { return isVolatile; }

	private boolean isUnsigned = false;
	public void setUnsigned(boolean b) { isUnsigned = b; }
	public boolean isUnsigned() {	return isUnsigned; }
	
	private boolean isShort = false;
	public void setShort(boolean b) { isShort = b; }
	public boolean isShort() { return isShort; }

	private boolean isLong = false;
	public void setLong(boolean b) { isLong = b; }
	public boolean isLong() {	return isLong;	}

	public static final int t_unknown = 0;
	public static final int t_char = 1;
	public static final int t_wchar_t = 2;
	public static final int t_bool = 3;
	public static final int t_int = 4;
	public static final int t_float = 5;
	public static final int t_double = 6;
	public static final int t_void = 7;
	private int type = t_unknown;
	public void setType(int t) { type = t; }
	public int getType() { return type; }
	
	
}
