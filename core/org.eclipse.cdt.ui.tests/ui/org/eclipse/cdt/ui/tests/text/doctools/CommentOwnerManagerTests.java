/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text.doctools;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.NullDocCommentOwner;

/**
 * Test comment ownership mapping
 */
public class CommentOwnerManagerTests extends BaseTestCase {
	IDocCommentOwner OWNER_1;
	IDocCommentOwner OWNER_2;
	IDocCommentOwner OWNER_3;
	DocCommentOwnerManager manager;
	
	ICProject projectA, projectB, projectC;
	
	protected void setUp() throws Exception {
		manager= DocCommentOwnerManager.getInstance();
		
		projectA= CProjectHelper.createCCProject("projectA", null);
		projectB= CProjectHelper.createCCProject("projectB", null);
		projectC= CProjectHelper.createCCProject("projectC", null);
		
		IDocCommentOwner[] owners= manager.getRegisteredOwners();
		OWNER_1= manager.getOwner("org.cdt.test.DCOM1");
		OWNER_2= manager.getOwner("org.cdt.test.DCOM2");
		OWNER_3= manager.getOwner("org.cdt.test.DCOM3");
	}
	
	protected void tearDown() throws Exception {
		if(projectA != null) {
			CProjectHelper.delete(projectA);
		}
		if(projectB != null) {
			CProjectHelper.delete(projectB);
		}
		if(projectC != null) {
			CProjectHelper.delete(projectC);			
		}
	}
	
	public static Test suite() {
		return new TestSuite(CommentOwnerManagerTests.class);
	}
	
	public void testProjectLevel() throws Exception {
		manager.setCommentOwner(projectA.getProject(), OWNER_3, true);
		manager.setCommentOwner(projectB.getProject(), OWNER_2, true);
		manager.setCommentOwner(projectC.getProject(), OWNER_1, true);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setCommentOwner(projectA.getProject(), OWNER_2, true);
		manager.setCommentOwner(projectB.getProject(), OWNER_1, true);
		manager.setCommentOwner(projectC.getProject(), OWNER_3, true);
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		projectA.getProject().close(NPM);
		projectB.getProject().close(NPM);
		projectC.getProject().close(NPM);
		
		projectA.getProject().open(NPM);
		projectB.getProject().open(NPM);
		projectC.getProject().open(NPM);
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
	}
	
	public void testBoundaryConditions1() throws Exception {
		DocCommentOwnerManager manager= DocCommentOwnerManager.getInstance();
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(null).getID());
	}
	
	public void testBoundaryConditions2() throws Exception {
		try {
			manager.setWorkspaceCommentOwner(null);
			fail();
		} catch(Exception e) {
			// expected
		}
	}
	
	public void testWorkspaceRootLevel() throws Exception {
		manager.setCommentOwner(projectA.getProject(), null, true);
		manager.setCommentOwner(projectB.getProject(), null, true);
		manager.setCommentOwner(projectC.getProject(), null, true);
		
		manager.setWorkspaceCommentOwner(OWNER_1);
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(OWNER_2);
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(OWNER_3);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setCommentOwner(projectA.getProject(), OWNER_3, true);
		manager.setCommentOwner(projectB.getProject(), OWNER_2, true);
		manager.setCommentOwner(projectC.getProject(), OWNER_1, true);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(NullDocCommentOwner.INSTANCE);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setCommentOwner(projectA.getProject(), null, true);

		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
	
		manager.setCommentOwner(projectC.getProject(), null, true);
		
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
	
		manager.setCommentOwner(projectB.getProject(), null, true);
		
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(OWNER_1);
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getProject().getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getProject().getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject()).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getProject().getFolder("foo/bar")).getID());
	}
}
