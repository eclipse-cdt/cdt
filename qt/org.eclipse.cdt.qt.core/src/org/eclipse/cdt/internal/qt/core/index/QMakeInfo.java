/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.cdt.qt.core.index.IQtVersion;
import org.eclipse.cdt.qt.core.index.IQMakeInfo;
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
	public static final IQMakeInfo INVALID = new QMakeInfo(
			false, null, Collections.<String>emptyList(), Collections.<String>emptyList(),
			Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(),
			Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList());

	private final boolean valid;
	private final IQtVersion qtVersion;
	private final List<String> involvedQMakeFiles;
	private final List<String> qtImportPath;
	private final List<String> qtQmlPath;
	private final List<String> includePath;
	private final List<String> defines;
	private final List<String> sourceFiles;
	private final List<String> headerFiles;
	private final List<String> otherFiles;

	private QMakeInfo(boolean valid, IQtVersion qtVersion, List<String> involvedQMakeFiles, List<String> qtImportPath, List<String> qtQmlPath, List<String> includePath, List<String> defines, List<String> sourceFiles, List<String> headerFiles, List<String> otherFiles) {
		this.valid = valid;
		this.qtVersion = qtVersion;
		this.involvedQMakeFiles = Collections.unmodifiableList(involvedQMakeFiles);
		this.qtImportPath = Collections.unmodifiableList(qtImportPath);
		this.qtQmlPath = Collections.unmodifiableList(qtQmlPath);
		this.includePath = Collections.unmodifiableList(includePath);
		this.defines = Collections.unmodifiableList(defines);
		this.sourceFiles = Collections.unmodifiableList(sourceFiles);
		this.headerFiles = Collections.unmodifiableList(headerFiles);
		this.otherFiles = Collections.unmodifiableList(otherFiles);
	}

	public static IQMakeInfo create(File projectFile, File qmake, String[] extraEnv) {
		return create(projectFile.getAbsolutePath(), qmake.getAbsolutePath(), extraEnv);
	}

	public static IQMakeInfo create(String proPath, String qmakePath, String[] extraEnv) {
		if (proPath == null || qmakePath == null) {
			return INVALID;
		}

		// run "qmake -query"
		Map<String, String> qmake1 = exec(PATTERN_QUERY_LINE, extraEnv, qmakePath, "-query");
	    if (qmake1 == null)
			return INVALID;

	    // check the qmake version
		QMakeVersion version = QMakeVersion.create(qmake1.get(QMakeParser.KEY_QMAKE_VERSION));

		// TODO - no support for pre-3.0
		// for QMake version 3.0 or newer, run "qmake -E file.pro"
		Map<String, String> qmake2 = version != null && version.getMajor() >= 3 ? exec(PATTERN_EVAL_LINE, extraEnv, qmakePath, "-E", proPath) : Collections.<String,String>emptyMap();

		IQtVersion qtVersion = QMakeVersion.create(qmake1.get(QMakeParser.KEY_QT_VERSION));
		List<String> qtImportPaths = QMakeParser.singleValue(qmake1, QMakeParser.KEY_QT_INSTALL_IMPORTS);
		List<String> qtQmlPaths = QMakeParser.singleValue(qmake1, QMakeParser.KEY_QT_INSTALL_QML);

		List<String> involvedQMakeFiles = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_QMAKE_INTERNAL_INCLUDED_FILES);
		List<String> includePath = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_INCLUDEPATH);
		List<String> defines = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_DEFINES);
		List<String> sourceFiles = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_SOURCES);
		List<String> headerFiles = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_HEADERS);
		List<String> otherFiles = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_HEADERS);
		List<String> qmlImportPath = QMakeParser.qmake3DecodeValueList(qmake2, QMakeParser.KEY_QML_IMPORT_PATH);

		// combine qtImportPath and qtQmlPath from both qmake runs
		List<String> realQtImportPaths = new ArrayList<String>(qtImportPaths);
		realQtImportPaths.addAll(qmlImportPath);
		List<String> realQtQmlPaths = new ArrayList<String>(qtQmlPaths);
		realQtQmlPaths.addAll(qmlImportPath);

		return new QMakeInfo(true, qtVersion, involvedQMakeFiles, realQtImportPaths, realQtQmlPaths, includePath, defines, sourceFiles, headerFiles, otherFiles);
	}

	@Override
	public boolean isValid() {
		return valid;
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
    @SuppressWarnings("resource")
    private static Map<String, String> exec(Pattern regex, String[] extraEnv, String...command) {
		if (command.length < 1 || ! new File(command[0]).exists()) {
			QtPlugin.log("qmake: cannot run command: " + (command.length > 0 ? command[0] : ""));
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
		} catch(IOException e) {
			QtPlugin.log(e);
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch(IOException e) {
					/* ignore */
				}
			if (process != null) {
				process.destroy();
			}
		}
	}

}
