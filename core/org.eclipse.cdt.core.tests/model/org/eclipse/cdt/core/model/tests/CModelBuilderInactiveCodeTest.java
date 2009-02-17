/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
 * Tests for C model inactive code parsing.
 */
public class CModelBuilderInactiveCodeTest extends BaseTestCase {

	public static Test suite() {
		return suite(CModelBuilderInactiveCodeTest.class, "_");
	}

	private ICProject fCProject;
	private ITranslationUnit fTU;		
		
	public CModelBuilderInactiveCodeTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCProject(getName(), null, IPDOMManager.ID_FAST_INDEXER);
		assertNotNull(fCProject);
		CProjectHelper.importSourcesFromPlugin(fCProject, CTestPlugin.getDefault().getBundle(), "/resources/cmodel");
		fTU= (ITranslationUnit) CProjectHelper.findElement(fCProject, "CModelBuilderInactiveCodeTest.cpp");
		assertNotNull(fTU);
	}

	@Override
	protected void tearDown() throws Exception {
		  CProjectHelper.delete(fCProject);
		  super.tearDown();
	}	
	
	public void testPreprocessorNodes() throws Exception {
		ISourceReference e= (ISourceReference) fTU.getElement("include");
		assertTrue(e instanceof IInclude);
		assertFalse(e.isActive());

		e= (ISourceReference) fTU.getElement("MACRO1");
		assertTrue(e instanceof IMacro);
		assertFalse(e.isActive());

		e= (ISourceReference) fTU.getElement("MACRO2");
		assertTrue(e instanceof IMacro);
		assertFalse(e.isActive());
	}
}
