/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.actions.ToggleNatureAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPropertyPage;

public class BuildPropertyPage extends FieldEditorPreferencePage implements
		IWorkbenchPropertyPage {
	private IAdaptable element;

	/**
	 * 
	 */
	public BuildPropertyPage() {
		setPreferenceStore(CodanUIActivator.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.P_RUN_ON_BUILD,
				CodanUIMessages.BuildPropertyPage_RunWithBuild, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_RUN_IN_EDITOR,
				CodanUIMessages.BuildPropertyPage_RunAsYouType, getFieldEditorParent()));
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			IAdaptable res = getElement();
			if (res instanceof IProject) {
				boolean runOnBuild = getPreferenceStore().getBoolean(
						PreferenceConstants.P_RUN_ON_BUILD);
				new ToggleNatureAction().toggleNature((IProject) res,
						runOnBuild);
				// if (runOnBuild == false) {
				// boolean openQuestion = MessageDialog
				// .openQuestion(
				// getShell(),
				// "Confirmation",
				// "Do you want to remove existing problems? If build is disabled they won't be updated anymore.");
				// if (openQuestion == true) {
				// CodanMarkerProblemReporter.deleteAllMarkers();
				// }
				// }
				return true;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	public IAdaptable getElement() {
		if (element.getAdapter(IProject.class) != null)
			return (IProject) element.getAdapter(IProject.class);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime
	 * .IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		this.element = element;
		if (getElement() != null) {
			IPreferenceStore scoped = CodanUIActivator.getDefault()
					.getPreferenceStore(((IProject) getElement()));
			setPreferenceStore(scoped);
		}
	}

	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage"; //$NON-NLS-1$
	}
}
