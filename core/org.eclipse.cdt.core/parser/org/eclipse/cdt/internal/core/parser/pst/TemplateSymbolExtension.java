/*
 * Created on Mar 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TemplateSymbolExtension extends StandardSymbolExtension {

	protected ISymbol replacement = null;
	
	/**
	 * @param symbol
	 * @param primaryDeclaration
	 */
	public TemplateSymbolExtension(ISymbol symbol, ASTSymbol primaryDeclaration) {
		super(symbol, primaryDeclaration);
	}

	/**
	 * @param spec
	 */
	public void replaceSymbol(ISpecializedSymbol spec) {
		replacement = spec; 
		spec.setASTExtension( this );
	}
	
	public ISymbol getSymbol(){
		return ( replacement == null ) ? super.getSymbol() : replacement;
	}
}
