/**********************************************************************
 * Copyright (c) 2004, 2005 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     TimeSys Corporation - Initial implementation
 *     Markus Schorn (Wind River Systems)
 **********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;


import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;

import org.eclipse.cdt.internal.core.model.CModelManager;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

/*
 * The preference page used for displaying/editing CDT file
 * type associations for a project
 */
public class CFileTypesPropertyPage extends PropertyPage {

	class FixCFileTypesPreferenceBlock extends CFileTypesPreferenceBlock {
		ArrayList list = new ArrayList();

		public FixCFileTypesPreferenceBlock() {
			super();
		}

		public FixCFileTypesPreferenceBlock(IProject input) {
			super(input);
		}

		private IContentTypeManager.ContentTypeChangeEvent newContentTypeChangeEvent(IContentType source, IScopeContext context) {
			return new IContentTypeManager.ContentTypeChangeEvent(source, context);
		}

		public boolean performOk() {
			boolean changed = super.performOk();
			if (changed) {
				int size = list.size();
				if (size > 0) {
					IContentTypeManager.ContentTypeChangeEvent[] events = new IContentTypeManager.ContentTypeChangeEvent[size];
					list.toArray(events);
					CModelManager.getDefault().contentTypeChanged(events);
				}
			}
			return changed;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.preferences.CFileTypesPreferenceBlock#addAssociation(org.eclipse.core.runtime.preferences.IScopeContext, org.eclipse.core.runtime.content.IContentType, java.lang.String, int)
		 */
		protected void addAssociation(IScopeContext context, IContentType contentType, String spec, int type) {
			super.addAssociation(context, contentType, spec, type);
			// accumulate the events
			list.add(newContentTypeChangeEvent(contentType, context));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.ui.preferences.CFileTypesPreferenceBlock#removeAssociation(org.eclipse.core.runtime.preferences.IScopeContext, org.eclipse.core.runtime.content.IContentType, java.lang.String, int)
		 */
		protected void removeAssociation(IScopeContext context, IContentType contentType, String spec, int type) {
			super.removeAssociation(context, contentType, spec, type);
			// accumulate the events
			list.add(newContentTypeChangeEvent(contentType, context));
		}
		
	}

	protected Button fUseWorkspace;
	protected Button fUseProject;
	protected FixCFileTypesPreferenceBlock fPrefsBlock;
	
	public CFileTypesPropertyPage(){
		super();
		noDefaultAndApplyButton();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite topPane = new Composite(parent, SWT.NONE);

		topPane.setLayout(new GridLayout());
		topPane.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Workspace radio buttons
		
		Composite radioPane = new Composite(topPane, SWT.NONE);

		radioPane.setLayout(new GridLayout());
		radioPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fUseWorkspace = new Button(radioPane, SWT.RADIO);
		fUseWorkspace.setText(PreferencesMessages.getString("CFileTypesPropertyPage.useWorkspaceSettings")); //$NON-NLS-1$
		fUseWorkspace.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (fUseWorkspace.getSelection()) {
					fPrefsBlock.setInput(null);
					fPrefsBlock.setEnabled(false);
				}
			}
		});

		final IProject project = getProject(); 
		boolean custom = CCorePlugin.usesProjectSpecificContentTypes(project);

		fUseProject = new Button(radioPane, SWT.RADIO);
		fUseProject.setText(PreferencesMessages.getString("CFileTypesPropertyPage.useProjectSettings")); //$NON-NLS-1$
		fUseProject.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (fUseProject.getSelection()) {
					fPrefsBlock.setInput(project);
					fPrefsBlock.setEnabled(true);
				}
			}
		});
		
		Composite blockPane = new Composite(topPane, SWT.NONE);

		blockPane.setLayout(new GridLayout());
		blockPane.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (custom) {
			fPrefsBlock = new FixCFileTypesPreferenceBlock(project);
		} else {
			fPrefsBlock = new FixCFileTypesPreferenceBlock();
		}

		fPrefsBlock.createControl(blockPane);
	
		fUseWorkspace.setSelection(!custom);
		fUseProject.setSelection(custom);
		fPrefsBlock.setEnabled(custom);
	
		PlatformUI.getWorkbench().getHelpSystem().setHelp( topPane, ICHelpContextIds.FILE_TYPES_STD_PAGE );
		return topPane;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fUseWorkspace.setSelection(true);
		fUseProject.setSelection(false);
		fPrefsBlock.setInput(null);
		fPrefsBlock.setEnabled(false);
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean useProjectContentTypes= fUseProject.getSelection();
		IProject project = getProject();
		if (CCorePlugin.usesProjectSpecificContentTypes(project) != useProjectContentTypes) {
			CCorePlugin.setUseProjectSpecificContentTypes(project, useProjectContentTypes);
			computeEvents(project);
		}
		if (useProjectContentTypes) {
			fPrefsBlock.performOk();
		}
		return super.performOk();
	}
	
	private IProject getProject(){
		Object		element	= getElement();
		IProject 	project	= null;
		
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project= (IProject) ((IAdaptable)element).getAdapter(IProject.class);
		}
		return project;
	}

	public IContentType[] getRegistedContentTypes() {
		String [] ids = CoreModel.getRegistedContentTypeIds();
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType [] ctypes = new IContentType[ids.length];
		for (int i = 0; i < ids.length; i++) {
			ctypes[i] = manager.getContentType(ids[i]);
		}
		return ctypes;
	}

	/**
	 * We are looking at the association from the project, if we have any
	 * we should generate an event.
	 * @param project
	 */
	void computeEvents(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IContentType[] ctypes = getRegistedContentTypes();
		ArrayList list = new ArrayList(ctypes.length);
		for (int i = 0; i < ctypes.length; i++) {
			IContentType ctype = ctypes[i];
			try {
				IContentTypeSettings projectSettings = ctype.getSettings(projectScope);
				String[] projectSpecs = projectSettings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				if (projectSpecs!= null && projectSpecs.length > 0) {
					list.add(ctype);
				} else {
					projectSpecs = projectSettings.getFileSpecs(IContentType.FILE_NAME_SPEC);
					if (projectSpecs != null && projectSpecs.length > 0) {
						list.add(ctype);
					}
				}
			} catch (CoreException e) {
				// ignore ?
			}
		}

		// fire the events
		if (list.size() > 0) {
			IContentTypeManager.ContentTypeChangeEvent[] events =  new IContentTypeManager.ContentTypeChangeEvent[list.size()];
			for (int i = 0; i < list.size(); ++i) {
				IContentType source = (IContentType)list.get(i);
				events[i] =  new IContentTypeManager.ContentTypeChangeEvent(source, projectScope);
			}
			CModelManager.getDefault().contentTypeChanged(events);
		}
	}

	boolean isSpecsChanged(String[] newSpecs, String[] oldSpecs) {
		if (newSpecs.length != oldSpecs.length) {
			return true;
		}
		for (int i = 0; i < newSpecs.length; ++i) {
			String newSpec = newSpecs[i];
			boolean found = false;
			for (int j = 0; j < oldSpecs.length; ++j) {
				String oldSpec = oldSpecs[j];
				if (newSpec.equalsIgnoreCase(oldSpec)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return true;
			}
		}
		return false;
	}

}
