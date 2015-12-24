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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunchConfiguration;
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
 * </ul>
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
	protected static final String SOURCE_NAME = "SourceLookup.cc"; //$NON-NLS-1$
	protected static final int SOURCE_LINE = 15;
	/** Compiled with absolute and canonical path to SourceLookup.cc */
	protected static final String EXEC_AC_NAME = "SourceLookupAC.exe"; //$NON-NLS-1$
	/** Compiled with absolute and non-canonical path to SourceLookup.cc */
	protected static final String EXEC_AN_NAME = "SourceLookupAN.exe"; //$NON-NLS-1$
	/** Compiled with relative and canonical path to SourceLookup.cc */
	protected static final String EXEC_RC_NAME = "SourceLookupRC.exe"; //$NON-NLS-1$
	/** Compiled with relative and non-canonical path to SourceLookup.cc */
	protected static final String EXEC_RN_NAME = "SourceLookupRN.exe"; //$NON-NLS-1$

	protected static final String SOURCE_ABSPATH = new File(SOURCE_PATH).getAbsolutePath();
	protected static final String BUILD_ABSPATH = new File(BUILD_PATH).getAbsolutePath();

	protected MapEntrySourceContainer fMapEntrySourceContainer = new MapEntrySourceContainer(new Path(BUILD_ABSPATH),
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

	public void doLaunch(String programName) throws Exception {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, programName);
		super.doLaunch();

		// Get a reference to the breakpoint service
		final DsfSession session = getGDBLaunch().getSession();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), session.getId());
				fCommandControl = tracker.getService(IGDBControl.class);
				fCommandFactory = fCommandControl.getCommandFactory();
				tracker.dispose();
			}
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
	 * Set default source locators and a path mapping
	 * {@link MappingSourceContainer} from BUILD_ABSPATH -> SOURCE_ABSPATH and
	 * do the launch
	 */
	protected void doMappingAndLaunch(String programName) throws CoreException, Exception {
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.setIsMappingWithBackendEnabled(false);
		mapContainer.addMapEntry(fMapEntrySourceContainer);
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
		mappingSourceContainer.removeMapEntry(fMapEntrySourceContainer);
		mappingSourceContainer.addMapEntry(incorrectMapEntry);
		sourceLocator.setSourceContainers(containers);

		assertSourceNotFound();

		// Change the source mappings back
		containers = sourceLocator.getSourceContainers();
		mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(incorrectMapEntry);
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainer);
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
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainer);
		ISourceContainer[] newContainers = new ISourceContainer[containers.length + 1];
		System.arraycopy(containers, 0, newContainers, 0, containers.length);
		newContainers[newContainers.length - 1] = mappingSourceContainer;
		sourceLocator.setSourceContainers(newContainers);

		assertSourceFoundByDirectorOnly();
	}

	/**
	 * Set default source locators and a path mapping
	 * {@link MappingSourceContainer} from BUILD_ABSPATH -> SOURCE_ABSPATH and
	 * do the launch
	 */
	protected void doSubstituteAndLaunch(String programName) throws CoreException, Exception {
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		// Explicit for the test, but true is the default
		mapContainer.setIsMappingWithBackendEnabled(true);
		mapContainer.addMapEntry(fMapEntrySourceContainer);
		setSourceContainer(mapContainer);
		doLaunch(EXEC_PATH + programName);
	}

	/**
	 * With mapping test that GDB does not locate the file, but the source
	 * lookup director and the source lookup service do find the file.
	 */
	protected void sourceSubstitute(String programName) throws Throwable {
		doSubstituteAndLaunch(programName);
		assertSourceFound();
	}

	/**
	 * With mapping test breakpoints can be inserted.
	 */
	protected void sourceSubstituteBreakpoints(String programName) throws Throwable {
		doSubstituteAndLaunch(programName);

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
	public void sourceSubstituteAC() throws Throwable {
		sourceSubstitute(EXEC_AC_NAME);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Test
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	public void sourceSubstituteAN() throws Throwable {
		sourceSubstitute(EXEC_AN_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteRC() throws Throwable {
		sourceSubstitute(EXEC_RC_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Test
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	public void sourceSubstituteRN() throws Throwable {
		sourceSubstitute(EXEC_RN_NAME);
	}

	/**
	 * Test source mappings with executable built with an Absolute and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteBreakpointsAC() throws Throwable {
		sourceSubstituteBreakpoints(EXEC_AC_NAME);
	}

	/**
	 * Test source mappings with executable built with an Absolute and
	 * Non-canonical build path
	 */
	@Test
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	public void sourceSubstituteBreakpointsAN() throws Throwable {
		sourceSubstituteBreakpoints(EXEC_AN_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and Canonical
	 * build path
	 */
	@Test
	public void sourceSubstituteBreakpointsRC() throws Throwable {
		sourceSubstituteBreakpoints(EXEC_RC_NAME);
	}

	/**
	 * Test source mappings with executable built with a Relative and
	 * Non-canonical build path
	 */
	@Test
	@Ignore("Not supported because GDB does not handle non-canonical paths. See Bug 477057")
	public void sourceSubstituteBreakpointsRN() throws Throwable {
		sourceSubstituteBreakpoints(EXEC_RN_NAME);
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
	@Test
	public void sourceSubstituteChanges() throws Throwable {
		doSubstituteAndLaunch(EXEC_AC_NAME);

		DsfSourceLookupDirector sourceLocator = (DsfSourceLookupDirector) getGDBLaunch().getSourceLocator();
		MapEntrySourceContainer incorrectMapEntry = new MapEntrySourceContainer(
				new Path(BUILD_ABSPATH + "/incorrectsubpath"), new Path(SOURCE_ABSPATH));

		assertSourceFound();

		// Change the source mappings
		ISourceContainer[] containers = sourceLocator.getSourceContainers();
		MappingSourceContainer mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(fMapEntrySourceContainer);
		mappingSourceContainer.addMapEntry(incorrectMapEntry);
		sourceLocator.setSourceContainers(containers);

		/*
		 * GDB (pre 7.0) changes the current directory when the above source is
		 * found as a result GDB is able to find the source. To make sure that
		 * GDB is really doing a substitution rather than looking in current
		 * directory, change the current directory. Without this, the
		 * assertSourceNotFound fails because GDB unexpectedly finds the source
		 * (for the wrong reason).
		 */
		doCdToBinDir();

		/*
		 * TODO: remove need for the sleep + step!
		 * 
		 * While the Source Substitute path code listens for changes on source
		 * containers, it does not invalidate the caches, doing the step manages
		 * that. The sleep is a temporary hack to ensure that the
		 * setSourceContainers has completed.
		 */
		Thread.sleep(1000);
		SyncUtil.step(StepType.STEP_INTO);

		assertSourceNotFound();

		// Change the source mappings back
		containers = sourceLocator.getSourceContainers();
		mappingSourceContainer = (MappingSourceContainer) containers[1];
		mappingSourceContainer.removeMapEntry(incorrectMapEntry);
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainer);
		sourceLocator.setSourceContainers(containers);

		/*
		 * TODO: same reason as above TODO
		 */
		Thread.sleep(1000);
		SyncUtil.step(StepType.STEP_INTO);

		assertSourceFound();
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
	public void sourceSubstituteAdded() throws Throwable {
		doLaunch(EXEC_PATH + EXEC_RC_NAME);

		assertSourceNotFound();

		// Change the source mappings back
		DsfSourceLookupDirector sourceLocator = (DsfSourceLookupDirector) getGDBLaunch().getSourceLocator();
		ISourceContainer[] containers = sourceLocator.getSourceContainers();
		MappingSourceContainer mappingSourceContainer = new MappingSourceContainer("Mappings");
		mappingSourceContainer.addMapEntry(fMapEntrySourceContainer);
		ISourceContainer[] newContainers = new ISourceContainer[containers.length + 1];
		System.arraycopy(containers, 0, newContainers, 0, containers.length);
		newContainers[newContainers.length - 1] = mappingSourceContainer;
		sourceLocator.setSourceContainers(newContainers);

		/*
		 * TODO: same reason as above TODO in sourceSubstituteChanges
		 */
		Thread.sleep(1000);
		SyncUtil.step(StepType.STEP_INTO);

		assertSourceFound();
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

}
