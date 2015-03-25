/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 */
package org.eclipse.cdt.internal.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.tag.ITagReader;
import org.eclipse.cdt.core.dom.ast.tag.ITagService;

public class TagService implements ITagService {
	/**
	 * First gives the IBinding instance a chance to convert itself, by calling
	 * IAdaptable#getAdapter(ITagReader.class) on the binding. If the binding doesn't provide
	 * an implementation then a simple, in-memory, non-cached implementation is created and
	 * returned.
	 */
	@Override
	public ITagReader findTagReader(IBinding binding) {
		if (binding == null)
			return null;

		// Let the binding adapt to its own tag reader
		ITagReader tagReader = binding.getAdapter(ITagReader.class);
		if (tagReader != null)
			return tagReader;

		return new NonCachedTaggable(binding);
	}
}
