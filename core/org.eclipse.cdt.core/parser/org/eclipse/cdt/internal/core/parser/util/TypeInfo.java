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
		_ptrOperator = ( ptrOp != null ) ? new String( ptrOp ) : null;
		_hasDefaultValue = hasDefault;
	}
	
	public TypeInfo( TypeInfo info ){
		super();
		
		_typeInfo = info._typeInfo;
		_typeDeclaration = info._typeDeclaration;
		_cvQualifier = info._cvQualifier;
		_ptrOperator = ( info._ptrOperator == null ) ? null : new String( info._ptrOperator );
		_hasDefaultValue = info._hasDefaultValue;
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
	public static final int t_undef       =  0; //not specified
	public static final int t_type        =  1; // Type Specifier
	public static final int t_namespace   =  2;
	public static final int t_class       =  3;
	public static final int t_struct      =  4;
	public static final int t_union       =  5;
	public static final int t_enumeration =  6;
	public static final int t_function    =  7;
	public static final int t_bool        =  8;
	public static final int t_char        =  9;
	public static final int t_wchar_t     = 10;
	public static final int t_int         = 11;
	public static final int t_float       = 12;
	public static final int t_double      = 13;
	public static final int t_void        = 14;
	public static final int t_enumerator  = 15;
		
	private static final String _image[] = {	"", 
												"", 
												"namespace", 
												"class", 
												"struct", 
												"union", 
												"enum",
												"",
												"bool",
												"char",
												"wchar_t",
												"int",
												"float",
												"double",
												"void",
												""
											 };
	//Partial ordering :
	// none		< const
	// none     < volatile
	// none		< const volatile
	// const	< const volatile
	// volatile < const volatile
	public static final int cvConst 			= 2;
	public static final int cvVolatile 		= 3;
	public static final int cvConstVolatile 	= 5;
	
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
	
	public int getTypeInfo(){
		return _typeInfo;
	}
	
	public void setTypeInfo( int typeInfo ){
		_typeInfo = typeInfo;
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

	public void addCVQualifier( int cv ){
		switch( _cvQualifier ){
			case 0:
				_cvQualifier = cv;
				break;
				
			case cvConst:
				if( cv != cvConst ){
					_cvQualifier = cvConstVolatile;
				}
				break;
			
			case cvVolatile:
				if( cv != cvVolatile ){
					_cvQualifier = cvConstVolatile;
				}
				break;
			
			case cvConstVolatile:
				break;	//nothing to do
		}
	}
	
	public String getPtrOperator(){
		return _ptrOperator;
	}
	
	public void setPtrOperator( String ptr ){
		_ptrOperator = ptr;
	}
	
	public void addPtrOperator( String ptr ){
		if( ptr == null ){
			return;
		}
		
		char chars[] = ( _ptrOperator == null ) ? ptr.toCharArray() : ( ptr + _ptrOperator ).toCharArray();
		
		int nChars = ( _ptrOperator == null ) ? ptr.length() : ptr.length() + _ptrOperator.length();
		
		char dest[] = new char [ nChars ];
		int j = 0;
		
		char currChar, nextChar, tempChar;
		
		for( int i = 0; i < nChars; i++ ){
			currChar = chars[ i ];
			nextChar = ( i + 1 < nChars ) ? chars[ i + 1 ] : 0;
			
			switch( currChar ){
				case '&':{
					switch( nextChar ){
						case '[':
							tempChar = ( i + 2 < nChars ) ? chars[ i + 2 ] : 0;
							if( tempChar == ']' ){
								i++;
								nextChar = '*'; 
							}
							//fall through to '*'
						case '*':
							i++;
							break;
						case '&':
						default:
							dest[ j++ ] = currChar;
							break;
					}
					break;
				}
				case '[':{
					if( nextChar == ']' ){
						i++;
						currChar = '*';
						nextChar = ( i + 2 < nChars ) ? chars[ i + 2 ] : 0;
					}
					//fall through to '*'
				}
				case '*':{
					
					if( nextChar == '&' ){
						i++;
					} else {
						dest[ j++ ] = currChar;
					}
					break;
				}
				default:
					break;

			}
		}
		
		_ptrOperator = new String( dest, 0, j );
	}
	
	public String getInvertedPtrOperator(){
		if( _ptrOperator == null ){
			return null;
		}
		
		char chars[] = _ptrOperator.toCharArray();
		int nChars = _ptrOperator.length();
		
		char dest[] = new char [ nChars ];
		char currChar;
		
		for( int i = 0; i < nChars; i++ ){
			currChar = chars[ i ];
			switch( currChar ){
				case '*' :	dest[ i ] = '&'; 		break;
				case '&' :	dest[ i ] = '*'; 		break;
				default: 	dest[ i ] = currChar;	break;
			}
		}
		
		return new String( dest );
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
	
	public boolean equals( Object t ){
		if( t == null || !(t instanceof TypeInfo) ){
			return false;
		}
		
		TypeInfo type = (TypeInfo)t;
		
		boolean result = ( _typeInfo == type._typeInfo );
		result &= ( _typeDeclaration == type._typeDeclaration );
		result &= ( _cvQualifier == type._cvQualifier );
		
		String op1 = ( _ptrOperator != null && _ptrOperator.equals("") ) ? null : _ptrOperator;
		String op2 = ( type._ptrOperator != null && type._ptrOperator.equals("") ) ? null : type._ptrOperator;
		result &= (( op1 != null && op2 != null && op1.equals( op2 ) ) || op1 == op2 );
		
		return result;
	}
	
	public String toString(){
		if( isType( t_type ) ){
			return _typeDeclaration.getName();
		} else {
			return _image[ getType() ];
		}
	}

	private int 		 _typeInfo = 0;
	private Declaration _typeDeclaration;	
	private int		 _cvQualifier = 0;
	
	private boolean	_hasDefaultValue = false;
	private String		_ptrOperator;	
}