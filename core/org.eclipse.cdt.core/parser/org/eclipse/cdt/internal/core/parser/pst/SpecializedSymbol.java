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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aniefe
 */

public class SpecializedSymbol extends TemplateSymbol implements ISpecializedSymbol {
	protected SpecializedSymbol( ParserSymbolTable table, char[] name ){
		super( table, name );
	}
	
	public Object clone(){
		SpecializedSymbol copy = (SpecializedSymbol)super.clone();
		
		copy._argumentList	  = ( _argumentList != Collections.EMPTY_LIST ) ? (List)((ArrayList) _argumentList).clone() : _argumentList;
		
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
		
		List actualArgs = new ArrayList( specArgs.size() );
		
		ISymbol templatedSymbol = getTemplatedSymbol();
		while( templatedSymbol.isTemplateInstance() ){
			templatedSymbol = templatedSymbol.getInstantiatedSymbol();
		}
		
		int numSpecArgs = specArgs.size();
		for( int i = 0; i < numSpecArgs; i++ ){
			ITypeInfo info = (ITypeInfo) specArgs.get(i);
			ITypeInfo mappedInfo = (ITypeInfo) arguments.get(i);
			
			//If the argument is a template parameter, we can't instantiate yet, defer for later
			if( mappedInfo.isType( ITypeInfo.t_type ) && mappedInfo.getTypeSymbol().isType( ITypeInfo.t_templateParameter ) ){
				return deferredInstance( arguments );
			}
			
			actualArgs.add( mappedInfo );
			if( info.isType( ITypeInfo.t_type ) && info.getTypeSymbol().isType( ITypeInfo.t_templateParameter )){
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
		
		List params = getParameterList();
		int numParams = params.size();
		for( int i = 0; i < numParams; i++ ){
			if( !argMap.containsKey( params.get(i) ) )
				return null;
		}
		
		IContainerSymbol instance = findInstantiation( actualArgs );
		if( instance != null ){
			return instance;
		} 
		IContainerSymbol symbol = null;
			
		if( getContainedSymbols().size() == 1 ){
			symbol = (IContainerSymbol)getContainedSymbols().getAt( 0 );
		}
			
		instance = (IContainerSymbol) symbol.instantiate( this, argMap );
		addInstantiation( instance, actualArgs );
		try{
			processDeferredInstantiations();
		} catch( ParserSymbolTableException e ){
			if( e.reason == ParserSymbolTableException.r_RecursiveTemplate ){
				//clean up some.
				removeInstantiation( instance );
			}
			throw e;
		}
		return instance;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#getArgumentList()
	 */
	public List getArgumentList(){
		return _argumentList;
	}
	
	public void prepareArguments( int size ){
		if( _argumentList == Collections.EMPTY_LIST )
			_argumentList = new ArrayList( size );
		else
			((ArrayList)_argumentList).ensureCapacity( size );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IParameterizedSymbol#addArgument(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addArgument(ITypeInfo arg) {
		if( _argumentList == Collections.EMPTY_LIST )
			_argumentList = new ArrayList(4);
		
		_argumentList.add( arg );
		
		//arg.setIsTemplateMember( isTemplateMember() || getType() == TypeInfo.t_template );
		
//		Command command = new AddArgumentCommand( this, arg );
//		getSymbolTable().pushCommand( command );
	}
	
//	static private class AddArgumentCommand extends Command{
//		public AddArgumentCommand( ISpecializedSymbol container, TypeInfo arg ){
//			_decl = container;
//			_arg = arg;
//		}
//		public void undoIt(){
//			_decl.getArgumentList().remove( _arg );
//		}
//
//		private ISpecializedSymbol _decl;
//		private TypeInfo _arg;
//	}
	
	private List      _argumentList = Collections.EMPTY_LIST;	  //template specialization arguments
	private ITemplateSymbol _primaryTemplate; //our primary template
}
