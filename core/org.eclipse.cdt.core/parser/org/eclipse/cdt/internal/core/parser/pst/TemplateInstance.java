/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Nov 6, 2003
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.Iterator;
import java.util.Map;


public class TemplateInstance extends BasicSymbol
{
	private final ParserSymbolTable _table;
	
	protected TemplateInstance( ParserSymbolTable table, ISymbol symbol, Map argMap ){
		super(table, ParserSymbolTable.EMPTY_NAME );
		this._table = table;
		_instantiatedSymbol = symbol;
		symbol.setTemplateInstance( this );
		_argumentMap = argMap;
	}
	
	public boolean equals( Object t ){
		if( t == null || !( t instanceof TemplateInstance ) ){ 
			return false;
		}
		
		TemplateInstance instance = (TemplateInstance) t;
		
		if( _instantiatedSymbol != instance._instantiatedSymbol ){
			return false;
		}
		
		//check arg map
		Iterator iter1 = _argumentMap.keySet().iterator();
		Iterator iter2 = instance._argumentMap.keySet().iterator();
		int size = _argumentMap.size();
		int size2 = instance._argumentMap.size();
		ISymbol t1 = null, t2 = null;
		if( size == size2 ){
			for( int i = size; i > 0; i-- ){
				t1 = (ISymbol)iter1.next();
				t2 = (ISymbol)iter2.next();
				if( t1 != t2 || !_argumentMap.get(t1).equals( instance._argumentMap.get(t2) ) ){
					return false;								
				}
			}
		}
		
		return true;
	}
	
	public ISymbol getInstantiatedSymbol(){
		_instantiatedSymbol.setTemplateInstance( this );
		return _instantiatedSymbol;
	}
	
	public TypeInfo.eType getType(){
		ISymbol symbol = _instantiatedSymbol;
		TypeInfo.eType returnType = _instantiatedSymbol.getType();
		if( returnType == TypeInfo.t_type ){
			symbol = symbol.getTypeSymbol();
			TypeInfo info = null;	
			while( symbol != null && symbol.getType() == TypeInfo.t_undef && symbol.getContainingSymbol().getType() == TypeInfo.t_template ){
				info = (TypeInfo) _argumentMap.get( symbol );
				if( !info.isType( TypeInfo.t_type ) ){
					break;
				}
				symbol = info.getTypeSymbol();
			}
			
			return ( info != null ) ? info.getType() : TypeInfo.t_type;
		}
		
		return returnType; 
	}

	public boolean isType( TypeInfo.eType type ){
		return ( type == TypeInfo.t_any || getType() == type );
	}

	public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
		if( type == TypeInfo.t_any )
			return true;

		if( upperType == TypeInfo.t_undef ){
			return ( getType() == type );
		} else {
			return ( getType().compareTo( type ) >= 0 && getType().compareTo( upperType ) <= 0 );
		}
	}
	
	public ISymbol getTypeSymbol(){
		ISymbol symbol = _instantiatedSymbol.getTypeSymbol();
		if( symbol != null && symbol.getType() == TypeInfo.t_undef && 
							  symbol.getContainingSymbol().getType() == TypeInfo.t_template )
		{
			TypeInfo info = (TypeInfo) _argumentMap.get( symbol );
			return ( info != null ) ? info.getTypeSymbol() : null;	
		}
		
		return symbol; 
	}

	public TypeInfo getTypeInfo(){
		ISymbol symbol = _instantiatedSymbol.getTypeSymbol();
		if( symbol != null && symbol.getType() == TypeInfo.t_undef && 
							  symbol.getContainingSymbol().getType() == TypeInfo.t_template )
		{
			TypeInfo info = (TypeInfo) _argumentMap.get( symbol );
			return info;
		}
		
		return _instantiatedSymbol.getTypeInfo();
	}
		
	public Map getArgumentMap(){
		return _argumentMap;
	}

	
	private ISymbol			 _instantiatedSymbol;
	//private LinkedList		 _arguments;
	private Map			 _argumentMap;
}
