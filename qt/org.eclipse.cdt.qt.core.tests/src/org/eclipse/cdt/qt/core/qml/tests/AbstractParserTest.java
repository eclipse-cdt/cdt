package org.eclipse.cdt.qt.core.qml.tests;

import java.io.IOException;

import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.junit.Rule;
import org.junit.rules.TestName;

public class AbstractParserTest {

	@Rule
	public TestName testName = new TestName();

	protected CharSequence getComment() throws IOException {
		return TestSourceReader.getContentsForTest(Activator.getBundle(), "src", getClass(), testName.getMethodName(), //$NON-NLS-1$
				1)[0];
	}

}
