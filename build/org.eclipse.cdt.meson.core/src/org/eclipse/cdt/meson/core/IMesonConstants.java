/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.meson.core;

public interface IMesonConstants {

	String MESON_ARGUMENTS = "meson.arguments"; //$NON-NLS-1$
	String MESON_ENV = "meson.environment"; //$NON-NLS-1$
	String MESON_PROJECT_OPTIONS = "meson.project.options"; //$NON-NLS-1$
	String NINJA_ENV = "meson.ninja.environment"; //$NON-NLS-1$
	String NINJA_ARGUMENTS = "meson.ninja.arguments"; //$NON-NLS-1$
	String MESON_ENV_SEPARATOR = "|"; //$NON-NLS-1$
	String MESON_GENERATOR = "meson.generator"; //$NON-NLS-1$
	String BUILD_COMMAND = "meson.command.build"; //$NON-NLS-1$
	String CLEAN_COMMAND = "meson.command.clean"; //$NON-NLS-1$

}
