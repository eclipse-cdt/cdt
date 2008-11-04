/*******************************************************************************
 * Copyright (c) 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.executables;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class StandardSourceFileRemapping implements ISourceFileRemapping {

	public String remapSourceFile(Executable executable, String filePath) {

		try {
			Object[] foundElements = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().findSourceElements(filePath);

			if (foundElements.length == 0) {
				Object foundElement = null;
				ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
				ILaunch[] launches = launchMgr.getLaunches();
				for (ILaunch launch : launches) {
					ISourceLocator locator = launch.getSourceLocator();
					if (locator instanceof ICSourceLocator || locator instanceof CSourceLookupDirector) {
						if (locator instanceof ICSourceLocator)
							foundElement = ((ICSourceLocator) locator).findSourceElement(filePath);
						else
							foundElement = ((CSourceLookupDirector) locator).getSourceElement(filePath);
					}
				}
				if (foundElement != null)
					foundElements = new Object[] { foundElement };
			}

			if (foundElements.length == 1 && foundElements[0] instanceof LocalFileStorage) {
				LocalFileStorage newLocation = (LocalFileStorage) foundElements[0];
				filePath = newLocation.getFullPath().toOSString();
			}

		} catch (CoreException e) {
		}
		return filePath;
	}

}