package org.eclipse.cdt.internal.debug.application;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.application.Messages;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DebugAttachedExecutable {
	
	private static final String GCC_BUILTIN_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector"; //$NON-NLS-1$
	private static final String GCC_COMPILE_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.debug.application.DwarfLanguageSettingsProvider"; //$NON-NLS-1$
	private static final String GCC_BUILD_OPTIONS_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$ 
	private static final String STANDALONE_QUALIFIER = "org.eclipse.cdt.debug.application"; //$NON-NLS-1$
	private static final String LAST_LAUNCH = "lastLaunch"; //$NON-NLS-1$
	private static final String DEBUG_PROJECT_ID = "org.eclipse.cdt.debug"; //$NON-NLS-1$


	public DebugAttachedExecutable() {
	}
	
	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	private static IProject createCProjectForExecutable(String projectName) throws OperationCanceledException, CoreException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject newProjectHandle = workspace.getRoot().getProject(projectName);

		IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(null);

		IProject newProject = CCorePlugin.getDefault().createCProject(description, newProjectHandle, null,
				DEBUG_PROJECT_ID);

		return newProject;
	}
	
	/**
	 * Import given executable into the Executables project then create a launch configuration.
	 * 
	 * @param monitor
	 * @param executable
	 * @param buildLog
	 * @param arguments
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public static ILaunchConfiguration createLaunchConfig(IProgressMonitor monitor,
			String buildLog)
			throws CoreException, InterruptedException {
		ILaunchConfiguration config = null;
		String defaultProjectName = "Executables"; //$NON-NLS-1$
		ICProject cProject = CoreModel.getDefault().getCModel()
				.getCProject(defaultProjectName);
		
		// if a valid executable is specified, remove any executables already loaded in workspace
		if (cProject.exists()) {
			monitor.subTask(Messages.RemoveOldExecutable);
			IProject proj = cProject.getProject();
			Collection<Executable> elist = ExecutablesManager.getExecutablesManager().getExecutablesForProject(proj);
			Executable[] executables = new Executable[elist.size()];
			elist.toArray(executables);
			@SuppressWarnings("unused")
			IStatus rc = ExecutablesManager.getExecutablesManager().removeExecutables(executables, new NullProgressMonitor());
			// Remove all old members of the Executables project from the last run
			IResource[] resources = proj.members();
			for (IResource resource : resources) {
				resource.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT|IResource.FORCE, new NullProgressMonitor());
			}

			monitor.worked(1);
			// Find last launch if one exists
			String memento = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH));
//					System.out.println("memento is " + memento);
			if (memento != null) {
				ILaunchConfiguration lastConfiguration = getLaunchManager().getLaunchConfiguration(memento);
				try {
					lastConfiguration.getType();
					if (lastConfiguration.exists())
						lastConfiguration.delete();
				} catch (CoreException e) {
					// do nothing
				}
			} 
			
			// Delete project because we have deleted .cproject and settings files
			// by this point so just create a new Executables C project to use for
			// importing the new executable.
			proj.delete(true, new NullProgressMonitor());
			monitor.worked(1);
		} 
		
		// Executables project doesn't already exist, create it
		createCProjectForExecutable(defaultProjectName);
		 
		monitor.worked(3);
		//	either get default project or create it
		cProject = CoreModel.getDefault().getCModel()
				.getCProject(defaultProjectName);
		if (cProject.exists()) {
			File buildLogFile = null;
			final IProject project = cProject.getProject();

			final ICProjectDescriptionManager projDescManager = CCorePlugin
					.getDefault().getProjectDescriptionManager();

			ICProjectDescription projectDescription = projDescManager
					.getProjectDescription(project,
							ICProjectDescriptionManager.GET_WRITABLE);

			monitor.subTask(Messages.SetLanguageProviders);
			final ICConfigurationDescription ccd = projectDescription
					.getActiveConfiguration();
			String[] langProviderIds = ((ILanguageSettingsProvidersKeeper) ccd)
					.getDefaultLanguageSettingsProvidersIds();
			boolean found = false;
			for (int i = 0; i < langProviderIds.length; ++i) {
				if (langProviderIds[i].equals(GCC_BUILTIN_PROVIDER_ID)) {
					found = true;
					break;
				}
			}
			// Look for the GCC builtin LanguageSettingsProvider id.  If it isn't already
			// there, add it.
			if (!found) {
				langProviderIds = Arrays.copyOf(langProviderIds,
						langProviderIds.length + 1);
				langProviderIds[langProviderIds.length - 1] = GCC_BUILTIN_PROVIDER_ID;
			}
			found = false;
			for (int i = 0; i < langProviderIds.length; ++i) {
				if (langProviderIds[i].equals(GCC_COMPILE_OPTIONS_PROVIDER_ID)) {
					found = true;
					break;
				}
			}
			// Look for our macro parser provider id.  If it isn't added already, do so now.
			if (!found) {
				langProviderIds = Arrays.copyOf(langProviderIds,
						langProviderIds.length + 1);
				langProviderIds[langProviderIds.length - 1] = GCC_COMPILE_OPTIONS_PROVIDER_ID;
			}

			if (buildLog != null) {
				File f = new File(buildLog);
				if (f.exists()) {
					buildLogFile = f;
					found = false;
					for (int i = 0; i < langProviderIds.length; ++i) {
						if (langProviderIds[i].equals(GCC_BUILD_OPTIONS_PROVIDER_ID)) {
							found = true;
							break;
						}
					}
					// Look for our macro parser provider id.  If it isn't added already, do so now.
					if (!found) {
						langProviderIds = Arrays.copyOf(langProviderIds,
								langProviderIds.length + 1);
						langProviderIds[langProviderIds.length - 1] = GCC_BUILD_OPTIONS_PROVIDER_ID;
					}
				}
			}

			//						System.out.println("creating language settings providers");
			// Create all the LanguageSettingsProviders
			List<ILanguageSettingsProvider> providers = LanguageSettingsManager
					.createLanguageSettingsProviders(langProviderIds);

			// Update the ids and providers for the configuration.
			((ILanguageSettingsProvidersKeeper) ccd)
			.setDefaultLanguageSettingsProvidersIds(langProviderIds);
			((ILanguageSettingsProvidersKeeper) ccd)
			.setLanguageSettingProviders(providers);

			monitor.worked(1);

			//						System.out.println("before setProjectDescription");

			// Update the project description.
			projDescManager.setProjectDescription(project,
					projectDescription);

			//						System.out.println("after setProjectDescription");

			monitor.worked(1);

			if (buildLogFile != null)	
				// We need to parse the build log to get compile options.  We need to lock the
				// workspace when we do this so we don't have multiple copies of GCCBuildOptionsParser
				// LanguageSettingsProvider and we end up filling in the wrong one.
				project.getWorkspace().run(new BuildOptionsParser(project, buildLogFile), 
						ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());

			//					System.out.println("about to close all editors");
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					final IWorkbenchPage activePage = workbenchWindow.getActivePage();
					if (activePage != null)
						activePage.closeAllEditors(false);
				}
			}
			//					System.out.println("about to create launch configuration");
			config = createConfiguration(true);
			String memento = config.getMemento();
			ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(new QualifiedName(STANDALONE_QUALIFIER, LAST_LAUNCH), memento);
			monitor.worked(1);
		} else {
			System.out.println("Import job failed");
			return null;
		}
		return config;
	}

	protected static ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				"org.eclipse.cdt.launch.attachLaunchType"); //$NON-NLS-1$
	}
	
	protected static ILaunchConfiguration createConfiguration(boolean save) {
//		System.out.println("creating launch configuration");
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName("CDT_DBG_ATTACH")); //$NON-NLS-1$

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					"Executables"); //$NON-NLS-1$
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
			if (save) {
				config = wc.doSave();
			} else {
				config = wc;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}

}
