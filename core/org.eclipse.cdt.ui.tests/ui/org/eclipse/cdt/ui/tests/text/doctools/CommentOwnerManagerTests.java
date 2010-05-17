/*******************************************************************************
 * Copyright (c) 2008, 2010 Symbian Software Systems and others.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwnershipListener;

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
	
	IProject projectA, projectB, projectC;
	
	@Override
	protected void setUp() throws Exception {
		manager= DocCommentOwnerManager.getInstance();
		
		projectA= CProjectHelper.createCCProject("projectA", null).getProject();
		projectB= CProjectHelper.createCCProject("projectB", null).getProject();
		projectC= CProjectHelper.createCCProject("projectC", null).getProject();
		
		IDocCommentOwner[] owners= manager.getRegisteredOwners();
		OWNER_1= manager.getOwner("org.cdt.test.DCOM1");
		OWNER_2= manager.getOwner("org.cdt.test.DCOM2");
		OWNER_3= manager.getOwner("org.cdt.test.DCOM3");
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(projectA != null) {
			CProjectHelper.delete(CoreModel.getDefault().create(projectA));
		}
		if(projectB != null) {
			CProjectHelper.delete(CoreModel.getDefault().create(projectB));
		}
		if(projectC != null) {
			CProjectHelper.delete(CoreModel.getDefault().create(projectC));			
		}
	}
	
	public static Test suite() {
		return new TestSuite(CommentOwnerManagerTests.class);
	}
	
	public void testProjectLevel() throws Exception {
		manager.setCommentOwner(projectA, OWNER_3, true);
		manager.setCommentOwner(projectB, OWNER_2, true);
		manager.setCommentOwner(projectC, OWNER_1, true);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setCommentOwner(projectA, OWNER_2, true);
		manager.setCommentOwner(projectB, OWNER_1, true);
		manager.setCommentOwner(projectC, OWNER_3, true);
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		projectA.close(npm());
		projectB.close(npm());
		projectC.close(npm());
		
		projectA.open(npm());
		projectB.open(npm());
		projectC.open(npm());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
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
		manager.setCommentOwner(projectA, null, true);
		manager.setCommentOwner(projectB, null, true);
		manager.setCommentOwner(projectC, null, true);
		
		manager.setWorkspaceCommentOwner(OWNER_1);
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(OWNER_2);
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(OWNER_3);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setCommentOwner(projectA, OWNER_3, true);
		manager.setCommentOwner(projectB, OWNER_2, true);
		manager.setCommentOwner(projectC, OWNER_1, true);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(NullDocCommentOwner.INSTANCE);
		
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_3.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setCommentOwner(projectA, null, true);

		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
	
		manager.setCommentOwner(projectC, null, true);
		
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_2.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
	
		manager.setCommentOwner(projectB, null, true);
		
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(NullDocCommentOwner.INSTANCE.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
		
		manager.setWorkspaceCommentOwner(OWNER_1);
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectA.getFolder("foo/bar")).getID());
		
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectB.getFolder("foo/bar")).getID());

		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFile("foo/bar/baz")).getID());
		assertEquals(OWNER_1.getID(), manager.getCommentOwner(projectC.getFolder("foo/bar")).getID());
	}
	
	public void testDocListenerEvents_221109() {
		TestListener tl= new TestListener();
		
		manager.setCommentOwner(projectA, null, true);
		manager.setCommentOwner(projectB, null, true);
		manager.setCommentOwner(projectC, null, true);
		manager.setWorkspaceCommentOwner(OWNER_1);
		manager.addListener(tl);
		
		tl.reset();
		manager.setCommentOwner(projectA, OWNER_1, true);
		assertEquals(0, tl.prjEvents);
		assertEquals(0, tl.wkspcEvents);
		
		tl.reset();
		manager.setCommentOwner(projectA, OWNER_2, true);
		assertEquals(1, tl.prjEvents);
		assertEquals(0, tl.wkspcEvents);
		
		tl.reset();
		manager.setCommentOwner(projectA, OWNER_3, true);
		assertEquals(1, tl.prjEvents);
		assertEquals(0, tl.wkspcEvents);
		
		tl.reset();
		manager.setCommentOwner(projectA, OWNER_1, true);
		assertEquals(1, tl.prjEvents);
		assertEquals(0, tl.wkspcEvents);
		
		tl.reset();
		manager.setCommentOwner(ResourcesPlugin.getWorkspace().getRoot(), OWNER_2, true);
		assertEquals(0, tl.prjEvents);
		assertEquals(1, tl.wkspcEvents);
		
		tl.reset();
		manager.setWorkspaceCommentOwner(OWNER_3);
		assertEquals(0, tl.prjEvents);
		assertEquals(1, tl.wkspcEvents);
		
		tl.reset();
		manager.setWorkspaceCommentOwner(OWNER_3);
		assertEquals(0, tl.prjEvents);
		assertEquals(0, tl.wkspcEvents);
		
		manager.removeListener(tl);
	}
}

class TestListener implements IDocCommentOwnershipListener {
	public int prjEvents, wkspcEvents;
	public void ownershipChanged(IResource resource,
			boolean submappingsRemoved, IDocCommentOwner oldOwner,
			IDocCommentOwner newOwner) {
		prjEvents++;
	}
	public void workspaceOwnershipChanged(IDocCommentOwner oldOwner,
			IDocCommentOwner newOwner) {
		wkspcEvents++;
	}
	public void reset() {
		prjEvents= wkspcEvents= 0;
	}
}
