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
 * An interface that provides read-only access to the tags associated with a particular binding.
 *
 * @see ITag
 * @see ITagService
 * @since 5.5
 */
public interface ITagReader {
	/**
	 * Looks for a tag for the receiver, returns null if there is no such tag.
	 *
	 * @param id
	 *            A string that uniquely identifies the tag to be returned. This value was provided
	 *            by the contributor when the tag was created
	 *            (see {@link ITagWriter#createTag(String, int)}).
	 */
	public ITag getTag(String id);

	/**
	 * Returns all tags known to the receiver. Does not return null.
	 */
	public Iterable<ITag> getTags();
}
