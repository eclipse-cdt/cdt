package org.eclipse.cdt.ui.newui;

public class PrefPage_Vars extends AbstractPrefPage {

	protected boolean isSingle() { return true; }
	protected boolean needsHeader() { return false; }

	// Tabs themselves should save data 
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}

}
