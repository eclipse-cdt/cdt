/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
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
		SourceViewer viewer= new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		CTextTools tools= CUIPlugin.getDefault().getTextTools();
		viewer.configure(new CSourceViewerConfiguration(tools, null));
		viewer.setEditable(false);
		viewer.setDocument(new Document());
		viewer.getTextWidget().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		
		Control control= viewer.getControl();
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
	
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {			
			public void getName(AccessibleEvent e) {
				e.result = PreferencesMessages.getString("TemplatePreferencePage.preview"); //$NON-NLS-1$
		}});
		
		return viewer;
	}

}
