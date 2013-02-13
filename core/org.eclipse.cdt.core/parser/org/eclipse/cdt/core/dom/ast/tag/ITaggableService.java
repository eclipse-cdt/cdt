/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Provides ITaggables for specific bindings.  The kind of the taggable will vary based on
 * the kind of the input binding.
 *
 * @see ITag
 * @see ITaggable
 * @since 5.5
 */
public interface ITaggableService
{
	/**
	 * Finds or creates a taggable for the specified binding.
	 */
	public ITaggable findTaggable( IBinding binding );
}
