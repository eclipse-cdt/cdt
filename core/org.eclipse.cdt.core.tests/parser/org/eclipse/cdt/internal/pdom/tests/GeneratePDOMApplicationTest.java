/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexerStateEvent;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.index.URIRelativeLocationConverter;
import org.eclipse.cdt.core.index.export.ExternalExportProjectProvider;
import org.eclipse.cdt.core.index.export.IExportProjectProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.export.GeneratePDOMApplication;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;

/**
 * Tests the GeneratePDOMApplication
 */
public class GeneratePDOMApplicationTest extends PDOMTestBase {
	private static final String SDK_VERSION = "com.acme.sdk.version";
	private static final String ACME_SDK_ID= "com.acme.sdk.4.0.1";
	private static List toDeleteOnTearDown= new ArrayList();
	private final static String locProject1= "resources/pdomtests/generatePDOMTests/project1";
	private final static String locProject2= "resources/pdomtests/generatePDOMTests/project2";
	private URI baseURI;

	public static Test suite() {
		return suite(GeneratePDOMApplicationTest.class);
	}

	protected void setUp() throws Exception {
		toDeleteOnTearDown.clear();
		baseURI= new URI("file:///base/"); // unimportant what the value is
	}

	protected void tearDown() throws Exception {
		for(Iterator i= toDeleteOnTearDown.iterator(); i.hasNext(); ) {
			ICProject cproject= (ICProject) i.next();
			cproject.getProject().delete(true, new NullProgressMonitor());
		}
	}	

	public void testBrokenExportProjectProvider1() throws Exception {
		setExpectedNumberOfLoggedNonOKStatusObjects(1); // IExportProjectProvider implementation returns null for createProject 
		File target= File.createTempFile("test", "pdom");
		doGenerate(new String[] {
			GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
			GeneratePDOMApplication.OPT_PROJECTPROVIDER, TestProjectProvider1.class.getName()
		});
	}

	public void testBrokenExportProjectProvider2() throws Exception {
		setExpectedNumberOfLoggedNonOKStatusObjects(1); // IExportProjectProvider implementation returns null for getLocationConverter 
		File target= File.createTempFile("test", "pdom");
		doGenerate(new String[] {
			GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
			GeneratePDOMApplication.OPT_PROJECTPROVIDER, TestProjectProvider2.class.getName()
		});
	}

	public void testSimpleExportProjectProvider1() throws Exception {
		File target= File.createTempFile("test", "pdom");
		doGenerate(new String[] {
				GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
				GeneratePDOMApplication.OPT_PROJECTPROVIDER, TestProjectProvider3.class.getName()
		});
		assertTrue(target.exists());
		WritablePDOM wpdom= new WritablePDOM(target, new URIRelativeLocationConverter(baseURI), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
		verifyProject1Content(wpdom);

		String fid= wpdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
		assertNotNull(fid);
		assertTrue(fid.startsWith("export")); // check for default export id
	}

	public void testSimpleExportProjectProvider2() throws Exception {
		File target= File.createTempFile("test", "pdom");
		doGenerate(new String[] {
				GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
				GeneratePDOMApplication.OPT_PROJECTPROVIDER, TestProjectProvider4.class.getName()
		});
		assertTrue(target.exists());
		WritablePDOM wpdom= new WritablePDOM(target, new URIRelativeLocationConverter(baseURI), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
		verifyProject1Content(wpdom);

		String fid= wpdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
		assertNotNull(fid);
		assertEquals(ACME_SDK_ID, fid); // check for custom export id

		String sdkVer= wpdom.getProperty(SDK_VERSION);
		assertNotNull(sdkVer);
		assertEquals("4.0.1", sdkVer); // check for custom property value
	}

	public void testExternalExportProjectProvider_BadCmdLine1() throws Exception {
		File target= File.createTempFile("test", "pdom");
		
		setExpectedNumberOfLoggedNonOKStatusObjects(1); // Expected failure: -source must be specified
		
		doGenerate(new String[] {
			GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
			GeneratePDOMApplication.OPT_PROJECTPROVIDER, ExternalExportProjectProvider.class.getName()
		});
		assertTrue(target.exists());
	}

	public void testExternalExportProjectProvider_BadCmdLine2() throws Exception {
		File target= File.createTempFile("test", "pdom");
		TestProjectProvider4 tpp4= new TestProjectProvider4();
		ICProject cproject= tpp4.createProject();
		
		setExpectedNumberOfLoggedNonOKStatusObjects(1); // Expected failure: -id must be specified
		
		doGenerate(new String[] {
			GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
			GeneratePDOMApplication.OPT_PROJECTPROVIDER, ExternalExportProjectProvider.class.getName(),
			ExternalExportProjectProvider.OPT_SOURCE, cproject.getProject().getLocation().toFile().getAbsolutePath()
		});
		assertTrue(target.exists());
	}
	
	public void testExternalExportProjectProvider_BadCmdLine3() throws Exception {
		File target= File.createTempFile("test", "pdom");
		TestProjectProvider4 tpp4= new TestProjectProvider4();
		ICProject cproject= tpp4.createProject();
		
		setExpectedNumberOfLoggedNonOKStatusObjects(1); // Expected failure: -target must be specified
		doGenerate(new String[] {
			GeneratePDOMApplication.OPT_PROJECTPROVIDER, ExternalExportProjectProvider.class.getName(),
			ExternalExportProjectProvider.OPT_SOURCE, cproject.getProject().getLocation().toFile().getAbsolutePath()
		});
		assertTrue(target.exists());
	}

	public void testExternalExportProjectProvider() throws Exception {
		File target= File.createTempFile("test", "pdom");

		final int[] stateCount = new int[1];
		IIndexerStateListener listener= new IIndexerStateListener() {
			public void indexChanged(IIndexerStateEvent event) {
				stateCount[0]++;
			}
		};
		CCorePlugin.getIndexManager().addIndexerStateListener(listener);

		URL url= FileLocator.find(CTestPlugin.getDefault().getBundle(), new Path(locProject1), null);
		String baseDir= FileLocator.toFileURL(url).getFile();

		doGenerate(new String[] {
				GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
				GeneratePDOMApplication.OPT_PROJECTPROVIDER, ExternalExportProjectProvider.class.getName(),
				ExternalExportProjectProvider.OPT_SOURCE, baseDir,
				ExternalExportProjectProvider.OPT_FRAGMENT_ID, "hello.world"
		});
		assertTrue(target.exists());

		WritablePDOM wpdom= new WritablePDOM(target, new URIRelativeLocationConverter(baseURI), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
		verifyProject1Content(wpdom);

		String fid= wpdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
		assertNotNull(fid);
		assertEquals("hello.world", fid); // check for id passed on command-line

		assertTrue(stateCount[0] == 2);
	}

	public void testExternalExportProjectProvider_SysIncludes() throws Exception {
		File target= File.createTempFile("test", "pdom");

		final int[] stateCount = new int[1];
		IIndexerStateListener listener= new IIndexerStateListener() {
			public void indexChanged(IIndexerStateEvent event) {
				stateCount[0]++;
			}
		};
		CCorePlugin.getIndexManager().addIndexerStateListener(listener);

		URL url= FileLocator.find(CTestPlugin.getDefault().getBundle(), new Path(locProject2), null);
		String baseDir= FileLocator.toFileURL(url).getFile();

		doGenerate(new String[] {
				GeneratePDOMApplication.OPT_TARGET, target.getAbsolutePath(), 
				GeneratePDOMApplication.OPT_PROJECTPROVIDER, ExternalExportProjectProvider.class.getName(),
				ExternalExportProjectProvider.OPT_SOURCE, baseDir,
				ExternalExportProjectProvider.OPT_FRAGMENT_ID, "hello.world"
		});
		assertTrue(target.exists());

		WritablePDOM wpdom= new WritablePDOM(target, new URIRelativeLocationConverter(baseURI), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
		verifyProject2Content(wpdom);
	}

	public void verifyProject1Content(WritablePDOM wpdom) throws Exception {
		wpdom.acquireReadLock();
		try {
			IBinding[] bindings= wpdom.findBindings(Pattern.compile(".*foo.*"), false, IndexFilter.ALL, PROGRESS);
			assertEquals(1, bindings.length);

			bindings= wpdom.findBindings(Pattern.compile(".*bar.*"), false, IndexFilter.ALL, PROGRESS);
			assertEquals(1, bindings.length);
		} finally {
			wpdom.releaseReadLock();
		}
	}

	public void verifyProject2Content(WritablePDOM wpdom) throws Exception {
		wpdom.acquireReadLock();
		try {
			IBinding[] bindings= wpdom.findBindings(Pattern.compile(".*"), true, IndexFilter.ALL, NPM);
			assertEquals(2, bindings.length);

			int b= bindings[0].getName().equals("A") ? 1 : 0;
			assertTrue(bindings[0] instanceof ICPPClassType);
			assertTrue(bindings[1] instanceof ICPPClassType);
			assertTrue(((ICPPClassType)bindings[1-b]).getBases().length==0);
			assertTrue(((ICPPClassType)bindings[b]).getBases().length==1);
		} finally {
			wpdom.releaseReadLock();
		}
	}

	private void doGenerate(String[] args) throws CoreException {
		GeneratePDOMApplication app = new GeneratePDOMApplication();
		IApplicationContext ac= new MockApplicationContext(args);
		app.start(ac);
	}

	/*
	 * IExportProjectProvider test implementations
	 */
	public static class TestProjectProvider1 implements IExportProjectProvider {
		public ICProject createProject() throws CoreException {return null;}
		public Map getExportProperties() {return null;}
		public IIndexLocationConverter getLocationConverter(ICProject cproject) {return null;}
		public void setApplicationArguments(String[] arguments) {}
	}

	public static class TestProjectProvider2 implements IExportProjectProvider {
		public ICProject createProject() throws CoreException {
			ICProject cproject= CProjectHelper.createCCProject("test"+System.currentTimeMillis(), null, IPDOMManager.ID_NO_INDEXER);
			toDeleteOnTearDown.add(cproject);
			CProjectHelper.importSourcesFromPlugin(cproject, CTestPlugin.getDefault().getBundle(), locProject1);
			return cproject;
		}
		public Map getExportProperties() {return null;}
		public IIndexLocationConverter getLocationConverter(ICProject cproject) {return null;}
		public void setApplicationArguments(String[] arguments) {}
	}

	public static class TestProjectProvider3 implements IExportProjectProvider {
		public ICProject createProject() throws CoreException {
			ICProject cproject= CProjectHelper.createCCProject("test"+System.currentTimeMillis(), null, IPDOMManager.ID_NO_INDEXER);
			toDeleteOnTearDown.add(cproject);
			CProjectHelper.importSourcesFromPlugin(cproject, CTestPlugin.getDefault().getBundle(), locProject1);
			return cproject;
		}
		public Map getExportProperties() {return null;}
		public IIndexLocationConverter getLocationConverter(ICProject cproject) {
			return new ResourceContainerRelativeLocationConverter(cproject.getProject());
		}
		public void setApplicationArguments(String[] arguments) {}
	}

	public static class TestProjectProvider4 implements IExportProjectProvider {		
		public ICProject createProject() throws CoreException {
			ICProject cproject= CProjectHelper.createCCProject("test"+System.currentTimeMillis(), null, IPDOMManager.ID_NO_INDEXER);
			toDeleteOnTearDown.add(cproject);
			CProjectHelper.importSourcesFromPlugin(cproject, CTestPlugin.getDefault().getBundle(), locProject1);
			return cproject;
		}
		public Map getExportProperties() {
			Map map= new HashMap();
			map.put(SDK_VERSION, "4.0.1");
			map.put(IIndexFragment.PROPERTY_FRAGMENT_ID, ACME_SDK_ID);
			return map;
		}
		public IIndexLocationConverter getLocationConverter(ICProject cproject) {
			return new ResourceContainerRelativeLocationConverter(cproject.getProject());
		}
		public void setApplicationArguments(String[] arguments) {}
	}
}

class MockApplicationContext implements IApplicationContext {
	Map arguments;
	MockApplicationContext(String[] appArgs) {
		arguments= new HashMap();
		arguments.put(APPLICATION_ARGS, appArgs);
	}
	public void applicationRunning() {}
	public Map getArguments() {return arguments;}
	public String getBrandingApplication() {return null;}
	public Bundle getBrandingBundle() {return null;}
	public String getBrandingDescription() {return null;}
	public String getBrandingId() {return null;}
	public String getBrandingName() {return null;}
	public String getBrandingProperty(String key) {return null;}
}
