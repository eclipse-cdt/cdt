/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.compare;

import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * A merge viewer for C/C++ code.
 */
public class CMergeViewer extends AbstractMergeViewer {

	private static final String TITLE = "CMergeViewer.title"; //$NON-NLS-1$

	CSourceViewerConfiguration fSourceViewerConfiguration;

	public CMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
	}

	@Override
	protected SourceViewerConfiguration getSourceViewerConfiguration() {
		if (fSourceViewerConfiguration == null) {
			CTextTools tools = CUIPlugin.getDefault().getTextTools();
			IPreferenceStore store = getPreferenceStore();
			fSourceViewerConfiguration = new CSourceViewerConfiguration(tools.getColorManager(), store, null,
					ICPartitions.C_PARTITIONING);
		}
		return fSourceViewerConfiguration;
	}

	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		// use workspace default for highlighting doc comments in compare viewer
		IDocCommentOwner owner = DocCommentOwnerManager.getInstance().getWorkspaceCommentOwner();
		return CUIPlugin.getDefault().getTextTools().createDocumentPartitioner(owner);
	}

	@Override
	public String getTitle() {
		return CUIPlugin.getResourceString(TITLE);
	}

	@Override
	protected void handlePropertyChange(PropertyChangeEvent event) {
		super.handlePropertyChange(event);

		if (fSourceViewerConfiguration != null && fSourceViewerConfiguration.affectsTextPresentation(event)) {
			fSourceViewerConfiguration.handlePropertyChangeEvent(event);
			invalidateTextPresentation();
		}
	}
}
