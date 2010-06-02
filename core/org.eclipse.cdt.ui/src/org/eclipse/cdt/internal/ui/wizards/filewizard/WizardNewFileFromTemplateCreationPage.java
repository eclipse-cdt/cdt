/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.wizards.filewizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.corext.template.c.FileTemplateContextType;

import org.eclipse.cdt.internal.ui.preferences.CodeTemplatePreferencePage;

/**
 * A new file creation wizard page with support for templates.
 *
 * @since 5.0
 */
public class WizardNewFileFromTemplateCreationPage extends WizardNewFileCreationPage {

	private Combo fTemplatesCombo;
	private Template[] fTemplates;
	protected boolean fUseTemplate= true;

	/**
	 * Create a new 'file from template' page.
	 * 
	 * @param pageName
	 * @param selection
	 */
	public WizardNewFileFromTemplateCreationPage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createAdvancedControls(Composite parent) {
		Composite groupComposite= new Composite(parent,SWT.NONE);
		groupComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		groupComposite.setLayout(layout);

		final Button useTemplateButton= new Button(groupComposite, SWT.CHECK);

		useTemplateButton.setText(NewFileWizardMessages.WizardNewFileFromTemplateCreationPage_useTemplate_label);
		useTemplateButton.setSelection(fUseTemplate);
		SelectionListener useTemplateListener= new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fUseTemplate= useTemplateButton.getSelection();
					fTemplatesCombo.setEnabled(fUseTemplate);
				}
			};
		useTemplateButton.addSelectionListener(useTemplateListener);

		fTemplatesCombo= new Combo(groupComposite, SWT.READ_ONLY);
		fTemplatesCombo.setEnabled(fUseTemplate);
		fTemplatesCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button configureButton= new Button(groupComposite, SWT.PUSH);
		configureButton.setText(NewFileWizardMessages.WizardNewFileFromTemplateCreationPage_configure_label);

		SelectionListener changeTemplateListener= new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					editTemplates();
				}
			};
		configureButton.addSelectionListener(changeTemplateListener);
		updateTemplates();

		super.createAdvancedControls(parent);
	}

	protected void editTemplates() {
		String prefPageId= CodeTemplatePreferencePage.PREF_ID;
		Map<String, String> data= null;
		String templateName= null;
		Template template= getSelectedTemplate();
		if (template != null) {
			templateName= template.getName();
		}
		if (templateName != null) {
			data= new HashMap<String, String>();
			data.put(CodeTemplatePreferencePage.DATA_SELECT_TEMPLATE, templateName);
		}
		PreferenceDialog dialog= PreferencesUtil.createPreferenceDialogOn(getShell(), prefPageId, new String[] { prefPageId }, data);
		if (dialog.open() == Window.OK) {
			updateTemplates();
		}
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	@Override
	protected InputStream getInitialContents() {
		Template template= getSelectedTemplate();
		if (fUseTemplate && template != null) {
			IFile fileHandle= createFileHandle(getContainerFullPath().append(getResourceName()));
			String lineDelimiter= StubUtility.getLineDelimiterPreference(getContainterProject());
			try {
				String content= StubUtility.getFileContent(template, fileHandle, lineDelimiter);
				if (content != null) {
					try {
						String charset= fileHandle.getParent().getDefaultCharset();
						return new ByteArrayInputStream(content.getBytes(charset));
					} catch (UnsupportedEncodingException exc) {
						return new ByteArrayInputStream(content.getBytes());
					}
				}
			} catch (CoreException exc) {
			}
		}
		return super.getInitialContents();
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		updateTemplates();
		super.handleEvent(event);
	}

	protected void updateTemplates() {
		Template selected= getSelectedTemplate();
		boolean isDefaultSelected= (selected != null && fTemplates.length == 1) || (fTemplatesCombo != null && fTemplatesCombo.getSelectionIndex() == 0);
		fTemplates= getApplicableTemplates();
		int idx= 0;
		String[] names= new String[fTemplates.length];
		for (int i = 0; i < names.length; i++) {
			names[i]= fTemplates[i].getName();
			if (!isDefaultSelected && selected != null && selected.getName().equals(names[i])) {
				idx= i;
			}
		}
		if (fTemplatesCombo != null) {
			if (names.length == 0) {
				names= new String[] { NewFileWizardMessages.WizardNewFileFromTemplateCreationPage_noTemplate_name };
			}
			fTemplatesCombo.setItems(names);
			fTemplatesCombo.select(idx);
		}
	}

	/**
	 * @return the selected template or <code>null</code> if none
	 */
	protected Template getSelectedTemplate() {
		if (fTemplates != null) {
			int index= 0;
			if (fTemplatesCombo != null) {
				index= fTemplatesCombo.getSelectionIndex();
				if (index < 0) {
					index= 0;
				}
			}
			if (index < fTemplates.length) {
				return fTemplates[index];
			}
		}
		return null;
	}

	private String getResourceName() {
		String fileName= getFileName();
		String fileExtension= getFileExtension();
		if (fileExtension != null && fileExtension.length() > 0 && !fileName.endsWith('.' + fileExtension)) {
			fileName += '.';
			fileName += fileExtension;
		}
		return fileName;
	}

	private IProject getContainterProject() {
		IPath containerPath= getContainerFullPath();
		if (containerPath != null) {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(containerPath.segment(0));
		}
		return null;
	}

	/**
     * Configure the set of selectable templates.
	 * @return the set of templates
	 */
	protected Template[] getApplicableTemplates() {
		IProject project= getContainterProject();
		String fileName= getResourceName();
		String[] contentTypes= getAllContentTypeIdsForFileName(project, fileName);
		return StubUtility.getFileTemplatesForContentTypes(contentTypes, project);
	}

	private static String[] getAllContentTypeIdsForFileName(IProject project, String fileName) {
		IContentTypeMatcher matcher;
		if (project == null || !project.isAccessible()) {
			IContentTypeManager contentTypeMgr= Platform.getContentTypeManager();
			matcher= contentTypeMgr;
		} else {
			try {
				matcher= project.getContentTypeMatcher();
			} catch (CoreException exc) {
				IContentTypeManager contentTypeMgr= Platform.getContentTypeManager();
				matcher= contentTypeMgr;
			}
		}
		IContentType[] contentTypes= matcher.findContentTypesFor(fileName);
		List<String> result= new ArrayList<String>(contentTypes.length * 2);
		for (int i = 0; i < contentTypes.length; i++) {
			IContentType contentType = contentTypes[i];
			String id= contentType.getId();
			result.add(id);
		}
		// add base types
		for (int i = 0; i < contentTypes.length; i++) {
			IContentType contentType = contentTypes[i].getBaseType();
			while (contentType != null) {
				String id= contentType.getId();
				if (result.contains(id)) {
					break;
				}
				result.add(id);
				contentType= contentType.getBaseType();
			}
		}
		if (result.isEmpty()) {
			result.add(FileTemplateContextType.CONTENTTYPE_TEXT);
		}
		return result.toArray(new String[result.size()]);
	}

}
