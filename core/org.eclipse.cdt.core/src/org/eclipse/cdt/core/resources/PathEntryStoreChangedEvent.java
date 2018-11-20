/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.resources;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

/**
 * PathEntryChangedEvent
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PathEntryStoreChangedEvent extends EventObject {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4051048549254706997L;
	public static final int CONTENT_CHANGED = 1;
	public static final int STORE_CLOSED = 2;

	private final int flags;
	private final IProject project;

	/**
	 *
	 */
	public PathEntryStoreChangedEvent(IPathEntryStore store, IProject project, int flags) {
		super(store);
		this.project = project;
		this.flags = flags;
	}

	public IPathEntryStore getPathEntryStore() {
		return (IPathEntryStore) getSource();
	}

	public IProject getProject() {
		return project;
	}

	public boolean hasContentChanged() {
		return (flags & CONTENT_CHANGED) != 0;
	}

	public boolean hasClosed() {
		return (flags & STORE_CLOSED) != 0;
	}
}
