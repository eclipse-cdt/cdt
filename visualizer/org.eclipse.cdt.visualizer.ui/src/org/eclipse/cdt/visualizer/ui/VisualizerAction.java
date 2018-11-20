/*******************************************************************************
 * Copyright (c) 2012, 2013 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;

// ---------------------------------------------------------------------------
// VisualizerAction
// ---------------------------------------------------------------------------

/** Base class for visualizer actions.
 *  (Viewers are not required to use this class. This is simply a
 *  convenience wrapper for the standard Action class.)
 */
public class VisualizerAction extends Action {
	// --- members ---

	// --- constructors/destructors ---

	/** Constructor. */
	protected VisualizerAction() {
		// NOTE: this constructor is only intended for deriving classes
		// that need to construct the text/description/image attributes
		// programmatically.
	}

	/** Constructor. */
	public VisualizerAction(String text, int style) {
		super(text, style);
	}

	/** Constructor. */
	public VisualizerAction(String text, String description) {
		super(text);
		setDescription(description);
	}

	/** Constructor. */
	public VisualizerAction(String text, String description, ImageDescriptor image) {
		super(text, image);
		setDescription(description);
	}

	/** Constructor. */
	public VisualizerAction(String text, String description, ImageDescriptor enabledImage,
			ImageDescriptor disabledImage) {
		super(text, enabledImage);
		setDescription(description);
		setDisabledImageDescriptor(disabledImage);
	}

	/** Dispose method. */
	public void dispose() {
	}

	// --- methods ---

	/** Invoked when action is triggered. */
	@Override
	public void run() {
	}

	/** Invoked when action is triggered,
	 *  with the event that caused it.
	 */
	@Override
	public void runWithEvent(Event event) {
		run();
	}
}
