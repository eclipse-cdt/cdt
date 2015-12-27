/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that interaction with source lookups works as expected.
 * 
 * All of these tests use one of SourceLookup*.exe that was built from a file
 * that was "moved" since build time. At build time the SourceLookup.cc file was
 * located in the {@link #BUILD_PATH} directory, but it is now located in the
 * {@link BaseTestCase#SOURCE_PATH} directory.
 * 
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
 * 
 * The result of the variations on compilation arguments means that some of the
 * tests are parameterised.
 * 
 * Some of the CDT source lookup features require newer versions of GDB than
 * others, therefore the relevant tests are ignored as needed in the subclasses
 * of {@link SourceLookupTest}.
 */
@RunWith(BackgroundRunner.class)
public class SourceLookupTest extends BaseTestCase {
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

	protected static final String SOURCE_ABSPATH = new File(SOURCE_PATH).getAbsolutePath();
	protected static final String BUILD_ABSPATH = new File(BUILD_PATH).getAbsolutePath();
	/** This path matches the non-canonical path used to build the *N.exe's */
	protected static final String BUILD_NONCANONICAL_PATH = new File(new File(BUILD2_PATH).getAbsolutePath(),
			"../build/").toString();

	/**
	 * Map entry for non-canonical build dirs
	 */
	protected MapEntrySourceContainer fMapEntrySourceContainerN = new MapEntrySourceContainer(
			new Path(BUILD_NONCANONICAL_PATH), new Path(SOURCE_ABSPATH));
	/**
	 * Map entry for canonical build dirs
	 */
	protected MapEntrySourceContainer fMapEntrySourceContainerC = new MapEntrySourceContainer(new Path(BUILD_ABSPATH),
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

	@Override
	public void doBeforeTest() throws Exception {
		removeTeminatedLaunchesBeforeTest();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		manager.addBreakpointListener(fBreakpointListener);
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

		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints();
		manager.removeBreakpoints(breakpoints, true);

		manager.removeBreakpointListener(fBreakpointListener);
	}

	protected void doLaunch(String programName) throws Exception {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, programName);
		super.doLaunch();
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
		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB Unexpectedly located the source", fileExists(frameData.getFile()));

		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertNull("Source Locator unexpectedly found the source", sourceElement);

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
		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB Unexpectedly located the source", fileExists(frameData.getFile()));

		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertTrue("Source locator failed to find source", sourceElement instanceof IStorage);

		// Check file as resolved by ISourceLookup service
		sourceElement = SyncUtil.getSource(frameData.getFile());
		assertTrue("Source Lookup service failed to find source", sourceElement instanceof IStorage);
	}

	/**
	 * Custom assertion that GDB and the Source Locator found the source file.
	 */
	protected void assertSourceFound() throws Throwable {
		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertTrue("GDB failed to find source", fileExists(frameData.getFile()));

		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertTrue("Source locator failed to find source", sourceElement instanceof IStorage);

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

		ArrayList<ISourceContainer> containerList = new ArrayList<ISourceContainer>(
				Arrays.asList(director.getSourceContainers()));
		container.init(director);
		containerList.add(container);
		director.setSourceContainers(containerList.toArray(new ISourceContainer[containerList.size()]));
		setLaunchAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, director.getMemento());
		setLaunchAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, director.getId());
	}

	/**
	 * Add the mapping source container to the common source lookup
	 */
	protected void doMappingInCommon(boolean canonical) {
		CSourceLookupDirector commonSourceLookupDirector = CDebugCorePlugin.getDefault()
				.getCommonSourceLookupDirector();
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		if (canonical) {
			mapContainer.addMapEntry(fMapEntrySourceContainerC);
		} else {
			mapContainer.addMapEntry(fMapEntrySourceContainerN);
		}
		ArrayList<ISourceContainer> containerList = new ArrayList<ISourceContainer>(
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
	 * 
	 * @return
	 */
	protected void doMappingAndLaunch(String programName) throws CoreException, Exception {
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
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
	 * With mapping test that GDB does not locate the file, but the source
	 * lookup director and the source lookup service do find the file.
	 */
	protected void sourceMapping(String programName) throws Throwable {
		doMappingAndLaunch(programName);
		assertSourceFoundByDirectorOnly();
	}

	/**
	 * With mapping test breakpoints can be inserted.
	 */
	protected void sourceMappingBreakpoints(String programName) throws Throwable {
		doMappingAndLaunch(programName);

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
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingAC() throws Throwable {
		sourceMapping(EXEC_AC_NAME);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Test
	public void sourceMappingAN() throws Throwable {
		sourceMapping(EXEC_AN_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingRC() throws Throwable {
		sourceMapping(EXEC_RC_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Test
	public void sourceMappingRN() throws Throwable {
		sourceMapping(EXEC_RN_NAME);
	}

	/**
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingBreakpointsAC() throws Throwable {
		sourceMappingBreakpoints(EXEC_AC_NAME);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	@Test
	public void sourceMappingBreakpointsAN() throws Throwable {
		sourceMappingBreakpoints(EXEC_AN_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceMappingBreakpointsRC() throws Throwable {
		sourceMappingBreakpoints(EXEC_RC_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	@Test
	public void sourceMappingBreakpointsRN() throws Throwable {
		sourceMappingBreakpoints(EXEC_RN_NAME);
	}

	/**
	 * Test that if the user changes the source mappings in the middle of a
	 * debug session (e.g. with CSourceNotFoundEditor) that the lookups are
	 * updated.
	 */
	@Test
	public void sourceMappingChanges() throws Throwable {
		doMappingAndLaunch(EXEC_AC_NAME);

		DsfSourceLookupDirector sourceLocator = (DsfSourceLookupDirector) getGDBLaunch().getSourceLocator();
		MapEntrySourceContainer incorrectMapEntry = new MapEntrySourceContainer(
				new Path(BUILD_ABSPATH + "/incorrectsubpath"), new Path(SOURCE_ABSPATH));

		assertSourceFoundByDirectorOnly();

		// Change the source mappings
		ISourceContainer[] containers = sourceLocator.getSourceContainers();
		MappingSourceContainer mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(fMapEntrySourceContainerC);
		mappingSourceContainer.addMapEntry(incorrectMapEntry);
		sourceLocator.setSourceContainers(containers);

		assertSourceNotFound();

		// Change the source mappings back
		containers = sourceLocator.getSourceContainers();
		mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(incorrectMapEntry);
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainerC);
		sourceLocator.setSourceContainers(containers);

		assertSourceFoundByDirectorOnly();
	}

	/**
	 * Test that if the user changes the source mappings in the middle of a
	 * debug session (e.g. with CSourceNotFoundEditor) that the lookups are
	 * updated.
	 * 
	 * This version is for a new source mapping where there wasn't one
	 * previously.
	 */
	@Test
	public void sourceMappingAdded() throws Throwable {
		doLaunch(EXEC_PATH + EXEC_AC_NAME);

		assertSourceNotFound();

		// Set the source mappings
		DsfSourceLookupDirector sourceLocator = (DsfSourceLookupDirector) getGDBLaunch().getSourceLocator();
		ISourceContainer[] containers = sourceLocator.getSourceContainers();
		MappingSourceContainer mappingSourceContainer = new MappingSourceContainer("Mappings");
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainerC);
		ISourceContainer[] newContainers = new ISourceContainer[containers.length + 1];
		System.arraycopy(containers, 0, newContainers, 0, containers.length);
		newContainers[newContainers.length - 1] = mappingSourceContainer;
		sourceLocator.setSourceContainers(newContainers);

		assertSourceFoundByDirectorOnly();
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

		IResource exeResource = createNiceMock(IResource.class);
		expect(exeResource.getFullPath()).andReturn(absPath);
		expect(exeResource.getProject()).andReturn(null);
		replay(exeResource);

		IBinary exeBin = createNiceMock(IBinary.class);
		expect(exeBin.getPath()).andReturn(absPath);
		expect(exeBin.getAdapter(IResource.class)).andReturn(exeResource);
		/*
		 * we use the adapter factory CSourceFinderFactory to convert IBinary to
		 * ISourceFinder. The way the adapter is resolved it will first try and
		 * and get the adapter from the IBinary (IBinary extends IAdaptable) so
		 * we need to return null here for a clean failure. If we didn't
		 * explicitly provide the null, an exception would be raised because an
		 * unexpected method is invoked.
		 */
		expect(exeBin.getAdapter(ISourceFinder.class)).andReturn(null);
		replay(exeBin);
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
	@Test
	public void sourceFinderMappingAC_ActiveLaunch() throws Throwable {
		assertFinderDoesNotFind(EXEC_AC_NAME, new File(BUILD_PATH, SOURCE_NAME).getAbsolutePath());
		doMappingAndLaunch(EXEC_AC_NAME);
		assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
	}

	/**
	 * Test the CSourceFinder's use of source lookup when there is a terminated
	 * launch.
	 * 
	 * In this case, the DSF specific director created as part of the launch
	 * gets used.
	 */
	@Test
	public void sourceFinderMappingAC_TerminatedLaunch() throws Throwable {
		sourceFinderMappingAC_ActiveLaunch();

		// Terminate the launch, but don't remove it
		doAfterTest();
		assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
	}

	/**
	 * Test the CSourceFinder's use of source lookup when there is a not active
	 * launch, but a launch configuration that can be used.
	 * 
	 * In this case, the c general director created as part of the launch gets
	 * used.
	 */
	@Test
	public void sourceFinderMappingAC_LaunchConfig() throws Throwable {
		sourceFinderMappingAC_TerminatedLaunch();

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

	/**
	 * Test that CSourceFinder works with the common source director, i.e. no
	 * launches or launch configs in place.
	 */
	@Test
	public void sourceFinderMappingAC_CommonLocator() throws Throwable {
		assertFinderDoesNotFind(EXEC_AC_NAME, new File(BUILD_PATH, SOURCE_NAME).getAbsolutePath());

		doMappingInCommon(true);
		try {
			assertFinderFinds(EXEC_AC_NAME, new File(SOURCE_PATH, SOURCE_NAME).getAbsolutePath());
		} finally {
			restoreCommonToDefault();
		}

		assertFinderDoesNotFind(EXEC_AC_NAME, new File(BUILD_PATH, SOURCE_NAME).getAbsolutePath());
	}
}
