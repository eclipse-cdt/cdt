package org.eclipse.cdt.launchbar.ui.internal;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LaunchBarPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public LaunchBarPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
	    setPreferenceStore(Activator.getDefault().getPreferenceStore());
	    setDescription("Preferences for the Launch Bar.");
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(Activator.PREF_ENABLE_LAUNCHBAR, "Enable the Launch Bar.", getFieldEditorParent()));
	}
	
}
