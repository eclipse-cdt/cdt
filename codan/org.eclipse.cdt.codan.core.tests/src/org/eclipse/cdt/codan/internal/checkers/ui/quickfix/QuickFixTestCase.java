/*******************************************************************************
 * Copyright (c) 2009, 2015 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.PreferenceConstants;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.core.tests.TestUtils;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract base class for Quck Fix tests.
 */
@SuppressWarnings("restriction")
public abstract class QuickFixTestCase extends CheckerTestCase {
	AbstractCodanCMarkerResolution quickFix;
	Display display;
	Map<IMarker, Boolean> isApplicableMap;

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
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage activePage = window.getActivePage();
			IWorkbenchPart activePart = activePage.getActivePart();
			if (activePart.getTitle().equals("Welcome")) { //$NON-NLS-1$
				//activePage.close();
				activePart.dispose();
			}
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		quickFix = createQuickFix();
		display = PlatformUI.getWorkbench().getDisplay();
		isApplicableMap = new HashMap<>();
		closeWelcome();
		IPreferenceStore store = CodanUIActivator.getDefault().getPreferenceStore(cproject.getProject());
		// turn off editor reconciler
		store.setValue(PreferenceConstants.P_RUN_IN_EDITOR, false);
	}

	@Override
	public void tearDown() throws Exception {
		Display.getDefault().syncExec(() -> {
			IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
			for (IWorkbenchPage page : pages) {
				page.closeAllEditors(false);
				dispatch(0);
			}
		});

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
	 * Calculate for which markers in the current test the QuickFix under test is
	 * applicable.
	 *
	 * @return A map reflecting for which markers the QuickFix is applicable.
	 */
	public Map<IMarker, Boolean> calculateQuickFixApplicability() {
		runCodan();
		Display.getDefault().syncExec(() -> {
			for (IMarker marker : markers) {
				isApplicableMap.put(marker, quickFix.isApplicable(marker));
			}
		});
		return isApplicableMap;
	}

	public String runQuickFixOneFile() throws IOException, CoreException {
		// need to load before running codan because otherwise marker is lost when doing quick fix 8[]
		runCodan();
		doRunQuickFix();
		String result = TestUtils.loadFile(currentIFile.getContents());
		return result;
	}

	public void doRunQuickFix() {
		Display.getDefault().syncExec(() -> {
			for (int i = 0; i < markers.length; i++) {
				IMarker marker = markers[i];
				isApplicableMap.put(marker, quickFix.isApplicable(marker));
				if (quickFix.isApplicable(marker)) {
					quickFix.run(marker);
					dispatch(0);
				}
			}
			PlatformUI.getWorkbench().saveAllEditors(false);
		});

	}

	/**
	 * @param result
	 * @param expected
	 */
	public void assertContainedIn(String expected, String result) {
		assertTrue("Text <" + expected + "> not found in <" + result + ">", result.contains(expected)); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Assert whether or not the QuickFix under test was applicable for all markers
	 * in the test code.
	 */
	public void assertIsApplicableForAllMarkers(boolean expected) {
		for (IMarker marker : markers) {
			assertEquals(expected, (boolean) isApplicableMap.get(marker));
		}
	}

	/**
	 * Changes the quick fix to be used
	 * @param quickFix
	 */
	public void setQuickFix(AbstractCodanCMarkerResolution quickFix) {
		this.quickFix = quickFix;
	}
}
