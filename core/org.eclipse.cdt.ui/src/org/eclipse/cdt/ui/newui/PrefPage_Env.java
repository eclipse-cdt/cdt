package org.eclipse.cdt.ui.newui;

public class PrefPage_Env extends AbstractPrefPage {
	
	protected boolean isSingle() { return true; }

	// Tabs themselves should save data 
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}
}
