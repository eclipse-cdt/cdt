/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.test;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Alena
 * 
 */
public class CheckerTestCase extends CodanTestCase {
	private IMarker[] markers;

	public void checkErrorLine(int i) {
		checkErrorLine(currentFile, i);
	}

	/**
	 * @param expectedLine
	 *            - line
	 */
	public void checkErrorLine(File file, int expectedLine) {
		assertTrue(markers != null);
		assertTrue("No problems found but should", markers.length > 0); //$NON-NLS-1$
		boolean found = false;
		Integer line = null;
		String mfile = null;
		for (int j = 0; j < markers.length; j++) {
			IMarker m = markers[j];
			Object pos;
			try {
				line = (Integer) m.getAttribute(IMarker.LINE_NUMBER);
				if (line == null || line.equals(-1)) {
					pos = m.getAttribute(IMarker.CHAR_START);
					line = new Integer(pos2line(((Integer) pos).intValue()));
				}
			} catch (CoreException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			mfile = m.getResource().getName();
			if (line.equals(expectedLine)) {
				found = true;
				if (file != null && !file.getName().equals(mfile))
					found = false;
				else
					break;
			}
		}
		assertEquals(Integer.valueOf(expectedLine), line);
		if (file != null)
			assertEquals(file.getName(), mfile);
		assertTrue(found);
	}

	public void checkNoErrors() {
		assertTrue("Found errors but should not", markers == null
				|| markers.length == 0);
	}

	/**
	 * 
	 */
	public void runOnProject() {
		try {
			indexFiles();
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		runCodan();
	}

	public void loadCodeAndRun(String code) {
		loadcode(code);
		runCodan();
	}

	public void loadCodeAndRunCpp(String code) {
		loadcode(code, true);
		runCodan();
	}

	/**
	 * 
	 */
	protected void runCodan() {
		CodanRuntime
				.getInstance()
				.getBuilder()
				.processResource(cproject.getProject(),
						new NullProgressMonitor());
		try {
			markers = cproject.getProject()
					.findMarkers(
							IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE,
							true, 1);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * @param problemId
	 * @param paramId
	 * @return
	 */
	protected IProblemPreference getPreference(String problemId,
			String paramId) {
		IProblem problem = CodanRuntime.getInstance().getChechersRegistry()
				.getWorkspaceProfile().findProblem(problemId);
		IProblemPreference pref = ((MapProblemPreference) problem
				.getPreference()).getChildDescriptor(paramId);
		return pref;
	}
}
