/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.utils.elf.parser.GNUElfParser;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final String GCC_BUILTIN_PROVIDER_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector"; //$NON-NLS-1$
	private ILaunchConfiguration config;

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    @Override
	public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(400, 300));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowMenuBar(true);
		configurer.setTitle(Messages.Debugger_Title);
		System.out.println("test");
		IWorkbenchActivitySupport support = getWindowConfigurer().getWindow()
				.getWorkbench().getActivitySupport();
		IActivityManager manager = support.getActivityManager();
		@SuppressWarnings("rawtypes")
		Set ids = manager.getEnabledActivityIds();
		@SuppressWarnings("rawtypes")
		Set defined = manager.getDefinedActivityIds();
		for (Object obj : ids) {
			String id = (String) obj;
			System.out.println("enabled id is " + id);
		}
		for (Object obj : defined) {
			String id = (String) obj;
			System.out.println("defined id is " + id);
		}
	}

	private class CWDTracker implements IWorkingDirectoryTracker {

		@Override
		public URI getWorkingDirectoryURI() {
			return null;
		}

	}
	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		String executable = "";
		String arguments = null;
		String[] args = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; ++i) {
			if ("-application".equals(args[i]))
				i++; // ignore the application specifier
			else if ("-e".equals(args[i])) {
				++i;
				if (i < args.length)
					executable = args[i];
				++i;
				if (i < args.length)
					arguments = args[i];
			}
		}
		final String[] fileNames = { executable };
		Job importJob = new Job(Messages.ExecutablesView_ImportExecutables) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				ExecutablesManager.getExecutablesManager().importExecutables(
						fileNames, monitor);
				return Status.OK_STATUS;
			}
		};
		importJob.schedule();
		try {
			importJob.join();
			if (importJob.getResult() == Status.OK_STATUS) {
//				 See if the default project exists
				String defaultProjectName = "Executables"; //$NON-NLS-1$
				ICProject cProject = CoreModel.getDefault().getCModel()
						.getCProject(defaultProjectName);
				if (cProject.exists()) {
					final IProject project = cProject.getProject();

					final ICProjectDescriptionManager projDescManager = CCorePlugin
							.getDefault().getProjectDescriptionManager();

					ICProjectDescription projectDescription = projDescManager
							.getProjectDescription(project,
									ICProjectDescriptionManager.GET_WRITABLE);

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
					if (!found) {
						langProviderIds = Arrays.copyOf(langProviderIds,
								langProviderIds.length + 1);
						langProviderIds[langProviderIds.length - 1] = GCC_BUILTIN_PROVIDER_ID;
						List<ILanguageSettingsProvider> providers = LanguageSettingsManager
								.createLanguageSettingsProviders(langProviderIds);
						final GCCCompileOptionsParser parser = new GCCCompileOptionsParser();
						Job parseJob = new Job(Messages.GetCompilerOptions) {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								IWorkingDirectoryTracker cwdTracker = new CWDTracker();
								try {
									ICProjectDescription projDesc = projDescManager
											.getProjectDescription(project,
													false);
									ICConfigurationDescription ccdesc = projDesc
											.getActiveConfiguration();
									parser.startup(ccdesc, cwdTracker);
									GNUElfParser binParser = new GNUElfParser();
									IBinaryFile bf = binParser
											.getBinary(new Path(fileNames[0]));
									ISymbolReader reader = (ISymbolReader)bf.getAdapter(ISymbolReader.class);
									String[] sourceFiles = reader
											.getSourceFiles();
									for (String sourceFile : sourceFiles) {
										IPath sourceFilePath = new Path(
												sourceFile);
										String sourceName = sourceFilePath
												.lastSegment();
										IContainer c = createFromRoot(project,
												new Path(sourceFile));
										Path sourceNamePath = new Path(
												sourceName);
										IFile source = c
												.getFile(sourceNamePath);
										source.createLink(sourceFilePath, 0,
												null);
									}
									// if (reader instanceof
									// ICompileOptionsFinder) {
									// ICompileOptionsFinder f =
									// (ICompileOptionsFinder) reader;
									// for (String fileName : sourceFiles) {
									// parser.setCurrentResourceName(fileName);
									// parser.processLine(f
									// .getCompileOptions(fileName));
									// }
									// parser.shutdown();
									// }
								} catch (CoreException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								return Status.OK_STATUS;
							}

						};
						parseJob.schedule();
						providers.add(parser);
						((ILanguageSettingsProvidersKeeper) ccd)
								.setLanguageSettingProviders(providers);
						projDescManager.setProjectDescription(project,
								projectDescription);
					}
				}
				config = createConfiguration(executable, arguments, true);
				DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private IContainer createFromRoot(IProject exeProject, IPath path)
			throws CoreException {
		int segmentCount = path.segmentCount() - 1;
		IContainer currentFolder = exeProject;

		for (int i = 0; i < segmentCount; i++) {
			currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
			if (!currentFolder.exists()) {
				((IFolder) currentFolder).create(IResource.VIRTUAL
						| IResource.DERIVED, true, new NullProgressMonitor());
			}
		}

		return currentFolder;
	}

	@Override
	public void postWindowClose() {
		super.postWindowClose();
		// We delete the launch configuration we created to keep workspace clean
		// If a user creates a launch configuration manually, then it is up to
		// the user to remove it
		try {
			config.delete();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				"org.eclipse.cdt.launch.applicationLaunchType"); //$NON-NLS-1$
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected ILaunchConfiguration createConfiguration(String bin,
			String arguments, boolean save) {
		ILaunchConfiguration config = null;
		try {
			String projectName = bin;
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName(bin));

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
					projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					"Executables"); //$NON-NLS-1$
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
			if (arguments != null)
				wc.setAttribute(
						ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
						arguments);
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
