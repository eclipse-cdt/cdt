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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5.SourceLookupTest_7_5;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class SourceLookupTest_7_6 extends SourceLookupTest_7_5 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_6);
	}

	/**
	 * Supported starting in GDB >= 7.6 because DSF is using the full path name
	 * to pass to the {@link ISourceContainer#findSourceElements(String)}. In
	 * versions prior to 7.6 the fullname field was not returned from GDB if the
	 * file was not found by GDB. See
	 * https://sourceware.org/ml/gdb-patches/2012-12/msg00557.html.
	 * 
	 * Therefore in version < 7.5 the MI frame info has file="SourceLookup.cc"
	 * and no fullname field. This means there is no path to source map against.
	 * 
	 * In version >= 7.6 the MI frame info has file="SourceLookup.cc",fullname=
	 * "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/build/SourceLookup.cc"
	 * fields, so there is a path to do the mapping against. Recall that the
	 * test maps
	 * "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/build"
	 * to "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/src"
	 */
	@Test
	@Override
	public void sourceMappingDisabledBackendMapping() throws Throwable {
		super.sourceMappingDisabledBackendMapping();
	}

	/**
	 * Not supported in GDB >= 7.6 because DSF is using the full path name to
	 * pass to the {@link ISourceContainer#findSourceElements(String)}. In
	 * versions prior to 7.6 the fullname field was not returned from GDB if the
	 * file was not found by GDB. See
	 * https://sourceware.org/ml/gdb-patches/2012-12/msg00557.html
	 * 
	 * Therefore in version < 7.5 the MI frame info has file="SourceLookup.cc"
	 * and no fullname field. This means that "SourceLookup.cc" gets passed to
	 * DirectorySourceContainer.findSourceElements(String), the container
	 * prepends the directory name and finds the source.
	 * 
	 * In version >= 7.6 the MI frame info has file="SourceLookup.cc",fullname=
	 * "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/build/SourceLookup.cc"
	 * As a result the fullname is passed to
	 * DirectorySourceContainer.findSourceElements(String), the container
	 * prepends the directory name and as a result does not find the source.
	 * What DirectorySourceContainer does is a file exists on:
	 * "<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/src/<cdt.git path>/dsf-gdb/org.eclipse.cdt.tests.dsf.gdb/data/launch/build/SourceLookup.cc"
	 * which obviously does not exist.
	 */
	@Ignore("Not supported in GDB >= 7.6 because DSF is using the full path name to	pass to	the ISourceContainer.findSourceElements")
	@Test
	@Override
	public void directorySource() throws Throwable {
		super.directorySource();
	}
}
