/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 5, 2004
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author aniefer
 */
public interface ITypeInfo {
    public static class OperatorExpression extends Enum{
    	
    	//5.3.1-1 : The unary * operator, the expression to which it is applied shall be
    	//a pointer to an object type or a pointer to a function type and the result
    	//is an lvalue refering to the object or function to which the expression points
    	public static final OperatorExpression indirection = new OperatorExpression( 1 );
    	
    	//5.3.1-2 : The result of the unary & operator is a pointer to its operand
    	public static final OperatorExpression addressof = new OperatorExpression( 0 );
    	
    	//5.2.1 A postfix expression followed by an expression in square brackets is a postfix
    	//expression.  one of the expressions shall have the type "pointer to T" and the other
    	//shall have a enumeration or integral type.  The result is an lvalue of type "T"
    	public static final OperatorExpression subscript = new OperatorExpression( 2 );
    	
    	protected OperatorExpression(int enumValue) {
    		super(enumValue);
    	}
    }

    public static class PtrOp {
    	public PtrOp( eType type ){
    		this.type = type;
    	}
    	public PtrOp( eType type, boolean isConst, boolean isVolatile ){
    		this.type = type;
    		this.isConstPtr = isConst;
    		this.isVolatilePtr = isVolatile;
    	}
    	public PtrOp( ISymbol memberOf, boolean isConst, boolean isVolatile ){
    		this.type = PtrOp.t_memberPointer;
    		this.isConstPtr = isConst;
    		this.isVolatilePtr = isVolatile;
    		this.memberOf = memberOf;
    	}
    	
    	public PtrOp(){
    		super();
    	}
    	
    	public static final eType t_undef_ptr     = new eType( 0 );
    	public static final eType t_pointer       = new eType( 1 );
    	public static final eType t_reference     = new eType( 2 );
    	public static final eType t_array         = new eType( 3 );
    	public static final eType t_memberPointer = new eType( 4 );
    
    	public eType 	getType()			 			{ return type; }
    	public void 	setType( eType type )			{ this.type = type; }
    	
    	public boolean 	isConst()						{ return isConstPtr; }
    	public boolean 	isVolatile()					{ return isVolatilePtr; }
    	public void 	setConst( boolean isConst ) 	{ this.isConstPtr = isConst; }
    	public void 	setVolatile(boolean isVolatile)	{ this.isVolatilePtr = isVolatile; }
    	
    	public ISymbol	getMemberOf()					{ return memberOf; }
    	public void 	setMemberOf( ISymbol member )	{ this.memberOf = member;	}
    	
    	public int compareCVTo( ITypeInfo.PtrOp ptr ){
    		int cv1 = ( isConst() ? 1 : 0 ) + ( isVolatile() ? 1 : 0 );
    		int cv2 = ( ptr.isConst() ? 1 : 0 ) + ( ptr.isVolatile() ? 1 : 0 );
    		
    		return cv1 - cv2;
    	}
    	public boolean equals( Object o ){
    		if( o == null || !(o instanceof ITypeInfo.PtrOp) ){
    			return false;
    		}	
    		ITypeInfo.PtrOp op = (ITypeInfo.PtrOp)o;
    		
    		return ( isConst() == op.isConst() &&
    				 isVolatile() == op.isVolatile() &&
    				 getType() == op.getType() );
    	}
    	
    	private eType type = PtrOp.t_undef_ptr;
    	private boolean isConstPtr = false;
    	private boolean isVolatilePtr = false;
    	private ISymbol memberOf = null;
    }

    public static class eType extends Enum implements Comparable{
    	protected eType( int v ){
    		super( v );
    	}
    	
    	public int compareTo( Object o ){
    		ITypeInfo.eType t = (ITypeInfo.eType) o;
    		return getEnumValue() - t.getEnumValue();
    	}
    	public int toInt() {
    		return getEnumValue();
    	}
    }

    public static final int isAuto 		= 1 << 0;
    public static final int isRegister 	= 1 << 1;
    public static final int isStatic 	= 1 << 2;
    public static final int isExtern 	= 1 << 3;
    public static final int isMutable 	= 1 << 4;
    public static final int isInline 	= 1 << 5;
    public static final int isVirtual 	= 1 << 6;
    public static final int isExplicit 	= 1 << 7;
    public static final int isTypedef 	= 1 << 8;
    public static final int isFriend 	= 1 << 9;
    public static final int isConst 	= 1 << 10;
    public static final int isVolatile 	= 1 << 11;
    public static final int isUnsigned 	= 1 << 12;
    public static final int isShort 	= 1 << 13;
    public static final int isLong 		= 1 << 14;
    public static final int isForward 	= 1 << 15;
    public static final int isComplex 	= 1 << 16;
    public static final int isImaginary = 1 << 17;
    public static final int isLongLong 	= 1 << 18;
    public static final int isSigned 	= 1 << 19;

    // Types 
    // Note that these should be considered ordered and if you change
    // the order, you should consider the ParserSymbolTable uses
    public static final ITypeInfo.eType t_any 				= new ITypeInfo.eType( -1 ); //don't care
    public static final ITypeInfo.eType t_undef 			= new ITypeInfo.eType( 0 ); //not specified
    public static final ITypeInfo.eType t_type 				= new ITypeInfo.eType( 1 ); //Type Specifier
    public static final ITypeInfo.eType t_namespace 		= new ITypeInfo.eType( 2 );
    public static final ITypeInfo.eType t_class 			= new ITypeInfo.eType( 3 );
    public static final ITypeInfo.eType t_struct 			= new ITypeInfo.eType( 4 );
    public static final ITypeInfo.eType t_union 			= new ITypeInfo.eType( 5 );
    public static final ITypeInfo.eType t_enumeration 		= new ITypeInfo.eType( 6 );
    public static final ITypeInfo.eType t_constructor 		= new ITypeInfo.eType( 7 );
    public static final ITypeInfo.eType t_function			= new ITypeInfo.eType( 8 );
    public static final ITypeInfo.eType t__Bool				= new ITypeInfo.eType( 9 );
    public static final ITypeInfo.eType t_bool 				= new ITypeInfo.eType( 10 );
    public static final ITypeInfo.eType t_char 				= new ITypeInfo.eType( 11 );
    public static final ITypeInfo.eType t_wchar_t 			= new ITypeInfo.eType( 12 );
    public static final ITypeInfo.eType t_int 				= new ITypeInfo.eType( 13 );
    public static final ITypeInfo.eType t_float 			= new ITypeInfo.eType( 14 );
    public static final ITypeInfo.eType t_double 			= new ITypeInfo.eType( 15 );
    public static final ITypeInfo.eType t_void 				= new ITypeInfo.eType( 16 );
    public static final ITypeInfo.eType t_enumerator		= new ITypeInfo.eType( 17 );
    public static final ITypeInfo.eType t_block				= new ITypeInfo.eType( 18 );
    public static final ITypeInfo.eType t_template 			= new ITypeInfo.eType( 19 );
    public static final ITypeInfo.eType t_asm 				= new ITypeInfo.eType( 20 );
    public static final ITypeInfo.eType t_linkage 			= new ITypeInfo.eType( 21 );
    public static final ITypeInfo.eType t_templateParameter = new ITypeInfo.eType( 22 );

    public static final ITypeInfo.eType t_typeName = new ITypeInfo.eType( 23 );

    public abstract void setBit( boolean b, int mask );

    public abstract boolean checkBit( int mask );

    public abstract void setType( ITypeInfo.eType t );

    public abstract ITypeInfo.eType getType();

    public abstract boolean isType( ITypeInfo.eType type );

    public abstract int getTypeBits();

    public abstract void setTypeBits( int typeInfo );

    public abstract ITypeInfo.eType getTemplateParameterType();

    public abstract void setTemplateParameterType( ITypeInfo.eType type );

    /**
     * 
     * @param infoProvider - TypeInfoProvider to use if pooling the TypeInfo created, if null,
     *                       pooling is not used.  If pooling is used, TypeInfoProvider.returnTypeInfo
     *                       must be called when the TypeInfo is no longer needed
     * @return
     */
    public abstract ITypeInfo getFinalType( TypeInfoProvider infoProvider );

    /**
     * 
     * @param type
     * @param upperType
     * @return boolean
     * 
     * type checking, check that this declaration's type is between type and
     * upperType (inclusive).  upperType of 0 means no range and our type must
     * be type.
     */
    public abstract boolean isType( ITypeInfo.eType type,
            ITypeInfo.eType upperType );

    public abstract ISymbol getTypeSymbol();

    public abstract void setTypeSymbol( ISymbol type );

    public abstract boolean hasPtrOperators();

    public abstract List getPtrOperators();

    public abstract boolean hasSamePtrs( ITypeInfo type );

    public abstract void applyOperatorExpression( ITypeInfo.OperatorExpression op );

    public abstract void addPtrOperator( ITypeInfo.PtrOp ptr );

    public abstract void addPtrOperator( List ptrs );

    public abstract void preparePtrOperators( int numPtrOps );

    public abstract boolean getHasDefault();

    public abstract void setHasDefault( boolean def );

    public abstract void setDefault( Object t );

    public abstract Object getDefault();

    /**
     * canHold
     * @param type
     * @return boolean
     * return true if our type can hold all the values of the passed in
     * type.
     * TODO, for now return true if our type is "larger" (based on ordering of
     * the type values)
     */
    public abstract boolean canHold( ITypeInfo type );

    public abstract boolean equals( Object t );

    public abstract char[] toCharArray();

    public abstract void clear();

    public abstract void copy( ITypeInfo t );
}
