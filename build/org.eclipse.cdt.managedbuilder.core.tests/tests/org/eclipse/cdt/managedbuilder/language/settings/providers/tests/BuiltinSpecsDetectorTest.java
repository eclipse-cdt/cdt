/*******************************************************************************
 * Copyright (c) 2010, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.envvar.UserDefinedEnvironmentSupplier;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases to test built-in specs detectors.
 */
public class BuiltinSpecsDetectorTest extends BaseTestCase {
	private static final String PROVIDER_ID = "provider.id";
	private static final String PROVIDER_NAME = "provider name";
	private static final String LANGUAGE_ID = "language.test.id";
	private static final String CUSTOM_COMMAND_1 = "echo 1";
	private static final String CUSTOM_COMMAND_2 = "echo 2";
	private static final String ELEM_TEST = "test";
	private static final String ENV_SAMPLE = "SAMPLE";
	private static final String ENV_SAMPLE_VALUE_1 = "Sample Value 1";
	private static final String ENV_SAMPLE_VALUE_2 = "Sample Value 2";

	// those attributes must match that in AbstractBuiltinSpecsDetector
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$

	/**
	 * Dummy to keep boilerplate code.
	 */
	private class DummyBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
		@Override
		protected List<String> parseOptions(String line) {
			return null;
		}

		@Override
		protected AbstractOptionParser[] getOptionParsers() {
			return null;
		}

		@Override
		protected String getCompilerCommand(String languageId) {
			return null;
		}
	}

	/**
	 * Mock built-in specs detector to test basic functionality of {@link AbstractBuiltinSpecsDetector}.
	 */
	private class MockBuiltinSpecsDetector extends DummyBuiltinSpecsDetector {
		@Override
		protected void startupForLanguage(String languageId) throws CoreException {
			super.startupForLanguage(languageId);
		}

		@Override
		protected void shutdownForLanguage() {
			super.shutdownForLanguage();
		}
	}

	/**
	 * Mock built-in specs detector to test execute() functionality.
	 */
	private class MockBuiltinSpecsDetectorExecutedFlag extends DummyBuiltinSpecsDetector {
		@Override
		protected void execute() {
			super.execute();
			waitForProviderToFinish();
		}

		protected boolean isExecuted() {
			return isExecuted;
		}
	}

	/**
	 * Mock built-in specs detector which track how many times it was run.
	 */
	private class MockBuiltinSpecsDetectorWithRunCount extends DummyBuiltinSpecsDetector {
		private int executedCount = 0;

		@Override
		public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker)
				throws CoreException {
			executedCount++;
			super.startup(cfgDescription, cwdTracker);
		}

		@Override
		public MockBuiltinSpecsDetectorEnvironmentChangeListener cloneShallow() throws CloneNotSupportedException {
			MockBuiltinSpecsDetectorEnvironmentChangeListener clone = (MockBuiltinSpecsDetectorEnvironmentChangeListener) super.cloneShallow();
			return clone;
		}

		@Override
		public MockBuiltinSpecsDetectorEnvironmentChangeListener clone() throws CloneNotSupportedException {
			MockBuiltinSpecsDetectorEnvironmentChangeListener clone = (MockBuiltinSpecsDetectorEnvironmentChangeListener) super.clone();
			return clone;
		}

		public int getExecutedCount() {
			return executedCount;
		}
	}

	/**
	 * Mock built-in specs detector to test environment change functionality.
	 */
	private class MockBuiltinSpecsDetectorEnvironmentChangeListener extends DummyBuiltinSpecsDetector {
		private String sampleEnvVarValue = null;

		@Override
		protected boolean validateEnvironment() {
			return false;
		}

		@Override
		protected void execute() {
			super.execute();
			sampleEnvVarValue = environmentMap.get(ENV_SAMPLE);
		}

		@Override
		public MockBuiltinSpecsDetectorEnvironmentChangeListener cloneShallow() throws CloneNotSupportedException {
			MockBuiltinSpecsDetectorEnvironmentChangeListener clone = (MockBuiltinSpecsDetectorEnvironmentChangeListener) super.cloneShallow();
			clone.sampleEnvVarValue = sampleEnvVarValue;
			return clone;
		}

		@Override
		public MockBuiltinSpecsDetectorEnvironmentChangeListener clone() throws CloneNotSupportedException {
			MockBuiltinSpecsDetectorEnvironmentChangeListener clone = (MockBuiltinSpecsDetectorEnvironmentChangeListener) super.clone();
			clone.sampleEnvVarValue = sampleEnvVarValue;
			return clone;
		}

		protected boolean isExecuted() {
			return isExecuted;
		}

		public String getSampleEnvVar() {
			return sampleEnvVarValue;
		}
	}

	/**
	 * Mock built-in specs detector to test parsing functionality.
	 */
	private class MockConsoleBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
		@SuppressWarnings("nls")
		private final AbstractOptionParser[] optionParsers = { new MacroOptionParser("#define (\\S*) *(\\S*)", "$1",
				"$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), };

		@Override
		protected int runProgramForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI,
				OutputStream consoleOut, OutputStream consoleErr, IProgressMonitor monitor)
				throws CoreException, IOException {
			String line = "#define MACRO VALUE";
			consoleOut.write((line + '\n').getBytes());
			consoleOut.flush();
			return ICommandLauncher.OK;
		}

		@Override
		protected IStatus runForEachLanguage(IProgressMonitor monitor) {
			return super.runForEachLanguage(monitor);
		}

		@Override
		protected List<String> parseOptions(final String line) {
			return new ArrayList<String>() {
				{
					add(line);
				}
			};
		}

		@Override
		protected AbstractOptionParser[] getOptionParsers() {
			return optionParsers;
		}

		@Override
		protected String getCompilerCommand(String languageId) {
			return null;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		waitForProviderToFinish();
		super.tearDown();
	}

	/**
	 * Waits until all AbstractBuiltinSpecsDetector jobs are finished.
	 */
	private void waitForProviderToFinish() {
		try {
			Job.getJobManager().join(AbstractBuiltinSpecsDetector.JOB_FAMILY_BUILTIN_SPECS_DETECTOR, null);
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Helper method to fetch configuration descriptions.
	 */
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project, false);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}

	/**
	 * Test configure, getters and setters.
	 */
	public void testAbstractBuiltinSpecsDetector_GettersSetters() throws Exception {
		{
			// provider configured with null parameters
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			provider.configureProvider(PROVIDER_ID, PROVIDER_NAME, null, null, null);

			assertEquals(PROVIDER_ID, provider.getId());
			assertEquals(PROVIDER_NAME, provider.getName());
			assertEquals(null, provider.getLanguageScope());
			assertEquals(null, provider.getSettingEntries(null, null, null));
			assertEquals("", provider.getCommand());
			assertEquals(false, provider.isExecuted());
			assertEquals(false, provider.isConsoleEnabled());
		}

		{
			// provider configured with non-null parameters
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			List<String> languages = new ArrayList<>();
			languages.add(LANGUAGE_ID);
			Map<String, String> properties = new HashMap<>();
			properties.put(ATTR_PARAMETER, CUSTOM_COMMAND_1);
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE",
					ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
			entries.add(entry);

			provider.configureProvider(PROVIDER_ID, PROVIDER_NAME, languages, entries, properties);
			assertEquals(PROVIDER_ID, provider.getId());
			assertEquals(PROVIDER_NAME, provider.getName());
			assertEquals(languages, provider.getLanguageScope());
			assertEquals(entries, provider.getSettingEntries(null, null, null));
			assertEquals(CUSTOM_COMMAND_1, provider.getCommand());
			assertEquals(false, provider.isConsoleEnabled());
			assertEquals(false, provider.isExecuted());

			// setters
			provider.setCommand(CUSTOM_COMMAND_2);
			assertEquals(CUSTOM_COMMAND_2, provider.getCommand());
			provider.setConsoleEnabled(true);
			assertEquals(true, provider.isConsoleEnabled());

			provider.execute();
			assertEquals(true, provider.isExecuted());
			assertEquals(null, provider.getSettingEntries(null, null, null));
		}
	}

	/**
	 * Test clone() and equals().
	 */
	public void testAbstractBuiltinSpecsDetector_CloneAndEquals() throws Exception {
		// define mock detector
		class MockDetectorCloneable extends MockBuiltinSpecsDetectorExecutedFlag implements Cloneable {
			@Override
			public MockDetectorCloneable clone() throws CloneNotSupportedException {
				return (MockDetectorCloneable) super.clone();
			}

			@Override
			public MockDetectorCloneable cloneShallow() throws CloneNotSupportedException {
				return (MockDetectorCloneable) super.cloneShallow();
			}
		}

		// create instance to compare to
		MockDetectorCloneable provider = new MockDetectorCloneable();

		List<String> languages = new ArrayList<>();
		languages.add(LANGUAGE_ID);
		List<ICLanguageSettingEntry> entries = new ArrayList<>();
		ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		entries.add(entry);

		// check clone after initialization
		MockDetectorCloneable clone0 = provider.clone();
		assertTrue(provider.equals(clone0));

		// configure provider
		Map<String, String> properties = new HashMap<>();
		properties.put(ATTR_PARAMETER, CUSTOM_COMMAND_1);
		provider.configureProvider(PROVIDER_ID, PROVIDER_NAME, languages, entries, properties);
		assertEquals(false, provider.isConsoleEnabled());
		provider.setConsoleEnabled(true);
		provider.execute();
		assertEquals(true, provider.isExecuted());
		assertFalse(provider.equals(clone0));

		// check another clone after configuring
		{
			MockDetectorCloneable clone = provider.clone();
			assertTrue(provider.equals(clone));
		}

		// check custom parameter
		{
			MockDetectorCloneable clone = provider.clone();
			clone.setCommand("changed");
			assertFalse(provider.equals(clone));
		}

		// check language scope
		{
			MockDetectorCloneable clone = provider.clone();
			clone.setLanguageScope(null);
			assertFalse(provider.equals(clone));
		}

		// check console flag
		{
			MockDetectorCloneable clone = provider.clone();
			boolean isConsoleEnabled = clone.isConsoleEnabled();
			clone.setConsoleEnabled(!isConsoleEnabled);
			assertFalse(provider.equals(clone));
		}

		// check isExecuted flag
		{
			MockDetectorCloneable clone = provider.clone();
			assertEquals(true, clone.isExecuted());
			clone.clear();
			assertEquals(false, clone.isExecuted());
			assertFalse(provider.equals(clone));
		}

		// check entries
		{
			MockDetectorCloneable clone = provider.clone();
			List<ICLanguageSettingEntry> entries2 = new ArrayList<>();
			entries2.add(new CMacroEntry("MACRO2", "VALUE2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY));
			clone.setSettingEntries(null, null, null, entries2);
			assertFalse(provider.equals(clone));
		}

		// check cloneShallow()
		{
			MockDetectorCloneable provider2 = provider.clone();
			MockDetectorCloneable clone = provider2.cloneShallow();
			assertEquals(false, clone.isExecuted());
			assertFalse(provider2.equals(clone));

			provider2.setSettingEntries(null, null, null, null);
			assertFalse(provider2.equals(clone));

			clone.execute();
			assertTrue(provider2.equals(clone));
		}
	}

	/**
	 * Test basic serialization functionality.
	 */
	public void testAbstractBuiltinSpecsDetector_SerializeDOM() throws Exception {
		{
			// create empty XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);

			// initialize provider
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(false, provider.isExecuted());
			// load the XML to new provider
			provider.load(rootElement);
			assertEquals(false, provider.isConsoleEnabled());
			assertEquals(false, provider.isExecuted());
		}

		Element elementProvider;
		{
			// define mock detector
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(false, provider.isConsoleEnabled());
			assertEquals(false, provider.isExecuted());

			// redefine the settings
			provider.setConsoleEnabled(true);
			assertEquals(true, provider.isConsoleEnabled());

			// serialize in XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);

			assertTrue(xmlString.contains(ATTR_CONSOLE));
		}
		{
			// create another instance of the provider
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(false, provider.isConsoleEnabled());
			assertEquals(false, provider.isExecuted());

			// load element
			provider.load(elementProvider);
			assertEquals(true, provider.isConsoleEnabled());
			assertEquals(false, provider.isExecuted());
		}
	}

	/**
	 * Test serialization of entries and "isExecuted" flag handling.
	 */
	public void testAbstractBuiltinSpecsDetector_SerializeEntriesDOM() throws Exception {
		Element rootElement;
		{
			// create provider
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(new CIncludePathEntry("path0", 1));
			provider.setSettingEntries(null, null, null, entries);
			// serialize entries
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			provider.serializeEntries(rootElement);
			// check XML
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains("path0"));
		}

		{
			// create new provider
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(true, provider.isEmpty());
			assertEquals(false, provider.isExecuted());

			// load the XML to the new provider
			provider.load(rootElement);
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertNotNull(entries);
			assertTrue(entries.size() > 0);
			assertEquals(new CIncludePathEntry("path0", 1), entries.get(0));
			assertEquals(false, provider.isEmpty());
			assertEquals(true, provider.isExecuted());

			// clear the new provider
			provider.clear();
			assertEquals(true, provider.isEmpty());
			assertEquals(false, provider.isExecuted());
		}

		{
			// create new provider
			MockBuiltinSpecsDetectorExecutedFlag provider = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(true, provider.isEmpty());
			assertEquals(false, provider.isExecuted());

			// execute provider
			provider.execute();
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertEquals(null, entries);
			// executed provider should NOT appear as empty even with no entries set
			assertEquals(false, provider.isEmpty());
			assertEquals(true, provider.isExecuted());
		}
	}

	/**
	 * Smoke test exercising passing {@code null} to the functions.
	 */
	public void testAbstractBuiltinSpecsDetector_Nulls() throws Exception {
		{
			// test AbstractBuiltinSpecsDetector.processLine(...) flow
			MockBuiltinSpecsDetector provider = new MockBuiltinSpecsDetector();
			provider.startup(null, null);
			provider.startupForLanguage(null);
			provider.processLine(null);
			provider.shutdownForLanguage();
			provider.shutdown();
		}
		{
			// test AbstractBuiltinSpecsDetector.processLine(...) flow
			MockConsoleBuiltinSpecsDetector provider = new MockConsoleBuiltinSpecsDetector();
			provider.startup(null, null);
			provider.runForEachLanguage(null);
			provider.shutdown();
		}
	}

	/**
	 * Test basic parsing functionality.
	 */
	public void testAbstractBuiltinSpecsDetector_RunConfiguration() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		MockConsoleBuiltinSpecsDetector provider = new MockConsoleBuiltinSpecsDetector();
		provider.setLanguageScope(new ArrayList<String>() {
			{
				add(LANGUAGE_ID);
			}
		});

		// Run provider
		provider.startup(cfgDescription, null);
		provider.runForEachLanguage(null);
		provider.shutdown();

		assertFalse(provider.isEmpty());

		List<ICLanguageSettingEntry> noentries = provider.getSettingEntries(null, null, null);
		assertNull(noentries);

		// Check parsed entries
		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(cfgDescription, null, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	/**
	 * Smoke test running as global provider on workspace level.
	 */
	public void testAbstractBuiltinSpecsDetector_RunGlobal() throws Exception {
		// Create provider
		MockConsoleBuiltinSpecsDetector provider = new MockConsoleBuiltinSpecsDetector();
		provider.setLanguageScope(new ArrayList<String>() {
			{
				add(LANGUAGE_ID);
			}
		});

		// Run provider
		provider.startup(null, null);
		provider.runForEachLanguage(null);
		provider.shutdown();

		assertFalse(provider.isEmpty());

		// Check parsed entries
		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	/**
	 * Test environment changes for provider registered to configuration.
	 */
	public void testAbstractBuiltinSpecsDetector_EnvChangesConfiguration_1() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// Create provider
		MockBuiltinSpecsDetectorEnvironmentChangeListener provider = new MockBuiltinSpecsDetectorEnvironmentChangeListener();
		// register environment listener on configuration - note that provider is not included in the configuration
		provider.registerListener(cfgDescription);
		waitForProviderToFinish();
		assertEquals(true, provider.isExecuted());
		assertEquals(null, provider.getSampleEnvVar());
		// unset "isExecuted" flag
		provider.clear();
		assertEquals(false, provider.isExecuted());
		assertEquals(null, provider.getSampleEnvVar());

		// Set an environment variable to the configuration
		{
			ICProjectDescription prjDescriptionWritable = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription cfgDescriptionWritable = prjDescriptionWritable.getActiveConfiguration();
			// create and set sample environment variable in the configuration
			IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

			// Set an environment variable
			IEnvironmentVariable var = new EnvironmentVariable(ENV_SAMPLE, ENV_SAMPLE_VALUE_1);
			contribEnv.addVariable(var, cfgDescriptionWritable);
			assertEquals(var, envManager.getVariable(ENV_SAMPLE, cfgDescriptionWritable, true));

			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		waitForProviderToFinish();
		// check if provider got executed with new value
		assertEquals(true, provider.isExecuted());
		assertEquals(ENV_SAMPLE_VALUE_1, provider.getSampleEnvVar());

		// Repeat one more time with different value of environment variable

		// unset "isExecuted" flag
		provider.clear();
		assertEquals(false, provider.isExecuted());
		assertEquals(ENV_SAMPLE_VALUE_1, provider.getSampleEnvVar());

		// Set an environment variable to the configuration
		{
			ICProjectDescription prjDescriptionWritable = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription cfgDescriptionWritable = prjDescriptionWritable.getActiveConfiguration();
			// create and set sample environment variable in the configuration
			IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

			// Set an environment variable
			IEnvironmentVariable var = new EnvironmentVariable(ENV_SAMPLE, ENV_SAMPLE_VALUE_2);
			contribEnv.addVariable(var, cfgDescriptionWritable);
			assertEquals(var, envManager.getVariable(ENV_SAMPLE, cfgDescriptionWritable, true));

			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		waitForProviderToFinish();
		// check if provider got executed with new value
		assertEquals(true, provider.isExecuted());
		assertEquals(ENV_SAMPLE_VALUE_2, provider.getSampleEnvVar());

		// unregister listeners
		provider.unregisterListener();
	}

	/**
	 * Test running on environment changes as provider assigned to a configuration.
	 */
	public void testAbstractBuiltinSpecsDetector_EnvChangesConfiguration_2() throws Exception {
		// Create a project with one configuration
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());

		// Assign a provider to configuration
		{
			ICProjectDescription prjDescriptionWritable = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription cfgDescriptionWritable = prjDescriptionWritable.getActiveConfiguration();
			// Create provider
			MockBuiltinSpecsDetectorEnvironmentChangeListener provider = new MockBuiltinSpecsDetectorEnvironmentChangeListener();
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(provider);
			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);
			// Write to project description
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);

			waitForProviderToFinish();
			// Check that provider got executed
			assertEquals(true, provider.isExecuted());
			assertEquals(null, provider.getSampleEnvVar());
		}

		// Set environment variable to the configuration
		{
			ICProjectDescription prjDescriptionWritable = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription cfgDescriptionWritable = prjDescriptionWritable.getActiveConfiguration();
			IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

			// Set an environment variable
			IEnvironmentVariable var = new EnvironmentVariable(ENV_SAMPLE, ENV_SAMPLE_VALUE_1);
			contribEnv.addVariable(var, cfgDescriptionWritable);
			assertEquals(var, envManager.getVariable(ENV_SAMPLE, cfgDescriptionWritable, true));

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable)
					.getLanguageSettingProviders();
			MockBuiltinSpecsDetectorEnvironmentChangeListener provider = (MockBuiltinSpecsDetectorEnvironmentChangeListener) providers
					.get(0);
			// unset "isExecuted" flag
			provider.clear();
			assertEquals(false, provider.isExecuted());
			assertEquals(null, provider.getSampleEnvVar());

			// Save project description including saving environment to the configuration
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}
		waitForProviderToFinish();

		// Check if the provider got executed
		{
			// check if environment variable got there
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
			ICConfigurationDescription cfgDescription = prjDescription.getActiveConfiguration();
			IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IEnvironmentVariable var = envManager.getVariable(ENV_SAMPLE, cfgDescription, true);
			assertNotNull(var);
			assertEquals(ENV_SAMPLE_VALUE_1, var.getValue());

			// check if provider got executed with new value
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			MockBuiltinSpecsDetectorEnvironmentChangeListener provider = (MockBuiltinSpecsDetectorEnvironmentChangeListener) providers
					.get(0);
			assertEquals(true, provider.isExecuted());
			assertEquals(ENV_SAMPLE_VALUE_1, provider.getSampleEnvVar());
		}

		// Repeat one more time with different value of environment variable
		// Set another environment variable to the configuration
		{
			ICProjectDescription prjDescriptionWritable = CoreModel.getDefault().getProjectDescription(project, true);
			ICConfigurationDescription cfgDescriptionWritable = prjDescriptionWritable.getActiveConfiguration();
			IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

			// Set an environment variable
			IEnvironmentVariable var = new EnvironmentVariable(ENV_SAMPLE, ENV_SAMPLE_VALUE_2);
			contribEnv.addVariable(var, cfgDescriptionWritable);
			assertEquals(var, envManager.getVariable(ENV_SAMPLE, cfgDescriptionWritable, true));

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable)
					.getLanguageSettingProviders();
			MockBuiltinSpecsDetectorEnvironmentChangeListener provider = (MockBuiltinSpecsDetectorEnvironmentChangeListener) providers
					.get(0);
			// unset "isExecuted" flag
			provider.clear();
			assertEquals(false, provider.isExecuted());
			assertEquals(ENV_SAMPLE_VALUE_1, provider.getSampleEnvVar());

			// Save project description including saving environment to the configuration
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}
		waitForProviderToFinish();

		// Check if the provider got executed
		{
			// check if environment variable got there
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
			ICConfigurationDescription cfgDescription = prjDescription.getActiveConfiguration();
			IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IEnvironmentVariable var = envManager.getVariable(ENV_SAMPLE, cfgDescription, true);
			assertNotNull(var);
			assertEquals(ENV_SAMPLE_VALUE_2, var.getValue());

			// check if provider got executed with new value
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			MockBuiltinSpecsDetectorEnvironmentChangeListener provider = (MockBuiltinSpecsDetectorEnvironmentChangeListener) providers
					.get(0);
			assertEquals(true, provider.isExecuted());
			assertEquals(ENV_SAMPLE_VALUE_2, provider.getSampleEnvVar());
		}
	}

	/**
	 * Test running on environment changes as global provider on workspace level.
	 */
	public void testAbstractBuiltinSpecsDetector_EnvChangesGlobal() throws Exception {
		// Create provider
		MockBuiltinSpecsDetectorEnvironmentChangeListener provider = new MockBuiltinSpecsDetectorEnvironmentChangeListener();
		// register environment listener on workspace
		provider.registerListener(null);
		waitForProviderToFinish();
		assertEquals(true, provider.isExecuted());
		assertEquals(null, provider.getSampleEnvVar());
		// unset "isExecuted" flag
		provider.clear();
		assertEquals(false, provider.isExecuted());
		assertEquals(null, provider.getSampleEnvVar());

		// create and set sample environment variable in the workspace
		UserDefinedEnvironmentSupplier fUserSupplier = EnvironmentVariableManager.fUserSupplier;
		StorableEnvironment vars = fUserSupplier.getWorkspaceEnvironmentCopy();
		vars.createVariable(ENV_SAMPLE, ENV_SAMPLE_VALUE_1);
		fUserSupplier.setWorkspaceEnvironment(vars);

		waitForProviderToFinish();

		// check if provider got executed with new value
		assertEquals(true, provider.isExecuted());
		assertEquals(ENV_SAMPLE_VALUE_1, provider.getSampleEnvVar());

		provider.clear();
		// create and set sample environment variable in the workspace
		vars.deleteAll();
		vars.createVariable(ENV_SAMPLE, ENV_SAMPLE_VALUE_2);
		fUserSupplier.setWorkspaceEnvironment(vars);

		waitForProviderToFinish();
		// check if provider got executed with new value
		assertEquals(true, provider.isExecuted());
		assertEquals(ENV_SAMPLE_VALUE_2, provider.getSampleEnvVar());

		// unregister listeners
		provider.unregisterListener();
	}

	/**
	 * Test running a provider on compiler upgrades.
	 */
	public void testAbstractBuiltinSpecsDetector_CompilerUpgrade() throws Exception {
		// Create a folder for this test
		IPath folder = ResourceHelper.createWorkspaceFolder(getName());

		// Create test "compiler"
		java.io.File compiler = new java.io.File(folder.append("compiler").toOSString());
		compiler.createNewFile();
		assertTrue(compiler.exists());
		String compilerPath = compiler.getAbsolutePath();

		// Create provider
		MockBuiltinSpecsDetectorWithRunCount provider = new MockBuiltinSpecsDetectorWithRunCount();
		provider.setCommand('"' + compilerPath + '"' + " arg1");
		// register environment listener on workspace
		provider.registerListener(null);
		waitForProviderToFinish();
		assertEquals(1, provider.getExecutedCount());

		// Check that an event doesn't trigger unnecessary rerun
		provider.handleEvent(null);
		waitForProviderToFinish();
		assertEquals(1, provider.getExecutedCount());

		// "Upgrade" the "compiler"
		long lastModified = compiler.lastModified();
		// less than 1 sec might be truncated
		compiler.setLastModified(lastModified + 1000);
		long lastModifiedUpdated = compiler.lastModified();
		assertTrue(lastModifiedUpdated != lastModified);

		// Check that an event triggers rerun after upgrade
		provider.handleEvent(null);
		waitForProviderToFinish();
		assertEquals(2, provider.getExecutedCount());

		// unregister listeners
		provider.unregisterListener();
	}

	/**
	 * Test running a provider on compiler upgrades when the compiler is a symbolic link.
	 */
	public void testAbstractBuiltinSpecsDetector_CompilerUpgrade_SymbolicLink() throws Exception {
		// do not test on systems where symbolic links are not supported
		if (!ResourceHelper.isSymbolicLinkSupported()) {
			return;
		}

		// Create a folder for this test
		IPath folder = ResourceHelper.createWorkspaceFolder(getName());

		// Create test "compiler"
		IPath compilerLocation = folder.append("compiler");
		java.io.File compiler = new java.io.File(compilerLocation.toOSString());
		compiler.createNewFile();
		assertTrue(compiler.exists());
		// Create symbolic link to the test compiler
		IPath compilerLinkLocation = folder.append("compilerLink");
		ResourceHelper.createSymbolicLink(compilerLinkLocation, compilerLocation);
		java.io.File compilerLink = new java.io.File(compilerLinkLocation.toOSString());
		assertTrue(compilerLink.exists());
		String compilerLinkPath = compilerLink.getAbsolutePath();

		// Create provider
		MockBuiltinSpecsDetectorWithRunCount provider = new MockBuiltinSpecsDetectorWithRunCount();
		provider.setCommand('"' + compilerLinkPath + '"' + " arg1");
		// register environment listener on workspace
		provider.registerListener(null);
		waitForProviderToFinish();
		assertEquals(1, provider.getExecutedCount());

		// Check that an event doesn't trigger unnecessary rerun
		provider.handleEvent(null);
		waitForProviderToFinish();
		assertEquals(1, provider.getExecutedCount());

		// "Upgrade" the "compiler". Note that less than 1 sec might be truncated.
		long lastModified = compiler.lastModified();
		// less than 1 sec might be truncated
		compiler.setLastModified(lastModified + 1000);
		long lastModifiedUpdated = compiler.lastModified();
		assertTrue(lastModifiedUpdated != lastModified);

		// Check that an event triggers rerun after upgrade
		provider.handleEvent(null);
		waitForProviderToFinish();
		assertEquals(2, provider.getExecutedCount());

		// unregister listeners
		provider.unregisterListener();
	}

	/**
	 * Test running a provider after changing the compiler command.
	 */
	public void testAbstractBuiltinSpecsDetector_RerunOnCommandArgsChange() throws Exception {
		// Create provider
		MockBuiltinSpecsDetectorWithRunCount provider = new MockBuiltinSpecsDetectorWithRunCount();
		provider.setCommand("compiler arg1");
		// register environment listener on workspace
		provider.registerListener(null);
		waitForProviderToFinish();
		assertEquals(1, provider.getExecutedCount());

		// Check that an event doesn't trigger unnecessary rerun
		provider.handleEvent(null);
		waitForProviderToFinish();
		assertEquals(1, provider.getExecutedCount());

		// Change the compiler command argument
		provider.setCommand("compiler arg2");

		// Check that an event triggers rerun after changing the compiler command
		provider.handleEvent(null);
		waitForProviderToFinish();
		assertEquals(2, provider.getExecutedCount());

		// unregister listeners
		provider.unregisterListener();
	}

	/**
	 * Check that entries get grouped by kinds by stock built-in specs detector.
	 */
	public void testAbstractBuiltinSpecsDetector_GroupSettings() throws Exception {
		// define benchmarks
		final CIncludePathEntry includePath_1 = new CIncludePathEntry("/include/path_1",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CIncludePathEntry includePath_2 = new CIncludePathEntry("/include/path_2",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CIncludeFileEntry includeFile_1 = new CIncludeFileEntry(new Path("/include.file1"),
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CIncludeFileEntry includeFile_2 = new CIncludeFileEntry(new Path("/include.file2"),
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CMacroEntry macro_1 = new CMacroEntry("MACRO_1", "", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CMacroEntry macro_2 = new CMacroEntry("MACRO_2", "",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.UNDEFINED);
		final CMacroFileEntry macroFile_1 = new CMacroFileEntry(new Path("/macro.file1"),
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CMacroFileEntry macroFile_2 = new CMacroFileEntry(new Path("/macro.file2"),
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryPathEntry libraryPath_1 = new CLibraryPathEntry(new Path("/lib/path_1"),
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryPathEntry libraryPath_2 = new CLibraryPathEntry(new Path("/lib/path_2"),
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryFileEntry libraryFile_1 = new CLibraryFileEntry("lib_1.a",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryFileEntry libraryFile_2 = new CLibraryFileEntry("lib_2.a",
				ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);

		// Define mock detector adding unorganized entries
		MockBuiltinSpecsDetector provider = new MockBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				detectedSettingEntries.add(libraryFile_1);
				detectedSettingEntries.add(libraryPath_1);
				detectedSettingEntries.add(macroFile_1);
				detectedSettingEntries.add(macro_1);
				detectedSettingEntries.add(includeFile_1);
				detectedSettingEntries.add(includePath_1);

				detectedSettingEntries.add(includePath_2);
				detectedSettingEntries.add(includeFile_2);
				detectedSettingEntries.add(macro_2);
				detectedSettingEntries.add(macroFile_2);
				detectedSettingEntries.add(libraryPath_2);
				detectedSettingEntries.add(libraryFile_2);
				return true;
			}
		};

		// run specs detector
		provider.startup(null, null);
		provider.startupForLanguage(null);
		provider.processLine("");
		provider.shutdownForLanguage();
		provider.shutdown();

		// compare benchmarks, expected well-sorted
		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);

		int i = 0;
		assertEquals(includePath_1, entries.get(i++));
		assertEquals(includePath_2, entries.get(i++));
		assertEquals(includeFile_1, entries.get(i++));
		assertEquals(includeFile_2, entries.get(i++));
		assertEquals(macro_1, entries.get(i++));
		assertEquals(macro_2, entries.get(i++));
		assertEquals(macroFile_1, entries.get(i++));
		assertEquals(macroFile_2, entries.get(i++));
		assertEquals(libraryPath_1, entries.get(i++));
		assertEquals(libraryPath_2, entries.get(i++));
		assertEquals(libraryFile_1, entries.get(i++));
		assertEquals(libraryFile_2, entries.get(i++));

		assertEquals(12, entries.size());
	}
}
