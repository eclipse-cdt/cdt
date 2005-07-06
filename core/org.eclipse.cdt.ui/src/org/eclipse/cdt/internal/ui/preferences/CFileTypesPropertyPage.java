/**********************************************************************
 * Copyright (c) 2004, 2005 TimeSys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     TimeSys Corporation - Initial implementation
 **********************************************************************/
package org.eclipse.cdt.internal.ui.preferences;


import java.util.ArrayList;

import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
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
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/*
 * The preference page used for displaying/editing CDT file
 * type associations for a project
 */
public class CFileTypesPropertyPage extends PropertyPage {

	private static final String CONTENT_TYPE_PREF_NODE = "content-types"; //$NON-NLS-1$
	private static final String FULLPATH_CONTENT_TYPE_PREF_NODE = Platform.PI_RUNTIME + IPath.SEPARATOR + CONTENT_TYPE_PREF_NODE;
	private static final String PREF_LOCAL_CONTENT_TYPE_SETTINGS = "enabled"; //$NON-NLS-1$
	private final static String PREF_FILE_EXTENSIONS = "file-extensions"; //$NON-NLS-1$
	private final static String PREF_FILE_NAMES = "file-names"; //$NON-NLS-1$
	private final static String PREF_SEPARATOR = ","; //$NON-NLS-1$
	private static final Preferences PROJECT_SCOPE = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
	//private static final InstanceScope INSTANCE_SCOPE = new InstanceScope();


	protected Button fUseWorkspace;
	protected Button fUseProject;
	protected CFileTypesPreferenceBlock fPrefsBlock;
	
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
		boolean custom = isProjectSpecificContentType(project.getName());

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
			fPrefsBlock = new CFileTypesPreferenceBlock(project);
		} else {
			fPrefsBlock = new CFileTypesPreferenceBlock();
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
		
		if (fUseProject.getSelection()) {
			IProject project = getProject();
			ProjectScope projectScope = new ProjectScope(project);
			Preferences contentTypePrefs = projectScope.getNode(FULLPATH_CONTENT_TYPE_PREF_NODE);
			if (! isProjectSpecificContentType(project.getName())) {
				// enable project-specific settings for this project
				contentTypePrefs.putBoolean(PREF_LOCAL_CONTENT_TYPE_SETTINGS, true);
			}
			fPrefsBlock.performOk();
			computeEvents(project);
			try {
				contentTypePrefs.flush();
			} catch (BackingStoreException e) {
				// ignore ??
			}
		} else if (fUseWorkspace.getSelection()) {
			IProject project = getProject();
			if (isProjectSpecificContentType(project.getName())) {
				ProjectScope projectScope = new ProjectScope(project);
				Preferences contentTypePrefs = projectScope.getNode(FULLPATH_CONTENT_TYPE_PREF_NODE);
				// enable project-specific settings for this project
				contentTypePrefs.putBoolean(PREF_LOCAL_CONTENT_TYPE_SETTINGS, false);
				try {
					contentTypePrefs.flush();
				} catch (BackingStoreException e) {
					// ignore ??
				}
				computeEvents(project);
			}
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

	protected static boolean isProjectSpecificContentType(String projectName) {
		try {
			// be careful looking up for our node so not to create any nodes as side effect
			Preferences node = PROJECT_SCOPE;
			//TODO once bug 90500 is fixed, should be simpler
			// for now, take the long way
			if (!node.nodeExists(projectName))
				return false;
			node = node.node(projectName);
			if (!node.nodeExists(Platform.PI_RUNTIME))
				return false;
			node = node.node(Platform.PI_RUNTIME);
			if (!node.nodeExists(CONTENT_TYPE_PREF_NODE))
				return false;
			node = node.node(CONTENT_TYPE_PREF_NODE);
			return node.getBoolean(PREF_LOCAL_CONTENT_TYPE_SETTINGS, false);
		} catch (BackingStoreException e) {
			// exception treated when retrieving the project preferences
		}
		return false;
	}

	void computeEvents(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);

		// Calculate the events to tell the clients of changes
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType[] ctypes = manager.getAllContentTypes();
		ArrayList list = new ArrayList(ctypes.length);
		for (int i = 0; i < ctypes.length; i++) {
			IContentType ctype = ctypes[i];
			try {
				IContentTypeSettings projectSettings = ctype.getSettings(projectScope);
				String[] globalSpecs = ctypes[i].getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				String[] projectSpecs = projectSettings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				if (isSpecsChanged(globalSpecs, projectSpecs)) {
					list.add(ctype);
				} else {
					globalSpecs = ctypes[i].getFileSpecs(IContentType.FILE_NAME_SPEC);
					projectSpecs = projectSettings.getFileSpecs(IContentType.FILE_NAME_SPEC);
					if (isSpecsChanged(globalSpecs, projectSpecs)) {
						list.add(ctype);
					}
				}
			} catch (CoreException e) {
				// ignore ?
			}
		}
		// fire the events
		for (int i = 0; i < list.size(); ++i) {
			IContentType source = (IContentType)list.get(i);
			IContentTypeManager.ContentTypeChangeEvent event =  new IContentTypeManager.ContentTypeChangeEvent(source, projectScope);
			CModelManager.getDefault().contentTypeChanged(event);
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
