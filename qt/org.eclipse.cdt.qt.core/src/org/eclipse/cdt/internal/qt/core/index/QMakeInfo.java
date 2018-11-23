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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

/**
 * Holder for QMake information.
 */
public final class QMakeInfo implements IQMakeInfo {

	// reg. exp. for parsing output of "qmake -query" command
	public static final Pattern PATTERN_QUERY_LINE = Pattern.compile("^(\\w+):(.*)$");
	// reg. exp. for parsing output of "qmake -E file.pro" command (for QMake 3.0 only)
	public static final Pattern PATTERN_EVAL_LINE = Pattern.compile("^([a-zA-Z0-9_\\.]+)\\s*=\\s*(.*)$");

	/**
	 * Instance that is used to present an invalid IQMakeInfo.
	 */
	public static final IQMakeInfo INVALID = new QMakeInfo(false, Collections.<String, String>emptyMap(),
			Collections.<String, String>emptyMap());

	private final boolean valid;
	private final Map<String, String> qmakeQueryMap;
	private final IQtVersion qtVersion;
	private final List<String> involvedQMakeFiles;
	private final List<String> qtImportPath;
	private final List<String> qtQmlPath;
	private final List<String> qtDocPath;
	private final List<String> includePath;
	private final List<String> defines;
	private final List<String> sourceFiles;
	private final List<String> headerFiles;
	private final List<String> resourceFiles;
	private final List<String> formFiles;
	private final List<String> otherFiles;

	public QMakeInfo(boolean valid, Map<String, String> queryMap, Map<String, String> proMap) {
		this.valid = valid;
		this.qmakeQueryMap = Collections.unmodifiableMap(queryMap);

		this.qtVersion = QMakeVersion.create(queryMap.get(QMakeParser.KEY_QT_VERSION));
		List<String> tmpQtImportPaths = new ArrayList<>(
				QMakeParser.singleValue(queryMap, QMakeParser.KEY_QT_INSTALL_IMPORTS));
		List<String> tmpQtQmlPaths = new ArrayList<>(QMakeParser.singleValue(queryMap, QMakeParser.KEY_QT_INSTALL_QML));
		this.qtDocPath = QMakeParser.singleValue(queryMap, QMakeParser.KEY_QT_INSTALL_DOCS);

		this.involvedQMakeFiles = QMakeParser.qmake3DecodeValueList(proMap,
				QMakeParser.KEY_QMAKE_INTERNAL_INCLUDED_FILES);
		this.includePath = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_INCLUDEPATH);
		this.defines = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_DEFINES);
		this.sourceFiles = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_SOURCES);
		this.headerFiles = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_HEADERS);
		this.resourceFiles = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_RESOURCES);
		this.formFiles = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_FORMS);
		this.otherFiles = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_OTHER_FILES);

		// combine qtImportPath and qtQmlPath from both qmake runs
		List<String> qmlImportPath = QMakeParser.qmake3DecodeValueList(proMap, QMakeParser.KEY_QML_IMPORT_PATH);
		tmpQtImportPaths.addAll(qmlImportPath);
		tmpQtQmlPaths.addAll(qmlImportPath);
		this.qtImportPath = Collections.unmodifiableList(tmpQtImportPaths);
		this.qtQmlPath = Collections.unmodifiableList(tmpQtQmlPaths);
	}

	public static IQMakeInfo create(String proPath, String qmakePath, String[] extraEnv) {
		if (proPath == null || qmakePath == null) {
			return INVALID;
		}

		// run "qmake -query"
		Map<String, String> qmake1 = exec(PATTERN_QUERY_LINE, extraEnv, qmakePath, "-query");
		if (qmake1 == null) {
			return INVALID;
		}

		// check the qmake version
		QMakeVersion version = QMakeVersion.create(qmake1.get(QMakeParser.KEY_QMAKE_VERSION));

		// TODO - no support for pre-3.0
		// for QMake version 3.0 or newer, run "qmake -E file.pro"
		Map<String, String> qmake2 = version != null && version.getMajor() >= 3
				? exec(PATTERN_EVAL_LINE, extraEnv, qmakePath, "-E", proPath)
				: Collections.<String, String>emptyMap();
		return new QMakeInfo(true, qmake1, qmake2);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public Map<String, String> getQMakeQueryMap() {
		return qmakeQueryMap;
	}

	@Override
	public IQtVersion getQtVersion() {
		return qtVersion;
	}

	@Override
	public List<String> getInvolvedQMakeFiles() {
		return involvedQMakeFiles;
	}

	@Override
	public List<String> getQtImportPath() {
		return qtImportPath;
	}

	@Override
	public List<String> getQtQmlPath() {
		return qtQmlPath;
	}

	@Override
	public List<String> getQtDocPath() {
		return qtDocPath;
	}

	@Override
	public List<String> getIncludePath() {
		return includePath;
	}

	@Override
	public List<String> getDefines() {
		return defines;
	}

	@Override
	public List<String> getSourceFiles() {
		return sourceFiles;
	}

	@Override
	public List<String> getHeaderFiles() {
		return headerFiles;
	}

	@Override
	public List<String> getResourceFiles() {
		return resourceFiles;
	}

	@Override
	public List<String> getFormFiles() {
		return formFiles;
	}

	@Override
	public List<String> getOtherFiles() {
		return otherFiles;
	}

	/**
	 * Executes a command and parses its output into a map.
	 *
	 * @param regex the reg. exp. used for parsing the output
	 * @param extraEnv the extra environment for command
	 * @param cmd the command line
	 * @return the map of resolved key-value pairs
	 */
	private static Map<String, String> exec(Pattern regex, String[] extraEnv, String... command) {
		if (command.length < 1 || !new File(command[0]).exists()) {
			Activator.log("qmake: cannot run command: " + (command.length > 0 ? command[0] : ""));
			return null;
		}
		BufferedReader reader = null;
		Process process = null;
		try {
			if (extraEnv != null && extraEnv.length > 0) {
				process = ProcessFactory.getFactory().exec(command, extraEnv);
			} else {
				process = ProcessFactory.getFactory().exec(command);
			}
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			return QMakeParser.parse(regex, reader);
		} catch (IOException e) {
			Activator.log(e);
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					/* ignore */
				}
			if (process != null) {
				process.destroy();
			}
		}
	}

}
