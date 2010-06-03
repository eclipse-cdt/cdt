/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

/**
 * Some CDI interfaces have a dispose method, but a number of others don't (and
 * should). E.g., ICDIVariable does, but ICDIStackFrame doesn't. This interface
 * was created to introduce a dispose capability to CDI objects that call for it
 * without breaking existing interfaces.
 * 
 * CDT uses instanceof to check whether a CDI object supports this interface and
 * if so calls the dispose method when it has no further need for the object. This
 * does not apply to all CDI object; just ones for which it makes sense. The list
 * is subject to grow:
 * <ul>
 * <li>{@link ICDITarget}
 * <li>{@link ICDIStackFrame}
 * <li>{@link ICDIThread}
 * </ul>
 * @since 6.0
 */
public interface ICDIDisposable {
	/**
	 * Called when the object is no longer needed by CDT.
	 */
	public void dispose();
}
