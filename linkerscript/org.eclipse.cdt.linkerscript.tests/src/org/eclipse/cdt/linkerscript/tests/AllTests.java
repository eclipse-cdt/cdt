package org.eclipse.cdt.linkerscript.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FullLinkerScriptFilesTest.class, NumbersTest.class, LinkerScriptParsingTest.class })
public class AllTests {

}
