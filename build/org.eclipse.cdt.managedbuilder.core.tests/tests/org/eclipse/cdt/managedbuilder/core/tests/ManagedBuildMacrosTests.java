/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ManagedBuildMacrosTests extends BaseTestCase {
	static IProject proj = null;
	static IManagedProject mproj = null;

	IConfiguration[] cfgs = null;
	IBuildMacroProvider mp = null;
	IWorkspace worksp = null;

	boolean print = false; // allows to print most of macros on console
	boolean flag = false; // uplevel flag for getMacro/getMacros methods
	IBuildMacroSupplier[] ms = null;
	public static int functionCalled = 0;
	public static final int GET_ONE_PROJECT = 1;
	public static final int GET_MANY_PROJECT = 2;
	public static final int GET_ONE_CONFIG = 4;
	public static final int GET_MANY_CONFIG = 8;
	public static final int RESERVED_NAME = 16;

	static final String UNKNOWN = "<HZ>"; //$NON-NLS-1$
	static final String LISTSEP = "|"; //$NON-NLS-1$
	static final String LISTSEP_REGEX = "\\|"; //$NON-NLS-1$
	static final String TEST = "TEST"; //$NON-NLS-1$
	static final String[] TST = { "DUMMY", "FILETEST", //$NON-NLS-1$ //$NON-NLS-2$
			"OPTTEST", "CFGTEST", "PRJTEST", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"WSPTEST", "INSTEST", "ENVTEST" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// used for options testing
	static final String OPT_IDS = "macro.test.string"; //$NON-NLS-1$
	static final String OPT_IDL = "macro.test.list"; //$NON-NLS-1$
	static final String INC_DEF = "${IncludeDefaults}";//$NON-NLS-1$
	static final String PATH_ENV_VAR = "${PATH}"; //$NON-NLS-1$

	public ManagedBuildMacrosTests() {
		super();
	}

	public ManagedBuildMacrosTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildMacrosTests.class);
		return suite;
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.addResourceCreated(proj);
		ResourceHelper.cleanUp(getName());
		super.tearDown();
	}

	public void testMacroOptL() {
		doInit();
		ITool t = cfgs[0].getTools()[0];
		IOption opt = t.getOptionById(OPT_IDL);
		OptionContextData ocd = new OptionContextData(opt, t);
		assertNotNull(opt);
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_OPTION, ocd);
		assertNotNull(ms);
		assertEquals(ms.length, 1);

		try {
			String[] set0 = opt.getStringListValue();
			assertNotNull(set0);
			final String[] set1 = { "new a", /*"test=${TEST}",*/ INC_DEF, "PRJ=${NEW_FOR_PRJ}", "LIST=" + INC_DEF,
					PATH_ENV_VAR };
			String[] resArr1 = { "new a", /*"test=CFGTEST",*/ "x", "y", "z", "PRJ=NewMacrosForProjectContext",
					"LIST=x|y|z" };
			List<String> res1 = new ArrayList<>(Arrays.asList(resArr1));
			try {
				// Add split ${PATH} to res1
				String strList = mp.resolveValue(PATH_ENV_VAR, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_OPTION,
						ocd);
				String[] split = strList.split(LISTSEP_REGEX);
				res1.addAll(Arrays.asList(split));
			} catch (BuildMacroException e) {
				fail(e.getLocalizedMessage());
			}

			opt = cfgs[0].setOption(t, opt, set1);
			assertNotNull(opt);

			ArrayList<String> res2 = new ArrayList<>(res1.size());
			for (int i = 0; i < set1.length; i++) {
				try {
					String[] aus = mp.resolveStringListValue(set1[i], UNKNOWN, LISTSEP,
							IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(opt, t));
					if (aus == null)
						continue;
					for (int j = 0; j < aus.length; j++)
						res2.add(aus[j]);
				} catch (BuildMacroException e) {
					fail(e.getLocalizedMessage());
				}
			}
			assertEquals(res1.size(), res2.size());
			for (int i = 0; i < res1.size(); i++)
				assertEquals(res1.get(i), res2.get(i));
		} catch (BuildException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * testMacroContext()
	 */
	public void rm_testMacroContext() {
		doInit();
		IBuildMacro mcfg = mp.getMacro(TEST, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0], true);
		IBuildMacro mprj = mp.getMacro(TEST, IBuildMacroProvider.CONTEXT_PROJECT, mproj, true);
		IBuildMacro mwsp = mp.getMacro(TEST, IBuildMacroProvider.CONTEXT_WORKSPACE, worksp, true);
		assertNotNull(mcfg);
		assertNotNull(mprj);
		assertNotNull(mwsp);
		try {
			assertEquals(mcfg.getStringValue(), TST[IBuildMacroProvider.CONTEXT_CONFIGURATION]);
			assertEquals(mprj.getStringValue(), TST[IBuildMacroProvider.CONTEXT_PROJECT]);
			assertEquals(mwsp.getStringValue(), TST[IBuildMacroProvider.CONTEXT_WORKSPACE]);
		} catch (BuildMacroException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * testMacroResolveExceptions()
	 */
	public void testMacroResolveExceptions() {
		doInit();

		boolean exceptionRaised = false;
		try { // ZERO is undefined macro
			mp.resolveValue("${ONE} - ${ZERO}", null, null, //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
		} catch (BuildMacroException e) {
			exceptionRaised = true;
		}
		assertTrue("Exception not raised for undefined macro", exceptionRaised); //$NON-NLS-1$

		exceptionRaised = false;
		try { // delimiter is undefined for list
			mp.resolveValue("${LST}", null, null, //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
		} catch (BuildMacroException e) {
			exceptionRaised = true;
		}
		assertTrue("Exception not raised for undefined delimiter", exceptionRaised); //$NON-NLS-1$
	}

	/*
	 * Below are service methods
	 */
	//TODO: comments for all methods

	// returns a list of macro's NAMES (not values).
	private String[] printMacros(IBuildMacro[] vars, String head) {
		ArrayList<String> ar = new ArrayList<>(0);
		if (vars != null) {
			if (vars.length > 0) {
				for (int i = 0; i < vars.length; i++) {
					try {
						ar.add(vars[i].getName());
						if (!print)
							continue;
						if ((vars[i].getMacroValueType() % 2) == 1) // not-list
							//if (vars[i] instanceof EclipseVarMacro) {
							if (vars[i].getName().endsWith("prompt")) { //$NON-NLS-1$
								System.out.println(head + "[" + i + "] " + //$NON-NLS-1$  //$NON-NLS-2$
										vars[i].getName() + " = <UNREACHABLE>"); //$NON-NLS-1$
							} else {
								System.out.println(head + "[" + i + "] " + //$NON-NLS-1$ //$NON-NLS-2$
										vars[i].getName() + " = " + vars[i].getStringValue()); //$NON-NLS-1$
							}
						else {
							System.out.println(head + "[" + i + "] " + //$NON-NLS-1$ //$NON-NLS-2$
									vars[i].getName() + ":"); //$NON-NLS-1$
							String[] m = vars[i].getStringListValue();
							printStrings(m, "    "); //$NON-NLS-1$
						}
					} catch (Exception e) {
					}
				}
			} else {
				if (print)
					System.out.println(head + ": array is empty"); //$NON-NLS-1$
			}
		} else {
			if (print)
				System.out.println(head + ": array is null"); //$NON-NLS-1$
		}
		return ar.toArray(new String[0]);
	}

	private void printStrings(String[] vars, String head) {
		if (!print)
			return;
		if (vars != null) {
			if (vars.length > 0) {
				for (int j = 0; j < vars.length; j++)
					System.out.println(head + vars[j]);
			} else {
				System.out.println(head + ": array is empty"); //$NON-NLS-1$
			}
		} else {
			System.out.println(head + ": array is null"); //$NON-NLS-1$
		}
	}

	/* Create new project or get existing one
	 *
	 * Sets "proj" "mproj" class variables
	 */

	static void createManagedProject(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		proj = root.getProject(name);
		if (proj.exists()) {
			mproj = ManagedBuildManager.getBuildInfo(proj).getManagedProject();
		} else {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			try {
				workspace.setDescription(workspaceDesc);
				proj = CCorePlugin.getDefault().createCProject(workspace.newProjectDescription(proj.getName()), proj,
						new NullProgressMonitor(), ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID);

				// 	add ManagedBuildNature
				IManagedBuildInfo info = ManagedBuildManager.createBuildInfo(proj);
				info.setValid(true);
				ManagedCProjectNature.addManagedNature(proj, null);
				ManagedCProjectNature.addManagedBuilder(proj, null);

				ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(proj, true);
				desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
				desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
				desc.saveProjectData();
			} catch (CoreException e) {
				fail("Cannot create project: " + e.getLocalizedMessage()); //$NON-NLS-1$
			}

			// Call this function just to avoid init problems in getProjectType();
			IProjectType[] projTypes = ManagedBuildManager.getDefinedProjectTypes();
			IProjectType projType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testenv.exe"); //$NON-NLS-1$
			assertNotNull(projType);
			try {
				mproj = ManagedBuildManager.createManagedProject(proj, projType);
			} catch (BuildException e) {
			}
			ManagedBuildManager.setNewProjectVersion(proj);
			IConfiguration[] cfgs = projType.getConfigurations();
			IConfiguration defcfg = cfgs.length > 0 ? mproj.createConfiguration(cfgs[0], projType.getId() + ".0") //$NON-NLS-1$
					: null;
			for (int i = 1; i < cfgs.length; ++i) { // sic ! from 1
				mproj.createConfiguration(cfgs[i], projType.getId() + "." + i); //$NON-NLS-1$
			}
			ManagedBuildManager.setDefaultConfiguration(proj, defcfg);
		}
		// open project w/o progress monitor; no action performed if it's opened
		try {
			proj.open(null);
		} catch (CoreException e) {
		}
	}

	/**
	 *  doInit() - call it at the beginning of every test
	 *
	 */
	private void doInit() {
		createManagedProject("Test"); //$NON-NLS-1$
		assertNotNull(proj);
		assertNotNull(mproj);
		worksp = proj.getWorkspace();
		assertNotNull(worksp);
		mp = ManagedBuildManager.getBuildMacroProvider();
		assertNotNull(mp);
		cfgs = mproj.getConfigurations();
		assertNotNull(cfgs);
	}

	/**
	 *      arrayContains
	 * check that ALL variables from list a have correspondence in list b
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean arrayContains(String[] a, String[] b) {
		assertNotNull(a);
		assertNotNull(b);
		for (int i = 0; i < a.length; i++) {
			boolean found = false;
			for (int j = 0; j < b.length; j++) {
				if (a[i].equals(b[j])) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	/*
	 * getFile() - open or creates sample file in current project
	 */
	private IFile getFile() {
		final String FILENAME = "main.c"; //$NON-NLS-1$
		final String FILETEXT = "int main(){\n return 0;\n}"; //$NON-NLS-1$

		IFile f = proj.getProject().getFile(FILENAME);
		if (!f.exists())
			try {
				f.create(new ByteArrayInputStream(FILETEXT.getBytes()), false, null);
			} catch (CoreException e) {
				fail(e.getLocalizedMessage());
			}
		return f;
	}
}
