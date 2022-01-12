/*******************************************************************************
 * Copyright (c) 2011 Stefan Ghiaus.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Ghiaus - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.wizards.classwizard;

import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.cdt.ui.wizards.NewClassCreationWizardPage;
import org.eclipse.core.runtime.IStatus;

public class ClassWizardNameTest extends BaseUITestCase {

	private class ClassCreationPage extends NewClassCreationWizardPage {
		private IStatus classNameStatus;

		public IStatus getClassNameStatus() {
			return classNameStatus;
		}

		@Override
		protected IStatus classNameChanged() {
			classNameStatus = super.classNameChanged();
			return classNameStatus;
		}
	}

	private boolean isErrorStatus(IStatus status) {
		return (status.getSeverity() == IStatus.ERROR) ? true : false;
	}

	public void testEmptyName() {
		ClassCreationPage page = new ClassCreationPage();
		page.setClassName("", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
		page.setClassName("nonempty", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
	}

	public void testFirstCharacter() {
		ClassCreationPage page = new ClassCreationPage();
		page.setClassName("1name", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
		page.setClassName("@name", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
		page.setClassName("name", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
	}

	public void testAllowedCharacters() {
		ClassCreationPage page = new ClassCreationPage();
		page.setClassName("name1", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
		page.setClassName("na1me", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
		page.setClassName("name#", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
		page.setClassName("na#me", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
	}

	public void testStandardKeyword() {
		ClassCreationPage page = new ClassCreationPage();
		page.setClassName("new", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
		page.setClassName("New", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
		page.setClassName("neW", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
		page.setClassName("class", true);
		assertTrue(isErrorStatus(page.getClassNameStatus()));
		page.setClassName("claSs", true);
		assertTrue(!isErrorStatus(page.getClassNameStatus()));
	}
}
