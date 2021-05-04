/*******************************************************************************
 * Copyright (c) 2007, 2021 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Lidia Popescu (Wind River Systems) - http://bugs.eclipse.org/573204
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.wizards.filewizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.corext.template.c.FileTemplateContextType;
import org.eclipse.cdt.internal.ui.preferences.CodeTemplatePreferencePage;
import org.eclipse.cdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * A new file creation wizard page with support for templates.
 *
 * @since 5.0
 */
public class WizardNewFileFromTemplateCreationPage extends WizardNewFileCreationPage {

	private Combo fTemplatesCombo;
	private Template[] fTemplates;
	protected boolean fUseTemplate = true;
	/** Dialog settings key to persist selected template. */
	private static final String KEY_TEMPLATE = "org.eclipse.cdt.internal.corext.codemanipulation"; //$NON-NLS-1$

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
		Composite groupComposite = new Composite(parent, SWT.NONE);
		groupComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		groupComposite.setLayout(layout);

		final Button useTemplateButton = new Button(groupComposite, SWT.CHECK);

		useTemplateButton.setText(NewFileWizardMessages.WizardNewFileFromTemplateCreationPage_useTemplate_label);
		useTemplateButton.setSelection(fUseTemplate);
		SelectionListener useTemplateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fUseTemplate = useTemplateButton.getSelection();
				fTemplatesCombo.setEnabled(fUseTemplate);
			}
		};
		useTemplateButton.addSelectionListener(useTemplateListener);

		fTemplatesCombo = new Combo(groupComposite, SWT.READ_ONLY);
		fTemplatesCombo.setEnabled(fUseTemplate);
		fTemplatesCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button configureButton = new Button(groupComposite, SWT.PUSH);
		configureButton.setText(NewFileWizardMessages.WizardNewFileFromTemplateCreationPage_configure_label);

		SelectionListener changeTemplateListener = new SelectionAdapter() {
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
		String prefPageId = CodeTemplatePreferencePage.PREF_ID;
		Map<String, String> data = null;
		String templateName = null;
		Template template = getSelectedTemplate();
		if (template != null) {
			templateName = template.getName();
		}
		if (templateName != null) {
			data = new HashMap<>();
			data.put(CodeTemplatePreferencePage.DATA_SELECT_TEMPLATE, templateName);
		}
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), prefPageId,
				new String[] { prefPageId }, data);
		if (dialog.open() == Window.OK) {
			updateTemplates();
		}
	}

	/*
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	@Override
	protected InputStream getInitialContents() {
		Template template = getSelectedTemplate();
		if (fUseTemplate && template != null) {
			IFile fileHandle = createFileHandle(getContainerFullPath().append(getResourceName()));
			String lineDelimiter = StubUtility.getLineDelimiterPreference(getContainterProject());
			try {
				String content = StubUtility.getFileContent(template, fileHandle, lineDelimiter);
				if (content != null) {
					try {
						saveSelection(getContainterProject(), getFileExtensionFromName(), template);
						String charset = fileHandle.getParent().getDefaultCharset();
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
		Template selected = getSelectedTemplate();
		boolean isDefaultSelected = (selected != null && fTemplates.length == 1)
				|| (fTemplatesCombo != null && fTemplatesCombo.getSelectionIndex() == 0);
		fTemplates = getApplicableTemplates();
		int idx = 0;
		String[] names = new String[fTemplates.length];
		for (int i = 0; i < names.length; i++) {
			names[i] = fTemplates[i].getName();
			if (!isDefaultSelected && selected != null && selected.getName().equals(names[i])) {
				idx = i;
			}
		}
		Optional<Integer> idxo = getSelection(getContainterProject(), getFileExtensionFromName(), fTemplates);
		if (idxo.isPresent()) {
			idx = idxo.get();
		}
		if (fTemplatesCombo != null) {
			if (names.length == 0) {
				names = new String[] { NewFileWizardMessages.WizardNewFileFromTemplateCreationPage_noTemplate_name };
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
			int index = 0;
			if (fTemplatesCombo != null) {
				index = fTemplatesCombo.getSelectionIndex();
				if (index < 0) {
					index = 0;
				}
			}
			if (index < fTemplates.length) {
				return fTemplates[index];
			}
		}
		return null;
	}

	private String getResourceName() {
		String fileName = getFileName();
		String fileExtension = getFileExtension();
		if (fileExtension != null && fileExtension.length() > 0 && !fileName.endsWith('.' + fileExtension)) {
			fileName += '.';
			fileName += fileExtension;
		}
		return fileName;
	}

	private IProject getContainterProject() {
		IPath containerPath = getContainerFullPath();
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
		IProject project = getContainterProject();
		String fileName = getResourceName();
		String[] contentTypes = getAllContentTypeIdsForFileName(project, fileName);
		return StubUtility.getFileTemplatesForContentTypes(contentTypes, project);
	}

	private static String[] getAllContentTypeIdsForFileName(IProject project, String fileName) {
		IContentTypeMatcher matcher;
		if (project == null || !project.isAccessible()) {
			IContentTypeManager contentTypeMgr = Platform.getContentTypeManager();
			matcher = contentTypeMgr;
		} else {
			try {
				matcher = project.getContentTypeMatcher();
			} catch (CoreException exc) {
				IContentTypeManager contentTypeMgr = Platform.getContentTypeManager();
				matcher = contentTypeMgr;
			}
		}
		IContentType[] contentTypes = matcher.findContentTypesFor(fileName);
		List<String> result = new ArrayList<>(contentTypes.length * 2);
		for (int i = 0; i < contentTypes.length; i++) {
			IContentType contentType = contentTypes[i];
			String id = contentType.getId();
			result.add(id);
		}
		// add base types
		for (int i = 0; i < contentTypes.length; i++) {
			IContentType contentType = contentTypes[i].getBaseType();
			while (contentType != null) {
				String id = contentType.getId();
				if (result.contains(id)) {
					break;
				}
				result.add(id);
				contentType = contentType.getBaseType();
			}
		}
		if (result.isEmpty()) {
			result.add(FileTemplateContextType.CONTENTTYPE_TEXT);
		}
		return result.toArray(new String[result.size()]);
	}

	/**
	 * @return
	 */
	private String getFileExtensionFromName() {
		String fName = getFileName();
		if (fName == null || fName.isEmpty() || !fName.contains(".")) { //$NON-NLS-1$
			return null;
		}
		return fName.substring(fName.lastIndexOf("."), fName.length()); //$NON-NLS-1$
	}

	/**
	 * Returns the specific section from dialog based on provided key
	 *
	 * @param fileExtension
	 * @return
	 */
	protected static IDialogSettings getDialogSettings(String fileExtension) {
		if (fileExtension == null) {
			return null;
		}
		IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings().getSection(KEY_TEMPLATE);
		if (settings == null) {
			settings = CUIPlugin.getDefault().getDialogSettings().addNewSection(KEY_TEMPLATE);
		}
		IDialogSettings eSettings = settings.getSection(fileExtension);
		if (eSettings == null) {
			eSettings = settings.addNewSection(fileExtension);
		}
		return eSettings;
	}

	/**
	 * Saves the template that has been used for a specific file extension.
	 *
	 * @param project
	 * @param fExtension
	 * @param template
	 */
	private static void saveSelection(IProject project, String fExtension, Template template) {
		if (fExtension == null || fExtension.isEmpty() || template == null) {
			return;
		}
		IDialogSettings settings = getDialogSettings(fExtension);
		if (settings != null) {
			TemplatePersistenceData[] data = getTemplatePersistentData(project);
			String templateID = getTemplateId(template, data);
			settings.put("id", templateID == null ? "" : templateID); //$NON-NLS-1$ //$NON-NLS-2$
			settings.put("name", template.getName()); //$NON-NLS-1$
			settings.put("contextId", template.getContextTypeId()); //$NON-NLS-1$
		}
	}

	/**
	 * @param template
	 * @param data
	 * @return
	 */
	private static String getTemplateId(Template template, TemplatePersistenceData[] data) {
		if (template == null || data == null)
			return null;
		for (int i = 0; i < data.length; i++) {
			TemplatePersistenceData tData = data[i];
			if (tData.getTemplate().equals(template)) {
				return tData.getId();
			}
		}
		return null;
	}

	/**
	 * @param project
	 * @return
	 */
	private static TemplatePersistenceData[] getTemplatePersistentData(IProject project) {
		TemplatePersistenceData[] templateDatas;
		if (project == null) {
			templateDatas = CUIPlugin.getDefault().getCodeTemplateStore().getTemplateData(true);
		} else {
			ProjectTemplateStore projectStore = new ProjectTemplateStore(project.getProject());
			try {
				projectStore.load();
			} catch (IOException e) {
				CUIPlugin.log(e);
			}
			templateDatas = projectStore.getTemplateData();
		}
		return templateDatas;
	}

	/**
	 * @param project
	 * @param fExtension
	 * @param fTemplates
	 * @return the position of found template in the list for combobox selection inside Optional
	 */
	private static Optional<Integer> getSelection(IProject project, String fExtension, Template[] fTemplates) {
		if (fExtension == null || fTemplates == null) {
			return Optional.empty();
		}
		if (fExtension.isEmpty() || fTemplates.length == 0) {
			return Optional.empty();
		}
		IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings().getSection(KEY_TEMPLATE);
		if (settings == null) {
			return Optional.empty();
		}
		IDialogSettings eSettings = settings.getSection(fExtension);
		if (eSettings == null) {
			return Optional.empty();
		}
		String tId = eSettings.get("id"); //$NON-NLS-1$
		String tName = eSettings.get("name"); //$NON-NLS-1$
		String tContextId = eSettings.get("contextId"); //$NON-NLS-1$
		Template template = getTemplateFromId(project, tId);

		if (template != null) {
			for (int i = 0; i < fTemplates.length; i++) {
				if (fTemplates[i].equals(template)) {
					return Optional.of(i);
				}
			}
		}
		for (int i = 0; i < fTemplates.length; i++) {
			if (fTemplates[i].getContextTypeId().equals(tContextId) && fTemplates[i].getName().equals(tName)) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	/**
	 * Templates provided thought extension point may have id, but user manually created templates will not have ids.
	 *
	 * The method finds template object from provided template id stored in DialogSettings.
	 * If the plugin that provided the template has been uninstalled, it may not found the template.
	 *
	 * @param project
	 * @param tId
	 * @return
	 */
	private static Template getTemplateFromId(IProject project, String tId) {
		if (tId == null || tId.isEmpty()) {
			return null;
		}
		TemplatePersistenceData[] data = getTemplatePersistentData(project);
		if (data == null) {
			return null;
		}
		for (int i = 0; i < data.length; i++) {
			if (tId.equals(data[i].getId())) {
				return data[i].getTemplate();
			}
		}
		return null;
	}
}
