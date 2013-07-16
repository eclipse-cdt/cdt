package org.eclipse.cdt.thirdParty.tests.suite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

public class ThirdPartyLanguageSettingsProvidersTest extends AbstractThirdPartyTest {
	
	protected final static String LANGUAGE_SETTINGS_PROVIDER_CDT_USER = "org.eclipse.cdt.ui.UserLanguageSettingsProvider"; //$NON-NLS-1$
	protected final static String LANGUAGE_SETTINGS_PROVIDER_BUILTIN_SPECS_DETECTOR = "org.eclipse.cdt.thirdParty.tests.languageSettingsProviders.ThirdPartyBuiltinSpecsDetector"; //$NON-NLS-1$
	protected final static String LANGUAGE_SETTINGS_PROVIDER_BUILD_OUTPUT_PARSER = "org.eclipse.cdt.thirdParty.tests.languageSettingsProviders.ThirdPartyBuildOutputParser";
	
	public static Test suite() {
		return new TestSuite(ThirdPartyLanguageSettingsProvidersTest.class);
	}

	public void testThirdPartyLanguageSettingsProviders(){
		// Create new project
		IProject project = null;
		try {
			project = createProject("LanguageSettingsProviderTestProject");
			// Now associate the builder with the project
			ManagedBuildTestHelper.addManagedBuildNature(project);
			IProjectDescription description = project.getDescription();
			// Make sure it has a managed nature
			if (description != null) {
				assertTrue(description.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
			}

		} catch (CoreException e) {
			fail("Test failed on project creation: " + e.getLocalizedMessage());
		}
		// Find the base project type definition
		IProjectType projType = ManagedBuildManager.getProjectType("org.eclipse.cdt.thirdParty.tests.thirdPartyProjectType");
		assertNotNull(projType);
		
		try {
			// Create a managed project
			IManagedBuildInfo managedBuildInfo = ManagedBuildManager.createBuildInfo(project);
			IManagedProject managedProject = ManagedBuildManager.createManagedProject(project, projType);
			managedBuildInfo.setManagedProject(managedProject);
			ICProjectDescription projectDescription = CCorePlugin.getDefault().getProjectDescription(project, true);
			if(projectDescription != null) {
				ICConfigurationDescription configDesc = projectDescription.createConfiguration("testConfiguration", "test_configuration", CCorePlugin.getDefault().getPreferenceConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID));
				IConfiguration configuration = ManagedBuildManager.getConfigurationForDescription(configDesc);
				configuration.setName("THIRD_PARTY_TEST_CONFIG");
				IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
				for (IToolChain toolChain : toolChains) {
					if(toolChain.getId().equalsIgnoreCase("org.eclipse.cdt.thirdParty.tests.ThirdPartyToolChain")) {
						IToolChain newChain = configuration.createToolChain(toolChain, ManagedBuildManager.calculateChildId(toolChain.getId(), null), toolChain.getId(), false);
						newChain.createOptions(toolChain);
						ITool[] tools = toolChain.getTools();
						for (ITool tool : tools) {
							IOption[] options = tool.getOptions();
							for(IOption option : options) {
								tool.createOption(option, ManagedBuildManager.calculateChildId(option.getId(),null), option.getName(), false);
							}
						}
						break;
					}
				}
				
				IBuilder builder = configuration.getEditableBuilder();
				builder.setCommand("make -f Makefile");
				builder.setManagedBuildOn(false);
				
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();

				// compiler inspection provider with specific command
				ILanguageSettingsProvider provider = LanguageSettingsManager.getExtensionProviderCopy(LANGUAGE_SETTINGS_PROVIDER_BUILTIN_SPECS_DETECTOR, true);
				if(provider instanceof AbstractBuiltinSpecsDetector) {
					((AbstractBuiltinSpecsDetector)provider).setCommand("gcc -E -P -v -dD ${INPUTS}");
					providers.add(provider);
				}
				
				// cdt user settings provider with test setting
				provider = LanguageSettingsManager.getExtensionProviderCopy(LANGUAGE_SETTINGS_PROVIDER_CDT_USER, true);
				if(provider instanceof ILanguageSettingsEditableProvider) {
					for(String languageID : getLanguageIDsForConfig(project, configDesc)) {
						List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
						entries.add(CDataUtil.createCMacroEntry("X", "1", 0)); //$NON-NLS-1$ 
						if(provider instanceof ILanguageSettingsEditableProvider) {
							((ILanguageSettingsEditableProvider)provider).setSettingEntries(configDesc, project, languageID, entries);
						} else {
							fail("Language settings provider not editable: " + provider.getName()); //$NON-NLS-1$
						}
						((ILanguageSettingsEditableProvider)provider).setSettingEntries(configDesc, project, languageID, entries);
						if(provider instanceof LanguageSettingsSerializableProvider) {
							LanguageSettingsManager.setStoringEntriesInProjectArea((LanguageSettingsSerializableProvider)provider, true);
						}
					}
				}
				providers.add(provider);
				
				// console output parser
				provider = LanguageSettingsManager.getExtensionProviderCopy(LANGUAGE_SETTINGS_PROVIDER_BUILD_OUTPUT_PARSER, true);
				if(provider instanceof AbstractBuildCommandParser) {
					((AbstractBuildCommandParser)provider).setCompilerPattern("gcc");
					((AbstractBuildCommandParser)provider).setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
					((AbstractBuildCommandParser)provider).setResolvingPaths(false);
				}
				providers.add(provider);

				if(configDesc instanceof ILanguageSettingsProvidersKeeper) {
					((ILanguageSettingsProvidersKeeper) configDesc).setLanguageSettingProviders(providers);
				}
				ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(null));
				CCorePlugin.getDefault().setProjectDescription(project, projectDescription);
				
				managedBuildInfo.setDefaultConfiguration(configuration);
				ManagedBuildManager.saveBuildInfo(project, true);
					
				addTestFiles(project);

				// TODO - this invocation of the build did not work for me - could somebody please give me a hint how to correctly invoke this?
				ManagedBuildManager.buildConfigurations(new IConfiguration[] {configuration}, null, new NullProgressMonitor(), true, IncrementalProjectBuilder.FULL_BUILD);

				// close and reopen the project to synchronize
				project.close(null);
				project.open(null);
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		}

		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
		}

		verifyLanguageSettingsProvidersOutput(project);
	}

	private void addTestFiles(IProject project) {
		addFile(project, "Makefile");
		addFile(project, "test_cpp.cpp");
		addFile(project, "test.c");
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
		}
	}

	private void addFile(IProject project, String fileName) {
		URL url = Platform.getBundle("org.eclipse.cdt.thirdParty.tests").getEntry("/resources/" + getResourceDir() + "/" + fileName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		FileOutputStream output = null;
		try {
			File testFile = new File(FileLocator.toFileURL(url).getFile());
			InputStream input = new FileInputStream(testFile);
			File outputFile = new File(project.getLocation().toString(), fileName);
			if(!outputFile.getParentFile().exists()) {
				assertTrue("Failure creating directory in extractProjects", outputFile.getParentFile().mkdirs()); //$NON-NLS-1$
			}
			output = new FileOutputStream(outputFile);
			try {
				int count;
				byte[] buf = new byte[4096];
				while ((count = input.read(buf)) != -1) {
					output.write( buf, 0, count );
				}
				output.close();
			} catch(IOException e) {
				if(output != null) {
					assertTrue("IOException thrown in inner try: " + e, false); //$NON-NLS-1$
					output.close();
				}
			}
		} catch (IOException e) {
			fail("Test failed on adding testfile: " + e.getLocalizedMessage());
		}	
	}

	protected Set<String> getLanguageIDsForConfig(IProject proj, ICConfigurationDescription configDesc) {
		Set<String> languageIDs = new HashSet<String>();
		ICResourceDescription rcDes = configDesc.getResourceDescription(proj.getLocation(), false);
		if(rcDes != null) {
			languageIDs.addAll(LanguageSettingsManager.getLanguages(rcDes));
		}

		return languageIDs;
	}
	
	private void verifyLanguageSettingsProvidersOutput(IProject project) {
		ICProjectDescription projDesc = CCorePlugin.getDefault().getProjectDescription(project, true);
		if(projDesc != null) {
			ICConfigurationDescription[] configDescs = projDesc.getConfigurations();
			assertTrue(configDescs.length > 0);
			for (ICConfigurationDescription configDesc : configDescs) {
				List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) configDesc).getLanguageSettingProviders();
				assertTrue(providers.size() == 3);
				for (ILanguageSettingsProvider provider : providers) {
					ICResourceDescription rcDes = configDesc.getResourceDescription(project.getLocation(), false);
					if(rcDes != null) {
						List<String> languageIDs = LanguageSettingsManager.getLanguages(rcDes);
						// check the status of the compiler inspection provider again - why is this failing?
						if(provider instanceof AbstractBuiltinSpecsDetector) {
							assertTrue(provider.getId().equals(LANGUAGE_SETTINGS_PROVIDER_BUILTIN_SPECS_DETECTOR));
							String command = ((AbstractBuiltinSpecsDetector)provider).getCommand();
							assertNotNull(command);
							assertTrue(command.length() > 0);
							for (String languageID : languageIDs) {
								List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, configDesc, project, languageID);
								assertTrue(entries.size() > 0);
							}
						} else if(provider instanceof AbstractBuildCommandParser) {
							assertTrue(provider.getId().equals(LANGUAGE_SETTINGS_PROVIDER_BUILD_OUTPUT_PARSER));
							for (String languageID : languageIDs) {
								List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, configDesc, project, languageID);
								assertTrue(entries.size() == 1);
								for (ICLanguageSettingEntry entry : entries) {
									if(entry.getKind() == ICSettingEntry.INCLUDE_PATH) {
										if(languageID.equals("org.eclipse.cdt.thirdParty.tests.thirdPartyCpp")) {
											assertTrue(entry.getName().equals("/test/cpp/include")); //$NON-NLS-1$
										} else if(languageID.equals("org.eclipse.cdt.thirdParty.tests.thirdPartyC")) {
											assertTrue(entry.getName().equals("/test/include")); //$NON-NLS-1$
										}
									}
								}
							}
						} else if(provider.getId().equals(LANGUAGE_SETTINGS_PROVIDER_CDT_USER)) {
							for (String languageID : languageIDs) {
								List<ICLanguageSettingEntry> entries = provider.getSettingEntries(configDesc, project, languageID);
								assertNotNull(entries);
								assertTrue(entries.size() == 1);
								for (ICLanguageSettingEntry entry : entries) {
									if(entry.getKind() == ICSettingEntry.MACRO) {
										assertTrue(entry.getName().equals("X") && entry.getValue().equals("1")); //$NON-NLS-1$
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private IProject createProject(String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject newProjectHandle = root.getProject(name);
		IProject project = null;

		if (!newProjectHandle.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(), ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID);
		} else {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			project = newProjectHandle;
		}

		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}

		return project;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		// commented out for now to be able to have a look at the project after running the test
//		for(Iterator<IProject> iter = projList.iterator(); iter.hasNext();){
//			IProject proj = iter.next();
//			try {
//				proj.delete(true, null);
//			} catch (Exception e){
//			}
//			iter.remove();
//		}
//		super.tearDown();
	}

	@Override
	protected String getResourceDir() {
		return "languageSettingsProvider"; //$NON-NLS-1$
	}
}
