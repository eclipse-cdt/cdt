/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.osgi.service.prefs.Preferences;

public class LaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(IEditorPart editor, String mode) {
		searchAndLaunch(new Object[] { editor.getEditorInput() }, mode);
	}

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).toArray(), mode);
		}
	}

	public void launch(IBinary bin, String mode) {
		ILaunchConfiguration config = findLaunchConfiguration(bin, mode);
		if (config != null) {
			DebugUITools.launch(config, mode);
		}
	}

	/**
	 * Method getLaunchConfigType.
	 * 
	 * @return ILaunchConfigurationType
	 */
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager()
				.getLaunchConfigurationType(ILaunchConstants.LAUNCH_ID);
	}

	/**
	 * Search and launch binary.
	 * 
	 * @param elements
	 *            Binaries to search.
	 * @param mode
	 *            Launch mode.
	 */
	private void searchAndLaunch(final Object[] elements, String mode) {
		if (elements != null && elements.length > 0) {
			IBinary bin = null;
			if (elements.length == 1 && elements[0] instanceof IBinary) {
				bin = (IBinary) elements[0];
			} else {
				final List<IBinary> results = new ArrayList<>();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
						getActiveWorkbenchShell());
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor pm)
							throws InterruptedException {
						int nElements = elements.length;
						pm.beginTask(
								Messages.LaunchShortcut_Looking_for_executables,
								nElements);
						try {
							IProgressMonitor sub = new SubProgressMonitor(pm, 1);
							for (int i = 0; i < nElements; i++) {
								if (elements[i] instanceof IAdaptable) {
									IResource r = (IResource) ((IAdaptable) elements[i])
											.getAdapter(IResource.class);
									if (r != null) {
										ICProject cproject = CoreModel
												.getDefault().create(
														r.getProject());
										if (cproject != null) {
											try {
												IBinary[] bins = cproject
														.getBinaryContainer()
														.getBinaries();

												for (IBinary bin : bins) {
													if (bin.isExecutable()) {
														results.add(bin);
													}
												}
											} catch (CModelException e) {
												// TODO should this be simply
												// ignored ?
											}
										}
									}
								}
								if (pm.isCanceled()) {
									throw new InterruptedException();
								}
								sub.done();
							}
						} finally {
							pm.done();
						}
					}
				};
				try {
					dialog.run(true, true, runnable);
				} catch (InterruptedException e) {
					return;
				} catch (InvocationTargetException e) {
					handleFail(e.getMessage());
					return;
				}
				int count = results.size();
				if (count == 0) {
					handleFail(Messages.LaunchShortcut_Binary_not_found);
				} else if (count > 1) {
					bin = chooseBinary(results, mode);
				} else {
					bin = results.get(0);
				}
			}
			if (bin != null) {
				launch(bin, mode);
			}
		} else {
			handleFail(Messages.LaunchShortcut_no_project_selected);
		}
	}

	/**
	 * Prompts the user to select a binary
	 * 
	 * @param binList
	 *            The list of binaries.
	 * @param mode
	 *            launch mode.
	 *
	 * @return the selected binary or <code>null</code> if none.
	 */
	protected IBinary chooseBinary(List<IBinary> binList, String mode) {
		ILabelProvider programLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					return ((IBinary) element).getPath().lastSegment();
				}
				return super.getText(element);
			}
		};

		ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary) element;
					StringBuilder name = new StringBuilder();
					name.append(bin.getCPU()
							+ (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(
				getActiveWorkbenchShell(), programLabelProvider,
				qualifierLabelProvider);
		dialog.setElements(binList.toArray());
		dialog.setTitle(Messages.LaunchShortcut_Launcher);
		dialog.setMessage(Messages.LaunchShortcut_Choose_a_local_application);
		dialog.setUpperListLabel(Messages.LaunchShortcut_Binaries);
		dialog.setLowerListLabel(Messages.LaunchShortcut_Qualifier);
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (IBinary) dialog.getFirstResult();
		}

		return null;
	}

	protected void handleFail(String message) {
		MessageDialog.openError(getActiveWorkbenchShell(),
				Messages.LaunchShortcut_Launcher, message);
	}

	/**
	 * Locate a configuration to launch for the given type. If one cannot be
	 * found, create one.
	 * 
	 * @param bin
	 *            The binary to look launch for.
	 * @param mode
	 *            Launch mode.
	 *
	 * @return A re-useable config or <code>null</code> if none.
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin,
			String mode) {
		ILaunchConfiguration configuration = null;
		ILaunchConfigurationType configType = getLaunchConfigType();
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList<>(configs.length);
			for (ILaunchConfiguration config : configs) {
				IPath programPath = CDebugUtils.getProgramPath(config);
				String projectName = CDebugUtils.getProjectName(config);
				IPath binPath = bin.getResource().getProjectRelativePath();
				if (programPath != null && programPath.equals(binPath)) {
					if (projectName != null
							&& projectName.equals(bin.getCProject()
									.getProject().getName())) {
						candidateConfigs.add(config);
					}
				}
			}
		} catch (CoreException e) {
			DockerLaunchUIPlugin.log(e);
		}

		// If there are no existing configurations associated with the IBinary,
		// create one. If there is exactly one configuration associated with the
		// IBinary, return it. Otherwise, if there is more than one
		// configuration associated with the IBinary, prompt the user to choose
		// one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			configuration = createConfiguration(bin, mode, true);
		} else if (candidateCount == 1) {
			configuration = candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a configuration. A null result means
			// the user
			// cancelled the dialog, in which case this method returns null,
			// since canceling the dialog should also cancel launching
			// anything.
			configuration = chooseConfiguration(candidateConfigs, mode);
		}
		return configuration;
	}

	/**
	 * Create a launch configuration based on a binary, and optionally save it
	 * to the underlying resource.
	 *
	 * @param bin
	 *            a representation of a binary
	 * @param save
	 *            true if the configuration should be saved to the underlying
	 *            resource, and false if it should not be saved.
	 * @return a launch configuration generated for the binary.
	 */
	protected ILaunchConfiguration createConfiguration(IBinary bin,
			String mode, boolean save) {
		ILaunchConfiguration config = null;
		try {
			String binaryPath = bin.getResource().getProjectRelativePath()
					.toString();

			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName(
							bin.getElementName()));

			// DSF settings...use GdbUIPlugin preference store for defaults
			IPreferenceStore preferenceStore = GdbUIPlugin.getDefault()
					.getPreferenceStore();
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					preferenceStore.getString(
							IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND));
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
					preferenceStore.getString(
							IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT));
			wc.setAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
					preferenceStore.getBoolean(
							IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP));
			wc.setAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
					IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
			wc.setAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
			wc.setAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_DEBUG_ON_FORK,
					IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_ON_FORK_DEFAULT);
			wc.setAttribute(
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
					binaryPath);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					bin.getCProject().getElementName());
			wc.setMappedResources(new IResource[] { bin.getResource(),
					bin.getResource().getProject() });
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);

			Preferences prefs = InstanceScope.INSTANCE
					.getNode(DockerLaunchUIPlugin.PLUGIN_ID);

			// get the connection from the ConnectionListener which waits for
			// any activity
			// from the DockerExplorerView
			IDockerConnection connection = ConnectionListener.getInstance()
					.getCurrentConnection();
			if (connection == null) {
				IDockerConnection[] connections = DockerConnectionManager
						.getInstance().getConnections();
				if (connections != null && connections.length > 0)
					connection = DockerConnectionManager.getInstance()
							.getConnections()[0];
			}

			// issue error message if no connections exist
			if (connection == null) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog.openError(
								Display.getCurrent().getActiveShell(),
								Messages.LaunchShortcut_Error_Launching,
								Messages.LaunchShortcut_No_Connections);
					}

				});
				return null;
			}

			wc.setAttribute(ILaunchConstants.ATTR_CONNECTION_URI,
					connection.getUri());

			// get any default image if specified, otherwise use first
			// image in image list for connection
			String image = prefs.get(PreferenceConstants.DEFAULT_IMAGE, null);
			if (image == null) {
				List<IDockerImage> images = connection.getImages();
				if (images != null && images.size() > 0)
					image = images.get(0).repoTags().get(0);
			}

			// issue error msg if no images exist
			if (image == null) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog.openError(
								Display.getCurrent().getActiveShell(),
								Messages.LaunchShortcut_Error_Launching,
								Messages.LaunchShortcut_No_Images);
					}

				});
				return null;
			}

			wc.setAttribute(ILaunchConstants.ATTR_IMAGE, (String) image);

			Boolean keepPref = prefs.getBoolean(
					PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH, false);
			wc.setAttribute(ILaunchConstants.ATTR_KEEP_AFTER_LAUNCH, keepPref);

			// For Debug mode we need to set gdbserver info as well
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				wc.setAttribute(ILaunchConstants.ATTR_GDBSERVER_COMMAND,
						"gdbserver"); //$NON-NLS-1$
				wc.setAttribute(ILaunchConstants.ATTR_GDBSERVER_PORT, "2345"); //$NON-NLS-1$
			}

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

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * specified launch configurations.
	 * 
	 * @param configList
	 *            The list of launch configurations to choose from.
	 * @param mode
	 *            Currently unused.
	 * @return The chosen config, or <code>null</code> if the user cancelled the
	 *         dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(
			List<ILaunchConfiguration> configList, String mode) {
		IDebugModelPresentation labelProvider = DebugUITools
				.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getActiveWorkbenchShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(Messages.LaunchShortcut_Launch_Configuration_Selection);
		dialog.setMessage(Messages.LaunchShortcut_Choose_a_launch_configuration);
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == IStatus.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	protected Shell getActiveWorkbenchShell() {
		return DockerLaunchUIPlugin.getActiveWorkbenchShell();
	}

}
