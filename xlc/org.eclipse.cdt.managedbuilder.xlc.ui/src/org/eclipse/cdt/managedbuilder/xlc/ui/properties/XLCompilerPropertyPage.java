/*******************************************************************************
 *  Copyright (c) 2007, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui.properties;

import org.eclipse.cdt.managedbuilder.ui.properties.BuildOptionComboFieldEditor;
import org.eclipse.cdt.managedbuilder.xlc.ui.Messages;
import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPropertyPage;

public class XLCompilerPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	protected String originalMessage;

	protected Composite versionParent;


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		createPathEditor();
		createVersionEditor();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	protected void createPathEditor() {

		Composite parent = getFieldEditorParent();

		fPathEditor = new DirectoryFieldEditor(PreferenceConstants.P_XL_COMPILER_ROOT,
				Messages.XLCompilerPropertyPage_0, parent) {
			@Override
			protected boolean doCheckState() {
				// always return true, as we don't want to fail cases when
				// compiler is installed remotely
				// just warn user
				if (!super.doCheckState()) {
					setMessage(Messages.XLCompilerPropertyPage_2, IMessageProvider.WARNING);
				} else {
					setMessage(originalMessage);
				}

				return true;
			}

			@Override
			protected boolean checkState() {
				return doCheckState();
			}

		};

		addField(fPathEditor);

		IProject project = ((IResource) (getElement().getAdapter(IResource.class))).getProject();

		String currentPath = null;

		try {
			currentPath = project.getPersistentProperty(new QualifiedName("", //$NON-NLS-1$
					PreferenceConstants.P_XL_COMPILER_ROOT));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (currentPath == null) {
			// if the property isn't set, then use the workbench preference
			IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
			currentPath = prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
		}

		fPathEditor.setStringValue(currentPath);

	}

	protected void createVersionEditor() {

		IProject project = ((IResource) (getElement().getAdapter(IResource.class))).getProject();
		String[] versionEntries = { PreferenceConstants.P_XL_COMPILER_VERSION_8_NAME,
				PreferenceConstants.P_XL_COMPILER_VERSION_9_NAME, PreferenceConstants.P_XL_COMPILER_VERSION_10_NAME, PreferenceConstants.P_XL_COMPILER_VERSION_11_NAME };

		versionParent = getFieldEditorParent();

		fVersionEditor = new BuildOptionComboFieldEditor(PreferenceConstants.P_XLC_COMPILER_VERSION,
				Messages.XLCompilerPropertyPage_1, versionEntries, null, versionParent);

		addField(fVersionEditor);

		String currentVersion = null;
		try {
			currentVersion = project.getPersistentProperty(new QualifiedName("", //$NON-NLS-1$
					PreferenceConstants.P_XLC_COMPILER_VERSION));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (currentVersion == null) {
			// if the property isn't set, then use the workbench preference
			IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
			currentVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
		}

		Combo versionCombo = fVersionEditor.getComboControl(versionParent);
		versionCombo.setText(PreferenceConstants.getVersionLabel(currentVersion));

	}

	protected DirectoryFieldEditor fPathEditor;

	protected BuildOptionComboFieldEditor fVersionEditor;

	// private Composite parent;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public XLCompilerPropertyPage() {
		super(FieldEditorPreferencePage.FLAT);

		originalMessage = getMessage();
	}

	@Override
	protected void performDefaults() {
		// default to whatever is set on the workbench preference
		IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
		String currentPath = prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
		String currentVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
		String currentVersionLabel = PreferenceConstants.getVersionLabel(currentVersion);
		if (fPathEditor != null) {
			fPathEditor.setStringValue(currentPath);
		}
		//set the selection to default setting
		fVersionEditor.setPreferenceStore(prefStore);
		fVersionEditor.setPreferenceName(PreferenceConstants.P_XLC_COMPILER_VERSION);
		fVersionEditor.loadDefault();
		//set the text entry to default setting
		fVersionEditor.getComboControl(versionParent).setText(currentVersionLabel);


	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		// store the value in the owner text field
		try {
			IProject project = ((IResource) (getElement().getAdapter(IResource.class))).getProject();

			if (fPathEditor != null) {
				project.setPersistentProperty(new QualifiedName("", //$NON-NLS-1$
						PreferenceConstants.P_XL_COMPILER_ROOT), fPathEditor.getStringValue());
			}
			String version = null;
			if (fVersionEditor.getSelection() != null) {
				version = PreferenceConstants.getVersion(fVersionEditor.getSelection());

				project.setPersistentProperty(new QualifiedName("", //$NON-NLS-1$
						PreferenceConstants.P_XLC_COMPILER_VERSION), version);
			}
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	/**
	 * The element.
	 */
	private IAdaptable element;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	@Override
	public IAdaptable getElement() {
		return element;
	}

	/**
	 * Sets the element that owns properties shown on this page.
	 *
	 * @param element
	 *            the element
	 */
	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
	}

}