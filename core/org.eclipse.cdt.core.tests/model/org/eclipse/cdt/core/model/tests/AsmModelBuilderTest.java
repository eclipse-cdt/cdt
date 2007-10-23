/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
		
	protected void setUp() throws Exception {
		super.setUp();
		fCProject= CProjectHelper.createCProject(getName(), null, IPDOMManager.ID_FAST_INDEXER);
		assertNotNull(fCProject);
		CProjectHelper.importSourcesFromPlugin(fCProject, CTestPlugin.getDefault().getBundle(), "/resources/asmTests");
		fTU= (ITranslationUnit) CProjectHelper.findElement(fCProject, "AsmTest.S");
		assertNotNull(fTU);
	}

	protected void tearDown() throws Exception {
		  CProjectHelper.delete(fCProject);
		  super.tearDown();
	}	
	
	public void testAsmModelElements() throws Exception {
		ICElement[] children= fTU.getChildren();
		assertEquals(7, children.length);
		
		assertEquals(ICElement.C_INCLUDE, children[0].getElementType());
		assertEquals("include.h", children[0].getElementName());
		
		assertEquals(ICElement.ASM_LABEL, children[1].getElementType());
		assertEquals("nonGlobalLabel", children[1].getElementName());
		assertFalse(((IAsmLabel)children[1]).isGlobal());
		assertEquals(0, ((IParent)children[1]).getChildren().length);
		
		assertEquals(ICElement.ASM_LABEL, children[2].getElementType());
		assertEquals("globalLabel1", children[2].getElementName());
		assertTrue(((IAsmLabel)children[2]).isGlobal());
		assertEquals(2, ((IParent)children[2]).getChildren().length);
		
		assertEquals(ICElement.C_MACRO, children[3].getElementType());
		assertEquals("MACRO", children[3].getElementName());
		
		assertEquals(ICElement.ASM_LABEL, children[4].getElementType());
		assertEquals("globalLabel2", children[4].getElementName());
		assertTrue(((IAsmLabel)children[4]).isGlobal());
		assertEquals(1, ((IParent)children[4]).getChildren().length);
		
		assertEquals(ICElement.ASM_LABEL, children[5].getElementType());
		assertEquals("globalLabel3", children[5].getElementName());
		assertTrue(((IAsmLabel)children[5]).isGlobal());
		assertEquals(1, ((IParent)children[5]).getChildren().length);
		
		assertEquals(ICElement.ASM_LABEL, children[6].getElementType());
		assertEquals("alloca", children[6].getElementName());
		assertTrue(((IAsmLabel)children[6]).isGlobal());
		assertEquals(0, ((IParent)children[6]).getChildren().length);
	}

	public void testAsmLabelRanges() throws Exception {
		String source= fTU.getBuffer().getContents();
		ICElement[] labels= (ICElement[]) fTU.getChildrenOfType(ICElement.ASM_LABEL).toArray(new ICElement[0]);
		for (int i = 0; i < labels.length; i++) {
			String name= labels[i].getElementName();
			ISourceReference label= (ISourceReference)labels[i];
			ISourceRange range= label.getSourceRange();
			assertEquals(source.substring(range.getIdStartPos(), range.getIdStartPos() + range.getIdLength()), name);
			int endOfLabel= source.indexOf("/* end */", range.getIdStartPos());
			assertEquals(range.getIdStartPos(), range.getStartPos());
			assertEquals(endOfLabel, range.getStartPos() + range.getLength());
		}
	}
	
}
