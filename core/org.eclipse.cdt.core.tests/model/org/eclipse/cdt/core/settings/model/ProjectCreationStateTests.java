/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ProjectCreationStateTests  extends BaseTestCase{
	private static final String PROJ_NAME_PREFIX = "ProjectCreationStateTests_";

	private IProject p1, p2, p3, p4;
	private Listener listener;
	
	private class Listener implements ICProjectDescriptionListener {
		private boolean fIsCreationCompletedNotified;
		private boolean fIsCreating;
		private boolean fIsNotified;
		private String fProjName;
		
		Listener(String projName){
			init();
			fProjName = projName;
		}
		
		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			if(!event.getProject().getName().equals(fProjName))
				return;
			fIsNotified = true;
			boolean creating = event.getNewCProjectDescription().isCdtProjectCreating();
			ICDescriptionDelta delta = event.getProjectDelta();
			boolean notified = delta != null ? (delta.getChangeFlags() & ICDescriptionDelta.PROJECT_CREAION_COMPLETED) != 0 : false;
			
			if(creating)
				assertTrue(fIsCreating);
			if(notified)
				assertFalse(fIsCreationCompletedNotified);
			
			fIsCreating = creating;
			fIsCreationCompletedNotified = notified;
		}
		
		void init(){
			fIsCreating = true;
			fIsCreationCompletedNotified = false;
			resetNotified();
		}
		
		boolean isCreating(){
			return fIsCreating;
		}
		
		boolean isCreationCompletedNotified(){
			return fIsCreationCompletedNotified;
		}

		boolean isNotified(){
			boolean notified = fIsNotified;
			resetNotified();
			return notified;
		}

		void resetNotified(){
			fIsNotified = false;
		}

	}
	
	public static TestSuite suite() {
		return suite(ProjectCreationStateTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
//		p1 = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "a", IPDOMManager.ID_NO_INDEXER);
	}
	
	private void initListener(String projName){
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		if(listener != null)
			mngr.removeCProjectDescriptionListener(listener);
		listener = new Listener(projName);
		mngr.addCProjectDescriptionListener(listener, CProjectDescriptionEvent.APPLIED);
		
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());

	}
	
	public void testProjectCreatingState() throws Exception {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		String projName = PROJ_NAME_PREFIX + "a";
		
		initListener(projName);
		
		
		ICProject cp1 = CProjectHelper.createNewStileCProject(projName, IPDOMManager.ID_NO_INDEXER, true);
		IProject project = cp1.getProject();
		p1 = project;
		assertTrue(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
		listener.resetNotified();
		
		ICProjectDescription des = mngr.getProjectDescription(project, false);
		assertTrue(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, true);
		assertTrue(des.isCdtProjectCreating());
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
		
		ICProjectDescription des2 = mngr.getProjectDescription(project, true);
		des2.createConfiguration(CDataUtil.genId(null), CDataUtil.genId(null), des2.getConfigurations()[0]);
		mngr.setProjectDescription(project, des2);
		des2 = mngr.getProjectDescription(project, false);
		assertTrue(des2.isCdtProjectCreating());
		des2 = mngr.getProjectDescription(project, true);
		assertTrue(des2.isCdtProjectCreating());
		assertTrue(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
		listener.resetNotified();
		
		des.createConfiguration(CDataUtil.genId(null), CDataUtil.genId(null), des.getConfigurations()[0]);
		des.setCdtProjectCreated();
		des2.createConfiguration(CDataUtil.genId(null), CDataUtil.genId(null), des2.getConfigurations()[0]);
		mngr.setProjectDescription(project, des);
		assertTrue(listener.isNotified());
		assertTrue(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());
		listener.resetNotified();

		assertTrue(des2.isCdtProjectCreating());
		mngr.setProjectDescription(project, des2);
		assertTrue(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());
		listener.resetNotified();

		des2 = mngr.getProjectDescription(project, false);
		assertFalse(des2.isCdtProjectCreating());
		des2 = mngr.getProjectDescription(project, true);
		assertFalse(des2.isCdtProjectCreating());
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());
		
		des2.createConfiguration(CDataUtil.genId(null), CDataUtil.genId(null), des2.getConfigurations()[0]);
		mngr.setProjectDescription(project, des2);
		assertTrue(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());
		listener.resetNotified();
	}
	
	public void testCreateProjectDescriptionForInexistingDes() throws Exception {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		String projName = PROJ_NAME_PREFIX + "b";
		initListener(projName);

		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot();
		IProject project = root.getProject(projName);
		p2 = project;
		project.create(null);
		project.open(null);
		CProjectHelper.addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
		
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());


		ICProjectDescription des = mngr.getProjectDescription(project, false);
		assertNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNull(des);

		des = mngr.createProjectDescription(project, true);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
		
		des = mngr.createProjectDescription(project, false);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());

		des = mngr.createProjectDescription(project, true, true);
		assertTrue(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());

		des = mngr.createProjectDescription(project, false, true);
		assertTrue(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
	}
	
	public void testCreateProjectDescriptionForCreatingDes() throws Exception {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		String projName = PROJ_NAME_PREFIX + "c";
		
		initListener(projName);
		
		
		ICProject cp3 = CProjectHelper.createNewStileCProject(projName, IPDOMManager.ID_NO_INDEXER, true);
		IProject project = cp3.getProject();
		p3 = project;

		
		assertTrue(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());

		ICProjectDescription des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);

		des = mngr.createProjectDescription(project, true);
		assertTrue(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
		
		des = mngr.createProjectDescription(project, false);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());

		des = mngr.createProjectDescription(project, true, true);
		assertTrue(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());

		des = mngr.createProjectDescription(project, false, true);
		assertTrue(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertTrue(listener.isCreating());
	}
	
	public void testCreateProjectDescriptionForCreatedDes() throws Exception {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		String projName = PROJ_NAME_PREFIX + "d";
		
		initListener(projName);
		
		
		ICProject cp4 = CProjectHelper.createNewStileCProject(projName, IPDOMManager.ID_NO_INDEXER, false);
		IProject project = cp4.getProject();
		p4 = project;

		
		assertTrue(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());

		ICProjectDescription des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);

		des = mngr.createProjectDescription(project, true);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());
		
		des = mngr.createProjectDescription(project, false);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());

		des = mngr.createProjectDescription(project, true, true);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());

		des = mngr.createProjectDescription(project, false, true);
		assertFalse(des.isCdtProjectCreating());
		des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		des = mngr.getProjectDescription(project, true);
		assertNotNull(des);
		assertFalse(listener.isNotified());
		assertFalse(listener.isCreationCompletedNotified());
		assertFalse(listener.isCreating());
	}

	
	@Override
	protected void tearDown() throws Exception {
		if(listener != null){
			CoreModel.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(listener);
			listener = null;
		}
		try {
			if(p1 != null){
				p1.getProject().delete(true, null);
				p1 = null;
			}
		} catch (CoreException e){
		}
		try {
			if(p2 != null){
				p2.getProject().delete(true, null);
				p2 = null;
			}
		} catch (CoreException e){
		}
		try {
			if(p3 != null){
				p3.getProject().delete(true, null);
				p3 = null;
			}
		} catch (CoreException e){
		}
		try {
			if(p4 != null){
				p4.getProject().delete(true, null);
				p4 = null;
			}
		} catch (CoreException e){
		}

	}

}
