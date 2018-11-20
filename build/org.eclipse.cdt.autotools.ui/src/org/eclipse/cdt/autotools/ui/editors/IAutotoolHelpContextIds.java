/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;

public interface IAutotoolHelpContextIds {
	String PREFIX = AutotoolsUIPlugin.getUniqueIdentifier();
	String AC_EDITOR_VIEW = PREFIX + "autoconf_editor"; //$NON-NLS-1$
	String SHOW_TOOLTIP_ACTION = PREFIX + "show_tooltip_action"; //$NON-NLS-1$
	String CONTENT_ASSIST = PREFIX + "content_assist"; //$NON-NLS-1$
}
