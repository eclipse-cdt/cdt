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

import org.eclipse.cdt.internal.core.parser.scanner2.ObjectMap;

public class BasicSymbol extends ExtensibleSymbol implements ISymbol
{
	
	public BasicSymbol( ParserSymbolTable table, char[] name ){
		super( table );
		_name = name;
	}	
	
	public BasicSymbol( ParserSymbolTable table, char[] name, ITypeInfo.eType typeInfo )
	{
		super( table );
		_name = name;
		_typeInfo = TypeInfoProvider.newTypeInfo( typeInfo );
	}
		
	public ISymbol instantiate( ITemplateSymbol template, ObjectMap argMap ) throws ParserSymbolTableException{
		if( !isTemplateMember() &&  !getContainingSymbol().isTemplateMember() ){
			return null;
		}
		ISymbol newSymbol = (ISymbol) clone();
		newSymbol.setTypeInfo( TemplateEngine.instantiateTypeInfo( newSymbol.getTypeInfo(), template, argMap ) );
		newSymbol.setInstantiatedSymbol( this );
		
		return newSymbol;	
	}
	
	public char[] getName() { return _name; }
	public void setName(char[] name) { _name = name; }


	public void setContainingSymbol( IContainerSymbol scope ){ 
		super.setContainingSymbol( scope );
		_depth = scope.getDepth() + 1; 
	}

	public void setType(ITypeInfo.eType t){
		getTypeInfo().setType( t );	 
	}

	public ITypeInfo.eType getType(){ 
		return getTypeInfo().getType(); 
	}

	public boolean isType( ITypeInfo.eType type ){
		return getTypeInfo().isType( type, ITypeInfo.t_undef ); 
	}

	public boolean isType( ITypeInfo.eType type, ITypeInfo.eType upperType ){
		return getTypeInfo().isType( type, upperType );
	}
	
	public ISymbol getTypeSymbol(){
		ISymbol symbol = getTypeInfo().getTypeSymbol();
		
		if( symbol != null && symbol.isForwardDeclaration() && symbol.getForwardSymbol() != null ){
			return symbol.getForwardSymbol();
		}
		
		return symbol;
	}

	public void setTypeSymbol( ISymbol type ){
		getTypeInfo().setTypeSymbol( type ); 
	}

	public ITypeInfo getTypeInfo(){
		return ( _typeInfo != null ) ? _typeInfo : (_typeInfo = new TypeInfo()); 
	}
	
	public void setTypeInfo( ITypeInfo info ) {
		_typeInfo = info;
	}
	
	public boolean isForwardDeclaration(){
		return _isForwardDeclaration;
	}
	
	public void setIsForwardDeclaration( boolean forward ){
		_isForwardDeclaration = forward;
	}
	public void setForwardSymbol( ISymbol forward ){
		_symbolDef = forward;  
	}
	public ISymbol getForwardSymbol(){
		return (_isForwardDeclaration || isType( ITypeInfo.t_namespace) ) ? _symbolDef : null;
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
			ITypeInfo.PtrOp op1 = null, op2 = null;
			for( int i = 0; i > size; i++ ){
				op1 = (ITypeInfo.PtrOp)symbol.getTypeInfo().getPtrOperators().get(i);
				op2 = (ITypeInfo.PtrOp)getTypeInfo().getPtrOperators().get(i);
	
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
	public void addPtrOperator( ITypeInfo.PtrOp ptrOp ){
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
		return ( _isTemplateInstance && _symbolDef != null );
	}
	public ISymbol getInstantiatedSymbol(){
		return _symbolDef;
	}
	public void setInstantiatedSymbol( ISymbol symbol ){
	    _isTemplateInstance = true;
		_symbolDef = symbol;
	}
	
	public boolean getIsInvisible(){
		return _isInvisible;
	}
	public void setIsInvisible( boolean invisible ){
		_isInvisible = invisible ;
	}
	
	private 	char[] 				_name;					//our name
	private		ITypeInfo			_typeInfo;				//our type info
	private		int 				_depth;					//how far down the scope stack we are
	
	private 	boolean				_isInvisible = false;	//used by friend declarations (11.4-9)
	private		boolean				_isTemplateMember = false;
	private		boolean				_isForwardDeclaration = false;
	private     boolean 			_isTemplateInstance = false;
	private		ISymbol				_symbolDef = null;		//used for forward declarations and template instantiations
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		
		BasicSymbol s = (BasicSymbol) super.clone();
		s._typeInfo = TypeInfoProvider.newTypeInfo( s._typeInfo );
		return s;
	}
}
