/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.envvar.EnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.UserDefinedEnvironmentSupplier;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.UserDefinedMacroSupplier;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ManagedBuildMacrosTests extends TestCase {
	static IProject proj = null;
	static IManagedProject mproj = null;
	
	IConfiguration[] cfgs = null;
	IBuildMacroProvider mp = null;
	IWorkspace worksp = null;
	boolean windows = false;
	
	boolean print = false; // allows to print most of macros on console
	boolean flag  = false; // uplevel flag for getMacro/getMacros methods 
	IBuildMacroSupplier[] ms = null;
	public static int functionCalled = 0;
	public static final int GET_ONE_PROJECT  = 1;
	public static final int GET_MANY_PROJECT = 2;
	public static final int GET_ONE_CONFIG   = 4;
	public static final int GET_MANY_CONFIG  = 8;
	public static final int RESERVED_NAME    = 16;
	
	static final String UNKNOWN = "<HZ>"; //$NON-NLS-1$
	static final String LISTSEP = "|";    //$NON-NLS-1$
	static final String TEST = "TEST";    //$NON-NLS-1$
	static final String[] TST = {"DUMMY", "FILETEST",   //$NON-NLS-1$ //$NON-NLS-2$
		"OPTTEST", "CFGTEST", "PRJTEST",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		"WSPTEST", "INSTEST", "ENVTEST"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// used for options testing
	final String OPT_IDS = "macro.test.string";  //$NON-NLS-1$
	final String OPT_IDL = "macro.test.list";    //$NON-NLS-1$
	final String INC_DEF  = "${IncludeDefaults}";//$NON-NLS-1$
	
	public ManagedBuildMacrosTests() { super(); }
	public ManagedBuildMacrosTests(String name) { super(name); }

	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildMacrosTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(new ManagedBuildMacrosTests("testMacroConf"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroEEnv"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroInst"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroProj"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroWrks"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroOptS"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroOptL"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroFile"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroContext"));//$NON-NLS-1$
		
		suite.addTest(new ManagedBuildMacrosTests("testMacroResolve"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroResolveExceptions"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroResolveLoop"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroResolveMake"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroResolveCase"));//$NON-NLS-1$
		suite.addTest(new ManagedBuildMacrosTests("testMacroSave"));//$NON-NLS-1$
		//$JUnit-END$
		return suite;
	}

	/**
	 * testMacroConf()
	 */
	public void testMacroConf(){
		doInit();
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
		assertNotNull(ms);
		assertEquals(ms.length, 4);
		assertTrue(addMacro(TEST, IBuildMacro.VALUE_TEXT, TST[IBuildMacroProvider.CONTEXT_CONFIGURATION],
				IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]));
		functionCalled = 0;
		String[] a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0], flag), "Configuration"); //$NON-NLS-1$
		assertEquals(GET_MANY_CONFIG, functionCalled);
		String[] b = {"ConfigName", "BuildArtifactFileExt", //$NON-NLS-1$ //$NON-NLS-2$
				"BuildArtifactFileBaseName", "TargetArchList", //$NON-NLS-1$ //$NON-NLS-2$
				"TargetOsList", "BuildArtifactFileName", //$NON-NLS-1$ //$NON-NLS-2$
				"PWD", "CWD", "ConfigDescription", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
				TEST, "NEW_FOR_CFG" }; //$NON-NLS-1$
		assertTrue(arrayContains(b, a));
	}

	/**
	 * testMacroEEnv()
	 */
	public void testMacroEEnv(){
		doInit();
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_ECLIPSEENV, null);
		assertNotNull(ms);
		assertEquals(ms.length, 1);
		assertFalse(addMacro(TEST, IBuildMacro.VALUE_TEXT, TST[IBuildMacroProvider.CONTEXT_ECLIPSEENV],
				IBuildMacroProvider.CONTEXT_ECLIPSEENV, null));
		String[] a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_ECLIPSEENV, null, flag), "EclipseEnv"); //$NON-NLS-1$		
//		String[] b = {"PATH", "USERNAME"}; //$NON-NLS-1$ //$NON-NLS-2$
//		assertTrue(arrayContains(b, a));
	}

	/**
	 * testMacroInst()
	 */
	public void testMacroInst(){
		doInit();
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_INSTALLATIONS, null);
		assertNotNull(ms);
		assertEquals(ms.length, 1);
		assertFalse(addMacro(TEST, IBuildMacro.VALUE_TEXT, TST[IBuildMacroProvider.CONTEXT_INSTALLATIONS],
				IBuildMacroProvider.CONTEXT_INSTALLATIONS, null));
		String[] a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_INSTALLATIONS, null, flag), "Installations "); //$NON-NLS-1$
		String[] b = {"HostArchName", "MBSVersion", //$NON-NLS-1$ //$NON-NLS-2$ 
				"EclipseVersion", "HostOsName", "CDTVersion"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(arrayContains(b, a));
	}

	/**
	 * testMacroProj()
	 */
	public void testMacroProj(){
		doInit();
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_PROJECT, mproj);
		assertNotNull(ms);
		assertEquals(ms.length, 4);
		assertTrue(addMacro(TEST, IBuildMacro.VALUE_TEXT, TST[IBuildMacroProvider.CONTEXT_PROJECT],
				IBuildMacroProvider.CONTEXT_PROJECT, mproj));
		functionCalled = 0;
		String[] a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_PROJECT, mproj, flag), "Project "); //$NON-NLS-1$
		assertEquals(GET_MANY_PROJECT, functionCalled);
		String[] b = {"ProjDirPath", "ProjName", //$NON-NLS-1$ //$NON-NLS-2$ 
				TEST, "NEW_FOR_PRJ"};          //$NON-NLS-1$
		assertTrue(arrayContains(b, a));
	}
	
	/**
	 * testMacroWrks()
	 */
	public void testMacroWrks(){
		doInit();
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_WORKSPACE, worksp);
		assertNotNull(ms);
		assertEquals(ms.length, 5);
		assertTrue(addMacro(TEST, IBuildMacro.VALUE_TEXT, TST[IBuildMacroProvider.CONTEXT_WORKSPACE],
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));
		String[] a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_WORKSPACE, worksp, flag), "Workspace "); //$NON-NLS-1$
		String[] b = {"DirectoryDelimiter", "WorkspaceDirPath", //$NON-NLS-1$ //$NON-NLS-2$ 
				"PathDelimiter", TEST};                       //$NON-NLS-1$
		assertTrue(arrayContains(b, a));
	}
	
	/**
	 * testMacroOptn()
	 */
	public void testMacroOptS(){
		doInit();
		IToolChain tc = cfgs[0].getToolChain();
		ITool       t = cfgs[0].getTools()[0];
		IOption   opt = t.getOptionById(OPT_IDS);
		assertNotNull(opt);

		// standard check of suppliers # and attempt to add macro (should fail) 
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(opt,tc));
		assertNotNull(ms);
		assertEquals(ms.length, 1);
		assertFalse(addMacro(TEST, IBuildMacro.VALUE_TEXT, TST[IBuildMacroProvider.CONTEXT_OPTION], IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(opt,t)));

		// modify value and check that macros is resolved 
		try {
			opt = cfgs[0].setOption(t, opt, "222 " + INC_DEF);  //$NON-NLS-1$
			String a = mp.resolveValue(opt.getStringValue(), UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(opt,tc));
			assertEquals(a, "222 111");  //$NON-NLS-1$
		} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }
		  catch (BuildException e) { fail(e.getLocalizedMessage()); }
		  
		// Create resource configuration
		IResourceConfiguration rc = cfgs[0].createResourceConfiguration(getFile());
		assertNotNull(rc);
		IOption ropt = rc.getTools()[0].getOptionById(OPT_IDS);
		try {
			ropt = rc.setOption(rc.getTools()[0], ropt, "333 " + INC_DEF);  //$NON-NLS-1$
			String a = mp.resolveValue(ropt.getStringValue(), UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(opt,tc));
			assertEquals(a, "333 111");  //$NON-NLS-1$
		} catch (Exception e) { fail(e.getLocalizedMessage());	}
	}
	
	
	public void testMacroOptL(){
		doInit();
		IToolChain tc = cfgs[0].getToolChain();
		ITool       t = cfgs[0].getTools()[0];
		IOption   opt = t.getOptionById(OPT_IDL);
		OptionContextData ocd = new OptionContextData(opt,tc);
		assertNotNull(opt);
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_OPTION, ocd);
		assertNotNull(ms);
		assertEquals(ms.length, 1);
		
		try {
			String[] set0 = opt.getStringListValue();
			assertNotNull(set0);
			final String[] set1 = {"new a", "test=${TEST}", INC_DEF, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
				 "${PATH}", "PRJ=${NEW_FOR_PRJ}", "LIST=" + INC_DEF};//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String[] res1 = {"new a", "test=CFGTEST", "x", "y",      //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					     "z", ":", "PRJ=<HZ>", "LIST=x|y|z"};        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			try {
				res1[5] = mp.resolveValue("${PATH}",UNKNOWN,LISTSEP,IBuildMacroProvider.CONTEXT_OPTION, ocd);  //$NON-NLS-1$
			} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); } 

			opt = cfgs[0].setOption(t, opt, set1);
			assertNotNull(opt);

			ArrayList ar = new ArrayList(1);
			for (int i=0; i<set1.length; i++) {
				try {
					String[] aus = mp.resolveStringListValue(set1[i], UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_OPTION, new OptionContextData(opt,tc));
					if (aus == null) continue;
					for (int j=0; j<aus.length; j++) ar.add(aus[j]);
				} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); } 
			}
			String[] res = (String[])ar.toArray(new String[0]);
			assertEquals(res.length, res1.length);
			for (int i=0; i<res.length; i++) assertEquals(res[i], res1[i]);
		} catch (BuildException e) { fail(e.getLocalizedMessage());	}
	}
	
	/**
	 * testMacroFile()
	 */
	public void testMacroFile(){
		final String EIN = "ein.c";     //$NON-NLS-1$
		final String AUS = "aus.o";     //$NON-NLS-1$
		final String UP2W = "..\\..\\"; //$NON-NLS-1$
		final String UP2U = "../../";   //$NON-NLS-1$
		final String KLMN = "\\k\\l\\m\\n\\o\\p\\";  //$NON-NLS-1$
		final String[] names = 
		{"InputFileName", "InputFileExt", "InputFileBaseName",   //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
		 "InputFileRelPath", "InputDirRelPath",                  //$NON-NLS-1$ //$NON-NLS-2$ 
		 "OutputFileName", "OutputFileExt", "OutputFileBaseName",//$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$  
		 "OutputFileRelPath", "OutputDirRelPath"};               //$NON-NLS-1$ //$NON-NLS-2$
		String[] values0wAbs = 
		{"a.f77", "f77", "a", "\\xz\\a.f77", "\\xz\\",    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		 "b.exe", "exe", "b", "\\tmp\\b.exe", "\\tmp\\"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		final String[] values0wRel = 
		{EIN, "c", "ein", UP2W+EIN, UP2W,  //$NON-NLS-1$ //$NON-NLS-2$
		 AUS, "o", "aus", UP2W+AUS, UP2W}; //$NON-NLS-1$ //$NON-NLS-2$
		
		final String[] values0u = 
		{EIN, "c", "ein", UP2U+EIN, UP2U,  //$NON-NLS-1$ //$NON-NLS-2$
		 AUS, "o", "aus", UP2U+AUS, UP2U}; //$NON-NLS-1$ //$NON-NLS-2$
		
		final String[] values1 = 
		{"$(notdir $<)", "$(suffix $(notdir $<))",       //$NON-NLS-1$ //$NON-NLS-2$
		 "$(basename $(notdir $<))", "$<", "$(dir $<)",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		 "$(notdir $@)", "$(suffix $(notdir $@))",       //$NON-NLS-1$ //$NON-NLS-2$
		 "$(basename $(notdir $@))", "$@", "$(dir $@)"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		FileContextData fd = null;
		
		doInit();
		ITool t = cfgs[0].getTools()[0];
		assertNotNull(t);
		IOption opt = t.getOptionById(OPT_IDS);
			
		String dir=null;
		try {
			dir = mp.getMacro("WorkspaceDirPath", IBuildMacroProvider.CONTEXT_WORKSPACE, worksp, flag).getStringValue(); //$NON-NLS-1$
		} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }
		IPath p = (new Path(dir)).addTrailingSeparator();
		
		if (windows) {
			// check behaviour in case of different disks usage
			
			// config #4 has changed BuilderMakeFileGenerator, #0 has standard one
			IBuildEnvironmentVariable cwdvar = ManagedBuildManager.getEnvironmentVariableProvider().getVariable("CWD", cfgs[0], false, true); //$NON-NLS-1$
			String dev0 = Path.fromOSString(cwdvar.getValue()).getDevice().toUpperCase();
			String dev1 = (dev0.startsWith("C")) ? "D:" : "C:";  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$  
			values0wAbs[3] = dev1 + values0wAbs[3];		
			values0wAbs[4] = dev1 + values0wAbs[4];		
			values0wAbs[8] = dev1 + values0wAbs[8];
			values0wAbs[9] = dev1 + values0wAbs[9];
			
			fd = new FileContextData(new Path(values0wAbs[3]), new Path(values0wAbs[8]),opt,cfgs[0].getToolChain());
			for (int i=0; i<names.length; i++) 
			try {	
				assertEquals(values0wAbs[i], mp.getMacro(names[i], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
			} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }
			
			// check that relative path are reported OK
			fd = new FileContextData(p.append(EIN), p.append(AUS),opt,cfgs[0].getToolChain());
			for (int i=0; i<names.length; i++) 
			try {
				assertEquals(values0wRel[i], mp.getMacro(names[i], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
			} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }

			//TODO:
			// check paths using changed BuilderMakeFileGenerator in cfg "Five"
/*			
			int index = 4;
			ManagedBuildManager.setDefaultConfiguration(proj, cfgs[index]);
			OptionContextData op = new OptionContextData(cfgs[index].getTools()[0].getOptions()[0], cfgs[index].getToolChain());
			String p0 = dev0 + KLMN;
			fd = new FileContextData(new Path(p0+EIN), new Path(p0+AUS), op);
			assertNotNull(fd);
			//TODO: the same dir, upper dir, lower dir 
			try {
				TestMacro.topBuildDir = Path.fromOSString(p0);
				assertEquals(p0+EIN, mp.getMacro(names[3], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
				assertEquals(p0,     mp.getMacro(names[4], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
				assertEquals(p0+AUS, mp.getMacro(names[8], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
				assertEquals(p0,     mp.getMacro(names[9], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
//				p0 = Path.fromOSString(p0).removeLastSegments(2).addTrailingSeparator().toOSString();
//				p0 = dev0+KLMN+"x\\";
//				TestMacro.topBuildDir = Path.fromOSString(p0);
				assertEquals(p0+EIN, mp.getMacro(names[3], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
				assertEquals(p0,     mp.getMacro(names[4], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
				assertEquals(p0+AUS, mp.getMacro(names[8], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
				assertEquals(p0,     mp.getMacro(names[9], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
			} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }
//				*/				
			
			
		} else {
			// check relative path only
			fd = new FileContextData(p.append(EIN), p.append(AUS),opt,cfgs[0].getToolChain());
			for (int i=0; i<names.length; i++) 
			try {	
				assertEquals(values0u[i], mp.getMacro(names[i], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
			} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }
		}
		
		// check supplier's parameters
		assertNotNull(fd);
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_FILE, fd);
		assertNotNull(ms);
		assertEquals(ms.length, 1);
		assertFalse(addMacro(TEST, IBuildMacro.VALUE_TEXT, 
				TST[IBuildMacroProvider.CONTEXT_FILE], IBuildMacroProvider.CONTEXT_FILE, fd));

		// For config #3, macros should contain lines specified in plugin.xml
		opt = cfgs[3].getTools()[0].getOptions()[0];		
		fd = new FileContextData(p.append(EIN), p.append(AUS),opt,cfgs[1].getToolChain());
		for (int i=0; i<names.length; i++) 
		try {
			assertEquals(values1[i], mp.getMacro(names[i], IBuildMacroProvider.CONTEXT_FILE, fd, flag).getStringValue());
		} catch (BuildMacroException e) { fail(e.getLocalizedMessage()); }
	}
	
	
	
	
	/**
	 * testMacroContext()
	 */
	public void testMacroContext(){
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
	 * testMacroResolve()
	 */
	public void testMacroResolve(){
		doInit();		
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_WORKSPACE, worksp);
		assertNotNull(ms);
		String[] lst = {"SCHEISE", "MERDE", "SHIT"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(addMacro("LST", IBuildMacro.VALUE_TEXT_LIST, lst, //$NON-NLS-1$
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));		
		 assertTrue(addMacro("ONE", IBuildMacro.VALUE_TEXT, "EIN", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));
		// 
		assertTrue(addMacro("L1", IBuildMacro.VALUE_TEXT, "nested L1-${L2}-L1", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));
		assertTrue(addMacro("L2", IBuildMacro.VALUE_TEXT, "L2-${L3}-L2", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));
		assertTrue(addMacro("L3", IBuildMacro.VALUE_TEXT, "L3-${L4}-L3", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp)); 
		assertTrue(addMacro("L4", IBuildMacro.VALUE_TEXT, "L4", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));
				
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_PROJECT, mproj);
		assertNotNull(ms);
		assertTrue(addMacro("TWO", IBuildMacro.VALUE_TEXT, "ZWEI", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_PROJECT, mproj));
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
		assertNotNull(ms);
		assertTrue(addMacro("three", IBuildMacro.VALUE_TEXT, "DREI", //$NON-NLS-1$ //$NON-NLS-2$
				IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0])); 
		
		
		// check normal workflow
		try {
			final String pattern = "${ONE} - ${TWO} - ${three} -> ${LST}"; //$NON-NLS-1$
			String a = mp.resolveValue(pattern, UNKNOWN, LISTSEP,
				IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
			String b = mp.resolveValue(pattern, UNKNOWN, LISTSEP,
					IBuildMacroProvider.CONTEXT_PROJECT, mproj);
			String c = mp.resolveValue(pattern, UNKNOWN, LISTSEP,
					IBuildMacroProvider.CONTEXT_WORKSPACE, worksp);
			String d = mp.resolveValue("${L1}", UNKNOWN, LISTSEP, //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_WORKSPACE, worksp);
			String e = mp.resolveValue("${one} - ${Two} - ${THREE} -> ${lst}", UNKNOWN, LISTSEP, //$NON-NLS-1$
					IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
			
			assertEquals(a, "EIN - ZWEI - DREI -> SCHEISE|MERDE|SHIT"); //$NON-NLS-1$
			assertEquals(b, "EIN - ZWEI - <HZ> -> SCHEISE|MERDE|SHIT"); //$NON-NLS-1$
			assertEquals(c, "EIN - <HZ> - <HZ> -> SCHEISE|MERDE|SHIT"); //$NON-NLS-1$
			assertEquals(d, "nested L1-L2-L3-L4-L3-L2-L1"); //$NON-NLS-1$
			assertEquals(e, "<HZ> - <HZ> - <HZ> -> <HZ>");  //$NON-NLS-1$  
		} catch (BuildMacroException e) {
			fail("Exception while resolving: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
	
	/**
	 * testMacroResolveExceptions()
	 */
	public void testMacroResolveExceptions () {
		doInit();		

		boolean exceptionRaised = false;
		try { // ZERO is undefined macro
			String a = mp.resolveValue("${ONE} - ${ZERO}", null, null,  //$NON-NLS-1$
				IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
		} catch (BuildMacroException e) {
			exceptionRaised = true;
		}
		assertTrue("Exception not raised for undefined macro", exceptionRaised);  //$NON-NLS-1$
		
		exceptionRaised = false;
		try { // delimiter is undefined for list
			String a = mp.resolveValue("${LST}", null, null,  //$NON-NLS-1$
				IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
		} catch (BuildMacroException e) {
			exceptionRaised = true;
		}
		assertTrue("Exception not raised for undefined delimiter", exceptionRaised);  //$NON-NLS-1$
	}
	
	/**
	 * testMacroResolveLoop()
	 */
	public void testMacroResolveLoop() {
		doInit();
		int ctx = IBuildMacroProvider.CONTEXT_WORKSPACE;
		Object obj = worksp;
		ms = mp.getSuppliers(ctx, obj);
		assertNotNull(ms);
		
		// check state before macros added (should be OK)
		try {
			mp.checkIntegrity(ctx, obj);
		} catch (BuildMacroException e) {
			fail("Macros integrity check is failed");  //$NON-NLS-1$
		}

		// create macro which references to undefined macro 	
		assertTrue(addMacro("B1", IBuildMacro.VALUE_TEXT, "B1-${B2}", ctx, obj)); //$NON-NLS-1$ //$NON-NLS-2$
		rmMacro("B2", ctx, obj); // usually it does not exist, but to be sure...  //$NON-NLS-1$

		// check state after macro added (should be exception)
		try {
			mp.checkIntegrity(ctx, obj);
			fail("Macros misintegrity (ref to undefined) is not detected");  //$NON-NLS-1$
		} catch (BuildMacroException e) {}
		
		// create "dead loop" of nested macros
		assertTrue(addMacro("B2", IBuildMacro.VALUE_TEXT, "B2-${B3}", ctx, obj));  //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(addMacro("B3", IBuildMacro.VALUE_TEXT, "B3-${B1}", ctx, obj));  //$NON-NLS-1$ //$NON-NLS-2$
		
		// check state after macros added (should be exception)
		try {
			mp.checkIntegrity(ctx, obj);
			fail("Macros misintegrity (dead loop) is not detected");  //$NON-NLS-1$
		} catch (BuildMacroException e) {}
		
		// remove "dead loop" of nested macros
		assertTrue(rmMacro("B1", ctx, obj)); //$NON-NLS-1$
		assertTrue(rmMacro("B2", ctx, obj)); //$NON-NLS-1$
		assertTrue(rmMacro("B3", ctx, obj)); //$NON-NLS-1$
		
		// check state after macros removed (should be OK)
		try {
			mp.checkIntegrity(ctx, obj);
		} catch (BuildMacroException e) {
			fail("Macros integrity check is failed " + e.getLocalizedMessage());  //$NON-NLS-1$
		}
	}
	/**
	 * testMacroResolveMake()
	 */
	public void testMacroResolveMake(){
		final String p1 = "USERNAME: ";    //$NON-NLS-1$
		final String p2 = "${USERNAME} ";  //$NON-NLS-1$
		final String p3 = "PATH: ";        //$NON-NLS-1$  
		final String p4 = "${PATH} ";      //$NON-NLS-1$
		final String p5 = "HostOsName: ${HostOsName} WorkspaceDirPath: ${WorkspaceDirPath}";  //$NON-NLS-1$ 
		final String ein1 = p1 + p2 + p3;
		final String ein2 = p4 + p5;
		final String ein = ein1 + ein2;
		final String aus1 = "@USERNAME ";  //$NON-NLS-1$
		final String aus2 = "@PATH ";      //$NON-NLS-1$
		doInit();
		// Config #0 contains "variableFormat" macro = "@=". Result: 2 first macros NOT resolved 
		try {
			UserDefinedEnvironmentSupplier env = EnvironmentVariableProvider.fUserSupplier;
			env.createVariable("PATH","",IBuildEnvironmentVariable.ENVVAR_PREPEND,null,worksp);
			env.createVariable("USERNAME","",IBuildEnvironmentVariable.ENVVAR_PREPEND,null,worksp);
			functionCalled = 0;
			String a = mp.resolveValueToMakefileFormat(ein, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
			String b = p1 + aus1 + p3 + mp.resolveValue(ein2, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[0]);
			assertEquals(a, b); // Env var names should not be resolved but converted to Makefile format			
			a = mp.resolveValueToMakefileFormat(ein, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_PROJECT, mproj);
			assertEquals(a, b); // Project context should return the same as default configuration
		} catch (BuildMacroException e) {
			fail(e.getLocalizedMessage());
		}
		// Config #1 does not contain "variableFormat" macro. Result: all macros resolved.
		try {
			String a = mp.resolveValue(ein, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[1]);
			String b = mp.resolveValueToMakefileFormat(ein, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[1]);
			assertEquals(a, b);
		} catch (BuildMacroException e) {
			fail(e.getLocalizedMessage());
		}
		// check that "isReservedName" was not called before
		assertEquals(functionCalled & RESERVED_NAME, 0);

		// Config #2 contains "...Supplier" macro. Result: PATH unresolved, USERNAME resolved.
		try {
			String a = mp.resolveValue(p1 + p2, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[2]);
			String b = mp.resolveValue(p5, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[2]);
			a = a + p3 + aus2 + b; // USERNAME: xxx PATH: @PATH HostOsName: xxx ...
			b = mp.resolveValueToMakefileFormat(ein, UNKNOWN, LISTSEP, IBuildMacroProvider.CONTEXT_CONFIGURATION, cfgs[2]);
			assertEquals(a, b);
			// check that "isReservedName" has been called
			assertEquals(functionCalled & RESERVED_NAME, RESERVED_NAME);
		} catch (BuildMacroException e) {
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * testMacroResolveCase()
	 */
	public void testMacroResolveCase(){
		doInit();
		addVars();
		final String winOut1 = "@CASETEST uppercase uppercase uppercase";   //$NON-NLS-1$
		final String winOut2 = "@CASETEST @CASETEST @CASETEST @CASETEST";   //$NON-NLS-1$
		
		final String unixOut1 = "@CASETEST capitalize lowercase upper2low"; //$NON-NLS-1$
		final String unixOut2 = "@CASETEST @CaseTest @casetest @CaSeTeSt";  //$NON-NLS-1$
		
		final String ein = "${CASETEST} ${CaseTest} ${casetest} ${CaSeTeSt}"; //$NON-NLS-1$
		final int ctx = IBuildMacroProvider.CONTEXT_CONFIGURATION;
		String a=null, b=null; 
		try {
			// Config #0 contains isVariableCaseSensitive = false  
			a = mp.resolveValueToMakefileFormat(ein, UNKNOWN, LISTSEP, ctx, cfgs[0]);
			// Config #3 contains isVariableCaseSensitive = true
			b = mp.resolveValueToMakefileFormat(ein, UNKNOWN, LISTSEP, ctx, cfgs[3]);
		} catch (BuildMacroException e) {
			fail(e.getLocalizedMessage());
		}
		if (windows) {
			assertEquals(a, winOut1);
			assertEquals(b, winOut2);
		} else { // linux
			assertEquals(a, unixOut1);
			assertEquals(b, unixOut2);
		}
	}

	/**
	 * testMacroSave()
	 */
	
	public void testMacroSave(){
		final String TO_SAVE_P = "TO_SAVE_P";  //$NON-NLS-1$
		final String TO_SAVE_W = "TO_SAVE_W";  //$NON-NLS-1$
		doInit();		
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_PROJECT, mproj);
		assertNotNull(ms);
		assertTrue(addMacro(TO_SAVE_P, IBuildMacro.VALUE_TEXT, TO_SAVE_P,
				IBuildMacroProvider.CONTEXT_PROJECT, mproj));
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_WORKSPACE, worksp);
		assertNotNull(ms);
		assertTrue(addMacro(TO_SAVE_W, IBuildMacro.VALUE_TEXT, TO_SAVE_W,
				IBuildMacroProvider.CONTEXT_WORKSPACE, worksp));
		try {
			// Save the buildinfo, and then remove it, to be complete
			ManagedBuildManager.saveBuildInfo(proj, true);
			ManagedBuildManager.removeBuildInfo(proj);
			proj.close(null);
			proj.open(null);
		} catch (CoreException e) {
			fail("Failed on project close/open: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_PROJECT, mproj);
		assertNotNull(ms);
		String[] a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_PROJECT, mproj, flag), TO_SAVE_P);
		String[] b1 = {TO_SAVE_P};
		assertTrue(arrayContains(b1, a));

		ms = mp.getSuppliers(IBuildMacroProvider.CONTEXT_WORKSPACE, worksp);
		assertNotNull(ms);
		a = printMacros(mp.getMacros(IBuildMacroProvider.CONTEXT_WORKSPACE, worksp, flag), TO_SAVE_W);
		String[] b2 = {TO_SAVE_W};
		assertTrue(arrayContains(b2, a));
	}	
	
/*
 * Below are service methods 
 */	
	//TODO: comments for all methods
	
	// returns a list of macro's NAMES (not values).
	private String[] printMacros(IBuildMacro[] vars, String head) {
		ArrayList ar = new ArrayList(0);
		if (vars != null) {
			if (vars.length > 0) {
				for (int i=0; i < vars.length; i++) {
					try {
						ar.add(vars[i].getName());
						if (!print) continue;
						if ((vars[i].getMacroValueType() % 2) == 1) // not-list
							//if (vars[i] instanceof EclipseVarMacro) {
							if (vars[i].getName().endsWith("prompt")) {  //$NON-NLS-1$
								System.out.println(head + "[" + i + "] " +  //$NON-NLS-1$  //$NON-NLS-2$
									vars[i].getName() + " = <UNREACHABLE>");  //$NON-NLS-1$ 
							} else {
								System.out.println(head + "[" + i + "] " +  //$NON-NLS-1$ //$NON-NLS-2$
								    vars[i].getName() + " = " + vars[i].getStringValue()); //$NON-NLS-1$
							}				
						else {
							System.out.println(head + "[" + i + "] " +  //$NON-NLS-1$ //$NON-NLS-2$
									vars[i].getName() + ":");  //$NON-NLS-1$
							String[] m = vars[i].getStringListValue();	
							printStrings(m, "    ");  //$NON-NLS-1$
						}
					} catch (Exception e) {}
				}
			} else { if (print) System.out.println(head + ": array is empty");	}  //$NON-NLS-1$
		} else { if (print) System.out.println(head + ": array is null"); }  //$NON-NLS-1$
		return (String[])ar.toArray(new String[0]);
	}

	private void printStrings(String[] vars, String head) {
		if (!print) return;
		if (vars != null) {
			if (vars.length > 0) {
				for (int j=0; j<vars.length; j++) System.out.println(head + vars[j]);
			} else { System.out.println(head + ": array is empty");	}  //$NON-NLS-1$
		} else { System.out.println(head + ": array is null"); }  //$NON-NLS-1$
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
				proj = CCorePlugin.getDefault().createCProject(workspace.newProjectDescription(proj.getName()), 
					proj, new NullProgressMonitor(), MakeCorePlugin.MAKE_PROJECT_ID);
			
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
			} catch (BuildException e) {}
			ManagedBuildManager.setNewProjectVersion(proj);
			IConfiguration[] cfgs = projType.getConfigurations();
			IConfiguration defcfg = cfgs.length > 0 ? mproj.createConfiguration(cfgs[0], projType.getId() + ".0") : null; //$NON-NLS-1$
			for (int i = 1; i < cfgs.length; ++i) { // sic ! from 1
				mproj.createConfiguration(cfgs[i], projType.getId() + "." + i); //$NON-NLS-1$
			}
			ManagedBuildManager.setDefaultConfiguration(proj, defcfg);
		}
		// open project w/o progress monitor; no action performed if it's opened
		try {
			proj.open(null);
		} catch (CoreException e) {}				
	}
	/**
	 *  doInit() - call it at the beginning of every test
	 *
	 */
	private void doInit() {
		createManagedProject("Test");  //$NON-NLS-1$
		assertNotNull(proj);
		assertNotNull(mproj);
		worksp = proj.getWorkspace();
		assertNotNull(worksp);
		mp = ManagedBuildManager.getBuildMacroProvider();
		assertNotNull(mp);
		cfgs = mproj.getConfigurations();
		assertNotNull(cfgs);
		windows = System.getProperty("os.name").toLowerCase().startsWith("windows");  //$NON-NLS-1$ //$NON-NLS-2$
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
		for (int i=0; i<a.length; i++) {
			boolean found = false;
			for (int j=0; j<b.length; j++) {
				if (a[i].equals(b[j])) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}	
		return true;
	}
	/**
	 *      addMacro
	 * @param name
	 * @param type
	 * @param value
	 * @param context
	 * @param obj
	 * @return
	 */
	private boolean addMacro(String name, int type, String value, int context, Object obj) {
		assertNotNull(ms);
		for(int i=0; i<ms.length; i++) {
			if (ms[i] instanceof UserDefinedMacroSupplier) {
				return (((UserDefinedMacroSupplier)ms[i]).createMacro(name,type,value,context,obj) != null);
			}
		}
		return false;
	}
	/**
	 *      addMacro
	 * @param name
	 * @param type
	 * @param value
	 * @param context
	 * @param obj
	 * @return
	 */
	private boolean addMacro(String name, int type, String[] value, int context, Object obj) {
		assertNotNull(ms);
		for(int i=0; i<ms.length; i++) {
			if (ms[i] instanceof UserDefinedMacroSupplier) {
				return (((UserDefinedMacroSupplier)ms[i]).createMacro(name,type,value,context,obj) != null);
			}
		}
		return false;
	}
	/**
	 *      rmMacro
	 * @param name     - name of macro 
	 * @param context  
	 * @param obj
	 * @return
	 */
	private boolean rmMacro(String name, int context, Object obj) {
		assertNotNull(ms);
		for(int i=0; i<ms.length; i++) {
			if (ms[i] instanceof UserDefinedMacroSupplier) {
				return (((UserDefinedMacroSupplier)ms[i]).deleteMacro(name,context,obj) != null);
			}
		}
		return false;
	}
	
	/*
	 * addVars() - adds macros for testMacroResolveCase
	 */
	private void addVars() {
		int app = IBuildEnvironmentVariable.ENVVAR_APPEND;
		String del = ""; //$NON-NLS-1$
		UserDefinedEnvironmentSupplier usup = null;
		usup = ManagedBuildEnvironmentTests.getSupplier(worksp, "Workspace"); //$NON-NLS-1$
		if (usup != null) {
			try {
				usup.createVariable("casetest","lowercase",  app, del, worksp ); //$NON-NLS-1$ //$NON-NLS-2$ 
				usup.createVariable("CaseTest","capitalize", app, del, worksp ); //$NON-NLS-1$ //$NON-NLS-2$
				usup.createVariable("CaSeTeSt","upper2low",  app, del, worksp ); //$NON-NLS-1$ //$NON-NLS-2$
				usup.createVariable("CASETEST","uppercase",  app, del, worksp ); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				fail("Failed to create workspace vars " + e.getLocalizedMessage()); //$NON-NLS-1$
			}
		}
	}
	/*
	 * getFile() - open or creates sample file in current project
	 */
	private IFile getFile() { 
		final String FILENAME = "main.c";            //$NON-NLS-1$
		final String FILETEXT = "int main(){\n return 0;\n}"; //$NON-NLS-1$

		IFile f = proj.getProject().getFile(FILENAME);
		if ( !f.exists() )
			try {
				f.create( new ByteArrayInputStream(FILETEXT.getBytes() ), false, null );
		} catch (CoreException e) {	fail(e.getLocalizedMessage()); }
		return f;
	}
}
