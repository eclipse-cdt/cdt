package org.eclipse.cdt.managedbuilder.mingw.ui.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.cdt.managedbuilder.gnu.mingw.MinGW;
import org.eclipse.cdt.managedbuilder.mingw.ui.Activator;

public class MinGWPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public MinGWPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.MinGWPrefPage_Description);
	}
	
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.MINGW_LOCATION, 
				Messages.MinGWPrefPage_MingwLocation, getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.MSYS_LOCATION, 
				Messages.MinGWPrefPage_MsysLocation, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}
	
	@Override
	public boolean performOk() {
		MinGW.invalidateCache();
		return super.performOk();
	}
	
}
