/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;

/**
 * The preference block for configuring style of include statements.
 */
public class IncludeStyleBlock extends TabConfigurationBlock {

	public IncludeStyleBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, getTabs(context, project, container), getTabLabels(), container);
	}

	private static OptionsConfigurationBlock[] getTabs(IStatusChangeListener context,
			IProject project, IWorkbenchPreferenceContainer container) {
		return new OptionsConfigurationBlock[] {
				new IncludeCategoriesBlock(context, project, container),
				new IncludeOrderBlock(context, project, container),
			};
	}

	private static String[] getTabLabels() {
		return new String[] {
				PreferencesMessages.IncludeStyleBlock_categories_tab,
				PreferencesMessages.IncludeStyleBlock_order_tab,
			};
	}
}
