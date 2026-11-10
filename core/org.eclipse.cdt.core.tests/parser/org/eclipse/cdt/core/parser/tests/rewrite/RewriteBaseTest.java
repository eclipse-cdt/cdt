/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.junit.jupiter.api.AfterEach;

/**
 * @author Guido Zgraggen IFS
 */
public abstract class RewriteBaseTest extends BaseTestFramework implements ILogListener {
	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();

	protected String fileWithSelection;
	protected TextSelection selection;

	protected StringBuilder getCodeFromFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		br.close();
		return code;
	}

	@AfterEach
	protected void closeAllFiles() throws Exception {
		System.gc();
		fileManager.closeAllFiles();
	}

	@Override
	public void logging(IStatus status, String plugin) {
		Throwable ex = status.getException();
		StringBuilder stackTrace = new StringBuilder();
		if (ex != null) {
			stackTrace.append('\n');
			for (StackTraceElement ste : ex.getStackTrace()) {
				stackTrace.append(ste.toString());
			}
		}
		fail("Log-Message: " + status.getMessage() + stackTrace.toString());
	}

	public void setFileWithSelection(String fileWithSelection) {
		this.fileWithSelection = fileWithSelection;
	}

	public void setSelection(TextSelection selection) {
		this.selection = selection;
	}
}
