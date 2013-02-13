/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.tag.ITaggable;
import org.eclipse.cdt.core.dom.ast.tag.ITaggableService;

public class TaggableService implements ITaggableService
{
	/**
	 * First gives the IBinding instance a chance to convert itself, by calling IAdaptable#getAdapter( ITaggable.class )
	 * on the binding.  If the binding doesn't provide an implementation then a simple, in-memory, non-cached
	 * implementation is created and returned.
	 */
	@Override
	public ITaggable findTaggable( IBinding binding )
	{
		if( binding == null )
			return null;

		// let the binding adapt to its own taggable
		ITaggable taggable = (ITaggable)binding.getAdapter( ITaggable.class );
		if( taggable != null )
			return taggable;

		return new NonCachedTaggable( binding );
	}
}
