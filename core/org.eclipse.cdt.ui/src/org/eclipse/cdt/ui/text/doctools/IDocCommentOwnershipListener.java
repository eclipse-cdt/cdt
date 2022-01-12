/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools;

import org.eclipse.core.resources.IResource;

/**
 * Implemented by clients interested in documentation comment ownership change events. These are generated
 * when the association between resource or workspace and documentation comment owner is set.
 * @since 5.0
 */
public interface IDocCommentOwnershipListener {
	/**
	 * Called when document comment ownership has changed at a particular resource level.
	 * @param resource the resource the ownership has changed for
	 * @param submappingsRemoved whether child resource mappings have been removed
	 * @param oldOwner the previous document comment owner
	 * @param newOwner the new document comment owner
	 */
	public void ownershipChanged(IResource resource, boolean submappingsRemoved, IDocCommentOwner oldOwner,
			IDocCommentOwner newOwner);

	/**
	 * Called when workspace-scope document comment owner changes.
	 * @param oldOwner the previous document comment owner
	 * @param newOwner the new document comment owner
	 */
	public void workspaceOwnershipChanged(IDocCommentOwner oldOwner, IDocCommentOwner newOwner);
}
