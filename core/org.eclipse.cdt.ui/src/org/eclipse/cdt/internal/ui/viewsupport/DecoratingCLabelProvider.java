/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Hofer [bug 325799]
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.jface.viewers.DecorationContext;
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
		super(labelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), DecorationContext.DEFAULT_CONTEXT);
		if (errorTick) {
			labelProvider.addLabelDecorator(new ProblemsLabelDecorator(null));
		}
	}
}
