/*******************************************************************************
 * Copyright (c) 2012 Google, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.externaltool;

import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.ui.PartInitException;

/**
 * @author alruiz@google.com (Alex Ruiz)
 */
class ConsolePrinterFactory {
	ConsolePrinter createConsolePrinter(String externalToolName, boolean shouldDisplayOutput) {
		if (shouldDisplayOutput) {
			try {
				return ConsolePrinterImpl.createOrFindConsole(externalToolName);
			} catch (PartInitException e) {
				CodanUIActivator.log("Unable to create/find console", e); //$NON-NLS-1$
			}
		}
		return ConsolePrinter.NullImpl;
	}
}
