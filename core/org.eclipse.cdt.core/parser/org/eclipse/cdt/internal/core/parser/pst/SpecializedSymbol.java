/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.Command;

/**
 * @author aniefe
 */

public class SpecializedSymbol extends TemplateSymbol implements ISpecializedSymbol {
	protected SpecializedSymbol( ParserSymbolTable table, String name ){
		super( table, name );
	}
	
	protected SpecializedSymbol( ParserSymbolTable table, String name, ISymbolASTExtension obj ){
		super( table, name, obj );
	}
	
	public Object clone(){
		SpecializedSymbol copy = (SpecializedSymbol)super.clone();
		
		copy._argumentList	  = ( _argumentList != ParserSymbolTable.EMPTY_LIST ) ? (LinkedList) _argumentList.clone() : _argumentList;
		
		return copy;	
	}
	
	
	public ITemplateSymbol getPrimaryTemplate(){
		return _primaryTemplate;
	}
	
	public void setPrimaryTemplate( ITemplateSymbol templateSymbol ){
		_primaryTemplate = templateSymbol;
	}
	
	public ISymbol instantiate( List arguments ) throws ParserSymbolTableException{
		Map argMap = new HashMap();
		
		List specArgs = getArgumentList();
		if( specArgs.size() != arguments.size() ){
			return null;
		}
		
		List actualArgs = new LinkedList();
		
		Iterator iter1 = specArgs.iterator();
		Iterator iter2 = arguments.iterator();
		
		ISymbol templatedSymbol = getTemplatedSymbol();
		while( templatedSymbol.isTemplateInstance() ){
			templatedSymbol = templatedSymbol.getInstantiatedSymbol();
		}
		
		while( iter1.hasNext() ){
			TypeInfo info = (TypeInfo) iter1.next();
			TypeInfo mappedInfo = (TypeInfo) iter2.next();
			
			//If the argument is a template parameter, we can't instantiate yet, defer for later
			if( mappedInfo.isType( TypeInfo.t_type ) && mappedInfo.getTypeSymbol().isType( TypeInfo.t_templateParameter ) ){
				return deferredInstance( arguments );
			}
			
			actualArgs.add( mappedInfo );
			if( info.isType( TypeInfo.t_type ) && info.getTypeSymbol().isType( TypeInfo.t_templateParameter )){
				ISymbol param = info.getTypeSymbol();
				
				param = TemplateEngine.translateParameterForDefinition ( templatedSymbol, param, getDefinitionParameterMap() );
				
				if( !argMap.containsKey( param ) ){
					argMap.put( param, mappedInfo );
				}
			}
		}
		
		//sanity check
		if( getParameterList().size() != argMap.size() )
			return null;
		
		Iterator params = getParameterList().iterator();
		while( params.hasNext() ){
			if( !argMap.containsKey( params.next() ) )
				return null;
		}
		
		IContainerSymbol instance = findInstantiation( actualArgs );
		if( instance != null ){
			return instance;
		} 
		IContainerSymbol symbol = null;
			
		if( getContainedSymbols().size() == 1 ){
			Iterator iter = getContainedSymbols().keySet().iterator();
			symbol = (IContainerSymbol)getContainedSymbols().get( iter.next() );
		}
			
		instance = (IContainerSymbol) symbol.instantiate( this, argMap );
		addInstantiation( instance, actualArgs );
		processDeferredInstantiations();
			
		return instance;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getArgumentList()
	 */
	public List getArgumentList(){
		return _argumentList;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addArgument(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addArgument(TypeInfo arg) {
		if( _argumentList == ParserSymbolTable.EMPTY_LIST )
			_argumentList = new LinkedList();
		
		_argumentList.add( arg );
		
		//arg.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
		Command command = new AddArgumentCommand( this, arg );
		getSymbolTable().pushCommand( command );
	}
	
	static private class AddArgumentCommand extends Command{
		public AddArgumentCommand( ISpecializedSymbol container, TypeInfo arg ){
			_decl = container;
			_arg = arg;
		}
		public void undoIt(){
			_decl.getArgumentList().remove( _arg );
		}

		private ISpecializedSymbol _decl;
		private TypeInfo _arg;
	}
	
	private LinkedList      _argumentList = ParserSymbolTable.EMPTY_LIST;	  //template specialization arguments
	private ITemplateSymbol _primaryTemplate; //our primary template
}
