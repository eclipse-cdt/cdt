/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.io.IOException;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.core.test.TestUtils;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * TODO: add description
 */
public abstract class QuickFixTestCase extends CheckerTestCase {
	AbstractCodanCMarkerResolution quickFix;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		quickFix = createQuickFix();
	}

	/**
	 * @return
	 */
	protected abstract AbstractCodanCMarkerResolution createQuickFix();

	/**
	 * @param code
	 * @param string
	 * @return
	 */
	protected ISelection textSelection(String code, String string) {
		return new TextSelection(code.indexOf(string), string.length());
	}

	/**
	 * @return
	 * @throws CModelException
	 * @throws PartInitException
	 * @throws IOException
	 * @throws CoreException
	 */
	public String runQuickFixOneFile() {
		// need to load before running codan because otherwise marker is lost when doing quick fix 8[]
		try {
			EditorUtility.openInEditor(currentIFile);
			runCodan();
			doRunQuickFix();
			String result = TestUtils.loadFile(currentIFile.getContents());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
	}

	/**
	 * 
	 */
	public void doRunQuickFix() {
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			quickFix.run(marker);
		}
		PlatformUI.getWorkbench().saveAllEditors(false);
	}

	/**
	 * @param result
	 * @param expected
	 */
	public void assertContainedIn(String expected, String result) {
		assertTrue(
				"Text <" + expected + "> not found in <" + result + ">", result.contains(expected)); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}
