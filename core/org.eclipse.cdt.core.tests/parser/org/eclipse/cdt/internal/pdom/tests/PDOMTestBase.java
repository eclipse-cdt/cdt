/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Symbian - Fix a race condition (157992)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.PDOM;
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
		return createProject(folderName, false);
	}
	
	protected ICProject createProject(String folderName, final boolean cpp) throws CoreException {
		final ICProject cprojects[] = new ICProject[1];
		ModelJoiner mj= new ModelJoiner();
		try {
			// Create the project
			projectName = "ProjTest_" + System.currentTimeMillis();
			final File rootDir = CTestPlugin.getDefault().getFileInPlugin(rootPath.append(folderName));
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// Create the project
					ICProject cproject= cpp ? CProjectHelper.createCCProject(projectName, null, IPDOMManager.ID_NO_INDEXER)
							: CProjectHelper.createCProject(projectName, null, IPDOMManager.ID_NO_INDEXER);

					// Import the files at the root
					ImportOperation importOp = new ImportOperation(cproject.getProject().getFullPath(),
							rootDir, FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
						public String queryOverwrite(String pathString) {
							return IOverwriteQuery.ALL;
						}
					});
					importOp.setCreateContainerStructure(false);
					try {
						importOp.run(monitor);
					} catch (Exception e) {
						throw new CoreException(new Status(IStatus.ERROR, CTestPlugin.PLUGIN_ID, 0, "Import Interrupted", e));
					}

					cprojects[0] = cproject;
				}
			}, null);
			mj.join();
			// Index the project
			CCorePlugin.getIndexManager().setIndexerId(cprojects[0], IPDOMManager.ID_FAST_INDEXER);
			// wait until the indexer is done
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000, new NullProgressMonitor()));
		} finally {
			mj.dispose();
		}
		return cprojects[0];
	}

	protected int offset(String projectRelativePath, String lookfor) throws Exception, CoreException {
		Path path= new Path(projectName + "/" + projectRelativePath);
		return TestSourceReader.indexOfInFile(lookfor, path);
	}

	protected IBinding[] findQualifiedName(PDOM pdom, String name) throws CoreException {
		String[] segments = name.split("::");
		Pattern[] pattern = new Pattern[segments.length];
		for (int i = 0; i < segments.length; i++) {
			pattern[i] = Pattern.compile(segments[i]);
		}
		return pdom.findBindings(pattern, true, IndexFilter.ALL_DECLARED, PROGRESS);
	}
	
	protected IBinding[] findQualifiedPossiblyImplicit(PDOM pdom, String name) throws CoreException {
		String[] segments = name.split("::");
		Pattern[] pattern = new Pattern[segments.length];
		for (int i = 0; i < segments.length; i++) {
			pattern[i] = Pattern.compile(segments[i]);
		}
		return pdom.findBindings(pattern, true, IndexFilter.ALL, PROGRESS);
	}

	protected IBinding[] findUnqualifiedName(PDOM pdom, String name) throws CoreException {
		String[] segments = name.split("::");
		Pattern[] pattern = new Pattern[segments.length];
		for (int i = 0; i < segments.length; i++) {
			pattern[i] = Pattern.compile(segments[i]);
		}
		return pdom.findBindings(pattern, false, IndexFilter.ALL, PROGRESS);
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
	
	protected void assertNameCount(PDOM pdom, IBinding binding, int options, int count) throws CoreException {
		IName[] names = pdom.findNames(binding, options);
		assertUniqueNameCount(names, count);
	}

	/**
	 * Convenience method for checking how many unique instances (i.e. same
	 * offset within the same file) there are within an array of INames.
	 */
	private void assertUniqueNameCount(IName[] names, int count) {
		Set offsets = new HashSet();
		for (IName name : names) {
			offsets.add(name.getFileLocation());
		}
		assertEquals(count, offsets.size());
	}

	protected void assertType(PDOM pdom, String name, Class c) throws CoreException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		assertEquals(1, bindings.length);
		assertTrue(c.isAssignableFrom(bindings[0].getClass()));
	}

	protected void assertVisibility(PDOM pdom, String name, int visibility) throws CoreException, DOMException {
		IBinding[] bindings = findQualifiedName(pdom, name);
		assertEquals(1, bindings.length);
		ICPPMember member = (ICPPMember) bindings[0];
		assertEquals(visibility, member.getVisibility());
	}



	public static final void assertFunctionRefCount(PDOM pdom, Class[] args, IBinding[] bindingPool, int refCount) throws CoreException {
		IBinding[] bindings = findIFunctions(args, bindingPool);
		assertEquals(1, bindings.length);
		IName[] refs = pdom.findNames(bindings[0], IIndex.FIND_REFERENCES);
		assertEquals(refCount, refs.length);
	}

	// this is only approximate - composite types are not supported
	public static IBinding[] findIFunctions(Class[] paramTypes, IBinding[] bindings) throws CoreException {
		List preresult = new ArrayList();
		for (IBinding binding : bindings) {
			if(binding instanceof IFunction) {
				IFunction function = (IFunction) binding;
				IType[] candidate = function.getType().getParameterTypes();
				boolean areEqual = candidate.length == paramTypes.length;
				for(int j=0; areEqual && j<paramTypes.length; j++) {
					if(!paramTypes[j].isAssignableFrom(candidate[j].getClass())) {
						areEqual = false;
					}
				}
				if(areEqual) {
					preresult.add(binding);
				}
			}
		}
		return (IBinding[]) preresult.toArray(new IBinding[preresult.size()]);
	}

	protected void assertInstance(Object o, Class c) {
		assertNotNull(o);
		assertTrue("Expected "+c.getName()+" but got "+o.getClass().getName(), c.isInstance(o));
	}
	
	public static Pattern[] makePatternArray(String[] args) {
		List preresult = new ArrayList();
		for (String arg : args) {
			preresult.add(Pattern.compile(arg));
		}
		return (Pattern[]) preresult.toArray(new Pattern[preresult.size()]);
	}
}
