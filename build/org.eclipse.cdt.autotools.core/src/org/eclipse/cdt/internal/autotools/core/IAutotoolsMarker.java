/*******************************************************************************
 * Copyright (c) 2010, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;

public interface IAutotoolsMarker {
	String AUTOTOOLS_PROBLEM_MARKER = AutotoolsPlugin.PLUGIN_ID + ".problem"; //$NON-NLS-1$
	String MARKER_VARIABLE = "problem.variable"; //$NON-NLS-1$
	String MARKER_EXTERNAL_LOCATION = "problem.externalLocation"; //$NON-NLS-1$
	String MARKER_LIBRARY_INFO = "problem.libraryInfo"; //$NON-NLS-1$
	String MARKER_PROBLEM_TYPE = "problem.type"; //$NON-NLS-1$
}
