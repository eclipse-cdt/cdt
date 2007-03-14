package org.eclipse.cdt.ui.newui;

public class PrefPage_Vars extends AbstractPrefPage {

	protected boolean isSingle() { return true; }
	protected String getHeader() { return null; }

	// Tabs themselves should save data 
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}

}
