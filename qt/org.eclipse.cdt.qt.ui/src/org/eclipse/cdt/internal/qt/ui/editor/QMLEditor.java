/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.editor;

import org.eclipse.cdt.internal.qt.ui.text.QMLSourceViewerConfiguration;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Basic editor for QML.  Thus far has only syntax highlighting capabilities.
 */
public class QMLEditor extends TextEditor {
	public static final String EDITOR_ID = "org.eclipse.cdt.qt.ui.QMLEditor"; //$NON-NLS-1$

	@Override
	protected void initializeEditor() {
		setSourceViewerConfiguration(new QMLSourceViewerConfiguration());
	}
}
