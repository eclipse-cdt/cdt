/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

/**
 * Interface common to all view parts that provide an input.
 */
public interface IViewPartInputProvider {
	
	/**
	 * Returns the input.
	 *
	 * @return the input object
	 */
	public Object getViewPartInput();

}
