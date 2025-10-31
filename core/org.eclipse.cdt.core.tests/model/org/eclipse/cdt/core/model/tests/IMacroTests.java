/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 6, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * IMacroTest - Class for testing IMacro
 *
 * @author bnicolle
 *
 */
public class IMacroTests extends IntegratedCModelTest {
	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileResource() {
		return "IMacroTest.h";
	}

	@Test
	public void testGetElementName() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayElements = tu.getChildrenOfType(ICElement.C_MACRO);

		String expectedList[] = new String[] { "SINGLETON", "NUMBER", "PRINT" };
		assertEquals(expectedList.length, arrayElements.size());
		for (int i = 0; i < expectedList.length; i++) {
			IMacro iMacro = (IMacro) arrayElements.get(i);
			assertEquals(expectedList[i], iMacro.getElementName());
		}
	}

	@Test
	@Disabled("TODO Bug# 38740")
	public void testGetIdentifierList() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayElements = tu.getChildrenOfType(ICElement.C_MACRO);

		String expectedList[] = new String[] { "", "", "string,msg" };
		assertEquals(expectedList.length, arrayElements.size());
		for (int i = 0; i < expectedList.length; i++) {
			IMacro iMacro = (IMacro) arrayElements.get(i);
			assertEquals(expectedList[i], iMacro.getIdentifierList());
		}
	}

	@Test
	@Disabled("TODO Bug# 38740")
	public void testGetTokenSequence() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayElements = tu.getChildrenOfType(ICElement.C_MACRO);

		String expectedList[] = new String[] { "", "1", "printf(string, msg)" };
		assertEquals(expectedList.length, arrayElements.size());
		for (int i = 0; i < expectedList.length; i++) {
			IMacro iMacro = (IMacro) arrayElements.get(i);
			assertEquals(expectedList[i], iMacro.getTokenSequence());
		}
	}
}
