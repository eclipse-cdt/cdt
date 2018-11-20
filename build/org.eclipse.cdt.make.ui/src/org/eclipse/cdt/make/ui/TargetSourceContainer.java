/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.ui;

import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * A class to represent source folders added to Make Targets View on top.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @since 7.1
 */
public class TargetSourceContainer {
	private IContainer container;

	/**
	 * Constructor.
	 *
	 * @param srcEntry - source entry backing the container.
	 */
	public TargetSourceContainer(ICSourceEntry srcEntry) {
		IWorkspaceRoot wspRoot = ResourcesPlugin.getWorkspace().getRoot();
		container = wspRoot.getFolder(srcEntry.getFullPath());
	}

	/**
	 * Returns resource container associated with the source entry.
	 *
	 * @return resource container.
	 */
	public IContainer getContainer() {
		return container;
	}

	@Override
	public int hashCode() {
		return container.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TargetSourceContainer)
			return container.equals(((TargetSourceContainer) obj).container);
		return false;
	}

}
