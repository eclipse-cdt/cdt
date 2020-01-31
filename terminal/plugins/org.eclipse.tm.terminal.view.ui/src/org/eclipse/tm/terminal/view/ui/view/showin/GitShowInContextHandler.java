/*******************************************************************************
 * Copyright (c) 2015, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.view.showin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Git show in context handler implementation.
 */
@SuppressWarnings("restriction")
public class GitShowInContextHandler {

	/**
	 * Converts the data from the input object into a selection.
	 *
	 * @param input The input element. Must not be <code>null</code>.
	 * @return The selection or <code>null</code>.
	 */
	public static ISelection getSelection(Object input) {
		Assert.isNotNull(input);

		List<Object> elements = new ArrayList<Object>();

		if (input instanceof org.eclipse.egit.ui.internal.history.HistoryPageInput) {
			org.eclipse.egit.ui.internal.history.HistoryPageInput inp = (org.eclipse.egit.ui.internal.history.HistoryPageInput) input;

			if (inp.isSingleFile()) {
				elements.add(inp.getSingleFile());
			} else {
				File[] fl = inp.getFileList();
				if (fl != null && fl.length > 0) {
					for (File f : fl) {
						if (f.canRead() && !elements.contains(f)) {
							elements.add(f);
						}
					}
				}

				IResource[] rl = inp.getItems();
				if (rl != null && rl.length > 0) {
					for (IResource r : rl) {
						if (r.isAccessible() && !elements.contains(r)) {
							elements.add(r);
						}
					}
				}
			}
		}

		return elements.isEmpty() ? null : new StructuredSelection(elements);
	}

	/**
	 * Returns the path of the given element.
	 *
	 * @param element The element. Must not be <code>null</code>.
	 * @return The path or <code>null</code>.
	 */
	public static IPath getPath(Object element) {
		Assert.isNotNull(element);

		IPath path = null;

		if (element instanceof org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode) {
			path = ((org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode<?>) element).getPath();
		}

		return path;
	}
}
