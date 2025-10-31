/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for C model inactive code parsing.
 */
public class CModelBuilderInactiveCodeTest extends BaseTestCase5 {

	private ICProject fCProject;
	private ITranslationUnit fTU;

	@BeforeEach
	protected void createProject() throws Exception {
		fCProject = CProjectHelper.createCProject(getName(), null, IPDOMManager.ID_FAST_INDEXER);
		assertNotNull(fCProject);
		CProjectHelper.importSourcesFromPlugin(fCProject, CTestPlugin.getDefault().getBundle(), "/resources/cmodel");
		fTU = (ITranslationUnit) CProjectHelper.findElement(fCProject, "CModelBuilderInactiveCodeTest.cpp");
		assertNotNull(fTU);
	}

	@AfterEach
	protected void deleteProject() throws Exception {
		CProjectHelper.delete(fCProject);
	}

	@Test
	public void testPreprocessorNodes() throws Exception {
		ISourceReference e = (ISourceReference) fTU.getElement("include");
		assertTrue(e instanceof IInclude);
		assertFalse(e.isActive());

		e = (ISourceReference) fTU.getElement("MACRO1");
		assertTrue(e instanceof IMacro);
		assertFalse(e.isActive());

		e = (ISourceReference) fTU.getElement("MACRO2");
		assertTrue(e instanceof IMacro);
		assertFalse(e.isActive());
	}
}
