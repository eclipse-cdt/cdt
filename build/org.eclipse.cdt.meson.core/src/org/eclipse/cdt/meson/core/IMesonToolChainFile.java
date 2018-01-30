/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Red Hat Inc. - modified for use with Meson builder
 *******************************************************************************/
package org.eclipse.cdt.meson.core;

import java.nio.file.Path;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.runtime.CoreException;

/**
 * A toolchain file.
 *
 * @noimplement
 * @noextend
 */
public interface IMesonToolChainFile {

	Path getPath();

	String getProperty(String key);

	void setProperty(String key, String value);

	IToolChain getToolChain() throws CoreException;

}
