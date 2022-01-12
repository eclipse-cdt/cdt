/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Doug Schaefer
 *
 * Root class for Index View Actions. Add an check to make sure the
 * action is valid with the current context.
 */
public abstract class IndexAction extends Action {

	final protected IndexView indexView;
	final protected TreeViewer viewer;

	protected IndexAction(IndexView view, TreeViewer viewer) {
		super();
		this.indexView = view;
		this.viewer = viewer;
	}

	protected IndexAction(IndexView view, TreeViewer viewer, String text) {
		super(text);
		this.indexView = view;
		this.viewer = viewer;
	}

	protected IndexAction(IndexView view, TreeViewer viewer, String text, ImageDescriptor image) {
		super(text, image);
		this.indexView = view;
		this.viewer = viewer;
	}

	protected IndexAction(IndexView view, TreeViewer viewer, String text, int style) {
		super(text, style);
		this.indexView = view;
		this.viewer = viewer;
	}

	public abstract boolean valid();
}
