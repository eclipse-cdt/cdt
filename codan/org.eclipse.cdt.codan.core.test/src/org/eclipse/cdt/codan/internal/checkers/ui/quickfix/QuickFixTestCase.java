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

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.core.test.TestUtils;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * TODO: add description
 */
public abstract class QuickFixTestCase extends CheckerTestCase {
	AbstractCodanCMarkerResolution quickFix;
	Display display;

	/**
	 * Dispatch ui events for at least msec - milliseconds
	 * 
	 * @param msec -
	 *        milliseconds delay
	 * @param display -
	 *        display that dispatches events
	 */
	public void dispatch(int msec) {
		long cur = System.currentTimeMillis();
		long pass = 0;
		while (pass < msec) {
			if (!display.readAndDispatch())
				display.sleep();
			pass = System.currentTimeMillis() - cur;
		}
	}

	public static void closeWelcome() {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			IWorkbenchPage activePage = window.getActivePage();
			IWorkbenchPart activePart = activePage.getActivePart();
			if (activePart.getTitle().equals("Welcome")) {
				//activePage.close();
				activePart.dispose();
			}
		} catch (Exception e) {
			// ignore
		}
	}

	@SuppressWarnings("restriction")
	@Override
	public void setUp() throws Exception {
		super.setUp();
		quickFix = createQuickFix();
		display = PlatformUI.getWorkbench().getDisplay();
		closeWelcome();
		IPreferenceStore store = CodanUIActivator.getDefault()
				.getPreferenceStore(cproject.getProject());
		// turn off editor reconsiler
		store.setValue(PreferenceConstants.P_RUN_IN_EDITOR, false);
	}

	@Override
	public void tearDown() throws CoreException {
		IWorkbenchPage[] pages = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPages();
		for (IWorkbenchPage page : pages) {
			page.closeAllEditors(false);
			dispatch(200);
		}
		super.tearDown();
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
			runCodan();
			doRunQuickFix();
			dispatch(500);
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
			dispatch(200);
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
