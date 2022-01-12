/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.ui.newui.AbstractPage;

public class AutotoolsHeadPropertyPage extends AbstractPage {

	@Override
	protected boolean isSingle() {
		return true;
	}

	@Override
	protected boolean showsConfig() {
		return false;
	}

}
