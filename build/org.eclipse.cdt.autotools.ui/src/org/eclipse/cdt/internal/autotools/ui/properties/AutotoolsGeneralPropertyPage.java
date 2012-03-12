package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.ui.newui.AbstractPage;

public class AutotoolsGeneralPropertyPage extends AbstractPage {

	@Override
	protected boolean isSingle() {
		return false;
	}
	
	@Override
	protected boolean showsConfig() { return false;	}

}
