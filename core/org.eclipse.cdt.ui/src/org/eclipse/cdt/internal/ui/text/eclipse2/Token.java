package org.eclipse.cdt.internal.ui.text.eclipse2;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.util.Assert;

 
/**
 * Standard implementation of <code>IToken</code>.
 */
public class Token implements IToken {
	
	private static final int T_UNDEFINED=	0;
	private static final int T_EOF=			1;
	private static final int T_WHITESPACE=	2;
	private static final int T_OTHER=		3;
	
	
	/** 
	 * Standard undefined token 
	 */
	public static final IToken UNDEFINED= new Token(T_UNDEFINED);
	/** 
	 * Standard End Of File token 
	 */
	public static final IToken EOF= new Token(T_EOF);
	/** 
	 * Standard whitespace token
	 */
	public static final IToken WHITESPACE= new Token(T_WHITESPACE);
	/**
	 * Standard token neither undefine, whitespace, nor whitespace
	 * @deprecated will be removed
	 */
	public static final IToken OTHER= new Token(T_OTHER);
	
	
	private int fType;
	private Object fData;
	
	/**
	 * Creates a new token according to the given specification which does not
	 * have any data attached to it.
	 *
	 * @param type the type of the token
	 */
	private Token(int type) {
		fType= type;
		fData= null;
	}
	
	/**
	 * Creates a new token which represents neither undefined, whitespace, nor EOF.
	 * The newly created token has the given data attached to it.
	 *
	 * @param data the data attached to the newly created token
	 */
	public Token(Object data) {
		fType= T_OTHER;
		fData= data;
	}
	
	/**
	 * Reinitializes the data of this token. The token may not represent
	 * undefined, whitepsace, or EOF.
	 * 
	 * @param the data to be attached to the token
	 */
	public void setData(Object data) {
		Assert.isTrue(isOther());
		fData= data;
	}
	
	/*
	 * @see IToken#getData()
	 */
	public Object getData() {
		return fData;
	}
	
	/*
	 * @see IToken#isOther()
	 */
	public boolean isOther() {
		return (fType == T_OTHER);
	}
	
	/*
	 * @see IToken#isEOF()
	 */
	public boolean isEOF() {
		return (fType == T_EOF);
	}
	
	/*
	 * @see IToken#isWhitespace()
	 */
	public boolean isWhitespace() {
		return (fType == T_WHITESPACE);
	}
	
	/*
	 * @see IToken#isUndefined()
	 */
	public boolean isUndefined() {
		return (fType == T_UNDEFINED);
	}	
}

