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
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.internal.ui.cview.DividerLine;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

/**
 * Specialization of the C label provider that allows drawing divider lines used
 * in Outline view and information control
 */
public class DecoratingCOutlineLabelProvider extends DecoratingCLabelProvider {

	public DecoratingCOutlineLabelProvider(CUILabelProvider labelProvider) {
		super(labelProvider, true);
	}

	@Override
	protected void measure(Event event, Object element) {
		if (!isOwnerDrawEnabled())
			return;
		if (element instanceof DividerLine) {
			event.width = 5000;
		} else {
			super.measure(event, element);
		}
	}

	@Override
	protected void paint(Event event, Object element) {
		if (!isOwnerDrawEnabled())
			return;
		if (element instanceof DividerLine) {
			int y = event.y + event.height / 2;
			ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
					.getColorRegistry();
			event.gc.setForeground(new Color(colorRegistry.getRGB(PreferenceConstants.OUTLINE_MARK_DIVIDER_COLOR)));
			// draw a line as wide as possible
			event.gc.drawLine(0, y, event.x + 4000, y);
		} else {
			super.paint(event, element);
		}
	}
}
