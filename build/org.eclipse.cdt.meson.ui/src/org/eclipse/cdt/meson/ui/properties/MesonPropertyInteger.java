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

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

public class MesonPropertyInteger extends MesonPropertyText {

	@SuppressWarnings("unused")
	private MesonPropertyPage page;
	private String errorMessage;

	public MesonPropertyInteger(Composite composite, MesonPropertyPage page, String name, String initialValue,
			String tooltip) {
		super(composite, name, initialValue, tooltip);
		this.page = page;
		text.addModifyListener((e) -> {
			if (isValid() != page.isValid()) {
				page.update();
			}
		});
	}

	@Override
	public boolean isValid() {
		errorMessage = null;
		try {
			Integer.parseInt(getFieldValue());
			return true;
		} catch (NumberFormatException e) {
			errorMessage = NLS.bind(Messages.MesonPropertyPage_not_an_integer, getFieldName());
		}
		return false;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

}
