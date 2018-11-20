/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.meson.ui.properties;

import org.eclipse.swt.widgets.Composite;

public class MesonPropertyArgs extends MesonPropertyText {

	public MesonPropertyArgs(Composite composite, String name, String initialValue, String tooltip) {
		super(composite, name, initialValue, tooltip);
	}

	@Override
	public String getConfiguredString() {
		// TODO Auto-generated method stub
		return "-D" + getFieldName() + "='" + getFieldValue() + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
