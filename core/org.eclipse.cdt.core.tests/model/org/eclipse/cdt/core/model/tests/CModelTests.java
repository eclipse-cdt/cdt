/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Graves (QNX Software Systems) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.settings.model.COutputEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;

/**
 * This file contains a set of generic tests for the core C model. Nothing 
 * exotic, but should be a small sanity set of tests.
 */
public class CModelTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    IProject project_c, project_cc;
    NullProgressMonitor monitor;

    /**
     * Constructor for CModelTests.
     * @param name
     */
    public CModelTests(String name) {
        super(name);
    }
    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    @Override
	protected void setUp() throws Exception {
        /***
         * The test of the tests assume that they have a working workspace
         * and workspace root object to use to create projects/files in, 
         * so we need to get them setup first.
         */
        IWorkspaceDescription desc;
        workspace= ResourcesPlugin.getWorkspace();
        root= workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (workspace==null) 
            fail("Workspace was not setup");
        if (root==null)
            fail("Workspace root was not setup");
		desc=workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);

    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @Override
	protected void tearDown() {
       // release resources here and clean-up
    }
    
    public static TestSuite suite() {
        return new TestSuite(CModelTests.class);
    }
    
    public static void main (String[] args){
        junit.textui.TestRunner.run(suite());
    }

   
    /***
     * The follow are a simple set of tests to make usre the HasC/CCNature calls
     * seem to be sane.
     * 
     * Assumes that the CProjectHelper.createCProject properly creates a C 
     * project with a C nature, but does not add the CC nature.
     * It also assums that the AddCCNature call works 
     * 
     * @see CProjectHelper#createCProject
     * @see CoreModel#addCCNature
     */
    public void testHasNature() throws CoreException {
        ICProject testProject;
        testProject=CProjectHelper.createCProject("naturetest", "none", IPDOMManager.ID_NO_INDEXER);
        if (testProject==null)
            fail("Unable to create project");
        assertTrue("hasCNature works", CoreModel.hasCNature(testProject.getProject()));
        assertTrue("hasCCNature works without ccnature", !(CoreModel.hasCCNature(testProject.getProject())));
   
   
        CCProjectNature.addCCNature(testProject.getProject(), monitor);
        assertTrue("hasCCNature works", (CoreModel.hasCCNature(testProject.getProject())));
        
        CCProjectNature.removeCCNature(testProject.getProject(), monitor);
        CProjectNature.removeCNature(testProject.getProject(), monitor);                
        assertTrue("hasCNature works without cnature", !CoreModel.hasCNature(testProject.getProject()));
        assertTrue("hasCCNature works without ccnature or cnature", !(CoreModel.hasCCNature(testProject.getProject())));
		try{
			testProject.getProject().delete(true,true,monitor);
		} 
		catch (CoreException e) {}
    }    

    /***
     * Simple tests to make sure the models file identification methods seem
     * to work as expected.
     */
    public void testFileType() throws CoreException,FileNotFoundException {
        ICProject testProject;
        testProject=CProjectHelper.createCProject("filetest", "none", IPDOMManager.ID_NO_INDEXER);
        if (testProject==null)
            fail("Unable to create project");

        IFile file = testProject.getProject().getFile("exetest_g");
        if (!file.exists()) {
            file.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o.g/exe_g"))),
            		false, monitor);
        }
        /***
         * file should be a binary, executable, not shared or archive
         */
        assertTrue("isBinary", CoreModel.getDefault().isBinary(file));
        assertTrue("isExecutable", CoreModel.getDefault().isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.getDefault().isSharedLib(file));
        assertTrue("isArchive", !CoreModel.getDefault().isArchive(file));
        assertTrue("isObject", !CoreModel.getDefault().isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));
        
        
        file = testProject.getProject().getFile("exetest.c");
        if (!file.exists()) {
            file.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/main.c"))),
					false, monitor);
        }
        /***
         * file should be a translation unit
         */
        assertTrue("isBinary", !CoreModel.getDefault().isBinary(file));
        assertTrue("isExecutable", !CoreModel.getDefault().isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.getDefault().isSharedLib(file));
        assertTrue("isArchive", !CoreModel.getDefault().isArchive(file));
        assertTrue("isObject", !CoreModel.getDefault().isObject(file));
        assertTrue("isTranslationUnit", CoreModel.isTranslationUnit(file));
        
        file = testProject.getProject().getFile("exetest.o");
        if (!file.exists()) {
            file.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/exe/x86/o.g/main.o"))),
					false, monitor);
        }
        /***
         * file should be a object file unit
         */
        assertTrue("isBinary", CoreModel.getDefault().isBinary(file));
        assertTrue("isExecutable", !CoreModel.getDefault().isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.getDefault().isSharedLib(file));
        assertTrue("isArchive", !CoreModel.getDefault().isArchive(file));
        assertTrue("isObject", CoreModel.getDefault().isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));

        file = testProject.getProject().getFile("liblibtest_g.so");
        if (!file.exists()) {
            file.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/testlib/x86/so.g/libtestlib_g.so"))),
					false, monitor);
        }
        /***
         * file should be a sharedlib/binary file
         */
        assertTrue("isBinary", CoreModel.getDefault().isBinary(file));
        assertTrue("isExecutable", !CoreModel.getDefault().isExecutable(file));
        assertTrue("isSharedLib", CoreModel.getDefault().isSharedLib(file));
        assertTrue("isArchive", !CoreModel.getDefault().isArchive(file));
        assertTrue("isObject", !CoreModel.getDefault().isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));

        file = testProject.getProject().getFile("liblibtest_g.a");
        if (!file.exists()) {
            file.create(new FileInputStream(
            		CTestPlugin.getDefault().getFileInPlugin(new Path("resources/testlib/x86/a.g/libtestlib_g.a"))),
					false, monitor);
        } else {
            fail("Does not exist?");
        }
        /***
         * file should be a archive file
         */
        assertTrue("isArchive", CoreModel.getDefault().isArchive(file));
        assertTrue("isBinary:", !CoreModel.getDefault().isBinary(file));
        assertTrue("isExecutable", !CoreModel.getDefault().isExecutable(file));
        assertTrue("isSharedLib", !CoreModel.getDefault().isSharedLib(file));
        assertTrue("isArchive", CoreModel.getDefault().isArchive(file));
        assertTrue("isObject", !CoreModel.getDefault().isObject(file));
        assertTrue("isTranslationUnit", !CoreModel.isTranslationUnit(file));


       
		try{
			testProject.getProject().delete(true,true,monitor);
		} 
		catch (CoreException e) {}
    }    

    /****
     * Some simple tests for isValidTranslationUnitName
     */
    public void testIsValidTranslationUnitName() throws CoreException {
        assertTrue("Invalid C file", !CoreModel.isValidTranslationUnitName(null, "notcfile"));        
        assertTrue("Invalid C file", !CoreModel.isValidTranslationUnitName(null, "not.c.file"));        
        assertTrue("Invalid C file", !CoreModel.isValidTranslationUnitName(null, "not.ca"));        
        assertTrue("Valid C file", CoreModel.isValidTranslationUnitName(null, "areal.c"));        
    }
    
    // bug 275609
    public void testSourceExclusionFilters_275609() throws Exception {
        ICProject testProject;
        testProject=CProjectHelper.createCProject("bug257609", "none", IPDOMManager.ID_NO_INDEXER);
        if (testProject==null)
            fail("Unable to create project");

        IFolder testFolder = testProject.getProject().getFolder("test");
    	testFolder.create(true, true, monitor);
        IFolder subFolder1 = testFolder.getFolder("1");
    	subFolder1.create(true, true, monitor);
        IFolder subFolder2 = testFolder.getFolder("2");
    	subFolder2.create(true, true, monitor);
        IFile file0 = testFolder.getFile("test0.c");
        file0.create(new ByteArrayInputStream(new byte[0]), true, monitor);
        IFile file1 = subFolder1.getFile("test1.c");
        file1.create(new ByteArrayInputStream(new byte[0]), true, monitor);
        IFile file2 = subFolder2.getFile("test2.c");
        file2.create(new ByteArrayInputStream(new byte[0]), true, monitor);

        List<ICElement> cSourceRoots = testProject.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cSourceRoots.size());
        assertEquals(testProject.getElementName(), cSourceRoots.get(0).getElementName());
        
        ISourceRoot sourceRoot = (ISourceRoot) cSourceRoots.get(0);
        
        List<ICElement> cContainers = sourceRoot.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cContainers.size());
        assertEquals("test", cContainers.get(0).getElementName());

        ICContainer testContainer = (ICContainer) cContainers.get(0);
        
        List<ICElement> subContainers = testContainer.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(2, subContainers.size());
        assertEquals("1", subContainers.get(0).getElementName());
        assertEquals("2", subContainers.get(1).getElementName());
		Object[] nonCResources= testContainer.getNonCResources();
		assertEquals(0, nonCResources.length);
		
        List<ICElement> tUnits = testContainer.getChildrenOfType(ICElement.C_UNIT);
        assertEquals(1, tUnits.size());
        assertEquals("test0.c", tUnits.get(0).getElementName());

		ICProjectDescription prjDesc= CoreModel.getDefault().getProjectDescription(testProject.getProject(), true);
		ICConfigurationDescription activeCfg= prjDesc.getActiveConfiguration();
		assertNotNull(activeCfg);
		
		// add filter to source entry
		ICSourceEntry[] entries = activeCfg.getSourceEntries();
		final String sourceEntryName = entries[0].getName();
		final IPath[] exclusionPatterns = new IPath[] { new Path("test/*") };

		ICSourceEntry entry = new CSourceEntry(sourceEntryName, exclusionPatterns, entries[0].getFlags());
		activeCfg.setSourceEntries(new ICSourceEntry[] {entry});

		// store the changed configuration
		CoreModel.getDefault().setProjectDescription(testProject.getProject(), prjDesc);
		
        cSourceRoots = testProject.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cSourceRoots.size());
        assertEquals(testProject.getElementName(), cSourceRoots.get(0).getElementName());
        
        sourceRoot = (ISourceRoot) cSourceRoots.get(0);
        
        cContainers = sourceRoot.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cContainers.size());
        assertEquals("test", cContainers.get(0).getElementName());
        
        testContainer = (ICContainer) cContainers.get(0);
        
        tUnits = testContainer.getChildrenOfType(ICElement.C_UNIT);
        assertEquals(0, tUnits.size());

        subContainers = testContainer.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(0, subContainers.size());
        nonCResources= testContainer.getNonCResources();
		assertEquals(3, nonCResources.length);
		assertEquals(subFolder1, nonCResources[0]);
		assertEquals(subFolder2, nonCResources[1]);
		assertEquals(file0, nonCResources[2]);
		
		try {
			testProject.getProject().delete(true,true,monitor);
		} 
		catch (CoreException e) {}

	}

    // bug 179474
    public void testSourceExclusionFilters_179474() throws Exception {
        ICProject testProject;
        testProject=CProjectHelper.createCProject("bug179474", "none", IPDOMManager.ID_NO_INDEXER);
        if (testProject==null)
            fail("Unable to create project");

        IFolder subFolder = testProject.getProject().getFolder("sub");
    	subFolder.create(true, true, monitor);
        IFile fileA = testProject.getProject().getFile("a.cpp");
        fileA.create(new ByteArrayInputStream(new byte[0]), true, monitor);
        IFile fileB = subFolder.getFile("b.cpp");
        fileB.create(new ByteArrayInputStream(new byte[0]), true, monitor);

        List<ICElement> cSourceRoots = testProject.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cSourceRoots.size());
        assertEquals(testProject.getElementName(), cSourceRoots.get(0).getElementName());
        
        ISourceRoot sourceRoot = (ISourceRoot) cSourceRoots.get(0);
        
        List<ICElement> cContainers = sourceRoot.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cContainers.size());
        assertEquals(subFolder.getName(), cContainers.get(0).getElementName());

        ICContainer subContainer = (ICContainer) cContainers.get(0);
        
        List<ICElement> tUnits = subContainer.getChildrenOfType(ICElement.C_UNIT);
        assertEquals(1, tUnits.size());
        assertEquals(fileB.getName(), tUnits.get(0).getElementName());

        tUnits = sourceRoot.getChildrenOfType(ICElement.C_UNIT);
        assertEquals(1, tUnits.size());
        assertEquals(fileA.getName(), tUnits.get(0).getElementName());

		ICProjectDescription prjDesc= CoreModel.getDefault().getProjectDescription(testProject.getProject(), true);
		ICConfigurationDescription activeCfg= prjDesc.getActiveConfiguration();
		assertNotNull(activeCfg);
		
		// add filter to source entry
		ICSourceEntry[] entries = activeCfg.getSourceEntries();
		final String sourceEntryName = entries[0].getName();
		final IPath[] exclusionPatterns = new IPath[] { new Path("**/*.cpp") };

		ICSourceEntry entry = new CSourceEntry(sourceEntryName, exclusionPatterns, entries[0].getFlags());
		activeCfg.setSourceEntries(new ICSourceEntry[] {entry});

		// store the changed configuration
		CoreModel.getDefault().setProjectDescription(testProject.getProject(), prjDesc);
		
        cSourceRoots = testProject.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cSourceRoots.size());
        assertEquals(testProject.getElementName(), cSourceRoots.get(0).getElementName());
        
        sourceRoot = (ISourceRoot) cSourceRoots.get(0);
        
        cContainers = sourceRoot.getChildrenOfType(ICElement.C_CCONTAINER);
        assertEquals(1, cContainers.size());
        assertEquals(subFolder.getName(), cContainers.get(0).getElementName());
        
        subContainer = (ICContainer) cContainers.get(0);
        
        tUnits = subContainer.getChildrenOfType(ICElement.C_UNIT);
        assertEquals(0, tUnits.size());

        tUnits = sourceRoot.getChildrenOfType(ICElement.C_UNIT);
        assertEquals(0, tUnits.size());

        Object[] nonCResources = subContainer.getNonCResources();
		assertEquals(1, nonCResources.length);
		assertEquals(fileB, nonCResources[0]);

		nonCResources = sourceRoot.getNonCResources();
		assertTrue(Arrays.asList(nonCResources).contains(fileA));
		
		try {
			testProject.getProject().delete(true,true,monitor);
		} 
		catch (CoreException e) {}
	}
    
    // bug 294965
    public void testBinaryInProjectRoot_294965() throws Exception {
        ICProject testProject;
        testProject = CProjectHelper.createCProject("bug294965", "none", IPDOMManager.ID_NO_INDEXER);
        if (testProject == null) {
            fail("Unable to create project");
        }
        CProjectHelper.addDefaultBinaryParser(testProject.getProject());
        CProjectHelper.importSourcesFromPlugin(testProject, CTestPlugin.getDefault().getBundle(), "resources/exe/x86/o");

		testProject.getProject().getFolder("out").create(true, true, monitor);
		
		ICProjectDescription prjDesc= CoreModel.getDefault().getProjectDescription(testProject.getProject(), true);
		ICConfigurationDescription cfg= prjDesc.getActiveConfiguration();
		assertNotNull(cfg);

		// add filter to source entry
		ICSourceEntry[] entries = cfg.getSourceEntries();
		final String sourceEntryName = entries[0].getName();
		final IPath[] exclusionPatterns = new IPath[] { new Path("test/*") };

		ICSourceEntry sourceEntry = new CSourceEntry(sourceEntryName, exclusionPatterns, entries[0].getFlags());
		cfg.setSourceEntries(new ICSourceEntry[] { sourceEntry });

		// set output entry
		ICOutputEntry outputEntry = new COutputEntry(testProject.getProject().getFolder("out"), new IPath[0], 0);
		cfg.getBuildSetting().setOutputDirectories(new ICOutputEntry[] { outputEntry });
		
		assertEquals(outputEntry, cfg.getBuildSetting().getOutputDirectories()[0]);
		
		// store the changed configuration
		CoreModel.getDefault().setProjectDescription(testProject.getProject(), prjDesc, true, monitor);
		testProject.close();
		testProject.getProject().close(monitor);
		testProject.getProject().open(monitor);

		prjDesc= CoreModel.getDefault().getProjectDescription(testProject.getProject(), false);
		cfg= prjDesc.getActiveConfiguration();
		assertEquals(outputEntry, cfg.getBuildSetting().getOutputDirectories()[0]);

        Object[] nonCResources = testProject.getNonCResources();
		assertEquals(7, nonCResources.length);
		
		try {
			testProject.getProject().delete(true,true,monitor);
		} 
		catch (CoreException e) {}
    }

    // bug 131165
    public void testPickUpBinariesInNewFolder_131165() throws Exception {
        ICProject testProject;
        testProject = CProjectHelper.createCProject("bug131165", "none", IPDOMManager.ID_NO_INDEXER);
        if (testProject == null) {
            fail("Unable to create project");
        }
        CProjectHelper.addDefaultBinaryParser(testProject.getProject());
        
        final IBinaryContainer bin = testProject.getBinaryContainer();
        assertEquals(0, bin.getBinaries().length);

        final boolean binContainerChanged[] = { false };
        
        IElementChangedListener elementChangedListener = new IElementChangedListener() {
            @Override
			public void elementChanged(ElementChangedEvent event) {
                ICElementDelta delta = event.getDelta();
                processDelta(delta);
            }
            private boolean processDelta(ICElementDelta delta) {
                if (delta.getElement().equals(bin)) {
                    synchronized (binContainerChanged) {
                        binContainerChanged[0] = true;
                        binContainerChanged.notify();
                    }
                    return true;
                }
                ICElementDelta[] childDeltas = delta.getChangedChildren();
                for (ICElementDelta childDelta : childDeltas) {
                    if (processDelta(childDelta)) {
                        return true;
                    }
                }
                return false;
            }
        };
        CoreModel.getDefault().addElementChangedListener(elementChangedListener );

        Thread waiter = new Thread() {
            @Override
            public void run() {
                synchronized (binContainerChanged) {
                    try {
                        binContainerChanged.wait(1000);
                    } catch (InterruptedException exc) {
                    }
                }
            }
        };
        waiter.start();
        Thread.sleep(50);
        
        // import with folder structure
        importSourcesFromPlugin(testProject, CTestPlugin.getDefault().getBundle(), "resources/exe/x86");

        // wait for delta notification
        waiter.join(1000);
        
        assertTrue(binContainerChanged[0]);
        assertEquals(2, bin.getBinaries().length);

        try {
            testProject.getProject().delete(true,true,monitor);
        } 
        catch (CoreException e) {}
    }
    
    // same as CprojectHelper.importSourcesFromPlugin(), but preserving folder structure
    private static void importSourcesFromPlugin(ICProject project, Bundle bundle, String sources) throws CoreException {
        try {
            String baseDir= FileLocator.toFileURL(FileLocator.find(bundle, new Path(sources), null)).getFile();
            ImportOperation importOp = new ImportOperation(project.getProject().getFullPath(),
                    new File(baseDir), FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
                        @Override
						public String queryOverwrite(String file) {
                            return ALL;
                        }});
            importOp.setCreateContainerStructure(true);
            importOp.run(new NullProgressMonitor());
        }
        catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, CTestPlugin.PLUGIN_ID, 0, "Import Interrupted", e));
        }
    }

}
