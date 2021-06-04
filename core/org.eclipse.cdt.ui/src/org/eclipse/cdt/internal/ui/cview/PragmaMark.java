/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPragma;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

public class PragmaMark extends WorkbenchAdapter implements IAdaptable {

	private ICElement element;
	private String elementName;

	public PragmaMark(IPragma element, String elementName) {
		this.elementName = elementName;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return adapter.cast(this);
		return null;
	}

	@Override
	public String getLabel(Object object) {
		return elementName;
	}

	@Override
	public StyledString getStyledText(Object object) {
		StyledString styledText = super.getStyledText(object);
		Styler boldStyler = new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
			}
		};
		styledText.setStyle(0, styledText.length(), boldStyler);
		return styledText;
	}

}
