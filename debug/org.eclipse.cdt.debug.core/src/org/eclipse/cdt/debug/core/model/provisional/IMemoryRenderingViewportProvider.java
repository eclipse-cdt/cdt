/*******************************************************************************
 * Copyright (c) 2010, Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model.provisional;

import java.math.BigInteger;

/**
 * An add-on interface for memory renderings to provide access to the first
 * address being displayed. As the user scrolls the rendering, this value
 * changes.
 */
public interface IMemoryRenderingViewportProvider {
	
	/**
	 * Return the first address being shown in the rendering. Subject to scrolling.
	 */
	BigInteger getViewportAddress();
}
