package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

public class Page_head_general extends PropertyPage {
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		return parent;
	}
}
