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
package org.eclipse.cdt.codan.core.externaltool;

/**
 * Finds an Eclipse console that uses the name of an external tool as its own.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public interface IConsolePrinterFinder {
	/**
	 * Finds an Eclipse console that uses the name of an external tool as its own.
	 * @param externalToolName the name of the external tool that will be used as the name of the
	 *        console.
	 * @param shouldDisplayOutput indicates whether the user wants to see the output of the external
	 *        tool in the console.
	 * @return the created or found console.
	 */
	IConsolePrinter findConsole(String externalToolName, boolean shouldDisplayOutput);
}