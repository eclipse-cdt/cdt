/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents an installation of the Qt SDK. Qt installs are defined by the path
 * to the qmake executable.
 *
 * @noimplement
 */
public interface IQtInstall {

	Path getQmakePath();

	String getSpec();

	Path getLibPath();

	Path getQmlPath();

	void setProperty(String key, String value);

	Map<String, String> getProperties();

}
