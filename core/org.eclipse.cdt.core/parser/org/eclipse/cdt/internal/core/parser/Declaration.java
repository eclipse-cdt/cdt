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
import java.util.Iterator;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class Declaration implements Cloneable {

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

	/**
	 * clone
	 * @see java.lang.Object#clone()
	 * 
	 * implement clone for the purposes of using declarations.
	 * int   		_typeInfo;				//by assignment
	 * String 		_name;					//by assignment
	 * Object 		_object;				//null this out
	 * Declaration	_typeDeclaration;		//by assignment
	 * Declaration	_containingScope;		//by assignment
	 * LinkedList 	_parentScopes;			//shallow copy
	 * LinkedList 	_usingDirectives;		//shallow copy
	 * HashMap		_containedDeclarations;	//shallow copy
	 * int 			_depth;					//by assignment
	 */
	public Object clone(){
		Declaration copy = null;
		try{
			copy = (Declaration)super.clone();
		}
		catch ( CloneNotSupportedException e ){
			//should not happen
			return null;
		}
		
		copy._object = null;
		copy._parentScopes          = ( _parentScopes != null ) ? (LinkedList) _parentScopes.clone() : null;
		copy._usingDirectives       = ( _usingDirectives != null ) ? (LinkedList) _usingDirectives.clone() : null; 
		copy._containedDeclarations = ( _containedDeclarations != null ) ? (HashMap) _containedDeclarations.clone() : null;
		copy._parameters            = ( _parameters != null ) ? (LinkedList) _parameters.clone() : null;
		
		return copy;	
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
	public static final int t_enumeration =  5;
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
	
	public boolean needsDefinition(){
		return _needsDefinition;
	}
	public void setNeedsDefinition( boolean need ) {
		_needsDefinition = need;
	}
	
	public String getCVQualifier(){
		return _cvQualifier;
	}
	
	public void setCVQualifier( String cv ){
		_cvQualifier = cv;
	}
	
	public String getPtrOperator(){
		return _ptrOperator;
	}
	public void setPtrOperator( String ptrOp ){
		_ptrOperator = ptrOp;
	}
	
	public int getReturnType(){
		return _returnType;
	}
	
	public void setReturnType( int type ){
		_returnType = type;
	}
	
	public void addParameter( Declaration typeDecl, String ptrOperator, boolean hasDefault ){
		if( _parameters == null ){
			_parameters = new LinkedList();
		}
		
		ParameterInfo info = new ParameterInfo();
		info.typeInfo = t_type;
		info.typeDeclaration = typeDecl;
		info.ptrOperator = ptrOperator;
		info.hasDefaultValue = hasDefault;
				
		_parameters.add( info );
	}
	
	public void addParameter( int type, String ptrOperator, boolean hasDefault ){
		if( _parameters == null ){
			_parameters = new LinkedList();
		}
		
		ParameterInfo info = new ParameterInfo();
		info.typeInfo = type;
		info.typeDeclaration = null;
		info.ptrOperator = ptrOperator;
		info.hasDefaultValue = hasDefault;
				
		_parameters.add( info );
	}
	
	public boolean hasSameParameters( Declaration function ){
		if( function.getType() != getType() ){
			return false;	
		}
		
		int size = _parameters.size();
		if( function._parameters.size() != size ){
			return false;
		}
		
		Iterator iter = _parameters.iterator();
		Iterator fIter = function._parameters.iterator();
		
		ParameterInfo info = null;
		ParameterInfo fInfo = null;
		
		for( int i = size; i > 0; i-- ){
			info = (ParameterInfo) iter.next();
			fInfo = (ParameterInfo) fIter.next();
			
			if( !info.equals( fInfo ) ){
				return false;
			}
		}
		
			
		return true;
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
	private	boolean	_needsDefinition;		//this name still needs to be defined
	private	String		_cvQualifier;
	private	String		_ptrOperator;
	protected	Declaration	_containingScope;		//the scope that contains us
	protected	LinkedList 	_parentScopes;			//inherited scopes (is base classes)
	protected	LinkedList 	_usingDirectives;		//collection of nominated namespaces
	protected	HashMap 	_containedDeclarations;	//declarations contained by us.
	
	protected LinkedList	_parameters;			//parameter list
	protected int			_returnType;			
	
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
	
	public class ParameterInfo
	{
		public ParameterInfo() {}
		public ParameterInfo( int t, Declaration decl, String ptr, boolean def ){
			typeInfo = t;
			typeDeclaration = decl;
			ptrOperator = ptr;
			hasDefaultValue = def;
		}
		
		public boolean equals( ParameterInfo obj ){
			return	( hasDefaultValue == obj.hasDefaultValue ) &&
				    ( typeInfo == obj.typeInfo ) &&
				    ( typeDeclaration == obj.typeDeclaration ) &&
				    ( ptrOperator.equals( obj.ptrOperator ) );
		}
		
		public boolean	hasDefaultValue;
		public int		typeInfo;
		public Declaration	typeDeclaration;
		public String		ptrOperator;
	}
}
