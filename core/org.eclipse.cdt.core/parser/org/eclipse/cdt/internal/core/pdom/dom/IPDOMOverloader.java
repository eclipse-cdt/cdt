/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for any element in the PDOM that can have the same name as a
 * sibling, but differ in other ways (i.e. function parameters, template
 * arguments).
 * 
 * @author Bryan Wilkinson
 */
public interface IPDOMOverloader {
	
	/**
	 * Gets the signature memento for this PDOM element, which will be unique
	 * for all sibling IPDOMOverloaders with the same name.
	 * 
	 * @return
	 * @throws CoreException
	 */
	public int getSignatureMemento() throws CoreException;
}
