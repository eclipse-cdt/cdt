/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.core.qmltypes.QMLModelBuilder;
import org.eclipse.cdt.internal.qt.core.qmltypes.QMLModuleInfo;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryInfo;
import org.eclipse.cdt.qt.core.qmljs.IQmlASTNode;

public class QMLModuleResolver {
	private final QMLAnalyzer analyzer;
	private final IQtInstallManager manager;
	private final QMLModelBuilder builder;

	public QMLModuleResolver(QMLAnalyzer analyzer) {
		this.analyzer = analyzer;
		this.manager = Activator.getService(IQtInstallManager.class);
		this.builder = new QMLModelBuilder();
	}

	// TODO: determine exactly how to give this to Tern. For now we'll just return the reference to the QMLModuleInfo
	// that we found
	public QMLModuleInfo resolveModule(String module) throws NoSuchMethodException, ScriptException {
		QMLModuleInfo info = builder.getModule(module);
		if (info == null) {
			Path path = getModulePath(module);
			if (path != null) {
				File qmldir = path.resolve("qmldir").normalize().toFile(); //$NON-NLS-1$
				try {
					String types = getQmlTypesFile(qmldir);
					File qmlTypes = path.resolve(types).toFile();
					String typeContents = fileToString(qmlTypes);
					IQmlASTNode ast = analyzer.parseString(typeContents, "qmltypes", false, false); //$NON-NLS-1$
					info = builder.addModule(module, ast);
				} catch (IOException e) {
					Activator.log(e);
				}
			}
		}
		return info;
	}

	private String fileToString(File file) throws IOException {
		try (InputStream stream = new FileInputStream(file)) {
			StringBuilder sb = new StringBuilder();
			int read = -1;
			while ((read = stream.read()) != -1) {
				sb.append((char) read);
			}
			return sb.toString();
		}
	}

	private String getQmlTypesFile(File qmldir) throws IOException {
		try (InputStream stream = new FileInputStream(qmldir)) {
			QMLDirectoryInfo info = new QMLDirectoryInfo(stream);
			return info.getTypesFileName();
		}
	}

	private Path getModulePath(String module) {
		if (module != null) {
			for (IQtInstall install : manager.getInstalls()) {
				Path qmlPath = install.getQmlPath();
				Path modPath = null;
				if (module.equals("QtQuick")) { //$NON-NLS-1$
					modPath = qmlPath.resolve("QtQuick.2").normalize(); //$NON-NLS-1$
				} else {
					modPath = qmlPath;
					for (String part : module.split("\\.")) { //$NON-NLS-1$
						modPath = modPath.resolve(part).normalize();
					}
				}
				if (modPath.toFile().exists()) {
					return modPath;
				}
			}
		}
		return null;
	}
}
