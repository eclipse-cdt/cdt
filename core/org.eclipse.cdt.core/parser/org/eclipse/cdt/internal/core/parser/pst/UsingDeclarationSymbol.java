/*
 * Created on Feb 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class UsingDeclarationSymbol extends ExtensibleSymbol implements IUsingDeclarationSymbol {

	public UsingDeclarationSymbol( ParserSymbolTable table, List referenced, List declared ){
		super( table );
		referencedSymbol = referenced;
		declaredSymbol = declared; 
	}

	public List getReferencedSymbols() { return referencedSymbol; }
	public List getDeclaredSymbols()   { return declaredSymbol;   }
	
	private final List referencedSymbol;
	private final List declaredSymbol;
}
