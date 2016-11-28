package org.eclipse.cdt.linkerscript.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FullLinkerScriptFilesTest.class, NumbersTest.class, LinkerScriptParsingTest.class,
		LinkerScriptParseErrorTest.class, ExpressionValidTest.class, ExpressionInValidTest.class,
		ExpressionReducerTest.class, EditSectionTest.class })
public class AllTests {

}
