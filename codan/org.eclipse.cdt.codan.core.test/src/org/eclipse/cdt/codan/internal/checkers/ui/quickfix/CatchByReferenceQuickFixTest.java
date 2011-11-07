/*******************************************************************************
 * Copyright (c) 2011 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.CatchByReference;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

/**
 * @author Tomasz Wesolowski
 */
public class CatchByReferenceQuickFixTest extends QuickFixTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CatchByReference.ER_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return null; // quick fix to be chosen per test
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C exception) {
	//    }
	// }
	@SuppressWarnings("restriction")
	public void testCatchByReference() throws Exception {
		setQuickFix(new CatchByReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (C & exception)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C) {
	//    }
	// }
	@SuppressWarnings("restriction")
	public void testCatchByReferenceNoDeclName() throws Exception {
		setQuickFix(new CatchByReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (C &)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C exception) {
	//    }
	// }
	@SuppressWarnings("restriction")
	public void testCatchByConstReference() throws Exception {
		setQuickFix(new CatchByConstReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (const C & exception)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C) {
	//    }
	// }
	@SuppressWarnings("restriction")
	public void testCatchByConstReferenceNoDeclName() throws Exception {
		setQuickFix(new CatchByConstReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (const C &)", result); //$NON-NLS-1$
	}
}
