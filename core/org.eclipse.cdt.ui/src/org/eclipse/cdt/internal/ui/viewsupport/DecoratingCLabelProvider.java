/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Hofer [bug 325799]
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.internal.ui.cview.DividerLine;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;

public class DecoratingCLabelProvider extends ColoringLabelProvider {

	/**
	 * Decorating label provider for C/C++. Combines a CUILabelProvider
	 * with problem and override indicator with the workbench decorator (label
	 * decorator extension point).
	 * @param labelProvider the label provider to decorate
	 */
	public DecoratingCLabelProvider(CUILabelProvider labelProvider) {
		this(labelProvider, true);
	}

	/**
	 * Decorating label provider for C/C++. Combines a CUILabelProvider
	 * (if enabled with problem indicator) with the workbench
	 * decorator (label decorator extension point).
	 * @param labelProvider the label provider to decorate
	 * @param errorTick show error ticks
	 */
	public DecoratingCLabelProvider(CUILabelProvider labelProvider, boolean errorTick) {
		super(labelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(),
				DecorationContext.DEFAULT_CONTEXT);
		if (errorTick) {
			labelProvider.addLabelDecorator(new ProblemsLabelDecorator(null));
		}
	}

	@Override
	protected void measure(Event event, Object element) {
		if (!isOwnerDrawEnabled())
			return;

		super.measure(event, element);

	}

	@Override
	protected void paint(Event event, Object element) {
		if (!isOwnerDrawEnabled())
			return;
		if (element instanceof DividerLine) {
			// XXX: Review this - is this an OK thing to do? Is there API already so I can delegate this to the DiverLine already?
			int y = event.y + event.height / 2;
			ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme()
					.getColorRegistry();
			event.gc.setForeground(new Color(colorRegistry.getRGB(PreferenceConstants.OUTLINE_MARK_TEXT_COLOR)));
			event.gc.drawLine(event.x - 2000, y, event.x + 2000, y);
		} else {
			super.paint(event, element);
		}
	}
}
