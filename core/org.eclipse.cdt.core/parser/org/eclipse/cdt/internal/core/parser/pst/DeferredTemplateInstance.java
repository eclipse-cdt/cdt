/**********************************************************************
 * Copyright (c) 2004 IBM - Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DeferredTemplateInstance extends BasicSymbol implements IDeferredTemplateInstance {

	public DeferredTemplateInstance( ParserSymbolTable table, ITemplateSymbol template, List args ){
		super(table, ParserSymbolTable.EMPTY_NAME );
		_template = template;
		_arguments = new ArrayList( args );
		
		setContainingSymbol( template );
		if( template.getTemplatedSymbol() != null )
			setASTExtension( template.getTemplatedSymbol().getASTExtension() );
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDeferredTemplateInstance#getTemplate()
	 */
	public ITemplateSymbol getTemplate() {
		return _template;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.pst.IDeferredTemplateInstance#getArgumentMap()
	 */
	public List getArguments() {
		return _arguments;
	}

	public ISymbol instantiate( ITemplateSymbol template, Map argMap ) throws ParserSymbolTableException{
		List args = getArguments();
		List newArgs = new ArrayList( args.size() );
		int size = args.size();
		for( int i = 0; i < size; i++ ){
			ITypeInfo arg = (ITypeInfo) args.get(i);
			newArgs.add( TemplateEngine.instantiateTypeInfo( arg, template, argMap ) );
		}
		
		ITemplateSymbol deferredTemplate = getTemplate(); 
		if( deferredTemplate.isType( ITypeInfo.t_templateParameter ) && argMap.containsKey( deferredTemplate ) ){
			ITypeInfo i = (ITypeInfo) argMap.get( deferredTemplate );
			deferredTemplate = (ITemplateSymbol) i.getTypeSymbol();
		}
		
		ISymbol instance = deferredTemplate.instantiate( newArgs );
//		if( !( instance instanceof IDeferredTemplateInstance ) )
//			return instance.instantiate( template, argMap );
//		else 
			return instance;
	}
	
	public boolean isType( ITypeInfo.eType type, ITypeInfo.eType upperType ){
		ISymbol symbol = _template.getTemplatedSymbol();
		if( symbol != null )
			return symbol.isType( type, upperType );
		return super.isType( type, upperType );
		
	}
	
	public ITypeInfo.eType getType(){ 		
		ISymbol symbol = _template.getTemplatedSymbol();
		if( symbol != null )
			return symbol.getType();
		return super.getType();
	}
	
	public ITypeInfo getTypeInfo(){
		ISymbol symbol = _template.getTemplatedSymbol();
		if( symbol != null )
			return symbol.getTypeInfo();
		return super.getTypeInfo();
	}

	public boolean isType( ITypeInfo.eType type ){
		return _template.getTemplatedSymbol().isType( type ); 
	}
	
	public ISymbolASTExtension getASTExtension(){
		if( super.getASTExtension() != null )
			return super.getASTExtension();
		else if( _template.getTemplatedSymbol() != null )
			return _template.getTemplatedSymbol().getASTExtension();
		else
			return _template.getASTExtension();
	}
	
	private ITemplateSymbol _template;
	private List			_arguments;
}
