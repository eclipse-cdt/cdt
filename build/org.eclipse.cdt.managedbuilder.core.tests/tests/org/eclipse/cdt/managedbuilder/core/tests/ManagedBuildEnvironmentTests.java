/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentBuildPathsChangeListener;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.internal.envvar.UserDefinedEnvironmentSupplier;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * 
 * */
public class ManagedBuildEnvironmentTests extends TestCase {
	final private String REQUIRED_TYPE = "cdt.managedbuild.target.testgnu.exe"; //$NON-NLS-1$
    // test variable names
	final private String NAME_CWD  = "CWD";    //$NON-NLS-1$
	final private String NAME_PWD  = "PWD";    //$NON-NLS-1$
	final private String NAME_CMN  = "COMMON"; //$NON-NLS-1$
	final private String NAME_WSP  = "WSP";    //$NON-NLS-1$
	final private String NAME_PRJI = "PRJI";   //$NON-NLS-1$
	final private String NAME_PRJL = "PRJL";   //$NON-NLS-1$
	final private String NAME_CFGI = "CFGI";   //$NON-NLS-1$
	final private String NAME_CFGL = "CFGL";   //$NON-NLS-1$
	final private String NAME_CFGX = "CFGX";   //$NON-NLS-1$
	final private String NAME_CFG  = "CFG";    //$NON-NLS-1$
	final private String NAME_REM1 = "REMTST1";//$NON-NLS-1$
	final private String NAME_REM2 = "REMTST2";//$NON-NLS-1$
    // test variable values
	final private String VAL_CWDPWD = "CWD_&_PWD_should not be changed";    //$NON-NLS-1$
	final private String VAL_DUMMY1  = "/a/b/c";         //$NON-NLS-1$
	final private String VAL_DUMMY2  = "/d/e/f";         //$NON-NLS-1$  
	final private String VAL_PRO_INC = "/project/inc";   //$NON-NLS-1$
	final private String VAL_PRO_LIB = "/project/lib";   //$NON-NLS-1$
	
	final private String LISTENER_DATA = "O1T1O1O2T2T1O1T1O2T2"; //$NON-NLS-1$
	
	// delimiters
	final private String DEL_WIN  = ";"; //$NON-NLS-1$
	final private String DEL_UNIX = ":"; //$NON-NLS-1$
	
	IEnvironmentVariableProvider envProvider = null;
	IWorkspace worksp = null;
	IProject proj = null;
	IManagedProject mproj = null;
	String listenerResult = ""; //$NON-NLS-1$
	
	IEnvironmentBuildPathsChangeListener listener = new IEnvironmentBuildPathsChangeListener(){
		public void buildPathsChanged(IConfiguration configuration, int buildPathType){
			listenerResult = listenerResult + configuration.getName().charAt(0) + buildPathType;
		}
	};
	
	public ManagedBuildEnvironmentTests() {	super(); }
	public ManagedBuildEnvironmentTests(String name) { super(name); }
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildEnvironmentTests.class.getName());
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvNotDef"));    //$NON-NLS-1$		
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvUpper"));	 //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvAppend"));	 //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvCWDPWD"));	 //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvSuppliers")); //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvGetPath"));   //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvSubscribe")); //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvGetParams")); //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvRemove"));    //$NON-NLS-1$
		suite.addTest(new ManagedBuildEnvironmentTests("testEnvProvider"));  //$NON-NLS-1$
		return suite;
	}
	
	//	 Checking behaviour when vars are not defined (except system)   
	public void testEnvNotDef(){
		doInit();
		assertNotNull("System  vars", envProvider.getVariables(null, true, false));    //$NON-NLS-1$
		assertNotNull("Worksp. vars", envProvider.getVariables(worksp, true, false));  //$NON-NLS-1$
		assertNotNull("Project vars", envProvider.getVariables(mproj, true, false));   //$NON-NLS-1$
		IConfiguration[] cfgs = mproj.getConfigurations();
		for (int k=0; k<cfgs.length; k++) {
			assertNotNull("Project vars["+k+"]",                  //$NON-NLS-1$ //$NON-NLS-2$
					envProvider.getVariables(cfgs[k], true, false));   
		}
	}
	
	/**
	 * testEnvUpper - check uplevel variables
	 * In each context, up-level vars should be returned  
	 */
	public void testEnvUpper(){
		doInit();
		addTestVariables();
		IBuildEnvironmentVariable[] a = envProvider.getVariables(null, true, false);
		IBuildEnvironmentVariable[] b = envProvider.getVariables(worksp, true, false);
		assertTrue(varListContainNames(a, b));
		IBuildEnvironmentVariable[] c = envProvider.getVariables(mproj, true, false);
		assertTrue(varListContainNames(b, c));
		
		IConfiguration[] cfgs = mproj.getConfigurations();
		for (int k=0; k<cfgs.length; k++) {
			IBuildEnvironmentVariable[] d = envProvider.getVariables(cfgs[k], true, false);
			assertTrue(varListContainNames(c, d));			
		}
	}

	/**
	 * 
	 *
	 */
	public void testEnvAppend(){
		doInit();

		IBuildEnvironmentVariable a = envProvider.getVariable(NAME_CMN, worksp, true, false);
		assertEquals(NAME_WSP, a.getValue());

		assertNotNull(a = envProvider.getVariable(NAME_CMN, mproj, true, false));
		assertEquals(NAME_WSP+DEL_UNIX+NAME_PRJI, a.getValue());

		IConfiguration cfg = mproj.getConfigurations()[0];
		assertNotNull(a = envProvider.getVariable(NAME_CMN, cfg, true, false));
		assertEquals(NAME_WSP+DEL_UNIX+NAME_PRJI+DEL_UNIX+NAME_CFGI, a.getValue());
	}

	/**
	 * 
	 *
	 */
	public void testEnvCWDPWD(){
		doInit();
		IConfiguration cfg = mproj.getConfigurations()[0];
		// CWD/PWD vars should NOT be overwritten anywhere
		assertNull(envProvider.getVariable(NAME_CWD, worksp, true, false)); 
		assertNull(envProvider.getVariable(NAME_CWD, mproj, true, false));
		IBuildEnvironmentVariable a = envProvider.getVariable(NAME_CWD, cfg, true, false);
		assertNotNull(a);
		if (VAL_CWDPWD.equals(a.getValue())) fail("CWD should not be rewritten !"); //$NON-NLS-1$
		
		assertNull(envProvider.getVariable(NAME_PWD, worksp, true, false)); 
		assertNull(envProvider.getVariable(NAME_PWD, mproj, true, false));
		a = envProvider.getVariable(NAME_PWD, cfg, true, false);
		assertNotNull(a);
		if (VAL_CWDPWD.equals(a.getValue())) fail("PWD should not be rewritten !"); //$NON-NLS-1$
		
		// try to delete: should fail
		UserDefinedEnvironmentSupplier usup = getSupplier(cfg, cfg.getName());
		assertNull(usup.deleteVariable(NAME_CWD, cfg));
		assertNull(usup.deleteVariable(NAME_PWD, cfg));
		assertNotNull(envProvider.getVariable(NAME_CWD, cfg, true, false));
		assertNotNull(envProvider.getVariable(NAME_PWD, cfg, true, false));
	}
		
	public void testEnvSuppliers() {
		doInit();
		
		IEnvironmentVariableSupplier[] arrSupSys = envProvider.getSuppliers(null);
		assertEquals("System suppliers count not equal to 1", arrSupSys.length, 1); //$NON-NLS-1$
		IBuildEnvironmentVariable[] a = arrSupSys[0].getVariables(null);
		assertNotNull(a);
		IBuildEnvironmentVariable[] b = envProvider.getVariables(null, false, false);
		assertTrue(varListContainNames(a, b));
		assertTrue(varListContainNames(b, a));
		
		IEnvironmentVariableSupplier[] arrSupWrk = envProvider.getSuppliers(worksp);
		assertEquals("Workspace suppliers count not equal to 1", arrSupWrk.length, 1); //$NON-NLS-1$
		a = arrSupWrk[0].getVariables(worksp);
		assertNotNull(a);
		b = envProvider.getVariables(worksp, false, false);
		assertTrue(varListContainNames(a, b));
		assertTrue(varListContainNames(b, a));	

		IEnvironmentVariableSupplier[] arrSupPro = envProvider.getSuppliers(mproj);
		assertEquals("Project suppliers count not equal to 2", arrSupPro.length, 2); //$NON-NLS-1$

		b = envProvider.getVariables(mproj, false, false);
		for (int k=0; k<arrSupPro.length; k++ ) {
			assertTrue(varListContainNames(arrSupPro[k].getVariables(mproj), b));
		}
		
		IConfiguration[] configs = mproj.getConfigurations();
		for (int j=0; j<configs.length; j++) {
			b = envProvider.getVariables(configs[j], false, false);
			IEnvironmentVariableSupplier[] arrSupCfg = envProvider.getSuppliers(configs[j]);
			assertEquals("Configuration suppliers count not equal to 3", arrSupCfg.length, 3); //$NON-NLS-1$
			for (int k=0; k<arrSupCfg.length; k++ ) {
				assertTrue(varListContainNames(arrSupCfg[k].getVariables(configs[j]), b));
			}
		}
	}
	
	/*
	 * plugin.xml contents:
	     <projectType id="cdt.managedbuild.target.testgnu.exe">
         ... 
	           <configuration name="Dbg"
               ...
                   <tool 
                   ...     
	  	              <envVarBuildPath
        	                  pathType="buildpathInclude"
                	          variableList="CFGI,CFG0,PRJI">
	                  </envVarBuildPath>
        	          <envVarBuildPath
                	          pathType="buildpathLibrary"
                        	  variableList="CFGL,PRJL">
	                  </envVarBuildPath>
         ... 
	           <configuration name="Rel"
               ...  
                   <tool 
                   ...     
                      <envVarBuildPath
        	                  pathType="buildpathInclude"
                	          variableList="CFGI,CFG1,PRJI">
	                  </envVarBuildPath>
        	          <envVarBuildPath
                	          pathType="buildpathLibrary"
                        	  variableList="CFGL,PRJL">
	                  </envVarBuildPath>
	 */
	public void testEnvGetPath(){
		doInit();
		IConfiguration[] configs = mproj.getConfigurations();
		
		for (int i=0; i<2; i++) {  // only 2 first configs are affected
			String[] val_inc = {"/config/include/"+i, "/config"+i+"/include", VAL_PRO_INC};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String[] val_lib = {"/config/lib/"+i, VAL_PRO_LIB};  //$NON-NLS-1$
			String[] s1, s2, s3;
			s1 = envProvider.getBuildPaths(configs[i], 1); // include
			s2 = envProvider.getBuildPaths(configs[i], 2); // library
			s3 = envProvider.getBuildPaths(configs[i], 0); // unknown

			assertNotNull("Include path is null", s1);  //$NON-NLS-1$
			assertNotNull("Library path is null", s2);  //$NON-NLS-1$
			assertNotNull("Bad path type returns null", s3); //$NON-NLS-1$
			assertEquals("Include path should contain 3 entries !", s1.length, 3); //$NON-NLS-1$
			assertEquals("Library path should contain 2 entries !", s2.length, 2); //$NON-NLS-1$
			assertEquals("Request with bad path type should return 0 entries !", s3.length, 0); //$NON-NLS-1$

			compareStringLists(configs[i].getName()+"-include", s1, val_inc); //$NON-NLS-1$
			compareStringLists(configs[i].getName()+"-library", s2, val_lib); //$NON-NLS-1$
		}
	}
	
	public void testEnvSubscribe(){
		doInit();
		IConfiguration[] configs = mproj.getConfigurations();
		
		IConfiguration cfg = configs[0];
		UserDefinedEnvironmentSupplier usup = getSupplier(cfg, cfg.getName());
		assertNotNull(usup);	
		try {
			
			usup.deleteVariable(NAME_CFGI,configs[0]);
			usup.deleteVariable(NAME_CFGI,configs[1]);
			usup.deleteVariable(NAME_CFG+"0",configs[0]);
			usup.deleteVariable(NAME_CFG+"1",configs[0]);
			usup.deleteVariable(NAME_CFG+"0",configs[1]);
			usup.deleteVariable(NAME_CFG+"1",configs[1]);
			usup.deleteVariable(NAME_CFGL,configs[0]);
			usup.deleteVariable(NAME_CFGL,configs[1]);
			usup.deleteVariable(NAME_PRJI,mproj);
			usup.deleteVariable(NAME_PRJL,mproj);
			usup.deleteVariable(NAME_CFGX,mproj);
			listenerResult = ""; //$NON-NLS-1$
			
	   	    envProvider.subscribe(listener);
			// should affect config Deb
			usup.createVariable(NAME_CFGI,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[0]);
			assertEquals("Step 1", listenerResult, LISTENER_DATA.substring(0,2)); //$NON-NLS-1$
			// should affect config Rel
			usup.createVariable(NAME_CFGI,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[1]);
			assertEquals("Step 2", listenerResult, LISTENER_DATA.substring(0,4)); //$NON-NLS-1$
			
			// should affect config Deb
			usup.createVariable(NAME_CFG+"0",VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[0]); //$NON-NLS-1$
			assertEquals("Step 3", listenerResult, LISTENER_DATA.substring(0,6)); //$NON-NLS-1$
			// should not affect anything - variable not in path of cfg 0 
			usup.createVariable(NAME_CFG+"1",VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[0]); //$NON-NLS-1$
			assertEquals("Step 4", listenerResult, LISTENER_DATA.substring(0,6)); //$NON-NLS-1$

			// should affect config Deb
			usup.createVariable(NAME_CFGL,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[0]);
			assertEquals("Step 5", listenerResult, LISTENER_DATA.substring(0,8)); //$NON-NLS-1$
			// should affect config Rel
			usup.createVariable(NAME_CFGL,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[1]);
			assertEquals("Step 6", listenerResult, LISTENER_DATA.substring(0,10)); //$NON-NLS-1$
			
			// should not affect anything - variable not in path of cfg 1 
			usup.createVariable(NAME_CFG+"0",VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[1]); //$NON-NLS-1$
			assertEquals("Step 7", listenerResult, LISTENER_DATA.substring(0,10)); //$NON-NLS-1$
			// should affect config Rel 
			usup.createVariable(NAME_CFG+"1",VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[1]); //$NON-NLS-1$
			assertEquals("Step 8", listenerResult, LISTENER_DATA.substring(0,12)); //$NON-NLS-1$
			
			// should affect both configurations
			usup.createVariable(NAME_PRJI,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, mproj);
			assertEquals("Step 9", listenerResult, LISTENER_DATA.substring(0,16)); //$NON-NLS-1$
			// should affect both configurations
			usup.createVariable(NAME_PRJL,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, mproj);
			assertEquals("Step 10", listenerResult, LISTENER_DATA); //$NON-NLS-1$

			
			// should not affect anything - no changes
			usup.createVariable(NAME_PRJL,VAL_DUMMY2,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, mproj);
			assertEquals("Step 11", listenerResult, LISTENER_DATA); //$NON-NLS-1$
			// should not affect anything - variable not in path 			
			usup.createVariable(NAME_CFGX,VAL_DUMMY2, IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, mproj);
			assertEquals("Step 12", listenerResult, LISTENER_DATA); //$NON-NLS-1$

			envProvider.unsubscribe(listener);

			// should NOT affect anything - subscription cancelled
			usup.createVariable(NAME_PRJI,VAL_DUMMY1,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, mproj);
			usup.createVariable(NAME_CFGI,VAL_DUMMY1,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[0]);
			usup.createVariable(NAME_CFGI,VAL_DUMMY1,IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, configs[1]);
			assertEquals("Step 13", listenerResult, LISTENER_DATA); //$NON-NLS-1$
			
		} catch (Exception e) {
			fail("Failed to create configuration vars !"); //$NON-NLS-1$
		}
	}
	
	/**
	 * 
	 *
	 */
	public void testEnvGetParams(){
		doInit();
		IEnvironmentVariableProvider envProvider = ManagedBuildManager.getEnvironmentVariableProvider();
		IBuildEnvironmentVariable x = null;
		IBuildEnvironmentVariable y = null;
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) { //$NON-NLS-1$ //$NON-NLS-2$
			assertEquals(envProvider.getDefaultDelimiter(), DEL_WIN);
			assertFalse(envProvider.isVariableCaseSensitive());
			// these var instances are different although contents is equal. 
			x = envProvider.getVariable("PATH", mproj, true, false);
			assertNotNull(x);
			y = envProvider.getVariable("path", mproj, true, false);
			assertNotNull(y);
			assertEquals(x.getName(), y.getName());
			assertEquals(x.getValue(), y.getValue());
		} else {
			assertEquals(envProvider.getDefaultDelimiter(), DEL_UNIX);
			assertTrue(envProvider.isVariableCaseSensitive());
			// "path" is different var (may absent); 
			x = envProvider.getVariable("PATH", mproj, true, false);
			assertNotNull(x);
			y = envProvider.getVariable("path", mproj, true, false);
			if (y != null) {
				assertFalse(x.getName().equals(y.getName()));
			}
		}		
	}
	/**
	 * 
	 *
	 */
	public void testEnvRemove(){
		doInit();
		IEnvironmentVariableProvider env = ManagedBuildManager.getEnvironmentVariableProvider();
		UserDefinedEnvironmentSupplier usup = null;
		
		// create vars for removal tests
		assertNotNull(usup = getSupplier(worksp, "Workspace")); //$NON-NLS-1$
		try {
			assertNotNull(usup.createVariable(NAME_REM1, VAL_DUMMY1, IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, worksp));
			assertNotNull(usup.createVariable(NAME_REM2, VAL_DUMMY1, IBuildEnvironmentVariable.ENVVAR_REPLACE, DEL_UNIX, worksp));
		} catch (Exception e) {	fail(e.getLocalizedMessage()); }

		assertNotNull(usup = getSupplier(mproj, "Project")); //$NON-NLS-1$
		try {
			assertNotNull(usup.createVariable(NAME_REM1, VAL_DUMMY2, IBuildEnvironmentVariable.ENVVAR_REMOVE, DEL_UNIX, mproj));
			assertNotNull(usup.createVariable(NAME_REM2, VAL_DUMMY2, IBuildEnvironmentVariable.ENVVAR_APPEND, DEL_UNIX, mproj));
		} catch (Exception e) {	fail(e.getLocalizedMessage()); }

		IConfiguration cfg = mproj.getConfigurations()[0];
		assertNotNull(usup = getSupplier(cfg, "Configuration 0")); //$NON-NLS-1$
		try {
			assertNotNull(usup.createVariable(NAME_REM1, VAL_CWDPWD, IBuildEnvironmentVariable.ENVVAR_REMOVE, DEL_UNIX, cfg));
			assertNotNull(usup.createVariable(NAME_REM2, VAL_CWDPWD, IBuildEnvironmentVariable.ENVVAR_REMOVE, DEL_UNIX, cfg));
		} catch (Exception e) {	fail(e.getLocalizedMessage()); }
		
		// Check vars presence/absence on different levels  
		IBuildEnvironmentVariable a = env.getVariable(NAME_REM1, worksp, true, false);
		IBuildEnvironmentVariable b = env.getVariable(NAME_REM2, worksp, true, false);
		assertNotNull(a);
		assertNotNull(b);
		a = env.getVariable(NAME_REM1, mproj, true, false);
		b = env.getVariable(NAME_REM2, mproj, true, false);
		assertNull(a);
		assertNotNull(b);
		assertEquals(b.getValue(), VAL_DUMMY1 + DEL_UNIX + VAL_DUMMY2);
		a = env.getVariable(NAME_REM1, cfg, true, false);
		b = env.getVariable(NAME_REM2, cfg, true, false);
		assertNull(a);
		assertNull(b);
	}
	/**
	 * testEnvProvider() - 
	 */
	public void testEnvProvider() {
		doInit();
		IBuildEnvironmentVariable a = envProvider.getVariable(TestMacro.PRJ_VAR, mproj, true, false);
		assertNotNull(a);
		assertEquals(TestMacro.PRJ_VAR + mproj.getName(), a.getValue());
		
		IConfiguration[] cfgs = mproj.getConfigurations();
		a = envProvider.getVariable(TestMacro.CFG_VAR, cfgs[0], true, false);
		assertNotNull(a);
		assertEquals(TestMacro.CFG_VAR + cfgs[0].getName(), a.getValue());
		
		// no provider for another configurations 
		a = envProvider.getVariable(TestMacro.CFG_VAR, cfgs[1], true, false);
		assertNull(a);
		
		// combination user-defined and provided variables
		UserDefinedEnvironmentSupplier usup = getSupplier(cfgs[0], cfgs[0].getName());
		usup.createVariable(TestMacro.PRJ_VAR, VAL_DUMMY1, IBuildEnvironmentVariable.ENVVAR_PREPEND, DEL_UNIX, cfgs[0]);
		a = envProvider.getVariable(TestMacro.PRJ_VAR, cfgs[0], true, false);
		assertNotNull(a);
		assertEquals(VAL_DUMMY1+DEL_UNIX+TestMacro.PRJ_VAR+mproj.getName(), a.getValue());
	}

	/**
	 * This test is not used iun suite. It just prints variabes 
	 */	
	public void testEnvPrint(){
		doInit();
		printVar("s-Var", envProvider.getVariables(null, false, false));    //$NON-NLS-1$
		printVar("w-Var", envProvider.getVariables(worksp, false, false));  //$NON-NLS-1$
		printVar("p-Var", envProvider.getVariables(mproj, false, false));   //$NON-NLS-1$
		IConfiguration[] cfgs = mproj.getConfigurations();
		for (int k=0; k<cfgs.length; k++) {
			printVar("c[" + k + "]-Var", envProvider.getVariables(cfgs[k], false, false));  //$NON-NLS-1$ //$NON-NLS-2$ 
		}
	}
  	
	// Create all required user variables
	
	public static UserDefinedEnvironmentSupplier getSupplier(Object obj, String objName) {
		IEnvironmentVariableSupplier[] arrSup = null; 
		arrSup = ManagedBuildManager.getEnvironmentVariableProvider().getSuppliers(obj);
		for (int i=0; i<arrSup.length; i++ ) {
			if (arrSup[i] instanceof UserDefinedEnvironmentSupplier) {
				return (UserDefinedEnvironmentSupplier) arrSup[i];
			}
		}
		fail("Cannot access user variable supplier for " + objName); //$NON-NLS-1$
		return null;
	}
	
	/**
	 * 
	 * 
	 * Note: CWD and PWD vars are not allowed to be added/changed
	 */
	private void addTestVariables() {
		final int STD_MODE = IBuildEnvironmentVariable.ENVVAR_REPLACE;
		UserDefinedEnvironmentSupplier usup = null;
		usup = getSupplier(worksp, "Workspace"); //$NON-NLS-1$
		try {
			if (usup != null) {
				assertNotNull(usup.createVariable(NAME_CMN, NAME_WSP, IBuildEnvironmentVariable.ENVVAR_APPEND, DEL_UNIX, worksp));
				assertNotNull(usup.createVariable(NAME_WSP,VAL_DUMMY1, STD_MODE, DEL_UNIX, worksp));
				assertNull(usup.createVariable(NAME_CWD,VAL_CWDPWD, STD_MODE, DEL_UNIX, worksp));
				assertNull(usup.createVariable(NAME_PWD,VAL_CWDPWD, STD_MODE, DEL_UNIX, worksp));
			}
		} catch (Exception e) {
			fail("Failed to create workspace vars " + e.getLocalizedMessage()); //$NON-NLS-1$
		}

		usup = getSupplier(mproj, "Project"); //$NON-NLS-1$
		try {
			if (usup != null) {
				assertNotNull(usup.createVariable(NAME_CMN, NAME_PRJI, IBuildEnvironmentVariable.ENVVAR_APPEND, DEL_UNIX, mproj));
				assertNotNull(usup.createVariable(NAME_PRJI,VAL_PRO_INC, STD_MODE, DEL_UNIX, mproj));
				assertNotNull(usup.createVariable(NAME_PRJL,VAL_PRO_LIB, STD_MODE, DEL_UNIX, mproj));
				assertNull(usup.createVariable(NAME_CWD, VAL_CWDPWD,  STD_MODE, DEL_UNIX, mproj));
				assertNull(usup.createVariable(NAME_PWD, VAL_CWDPWD,  STD_MODE, DEL_UNIX, mproj));
			}
		} catch (Exception e) {
			fail("Failed to create project vars " + e.getLocalizedMessage()); //$NON-NLS-1$
		}

		IConfiguration[] configs = mproj.getConfigurations();
		for (int i = 0; i < 2; i++) { // only 2 first configs are affected
			IConfiguration cfg = configs[i];
			usup = getSupplier(cfg, "Configuration " + cfg.getName()); //$NON-NLS-1$
			try {
				if (usup != null) {	
					assertNotNull(usup.createVariable(NAME_CMN, NAME_CFGI, IBuildEnvironmentVariable.ENVVAR_APPEND, DEL_UNIX, cfg));
					assertNotNull(usup.createVariable(NAME_CFGI, "/config/include/"+i,  STD_MODE, DEL_UNIX, cfg)); //$NON-NLS-1$
					assertNotNull(usup.createVariable(NAME_CFG+i,"/config"+i+"/include",STD_MODE, DEL_UNIX, cfg)); //$NON-NLS-1$ //$NON-NLS-2$
					assertNotNull(usup.createVariable(NAME_CFGL, "/config/lib/"+i,      STD_MODE, DEL_UNIX, cfg)); //$NON-NLS-1$
					assertNotNull(usup.createVariable(NAME_CFGX, "/config/unused",      STD_MODE, DEL_UNIX, cfg)); //$NON-NLS-1$
					assertNull(usup.createVariable(NAME_CWD, VAL_CWDPWD,             STD_MODE, DEL_UNIX, cfg));
					assertNull(usup.createVariable(NAME_PWD, VAL_CWDPWD,             STD_MODE, DEL_UNIX, cfg));
				}
			} catch (Exception e) {
				fail("Failed to create configuration vars for <" + cfg.getName() + "> - "+ e.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		try {
			proj.build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (Exception e) {}
	}
	private void doInit() {
		envProvider = ManagedBuildManager.getEnvironmentVariableProvider();
		assertNotNull(envProvider);
		ManagedBuildMacrosTests.createManagedProject("Merde"); //$NON-NLS-1$
		proj = ManagedBuildMacrosTests.proj;
		assertNotNull(proj);
		mproj = ManagedBuildMacrosTests.mproj;	
		assertNotNull(mproj);
		worksp = proj.getWorkspace();
		assertNotNull(worksp);
	}
	
	/*
	 * Print contents of env.var array, with given header.
	 */
	private void printVar(String head, IBuildEnvironmentVariable[] vars) {
		if (vars != null) {
			if (vars.length > 0) {
				for (int i=0; i < vars.length; i++) {
					System.out.println(head + "[" + i + "] " +  //$NON-NLS-1$ //$NON-NLS-2$ 
							vars[i].getName() + " = " +         //$NON-NLS-1$
							vars[i].getValue() + " / " +        //$NON-NLS-1$
							vars[i].getOperation() + vars[i].getDelimiter());
				}
			} else { System.out.println(head + ": array is empty");	} //$NON-NLS-1$
		} else { System.out.println(head + ": array is null"); } //$NON-NLS-1$
	}

	/*
	 * check that ALL variables from list "a" have correspondence 
	 * in list "b"
	 * empty list or null are treated as corresponding to anything
	 */
	private boolean varListContainNames(IBuildEnvironmentVariable[] a, IBuildEnvironmentVariable[] b) {
		if (a == null) return true;
		else if (a.length == 0) return true;
		else if (b == null) return false;
		
		for (int i=0; i<a.length; i++) {
			boolean found = false;
			for (int j=0; j<b.length; j++) {
				if (a[i].getName().equals(b[j].getName())) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}	
		return true;
	}
	
	/**
	 * 
	 * @param head
	 * @param a
	 * @param b
	 */
	private void compareStringLists(String head, String[] a, String[] b) {
		long mask =0;
		long finalmask = Math.round(Math.pow(2,b.length) - 1);
		for (int k=0; k<a.length; k++) {
			boolean found = false;
			for (int m=0; m<b.length; m++) {
				if (a[k].equals(b[m])) {
					mask |= 1 << m;
					found = true;
					break;
				}
			}
			assertTrue(found);
		}
		assertEquals(mask, finalmask);
	}
}


