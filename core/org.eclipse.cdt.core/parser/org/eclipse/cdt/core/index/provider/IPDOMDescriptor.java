/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.provider;

import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.core.runtime.IPath;

/**
 * Describes a PDOM format file in the local file system.
 * @since 4.0
 */
public interface IPDOMDescriptor {
	/**
	 * The absolute location in a local file system of the PDOM format file
	 * to contribute to the logical index.
	 * @return an absolute location of an existing file
	 */
	IPath getLocation();

	/**
	 * An index location converter suitable of translating the internal formatted
	 * path representations to index locations representing the current content location.
	 * @return an index location converter
	 */
	IIndexLocationConverter getIndexLocationConverter();
}
