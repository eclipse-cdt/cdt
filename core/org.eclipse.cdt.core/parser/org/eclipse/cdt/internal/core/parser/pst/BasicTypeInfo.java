/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 5, 2004
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;


/**
 * @author aniefer
 */
public class BasicTypeInfo implements ITypeInfo {

    public void setBit( boolean b, int mask ) {
    	if( b ){
    		_typeBits = _typeBits | mask; 
    	} else {
    		_typeBits = _typeBits & ~mask; 
    	} 
    }

    public boolean checkBit( int mask ) {
    	return (_typeBits & mask) != 0;
    }

    public void setType( ITypeInfo.eType t ) {
    	_type = t; 
    }

    public ITypeInfo.eType getType() { 
    	return _type; 
    }

    public boolean isType( ITypeInfo.eType type ) {
    	return isType( type, t_undef ); 
    }

    public int getTypeBits() {
    	return _typeBits;
    }

    public void setTypeBits( int typeInfo ) {
    	_typeBits = typeInfo;
    }

    /**
     * 
     * @param infoProvider - TypeInfoProvider to use if pooling the TypeInfo created, if null,
     *                       pooling is not used.  If pooling is used, TypeInfoProvider.returnTypeInfo
     *                       must be called when the TypeInfo is no longer needed
     * @return
     */
    public ITypeInfo getFinalType( TypeInfoProvider infoProvider ) {
    	return ParserSymbolTable.getFlatTypeInfo( this, infoProvider ); 
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
    public boolean isType( ITypeInfo.eType type, ITypeInfo.eType upperType ) {
    	//type of -1 means we don't care
    	if( type == t_any )
    		return true;
    
    	//upperType of 0 means no range
    	if( upperType == t_undef ){
    		return ( getType() == type );
    	} 
    	return ( getType().compareTo( type ) >= 0 && getType().compareTo( upperType ) <= 0 );
    }



    public boolean hasPtrOperators() {
    	return _ptrOperators.size() > 0;	
    }

    public List getPtrOperators() {
    	return _ptrOperators;
    }

    public boolean hasSamePtrs( ITypeInfo type ) {
    	int size = getPtrOperators().size();
    	int size2 = type.getPtrOperators().size();
    	ITypeInfo.PtrOp ptr1 = null, ptr2 = null;
    	
    	if( size == size2 ){
    		if( size > 0 ){
    			for( int i = 0; i < size; i++ ){
    				ptr1 = (ITypeInfo.PtrOp)getPtrOperators().get(i);
    				ptr2 = (ITypeInfo.PtrOp)type.getPtrOperators().get(i);
    				if( ptr1.getType() != ptr2.getType() ){
    					return false;
    				}
    			}
    		}
    		return true;
    	} 
    	return false;
    }

    public void applyOperatorExpression( ITypeInfo.OperatorExpression op ) {
    	if( op == null )
    		return;
    		
    	if( op == ITypeInfo.OperatorExpression.indirection ||
    		op == ITypeInfo.OperatorExpression.subscript )
    	{
    		//indirection operator, can only be applied to a pointer
    		//subscript should be applied to something that is "pointer to T", the result is a lvalue of type "T"
    		if( hasPtrOperators() ){
    			ListIterator iterator = getPtrOperators().listIterator( getPtrOperators().size() );
    			ITypeInfo.PtrOp last = (ITypeInfo.PtrOp)iterator.previous();
    			if( last.getType() == ITypeInfo.PtrOp.t_pointer ||
    				last.getType() == ITypeInfo.PtrOp.t_array  )
    			{
    				iterator.remove();
    			}
    		}
    	} else if( op == ITypeInfo.OperatorExpression.addressof ){
    		//Address-of unary operator, results in pointer to T
    		//TODO or pointer to member
    		ITypeInfo.PtrOp newOp = new ITypeInfo.PtrOp( ITypeInfo.PtrOp.t_pointer );
    		addPtrOperator( newOp );
    	}
    }

    public void addPtrOperator( ITypeInfo.PtrOp ptr ) {
        if( ptr != null ){
			if( _ptrOperators == Collections.EMPTY_LIST ){
				_ptrOperators = new ArrayList(4);
			}
    	
    		_ptrOperators.add( ptr );
        }
    }

    public void addPtrOperator( List ptrs ) {
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

    public void preparePtrOperators( int numPtrOps ) {
    	if( _ptrOperators == Collections.EMPTY_LIST )
    		_ptrOperators = new ArrayList( numPtrOps );
    	else
    		((ArrayList) _ptrOperators).ensureCapacity( numPtrOps );
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
    public boolean canHold( ITypeInfo type ) {
    	if( getType().compareTo( type.getType()) > 0 ){
    		return true;
    	} 
    	int mask = isShort | isLong | isLongLong;
    	return ( getTypeBits() & mask ) >= ( type.getTypeBits() & mask );
    }

    public boolean equals( Object t ) {
    	if( t == null || !(t instanceof ITypeInfo) ){
    		return false;
    	}
    
    	ITypeInfo type = (ITypeInfo)t;
    
    	int bits1 = _typeBits & ~isTypedef & ~isForward & ~isStatic & ~isExtern;
    	int bits2 = type.getTypeBits() & ~isTypedef & ~isForward & ~isStatic & ~isExtern;
    	boolean result = ( bits1 == bits2 );
    	
    	result &= ( _type == type.getType() );
    		
//    	Object def1 = getDefault();
//    	Object def2 = type.getDefault();
//    	result &= ( (def1 == null && def2 == null) ||
//    	            (def1 != null && def2 != null && def1.equals( def2 )) );
//    	
    	if( !result )
    	    return false;
    	
    	int size1 = _ptrOperators.size();
    	int size2 = type.getPtrOperators().size();
    	if( size1 == size2 ){
    		if( size1 != 0 ){
    			ITypeInfo.PtrOp op1 = null, op2 = null;
    			for( int i = 0; i < size1; i++ ){
    				op1 = (ITypeInfo.PtrOp)_ptrOperators.get(i);
    				op2 = (ITypeInfo.PtrOp)type.getPtrOperators().get(i);
    				
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

    public String toString() {
    	if( isType( t_type ) && getTypeSymbol() != null ){
    		return getTypeSymbol().getName();
    	} 
    	return _image[ getType().toInt() ];
    }

    public void clear() {
    	_typeBits = 0;
    	_type = t_undef;
    	_ptrOperators = Collections.EMPTY_LIST;
    }

    public void copy( ITypeInfo t ) {
        if( t == null )
            return;
    	_typeBits = t.getTypeBits();
    	_type = t.getType();

    	if( t.getPtrOperators() != Collections.EMPTY_LIST )
    		_ptrOperators = (ArrayList)((ArrayList)t.getPtrOperators()).clone();
    	else 
    		_ptrOperators = Collections.EMPTY_LIST;
    }

    public boolean getHasDefault(){
        return _hasDefault; 
    }
    public void setHasDefault( boolean def ){
        _hasDefault = def; 
    }
    
    /**
     * The following functions are implemented in derived classes
     */
    public ITypeInfo.eType getTemplateParameterType() 				{ return ITypeInfo.t_undef; }
    public void setTemplateParameterType( ITypeInfo.eType type ) 	{ throw new UnsupportedOperationException(); }
    public ISymbol getTypeSymbol() 									{ return null;  } 
    public void setTypeSymbol( ISymbol type ) 						{ if( type != null ) throw new UnsupportedOperationException(); }
    public void setDefault( Object t ) 								{ if( t != null )    throw new UnsupportedOperationException(); }
    public Object getDefault() 										{ return null;  }
    
    protected int _typeBits = 0;
    protected ITypeInfo.eType _type = t_undef;
    protected List _ptrOperators = Collections.EMPTY_LIST;
    protected boolean _hasDefault = false;
}
