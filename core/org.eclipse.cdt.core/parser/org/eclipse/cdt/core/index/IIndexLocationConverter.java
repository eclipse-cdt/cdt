/*******************************************************************************
 * Copyright (c) 2006, 2013 Symbian Software Ltd. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index;

/**
 * Each IIndexFragment stores file location representations in an implementation specific manner.
 * External to IIndexFragment files are identified by an {@link IIndexFileLocation}
 *
 * Internal to IIndexFragment a mechanism for converting between the string location format used
 * and the URI world is needed. This interface represents that mechanism.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIndexLocationConverter {
	/**
	 * Convert a raw string in an internal IIndexFragment implementation specific format to
	 * an IIndexFileLocation or null if the internal format could not be translated.
	 */
	public abstract IIndexFileLocation fromInternalFormat(String raw);

	/**
	 * Convert a IIndexFileLocation to the internal IIndexFragment implementation specific format
	 * or null if the location could not be translated.
	 * @param location
	 * @return an internal representation for the location specified
	 */
	public abstract String toInternalFormat(IIndexFileLocation location);
}
