/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier.Type;

public class DeclarationWrapper implements IDeclaratorOwner
{
	private int flag = 0;
	protected void setBit(boolean b, int mask){
		if( b ){
			flag = flag | mask; 
		} else {
			flag = flag & ~mask; 
		} 
	}
	
	protected boolean checkBit(int mask){
		return (flag & mask) != 0;
	}	

	private static final int DEFAULT_LIST_SIZE = 4;	
	
	protected static final int IS_IMAGINARY   = 0x00000010;
	protected static final int IS_COMPLEX     = 0x00000020;
	protected static final int IS_RESTRICT    = 0x00000040;
	protected static final int IS_SIGNED      = 0x00000080;
	protected static final int IS_SHORT       = 0x00000100;
	protected static final int IS_UNSIGNED    = 0x00000200;
	protected static final int IS_LONG        = 0x00000400;
	protected static final int IS_TYPENAMED   = 0x00000800;
	protected static final int IS_VOLATILE    = 0x00001000;
	protected static final int IS_VIRTUAL     = 0x00002000;
	protected static final int IS_TYPEDEF     = 0x00004000;
	protected static final int IS_STATIC      = 0x00008000;
	protected static final int IS_REGISTER    = 0x00010000;
	protected static final int IS_EXTERN      = 0x00020000;
	protected static final int IS_EXPLICIT    = 0x00040000;
	protected static final int IS_CONST       = 0x00080000;
	protected static final int IS_AUTO        = 0x00100000;
	protected static final int IS_GLOBAL      = 0x00200000;
	protected static final int IS_MUTABLE     = 0x00400000;
	protected static final int IS_FRIEND      = 0x00800000;
	protected static final int IS_INLINE      = 0x01000000;


    public int startingOffset = 0;
	public int startingLine;
    public int endOffset;
    
    private ITokenDuple name;
    private Type simpleType = IASTSimpleTypeSpecifier.Type.UNSPECIFIED;
    private final Object templateDeclaration;
    private final Object scope;
    private Object typeSpecifier;
	
    private List declarators = Collections.EMPTY_LIST;
    /**
     * @param b
     */
    public void setAuto(boolean b)
    {
        setBit( b, IS_AUTO );
    }
    /**
     * @return
     */
    public Object getScope()
    {
        return scope;
    }
        
    /**
     * @param scope
     * @param filename TODO
     */
    public DeclarationWrapper(
        Object scope,
        int startingOffset,
        int startingLine, Object templateDeclaration, char[] filename)
    {
        this.scope = scope;
        this.startingOffset = startingOffset;
        this.startingLine = startingLine;
        this.templateDeclaration = templateDeclaration;
        this.fn = filename;
    }
    /**
     * @param b
     */
    public void setTypenamed(boolean b)
    {
    	setBit( b, IS_TYPENAMED );
    }
    /**
     * @param b
     */
    public void setMutable(boolean b)
    {
    	setBit( b, IS_MUTABLE);
    }
    /**
     * @param b
     */
    public void setFriend(boolean b)
    {
    	setBit( b, IS_FRIEND );
    }
    /**
     * @param b
     */
    public void setInline(boolean b)
    {
        setBit( b, IS_INLINE );
    }
    /**
     * @param b
     */
    public void setRegister(boolean b)
    {
        setBit( b, IS_REGISTER );
    }
    /**
     * @param b
     */
    public void setStatic(boolean b)
    {
        setBit( b, IS_STATIC );
    }
    /**
     * @param b
     */
    public void setTypedef(boolean b)
    {
        setBit( b, IS_TYPEDEF );
    }
    /**
     * @param b
     */
    public void setVirtual(boolean b)
    {
        setBit( b, IS_VIRTUAL );
    }
    /**
     * @param b
     */
    public void setVolatile(boolean b)
    {
        setBit( b, IS_VOLATILE );
    }
    /**
     * @param b
     */
    public void setExtern(boolean b)
    {
        setBit( b, IS_EXTERN );
    }
    /**
     * @param b
     */
    public void setExplicit(boolean b)
    {
        setBit( b, IS_EXPLICIT );
    }
    /**
     * @param b
     */
    public void setConst(boolean b)
    {
        setBit( b, IS_CONST );
    }
    /**
     * @return
     */
    public boolean isAuto()
    {
        return checkBit( IS_AUTO );
    }
    /**
     * @return
     */
    public boolean isConst()
    {
        return checkBit( IS_CONST );
    }
    /**
     * @return
     */
    public boolean isExplicit()
    {
    	return checkBit( IS_EXPLICIT );
    }
    /**
     * @return
     */
    public boolean isExtern()
    {
    	return checkBit( IS_EXTERN );
    }
    /**
     * @return
     */
    public boolean isFriend()
    {
    	return checkBit( IS_FRIEND );
    }
    /**
     * @return
     */
    public boolean isInline()
    {
    	return checkBit( IS_INLINE );
    }
    /**
     * @return
     */
    public boolean isMutable()
    {
    	return checkBit( IS_MUTABLE );
    }
    /**
     * @return
     */
    public boolean isRegister()
    {
    	return checkBit( IS_REGISTER );
    }
    
    /**
     * @return
     */
    public boolean isStatic()
    {
    	return checkBit( IS_STATIC );
    }
    /**
     * @return
     */
    public boolean isTypedef()
    {
    	return checkBit( IS_TYPEDEF );
    }
    /**
     * @return
     */
    public boolean isTypeNamed()
    {
    	return checkBit( IS_TYPENAMED );
    }
    /**
     * @return
     */
    public boolean isVirtual()
    {
    	return checkBit( IS_VIRTUAL );
    }
    /**
     * @return
     */
    public boolean isVolatile()
    {
    	return checkBit( IS_VOLATILE );
    }
    
    public void addDeclarator(Declarator d)
    {
    	if( declarators == Collections.EMPTY_LIST )
    		declarators = new ArrayList(DEFAULT_LIST_SIZE);
        declarators.add(d);
    }
    public Iterator getDeclarators()
    {
        return declarators.iterator();
    }
    /**
     * @return
     */
    public Object getTypeSpecifier()
    {
        return typeSpecifier;
    }
    /**
     * @param enumeration
     */
    public void setTypeSpecifier(Object enumeration)
    {
        typeSpecifier = enumeration;
    }
	public int endLine;

	public final char[] fn;

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.IDeclaratorOwner#getDeclarationWrapper()
     */
    public DeclarationWrapper getDeclarationWrapper()
    {
        return this;
    }
    /**
     * @return
     */
    public boolean isUnsigned()
    {
    	return checkBit( IS_UNSIGNED );
    }
    /**
     * @return
     */
    public boolean isSigned()
    {
    	return checkBit( IS_SIGNED );
    }
    /**
     * @return
     */
    public boolean isShort()
    {
    	return checkBit( IS_SHORT );
    }
    /**
     * @return
     */
    public boolean isLong()
    {
    	return checkBit( IS_LONG );
    }
    /**
     * @param b
     */
    public void setLong(boolean b)
    {
    	setBit( b, IS_LONG );
    }
    /**
     * @param b
     */
    public void setShort(boolean b)
    {
    	setBit( b, IS_SHORT );
    }
    /**
     * @param b
     */
    public void setSigned(boolean b)
    {
    	setBit( b, IS_SIGNED );
    }
    /**
     * @param b
     */
    public void setUnsigned(boolean b)
    {
        setBit( b, IS_UNSIGNED );
    }
    /**
     * @return
     */
    public Type getSimpleType()
    {
        return simpleType;
    }
    /**
     * @param type
     */
    public void setSimpleType(Type type)
    {
        simpleType = type;
    }
    /**
     * @param duple
     */
    public void setTypeName(ITokenDuple duple)
    {
        name = duple;
    }
    /**
     * @return
     */
    public final ITokenDuple getName()
    {
        return name;
    }

    /**
     * @return
     */
    public final Object getOwnerTemplate()
    {
        return templateDeclaration;
    }
    /**
     * @param i
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
        endOffset = offset;
        endLine = lineNumber;
    }
    /**
     * @param b
     */
    public void setRestrict(boolean b)
    {
        setBit( b, IS_RESTRICT );
    }
    

    /**
     * @return
     */
    public boolean isRestrict()
    {
    	return checkBit( IS_RESTRICT );
    }
    /**
     * @param b
     */
    public void setImaginary(boolean b)
    {
    	setBit( b, IS_IMAGINARY );
    }

    /**
     * @return
     */
    public boolean isComplex()
    {
    	return checkBit( IS_COMPLEX );
    }

    /**
     * @return
     */
    public boolean isImaginary()
    {
    	return checkBit( IS_IMAGINARY );
    }

    /**
     * @param b
     */
    public void setComplex(boolean b)
    {
        setBit( b, IS_COMPLEX );
    }
	/**
	 * @param b
	 */
	public void setGloballyQualified(boolean b) {
		setBit( b, IS_GLOBAL );
	}
	
	public boolean isGloballyQualified(){
		return checkBit( IS_GLOBAL );
	}
	
	private Map extensionParameters = Collections.EMPTY_MAP;
	/**
	 * @param key
	 * @param typeOfExpression
	 */
	public void setExtensionParameter(String key, Object value) {
		if( extensionParameters == Collections.EMPTY_MAP )
			extensionParameters = new Hashtable( 4 );
		extensionParameters.put( key, value );
	}
	
	public Map getExtensionParameters()
	{
		return extensionParameters;
	}
	/**
	 * @return
	 */
	public boolean consumedRawType() {
		return( getSimpleType() != IASTSimpleTypeSpecifier.Type.UNSPECIFIED );
	}
}