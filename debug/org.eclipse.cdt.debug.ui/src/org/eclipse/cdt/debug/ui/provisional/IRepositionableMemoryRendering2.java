/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.provisional;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;

/**
 * An extension to the platform's repositionable rendering interface.
 */
public interface IRepositionableMemoryRendering2 extends IRepositionableMemoryRendering {

	/**
	 * Position the rendering to the given address.
	 * 
	 * @param address
	 *            the address to go to
	 * @param expression
	 *            if the goto address originated as an expression, then this is
	 *            that expression. Null if n/a. <i>This is for informational
	 *            purposes only</i>. Implementation should behave just as if
	 *            {@link IRepositionableMemoryRendering#goToAddress(BigInteger)}
	 *            had been called, though the implementation may want to
	 *            consider the expression when providing the label and/or image.
	 * @throws DebugException
	 *             when there is a problem repositioning the rendering to the
	 *             address
	 */
	public abstract void goToAddress(BigInteger address, String expression) throws DebugException;
}
