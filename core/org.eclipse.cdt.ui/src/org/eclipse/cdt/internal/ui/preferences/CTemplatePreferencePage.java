/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Wind River Systems, Inc. - Bug fixes
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;

/**
 * CTemplatePreferencePage
 */
public class CTemplatePreferencePage extends TemplatePreferencePage {

	public CTemplatePreferencePage() {
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setTemplateStore(CUIPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(CUIPlugin.getDefault().getTemplateContextRegistry());
	}
	
	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.TEMPLATE_PREFERENCE_PAGE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
	 */
	protected String getFormatterPreferenceKey() {
		return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		CUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}	
	
	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected SourceViewer createViewer(Composite parent) {
		IPreferenceStore store= CUIPlugin.getDefault().getCombinedPreferenceStore();
		CSourceViewer viewer= new CSourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		CSourceViewerConfiguration configuration = new CSourceViewerConfiguration(tools, null);
		IDocument document = new Document();
		tools.setupCDocument(document);
		viewer.configure(configuration);
		viewer.setEditable(false);
		viewer.setDocument(document);
	
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		
		Control control= viewer.getControl();
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
	
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {			
			public void getName(AccessibleEvent e) {
				e.result = PreferencesMessages.TemplatePreferencePage_Viewer_preview; 
		}});
		
		CSourcePreviewerUpdater.registerPreviewer(viewer, configuration, CUIPlugin.getDefault().getCombinedPreferenceStore());
		return viewer;
	}

}
