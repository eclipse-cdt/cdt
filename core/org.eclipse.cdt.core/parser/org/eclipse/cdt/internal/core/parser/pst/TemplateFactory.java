/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.core.parser.pst.ParserSymbolTable.LookupData;

/**
 * @author aniefer
 */
public class TemplateFactory implements ITemplateFactory {

	protected TemplateFactory( ITemplateSymbol primary, List params ){
		templatesList = new LinkedList();
		templatesList.add( primary );
		 
		parametersList = new LinkedList();
		parametersList.add( new LinkedList( params ) );
	}
	
	protected TemplateFactory( List templates, List params ){
		templatesList  = templates;
		parametersList = params;
	}
	
	public ITemplateFactory lookupTemplateForMemberDefinition( String name, List parameters, List arguments ) throws ParserSymbolTableException{
		if( templatesList == null || templatesList.isEmpty() ){
			return null;
		}
		
		ITemplateSymbol template = (ITemplateSymbol) templatesList.get( 0 );
		IContainerSymbol symbol = template.getTemplatedSymbol();
		LookupData data = new LookupData( name, TypeInfo.t_any ); //, null );
		
		ParserSymbolTable.lookup( data, symbol );	
		
		ISymbol look = ParserSymbolTable.resolveAmbiguities( data );
		
		if( look.getContainingSymbol() instanceof ITemplateSymbol ){
			template = TemplateEngine.selectTemplateOrSpecialization( (ITemplateSymbol) look.getContainingSymbol(), parameters, arguments );
			if( template != null ){
				List newTemplatesList = new LinkedList( templatesList  );
				List newParamsList    = new LinkedList( parametersList );
				
				newTemplatesList.add( template );
				newParamsList.add( new LinkedList( parameters ) );
				
				return new TemplateFactory( newTemplatesList, newParamsList );
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory#addSymbol(org.eclipse.cdt.internal.core.parser.pst.ISymbol)
	 */
	public void addSymbol( ISymbol symbol ) throws ParserSymbolTableException {
		Iterator templatesIter  = getTemplatesList().iterator();
		Iterator parametersIter = getParametersList().iterator();
		
		while( templatesIter.hasNext() ){
			Map defnMap = new HashMap();
			
			ITemplateSymbol template = (ITemplateSymbol) templatesIter.next();
			
			Iterator tempIter = template.getParameterList().iterator();
			
			if( !parametersIter.hasNext() ){
				throw new ParserSymbolTableError( ParserSymbolTableError.r_InternalError );
			}
			
			List params = (List) parametersIter.next();
			Iterator iter = params.iterator();
			
			while( iter.hasNext() ){
				ISymbol param = (ISymbol) iter.next();
				ISymbol tempParam = (ISymbol) tempIter.next();
				defnMap.put( param, tempParam );	
			}
			
			template.getDefinitionParameterMap().put( symbol, defnMap );			
		}
				
		ITemplateSymbol template = (ITemplateSymbol) getTemplatesList().get( getTemplatesList().size() - 1 );
		IContainerSymbol container = template.getTemplatedSymbol();
		
		if( container.isForwardDeclaration() &&	container.getTypeSymbol() == symbol ){
			template.addSymbol( symbol );
		} else {
			container.addSymbol( symbol );	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory#lookupMemberForDefinition(java.lang.String)
	 */
	public ISymbol lookupMemberForDefinition(String name) throws ParserSymbolTableException {
		Set keys = getPrimaryTemplate().getContainedSymbols().keySet();
		IContainerSymbol symbol = (IContainerSymbol) getPrimaryTemplate().getContainedSymbols().get( keys.iterator().next() );

		return symbol.lookupMemberForDefinition( name );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory#lookupMemberFunctionForDefinition(java.lang.String, java.util.List)
	 */
	public IParameterizedSymbol lookupMemberFunctionForDefinition( String name, List params) throws ParserSymbolTableException {
		Set keys = getPrimaryTemplate().getContainedSymbols().keySet();
		IContainerSymbol symbol = (IContainerSymbol) getPrimaryTemplate().getContainedSymbols().get( keys.iterator().next() );

		return symbol.lookupMethodForDefinition( name, params );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.ITemplateFactory#getPrimaryTemplate()
	 */
	public ITemplateSymbol getPrimaryTemplate() {
		return (ITemplateSymbol) templatesList.get( 0 );
	}

	public ISymbol lookupParam( String name ) throws ParserSymbolTableException{
		Iterator iter = getParametersList().iterator();
		
		while( iter.hasNext() ){
			List list = (List) iter.next();
			Iterator params = list.iterator();
			while( params.hasNext() ){
				ISymbol p = (ISymbol) params.next();
				if( p.getName().equals( name ) ){
					return p;
				}
			}
		}
		
		return getPrimaryTemplate().lookup( name );
	}
	
	
	protected List getTemplatesList() {
		return templatesList;
	}
	protected List getParametersList() {
		return parametersList;
	}

	private List templatesList;
	private List parametersList;
}
