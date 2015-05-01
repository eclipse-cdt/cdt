package org.eclipse.launchbar.ui.internal;

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
	    setDescription(Messages.LaunchBarPreferencePage_0);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(Activator.PREF_ENABLE_LAUNCHBAR, Messages.LaunchBarPreferencePage_1, getFieldEditorParent()));
	}
	
}
