/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.compare;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.editor.MakefileDocumentSetupParticipant;
import org.eclipse.cdt.make.internal.ui.editor.MakefileSourceConfiguration;
import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefilePartitionScanner;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * A merge viewer for Makefiles.
 */
public class MakefileMergeViewer extends TextMergeViewer {

	private MakefileSourceConfiguration fSourceViewerConfiguration;
	private IPropertyChangeListener fPreferenceChangeListener;
	private IPreferenceStore fPreferenceStore;

	protected boolean fUseSystemColors;

	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	protected static RGB createColor(IPreferenceStore store, String key) {
		if (!store.contains(key))
			return null;
		if (store.isDefault(key))
			return PreferenceConverter.getDefaultColor(store, key);
		return PreferenceConverter.getColor(store, key);
	}

	/**
	 * Create a new merge viewer.
	 */
	public MakefileMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
		super(parent, style | SWT.LEFT_TO_RIGHT, configuration);

		IPreferenceStore store = getPreferenceStore();

		fUseSystemColors= store.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		if (!fUseSystemColors) {
			RGB bg= createColor(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			setBackgroundColor(bg);
			RGB fg= createColor(store, ColorManager.MAKE_DEFAULT_COLOR);
			setForegroundColor(fg);
		}

	}

	protected IPreferenceStore getPreferenceStore() {
		if (fPreferenceStore == null) {
			fPreferenceStore= MakeUIPlugin.getDefault().getCombinedPreferenceStore();
			fPreferenceChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePropertyChange(event);
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPreferenceChangeListener);
		}
		return fPreferenceStore;
	}

	@Override
	protected void handleDispose(DisposeEvent event) {
		if (fPreferenceChangeListener != null) {
			fPreferenceStore.removePropertyChangeListener(fPreferenceChangeListener);
			fPreferenceChangeListener= null;
		}
		super.handleDispose(event);
	}

	protected void handlePropertyChange(PropertyChangeEvent event) {
		
		String key= event.getProperty();
		
		if (key.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND)) {
			if (!fUseSystemColors) {
				RGB bg= createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
				setBackgroundColor(bg);
			}
						
		} else if (key.equals(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
			fUseSystemColors= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
			if (fUseSystemColors) {
				setBackgroundColor(null);
				setForegroundColor(null);
			} else {
				RGB bg= createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
				setBackgroundColor(bg);
				RGB fg= createColor(fPreferenceStore, ColorManager.MAKE_DEFAULT_COLOR);
				setForegroundColor(fg);
			}
		} else if (key.equals(ColorManager.MAKE_DEFAULT_COLOR)) {
			if (!fUseSystemColors) {
				RGB fg= createColor(fPreferenceStore, ColorManager.MAKE_DEFAULT_COLOR);
				setForegroundColor(fg);
			}
		}

		if (fSourceViewerConfiguration != null && fSourceViewerConfiguration.affectsBehavior(event)) {
			fSourceViewerConfiguration.adaptToPreferenceChange(event);
			invalidateTextPresentation();
		}

	}

	@Override
	protected String getDocumentPartitioning() {
		return MakefileDocumentSetupParticipant.MAKEFILE_PARTITIONING;
	}

	@Override
	protected IDocumentPartitioner getDocumentPartitioner() {
		return new FastPartitioner(new MakefilePartitionScanner(), MakefilePartitionScanner.MAKE_PARTITIONS);
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
		}
	}

	/*
	 * @see org.eclipse.compare.contentmergeviewer.ContentMergeViewer#getTitle()
	 */
	@Override
	public String getTitle() {
		return MakeUIPlugin.getResourceString("MakefileMergeViewer.title"); //$NON-NLS-1$
	}

	/**
	 * @return a source configuration for the viewer
	 */
	protected SourceViewerConfiguration getSourceViewerConfiguration() {
		if (fSourceViewerConfiguration == null) {
			fSourceViewerConfiguration= new MakefileSourceConfiguration(getPreferenceStore(), null);
		}
		return fSourceViewerConfiguration;
	}

}
