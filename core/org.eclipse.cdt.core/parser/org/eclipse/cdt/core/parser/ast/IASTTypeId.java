/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.ast;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public interface IASTTypeId extends ISourceElementCallbackDelegate
{
	public IASTSimpleTypeSpecifier.Type getKind(); 
	public String getTypeOrClassName(); 
	public char[] getTypeOrClassNameCharArray();
	public Iterator getPointerOperators();
	public Iterator getArrayModifiers();
	
	public boolean isConst();
	public boolean isVolatile();
	public boolean    isLong(); 
	public boolean    isShort(); 
	public boolean    isSigned(); 
	public boolean    isUnsigned();
	public boolean    isTypename();
	
	
	public String  getFullSignature(); 
	public char[]  getFullSignatureCharArray();
	public ISymbol getTypeSymbol() throws ASTNotImplementedException;
	/**
	 * @param manager
	 */
	public void freeReferences(IReferenceManager manager); 
}
