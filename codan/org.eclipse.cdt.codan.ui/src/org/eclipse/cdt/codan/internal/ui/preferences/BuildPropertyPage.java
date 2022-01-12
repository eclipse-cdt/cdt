/*******************************************************************************
 * Copyright (c) 2011, 2015 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.actions.ToggleNatureAction;
import org.eclipse.cdt.codan.ui.LabelFieldEditor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPropertyPage;

public class BuildPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {
	private IAdaptable element;

	public BuildPropertyPage() {
		setPreferenceStore(CodanUIActivator.getDefault().getCorePreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor(CodanUIMessages.BuildPropertyPage_Description, getFieldEditorParent()));
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
				boolean runOnBuild = getPreferenceStore().getBoolean(PreferenceConstants.P_RUN_ON_BUILD);
				new ToggleNatureAction().toggleNature((IProject) res, runOnBuild);
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

	@Override
	public IAdaptable getElement() {
		if (element.getAdapter(IProject.class) != null)
			return element.getAdapter(IProject.class);
		return null;
	}

	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
		if (getElement() != null) {
			IPreferenceStore scoped = CodanUIActivator.getDefault().getPreferenceStore(((IProject) getElement()));
			setPreferenceStore(scoped);
		}
	}
}
