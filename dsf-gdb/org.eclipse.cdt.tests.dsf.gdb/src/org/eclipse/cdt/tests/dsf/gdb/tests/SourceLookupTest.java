/*******************************************************************************
 * Copyright (c) 2015, 2018 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.ISourceFinder;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.ProgramRelativePathSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles.IDebugSourceFileInfo;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIMixedInstruction;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests that interaction with source lookups works as expected.
 * <p>
 * All of these tests use one of SourceLookup*.exe that was built from a file
 * that was "moved" since build time. At build time the SourceLookup.cc file was
 * located in the {@link #BUILD_PATH} directory, but it is now located in the
 * {@link BaseTestCase#SOURCE_PATH} directory.
 * <p>
 * The wild card in SourceLookup*.exe can be one of the following to cover the
 * different effective types of source lookups that need to be done depending on
 * how the program was compiled. Each of these options produces different debug
 * information about where to find the source file. See the Makefile for more
 * information.
 * <ul>
 * <li><b>AC</b>: Absolute and Canonical path (no ../ in path passed to GCC)
 * </li>
 * <li><b>AN</b>: Absolute and Non-Canonical path (a ../ in path passed to GCC)
 * </li>
 * <li><b>RC</b>: Relative and Canonical path (no ../ in path passed to GCC)
 * </li>
 * <li><b>RN</b>: Relative and Non-Canonical path (a ../ in path passed to GCC)
 * </li>
 * <li><b>No suffix</b>: Compilation that does not need mapping to be found
 * </ul>
 * In addition, there can also be a <b>Dwarf2</b> in the name. That means it is
 * designed to run with GDB <= 7.4, see comment in Makefile for OLDDWARFFLAGS.
 * <p>
 * The result of the variations on compilation arguments means that some of the
 * tests are parameterised.
 * <p>
 * Some of the CDT source lookup features require newer versions of GDB than
 * others, therefore the relevant tests use assumeGdbVersion* methods to be
 * skipped when appropriate.
 */
@RunWith(Parameterized.class)
public class SourceLookupTest extends BaseParametrizedTestCase {
	protected static final String BUILD_PATH = "data/launch/build/";
	protected static final String BUILD2_PATH = "data/launch/build2/";
	protected static final String SOURCE_NAME = "SourceLookup.cc"; //$NON-NLS-1$
	protected static final int SOURCE_LINE = 15;
	/** Compiled with absolute and canonical path to SourceLookup.cc */
	protected String EXEC_AC_NAME;
	/** Compiled with absolute and non-canonical path to SourceLookup.cc */
	protected String EXEC_AN_NAME;
	/** Compiled with relative and canonical path to SourceLookup.cc */
	protected String EXEC_RC_NAME;
	/** Compiled with relative and non-canonical path to SourceLookup.cc */
	protected String EXEC_RN_NAME;
	/**
	 * File compiled with SourceLookup.cc in the src directory, so GDB resolves
	 * it without help.
	 */
	protected String EXEC_NAME;

	/**
	 * For version of GDB <= 7.4 we need to use the strict dwarf2 flags. See
	 * comment in Makefile on OLDDWARFFLAGS.
	 */
	protected void setExeNames() {
		String gdbVersion = getGdbVersion();
		// has to be strictly lower
		boolean isLower = LaunchUtils.compareVersions(ITestConstants.SUFFIX_GDB_7_5, gdbVersion) > 0;
		if (isLower) {
			EXEC_AC_NAME = "SourceLookupDwarf2AC.exe"; //$NON-NLS-1$
			EXEC_AN_NAME = "SourceLookupDwarf2AN.exe"; //$NON-NLS-1$
			EXEC_RC_NAME = "SourceLookupDwarf2RC.exe"; //$NON-NLS-1$
			EXEC_RN_NAME = "SourceLookupDwarf2RN.exe"; //$NON-NLS-1$
			EXEC_NAME = "SourceLookupDwarf2.exe"; //$NON-NLS-1$
		} else {
			EXEC_AC_NAME = "SourceLookupAC.exe"; //$NON-NLS-1$
			EXEC_AN_NAME = "SourceLookupAN.exe"; //$NON-NLS-1$
			EXEC_RC_NAME = "SourceLookupRC.exe"; //$NON-NLS-1$
			EXEC_RN_NAME = "SourceLookupRN.exe"; //$NON-NLS-1$
			EXEC_NAME = "SourceLookup.exe"; //$NON-NLS-1$
		}
	}

	protected static final String SOURCE_ABSPATH = new File(SOURCE_PATH).getAbsolutePath();
	protected static final String BUILD_ABSPATH = new File(BUILD_PATH).getAbsolutePath();
	/** This path matches the non-canonical path used to build the *N.exe's */
	protected static final String BUILD_NONCANONICAL_PATH = new File(new File(BUILD2_PATH).getAbsolutePath(),
			"../build/").toString();

	/**
	 * Map entry for non-canonical build dirs
	 */
	protected MapEntrySourceContainer fMapEntrySourceContainerN = new MapEntrySourceContainer(BUILD_NONCANONICAL_PATH,
			new Path(SOURCE_ABSPATH));
	/**
	 * Map entry for canonical build dirs
	 */
	protected MapEntrySourceContainer fMapEntrySourceContainerC = new MapEntrySourceContainer(BUILD_ABSPATH,
			new Path(SOURCE_ABSPATH));

	protected AsyncCompletionWaitor fBreakpointInstalledWait = new AsyncCompletionWaitor();
	protected IBreakpointListener fBreakpointListener = new IBreakpointListener() {
		@Override
		public void breakpointAdded(IBreakpoint breakpoint) {
		}

		@Override
		public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		}

		@Override
		public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
			if (breakpoint instanceof ICBreakpoint) {
				try {
					if (((ICBreakpoint) breakpoint).isInstalled()) {
						fBreakpointInstalledWait.waitFinished();
					}
				} catch (CoreException e) {
				}
			}
		}
	};

	private IGDBControl fCommandControl;
	private CommandFactory fCommandFactory;

	@Override
	public void doBeforeTest() throws Exception {
		removeTeminatedLaunchesBeforeTest();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener(fBreakpointListener);
		setExeNames();
		super.setLaunchAttributes();
		// executable and source lookup attributes are custom per test,
		// so delay launch until they are setup
	}

	/**
	 * Remove any platform breakpoints that have been created.
	 */
	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		removeAllPlatformBreakpoints();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.removeBreakpointListener(fBreakpointListener);
	}

	protected void doLaunch(String programName) throws Exception {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, programName);
		super.doLaunch();

		final DsfSession session = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
			fCommandControl = tracker.getService(IGDBControl.class);
			fCommandFactory = fCommandControl.getCommandFactory();
			tracker.dispose();
		};
		session.getExecutor().submit(runnable).get();
	}

	@Override
	protected void doLaunch() throws Exception {
		throw new RuntimeException("Within this test you must use doLaunch(String) to setup program");
	}

	/**
	 * Return true if file refers to the real source file.
	 */
	protected boolean fileExists(String file) {
		if (!file.endsWith(SOURCE_NAME))
			return false;
		return Files.exists(Paths.get(file));
	}

	/**
	 * Custom assertion that neither GDB nor the Source Locator found the source
	 * file.
	 */
	protected void assertSourceNotFound() throws Throwable {
		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertNull("Source Locator unexpectedly found the source", sourceElement);

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB Unexpectedly located the source", fileExists(frameData.getFile()));

		// Check file as resolved by ISourceLookup service
		try {
			SyncUtil.getSource(frameData.getFile());
			fail("Source Lookup service unexpectedly found the source");
		} catch (ExecutionException e) {
			assertNotNull(e.getCause());
			assertTrue(e.getCause() instanceof CoreException);
			assertEquals("No sources found", e.getCause().getMessage());
		}
	}

	/**
	 * Custom assertion that GDB did not find the source, but the Source Locator
	 * found the source file.
	 */
	protected void assertSourceFoundByDirectorOnly() throws Throwable {
		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertTrue("Source locator failed to find source", sourceElement instanceof IStorage);

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB Unexpectedly located the source", fileExists(frameData.getFile()));

		// Check file as resolved by ISourceLookup service
		sourceElement = SyncUtil.getSource(frameData.getFile());
		assertTrue("Source Lookup service failed to find source", sourceElement instanceof IStorage);
	}

	/**
	 * Custom assertion that GDB and the Source Locator found the source file.
	 */
	protected void assertSourceFound() throws Throwable {
		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertTrue("Source locator failed to find source", sourceElement instanceof IStorage);

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertTrue("GDB failed to find source", fileExists(frameData.getFile()));

		// Check file as resolved by ISourceLookup service
		sourceElement = SyncUtil.getSource(frameData.getFile());
		assertTrue("Source Lookup service failed to find source", sourceElement instanceof IStorage);
	}

	/**
	 * Test with only default source locators on, that the source file was not
	 * found when stopped at main by any of GDB directly, with the source lookup
	 * director, or via the ISourceLookup service.
	 */
	@Test
	public void defaultSourceLookup() throws Throwable {
		doLaunch(EXEC_PATH + EXEC_RC_NAME);
		assertSourceNotFound();
	}

	protected AbstractSourceLookupDirector setSourceContainer(ISourceContainer container) throws CoreException {
		AbstractSourceLookupDirector director = (AbstractSourceLookupDirector) DebugPlugin.getDefault()
				.getLaunchManager().newSourceLocator("org.eclipse.cdt.debug.core.sourceLocator");
		addSourceContainer(director, new DefaultSourceContainer());
		addSourceContainer(director, container);
		return director;
	}

	protected void addSourceContainer(AbstractSourceLookupDirector director, ISourceContainer container)
			throws CoreException {

		ArrayList<ISourceContainer> containerList = new ArrayList<>(Arrays.asList(director.getSourceContainers()));
		container.init(director);
		containerList.add(container);
		director.setSourceContainers(containerList.toArray(new ISourceContainer[containerList.size()]));
		setLaunchAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, director.getMemento());
		setLaunchAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, director.getId());
	}

	/**
	 * Add the mapping source container to the common source lookup
	 */
	protected void doMappingInCommon(boolean canonical, boolean withBackend) {
		CSourceLookupDirector commonSourceLookupDirector = CDebugCorePlugin.getDefault()
				.getCommonSourceLookupDirector();
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.setIsMappingWithBackendEnabled(withBackend);
		if (canonical) {
			mapContainer.addMapEntry(fMapEntrySourceContainerC);
		} else {
			mapContainer.addMapEntry(fMapEntrySourceContainerN);
		}
		ArrayList<ISourceContainer> containerList = new ArrayList<>(
				Arrays.asList(commonSourceLookupDirector.getSourceContainers()));
		containerList.add(mapContainer);
		commonSourceLookupDirector
				.setSourceContainers(containerList.toArray(new ISourceContainer[containerList.size()]));
	}

	/**
	 * Resource common source container to the default
	 */
	protected void restoreCommonToDefault() {
		CSourceLookupDirector commonSourceLookupDirector = CDebugCorePlugin.getDefault()
				.getCommonSourceLookupDirector();
		ISourceContainer[] containers = new ISourceContainer[3];
		int i = 0;
		containers[i++] = new AbsolutePathSourceContainer();
		containers[i++] = new ProgramRelativePathSourceContainer();
		containers[i++] = new CProjectSourceContainer(null, true);
		commonSourceLookupDirector.setSourceContainers(containers);
	}

	/**
	 * Set default source locators and a path mapping
	 * {@link MappingSourceContainer} from BUILD_ABSPATH -> SOURCE_ABSPATH and
	 * do the launch
	 */
	protected void doMappingAndLaunch(String programName, boolean withBackend) throws CoreException, Exception {
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.setIsMappingWithBackendEnabled(withBackend);
		if (programName.endsWith("N.exe")) {
			mapContainer.addMapEntry(fMapEntrySourceContainerN);
		} else if (programName.endsWith("C.exe")) {
			mapContainer.addMapEntry(fMapEntrySourceContainerC);
		} else {
			fail("Unexpected file");
		}
		setSourceContainer(mapContainer);
		doLaunch(EXEC_PATH + programName);
	}

	/**
	 * Mapping test common.
	 *
	 * If backend is used for mapping then every layer should be able to find
	 * source.
	 *
	 * If backned is not used for mapping then only once the source lookup
	 * director gets involved should the source be found as GDB will not know
	 * how to find it on its own.
	 */
	protected void sourceMapping(String programName, boolean withBackend) throws Throwable {
		doMappingAndLaunch(programName, withBackend);
		if (withBackend) {
			assertSourceFound();
		} else {
			assertSourceFoundByDirectorOnly();
		}
	}

	/**
	 * With mapping test breakpoints can be inserted.
	 */
	protected void sourceMappingBreakpoints(String programName, boolean withBackend) throws Throwable {
		doMappingAndLaunch(programName, withBackend);

		assertInsertBreakpointSuccessful();
	}

	/**
	 * Assert that a breakpoint can be successfully inserted. To successfully
	 * insert a breakpoint it means the the mapping of local file names to
	 * compilation paths is working properly.
	 */
	protected void assertInsertBreakpointSuccessful() throws Throwable {
		// insert breakpoint in source file
		fBreakpointInstalledWait.waitReset();
		ICLineBreakpoint bp = CDIDebugModel.createLineBreakpoint(
				new Path(SOURCE_ABSPATH).append(SOURCE_NAME).toOSString(), ResourcesPlugin.getWorkspace().getRoot(),
				ICBreakpointType.REGULAR, SOURCE_LINE, true, 0, "", true);
		// The delay here is based on:
		// 1) The installation of the breakpoint takes some time
		// 2) The notification of the IBreakpoint change needs the autobuild
		// to run, and it has up to a 1 second delay (depending on how
		// long since it last ran). See
		// org.eclipse.core.internal.events.AutoBuildJob.computeScheduleDelay()
		fBreakpointInstalledWait.waitUntilDone(TestsPlugin.massageTimeout(2000));
		assertTrue("Breakpoint failed to install", bp.isInstalled());
	}

	/**
	 * Tests that GDB >= 7.6 because DSF is using the full path name to pass to
	 * the {@link ISourceContainer#findSourceElements(String)}. In versions
	 * prior to 7.6 the fullname field was not returned from GDB if the file was
	 * not found by GDB. See
	 * <a href= "https://sourceware.org/ml/gdb-patches/2012-12/msg00557.html">
	 * the mailing list</a> and associated <a href=
	 * "https://sourceware.org/git/gitweb.cgi?p=binutils-gdb.git;a=commitdiff;h=ec83d2110de6831ac2ed0e5a56dc33c60a477eb6">
	 * gdb/NEWS item</a> (although you have to dig quite deep on these changes.)
	 *
	 * Therefore in version < 7.6 the MI frame info has file="SourceLookup.cc"
	 * and no fullname field. This means there is no path to source map against.
	 *
	 * In version >= 7.6 the MI frame info has file="SourceLookup.cc",fullname=
	 * "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/build/SourceLookup.cc"
	 * fields, so there is a path to do the mapping against. Recall that the
	 * test maps
	 * "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/build"
	 * to "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/src"
	 */
	protected void assumeGdbVersionFullnameWorking() {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_6);
	}

	/**
	 * Inverse of {@link #assumeGdbVersionFullnameWorking()}
	 */
	protected void assumeGdbVersionFullnameNotWorking() {
		assumeGdbVersionLowerThen(ITestConstants.SUFFIX_GDB_7_6);
	}

	/**
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingAC() throws Throwable {
		assumeGdbVersionFullnameWorking();
		sourceMapping(EXEC_AC_NAME, false);
	}

	/**
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteAC() throws Throwable {
		sourceMapping(EXEC_AC_NAME, true);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Test
	public void sourceMappingAN() throws Throwable {
		assumeGdbVersionFullnameWorking();
		sourceMapping(EXEC_AN_NAME, false);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Test
	public void sourceSubstituteAN() throws Throwable {
		/*
		 * GDB < 6.8 does not work correctly with substitute-paths with .. in
		 * the build path when the build path is an absolute path. GDB 6.8 and
		 * above works fine in this case.
		 */
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_6_8);
		sourceMapping(EXEC_AN_NAME, true);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingRC() throws Throwable {
		assumeGdbVersionFullnameWorking();
		sourceMapping(EXEC_RC_NAME, false);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteRC() throws Throwable {
		sourceMapping(EXEC_RC_NAME, true);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Test
	public void sourceMappingRN() throws Throwable {
		assumeGdbVersionFullnameWorking();
		sourceMapping(EXEC_RN_NAME, false);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Test
	public void sourceSubstituteRN() throws Throwable {
		/*
		 * GDB < 7.6 does not work correctly with substitute-paths with .. in
		 * the build path when the build path is a relative path. GDB 7.6 and
		 * above works fine in this case.
		 */
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_6);
		sourceMapping(EXEC_RN_NAME, true);
	}

	/**
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingBreakpointsAC() throws Throwable {
		assumeGdbVersionFullnameWorking();
		sourceMappingBreakpoints(EXEC_AC_NAME, false);
	}

	/**
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteBreakpointsAC() throws Throwable {
		sourceMappingBreakpoints(EXEC_AC_NAME, true);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	@Test
	public void sourceMappingBreakpointsAN() throws Throwable {
		sourceMappingBreakpoints(EXEC_AN_NAME, false);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Test
	public void sourceSubstituteBreakpointsAN() throws Throwable {
		/*
		 * GDB < 6.8 does not work correctly with substitute-paths with .. in
		 * the build path when the build path is an absolute path. GDB 6.8 and
		 * above works fine in this case.
		 */
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_6_8);
		sourceMappingBreakpoints(EXEC_AN_NAME, true);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingBreakpointsRC() throws Throwable {
		assumeGdbVersionFullnameWorking();
		sourceMappingBreakpoints(EXEC_RC_NAME, false);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteBreakpointsRC() throws Throwable {
		sourceMappingBreakpoints(EXEC_RC_NAME, true);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	@Test
	public void sourceMappingBreakpointsRN() throws Throwable {
		sourceMappingBreakpoints(EXEC_RN_NAME, false);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Test
	public void sourceSubstituteBreakpointsRN() throws Throwable {
		/*
		 * GDB < 7.6 does not work correctly with substitute-paths with .. in
		 * the build path when the build path is a relative path. GDB 7.6 and
		 * above works fine in this case.
		 */
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_6);
		sourceMappingBreakpoints(EXEC_RN_NAME, true);
	}

	/**
	 * Change directory to the binary (aka EXEC_PATH)
	 */
	protected void doCdToBinDir() throws Exception {
		Query<MIInfo> query = new Query<MIInfo>() {
			@Override
			protected void execute(DataRequestMonitor<MIInfo> rm) {
				fCommandControl.queueCommand(fCommandFactory.createMIEnvironmentCD(fCommandControl.getContext(),
						new File(EXEC_PATH).getAbsolutePath()), rm);
			}
		};

		fCommandControl.getExecutor().execute(query);
		query.get();
	}

	/**
	 * Test that if the user changes the source mappings in the middle of a
	 * debug session (e.g. with CSourceNotFoundEditor) that the lookups are
	 * updated.
	 */
	public void sourceMappingChangesHelper(boolean withBackend) throws Throwable {
		doMappingAndLaunch(EXEC_AC_NAME, withBackend);

		DsfSourceLookupDirector sourceLocator = (DsfSourceLookupDirector) getGDBLaunch().getSourceLocator();
		MapEntrySourceContainer incorrectMapEntry = new MapEntrySourceContainer(BUILD_ABSPATH + "/incorrectsubpath",
				new Path(SOURCE_ABSPATH));

		if (withBackend) {
			assertSourceFound();
		} else {
			assertSourceFoundByDirectorOnly();
		}

		// Change the source mappings
		ISourceContainer[] containers = sourceLocator.getSourceContainers();
		MappingSourceContainer mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(fMapEntrySourceContainerC);
		mappingSourceContainer.addMapEntry(incorrectMapEntry);
		sourceLocator.setSourceContainers(containers);

		/*
		 * GDB (pre 7.0) changes the current directory when the above source is
		 * found. As a result GDB is able to find the source even though we have
		 * changed the source lookup paths. To make sure that GDB is really
		 * doing a substitution rather than looking in current directory, change
		 * the current directory. Without this, the assertSourceNotFound fails
		 * because GDB unexpectedly finds the source (for the wrong reason).
		 */
		if (withBackend) {
			doCdToBinDir();
		}

		assertSourceNotFound();

		// Change the source mappings back
		containers = sourceLocator.getSourceContainers();
		mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(incorrectMapEntry);
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainerC);
		sourceLocator.setSourceContainers(containers);

		if (withBackend) {
			assertSourceFound();
		} else {
			assertSourceFoundByDirectorOnly();
		}
	}

	@Test
	public void sourceMappingChanges() throws Throwable {
		sourceMappingChangesHelper(false);
	}

	@Test
	public void sourceSubstituteChanges() throws Throwable {
		sourceMappingChangesHelper(true);
	}

	/**
	 * Test that if the user changes the source mappings in the middle of a
	 * debug session (e.g. with CSourceNotFoundEditor) that the lookups are
	 * updated.
	 *
	 * This version is for a new source mapping where there wasn't one
	 * previously.
	 */
	public void sourceMappingAddedHelper(boolean withBackend) throws Throwable {
		doLaunch(EXEC_PATH + EXEC_AC_NAME);

		assertSourceNotFound();

		// Set the source mappings
		DsfSourceLookupDirector sourceLocator = (DsfSourceLookupDirector) getGDBLaunch().getSourceLocator();
		ISourceContainer[] containers = sourceLocator.getSourceContainers();
		MappingSourceContainer mappingSourceContainer = new MappingSourceContainer("Mappings");
		mappingSourceContainer.setIsMappingWithBackendEnabled(withBackend);
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainerC);
		ISourceContainer[] newContainers = new ISourceContainer[containers.length + 1];
		System.arraycopy(containers, 0, newContainers, 0, containers.length);
		newContainers[newContainers.length - 1] = mappingSourceContainer;
		sourceLocator.setSourceContainers(newContainers);

		if (withBackend) {
			assertSourceFound();
		} else {
			assertSourceFoundByDirectorOnly();
		}
	}

	@Test
	public void sourceMappingAdded() throws Throwable {
		sourceMappingAddedHelper(false);
	}

	@Test
	public void sourceSubstituteAdded() throws Throwable {
		sourceMappingAddedHelper(true);
	}

	/**
	 * Test with default source locators and a {@link DirectorySourceContainer}
	 * for SOURCE_ABSPATH that GDB does not locate the file, but the source
	 * lookup director and the source lookup service do find the file.
	 *
	 * This test does not work with modern GDBs because the path passed into
	 * DirectorySourceContainer is an absolute path. See versioned test suites.
	 */
	@Test
	public void directorySource() throws Throwable {
		/*
		 * DirectorySourceContainer only works if there is no fullname coming
		 * from GDB
		 */
		assumeGdbVersionFullnameNotWorking();
		DirectorySourceContainer container = new DirectorySourceContainer(new Path(SOURCE_ABSPATH), false);
		setSourceContainer(container);
		doLaunch(EXEC_PATH + EXEC_RC_NAME);
		assertSourceFoundByDirectorOnly();
	}

	/**
	 * Create an IBinary with the minimum necessary for use in
	 * org.eclipse.cdt.debug.internal.core.srcfinder.CSourceFinder.
	 *
	 * A mock is used to avoid having to set up the significant of glue
	 * necessary to create a real IBinary. All that CSourceFinder needs is the
	 * path to the file.
	 */
	protected IBinary createMockIBinary(String path) {
		IPath absPath = new Path(new File(path).getAbsolutePath());

		IResource exeResource = mock(IResource.class);
		when(exeResource.getFullPath()).thenReturn(absPath);
		when(exeResource.getProject()).thenReturn(null);

		IBinary exeBin = mock(IBinary.class);
		when(exeBin.getPath()).thenReturn(absPath);
		when(exeBin.getAdapter(IResource.class)).thenReturn(exeResource);
		/*
		 * we use the adapter factory CSourceFinderFactory to convert IBinary to
		 * ISourceFinder. The way the adapter is resolved it will first try and
		 * and get the adapter from the IBinary (IBinary extends IAdaptable) so
		 * we need to return null here for a clean failure. If we didn't
		 * explicitly provide the null, an exception would be raised because an
		 * unexpected method is invoked.
		 */
		when(exeBin.getAdapter(ISourceFinder.class)).thenReturn(null);
		return exeBin;
	}

	/**
	 * Assert that the finder is able resolve the source file name
	 */
	protected void assertFinderFinds(String programName, String buildAbspath) {
		IBinary binary = createMockIBinary(EXEC_PATH + programName);
		ISourceFinder finder = Adapters.adapt(binary, ISourceFinder.class, true);
		try {
			String localPath = finder.toLocalPath(buildAbspath);
			assertEquals("Source Finder failed to find file", new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath(),
					localPath);
		} finally {
			finder.dispose();
		}
	}

	/**
	 * Assert that the finder is not able resolve the source file name
	 */
	protected void assertFinderDoesNotFind(String programName, String buildAbspath) {
		IBinary binary = createMockIBinary(EXEC_PATH + programName);
		ISourceFinder finder = Adapters.adapt(binary, ISourceFinder.class, true);
		try {
			String localPath = finder.toLocalPath(buildAbspath);
			assertNotEquals("Source Finder unexpectedly found file",
					new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath(), localPath);
		} finally {
			finder.dispose();
		}
	}

	/**
	 * Test the base case of the source finder, when it does not need any
	 * special help like mapping to find a file.
	 */
	@Test
	public void sourceFinder() throws Throwable {
		assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
	}

	/**
	 * Test the CSourceFinder's use of source lookup when there is an active
	 * launch.
	 *
	 * In this case, the DSF specific director created as part of the launch
	 * gets used.
	 */
	public void sourceFinderMappingAC_ActiveLaunchHelper(boolean withBackend) throws Throwable {
		assertFinderDoesNotFind(EXEC_AC_NAME, new File(BUILD_PATH, SOURCE_NAME).getAbsolutePath());
		doMappingAndLaunch(EXEC_AC_NAME, withBackend);
		assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
	}

	@Test
	public void sourceFinderMappingAC_ActiveLaunch() throws Throwable {
		sourceFinderMappingAC_ActiveLaunchHelper(false);
	}

	@Test
	public void sourceFinderSubstituteAC_ActiveLaunch() throws Throwable {
		sourceFinderMappingAC_ActiveLaunchHelper(true);
	}

	/**
	 * Test the CSourceFinder's use of source lookup when there is a terminated
	 * launch.
	 *
	 * In this case, the DSF specific director created as part of the launch
	 * gets used.
	 */
	public void sourceFinderMappingAC_TerminatedLaunchHelper(boolean withBackend) throws Throwable {
		sourceFinderMappingAC_ActiveLaunchHelper(withBackend);

		// Terminate the launch, but don't remove it
		doAfterTest();
		assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
	}

	@Test
	public void sourceFinderMappingAC_TerminatedLaunch() throws Throwable {
		sourceFinderMappingAC_TerminatedLaunchHelper(false);
	}

	@Test
	public void sourceFinderSubstituteAC_TerminatedLaunch() throws Throwable {
		sourceFinderMappingAC_ActiveLaunchHelper(true);
	}

	/**
	 * Test the CSourceFinder's use of source lookup when there is a not active
	 * launch, but a launch configuration that can be used.
	 *
	 * In this case, the c general director created as part of the launch gets
	 * used.
	 */
	public void sourceFinderMappingAC_LaunchConfigHelper(boolean withBackend) throws Throwable {
		sourceFinderMappingAC_TerminatedLaunchHelper(withBackend);

		// Remove the launch, so that we can test with the existing
		// configuration
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		assertEquals("Unexpected number of launches", 1, launches.length);
		assertTrue(launches[0].isTerminated());
		launchManager.removeLaunches(launches);

		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations();
		assertEquals("Unexpected number of launch configuration", 1, launchConfigurations.length);

		assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
	}

	@Test
	public void sourceFinderMappingAC_LaunchConfig() throws Throwable {
		removeLaunchConfigurationsBeforeTest();
		sourceFinderMappingAC_LaunchConfigHelper(false);
	}

	@Test
	public void sourceFinderSubstituteAC_LaunchConfig() throws Throwable {
		removeLaunchConfigurationsBeforeTest();
		sourceFinderMappingAC_LaunchConfigHelper(true);
	}

	/**
	 * Test that CSourceFinder works with the common source director, i.e. no
	 * launches or launch configs in place.
	 */
	public void sourceFinderMappingAC_CommonLocatorHelper(boolean withBackend) throws Throwable {
		assertFinderDoesNotFind(EXEC_AC_NAME, new File(BUILD_PATH, SOURCE_NAME).getAbsolutePath());

		doMappingInCommon(true, withBackend);
		try {
			assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
		} finally {
			restoreCommonToDefault();
		}

		assertFinderDoesNotFind(EXEC_AC_NAME, new File(BUILD_PATH, SOURCE_NAME).getAbsolutePath());
	}

	@Test
	public void sourceFinderMappingAC_CommonLocator() throws Throwable {
		sourceFinderMappingAC_CommonLocatorHelper(false);
	}

	@Test
	public void sourceFinderSubstituteAC_CommonLocator() throws Throwable {
		sourceFinderMappingAC_CommonLocatorHelper(true);
	}

	/**
	 * This test verifies that doing a source lookup where the absolute name of
	 * the file is provided by the backend resolves.
	 *
	 * In the normal DSF case
	 * {@link ISourceLookupDirector#findSourceElements(Object)} is called with a
	 * {@link IDMContext}, e.g. a stack frame DMC.
	 *
	 * However, the disassembly view/editor does the lookup on a String (it
	 * passes the result of {@link MIMixedInstruction#getFileName()} to
	 * findSourceElements).
	 *
	 * In both the CDI and DSF participants there is special handling to ensure
	 * that absolute file names are resolved even if there are no source
	 * containers in the launch configuration.
	 */
	@Test
	public void noExplicitSourceContainers() throws Throwable {
		// create a director with no containers so that the memento can be
		// created.
		AbstractSourceLookupDirector tmpDirector = (AbstractSourceLookupDirector) DebugPlugin.getDefault()
				.getLaunchManager().newSourceLocator("org.eclipse.cdt.debug.core.sourceLocator");
		tmpDirector.setSourceContainers(new ISourceContainer[0]);
		setLaunchAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, tmpDirector.getMemento());
		setLaunchAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, tmpDirector.getId());

		// We are using the version of the executable that is resolvable, i.e
		// the one that has not had its source file moved since we compiled.
		doLaunch(EXEC_PATH + EXEC_NAME);

		assertSourceFound();
	}

	/**
	 * Test verifies interaction between director that has two mappers, one with
	 * backend enabled and one without with the second being the only valid one.
	 */
	@Test
	public void twoMappersSecondValid() throws Throwable {
		MappingSourceContainer substituteContainer = new MappingSourceContainer("Mappings With Backend");
		substituteContainer.setIsMappingWithBackendEnabled(true);
		/*
		 * the entry here does not matter, as long as it is not the valid
		 * substitution, we want to make sure that we process the other
		 * MappingSourceContainer correctly
		 */
		substituteContainer.addMapEntry(new MapEntrySourceContainer("/from_invalid", new Path("/to_invalid")));
		AbstractSourceLookupDirector director = setSourceContainer(substituteContainer);

		// this is the mapping we want to do the work
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.setIsMappingWithBackendEnabled(false);
		mapContainer.addMapEntry(fMapEntrySourceContainerC);
		addSourceContainer(director, mapContainer);

		doLaunch(EXEC_PATH + EXEC_AC_NAME);

		/*
		 * because the backend substitution does not apply, we resolve with the
		 * CDT mapping, in this case that means that only the director should
		 * locate the source.
		 */
		assertSourceFoundByDirectorOnly();
		assertInsertBreakpointSuccessful();
	}

	/**
	 * Test verifies interaction between director that has two mappers, one with
	 * backend enabled and one without, with the first being the only valid one,
	 * and the second causing the breakpoint installation to fail.
	 */
	@Test
	public void twoMappersFirstValid() throws Throwable {
		// This first mapping is valid and should cause to find the source
		MappingSourceContainer substituteContainer = new MappingSourceContainer("Mappings With Backend");
		substituteContainer.setIsMappingWithBackendEnabled(true);
		substituteContainer.addMapEntry(fMapEntrySourceContainerC);
		AbstractSourceLookupDirector director = setSourceContainer(substituteContainer);

		/*
		 * Because of the above valid mapping substitution, GDB will provide the
		 * proper path to the source and it will be found no matter what the
		 * below mapping is set to. On the other hand, when setting a
		 * breakpoint, we have to make sure that the below mapping does not
		 * change the path to something GDB does not know. Therefore, we set the
		 * below mapping from an invalid compilation path to the proper source
		 * path. This is so that if the below mapping is triggered it will cause
		 * us to try to set a breakpoint in GDB on an invalid path, thus failing
		 * the test. This allows to verify that the first mapping is used once
		 * it is found to be valid and does not fallback to the next mapping.
		 */
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.setIsMappingWithBackendEnabled(false);
		mapContainer.addMapEntry(new MapEntrySourceContainer("/from_invalid", new Path(SOURCE_ABSPATH)));
		addSourceContainer(director, mapContainer);

		doLaunch(EXEC_PATH + EXEC_AC_NAME);

		/*
		 * because the backend substitution applies, we should be able to find
		 * the source with the director or without it.
		 */
		assertSourceFound();
		assertInsertBreakpointSuccessful();
	}

	/**
	 * Terminate the session on the executor thread without blocking.
	 *
	 * This models the way DsfTerminateCommand terminates the launches.
	 */
	protected void terminateAsync(DsfSession session) throws Exception {
		DsfExecutor executor = session.getExecutor();
		executor.execute(() -> {
			DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
			IGDBControl commandControl = tracker.getService(IGDBControl.class);
			tracker.dispose();
			commandControl.terminate(new RequestMonitor(executor, null));
		});
	}

	/**
	 * Test that two launches can be launched and terminated without becoming
	 * interlocked.
	 *
	 * This is a regression test for Bug 494650.
	 *
	 * XXX: If this test fails as it did for the reason of Bug 494650, there is
	 * deadlock and the JVM does not recover, causing this test to timeout and
	 * all subsequent tests not to work.
	 */
	@Test
	public void twoLaunchesTerminate() throws Throwable {
		Assume.assumeFalse("Test framework only supports multiple launches for non-remote", remote);
		// Launch first session
		doLaunch(EXEC_PATH + EXEC_NAME);
		GdbLaunch launch1 = getGDBLaunch();
		// Launch additional session with same launch configuration
		GdbLaunch launch2 = doLaunchInner();

		/*
		 * Bug 494650 affects when two launches are terminated too close
		 * together. In normal operation that means that the two terminates is
		 * sufficient. However, it can happen that the first one terminates
		 * progresses sufficiently far that the deadlock does not happen on the
		 * second.
		 *
		 * NOTE: Can't use launch.terminate() here because it terminates
		 * synchronously when adapters are not installed. Instead we need to
		 * issue the terminates in a non-blocking way on both the the executor
		 * threads, the way that terminate works when adapters are installed.
		 */
		terminateAsync(launch1.getSession());
		terminateAsync(launch2.getSession());

		/*
		 * In Bug 494650 the UI locks up because the executor thread of both
		 * sessions is waiting on each other and the UI thread is waiting on the
		 * executor thread. The UI thread is waiting by using a Query, and
		 * before the bug fix the two executor threads were waiting on each
		 * other using a Query too.
		 *
		 * This test does not use the UI thread (aka main), but instead the
		 * JUnit test thread. We determine success if both launches terminate,
		 * because if they both terminate they have stayed responsive and
		 * successfully completed the entire shutdown sequences without
		 * deadlocking.
		 */
		waitUntil("Timeout waiting for launches to terminate", () -> launch1.isTerminated() && launch2.isTerminated());
	}

	/**
	 * Helper method that actually performs the test/assertions for
	 * {@link IDebugSourceFiles#getSources(IDMContext, DataRequestMonitor)} tests.
	 */
	private void testGetSourcesListInner(String path) throws Throwable {
		IDebugSourceFileInfo[] sources = SyncUtil.getSources(SyncUtil.getContainerContext());
		String expectedPath = Paths.get(path, SOURCE_NAME).toString();
		boolean anyMatch = Arrays.asList(sources).stream().anyMatch(source -> {
			return source.getPath().equals(expectedPath);
		});
		assertTrue(anyMatch);
	}

	/**
	 * Test for {@link IDebugSourceFiles#getSources(IDMContext, DataRequestMonitor)}
	 * with source path substitution on. Therefore make sure there is an entry
	 * for the resolved source path of {@value #SOURCE_NAME}
	 */
	private void testGetSourcesList(String execName) throws Throwable {
		doMappingAndLaunch(execName, true);
		testGetSourcesListInner(SOURCE_ABSPATH);
	}

	/**
	 * Test for {@link IDebugSourceFiles#getSources(IDMContext, DataRequestMonitor)}
	 * with no source path substitution on. Therefore make sure there is an entry
	 * for the build path of {@value #SOURCE_NAME}
	 */
	@Test
	public void testGetSourcesListNoSourceLookup() throws Throwable {
		doLaunch(EXEC_PATH + EXEC_AC_NAME);
		testGetSourcesListInner(BUILD_ABSPATH);
	}

	@Test
	public void testGetSourcesListAC() throws Throwable {
		testGetSourcesList(EXEC_AC_NAME);
	}

	@Test
	public void testGetSourcesListAN() throws Throwable {
		testGetSourcesList(EXEC_AN_NAME);
	}

	@Test
	public void testGetSourcesListRC() throws Throwable {
		testGetSourcesList(EXEC_RC_NAME);
	}

	@Test
	public void testGetSourcesListRN() throws Throwable {
		testGetSourcesList(EXEC_RN_NAME);
	}
}
