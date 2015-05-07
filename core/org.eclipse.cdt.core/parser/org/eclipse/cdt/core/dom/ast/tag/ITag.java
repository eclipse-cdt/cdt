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

/**
 * Tags are used to annotate {@link ITagReader}'s with extra information. They are created by
 * implementations of {@link IBindingTagger} which are contributed using
 * the org.eclipse.cdt.core.tagger extension point. The base tag interface is read-only, it is
 * extended by the writable {@link IWritableTag}.
 *
 * @see IBindingTagger
 * @see ITagReader
 * @see IWritableTag
 * @since 5.5
 */
public interface ITag {
	/** A constant that is returned to indicate a read failure. */
	public static final int FAIL = -1;

	/** Returns the number of bytes in the tag's data payload. */
	public int getDataLen();

	/** Returns the globally unique id of the tagger that created the receiver. */
	public String getTaggerId();

	/** Returns the byte from the specified offset or {@link #FAIL} on failure. */
	public int getByte(int offset);

	/**
	 * Returns the specified number of bytes from the specified offset. Specify {@code len} of -1
	 * to read all bytes from the specified offset to the end of the payload. Returns null if
	 * the given range is not valid. This would be expected if the version of the contributor has
	 * changed in a way that changes the structure of the data that it stores. Contributors must be
	 * able to deal with that case.
	 */
	public byte[] getBytes(int offset, int len);
}
