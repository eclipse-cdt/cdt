/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 23, 2004
 */
package org.eclipse.cdt.core.dom.ast;

/**
 * @author aniefer
 */
public interface IEnumerator extends IBinding {
	/**
	 * returns the type of this enumeration.  The type of an enumerator
	 * is the enumeration in which it is declared.
	 * 
	 * @return the type of the enumeration
	 */
	public IType getType() throws DOMException;
}
