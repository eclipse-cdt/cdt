/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.ast.tag.SimpleTaggable;

/**
 * A marker interface for IBindings that can be tagged.  The binding can specify a particular
 * implementation by returning for from #getAdapter( ITaggable.class ).  Otherwise a simple,
 * non-cached, in-memory implementation is provided.
 *
 * @see ITag
 * @see ITaggable.Converter
 * @since 5.5
 */
public interface ITaggable
{
	/**
	 * Look for a tag for the receiver, returns null if there is no such tag.
	 *
	 * @param id A string that uniquely identifies the tag to be returned.  This value was provided by the contributor
	 *           when the tag was created (see {@link #createTag(String, int)}).
	 */
	public ITag getTag( String id );

	/**
	 * Create and return a new tag for the receiver.  E.g., if the ITaggable is associated with a persistent binding,
	 * then returned tag will read and write from the PDOM database.
	 *
	 * @param id A string that uniquely identifies the tag to be returned.  This value will be used by the contributor
	 *           when to find the tag (see {@link #getTag(String)}).
	 * @param len The number of bytes that should be allocated to store the tag's data.
	 */
	public IWritableTag createTag( String id, int len );

	/**
	 * Converts IBindings to ITaggables, which is provided to avoid changing the API of IBinding.
	 */
	public static class Converter
	{
		private Converter() { }

		/**
		 * First gives the IBinding instance a chance to convert itself, by calling IAdaptable#getAdapter( ITaggable.class )
		 * on the binding.  If the binding doesn't provide an implementation then a simple, in-memory, non-cached
		 * implementation is created and returned.
		 */
		public static final ITaggable from( IBinding binding )
		{
			if( binding == null )
				return null;

			// let the binding adapt to its own taggable
			ITaggable taggable = (ITaggable)binding.getAdapter( ITaggable.class );
			if( taggable != null )
				return taggable;

			// otherwise return an in-memory taggable
			return new SimpleTaggable( binding );
		}
	}
}
