/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.meson.core;

public interface IMesonConstants {
	
	public static final String MESON_ARGUMENTS = "meson.arguments"; //$NON-NLS-1$
	public static final String MESON_ENV = "meson.environment"; //$NON-NLS-1$
	public static final String MESON_ENV_SEPARATOR = "|"; //$NON-NLS-1$
	String MESON_GENERATOR = "meson.generator"; //$NON-NLS-1$
	String BUILD_COMMAND = "meson.command.build"; //$NON-NLS-1$
	String CLEAN_COMMAND = "meson.command.clean"; //$NON-NLS-1$

}
