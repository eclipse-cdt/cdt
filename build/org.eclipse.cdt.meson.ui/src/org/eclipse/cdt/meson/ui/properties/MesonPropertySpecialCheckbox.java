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

public class MesonPropertySpecialCheckbox extends MesonPropertyCheckbox {

	public MesonPropertySpecialCheckbox(Composite composite, String name, boolean initialValue, String tooltip) {
		super(composite, name, initialValue, tooltip);
	}

	@Override
	public String getUnconfiguredString() {
		if (checkbox.getSelection()) {
			return "--" + getFieldName(); //$NON-NLS-1$
		}
		return "";
	}

}
