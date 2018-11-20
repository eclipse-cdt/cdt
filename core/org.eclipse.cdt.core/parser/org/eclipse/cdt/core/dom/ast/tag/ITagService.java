/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Provides ITagReaders for specific bindings. The kind of the reader will vary based on the kind
 * of the input binding.
 *
 * @see ITag
 * @see ITagReader
 * @since 5.5
 */
public interface ITagService {
	/**
	 * Finds or creates a tag reader for the specified binding or null if a reader cannot be
	 * associated with this binding.
	 *
	 * @param binding could be null
	 */
	public ITagReader findTagReader(IBinding binding);
}
