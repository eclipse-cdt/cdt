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
 * org.eclipse.cdt.core.tagger extension point.
 *
 * @see IBindingTagger
 * @see ITaggable
 * @since 5.5
 */
public interface IWritableTag extends ITag
{
	/** Write the given byte to the given offset in the tag.  Return true if successful and false otherwise. */
	public boolean putByte( int offset, byte data );

	/** Write the argument buffer into the receiver's payload starting at the specified offset.  Write the specified
	 *  number of bytes or the full buffer when len is -1.  Return true if successful and false otherwise. */
	public boolean putBytes( int offset, byte data[], int len );

	// TODO write for all types
}
