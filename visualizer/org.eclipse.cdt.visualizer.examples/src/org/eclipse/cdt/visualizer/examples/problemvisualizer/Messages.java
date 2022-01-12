/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.visualizer.examples.problemvisualizer;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 */
class Messages extends NLS {
	public static String ProblemCountVisualizer_Name;
	public static String ProblemCountVisualizer_DisplayName;
	public static String ProblemCountVisualizer_Description;
	public static String ProblemCountVisualizer_Errors;
	public static String ProblemCountVisualizer_Warnings;
	public static String ProblemCountVisualizer_Infos;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
