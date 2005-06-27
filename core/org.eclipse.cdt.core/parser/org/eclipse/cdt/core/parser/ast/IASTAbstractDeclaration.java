/*******************************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.ast;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTAbstractDeclaration extends IASTTypeSpecifierOwner, ISourceElementCallbackDelegate
{
	public boolean isConst();
	public boolean isVolatile();
	public Iterator getPointerOperators();
	public Iterator getArrayModifiers();
	public Iterator getParameters();
	public ASTPointerOperator getPointerToFunctionOperator();

}
