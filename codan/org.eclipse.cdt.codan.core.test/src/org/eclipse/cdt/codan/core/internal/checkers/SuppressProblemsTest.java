/*******************************************************************************
 * Copyright (c) 2013 Andreas Muelder and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Muelder (itemis)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.core.FilterRegistry;
import org.eclipse.cdt.codan.internal.core.model.CommentBasedProblemFilter;

public class SuppressProblemsTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		CodanCorePlugin.getDefault().getStorePreferences().put(FilterRegistry.FILTER_PREFERENCE, CommentBasedProblemFilter.ID);
		super.setUp();
	}

	// //codan:off
	// void main() {
	// int x = 0;
	// x = x;
	// }
	public void testDisableAllWarningsForFile() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 0;
	// //codan:off
	// x = x;
	// //codan:on
	// }
	public void testDisableAllWarningsForNextLine() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 0;
	// //codan:off
	// x = x;
	// x = x;
	// //codan:on
	// }
	public void testDisableAllWarningsForNextTwoLines() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 0;
	// //codan:off #AssignmentToItselfProblem
	// x = x;
	// x = x;
	// //codan:on #AssignmentToItselfProblem
	// }
	public void testDisableSpecificWarningForNextLine() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	// int x = 0;
	// //codan:off #Other
	// x = x;
	// //codan:on #Other
	// }
	public void testDisableOtherWarningForNextLine() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// //codan:off
	// void main() {
	// int x = 0;
	// //codan:on #AssignmentToItselfProblem
	// x = x;
	// }
	public void testDisableSpecificNextLineWrappedInCodanOff() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}
}
