/*******************************************************************************
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Eidsness - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.tag;

/**
 * Tags are used to annotate {@link ITagWriter}'s with extra information. They are created by
 * implementations of {@link IBindingTagger} which are contributed using
 * the org.eclipse.cdt.core.tagger extension point.
 *
 * @see IBindingTagger
 * @see ITagReader
 * @see ITagWriter
 * @since 5.5
 */
public interface IWritableTag extends ITag {
	/**
	 * Writes the given byte to the given offset in the tag. Returns {@code true} if successful.
	 */
	public boolean putByte(int offset, byte data);

	/**
	 * Writes the argument buffer into the receiver's payload starting at the specified offset.
	 * Writes the specified number of bytes or the full buffer when {@code len} is -1. Returns
	 * {@code true} if successful.
	 */
	public boolean putBytes(int offset, byte data[], int len);
}
