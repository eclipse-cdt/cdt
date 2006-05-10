/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.compare;

import org.eclipse.cdt.internal.ui.text.*;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class CMergeViewer extends TextMergeViewer {
	
	private static final String TITLE= "CMergeViewer.title"; //$NON-NLS-1$

	private IPropertyChangeListener fPreferenceChangeListener;
	private IPreferenceStore fPreferenceStore;
	private boolean fUseSystemColors;
	private CSourceViewerConfiguration fSourceViewerConfiguration;
		
	public CMergeViewer(Composite parent, int styles, CompareConfiguration mp) {
		super(parent, styles, mp);
		fPreferenceStore= CUIPlugin.getDefault().getCombinedPreferenceStore();
		if (fPreferenceStore != null) {
			 fPreferenceChangeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					handlePropertyChange(event);
				}
			};
			fPreferenceStore.addPropertyChangeListener(fPreferenceChangeListener);
		}
		
		fUseSystemColors= fPreferenceStore.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		if (! fUseSystemColors) {
			RGB bg= createColor(fPreferenceStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
			setBackgroundColor(bg);
			RGB fg= createColor(fPreferenceStore, ICColorConstants.C_DEFAULT);
			setForegroundColor(fg);
		}

	}

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
				RGB fg= createColor(fPreferenceStore, ICColorConstants.C_DEFAULT);
				setForegroundColor(fg);
			}
		} else if (key.equals(ICColorConstants.C_DEFAULT)) {

			if (!fUseSystemColors) {
				RGB fg= createColor(fPreferenceStore, ICColorConstants.C_DEFAULT);
				setForegroundColor(fg);
			}
		}
		
		if (getSourceViewerConfiguration().affectsBehavior(event)) {
			getSourceViewerConfiguration().adaptToPreferenceChange(event);
			invalidateTextPresentation();
		}
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private static RGB createColor(IPreferenceStore store, String key) {
		if (!store.contains(key))
			return null;
		if (store.isDefault(key))
			return PreferenceConverter.getDefaultColor(store, key);
		return PreferenceConverter.getColor(store, key);
	}

	private CSourceViewerConfiguration getSourceViewerConfiguration() {
		if (fSourceViewerConfiguration == null) {
			CTextTools tools= CUIPlugin.getDefault().getTextTools();
			fSourceViewerConfiguration = new CSourceViewerConfiguration(tools, null) {
				public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
					return IDocumentExtension3.DEFAULT_PARTITIONING;
				}
				
			};
		}
		return fSourceViewerConfiguration;
	}

	public String getTitle() {
		return CUIPlugin.getResourceString(TITLE);
	}
	
	protected IDocumentPartitioner getDocumentPartitioner() {
		return CUIPlugin.getDefault().getTextTools().createDocumentPartitioner();
	}

	protected void configureTextViewer(TextViewer textViewer) {
		if (textViewer instanceof SourceViewer) {
			((SourceViewer)textViewer).configure(getSourceViewerConfiguration());
		}
	}

}
