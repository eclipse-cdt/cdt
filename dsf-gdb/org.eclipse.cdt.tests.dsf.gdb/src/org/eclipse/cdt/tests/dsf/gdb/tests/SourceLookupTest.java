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

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that interaction with source lookups works as expected.
 * 
 * All of these tests use a program (SourceLookup.exe) that was built from
 * sources that have moved. At build time source was ../build/SourceLookup.cc,
 * but now is ../src/SourceLookup.cc.
 * 
 * TODO: Missing features and tests:
 * 
 * - Update source lookup path in running session (i.e. simulate behaviour of
 * CSourceNotFoundEditor)
 * 
 * - Ensure breakpoints are inserted properly. This needs to be done by creating
 * platform breakpoints and checking they are installed correctly.
 */
@RunWith(BackgroundRunner.class)
public class SourceLookupTest extends BaseTestCase {
	protected static final String BUILD_PATH = "data/launch/build/";
	protected static final String EXEC_NAME = "SourceLookup.exe"; //$NON-NLS-1$

	protected static final String SOURCE_ABSPATH = new File(SOURCE_PATH).getAbsolutePath();
	protected static final String BUILD_ABSPATH = new File(BUILD_PATH).getAbsolutePath();

	@Override
	public void doBeforeTest() throws Exception {
		setLaunchAttributes();
		// source lookup attributes are custom per test, so delay launch until
		// they are setup
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	/**
	 * Test with only default source locators on, that the source file was not
	 * found when stopped at main by any of GDB directly, with the source lookup
	 * director, or via the ISourceLookup service.
	 */
	@Test
	public void defaultSourceLookup() throws Throwable {
		doLaunch();

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB Unexpectedly located the source", Files.exists(Paths.get(frameData.getFile())));

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
	 * Test with default source locators and a path mapping
	 * {@link MappingSourceContainer} from BUILD_ABSPATH -> SOURCE_ABSPATH that
	 * GDB does not locate the file, but the source lookup director and the
	 * source lookup service do find the file.
	 */
	@Test
	public void sourceMapping() throws Throwable {
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.addMapEntry(new MapEntrySourceContainer(new Path(BUILD_ABSPATH), new Path(SOURCE_ABSPATH)));
		setSourceContainer(mapContainer);
		doLaunch();

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertTrue("GDB failed to find source", Files.exists(Paths.get(frameData.getFile())));

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
	 * Same as {@link #sourceMapping()}, however performs mapping in DSF instead
	 * of delegating to DSF.
	 * 
	 * This can be seen as the "legacy" behaviour of the mapping source
	 * containers.
	 */
	@Test
	public void sourceMappingDisabledBackendMapping() throws Throwable {
		MappingSourceContainer mapContainer = new MappingSourceContainer("Mappings");
		mapContainer.setIsMappingWithBackendEnabled(false);
		mapContainer.addMapEntry(new MapEntrySourceContainer(new Path(BUILD_ABSPATH), new Path(SOURCE_ABSPATH)));
		setSourceContainer(mapContainer);
		doLaunch();

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB unexpectedly found source", Files.exists(Paths.get(frameData.getFile())));

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
		doLaunch();

		// Check file name as returned from back end
		IFrameDMData frameData = SyncUtil.getFrameData(0, 0);
		assertFalse("GDB Unexpectedly located the source", Files.exists(Paths.get(frameData.getFile())));

		// Check file as resolved by source lookup director
		ISourceLookupDirector director = (ISourceLookupDirector) getGDBLaunch().getSourceLocator();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(0, 0);
		Object sourceElement = director.getSourceElement(frameDmc);
		assertTrue("Source locator failed to find source", sourceElement instanceof IStorage);

		// Check file as resolved by ISourceLookup service
		sourceElement = SyncUtil.getSource(frameData.getFile());
		assertTrue("Source Lookup service failed to find source", sourceElement instanceof IStorage);
	}
}
