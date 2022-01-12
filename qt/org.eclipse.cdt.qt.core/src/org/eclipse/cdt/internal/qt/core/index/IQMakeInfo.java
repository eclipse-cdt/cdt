/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.List;
import java.util.Map;

/**
 * Represents a QMake information.
 *
 * Note that current implementation of does not handle support QMake 2.0 in full range so provided information might be incomplete.
 *
 * This class is not meant to be implemented.
 */
public interface IQMakeInfo {

	/**
	 * Returns whether this information is valid. If invalid, then returned value of the other method is unspecified.
	 *
	 * @return true if this information is valid
	 */
	boolean isValid();

	/**
	 * Returns a map of key-value pairs provided by "qmake -query" command.
	 *
	 * @return the map
	 */
	Map<String, String> getQMakeQueryMap();

	/**
	 * Returns a Qt version as provided by "qmake -query" command.
	 *
	 * @return the Qt version
	 */
	IQtVersion getQtVersion();

	/**
	 * Returns a list of QMake files (.pro, .pri, .prf, ...) that are involved in resolving this information as retrieved
	 * via "qmake -E file.pro" command.
	 *
	 * @return the list of involved QMake files
	 */
	List<String> getInvolvedQMakeFiles();

	/**
	 * Returns a list of Qt Import paths. Represents QT_IMPORT_PATH that is used by QDeclarativeEngine (aka QtQuick1) runtime to load QML modules.
	 *
	 * @return the list of Qt Import paths
	 */
	List<String> getQtImportPath();

	/**
	 * Returns a list of Qt Qml paths that is used by QQmlEngine (aka QtQuick2) runtime to load QML modules.
	 *
	 * @return the list of Qt Qml paths
	 */
	List<String> getQtQmlPath();

	/**
	 * Returns a list of Qt Documentation paths.
	 *
	 * @return the list of Qt Documentation paths
	 */
	List<String> getQtDocPath();

	/**
	 * Returns a list of include paths that are used for compilation of a related project.
	 *
	 * @return the list of include paths
	 */
	List<String> getIncludePath();

	/**
	 * Returns a list of defines that are used for compilation of a related project i.e. specified via DEFINES QMake variable.
	 *
	 * @return the list of defines.
	 */
	List<String> getDefines();

	/**
	 * Returns a list of source file paths i.e. specified via SOURCES QMake variable.
	 *
	 * @return the list of source file paths
	 */
	List<String> getSourceFiles();

	/**
	 * Returns a list of header file paths i.e. specified via HEADERS QMake variable.
	 *
	 * @return the list of header file paths
	 */
	List<String> getHeaderFiles();

	/**
	 * Returns a list of resource file paths i.e. specified via RESOURCES QMake variable.
	 *
	 * @return the list of other file paths
	 */
	List<String> getResourceFiles();

	/**
	 * Returns a list of other file paths i.e. specified via FORMS QMake variable.
	 *
	 * @return the list of other file paths
	 */
	List<String> getFormFiles();

	/**
	 * Returns a list of other file paths i.e. specified via OTHER_FILES QMake variable.
	 *
	 * @return the list of other file paths
	 */
	List<String> getOtherFiles();

}
