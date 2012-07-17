/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Jason Litton (Sage Electronic Engineering, LLC) - Added support for dynamic debug tracing
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import org.eclipse.cdt.ui.CUIDebugOptions;
import org.eclipse.cdt.ui.CUIPlugin;

public class Util implements IDebugLogConstants{
	/**
	 * @deprecated use org.eclipse.cdt.ui.CUIDebugOptions.DEBUG_CONTENT_ASSIST 
	 * to take advantage of dynamic debugging
	 */
	@Deprecated
	public static boolean VERBOSE_CONTENTASSIST = false;
	private Util() {
	}
	/*
	 * Add a log entry
	 */
	
	/**
	 * @deprecated use org.eclipse.cdt.ui.CUIDebugOptions to take
	 * advantage of dynamic debugging
	 */
	@Deprecated
	public static void debugLog(String message, DebugLogConstant client) {
		if( CUIPlugin.getDefault() == null ) return;
		if ( CUIPlugin.getDefault().isDebugging() && isActive(client)) {
			while (message.length() > 100) {	
				String partial = message.substring(0, 100);
				message = message.substring(100);
				System.out.println(partial + "\\"); //$NON-NLS-1$
			}
			if (message.endsWith("\n")) { //$NON-NLS-1$
				System.err.print(message);
			} else {
				System.out.println(message);
			}
		}
	}
	
	public static boolean isActive(DebugLogConstant client) {
		if (client.equals(IDebugLogConstants.CONTENTASSIST)){
			return CUIDebugOptions.DEBUG_CONTENT_ASSIST;
		}
		return false;
	}
	
}
