/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.core.dom.ast.tag;


/**
 * A marker interface for IBindings that can be tagged.  The binding can specify a particular
 * implementation by returning for from #getAdapter( ITaggable.class ).  Otherwise a simple,
 * non-cached, in-memory implementation is provided.
 *
 * @see ITag
 * @see ITaggableService
 * @since 5.5
 */
public interface ITaggable
{
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
	 * Look for a tag for the receiver, returns null if there is no such tag.
	 *
	 * @param id A string that uniquely identifies the tag to be returned.  This value was provided by the contributor
	 *           when the tag was created (see {@link #createTag(String, int)}).
	 */
	public ITag getTag( String id );

	/**
	 * Return all tags known to the receiver.  Does not return null.
	 */
	public Iterable<ITag> getTags();
}
