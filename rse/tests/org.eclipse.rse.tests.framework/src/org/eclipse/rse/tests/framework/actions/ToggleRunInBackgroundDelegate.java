package org.eclipse.rse.tests.framework.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.tests.framework.TestFrameworkPlugin;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ToggleRunInBackgroundDelegate extends Object implements IViewActionDelegate {
	
	public void init(IViewPart view) {
	}

	public void run(IAction action) {
		boolean runInBackground = action.isChecked();
		setPreference(runInBackground);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	private void setPreference(boolean runInBackground) {
		IPreferenceStore store = TestFrameworkPlugin.getDefault().getPreferenceStore();
		store.setValue(TestFrameworkPlugin.PREF_RUN_IN_BACKGROUND, runInBackground);
	}
	
}
