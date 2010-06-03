/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.IAsmLabel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;

/**
 * Tests for the default assembly model builder.
 *
 * @since 5.0
 */
public class AsmModelBuilderTest extends BaseTestCase {

	public static Test suite() {
		return suite(AsmModelBuilderTest.class, "_");
	}

	private ICProject fCProject;
	private ITranslationUnit fTU;		
		
	public AsmModelBuilderTest(String name) {
		super(name);
	}
		
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCProject(getName(), null, IPDOMManager.ID_FAST_INDEXER);
		assertNotNull(fCProject);
		CProjectHelper.importSourcesFromPlugin(fCProject, CTestPlugin.getDefault().getBundle(), "/resources/asmTests");
		fTU= (ITranslationUnit) CProjectHelper.findElement(fCProject, "AsmTest.S");
		assertNotNull(fTU);
	}

	@Override
	protected void tearDown() throws Exception {
		  CProjectHelper.delete(fCProject);
		  super.tearDown();
	}	
	
	public void testAsmModelElements() throws Exception {
		ICElement[] children= fTU.getChildren();
		assertEquals(8, children.length);
		
		int idx= 0;
		assertEquals(ICElement.C_INCLUDE, children[idx].getElementType());
		assertTrue(((IInclude)children[idx]).isStandard());
		assertEquals("include1.h", children[idx++].getElementName());
		assertEquals(ICElement.C_INCLUDE, children[idx].getElementType());
		assertFalse(((IInclude)children[idx]).isStandard());
		assertEquals("include2.h", children[idx++].getElementName());
		
		
		assertEquals(ICElement.ASM_LABEL, children[idx].getElementType());
		assertEquals("nonGlobalLabel", children[idx].getElementName());
		assertFalse(((IAsmLabel)children[idx]).isGlobal());
		assertEquals(0, ((IParent)children[idx++]).getChildren().length);
		
		assertEquals(ICElement.ASM_LABEL, children[idx].getElementType());
		assertEquals("globalLabel1", children[idx].getElementName());
		assertTrue(((IAsmLabel)children[idx]).isGlobal());
		assertEquals(2, ((IParent)children[idx++]).getChildren().length);
		
		assertEquals(ICElement.C_MACRO, children[idx].getElementType());
		assertEquals("MACRO", children[idx++].getElementName());
		
		assertEquals(ICElement.ASM_LABEL, children[idx].getElementType());
		assertEquals("globalLabel2", children[idx].getElementName());
		assertTrue(((IAsmLabel)children[idx]).isGlobal());
		assertEquals(1, ((IParent)children[idx++]).getChildren().length);
		
		assertEquals(ICElement.ASM_LABEL, children[idx].getElementType());
		assertEquals("globalLabel3", children[idx].getElementName());
		assertTrue(((IAsmLabel)children[idx]).isGlobal());
		assertEquals(1, ((IParent)children[idx++]).getChildren().length);
		
		assertEquals(ICElement.ASM_LABEL, children[idx].getElementType());
		assertEquals("alloca", children[idx].getElementName());
		assertTrue(((IAsmLabel)children[idx]).isGlobal());
		assertEquals(0, ((IParent)children[idx++]).getChildren().length);
	}

	public void testAsmLabelRanges() throws Exception {
		String source= fTU.getBuffer().getContents();
		ICElement[] labels= fTU.getChildrenOfType(ICElement.ASM_LABEL).toArray(new ICElement[0]);
		for (ICElement label2 : labels) {
			String name= label2.getElementName();
			ISourceReference label= (ISourceReference)label2;
			ISourceRange range= label.getSourceRange();
			assertEquals(source.substring(range.getIdStartPos(), range.getIdStartPos() + range.getIdLength()), name);
			int endOfLabel= source.indexOf("/* end */", range.getIdStartPos());
			assertEquals(range.getIdStartPos(), range.getStartPos());
			assertEquals(endOfLabel, range.getStartPos() + range.getLength());
		}
	}
	
}
