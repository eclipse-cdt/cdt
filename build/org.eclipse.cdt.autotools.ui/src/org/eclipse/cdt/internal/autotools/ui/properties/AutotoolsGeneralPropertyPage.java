/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
