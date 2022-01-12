/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - modified for Automake editor usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

public class GNUMakefileConstants extends MakeFileConstants {
	static final String CONDITIONAL_ELSE = "else"; //$NON-NLS-1$
	static final String CONDITIONAL_IF = "if"; //$NON-NLS-1$
	static final String AT_CONDITIONAL_IF = "@if"; //$NON-NLS-1$
	static final String CONDITIONAL_IFNEQ = "ifneq"; //$NON-NLS-1$
	static final String CONDITIONAL_IFNDEF = "ifndef"; //$NON-NLS-1$
	static final String CONDITIONAL_IFEQ = "ifeq"; //$NON-NLS-1$
	static final String CONDITIONAL_IFDEF = "ifdef"; //$NON-NLS-1$

	static final String TERMINAL_ENDEF = "endef"; //$NON-NLS-1$
	static final String TERMINAL_ENDIF = "endif"; //$NON-NLS-1$
	static final String AT_TERMINAL_ENDIF = "@endif"; //$NON-NLS-1$

	static final String DIRECTIVE_VPATH = "vpath"; //$NON-NLS-1$
	static final String DIRECTIVE_UNEXPORT = "unexport"; //$NON-NLS-1$

	static final String VARIABLE_DEFINE = "define"; //$NON-NLS-1$
	static final String VARIABLE_EXPORT = "export"; //$NON-NLS-1$
	static final String VARIABLE_OVERRIDE = "override"; //$NON-NLS-1$

	static final String DIRECTIVE_INCLUDE = "include"; //$NON-NLS-1$

	static final String RULE_DELETE_ON_ERROR = ".DELETE_ON_ERROR"; //$NON-NLS-1$
	static final String RULE_PHONY = ".PHONY"; //$NON-NLS-1$
	static final String RULE_SECONDARY = ".SECONDARY"; //$NON-NLS-1$
	static final String RULE_LOW_RESOLUTION_TIME = ".LOW_RESOLUTION_TIME"; //$NON-NLS-1$
	static final String RULE_NOT_PARALLEL = ".NOTPARALLEL"; //$NON-NLS-1$
	static final String RULE_EXPORT_ALL_VARIABLES = ".EXPORT_ALL_VARIABLES"; //$NON-NLS-1$
	static final String RULE_INTERMEDIATE = ".INTERMEDIATE"; //$NON-NLS-1$
}
