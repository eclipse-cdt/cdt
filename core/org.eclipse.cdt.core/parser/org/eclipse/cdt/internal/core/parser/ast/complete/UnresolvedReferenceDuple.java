/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;

/**
 * @author jcamelon
 */
public class UnresolvedReferenceDuple {

	public UnresolvedReferenceDuple( IContainerSymbol scope, ITokenDuple name ){
		this.scope = scope;
		this.name = name;
	}
	
	private final IContainerSymbol scope;
	private final ITokenDuple name;

	public IContainerSymbol getScope()
	{
		return scope;
	}
	
	public ITokenDuple      getName()
	{
		return name;
	}
}
