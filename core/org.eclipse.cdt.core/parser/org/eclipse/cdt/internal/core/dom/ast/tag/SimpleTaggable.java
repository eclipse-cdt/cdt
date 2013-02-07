/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.internal.core.dom.ast.tag;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITaggable;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;

/**
 * A simple implementation that stores all data in memory.  No caching is provided, the tagger
 * is given an opportunity to process the binding each time {@link #getTag(String)} is evaluated.
 */
/* NOTE: This could be changed to cache tags for bindings, however my timing tests with the
 *       current implementation of IPDOMTagger suggest it is not worth it.  Lookup from the
 *       cache takes between 10 and 40 microseconds, and generating the tag takes between 20
 *       and 40 microseconds (on my PC).  More complex implementations of IPDOMTagger would
 *       take longer, and if that becomes common, then a cache could be implemented.  Use
 *       IndexCPPSignatureUtil.getSignature( IBinding ) to generate a key.
 */
public class SimpleTaggable implements ITaggable
{
	private final IBinding binding;

	public SimpleTaggable( IBinding binding )
	{
		this.binding = binding;
	}

	@Override
	public ITag getTag( String id )
	{
		return TagManager.INSTANCE.process( id, this, binding );
	}

	@Override
	public IWritableTag createTag( String id, final int len )
	{
		return new SimpleTag( len );
	}
}
