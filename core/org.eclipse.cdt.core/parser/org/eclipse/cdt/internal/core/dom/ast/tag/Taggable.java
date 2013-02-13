/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.dom.ast.tag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITaggable;
import org.eclipse.cdt.core.dom.ast.tag.ITaggableService;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;

/**
 * A simple implementation that stores all data in memory.  There is a one-to-one mapping between
 * non-persistent IBindings and implementations of Taggable.  The Taggable stores up to one Tag
 * for each Tagger's globally unique id.
 *
 * @see ITaggableService#findTaggable(org.eclipse.cdt.core.dom.ast.IBinding)
 */
public class Taggable implements ITaggable
{
	private final Map<String, Tag> tags = new HashMap<String, Tag>();

	@Override
	public ITag getTag( String id )
	{
		return tags.get( id );
	}

	@Override
	public synchronized IWritableTag createTag( String id, final int len )
	{
		Tag tag = tags.get( id );
		if( tag == null )
		{
			tag = new Tag( id, len );
			tags.put( id, tag );
		}

		return tag;
	}

	@Override
	public Iterable<ITag> getTags()
	{
		return new Iterable<ITag>()
		{
			@Override
			public Iterator<ITag> iterator()
			{
				final Iterator<Tag> i = tags.values().iterator();

				return new Iterator<ITag>()
				{
					Tag next = null;

					@Override public void remove() { i.remove(); }
					@Override public ITag next() { return next; }

					@Override
					public boolean hasNext()
					{
						while( i.hasNext() )
						{
							next = i.next();
							if( next != null )
								return true;
						}

						return false;
					}
				};
			}
		};
	}
}
