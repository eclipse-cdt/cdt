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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

/**
 * Specialization of the C label provider that allows drawing divider lines used
 * in Outline view and information control
 */
public class DecoratingCOutlineLabelProvider extends DecoratingCLabelProvider {

	private static final int MAXIMUM_REASONABLE_WIDTH = 4000;

	public DecoratingCOutlineLabelProvider(CUILabelProvider labelProvider) {
		super(labelProvider, true);
	}

	@Override
	protected void measure(Event event, Object element) {
		if (!isOwnerDrawEnabled())
			return;
		if (element instanceof DividerLine) {
			GC gc = event.gc;
			if (gc == null) {
				// If there is no gc (can this happen?) default to a reasonably wide measurement
				event.width = MAXIMUM_REASONABLE_WIDTH;
			} else {
				// Use the clipping area of the event to know how wide the tree control
				// is so that we can make a line exactly the right size.
				// This has the side effect the tree can never become narrower than this
				// width as the width of the tree is in part based on the width of its
				// widest item. Therefore if the view becomes smaller, a horizontal
				// scroll bar is created
				// We use a max of MAXIMUM_REASONABLE_WIDTH here to ensure that we don't
				// end up in a loop that the item asks for a wider width, so the tree gets wider, etc.
				Rectangle clipping = gc.getClipping();
				event.width = Math.min(clipping.width - event.x, MAXIMUM_REASONABLE_WIDTH);
			}
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
			// draw a line as wide as possible, we can't use event.width here as that doesn't take into account
			// our declared width in measure. On Windows this is clipped to the size we measured above, but
			// on GTK and macOS this is clipped to the containing tree control.
			event.gc.drawLine(0, y, event.x + MAXIMUM_REASONABLE_WIDTH, y);
		} else {
			super.paint(event, element);
		}
	}
}
