/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.ui; 

import org.eclipse.cdt.debug.mi.internal.ui.GDBSolibBlock;
import org.eclipse.cdt.debug.mi.internal.ui.SolibSearchPathBlock;
import org.eclipse.cdt.debug.mi.internal.ui.dialogfields.IListAdapter;

/**
 * This class provides utilities for clients of the MI UI.
 */
public class MIUIUtils {

	public static IMILaunchConfigurationComponent createGDBSolibBlock( IMILaunchConfigurationComponent solibSearchBlock, boolean autoSolib, boolean stopOnSolibEvents ) {
		return new GDBSolibBlock( solibSearchBlock, autoSolib, stopOnSolibEvents );
	}

	public static IMILaunchConfigurationComponent createGDBSolibBlock( boolean autoSolib, boolean stopOnSolibEvents ) {
		return new GDBSolibBlock( new SolibSearchPathBlock(), autoSolib, stopOnSolibEvents );
	}

	public static IMILaunchConfigurationComponent createSolibSearchPathBlock( String[] customButtonLabels, IListAdapter listAdapter ) {
		return new SolibSearchPathBlock( customButtonLabels, listAdapter );
	}
}
