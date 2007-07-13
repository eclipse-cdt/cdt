/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.asm.AsmSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextTools;

/**
 * A merge viewer for assembly code.
 */
public class AsmMergeViewer extends AbstractMergeViewer {

	private static final String TITLE= "AsmMergeViewer.title"; //$NON-NLS-1$

	AsmSourceViewerConfiguration fSourceViewerConfiguration;

	/**
	 * Create a new assembly merge viewer.
	 * 
	 * @param parent
	 * @param style
	 * @param configuration
	 */
	public AsmMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
		super(parent, style, configuration);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.compare.AbstractMergeViewer#getSourceViewerConfiguration()
	 */
	protected SourceViewerConfiguration getSourceViewerConfiguration() {
		if (fSourceViewerConfiguration == null) {
			AsmTextTools tools= CUIPlugin.getDefault().getAsmTextTools();
			IPreferenceStore store = getPreferenceStore();
			fSourceViewerConfiguration = new AsmSourceViewerConfiguration(tools, store);
		}
		return fSourceViewerConfiguration;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.compare.AbstractMergeViewer#getTitle()
	 */
	public String getTitle() {
		return CUIPlugin.getResourceString(TITLE);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.compare.AbstractMergeViewer#handlePropertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected void handlePropertyChange(PropertyChangeEvent event) {
		super.handlePropertyChange(event);

		if (fSourceViewerConfiguration != null) {
			AsmTextTools tools= CUIPlugin.getDefault().getAsmTextTools();
			if (tools.affectsBehavior(event)) {
				invalidateTextPresentation();
			}
		}
	}
}
