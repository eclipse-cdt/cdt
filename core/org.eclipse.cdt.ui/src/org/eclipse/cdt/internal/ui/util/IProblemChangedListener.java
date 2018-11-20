/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.core.resources.IResource;

/**
 * Can be added to a ProblemMarkerManager to get notified about error
 * marker changes. Used to update error ticks.
 */
public interface IProblemChangedListener {

	/**
	 * @param changedElements  A set of type <code>IPath</code> that
	 * describe the resources that had an error marker change.
	 */
	void problemsChanged(IResource[] changedResources, boolean markerChanged);
}
