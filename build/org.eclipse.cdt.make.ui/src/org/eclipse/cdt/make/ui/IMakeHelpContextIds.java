/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

public interface IMakeHelpContextIds {
	public static final String PREFIX = MakeUIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$
	
	public static final String MAKE_PATH_SYMBOL_SETTINGS = PREFIX + "cdt_paths_symbols_page"; //$NON-NLS-1$
	public static final String MAKE_BUILDER_SETTINGS = PREFIX + "cdt_make_builder_page"; //$NON-NLS-1$
}
