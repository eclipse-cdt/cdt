/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Wind River Systems, Inc. - Bug fixes
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * Template preference page for C/C++ editor templates.
 */
public class CTemplatePreferencePage extends TemplatePreferencePage {

	/**
	 * A dialog to edit a template.
	 */
	protected class CEditTemplateDialog extends EditTemplateDialog {

		public CEditTemplateDialog(Shell shell, Template template, boolean edit, boolean isNameModifiable,
				ContextTypeRegistry contextTypeRegistry) {
			super(shell, template, edit, isNameModifiable, contextTypeRegistry);
		}

		/*
		 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage.EditTemplateDialog#createViewer(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected SourceViewer createViewer(Composite parent) {
			IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
			CSourceViewer viewer = new CSourceViewer(parent, null, null, false,
					SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
			CTextTools tools = CUIPlugin.getDefault().getTextTools();
			CSourceViewerConfiguration configuration = new CSourceViewerConfiguration(tools.getColorManager(), store,
					null, tools.getDocumentPartitioning()) {
				@Override
				public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
					ContentAssistant assistant = new ContentAssistant();
					assistant.enableAutoActivation(true);
					assistant.enableAutoInsert(true);
					assistant.setContentAssistProcessor(getTemplateProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
					assistant.setContentAssistProcessor(getTemplateProcessor(), ICPartitions.C_MULTI_LINE_COMMENT);
					assistant.setContentAssistProcessor(getTemplateProcessor(), ICPartitions.C_SINGLE_LINE_COMMENT);
					assistant.setContentAssistProcessor(getTemplateProcessor(), ICPartitions.C_PREPROCESSOR);
					return assistant;
				}
			};
			IDocument document = new Document();
			tools.setupCDocument(document);
			viewer.configure(configuration);
			viewer.setEditable(true);
			viewer.setDocument(document);

			Font font = JFaceResources.getFontRegistry().get(PreferenceConstants.EDITOR_TEXT_FONT);
			viewer.getTextWidget().setFont(font);

			CSourcePreviewerUpdater.registerPreviewer(viewer, configuration,
					CUIPlugin.getDefault().getCombinedPreferenceStore());
			return viewer;
		}
	}

	public CTemplatePreferencePage() {
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setTemplateStore(CUIPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(CUIPlugin.getDefault().getTemplateContextRegistry());
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.TEMPLATE_PREFERENCE_PAGE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
	 */
	@Override
	protected String getFormatterPreferenceKey() {
		return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createTemplateEditDialog2(org.eclipse.jface.text.templates.Template, boolean, boolean)
	 */
	@Override
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		CEditTemplateDialog dialog = new CEditTemplateDialog(getShell(), template, edit, isNameModifiable,
				getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected SourceViewer createViewer(Composite parent) {
		IPreferenceStore store = CUIPlugin.getDefault().getCombinedPreferenceStore();
		CSourceViewer viewer = new CSourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL,
				store);
		CTextTools tools = CUIPlugin.getDefault().getTextTools();
		CSourceViewerConfiguration configuration = new CSourceViewerConfiguration(tools.getColorManager(), store, null,
				tools.getDocumentPartitioning());
		IDocument document = new Document();
		tools.setupCDocument(document);
		viewer.configure(configuration);
		viewer.setEditable(false);
		viewer.setDocument(document);

		Font font = JFaceResources.getFontRegistry().get(PreferenceConstants.EDITOR_TEXT_FONT);
		viewer.getTextWidget().setFont(font);

		Control control = viewer.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(5);
		control.setLayoutData(data);

		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = PreferencesMessages.TemplatePreferencePage_Viewer_preview;
			}
		});

		CSourcePreviewerUpdater.registerPreviewer(viewer, configuration,
				CUIPlugin.getDefault().getCombinedPreferenceStore());
		return viewer;
	}

}
