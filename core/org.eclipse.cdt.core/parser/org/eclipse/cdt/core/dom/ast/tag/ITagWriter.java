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
 * An interface that allows tag creation and modification.
 *
 * @see ITag
 * @see ITagService
 * @since 5.5
 */
public interface ITagWriter {
	/**
	 * Creates and returns a new tag for the receiver. E.g., if this writer is associated with
	 * a persistent binding, then returned tag will read and write from the PDOM database.
	 *
	 * @param id
	 *            A string that uniquely identifies the tag to be returned. This value will be used
	 *            by the contributor when to find the tag (see {@link ITagReader#getTag(String)}).
	 * @param len
	 *            The number of bytes that should be allocated to store the tag's data.
	 */
	public IWritableTag createTag(String id, int len);

	/**
	 * Sets the receiver's tags to only the ones provided. Deletes existing tags that are not in
	 * the argument list.
	 */
	public boolean setTags(Iterable<ITag> tags);
}
