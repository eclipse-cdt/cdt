/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerFileSICollector;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.runtime.IPath;

/**
 * @author laggarcia
 *
 */
public class PerFileXLCScannerInfoCollector extends PerFileSICollector
		implements IScannerInfoCollector3, IManagedScannerInfoCollector {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#getDefinedSymbols()
	 */
	public Map getDefinedSymbols() {
		return getAllSymbols();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#getIncludePaths()
	 */
	public List getIncludePaths() {
		List<String> pathStrings = new LinkedList<String>();
		
		List<IPath> paths = Arrays.asList(getAllIncludePaths(INCLUDE_PATH));
		paths.addAll(Arrays.asList(getAllIncludePaths(QUOTE_INCLUDE_PATH)));
		
		for(IPath path : paths) {
			pathStrings.add(path.toString());
		}
		
		return pathStrings;
	}

}