/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.core.dom;

public interface IExpressionOwner {

	public Expression getExpression(); 
	public void setExpression( Expression exp );
}
