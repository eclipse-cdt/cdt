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
package org.eclipse.cdt.debug.mi.core; 

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
 
/**
 * Utility methods.
 */
public class MICoreUtils {
	
	public static File[] getAutoSolibs( ILaunchConfiguration configuration ) throws CoreException {
		List autoSolibs = configuration.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB_LIST, Collections.EMPTY_LIST );
		List list = new ArrayList( autoSolibs.size() );
		Iterator it = autoSolibs.iterator();
		while( it.hasNext() ) {
			list.add( new File( (String)it.next() ) );
		}
		return (File[])list.toArray( new File[list.size()] );
	}
}
