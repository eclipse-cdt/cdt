/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.parser.util;

import org.eclipse.cdt.internal.core.parser.Declaration;
import org.eclipse.cdt.internal.core.parser.ParserSymbolTableException;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class TypeInfo{
	public TypeInfo(){
		super();	
	}
	
	public TypeInfo( int type, Declaration decl ){
		super();
		_typeInfo = type;
		_typeDeclaration = decl;	
	}
	
	public TypeInfo( int type, Declaration decl, int cvQualifier, String ptrOp, boolean hasDefault ){
		super();
		_typeInfo = type;
		_typeDeclaration = decl;
		_cvQualifier = cvQualifier;
		_ptrOperator = ptrOp;
		_hasDefaultValue = hasDefault;
	}
		
	public static final int typeMask   = 0x001f;
	public static final int isAuto     = 0x0020;
	public static final int isRegister = 0x0040;
	public static final int isStatic   = 0x0080;
	public static final int isExtern   = 0x0100;
	public static final int isMutable  = 0x0200;
	public static final int isInline   = 0x0400;
	public static final int isVirtual  = 0x0800;
	public static final int isExplicit = 0x1000;
	public static final int isTypedef  = 0x2000;
	public static final int isFriend   = 0x4000;
	public static final int isConst    = 0x8000;
	public static final int isVolatile = 0x10000;
	public static final int isUnsigned = 0x20000;
	public static final int isShort    = 0x40000;
	public static final int isLong     = 0x80000;
		
	// Types (maximum type is typeMask
	// Note that these should be considered ordered and if you change
	// the order, you should consider the ParserSymbolTable uses
	public static final int t_type        =  0; // Type Specifier
	public static final int t_namespace   =  1;
	public static final int t_class       =  2;
	public static final int t_struct      =  3;
	public static final int t_union       =  4;
	public static final int t_enumeration =  5;
	public static final int t_function    =  6;
	public static final int t_bool        =  7;
	public static final int t_char        =  8;
	public static final int t_wchar_t     =  9;
	public static final int t_int         = 10;
	public static final int t_float       = 11;
	public static final int t_double      = 12;
	public static final int t_void        = 13;
	public static final int t_enumerator  = 14;
		
	//Partial ordering :
	// none		< const
	// none     < volatile
	// none		< const volatile
	// const	< const volatile
	// volatile < const volatile
	public static final int cvConst 			= 1;
	public static final int cvVolatile 		= 2;
	public static final int cvConstVolatile 	= 4;
	
		// Convenience methods
	public void setBit(boolean b, int mask){
		if( b ){
			_typeInfo = _typeInfo | mask; 
		} else {
			_typeInfo = _typeInfo & ~mask; 
		} 
	}
		
	public boolean checkBit(int mask){
		return (_typeInfo & mask) != 0;
	}	
		
	public void setType(int t) throws ParserSymbolTableException{ 
		//sanity check, t must fit in its allocated 5 bits in _typeInfo
		if( t > typeMask ){
			throw new ParserSymbolTableException( ParserSymbolTableException.r_BadTypeInfo );
		}
		
		_typeInfo = _typeInfo & ~typeMask | t; 
	}
		
	public int getType(){ 
		return _typeInfo & typeMask; 
	}
	
	public boolean isType( int type ){
		return isType( type, 0 ); 
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
	public boolean isType( int type, int upperType ){
		//type of -1 means we don't care
		if( type == -1 )
			return true;
		
		//upperType of 0 means no range
		if( upperType == 0 ){
			return ( getType() == type );
		} else {
			return ( getType() >= type && getType() <= upperType );
		}
	}
		
	public Declaration getTypeDeclaration(){	
		return _typeDeclaration; 
	}
	
	public void setTypeDeclaration( Declaration type ){
		_typeDeclaration = type; 
	}
	
	public int getCVQualifier(){
		return _cvQualifier;
	}
	
	public void setCVQualifier( int cv ){
		_cvQualifier = cv;
	}
	
	public String getPtrOperator(){
		return _ptrOperator;
	}
	
	public void setPtrOperator( String ptr ){
		_ptrOperator = ptr;
	}
	
	public boolean getHasDefault(){
		return _hasDefaultValue;
	}

	public void setHasDefault( boolean def ){
		_hasDefaultValue = def;
	}

	/**
	 * canHold
	 * @param type
	 * @return boolean
	 * return true is the our type can hold all the values of the passed in
	 * type.
	 * TBD, for now return true if our type is "larger" (based on ordering of
	 * the type values)
	 */
	public boolean canHold( TypeInfo type ){
		return getType() >= type.getType();	
	}
	
	private int 		 _typeInfo = 0;
	private Declaration _typeDeclaration;	
	private int		 _cvQualifier = 0;
	
	private boolean	_hasDefaultValue = false;
	private String		_ptrOperator;	
}