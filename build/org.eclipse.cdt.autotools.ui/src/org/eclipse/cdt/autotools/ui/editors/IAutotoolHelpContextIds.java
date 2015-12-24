/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
