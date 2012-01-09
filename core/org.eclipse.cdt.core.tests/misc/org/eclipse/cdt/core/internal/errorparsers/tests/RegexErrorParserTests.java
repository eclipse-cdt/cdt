/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.internal.errorparsers.tests;

import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IErrorParserNamed;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.errorparsers.ErrorParserNamedWrapper;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.errorparsers.ErrorParserExtensionManager;
import org.eclipse.cdt.internal.errorparsers.GASErrorParser;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Test cases testing RegexErrorParser functionality
 */
public class RegexErrorParserTests extends TestCase {
	// These should match id and name of extension point defined in plugin.xml
	private static final String REGEX_ERRORPARSER_ID = "org.eclipse.cdt.core.tests.RegexErrorParserId";
	private static final String REGEX_ERRORPARSER_NAME = "Test Plugin RegexErrorParser";
	private static final String NOTREGEX_ERRORPARSER_ID = "org.eclipse.cdt.core.GASErrorParser";

	private static final String TEST_PROJECT_NAME = "RegexErrorParserTests";

	private IProject fProject = null;
	private ArrayList<ProblemMarkerInfo> errorList;

	private final IMarkerGenerator markerGenerator = new IMarkerGenerator() {
		// deprecated
		@Override
		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {}

		@Override
		public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
			errorList.add(problemMarkerInfo);
		}
	};

	/**
	 * Dummy error parser
	 */
	public static class DummyErrorParser implements IErrorParser {
		/**
		 * Constructor
		 */
		public DummyErrorParser() {
		}

		@Override
		public boolean processLine(String line, ErrorParserManager eoParser) {
			return false;
		}
	}

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public RegexErrorParserTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		fProject = ResourceHelper.createCDTProject(TEST_PROJECT_NAME);
		assertNotNull(fProject);
		errorList = new ArrayList<ProblemMarkerInfo>();
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
		fProject = null;

		ErrorParserManager.setUserDefinedErrorParsers(null);
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(RegexErrorParserTests.class);
	}

	/**
	 * main function of the class.
	 *
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Check if error pattern can be added/deleted.
	 *
	 * @throws Exception...
	 */
	public void testRegexErrorParserAddDeletePattern() throws Exception {
		RegexErrorParser regexErrorParser = new RegexErrorParser();
		regexErrorParser.addPattern(new RegexErrorPattern("pattern 1",
				null, null, null, null, RegexErrorPattern.SEVERITY_SKIP, true));
		regexErrorParser.addPattern(new RegexErrorPattern("delete me",
				null, null, null, null, RegexErrorPattern.SEVERITY_SKIP, true));
		regexErrorParser.addPattern(new RegexErrorPattern("pattern 3",
				null, null, null, null, RegexErrorPattern.SEVERITY_SKIP, true));

		// adding patterns
		RegexErrorPattern[] patternsBefore = regexErrorParser.getPatterns();
		assertEquals(3, patternsBefore.length);
		assertEquals("delete me", patternsBefore[1].getPattern());
		RegexErrorPattern next = patternsBefore[2];

		// delete pattern test
		regexErrorParser.removePattern(patternsBefore[1]);

		RegexErrorPattern[] patternsAfter = regexErrorParser.getPatterns();
		assertEquals(2, patternsAfter.length);
		assertEquals(next, patternsAfter[1]);
	}

	/**
	 * Make sure the order of patterns is preserved.
	 *
	 * @throws Exception...
	 */
	public void testRegexErrorParserPatternOrder() throws Exception {
		final int ERR=IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
		RegexErrorParser regexErrorParser = new RegexErrorParser();
		RegexErrorPattern removable = new RegexErrorPattern("CCC", null, null, null, null, ERR, true);
		regexErrorParser.addPattern(new RegexErrorPattern("AAA", null, null, null, null, ERR, true));
		regexErrorParser.addPattern(new RegexErrorPattern("BBB", null, null, null, null, ERR, true));
		regexErrorParser.addPattern(removable);
		regexErrorParser.addPattern(new RegexErrorPattern("DDD", null, null, null, null, ERR, true));
		regexErrorParser.addPattern(new RegexErrorPattern("ZZZ", null, null, null, null, ERR, true));

		{
			RegexErrorPattern[] patterns = regexErrorParser.getPatterns();
			assertEquals("AAA", patterns[0].getPattern());
			assertEquals("BBB", patterns[1].getPattern());
			assertEquals("CCC", patterns[2].getPattern());
			assertEquals("DDD", patterns[3].getPattern());
			assertEquals("ZZZ", patterns[4].getPattern());
		}

		regexErrorParser.removePattern(removable);

		{
			RegexErrorPattern[] patterns = regexErrorParser.getPatterns();
			assertEquals("AAA", patterns[0].getPattern());
			assertEquals("BBB", patterns[1].getPattern());
			assertEquals("DDD", patterns[2].getPattern());
			assertEquals("ZZZ", patterns[3].getPattern());
		}
	}

	/**
	 * Check how RegexErrorParser parses output.
	 *
	 * @throws Exception...
	 */
	public void testRegexErrorParserParseOutput() throws Exception {
		RegexErrorParser regexErrorParser = new RegexErrorParser();
		regexErrorParser.addPattern(new RegexErrorPattern("(.*)#(.*)#(.*)#(.*)",
				"$1", "$2", "$3 $4", "var=$4", IMarkerGenerator.SEVERITY_ERROR_RESOURCE, true));
		regexErrorParser.addPattern(new RegexErrorPattern("(.*)!(skip me)!(.*)!(.*)",
				null, null, null, null, RegexErrorPattern.SEVERITY_SKIP, true));
		regexErrorParser.addPattern(new RegexErrorPattern("(.*)!(Description)!(.*)!(.*)",
				"$4", "$3", "$2", "$1", IMarkerGenerator.SEVERITY_WARNING, /*eat-line*/ false));
		// broken pattern
		regexErrorParser.addPattern(new RegexErrorPattern("(.*)!(.*)",
				"$6", "$7", "$8", "$9", IMarkerGenerator.SEVERITY_WARNING, true));
		regexErrorParser.addPattern(new RegexErrorPattern("(.*)!(.*)!(.*)!(.*)",
				null, null, null, null, IMarkerGenerator.SEVERITY_INFO, true));

		String fileName = "RegexErrorParser.c";
		ResourceHelper.createFile(fProject, fileName);

		errorList.clear();
		ErrorParserManager epManager = new ErrorParserManager(fProject, markerGenerator, new String[0]);
		
		ProblemMarkerInfo problemMarkerInfo;

		// Regular pattern
		regexErrorParser.processLine(fileName+"#10#Description#Variable", epManager);
		// This should get ignored
		regexErrorParser.processLine("Variable!skip me!10!"+fileName, epManager);
		// Eat-line=false + qualifying next pattern (nulls), i.e. generates 2 problems
		regexErrorParser.processLine("Variable!Description!10!"+fileName, epManager);

		assertEquals(3, errorList.size());

		// Regular
		problemMarkerInfo = errorList.get(0);
		assertEquals(IMarkerGenerator.SEVERITY_ERROR_RESOURCE, problemMarkerInfo.severity);
		assertEquals("L/"+TEST_PROJECT_NAME+"/"+fileName, problemMarkerInfo.file.toString());
		assertEquals(fileName, problemMarkerInfo.file.getName());
		assertEquals(10, problemMarkerInfo.lineNumber);
		assertEquals("Description Variable",problemMarkerInfo.description);
		assertEquals("var=Variable",problemMarkerInfo.variableName);

		// Eat-line
		problemMarkerInfo = errorList.get(1);
		assertEquals(IMarkerGenerator.SEVERITY_WARNING, problemMarkerInfo.severity);
		assertEquals("L/"+TEST_PROJECT_NAME+"/"+fileName, problemMarkerInfo.file.toString());
		assertEquals(fileName, problemMarkerInfo.file.getName());
		assertEquals(10, problemMarkerInfo.lineNumber);
		assertEquals("Description",problemMarkerInfo.description);
		assertEquals("Variable",problemMarkerInfo.variableName);

		// Nulls
		problemMarkerInfo = errorList.get(2);
		assertEquals(IMarkerGenerator.SEVERITY_INFO, problemMarkerInfo.severity);
		assertEquals("P/"+TEST_PROJECT_NAME, problemMarkerInfo.file.toString());
		assertEquals(0, problemMarkerInfo.lineNumber);
		assertEquals("",problemMarkerInfo.description);
		assertEquals("",problemMarkerInfo.variableName);

		// clone & equals
		RegexErrorParser cloned = (RegexErrorParser)regexErrorParser.clone();
		assertTrue(cloned!=regexErrorParser);
		assertEquals(regexErrorParser, cloned);
		assertTrue(cloned.getPatterns()!=regexErrorParser.getPatterns());
		assertEquals(cloned.getPatterns().length, regexErrorParser.getPatterns().length);
		for (int i=0; i<regexErrorParser.getPatterns().length; i++) {
			// Checking deep copy
			assertTrue(cloned.getPatterns()[i]!=regexErrorParser.getPatterns()[i]);
			assertEquals(cloned.getPatterns()[i],regexErrorParser.getPatterns()[i]);
		}
	}

	/**
	 * Checks if compatibility with CCorePlugin methods from CDT 6.0 was not violated.
	 *
	 * @throws Exception...
	 */
	public void testCompatibility() throws Exception {
		final CCorePlugin cCorePlugin = CCorePlugin.getDefault();

		// CCorePlugin.getAllErrorParsersIDs()
		String all = ErrorParserManager.toDelimitedString(cCorePlugin.getAllErrorParsersIDs());
		assertTrue(all.contains(NOTREGEX_ERRORPARSER_ID));

		// CCorePlugin.getErrorParser(id)
		IErrorParser[] gccErrorParserArray = cCorePlugin.getErrorParser(NOTREGEX_ERRORPARSER_ID);
		assertNotNull(gccErrorParserArray);
		assertEquals(1, gccErrorParserArray.length);
		assertTrue(gccErrorParserArray[0] instanceof GASErrorParser);
	}

	/**
	 * Check that regular error parser extension defined in plugin.xml is accessible.
	 *
	 * @throws Exception...
	 */
	public void testExtension() throws Exception {
		// ErrorParserManager.getErrorParser
		{
			IErrorParserNamed errorParser = ErrorParserManager.getErrorParserCopy(REGEX_ERRORPARSER_ID);
			assertNotNull(errorParser);
			assertEquals(REGEX_ERRORPARSER_NAME, errorParser.getName());

			assertTrue(errorParser instanceof RegexErrorParser);
			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParser;
			assertEquals(REGEX_ERRORPARSER_ID, regexErrorParser.getId());
			assertEquals(REGEX_ERRORPARSER_NAME, regexErrorParser.getName());

			RegexErrorPattern[] patterns = regexErrorParser.getPatterns();
			assertEquals(1, patterns.length);

			RegexErrorPattern pattern = patterns[0];
			assertEquals(IMarker.SEVERITY_ERROR, pattern.getSeverity());
			assertEquals(true, pattern.isEatProcessedLine());
			assertEquals("(.*):(.*):regex (.*)", pattern.getPattern());
			assertEquals("$1", pattern.getFileExpression());
			assertEquals("$2", pattern.getLineExpression());
			assertEquals("$3", pattern.getDescriptionExpression());
			assertEquals("", pattern.getVarNameExpression());
		}

		// ErrorParserManager.getErrorParsers
		{
			IErrorParser errorParser = ErrorParserManager.getErrorParserCopy(REGEX_ERRORPARSER_ID);
			assertTrue(errorParser instanceof RegexErrorParser);

			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParser;
			assertEquals(REGEX_ERRORPARSER_ID, regexErrorParser.getId());
			assertEquals(REGEX_ERRORPARSER_NAME, regexErrorParser.getName());
		}
	}

	/**
	 * Make sure extensions contributed through extension point are sorted by name
	 * unless deprecated or contributed by test plugin.
	 *
	 * @throws Exception...
	 */
	public void testExtensionsSorting() throws Exception {
		{
			String[] ids = ErrorParserManager.getErrorParserExtensionIds();
			String lastName = "";
			boolean lastIsDeprecated = false;
			boolean lastIsTestPlugin = false;
			// first regular error parsers
			// then deprecated ones
			// then contributed by test plugin
			for (String id : ids) {
				String name = ErrorParserManager.getErrorParserCopy(id).getName();
				boolean isDeprecated = name.contains("(Deprecated)");
				boolean isTestPlugin = id.startsWith(CTestPlugin.PLUGIN_ID);
				String message = "Parser ["+lastName+"] preceeds ["+name+"]";
				
				// inside the same category sorted by names
				if (lastIsDeprecated==isDeprecated && lastIsTestPlugin==isTestPlugin) {
					assertTrue(message, lastName.compareTo(name)<=0);
				}
				// deprecated follow non-deprecated (unless parsers from test plugin show up)
				if (lastIsTestPlugin==isTestPlugin) {
					assertFalse(message, lastIsDeprecated==true && isDeprecated==false);
				}
				// error parsers from test plugin are the last
				assertFalse(message, lastIsTestPlugin==true && isTestPlugin==false);
				
				lastName = name;
				lastIsDeprecated = isDeprecated;
				lastIsTestPlugin = isTestPlugin;
			}
		}
	}

	/**
	 * Test setting/retrieval of error parsers and their IDs.
	 *
	 * @throws Exception...
	 */
	public void testAvailableErrorParsers() throws Exception {
		final String TESTING_ID = "org.eclipse.cdt.core.test.errorparser";
		final String TESTING_NAME = "An error parser";

		final String[] availableParserIds = ErrorParserManager.getErrorParserAvailableIds();
		assertNotNull(availableParserIds);
		assertTrue(availableParserIds.length>0);
		final String firstId = ErrorParserManager.getErrorParserAvailableIds()[0];
		final IErrorParserNamed firstErrorParser = ErrorParserManager.getErrorParserCopy(firstId);
		assertNotNull(firstErrorParser);
		assertEquals(firstId, firstErrorParser.getId());
		final String firstName = firstErrorParser.getName();
		// Preconditions
		{
			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			assertEquals(false, all.contains(TESTING_ID));
			assertEquals(true, all.contains(firstId));

			assertNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));

			IErrorParserNamed retrieved2 = ErrorParserManager.getErrorParserCopy(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstErrorParser, retrieved2);
		}

		// set available parsers
		{
			IErrorParser dummy1 = new DummyErrorParser();
			IErrorParser dummy2 = new DummyErrorParser();
			ErrorParserManager.setUserDefinedErrorParsers(new IErrorParserNamed[] {
					// add brand new one
					new ErrorParserNamedWrapper(TESTING_ID, TESTING_NAME, dummy1),
					// override extension with another one
					new ErrorParserNamedWrapper(firstId, firstName, dummy2),
			});
			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			assertEquals(true, all.contains(TESTING_ID));
			assertEquals(true, all.contains(firstId));

			IErrorParserNamed retrieved1 = ErrorParserManager.getErrorParserCopy(TESTING_ID);
			assertNotNull(retrieved1);
			assertEquals(TESTING_NAME, retrieved1.getName());
			assertTrue(retrieved1 instanceof ErrorParserNamedWrapper);
			assertEquals(dummy1, ((ErrorParserNamedWrapper)retrieved1).getErrorParser());

			IErrorParserNamed retrieved2 = ErrorParserManager.getErrorParserCopy(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstName, retrieved2.getName());
			assertTrue(retrieved2 instanceof ErrorParserNamedWrapper);
			assertEquals(dummy2, ((ErrorParserNamedWrapper)retrieved2).getErrorParser());
			
			IErrorParserNamed retrieved2_ext = ErrorParserManager.getErrorParserExtensionCopy(firstId);
			assertNotNull(retrieved2_ext);
			assertEquals(firstName, retrieved2_ext.getName());
			assertEquals(firstErrorParser, retrieved2_ext);
		}
		// reset available parsers
		{
			ErrorParserManager.setUserDefinedErrorParsers(null);
			String[] userDefinedIds = ErrorParserManager.getUserDefinedErrorParserIds();
			assertNull(userDefinedIds);

			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			assertEquals(false, all.contains(TESTING_ID));
			assertEquals(true, all.contains(firstId));

			assertNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));

			IErrorParserNamed retrieved2 = ErrorParserManager.getErrorParserCopy(firstId);
			assertNotNull(retrieved2);
			assertEquals(firstErrorParser, retrieved2);
		}
	}

	/**
	 * Test setting/retrieval of user defined error parsers.
	 *
	 * @throws Exception...
	 */
	public void testUserDefinedErrorParsers() throws Exception {
		final String TESTING_ID = "org.eclipse.cdt.core.test.errorparser";
		final String TESTING_NAME = "An error parser";
		// reset parsers
		{
			ErrorParserManager.setUserDefinedErrorParsers(null);
			String[] userDefinedIds = ErrorParserManager.getUserDefinedErrorParserIds();
			assertNull(userDefinedIds);

			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			String extensions = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserExtensionIds());
			assertEquals(all, extensions);
		}
		{
			ErrorParserManager.setUserDefinedErrorParsers(new IErrorParserNamed[] {
					new ErrorParserNamedWrapper(TESTING_ID, TESTING_NAME, new DummyErrorParser()),
			});
			String userDefinedIds = ErrorParserManager.toDelimitedString(ErrorParserManager.getUserDefinedErrorParserIds());
			assertEquals(TESTING_ID, userDefinedIds);

			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			String extensions = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserExtensionIds());
			assertFalse(all.equals(extensions));
		}
	}

	/**
	 * Test setting/retrieval of default error parser IDs preferences.
	 *
	 * @throws Exception...
	 */
	public void testDefaultErrorParserIds() throws Exception {
		final String[] availableParserIds = ErrorParserManager.getErrorParserAvailableIds();
		assertNotNull(availableParserIds);
		final String[] initialDefaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();

		// preconditions
		{
			String[] defaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();
			assertNotNull(defaultErrorParserIds);
			assertEquals(ErrorParserManager.toDelimitedString(availableParserIds),
					ErrorParserManager.toDelimitedString(defaultErrorParserIds));
		}
		// setDefaultErrorParserIds
		{
			String[] newDefaultErrorParserIds = {
					"org.eclipse.cdt.core.test.errorparser0",
					"org.eclipse.cdt.core.test.errorparser1",
					"org.eclipse.cdt.core.test.errorparser2",
			};
			ErrorParserManager.setDefaultErrorParserIds(newDefaultErrorParserIds);
			String[] defaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();
			assertNotNull(defaultErrorParserIds);
			assertEquals(ErrorParserManager.toDelimitedString(newDefaultErrorParserIds),
					ErrorParserManager.toDelimitedString(defaultErrorParserIds));
		}

		// reset
		{
			ErrorParserManager.setDefaultErrorParserIds(null);
			String[] defaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();
			assertNotNull(defaultErrorParserIds);
			assertEquals(ErrorParserManager.toDelimitedString(availableParserIds),
					ErrorParserManager.toDelimitedString(defaultErrorParserIds));
		}
	}

	/**
	 * Test serialization of user defined error parsers.
	 *
	 * @throws Exception...
	 */
	public void testSerializeErrorParser() throws Exception {
		final String TESTING_ID = "org.eclipse.cdt.core.test.errorparser";
		final String TESTING_NAME = "An error parser";

		{
			// Create error parser
			IErrorParser errorParser = new GASErrorParser();
			// Add to available parsers
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(new IErrorParserNamed[] {new ErrorParserNamedWrapper(TESTING_ID, TESTING_NAME, errorParser)});
			assertNotNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));
			assertEquals(TESTING_NAME, ErrorParserManager.getErrorParserCopy(TESTING_ID).getName());
			// Serialize in persistent storage
			ErrorParserExtensionManager.serializeUserDefinedErrorParsers();
		}
		{
			// Remove from available parsers
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(null);
			assertNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));
		}

		{
			// Re-load from persistent storage and check it out
			ErrorParserExtensionManager.loadUserDefinedErrorParsers();
			IErrorParserNamed errorParser = ErrorParserManager.getErrorParserCopy(TESTING_ID);
			assertNotNull(errorParser);
			assertEquals(TESTING_NAME, errorParser.getName());
			assertTrue(errorParser instanceof ErrorParserNamedWrapper);
			assertTrue(((ErrorParserNamedWrapper)errorParser).getErrorParser() instanceof GASErrorParser);
		}
		{
			// Remove from available parsers as clean-up
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(null);
			assertNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));
		}
	}

	/**
	 * Test serialization of user defined RegexErrorParser.
	 *
	 * @throws Exception...
	 */
	public void testSerializeRegexErrorParser() throws Exception {

		final String TESTING_ID = "org.eclipse.cdt.core.test.regexerrorparser";
		final String TESTING_NAME = "Regex Error Parser";
		final String ALL_IDS = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
		{
			// Create error parser with the same id as in eclipse registry
			RegexErrorParser regexErrorParser = new RegexErrorParser(TESTING_ID, TESTING_NAME);
			regexErrorParser.addPattern(new RegexErrorPattern("Pattern-Y",
					"line-Y", "file-Y", "description-Y", null, IMarkerGenerator.SEVERITY_WARNING, false));

			// Add to available parsers
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(new IErrorParserNamed[] {regexErrorParser});
			assertNotNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));
			// And serialize in persistent storage
			ErrorParserExtensionManager.serializeUserDefinedErrorParsers();
		}

		{
			// Remove from available parsers
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(null);
			assertNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));
		}

		{
			// Re-load from persistent storage and check it out
			ErrorParserExtensionManager.loadUserDefinedErrorParsers();
			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			assertTrue(all.contains(TESTING_ID));

			IErrorParser errorParser = ErrorParserManager.getErrorParserCopy(TESTING_ID);
			assertNotNull(errorParser);
			assertTrue(errorParser instanceof RegexErrorParser);
			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParser;
			assertEquals(TESTING_ID, regexErrorParser.getId());
			assertEquals(TESTING_NAME, regexErrorParser.getName());

			RegexErrorPattern[] errorPatterns = regexErrorParser.getPatterns();
			assertEquals(1, errorPatterns.length);
			assertEquals("Pattern-Y", errorPatterns[0].getPattern());
		}

		{
			// Remove from available parsers and serialize
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(null);
			ErrorParserExtensionManager.serializeUserDefinedErrorParsers();

			// Re-load from persistent storage and check it out
			ErrorParserExtensionManager.loadUserDefinedErrorParsers();
			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			assertEquals(ALL_IDS, all);
		}
	}

	/**
	 * Make sure special characters are serialized properly.
	 *
	 * @throws Exception...
	 */
	public void testSerializeRegexErrorParserSpecialCharacters() throws Exception {

		final String TESTING_ID = "org.eclipse.cdt.core.test.regexerrorparser";
		final String TESTING_NAME = "<>\"'\\& Error Parser";
		final String TESTING_REGEX = "Pattern-<>\"'\\&";
		final String ALL_IDS = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
		{
			// Create error parser with the same id as in eclipse registry
			RegexErrorParser regexErrorParser = new RegexErrorParser(TESTING_ID, TESTING_NAME);
			regexErrorParser.addPattern(new RegexErrorPattern(TESTING_REGEX,
					"line-<>\"'\\&", "file-<>\"'\\&", "description-<>\"'\\&", null, IMarkerGenerator.SEVERITY_WARNING, false));

			// Add to available parsers
			ErrorParserExtensionManager.setUserDefinedErrorParsersInternal(new IErrorParserNamed[] {regexErrorParser});
			assertNotNull(ErrorParserManager.getErrorParserCopy(TESTING_ID));
			// And serialize in persistent storage
			ErrorParserExtensionManager.serializeUserDefinedErrorParsers();
		}

		{
			// Re-load from persistent storage and check it out
			ErrorParserExtensionManager.loadUserDefinedErrorParsers();
			String all = ErrorParserManager.toDelimitedString(ErrorParserManager.getErrorParserAvailableIds());
			assertTrue(all.contains(TESTING_ID));

			IErrorParser errorParser = ErrorParserManager.getErrorParserCopy(TESTING_ID);
			assertNotNull(errorParser);
			assertTrue(errorParser instanceof RegexErrorParser);
			RegexErrorParser regexErrorParser = (RegexErrorParser)errorParser;
			assertEquals(TESTING_ID, regexErrorParser.getId());
			assertEquals(TESTING_NAME, regexErrorParser.getName());

			RegexErrorPattern[] errorPatterns = regexErrorParser.getPatterns();
			assertEquals(1, errorPatterns.length);
			assertEquals(TESTING_REGEX, errorPatterns[0].getPattern());
		}
	}

	/**
	 * Check that default parser IDs are stored properly.
	 *
	 * @throws Exception...
	 */
	public void testSerializeDefaultErrorParserIds() throws Exception {
		final String[] testingDefaultErrorParserIds = {
				"org.eclipse.cdt.core.test.errorparser0",
				"org.eclipse.cdt.core.test.errorparser1",
				"org.eclipse.cdt.core.test.errorparser2",
		};
		final String TESTING_IDS = ErrorParserManager.toDelimitedString(testingDefaultErrorParserIds);
		final String DEFAULT_IDS = ErrorParserManager.toDelimitedString(ErrorParserManager.getDefaultErrorParserIds());

		{
			// setDefaultErrorParserIds
			ErrorParserExtensionManager.setDefaultErrorParserIdsInternal(testingDefaultErrorParserIds);

			String[] defaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();
			assertNotNull(defaultErrorParserIds);
			assertEquals(TESTING_IDS, ErrorParserManager.toDelimitedString(defaultErrorParserIds));

			// serialize them
			ErrorParserExtensionManager.serializeDefaultErrorParserIds();
		}

		{
			// Remove from internal list
			ErrorParserExtensionManager.setDefaultErrorParserIdsInternal(null);
			assertEquals(DEFAULT_IDS, ErrorParserManager.toDelimitedString(ErrorParserManager.getDefaultErrorParserIds()));
		}

		{
			// Re-load from persistent storage and check it out
			ErrorParserExtensionManager.loadDefaultErrorParserIds();

			String[] defaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();
			assertNotNull(defaultErrorParserIds);
			assertEquals(TESTING_IDS, ErrorParserManager.toDelimitedString(defaultErrorParserIds));
		}

		{
			// Reset IDs and serialize
			ErrorParserExtensionManager.setDefaultErrorParserIdsInternal(null);
			ErrorParserExtensionManager.serializeDefaultErrorParserIds();

			// Check that default IDs are loaded
			ErrorParserExtensionManager.loadDefaultErrorParserIds();
			String[] defaultErrorParserIds = ErrorParserManager.getDefaultErrorParserIds();
			assertNotNull(defaultErrorParserIds);
			assertEquals(DEFAULT_IDS, ErrorParserManager.toDelimitedString(defaultErrorParserIds));
		}
	}

	/**
	 * Test retrieval of error parser, clone() and equals().
	 *
	 * @throws Exception...
	 */
	public void testGetErrorParserCopy() throws Exception {
		{
			IErrorParserNamed clone1 = ErrorParserManager.getErrorParserCopy(REGEX_ERRORPARSER_ID);
			IErrorParserNamed clone2 = ErrorParserManager.getErrorParserCopy(REGEX_ERRORPARSER_ID);
			assertEquals(clone1, clone2);
			assertNotSame(clone1, clone2);
		}
		{
			IErrorParserNamed clone1 = ErrorParserManager.getErrorParserCopy(NOTREGEX_ERRORPARSER_ID);
			IErrorParserNamed clone2 = ErrorParserManager.getErrorParserCopy(NOTREGEX_ERRORPARSER_ID);
			assertEquals(clone1, clone2);
			assertNotSame(clone1, clone2);

			assertTrue(clone1 instanceof ErrorParserNamedWrapper);
			assertTrue(clone2 instanceof ErrorParserNamedWrapper);
			IErrorParser gccClone1 = ((ErrorParserNamedWrapper)clone1).getErrorParser();
			IErrorParser gccClone2 = ((ErrorParserNamedWrapper)clone2).getErrorParser();
			assertNotSame(clone1, clone2);
		}
	}
	
	/**
	 * Check how RegexErrorParser parses output.
	 *
	 * @throws Exception...
	 */
	public void testRegexErrorParserExternalLocation_bug301338() throws Exception {
		RegexErrorParser regexErrorParser = new RegexErrorParser();
		regexErrorParser.addPattern(new RegexErrorPattern("pattern",
				"", "", "", "$0", IMarkerGenerator.SEVERITY_ERROR_RESOURCE, true));

		errorList.clear();
		ErrorParserManager epManager = new ErrorParserManager(fProject, markerGenerator, new String[0]);

		regexErrorParser.processLine("wrong pattern", epManager);
		regexErrorParser.processLine("pattern wrong", epManager);

		assertEquals(0, errorList.size());
	}


}
