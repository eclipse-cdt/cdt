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

import java.io.IOException;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Alena
 * 
 */
public class CheckerTestCase extends CodanTestCase {
	private IMarker[] markers;


	/**
	 * @param i
	 *            - line
	 */
	public void checkErrorLine(int i) {
		assertTrue(markers != null);
		assertTrue(markers.length > 0);
		boolean found = false;
		for (int j = 0; j < markers.length; j++) {
			IMarker m = markers[j];
			Object line = null;
			Object pos;
			try {
				line = m.getAttribute(IMarker.LINE_NUMBER);
				if (line == null || line.equals(-1)) {
					pos = m.getAttribute(IMarker.CHAR_START);
					line = new Integer(pos2line(((Integer) pos).intValue()));
				}
			} catch (CoreException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			if (line.equals(i)) {
				found = true;
			}
		}
		assertTrue("Error on line " + i + " not found ", found);
	}

	public void checkNoErrors() {
		assertTrue("Found errors but should not", markers == null
				|| markers.length == 0);
	}

	/**
	 * 
	 */
	public void runOnFile() {
		try {
			loadFiles();
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		CodanRuntime.getInstance().getBuilder().processResource(
				cproject.getProject(), NPM);
		try {
			markers = cproject.getProject()
					.findMarkers(
							IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE,
							true, 1);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}
}
