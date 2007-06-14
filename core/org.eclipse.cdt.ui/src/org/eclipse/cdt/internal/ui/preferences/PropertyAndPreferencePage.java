/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;

/**
 * Base for project property and preference pages
 */
public abstract class PropertyAndPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {
	
	private Control fConfigurationBlockControl;
	private ControlEnableState fBlockEnableState;
	private Link fChangeWorkspaceSettings;
//	private SelectionButtonDialogField fUseProjectSettings;
	private IStatus fBlockStatus;
	private Composite fParentComposite;
	
	private IProject fProject; // project or null
	private Map fData; // page data
	
	public static final String DATA_NO_LINK= "PropertyAndPreferencePage.nolink"; //$NON-NLS-1$
	
	public PropertyAndPreferencePage() {
		fBlockStatus= new StatusInfo();
		fBlockEnableState= null;
		fProject= null;
		fData= null;
	}

	protected abstract Control createPreferenceContent(Composite composite);
	protected abstract boolean hasProjectSpecificOptions(IProject project);
	
	protected abstract String getPreferencePageID();
	protected abstract String getPropertyPageID();
	
	protected boolean supportsProjectSpecificOptions() {
		return getPropertyPageID() != null;
	}
	
	protected boolean offerLink() {
		return fData == null || !Boolean.TRUE.equals(fData.get(DATA_NO_LINK));
	}
	
// TODO: Project specific settings are not supported yet.
//protected Label createDescriptionLabel(Composite parent) {
//		fParentComposite= parent;
//		if (isProjectPreferencePage()) {
//			Composite composite= new Composite(parent, SWT.NONE);
//			composite.setFont(parent.getFont());
//			GridLayout layout= new GridLayout();
//			layout.marginHeight= 0;
//			layout.marginWidth= 0;
//			layout.numColumns= 2;
//			composite.setLayout(layout);
//			composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//			
//			IDialogFieldListener listener= new IDialogFieldListener() {
//				public void dialogFieldChanged(DialogField field) {
//					enableProjectSpecificSettings(((SelectionButtonDialogField)field).isSelected());
//				}
//			};
//			
//			fUseProjectSettings= new SelectionButtonDialogField(SWT.CHECK);
//			fUseProjectSettings.setDialogFieldListener(listener);
//			fUseProjectSettings.setLabelText(PreferencesMessages.PropertyAndPreferencePage_useprojectsettings_label); 
//			fUseProjectSettings.doFillIntoGrid(composite, 1);
//			LayoutUtil.setHorizontalGrabbing(fUseProjectSettings.getSelectionButton(null));
//			
//			if (offerLink()) {
//				fChangeWorkspaceSettings= createLink(composite, PreferencesMessages.PropertyAndPreferencePage_useworkspacesettings_change);
//				fChangeWorkspaceSettings.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
//			} else {
//				LayoutUtil.setHorizontalSpan(fUseProjectSettings.getSelectionButton(null), 2);
//			}
//			
//			Label horizontalLine= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
//			horizontalLine.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
//			horizontalLine.setFont(composite.getFont());
//		} else if (supportsProjectSpecificOptions() && offerLink()) {
//			fChangeWorkspaceSettings= createLink(parent, PreferencesMessages.PropertyAndPreferencePage_showprojectspecificsettings_label);
//			fChangeWorkspaceSettings.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
//		}
//
//		return super.createDescriptionLabel(parent);
//    }
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
			
		GridData data= new GridData(GridData.FILL, GridData.FILL, true, true);
		
		fConfigurationBlockControl= createPreferenceContent(composite);
		fConfigurationBlockControl.setLayoutData(data);

//      TODO: Project specific settings are not supported yet.
//		if (isProjectPreferencePage()) {
//			boolean useProjectSettings= hasProjectSpecificOptions(getProject());
//			enableProjectSpecificSettings(useProjectSettings);
//		}

		Dialog.applyDialogFont(composite);
		return composite;
	}

	protected boolean useProjectSettings() {
//      TODO: Project specific settings are not supported yet.
//		return isProjectPreferencePage() && fUseProjectSettings != null && fUseProjectSettings.isSelected();
		return false;
	}
	
	protected boolean isProjectPreferencePage() {
		return fProject != null;
	}
	
	protected IProject getProject() {
		return fProject;
	}
	
	protected final void openWorkspacePreferences(Object data) {
		String id= getPreferencePageID();
		PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, data).open();
	}
	
	protected final void openProjectProperties(IProject project, Object data) {
		String id= getPropertyPageID();
		if (id != null) {
			PreferencesUtil.createPropertyDialogOn(getShell(), project, id, new String[] { id }, data).open();
		}
	}
	
//	TODO: Project specific settings are not supported yet.
//	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
//		fUseProjectSettings.setSelection(useProjectSpecificSettings);
//		enablePreferenceContent(useProjectSpecificSettings);
//		updateLinkVisibility();
//		doStatusChanged();
//	}
//	
//	private void updateLinkVisibility() {
//		if (fChangeWorkspaceSettings == null || fChangeWorkspaceSettings.isDisposed()) {
//			return;
//		}
//		
//		if (isProjectPreferencePage()) {
//			fChangeWorkspaceSettings.setEnabled(!useProjectSettings());
//		}
//	}
	
	protected void setPreferenceContentStatus(IStatus status) {
		fBlockStatus= status;
		doStatusChanged();
	}
	
	/**
	 * Returns a new status change listener that calls {@link #setPreferenceContentStatus(IStatus)}
	 * when the status has changed
	 * @return The new listener
	 */
	protected IStatusChangeListener getNewStatusChangedListener() {
		return new IStatusChangeListener() {
			public void statusChanged(IStatus status) {
				setPreferenceContentStatus(status);
			}
		};		
	}
	
	protected IStatus getPreferenceContentStatus() {
		return fBlockStatus;
	}

	protected void doStatusChanged() {
		if (!isProjectPreferencePage() || useProjectSettings()) {
			updateStatus(fBlockStatus);
		} else {
			updateStatus(new StatusInfo());
		}
	}
		
	protected void enablePreferenceContent(boolean enable) {
		if (enable) {
			if (fBlockEnableState != null) {
				fBlockEnableState.restore();
				fBlockEnableState= null;
			}
		} else {
			if (fBlockEnableState == null) {
				fBlockEnableState= ControlEnableState.disable(fConfigurationBlockControl);
			}
		}	
	}
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
//      TODO: Project specific settings are not supported yet.
//		if (useProjectSettings()) {
//			enableProjectSpecificSettings(false);
//		}
		super.performDefaults();
	}

	private void updateStatus(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		return fProject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		fProject= (IProject) element.getAdapter(IResource.class);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
	 */
	public void applyData(Object data) {
		if (data instanceof Map) {
			fData= (Map) data;
		}
		if (fChangeWorkspaceSettings != null) {
			if (!offerLink()) {
				fChangeWorkspaceSettings.dispose();
				fParentComposite.layout(true, true);
			}
		}
 	}
	
	protected Map getData() {
		return fData;
	}
}
