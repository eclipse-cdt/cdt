/**********************************************************************
 * Copyright (c) 2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
/*
 * Created on Nov 4, 2003
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;
import java.util.Map;



public class BasicSymbol extends ExtensibleSymbol implements ISymbol
{
	
	public BasicSymbol( ParserSymbolTable table, String name ){
		super( table );
		_name = name;
		_typeInfo = new TypeInfo();
	}
	
	public BasicSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, obj );
		_name   = name;
		_typeInfo = new TypeInfo();
	}
	
	public BasicSymbol( ParserSymbolTable table, String name, TypeInfo.eType typeInfo )
	{
		super( table );
		_name = name;
		_typeInfo = new TypeInfo( typeInfo, 0, null );
	}
		
	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() &&  !getContainingSymbol().isTemplateMember() ){
			return null;
		}
		ISymbol newSymbol = (ISymbol) clone();
		newSymbol.setTypeInfo( TemplateEngine.instantiateTypeInfo( newSymbol.getTypeInfo(), template, argMap ) );
		newSymbol.setInstantiatedSymbol( this );
		
		return newSymbol;	
	}
	
	public String getName() { return _name; }
	public void setName(String name) { _name = name; }


	public void setContainingSymbol( IContainerSymbol scope ){ 
		super.setContainingSymbol( scope );
		_depth = scope.getDepth() + 1; 
	}

	public void setType(TypeInfo.eType t){
		getTypeInfo().setType( t );	 
	}

	public TypeInfo.eType getType(){ 
		return getTypeInfo().getType(); 
	}

	public boolean isType( TypeInfo.eType type ){
		return getTypeInfo().isType( type, TypeInfo.t_undef ); 
	}

	public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
		return getTypeInfo().isType( type, upperType );
	}
	
	public ISymbol getTypeSymbol(){
		ISymbol symbol = getTypeInfo().getTypeSymbol();
		
		if( symbol != null && symbol.getTypeInfo().isForwardDeclaration() && symbol.getTypeSymbol() != null ){
			return symbol.getTypeSymbol();
		}
		
		return symbol;
	}

	public void setTypeSymbol( ISymbol type ){
		getTypeInfo().setTypeSymbol( type ); 
	}

	public TypeInfo getTypeInfo(){
		return _typeInfo; 
	}
	
	public void setTypeInfo( TypeInfo info ) {
		_typeInfo = info;
	}
	
	public boolean isForwardDeclaration(){
		return getTypeInfo().isForwardDeclaration();
	}
	
	public void setIsForwardDeclaration( boolean forward ){
		getTypeInfo().setIsForwardDeclaration( forward );
	}
	
	/**
	 * returns 0 if same, non zero otherwise
	 */
	public int compareCVQualifiersTo( ISymbol symbol ){
		int size = symbol.getTypeInfo().hasPtrOperators() ? symbol.getTypeInfo().getPtrOperators().size() : 0;
		int size2 = getTypeInfo().hasPtrOperators() ? getTypeInfo().getPtrOperators().size() : 0;
			
		if( size != size2 ){
			return size2 - size;
		} else if( size == 0 ) 
			return 0; 
		else {
			TypeInfo.PtrOp op1 = null, op2 = null;
			for( int i = 0; i > size; i++ ){
				op1 = (TypeInfo.PtrOp)symbol.getTypeInfo().getPtrOperators().get(i);
				op2 = (TypeInfo.PtrOp)getTypeInfo().getPtrOperators().get(i);
	
				if( op1.compareCVTo( op2 ) != 0 ){
					return -1;
				}
			}
		}
		
		return 0;
	}
	
	public List getPtrOperators(){
		return getTypeInfo().getPtrOperators();
	}
	public void addPtrOperator( TypeInfo.PtrOp ptrOp ){
		getTypeInfo().addPtrOperator( ptrOp );
	}	
	public void preparePtrOperatros(int numPtrOps) {
		getTypeInfo().preparePtrOperators( numPtrOps );
	}		

	
	public int getDepth(){
		return _depth;
	}
	
	public boolean isTemplateMember(){
		return _isTemplateMember;
	}
	public void setIsTemplateMember( boolean isMember ){
		_isTemplateMember = isMember;
	}
	public boolean isTemplateInstance(){
		return ( _instantiatedSymbol != null );
	}
	public ISymbol getInstantiatedSymbol(){
		return _instantiatedSymbol;
	}
	public void setInstantiatedSymbol( ISymbol symbol ){
		_instantiatedSymbol = symbol;
	}
	
	public boolean getIsInvisible(){
		return _isInvisible;
	}
	public void setIsInvisible( boolean invisible ){
		_isInvisible = invisible ;
	}
	
	private 	String 				_name;					//our name
	private		TypeInfo			_typeInfo;				//our type info
	private		int 				_depth;					//how far down the scope stack we are
	
	private 	boolean				_isInvisible = false;	//used by friend declarations (11.4-9)
	
	private		boolean				_isTemplateMember = false;
	private		ISymbol				_instantiatedSymbol = null;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		
		BasicSymbol s = (BasicSymbol) super.clone();
		s._typeInfo = new TypeInfo( s._typeInfo );
		return s;
	}
}
