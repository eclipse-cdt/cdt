/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.ui.text.folding;

import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Contributors to the
 * <code>org.eclipse.cdt.ui.foldingStructureProvider</code> extension
 * point must specify an implementation of this interface which will create and
 * maintain {@link org.eclipse.jface.text.source.projection.ProjectionAnnotation} objects
 * that define folded regions in the the {@link org.eclipse.jface.text.source.projection.ProjectionViewer}.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 */
public interface ICFoldingStructureProvider {

	/**
	 * Installs this structure provider on the given editor and viewer.
	 * Implementations should listen to the projection events generated by
	 * <code>viewer</code> and enable / disable generation of projection
	 * structure accordingly.
	 *
	 * @param editor the editor that this provider works on
	 * @param viewer the projection viewer that displays the annotations created
	 *        by this structure provider
	 */
	public abstract void install(ITextEditor editor, ProjectionViewer viewer);

	/**
	 * Uninstalls this structure provider. Any references to editors or viewers
	 * should be cleared.
	 */
	public abstract void uninstall();

	/**
	 * (Re-)initializes the structure provided by the receiver.
	 */
	public abstract void initialize();

}
