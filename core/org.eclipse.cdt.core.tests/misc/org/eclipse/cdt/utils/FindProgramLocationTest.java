/*******************************************************************************
 *  Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class FindProgramLocationTest extends TestCase {

	private static final String PATH_SEPARATOR = File.pathSeparator;

	public static Test suite() {
		return new TestSuite(FindProgramLocationTest.class);
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	public void testCornerCases() throws CoreException, IOException {
		assertNull(PathUtil.findProgramLocation("", ""));
		assertNull(PathUtil.findProgramLocation("prog", ""));
		assertNull(PathUtil.findProgramLocation("prog", PATH_SEPARATOR));
		assertNull(PathUtil.findProgramLocation("prog", "x"+PATH_SEPARATOR));
		assertNull(PathUtil.findProgramLocation("prog", PATH_SEPARATOR+"x"));
		assertNull(PathUtil.findProgramLocation("prog", PATH_SEPARATOR+PATH_SEPARATOR));
		assertNull(PathUtil.findProgramLocation("prog", PATH_SEPARATOR+"x"+PATH_SEPARATOR));
	}

	public void testFind() throws CoreException, IOException {
		String name1 = "file1";
		String name2 = "file2";
		String name3 = "file3";
		String nameA = "fileA";

		// Create some folders and files
		IPath dir1 = ResourceHelper.createTemporaryFolder();
		IPath dir2 = ResourceHelper.createTemporaryFolder();
		IPath dir3 = ResourceHelper.createTemporaryFolder();

		IPath filePath1 = new Path(dir1 + File.separator + name1);
		IPath filePath2 = new Path(dir2 + File.separator + name2);
		IPath filePath3 = new Path(dir3 + File.separator + name3);

		IPath filePath2A = new Path(dir2 + File.separator + nameA);
		IPath filePath3A = new Path(dir3 + File.separator + nameA);

		File file1 = filePath1.toFile();
		file1.createNewFile();
		assertTrue(file1.exists());

		File file2 = filePath2.toFile();
		file2.createNewFile();
		assertTrue(file2.exists());

		File file3 = filePath3.toFile();
		file3.createNewFile();
		assertTrue(file3.exists());

		File file2A = filePath2A.toFile();
		file2A.createNewFile();
		assertTrue(file2A.exists());

		File file3A = filePath3A.toFile();
		file3A.createNewFile();
		assertTrue(file3A.exists());

		// sample $PATH
		String path123 = dir1 + PATH_SEPARATOR + dir2 + PATH_SEPARATOR + dir3;

		{
			// non-existing file
			IPath actual = PathUtil.findProgramLocation("non-existing", path123);
			assertEquals(null, actual);
		}
		{
			// file in the first path
			IPath actual = PathUtil.findProgramLocation(name1, path123);
			assertEquals(filePath1, actual);
		}
		{
			// file in the middle path
			IPath actual = PathUtil.findProgramLocation(name2, path123);
			assertEquals(filePath2, actual);
		}
		{
			// file in the last path
			IPath actual = PathUtil.findProgramLocation(name3, path123);
			assertEquals(filePath3, actual);
		}
		{
			// if two exist return first
			IPath actual = PathUtil.findProgramLocation(nameA, path123);
			assertEquals(filePath2A, actual);
		}
		{
			// try directory
			String nameDir = "subdir";
			IPath subdirPath = new Path(dir1 + File.separator + nameDir);
			File subdir = subdirPath.toFile();
			subdir.mkdir();
			assertTrue(file1.exists());

			// directory should not be found
			IPath actual = PathUtil.findProgramLocation(nameDir, path123);
			assertEquals(null, actual);
		}
	}

	public void testWindows() throws CoreException, IOException {
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			return;

		String name1 = "file1";
		String name2 = "file2";
		String name3 = "file3";

		// Create some folders and files
		IPath dir1 = ResourceHelper.createTemporaryFolder();
		IPath dir2 = ResourceHelper.createTemporaryFolder();

		IPath filePath1_com = new Path(dir1 + File.separator + name1 + ".com");
		IPath filePath2_exe = new Path(dir1 + File.separator + name2 + ".exe");

		IPath filePath3 = new Path(dir1 + File.separator + name3);
		IPath filePath3_exe = new Path(dir2 + File.separator + name3 + ".exe");

		File file1_com = filePath1_com.toFile();
		file1_com.createNewFile();
		assertTrue(file1_com.exists());

		File file2_exe = filePath2_exe.toFile();
		file2_exe.createNewFile();
		assertTrue(file2_exe.exists());

		File file3 = filePath3.toFile();
		file3.createNewFile();
		assertTrue(file3.exists());

		File file3_exe = filePath3_exe.toFile();
		file3_exe.createNewFile();
		assertTrue(file3_exe.exists());

		String path1 = dir1.toOSString();
		{
			// file.com
			IPath actual = PathUtil.findProgramLocation(name1, path1);
			assertEquals(filePath1_com, actual);
		}
		{
			// file.exe
			IPath actual = PathUtil.findProgramLocation(name2, path1);
			assertEquals(filePath2_exe, actual);
		}
		
		String path12 = dir1.toOSString() + PATH_SEPARATOR + dir2.toOSString();
		{
			// dir2/file.exe is preferred to dir1/file
			IPath actual = PathUtil.findProgramLocation(name3, path12);
			assertEquals(filePath3_exe, actual);
		}
	}

}
