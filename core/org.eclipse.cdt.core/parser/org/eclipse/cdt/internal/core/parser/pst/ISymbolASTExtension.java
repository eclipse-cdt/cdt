/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;


import java.util.Iterator;

import org.eclipse.cdt.internal.core.parser.ast.complete.ASTNode;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTSymbol;

/**
 * @author jcamelon
 *
 */
public interface ISymbolASTExtension extends ISymbolOwner
{
	public class ExtensionException extends Exception
	{
	}
	
	
	public ASTNode       getPrimaryDeclaration();
	public IExtensibleSymbol getExtensibleSymbol();
	public Iterator        getAllDefinitions();
	public void            addDefinition( ASTSymbol definition ) throws ExtensionException; 	

}
