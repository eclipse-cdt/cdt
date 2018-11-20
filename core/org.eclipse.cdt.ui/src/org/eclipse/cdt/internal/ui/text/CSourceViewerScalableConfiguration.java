/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Configuration for an <code>SourceViewer</code> which shows C/C++ code.
 * It turns off some editor features when scalability mode options are enabled.
 */
public class CSourceViewerScalableConfiguration extends CSourceViewerConfiguration {

	public CSourceViewerScalableConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (((CEditor) getEditor()).isEnableScalablilityMode()
				&& fPreferenceStore.getBoolean(PreferenceConstants.SCALABILITY_RECONCILER))
			return null;
		return super.getReconciler(sourceViewer);
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		if (((CEditor) getEditor()).isEnableScalablilityMode()
				&& fPreferenceStore.getBoolean(PreferenceConstants.SCALABILITY_SYNTAX_COLOR))
			return null;
		return super.getPresentationReconciler(sourceViewer);
	}
}
