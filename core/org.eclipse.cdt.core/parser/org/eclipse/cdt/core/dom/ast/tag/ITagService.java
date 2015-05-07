/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
