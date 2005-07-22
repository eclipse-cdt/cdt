/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * This interface is implemented by clients that walk the ICElement tree.
 */
public interface ICElementVisitor {

	/**
	 * Visited a member if the ICElement tree. Returns whether to visit the children
	 * of this element.
	 * 
	 * @param element
	 * @return
	 */
	public boolean visit(ICElement element) throws CoreException;
	
}
