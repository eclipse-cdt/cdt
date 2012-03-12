package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.ui.newui.AbstractPage;

public class AutotoolsHeadPropertyPage extends AbstractPage {

	@Override
	protected boolean isSingle() {
		return true;
	}

	@Override
	protected boolean showsConfig() { return false;	}

}
