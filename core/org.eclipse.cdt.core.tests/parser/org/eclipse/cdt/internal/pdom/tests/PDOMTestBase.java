/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.fast.PDOMFastIndexer;
import org.eclipse.cdt.internal.core.pdom.indexer.fast.PDOMFastReindex;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * @author Doug Schaefer
 */
public class PDOMTestBase extends BaseTestCase {

	protected static final IProgressMonitor PROGRESS = new NullProgressMonitor();
	static IPath rootPath = new Path("resources/pdomtests");
	private String projectName= null;

	protected ICProject createProject(String folderName) throws CoreException {
		
		// Create the project
		projectName = "ProjTest_" + System.currentTimeMillis();
		final File rootDir = CTestPlugin.getDefault().getFileInPlugin(rootPath.append(folderName));
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final ICProject cprojects[] = new ICProject[1];
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IPDOMManager manager = CCorePlugin.getPDOMManager(); 
				// Make sure the default is no indexer
				String oldDefault = manager.getDefaultIndexerId();
				if (!PDOMNullIndexer.ID.equals(oldDefault))
					manager.setDefaultIndexerId(PDOMNullIndexer.ID);
				else
					oldDefault = null;
				
				// Create the project
				IProject project = workspace.getRoot().getProject(projectName);
				project.create(monitor);
				project.open(monitor);
				
				// Set up as C++ project
				IProjectDescription description = project.getDescription();
				String[] prevNatures = description.getNatureIds();
				String[] newNatures = new String[prevNatures.length + 2];
				System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
				newNatures[prevNatures.length] = CProjectNature.C_NATURE_ID;
				newNatures[prevNatures.length + 1] = CCProjectNature.CC_NATURE_ID;
				description.setNatureIds(newNatures);
				project.setDescription(description, monitor);

				// Import the files at the root
				ImportOperation importOp = new ImportOperation(project.getFullPath(),
						rootDir, FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
					public String queryOverwrite(String pathString) {
						return IOverwriteQuery.ALL;
					}
				});
				importOp.setCreateContainerStructure(false);
				try {
					importOp.run(monitor);
				} catch (Exception e) {
					throw new CoreException(new Status(IStatus.ERROR,
							CTestPlugin.PLUGIN_ID, 0, "Import Interrupted", e));
				}
				
				ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);

				// Index the project
				PDOMFastIndexer indexer = new PDOMFastIndexer();
				indexer.setProject(cproject);
				PDOMFastReindex reindex = new PDOMFastReindex(indexer);
				reindex.run(monitor);
				
				// Set the default indexer back
				if (oldDefault != null)
					manager.setDefaultIndexerId(oldDefault);

				cprojects[0] = cproject;
			}
		}, null);
		
		return cprojects[0];
	}

	protected int offset(String projectRelativePath, String lookfor) throws BadLocationException, CoreException {
		Path path= new Path(projectName + "/" + projectRelativePath);
		return TestSourceReader.indexOfInFile(lookfor, path);
	}

	protected IBinding[] findQualifiedName(PDOM pdom, String name) throws CoreException {
		String[] segments = name.split("::");
		Pattern[] pattern = new Pattern[segments.length];
		for (int i = 0; i < segments.length; i++) {
			pattern[i] = Pattern.compile(segments[i]);
		}
		return pdom.findBindings(pattern, true, new IndexFilter(), PROGRESS);
	}

	/**
	 * Convenience method for checking the number of PDOM references for a
	 * particular name.
	 */
	protected void assertReferenceCount(PDOM pdom, String name, int count) throws CoreException {
		assertNameCount(pdom, name, count, IIndexFragment.FIND_REFERENCES);
	}

	/**
	 * Convenience method for checking the number of PDOM declarations for a
	 * particular name.
	 */
	protected void assertDeclarationCount(PDOM pdom, String name, int count) throws CoreException {
		assertNameCount(pdom, name, count, IIndexFragment.FIND_DECLARATIONS_DEFINITIONS);
	}

	/**
	 * Convenience method for checking the number of PDOM definitions for a
	 * particular name.
	 */
	protected void assertDefinitionCount(PDOM pdom, String name, int count) throws CoreException {
		assertNameCount(pdom, name, count, IIndexFragment.FIND_DEFINITIONS);
	}

	private void assertNameCount(PDOM pdom, String name, int count, int options) throws CoreException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		if (count > 0) {
			assertEquals(1, bindings.length);
		}
		if (bindings.length > 0) {
			IName[] names = pdom.findNames(bindings[0], options);
			assertUniqueNameCount(names, count);
		} else {
			assertEquals(0, count);
		}
		
	}

	/**
	 * Convenience method for checking how many unique instances (i.e. same
	 * offset within the same file) there are within an array of INames.
	 */
	private void assertUniqueNameCount(IName[] names, int count) {
		Set offsets = new HashSet();
		for (int i = 0; i < names.length; i++) {
			offsets.add(names[i].getFileLocation());
		}
		assertEquals(count, offsets.size());
	}

	protected void assertType(PDOM pdom, String name, Class c) throws CoreException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		assertEquals(1, bindings.length);
		assertTrue(c.isAssignableFrom(bindings[0].getClass()));
	}

	protected void assertVisibility(PDOM pdom, String name, int visibility) throws CoreException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		assertEquals(1, bindings.length);
		ICPPMember member = (ICPPMember) bindings[0];
		assertEquals(visibility, member.getVisibility());
	}
}
