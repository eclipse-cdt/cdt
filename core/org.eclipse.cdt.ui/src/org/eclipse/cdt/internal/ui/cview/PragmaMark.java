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

import org.eclipse.cdt.core.model.IPragma;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

public class PragmaMark extends WorkbenchAdapter implements IAdaptable {

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
	public ImageDescriptor getImageDescriptor(Object object) {
		return CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OUTLINE_MARK);
	}

	@Override
	public StyledString getStyledText(Object object) {
		StyledString styledText = new StyledString(elementName);
		Styler boldStyler = new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.font = JFaceResources.getFont(PreferenceConstants.OUTLINE_MARK_TEXT_FONT);
				ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
						.getColorRegistry();
				textStyle.foreground = new Color(colorRegistry.getRGB(PreferenceConstants.OUTLINE_MARK_TEXT_COLOR));
			}
		};
		styledText.setStyle(0, styledText.length(), boldStyler);
		return styledText;
	}

}
