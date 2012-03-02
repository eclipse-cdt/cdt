/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
