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

import java.util.Iterator;
import java.util.LinkedList;
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
		_arguments = new LinkedList( args );
		
		setContainingSymbol( template );
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
		List newArgs = new LinkedList();
		Iterator iter = args.iterator();
		
		while( iter.hasNext() ){
			TypeInfo arg = (TypeInfo) iter.next();
			newArgs.add( TemplateEngine.instantiateTypeInfo( arg, template, argMap ) );
		}
		
		ITemplateSymbol deferredTemplate = getTemplate(); 
		if( deferredTemplate.isType( TypeInfo.t_templateParameter ) && argMap.containsKey( deferredTemplate ) ){
			TypeInfo i = (TypeInfo) argMap.get( deferredTemplate );
			deferredTemplate = (ITemplateSymbol) i.getTypeSymbol();
		}
		
		ISymbol instance = deferredTemplate.instantiate( newArgs );
//		if( !( instance instanceof IDeferredTemplateInstance ) )
//			return instance.instantiate( template, argMap );
//		else 
			return instance;
	}
	
	public boolean isType( TypeInfo.eType type, TypeInfo.eType upperType ){
		return _template.getTemplatedSymbol().isType( type, upperType );
	}
	
	public TypeInfo.eType getType(){ 
		return _template.getTemplatedSymbol().getType(); 
	}

	public boolean isType( TypeInfo.eType type ){
		return _template.getTemplatedSymbol().isType( type ); 
	}

	public ISymbolASTExtension getASTExtension() {
		return _template.getTemplatedSymbol().getASTExtension();
	}
	
	private ITemplateSymbol _template;
	private List			_arguments;
}
