/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.model;

public interface IEnumerator extends ICElement, ISourceManipulation{

	/**
	 * Returns the enumerator's constant expression if any.
	 * Returns null otherwise.
	 * @return String
	 */
	String getConstantExpression();
}
