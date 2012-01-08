/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.index.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class TeamSharedIndexTest extends IndexTestBase {

	public static TestSuite suite() {
		return suite(TeamSharedIndexTest.class);
	}

	private Collection fProjects= new LinkedList();
	private static final IIndexManager fPDOMManager = CCorePlugin.getIndexManager();

	public TeamSharedIndexTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fProjects.clear();
	}
	
	@Override
	protected void tearDown() throws Exception {
		for (Iterator iterator = fProjects.iterator(); iterator.hasNext();) {
			ICProject project = (ICProject) iterator.next();
			CProjectHelper.delete(project);
		}
		super.tearDown();
	}
			
	private void registerProject(ICProject prj) {
		fProjects.add(prj);
	}
	private void unregisterProject(ICProject prj) {
		fProjects.remove(prj);
	}
	
	private ICProject createProject(String name) throws CoreException {
		ModelJoiner mj= new ModelJoiner();
		try {
			ICProject project= CProjectHelper.createCCProject(name, null, IPDOMManager.ID_NO_INDEXER);
			registerProject(project);
			TestSourceReader.createFile(project.getProject(), "a.cpp", "int a;");
			TestSourceReader.createFile(project.getProject(), "b.cpp", "int b;");
			TestSourceReader.createFile(project.getProject(), "c.cpp", "int c;");
			mj.join(); // in order we are sure the indexer task has been scheduled before joining the indexer

			fPDOMManager.setIndexerId(project, IPDOMManager.ID_FAST_INDEXER);
			assertTrue(fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm()));
			return project;
		} finally {
			mj.dispose();
		}
	}
	
	private ICProject recreateProject(final String prjName) throws Exception {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ModelJoiner pj= new ModelJoiner();
		try {
			final IProject prjHandle= workspace.getRoot().getProject(prjName);
			workspace.run(new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					IProjectDescription desc= IDEWorkbenchPlugin.getPluginWorkspace().newProjectDescription(prjName);
					prjHandle.create(desc, npm());
					prjHandle.open(0, npm());
				}
			}, null);
			pj.join();  // in order we are sure the indexer task has been scheduled before joining the indexer
		} finally {
			pj.dispose();
		}
		ICProject result= CoreModel.getDefault().create(workspace.getRoot().getProject(prjName));
		waitForIndexer(result);
		return result;
	}

	private void checkVariable(ICProject prj, String var, int expectedCount)
			throws CoreException, InterruptedException {
		IIndex index= fPDOMManager.getIndex(prj);
		index.acquireReadLock();
		try {
			IBinding[] binding= index.findBindings(var.toCharArray(), IndexFilter.ALL, npm());
			int count= 0;
			assertTrue(binding.length < 2);
			if (binding.length == 1) {
				assertTrue(binding[0] instanceof IVariable);
				count= index.findNames(binding[0], IIndex.FIND_ALL_OCCURRENCES).length;
			}
			assertEquals(var, expectedCount, count);
		}
		finally {
			index.releaseReadLock();
		}
	} 

	public void testDefaultExport() throws Exception {
		String prjName= "__testDefaultExport__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);

		// export the project.
		fPDOMManager.export(prj, loc, 0, npm());
		
		// set indexer to the fake one.
		fPDOMManager.setIndexerId(prj, FakeIndexer.ID);		
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 0);
		checkVariable(prj, "c", 0);
		
		// delete project
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		assertEquals(FakeIndexer.ID, fPDOMManager.getIndexerId(prj));

		registerProject(prj);
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
	}
	
	public void testExportWithFileChange() throws Exception {
		String prjName= "__testExportWithChange__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		
		// export the project.
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.export(prj, loc, 0, npm());
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		
		// change file
		changeFile(prj);
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		registerProject(prj);
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		checkVariable(prj, "d", 1);
	}

	private void changeFile(ICProject prj) throws CoreException {
		final IFile file = prj.getProject().getFile("a.cpp");
		final File location = file.getLocation().toFile();
		final long lm= location.lastModified();
		file.setContents(new ByteArrayInputStream("int d;".getBytes()), true, false, npm());
		if (location.lastModified() == lm) {
			location.setLastModified(lm+1000);
		}
	}
	
	private void deleteAndWait(ICProject prj) throws CoreException {
		ModelJoiner dj= new ModelJoiner();
		try {
			prj.getProject().delete(false, true, npm());
			dj.join();
		} finally {
			dj.dispose();
		}
	}

	public void testExportWithFileChangeFake() throws Exception {
		String prjName= "__testExportWithChangeFake__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		
		// export the project.
		fPDOMManager.export(prj, loc, 0, npm());
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		
		// set indexer to the fake one.
		fPDOMManager.setIndexerId(prj, FakeIndexer.ID);		
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 0);
		checkVariable(prj, "c", 0);
		
		changeFile(prj);
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		registerProject(prj);
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		checkVariable(prj, "d", 0);
	}

	public void testExportWithAddition() throws Exception {
		String prjName= "__testExportWithAddition__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		
		// export the project.
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.export(prj, loc, 0, npm());
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		
		// add file
		TestSourceReader.createFile(prj.getProject(), "d.cpp", "int d;");
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		registerProject(prj);
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		checkVariable(prj, "d", 1);
	}

	public void testExportWithAdditionFake() throws Exception {
		String prjName= "__testExportWithAdditionFake__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		
		// export the project.
		fPDOMManager.export(prj, loc, 0, npm());
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		
		// set indexer to the fake one.
		fPDOMManager.setIndexerId(prj, FakeIndexer.ID);		
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 0);
		checkVariable(prj, "c", 0);

		// add file
		TestSourceReader.createFile(prj.getProject(), "d.cpp", "int d;");
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		registerProject(prj);
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		checkVariable(prj, "d", 0);
	}

	public void testExportWithRemoval() throws Exception {
		String prjName= "__testExportWithRemoval__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		
		// export the project.
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.export(prj, loc, 0, npm());
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		
		// delete file
		prj.getProject().getFile("a.cpp").delete(true, npm());
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		registerProject(prj);
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
	}

	public void testExportWithRemovalFake() throws Exception {
		String prjName= "__testExportWithRemovalFake__";
		ICProject prj= createProject(prjName);
		String loc= IndexerPreferences.getIndexImportLocation(prj.getProject());
		checkVariable(prj, "a", 1);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
		
		// export the project.
		fPDOMManager.export(prj, loc, 0, npm());
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		
		// set indexer to the fake one.
		fPDOMManager.setIndexerId(prj, FakeIndexer.ID);		
		IndexerPreferences.setScope(prj.getProject(), IndexerPreferences.SCOPE_PROJECT_SHARED);
		new ProjectScope(prj.getProject()).getNode(CCorePlugin.PLUGIN_ID).flush();
		fPDOMManager.joinIndexer(INDEXER_WAIT_TIME, npm());
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 0);
		checkVariable(prj, "c", 0);

		// delete file
		prj.getProject().getFile("a.cpp").delete(true, npm());
		deleteAndWait(prj);
		unregisterProject(prj);
		
		// import project
		prj = recreateProject(prjName);
		registerProject(prj);
		checkVariable(prj, "a", 0);
		checkVariable(prj, "b", 1);
		checkVariable(prj, "c", 1);
	}
}
