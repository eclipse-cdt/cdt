/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.core.dom.ast.tag;

/**
 * Tags are used to annotate {@link ITaggable}'s with extra information.  They are created
 * by implementations of {@link IBindingTagger} which are contributed using the
 * org.eclipse.cdt.core.tagger extension point.  The base tag interface is read-only, it
 * is extended by the writable {@link IWritableTag}.
 *
 * @see IBindingTagger
 * @see ITaggable
 * @see IWritableTag
 * @since 5.5
 */
public interface ITag
{
	/** A constant that is returned to indicate a read failure. */
	public static final int Fail = -1;

	/** Return the byte from the specified offset or {@link #Fail} on failure. */
	public int getByte( int offset );

	// TODO read methods for all the other types
}
