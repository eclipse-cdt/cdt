/*******************************************************************************
 * Copyright (c) 2011, 2012 Tomasz Wesolowski and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.CatchByReference;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * @author Tomasz Wesolowski
 */
@SuppressWarnings("restriction")
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

	private boolean setPlaceConstRight(boolean placeRight) {
		IEclipsePreferences node = new ProjectScope(cproject.getProject()).getNode(CCorePlugin.PLUGIN_ID);
		boolean before = node.getBoolean(CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE,
				CCorePreferenceConstants.DEFAULT_PLACE_CONST_RIGHT_OF_TYPE);
		node.putBoolean(CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, placeRight);
		CCorePreferenceConstants.getPreference(CCorePreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, cproject,
				CCorePreferenceConstants.DEFAULT_PLACE_CONST_RIGHT_OF_TYPE);
		return before;
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
	public void testCatchByReference() throws Exception {
		setQuickFix(new CatchByReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (C &exception)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C) {
	//    }
	// }
	public void testCatchByReferenceNoDeclName() throws Exception {
		setQuickFix(new CatchByReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (C&)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C exception) {
	//    }
	// }
	public void testCatchByConstReference() throws Exception {
		setQuickFix(new CatchByConstReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (const C &exception)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C exception) {
	//    }
	// }
	public void testCatchByConstReferenceHonorsConstPlacementSettings_532120() throws Exception {
		setQuickFix(new CatchByConstReferenceQuickFix());
		loadcode(getAboveComment());

		boolean before = setPlaceConstRight(true);
		String result = runQuickFixOneFile();
		setPlaceConstRight(before);

		assertContainedIn("catch (C const &exception)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C) {
	//    }
	// }
	public void testCatchByConstReferenceNoDeclName() throws Exception {
		setQuickFix(new CatchByConstReferenceQuickFix());
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("catch (const C&)", result); //$NON-NLS-1$
	}

	// struct C {
	// };
	// void foo() {
	//    try {
	//    } catch (C) {
	//    }
	// }
	public void testCatchByConstReferenceNoDeclNameHonorsConstPlacementSettings_532120() throws Exception {
		setQuickFix(new CatchByConstReferenceQuickFix());
		loadcode(getAboveComment());

		boolean before = setPlaceConstRight(true);
		String result = runQuickFixOneFile();
		setPlaceConstRight(before);

		assertContainedIn("catch (C const&)", result); //$NON-NLS-1$
	}

}
