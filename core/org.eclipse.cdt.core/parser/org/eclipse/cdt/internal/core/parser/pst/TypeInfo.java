/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.core.parser.Enum;


public class TypeInfo {
	public TypeInfo(){
		super();	
	}

	public TypeInfo( TypeInfo.eType type, int bits, ISymbol symbol ){
		super();
		_typeBits = bits;
		_type = type;
		_typeDeclaration = symbol;	
	}

	public TypeInfo( TypeInfo.eType type, int bits, ISymbol symbol, TypeInfo.PtrOp op, boolean hasDefault ){
		super();
		_typeBits = bits;
		_type = type;
		_typeDeclaration = symbol;
		if( op != null ){
			_ptrOperators = new ArrayList(2);
			_ptrOperators.add( op );
		} else {
			_ptrOperators = Collections.EMPTY_LIST;
		}
		_hasDefaultValue = hasDefault;
	}
	
	public TypeInfo( TypeInfo.eType type, int bits, ISymbol symbol, TypeInfo.PtrOp op, Object def ){
		super();
		_typeBits = bits;
		_type = type;
		_typeDeclaration = symbol;
		if( op != null ){
			_ptrOperators = new ArrayList( 1 );
			_ptrOperators.add( op );
		} else {
			_ptrOperators = Collections.EMPTY_LIST;
		}
		_hasDefaultValue = true;
		setDefault( def );
	}

	public TypeInfo( TypeInfo info ){
		super();
	
		_typeBits = info._typeBits;
		_type = info._type;
		_typeDeclaration = info._typeDeclaration;
		_ptrOperators = ( info._ptrOperators == Collections.EMPTY_LIST ) ? info._ptrOperators : (ArrayList)((ArrayList)info._ptrOperators).clone();
		_hasDefaultValue = info._hasDefaultValue;
		_defaultValue = info._defaultValue;
	}

	public static final int isAuto     = 0x00001;
	public static final int isRegister = 0x00002;
	public static final int isStatic   = 0x00004;
	public static final int isExtern   = 0x00008;
	public static final int isMutable  = 0x00010;
	public static final int isInline   = 0x00020;
	public static final int isVirtual  = 0x00040;
	public static final int isExplicit = 0x00080;
	public static final int isTypedef  = 0x00100;
	public static final int isFriend   = 0x00200;
	public static final int isConst    = 0x00400;
	public static final int isVolatile = 0x00800;
	public static final int isUnsigned = 0x01000;
	public static final int isShort    = 0x02000;
	public static final int isLong     = 0x04000;
	public static final int isForward  = 0x08000;
	public static final int isComplex  = 0x10000;
	public static final int isImaginary= 0x20000;
	public static final int isLongLong = 0x40000;
	public static final int isSigned   = 0x80000;
	
	// Types (maximum type is typeMask
	// Note that these should be considered ordered and if you change
	// the order, you should consider the ParserSymbolTable uses
	public static final TypeInfo.eType t_any         = new TypeInfo.eType( -1 ); //don't care
	public static final TypeInfo.eType t_undef       = new TypeInfo.eType(  0 ); //not specified
	public static final TypeInfo.eType t_type        = new TypeInfo.eType(  1 ); //Type Specifier
	public static final TypeInfo.eType t_namespace   = new TypeInfo.eType(  2 );
	public static final TypeInfo.eType t_class       = new TypeInfo.eType(  3 );
	public static final TypeInfo.eType t_struct      = new TypeInfo.eType(  4 );
	public static final TypeInfo.eType t_union       = new TypeInfo.eType(  5 );
	public static final TypeInfo.eType t_enumeration = new TypeInfo.eType(  6 );
	public static final TypeInfo.eType t_constructor = new TypeInfo.eType(  7 );
	public static final TypeInfo.eType t_function    = new TypeInfo.eType(  8 );
    public static final TypeInfo.eType t__Bool       = new TypeInfo.eType(  9 ); 
	public static final TypeInfo.eType t_bool        = new TypeInfo.eType( 10 );
	public static final TypeInfo.eType t_char        = new TypeInfo.eType( 11 );
	public static final TypeInfo.eType t_wchar_t     = new TypeInfo.eType( 12 );
	public static final TypeInfo.eType t_int         = new TypeInfo.eType( 13 );
	public static final TypeInfo.eType t_float       = new TypeInfo.eType( 14 );
	public static final TypeInfo.eType t_double      = new TypeInfo.eType( 15 );
	public static final TypeInfo.eType t_void        = new TypeInfo.eType( 16 );
	public static final TypeInfo.eType t_enumerator  = new TypeInfo.eType( 17 );
	public static final TypeInfo.eType t_block       = new TypeInfo.eType( 18 );
	public static final TypeInfo.eType t_template    = new TypeInfo.eType( 19 );
	public static final TypeInfo.eType t_asm         = new TypeInfo.eType( 20 );
	public static final TypeInfo.eType t_linkage     = new TypeInfo.eType( 21 );
	public static final TypeInfo.eType t_templateParameter = new TypeInfo.eType( 22 );
	public static final TypeInfo.eType t_typeName    = new TypeInfo.eType( 23 );
	
	//public static final eType t_templateParameter = new eType( 18 );
	
	public static class eType implements Comparable{
		protected eType( int v ){
			_val = v;
		}
		
		public int compareTo( Object o ){
			TypeInfo.eType t = (TypeInfo.eType) o;
			return _val - t._val;
		}
		
		public int toInt(){
			return _val;
		}
		
		private int _val;
	}
	
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
		public PtrOp( TypeInfo.eType type ){
			this.type = type;
		}
		public PtrOp( TypeInfo.eType type, boolean isConst, boolean isVolatile ){
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
		
		public static final TypeInfo.eType t_undef_ptr         = new TypeInfo.eType( 0 );
		public static final TypeInfo.eType t_pointer       = new TypeInfo.eType( 1 );
		public static final TypeInfo.eType t_reference     = new TypeInfo.eType( 2 );
		public static final TypeInfo.eType t_array         = new TypeInfo.eType( 3 );
		public static final TypeInfo.eType t_memberPointer = new TypeInfo.eType( 4 );

		public TypeInfo.eType 	getType()			 			{ return type; }
		public void 	setType( TypeInfo.eType type )			{ this.type = type; }
		
		public boolean 	isConst()						{ return isConstPtr; }
		public boolean 	isVolatile()					{ return isVolatilePtr; }
		public void 	setConst( boolean isConst ) 	{ this.isConstPtr = isConst; }
		public void 	setVolatile(boolean isVolatile)	{ this.isVolatilePtr = isVolatile; }
		
		public ISymbol	getMemberOf()					{ return memberOf; }
		public void 	setMemberOf( ISymbol member )	{ this.memberOf = member;	}
		
		public int compareCVTo( TypeInfo.PtrOp ptr ){
			int cv1 = ( isConst() ? 1 : 0 ) + ( isVolatile() ? 1 : 0 );
			int cv2 = ( ptr.isConst() ? 1 : 0 ) + ( ptr.isVolatile() ? 1 : 0 );
			
			return cv1 - cv2;
		}
		public boolean equals( Object o ){
			if( o == null || !(o instanceof TypeInfo.PtrOp) ){
				return false;
			}	
			TypeInfo.PtrOp op = (TypeInfo.PtrOp)o;
			
			return ( isConst() == op.isConst() &&
					 isVolatile() == op.isVolatile() &&
					 getType() == op.getType() );
		}
		
		private TypeInfo.eType type = PtrOp.t_undef_ptr;
		private boolean isConstPtr = false;
		private boolean isVolatilePtr = false;
		private ISymbol memberOf = null;
	}

	private static final String _image[] = {	"", 		//$NON-NLS-1$	t_undef
												"", 		//$NON-NLS-1$	t_type
												"namespace", //$NON-NLS-1$	t_namespace
												"class", 	//$NON-NLS-1$	t_class
												"struct", 	//$NON-NLS-1$	t_struct
												"union", 	//$NON-NLS-1$	t_union
												"enum",		//$NON-NLS-1$	t_enumeration
												"",			//$NON-NLS-1$	t_constructor
												"",			//$NON-NLS-1$	t_function
												"_Bool",    //$NON-NLS-1$   t__Bool
												"bool",		//$NON-NLS-1$	t_bool
												"char",		//$NON-NLS-1$	t_char
												"wchar_t",	//$NON-NLS-1$	t_wchar_t
												"int",		//$NON-NLS-1$	t_int
												"float",	//$NON-NLS-1$	t_float
												"double",	//$NON-NLS-1$	t_double
												"void",		//$NON-NLS-1$	t_void
												"",			//$NON-NLS-1$	t_enumerator
												"",			//$NON-NLS-1$	t_block	
												"template",	//$NON-NLS-1$	t_template
												"",			//$NON-NLS-1$	t_asm			
												"",			//$NON-NLS-1$	t_linkage
												"",         //$NON-NLS-1$   t_templateParameter
												"typename"  //$NON-NLS-1$   t_typeName
											 };
		// Convenience methods
	public void setBit(boolean b, int mask){
		if( b ){
			_typeBits = _typeBits | mask; 
		} else {
			_typeBits = _typeBits & ~mask; 
		} 
	}
	
	public boolean checkBit(int mask){
		return (_typeBits & mask) != 0;
	}	
	
	public void setType( TypeInfo.eType t){
		_type = t; 
	}
	
	public TypeInfo.eType getType(){ 
		return _type; 
	}

	public boolean isType( TypeInfo.eType type ){
		return isType( type, TypeInfo.t_undef ); 
	}

	public int getTypeInfo(){
		return _typeBits;
	}

	public void setTypeInfo( int typeInfo ){
		_typeBits = typeInfo;
	}

	public eType getTemplateParameterType(){
		return _templateParameterType;
	}
	
	public void setTemplateParameterType( eType type ){
		_templateParameterType = type;
	}
	
	public TypeInfo getFinalType(){
		return ParserSymbolTable.getFlatTypeInfo( this );
	}
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
	public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
		//type of -1 means we don't care
		if( type == TypeInfo.t_any )
			return true;
	
		//upperType of 0 means no range
		if( upperType == TypeInfo.t_undef ){
			return ( getType() == type );
		} 
		return ( getType().compareTo( type ) >= 0 && getType().compareTo( upperType ) <= 0 );
	}
	
	public ISymbol getTypeSymbol(){	
		return _typeDeclaration; 
	}

	public void setTypeSymbol( ISymbol type ){
		_typeDeclaration = type; 
	}

	public boolean hasPtrOperators(){
		return _ptrOperators.size() > 0;	
	}
	
	public List getPtrOperators(){
		return _ptrOperators;
	}
	
	public boolean hasSamePtrs( TypeInfo type ){
		int size = getPtrOperators().size();
		int size2 = type.getPtrOperators().size();
		Iterator iter1 = getPtrOperators().iterator();
		Iterator iter2 = type.getPtrOperators().iterator();
		TypeInfo.PtrOp ptr1 = null, ptr2 = null;
		
		if( size == size2 ){
			if( size > 0 ){
				for( int i = size; i > 0; i-- ){
					ptr1 = (TypeInfo.PtrOp)iter1.next();
					ptr2 = (TypeInfo.PtrOp)iter2.next();
					if( ptr1.getType() != ptr2.getType() ){
						return false;
					}
				}
			}
			return true;
		} 
		return false;
	}

	public List getOperatorExpressions(){
		return _operatorExpressions;
	}
	

	public void applyOperatorExpressions( List ops ){
		if( ops == null || ops.isEmpty() )
			return;
			
		int size = ops.size();
		OperatorExpression op = null;
		for( int i = 0; i < size; i++ ){
			op = (OperatorExpression)ops.get(i);
			if( op == OperatorExpression.indirection ||
				op == OperatorExpression.subscript )
			{
				//indirection operator, can only be applied to a pointer
				//subscript should be applied to something that is "pointer to T", the result is a lvalue of type "T"
				if( hasPtrOperators() ){
					ListIterator iterator = getPtrOperators().listIterator( getPtrOperators().size() );
					TypeInfo.PtrOp last = (TypeInfo.PtrOp)iterator.previous();
					if( last.getType() == TypeInfo.PtrOp.t_pointer ||
						last.getType() == TypeInfo.PtrOp.t_array  )
					{
						iterator.remove();
					}
				}
			} else if( op == OperatorExpression.addressof ){
				//Address-of unary operator, results in pointer to T
				//TODO or pointer to member
				TypeInfo.PtrOp newOp = new TypeInfo.PtrOp( PtrOp.t_pointer );
				addPtrOperator( newOp );
			}
		}
	}

	public void addPtrOperator( TypeInfo.PtrOp ptr ){
		if( _ptrOperators == Collections.EMPTY_LIST ){
			_ptrOperators = new ArrayList(4);
		}
		if( ptr != null )
			_ptrOperators.add( ptr );	
	}
	
	public void addPtrOperator( List ptrs ){
		if( ptrs == null || ptrs.size() == 0 )
			return;
		
		if( _ptrOperators == Collections.EMPTY_LIST ){
			_ptrOperators = new ArrayList( ptrs.size() );
		}
		
		int size = ptrs.size();
		for( int i = 0; i < size; i++ ){
			_ptrOperators.add( ptrs.get( i ) );
		}
	}
	
	public void preparePtrOperators(int numPtrOps) {
		if( _ptrOperators == Collections.EMPTY_LIST )
			_ptrOperators = new ArrayList( numPtrOps );
		else
			((ArrayList) _ptrOperators).ensureCapacity( numPtrOps );
	}
	
	public void addOperatorExpression( OperatorExpression exp ){
		if( _operatorExpressions == Collections.EMPTY_LIST ){
			_operatorExpressions = new ArrayList(4);
		}
		_operatorExpressions.add( exp );
	}
	
	public boolean getHasDefault(){
		return _hasDefaultValue;
	}

	public void setHasDefault( boolean def ){
		_hasDefaultValue = def;
	}
	public void setDefault( Object t ){
		_defaultValue = t;
	}
	public Object getDefault(){
		return _defaultValue;
	}

	public boolean isForwardDeclaration(){
		return checkBit( isForward );
	}
	
	public void setIsForwardDeclaration( boolean forward ){
		setBit( forward, isForward );
	}
	
	/**
	 * canHold
	 * @param type
	 * @return boolean
	 * return true if our type can hold all the values of the passed in
	 * type.
	 * TODO, for now return true if our type is "larger" (based on ordering of
	 * the type values)
	 */
	public boolean canHold( TypeInfo type ){
		if( getType().compareTo( type.getType()) > 0 ){
			return true;
		} 
		int mask = TypeInfo.isShort | TypeInfo.isLong | TypeInfo.isLongLong;
		return ( getTypeInfo() & mask ) >= ( type.getTypeInfo() & mask );
	}

	public boolean equals( Object t ){
		if( t == null || !(t instanceof TypeInfo) ){
			return false;
		}
	
		TypeInfo type = (TypeInfo)t;
	
		boolean result = ( _typeBits == type._typeBits );
		result &= ( _type == type._type );
		
		if( _typeDeclaration != null && type._typeDeclaration != null ){
			if( _typeDeclaration.isType( TypeInfo.t__Bool, TypeInfo.t_void ) &&
				type._typeDeclaration.isType( TypeInfo.t__Bool, TypeInfo.t_void ) )
			{
				//if typeDeclaration is a basic type, then only need the types the same
				result &= ( _typeDeclaration.getType() == type._typeDeclaration.getType() );	
			} else if( _typeDeclaration.isType( TypeInfo.t_function ) &&
					   type._typeDeclaration.isType( TypeInfo.t_function ) )
			{
				//function pointers... functions must have same parameter lists and return types
				IParameterizedSymbol f1 = (IParameterizedSymbol) _typeDeclaration;
				IParameterizedSymbol f2 = (IParameterizedSymbol) type._typeDeclaration;
				
				result &= f1.hasSameParameters( f2 );
				if( f1.getReturnType() != null && f2.getReturnType() != null )
					result &= f1.getReturnType().getTypeInfo().equals( f2.getReturnType().getTypeInfo() );
				else
					result &= (f1.getReturnType() == f2.getReturnType());
			} else if( _typeDeclaration.isType( TypeInfo.t_templateParameter ) &&
					   type._typeDeclaration.isType( TypeInfo.t_templateParameter ) )
			{
				//template parameters
				result &= TemplateEngine.templateParametersAreEquivalent( _typeDeclaration, type._typeDeclaration );
			} else if ( _typeDeclaration instanceof IDeferredTemplateInstance &&
					    type._typeDeclaration instanceof IDeferredTemplateInstance )
			{
				result &= TemplateEngine.deferedInstancesAreEquivalent( (IDeferredTemplateInstance) _typeDeclaration, (IDeferredTemplateInstance)type._typeDeclaration );
			}else {
				//otherwise, its a user defined type, need the decls the same
				result &= ( _typeDeclaration == type._typeDeclaration );
			}	
		} else {
			result &= ( _typeDeclaration == type._typeDeclaration );
		}
			
		int size1 = _ptrOperators.size();
		int size2 = type._ptrOperators.size();
		if( size1 == size2 ){
			if( size1 != 0 ){
				TypeInfo.PtrOp op1 = null, op2 = null;
				for( int i = 0; i < size1; i++ ){
					op1 = (TypeInfo.PtrOp)_ptrOperators.get(i);
					op2 = (TypeInfo.PtrOp)type._ptrOperators.get(i);
					
					if( !op1.equals(op2) ){
						return false;
					}
				}
			}
		} else {
			return false;
		}
		
		return result;
	}

	public String toString(){
		if( isType( TypeInfo.t_type ) ){
			return _typeDeclaration.getName();
		} 
		return TypeInfo._image[ getType().toInt() ];
	}

	private int 	_typeBits = 0;
	private eType   _type = TypeInfo.t_undef;
	private eType	_templateParameterType = t_typeName;
	private ISymbol _typeDeclaration;	

	private boolean	_hasDefaultValue = false;
	private Object _defaultValue = null;
	private List _ptrOperators = Collections.EMPTY_LIST;	
	private List _operatorExpressions = Collections.EMPTY_LIST;
}