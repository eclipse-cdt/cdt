/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.AsmSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;

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

	@Override
	protected SourceViewerConfiguration getSourceViewerConfiguration() {
		if (fSourceViewerConfiguration == null) {
			IPreferenceStore store= getPreferenceStore();
			final IColorManager colorManager = CDTUITools.getColorManager();
			fSourceViewerConfiguration= new AsmSourceViewerConfiguration(colorManager, store, null, ICPartitions.C_PARTITIONING);
		}
		return fSourceViewerConfiguration;
	}

	@Override
	public String getTitle() {
		return CUIPlugin.getResourceString(TITLE);
	}

	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		return CDTUITools.createAsmDocumentPartitioner();
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
