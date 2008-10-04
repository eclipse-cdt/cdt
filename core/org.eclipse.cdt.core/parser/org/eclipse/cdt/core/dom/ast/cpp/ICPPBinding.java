/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Mar 15, 2005
 */
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @author aniefer
 */
public interface ICPPBinding extends IBinding {
	/**
	 * Returns an array of strings representing the qualified name of this binding.
	 */
	public String[] getQualifiedName() throws DOMException;
	public char[][] getQualifiedNameCharArray() throws DOMException;
	
	/**
	 * Returns true if this binding is qualified with respect to the translation unit
	 * for example, local variables, function parameters and local classes will
	 * all return false.
	 * @throws DOMException
	 */
	public boolean isGloballyQualified() throws DOMException;
}
