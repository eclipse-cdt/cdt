/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;

public interface IAutotoolsMarker {
	public static String AUTOTOOLS_PROBLEM_MARKER = AutotoolsPlugin.PLUGIN_ID + ".problem"; //$NON-NLS-1$
	public static final String MARKER_VARIABLE = "problem.variable"; //$NON-NLS-1$
	public static final String MARKER_EXTERNAL_LOCATION = "problem.externalLocation"; //$NON-NLS-1$
	public static final String MARKER_LIBRARY_INFO="problem.libraryInfo"; //$NON-NLS-1$
	public static final String MARKER_PROBLEM_TYPE="problem.type"; //$NON-NLS-1$	
}
