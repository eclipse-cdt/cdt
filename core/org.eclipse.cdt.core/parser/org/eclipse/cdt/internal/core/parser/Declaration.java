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

package org.eclipse.cdt.internal.core.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class Declaration {

	/**
	 * Constructor for Declaration.
	 */
	public Declaration(){
		super();
	}

	public Declaration( String name ){
		_name = name;
	}
	
	public Declaration( String name, Object obj ){
		_name   = name;
		_object = obj;
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
	
	// Types
	// Note that these should be considered ordered and if you change
	// the order, you should consider the ParserSymbolTable uses
	public static final int t_type        =  0; // Type Specifier
	public static final int t_namespace   =  1;
	public static final int t_class       =  2;
	public static final int t_struct      =  3;
	public static final int t_union       =  4;
	public static final int t_enum        =  5;
	public static final int t_function    =  6;
	public static final int t_char        =  7;
	public static final int t_wchar_t     =  8;
	public static final int t_bool        =  9;
	public static final int t_int         = 10;
	public static final int t_float       = 11;
	public static final int t_double      = 12;
	public static final int t_void        = 13;
	public static final int t_enumerator  = 14;
	

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
		//setting our type to a declaration implies we are type t_type
		try { 
			setType( t_type ); 
		} catch (ParserSymbolTableException e) { 
			/*will never happen*/ 
		}
		
		_typeDeclaration = type; 
	}
	
	public String getName() { return _name; }
	public void setName(String name) { _name = name; }
	
	public Object getObject() { return _object; }
	public void setObject( Object obj ) { _object = obj; }
	
	public Declaration	getContainingScope() { return _containingScope; }
	protected void setContainingScope( Declaration scope ){ 
		_containingScope = scope;
		_depth = scope._depth + 1; 
	}
	
	public void addParent( Declaration parent ){
		addParent( parent, false );
	}
	public void addParent( Declaration parent, boolean virtual ){
		if( _parentScopes == null ){
			_parentScopes = new LinkedList();
		}
			
		_parentScopes.add( new ParentWrapper( parent, virtual ) );
	}
	
	public Map getContainedDeclarations(){
		return _containedDeclarations;
	}
	
	public Map createContained(){
		if( _containedDeclarations == null )
			_containedDeclarations = new HashMap();
		
		return _containedDeclarations;
	}

	public LinkedList getParentScopes(){
		return _parentScopes;
	}
	
	// Convenience methods
	private void setBit(boolean b, int mask){
		if( b ){
			_typeInfo = _typeInfo | mask; 
		} else {
			_typeInfo = _typeInfo & ~mask; 
		} 
	}
	
	private boolean checkBit(int mask){
		return (_typeInfo & mask) != 0;
	}	
	
	private 	int   		_typeInfo;				//our type info
	private 	String 		_name;					//our name
	private	Object 		_object;				//the object associated with us
	private 	Declaration	_typeDeclaration;		//our type if _typeInfo says t_type
	
	protected	Declaration	_containingScope;		//the scope that contains us
	protected	LinkedList 	_parentScopes;			//inherited scopes (is base classes)
	protected	LinkedList 	_usingDirectives;		//collection of nominated namespaces
	protected	Map    		_containedDeclarations;	//declarations contained by us.
	
	protected	int 		_depth;					//how far down the scope stack we are
		
	protected class ParentWrapper
	{
		public ParentWrapper( Declaration p, boolean v ){
			parent    = p;
			isVirtual = v;
		}
		
		public boolean isVirtual = false;
		public Declaration parent = null;
	}
	
}
