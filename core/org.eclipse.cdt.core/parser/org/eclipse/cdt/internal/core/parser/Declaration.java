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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.util.TypeInfo;

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
		_typeInfo = new TypeInfo();
	}

	public Declaration( String name ){
		super();
		_name = name;
		_typeInfo = new TypeInfo();
	}
	
	public Declaration( String name, Object obj ){
		super();
		_name   = name;
		_object = obj;
		_typeInfo = new TypeInfo();
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
	
	public void setType(int t) throws ParserSymbolTableException{
		_typeInfo.setType( t );	 
	}
	
	public int getType(){ 
		return _typeInfo.getType(); 
	}
	
	public boolean isType( int type ){
		return _typeInfo.isType( type, 0 ); 
	}

	public boolean isType( int type, int upperType ){
		return _typeInfo.isType( type, upperType );
	}
		
	public Declaration getTypeDeclaration(){	
		return _typeInfo.getTypeDeclaration(); 
	}
	
	public void setTypeDeclaration( Declaration type ){
		_typeInfo.setTypeDeclaration( type ); 
	}
	
	public TypeInfo getTypeInfo(){
		return _typeInfo;
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
	
	public int getCVQualifier(){
		return _cvQualifier;
	}
	
	public void setCVQualifier( int cv ){
		_cvQualifier = cv;
	}
	
	public String getPtrOperator(){
		return _typeInfo.getPtrOperator();
	}
	public void setPtrOperator( String ptrOp ){
		_typeInfo.setPtrOperator( ptrOp );
	}
	
	public int getReturnType(){
		return _returnType;
	}
	
	public void setReturnType( int type ){
		_returnType = type;
	}
	
	public void addParameter( Declaration typeDecl, int cvQual, String ptrOperator, boolean hasDefault ){
		if( _parameters == null ){
			_parameters = new LinkedList();
		}
		
		TypeInfo info = new TypeInfo( TypeInfo.t_type, typeDecl, cvQual, ptrOperator, hasDefault );
				
		_parameters.add( info );
	}
	
	public void addParameter( int type, int cvQual, String ptrOperator, boolean hasDefault ){
		if( _parameters == null ){
			_parameters = new LinkedList();
		}
		
		TypeInfo info = new TypeInfo(type, null, cvQual, ptrOperator, hasDefault );
				
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
		
		TypeInfo info = null;
		TypeInfo fInfo = null;
		
		for( int i = size; i > 0; i-- ){
			info = (TypeInfo) iter.next();
			fInfo = (TypeInfo) fIter.next();
			
			if( !info.equals( fInfo ) ){
				return false;
			}
		}
		
			
		return true;
	}
	
	private 	String 		_name;					//our name
	private	Object 		_object;				//the object associated with us
	private	boolean	_needsDefinition;		//this name still needs to be defined
	private	int		_cvQualifier;
	//private	String		_ptrOperator;
	protected	TypeInfo	_typeInfo;				//our type info
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
}
