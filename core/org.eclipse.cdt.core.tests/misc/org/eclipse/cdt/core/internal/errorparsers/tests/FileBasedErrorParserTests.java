package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.runtime.Path;

public class FileBasedErrorParserTests extends GenericErrorParserTests {

	File errorFile;

	public FileBasedErrorParserTests(File file) {
		super("testErrorsInFiles");
		errorFile = file;
	}
	
	public String getName() {
		return super.getName() + " " + errorFile.getName();
	}

	public void testErrorsInFiles() throws IOException {
		InputStream stream = new FileInputStream(errorFile);

		runParserTest(stream, -1, -1, null, null, new String[]{GCC_ERROR_PARSER_ID});
		stream.close();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(FileBasedErrorParserTests.class.getName());
		File dir = CTestPlugin.getDefault().getFileInPlugin(new Path("resources/errortests/"));
		File[] testsfiles = dir.listFiles();
		for (int i = 0; i < testsfiles.length; i++) {
			if (testsfiles[i].isFile())
				suite.addTest(new FileBasedErrorParserTests(testsfiles[i]));
		}
		return suite;
	}
}
