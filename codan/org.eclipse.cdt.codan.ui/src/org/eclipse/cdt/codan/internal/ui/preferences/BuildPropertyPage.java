package org.eclipse.cdt.codan.internal.ui.preferences;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.ui.actions.ToggleNatureAction;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class BuildPropertyPage extends FieldEditorPreferencePage implements
		IWorkbenchPropertyPage {
	private IAdaptable element;

	/**
	 * 
	 */
	public BuildPropertyPage() {
		setPreferenceStore(org.eclipse.cdt.codan.ui.Activator.getDefault()
				.getPreferenceStore());
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
				"&Run with Build", getFieldEditorParent()));
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
		return element;
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
		ProjectScope ps = new ProjectScope((IProject) getElement());
		ScopedPreferenceStore scoped = new ScopedPreferenceStore(ps,
				CodanCorePlugin.PLUGIN_ID);
		scoped
				.setSearchContexts(new IScopeContext[] { ps,
						new InstanceScope() });
		setPreferenceStore(scoped);
	}

	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage";
	}
}
