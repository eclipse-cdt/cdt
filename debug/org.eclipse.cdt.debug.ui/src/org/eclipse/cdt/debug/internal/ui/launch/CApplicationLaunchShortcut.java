/*******************************************************************************
 * Copyright (c) 2005, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - bugs 178731, 246201
 *     John Dallaway - Match launch configuration type (bug 545941)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public class CApplicationLaunchShortcut implements ILaunchShortcut2 {
	private final static String CONNECTION_URI = "org.eclipse.cdt.docker.launcher.connection_uri"; //$NON-NLS-1$

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
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 *
	 * @return a re-usable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
		ILaunchConfiguration configuration = null;
		List<ILaunchConfiguration> candidateConfigs = Collections.emptyList();
		try {
			ILaunchConfigurationType type = getCLaunchConfigType();
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
			candidateConfigs = new ArrayList<>(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				IPath programPath = CDebugUtils.getProgramPath(config);
				String projectName = CDebugUtils.getProjectName(config);
				IPath name = bin.getResource().getProjectRelativePath();
				// don't match any launch config that is used for a Container launch
				String connectionURI = config.getAttribute(CONNECTION_URI, (String) null);
				if (connectionURI == null) {
					if (programPath != null && programPath.equals(name)) {
						if (projectName != null && projectName.equals(bin.getCProject().getProject().getName())) {
							candidateConfigs.add(config);
						}
					}
				}
			}
		} catch (CoreException e) {
			CDebugUIPlugin.log(e);
		}

		// If there are no existing configs associated with the IBinary, create one.
		// If there is exactly one config associated with the IBinary, return it.
		// Otherwise, if there is more than one config associated with the IBinary, prompt the
		// user to choose one.
		int candidateCount = candidateConfigs.size();
		if (candidateCount < 1) {
			configuration = createConfiguration(bin, mode);
		} else if (candidateCount == 1) {
			configuration = candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config.  A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since canceling the dialog should also cancel launching anything.
			configuration = chooseConfiguration(candidateConfigs, mode);
		}
		return configuration;
	}

	/**
	 * Method createConfiguration.
	 * @param bin
	 * @return ILaunchConfiguration
	 */
	private ILaunchConfiguration createConfiguration(IBinary bin, String mode) {
		ILaunchConfiguration config = null;
		try {
			String projectName = bin.getResource().getProjectRelativePath().toString();
			ILaunchConfigurationType configType = getCLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager().generateLaunchConfigurationName(bin.getElementName()));
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
			wc.setMappedResources(new IResource[] { bin.getResource().getProject() });
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);

			ICProjectDescription projDes = CCorePlugin.getDefault()
					.getProjectDescription(bin.getCProject().getProject());
			if (projDes != null) {
				String buildConfigID = projDes.getActiveConfiguration().getId();
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, buildConfigID);
			}

			config = wc.doSave();
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}
		return config;
	}

	/**
	 * Method getCLaunchConfigType.
	 * @return ILaunchConfigurationType
	 */
	protected ILaunchConfigurationType getCLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Convenience method to get the window that owns this action's Shell.
	 */
	protected Shell getShell() {
		IWorkbenchWindow w = CDebugUIPlugin.getActiveWorkbenchWindow();
		if (w != null) {
			return w.getShell();
		}
		return null;
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the specified
	 * launch configurations.  Return the chosen config, or <code>null</code> if the
	 * user cancelled the dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList, String mode) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(getLaunchSelectionDialogTitleString(configList, mode));
		dialog.setMessage(getLaunchSelectionDialogMessageString(configList, mode));
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	protected String getLaunchSelectionDialogTitleString(List<ILaunchConfiguration> configList, String mode) {
		return LaunchMessages.getString("CApplicationLaunchShortcut.LaunchConfigSelection"); //$NON-NLS-1$
	}

	protected String getLaunchSelectionDialogMessageString(List<ILaunchConfiguration> binList, String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLaunchConfigToDebug"); //$NON-NLS-1$
		} else if (mode.equals(ILaunchManager.RUN_MODE)) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLaunchConfigToRun"); //$NON-NLS-1$
		}
		return LaunchMessages.getString("CApplicationLaunchShortcut.Invalid_launch_mode_2"); //$NON-NLS-1$
	}

	/**
	 * Prompts the user to select a  binary
	 *
	 * @return the selected binary or <code>null</code> if none.
	 */
	protected IBinary chooseBinary(List<IBinary> binList, String mode) {
		ILabelProvider programLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary) element;
					StringBuilder name = new StringBuilder();
					name.append(bin.getPath().lastSegment());
					return name.toString();
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
					name.append(bin.getCPU()).append(bin.isLittleEndian() ? "le" : "be"); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(getShell(), programLabelProvider,
				qualifierLabelProvider);
		dialog.setElements(binList.toArray());
		dialog.setTitle(getBinarySelectionDialogTitleString(binList, mode));
		dialog.setMessage(getBinarySelectionDialogMessageString(binList, mode));
		dialog.setUpperListLabel(LaunchMessages.getString("Launch.common.BinariesColon")); //$NON-NLS-1$
		dialog.setLowerListLabel(LaunchMessages.getString("Launch.common.QualifierColon")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (IBinary) dialog.getFirstResult();
		}

		return null;
	}

	protected String getBinarySelectionDialogTitleString(List<IBinary> binList, String mode) {
		return LaunchMessages.getString("CApplicationLaunchShortcut.CLocalApplication"); //$NON-NLS-1$
	}

	protected String getBinarySelectionDialogMessageString(List<IBinary> binList, String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLocalAppToDebug"); //$NON-NLS-1$
		} else if (mode.equals(ILaunchManager.RUN_MODE)) {
			return LaunchMessages.getString("CApplicationLaunchShortcut.ChooseLocalAppToRun"); //$NON-NLS-1$
		}
		return LaunchMessages.getString("CApplicationLaunchShortcut.Invalid_launch_mode_3"); //$NON-NLS-1$
	}

	/**
	 * Method searchAndLaunch.
	 * @param objects
	 * @param mode
	 */
	private void searchAndLaunch(final Object[] elements, String mode) {
		if (elements != null && elements.length > 0) {
			IBinary bin = null;
			if (elements.length == 1 && elements[0] instanceof IBinary) {
				bin = (IBinary) elements[0];
			} else {
				final List<IBinary> results = new ArrayList<>();
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor pm) throws InterruptedException {
						int nElements = elements.length;
						pm.beginTask("Looking for executables", nElements); //$NON-NLS-1$
						try {
							IProgressMonitor sub = new SubProgressMonitor(pm, 1);
							for (int i = 0; i < nElements; i++) {
								if (elements[i] instanceof IAdaptable) {
									IResource r = ((IAdaptable) elements[i]).getAdapter(IResource.class);
									if (r != null) {
										ICProject cproject = CoreModel.getDefault().create(r.getProject());
										if (cproject != null) {
											try {
												IBinary[] bins = cproject.getBinaryContainer().getBinaries();

												for (int j = 0; j < bins.length; j++) {
													if (bins[j].isExecutable()) {
														results.add(bins[j]);
													}
												}
											} catch (CModelException e) {
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
					MessageDialog.openError(getShell(),
							LaunchMessages.getString("CApplicationLaunchShortcut.Application_Launcher"), //$NON-NLS-1$
							e.getMessage());
					return;
				}
				int count = results.size();
				if (count == 0) {
					MessageDialog.openError(getShell(),
							LaunchMessages.getString("CApplicationLaunchShortcut.Application_Launcher"), //$NON-NLS-1$
							LaunchMessages.getString("CApplicationLaunchShortcut.Launch_failed_no_binaries")); //$NON-NLS-1$
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
			MessageDialog.openError(getShell(),
					LaunchMessages.getString("CApplicationLaunchShortcut.Application_Launcher"), //$NON-NLS-1$
					LaunchMessages.getString("CApplicationLaunchShortcut.Launch_failed_no_project_selected")); //$NON-NLS-1$
		}
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		// This returns null so the platform will use the ILaunchShortcut behavior
		// and get the configurations based on the project resource association.
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		// This returns null so the platform will use the ILaunchShortcut behavior
		// and get the configurations based on the project resource association.
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		// Take the selection and determine which project is intended to
		// be used for the launch.
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			if (firstElement != null) {
				if (firstElement instanceof IFile) {
					IFile file = (IFile) firstElement;
					return file.getProject();
				}
				if (firstElement instanceof Executable) {
					return ((Executable) firstElement).getProject();
				}
				if (firstElement instanceof IBinary) {
					return ((IBinary) firstElement).getResource().getProject();
				}
			}
		}
		List<IProject> projects = getProjectsFromSelection(selection);
		if (projects.size() > 0) {
			return projects.get(0);
		}
		return null;
	}

	/**
	 * Gets the owning project(s) of the selected object(s) if any
	 * @param selection the current selection
	 * @return a list of projects - may be empty
	 */
	public static List<IProject> getProjectsFromSelection(ISelection selection) {
		List<IProject> projects = new ArrayList<>();

		if (selection != null && !selection.isEmpty()) {
			if (selection instanceof ITextSelection) {

				IWorkbenchWindow activeWindow = CDebugUIPlugin.getActiveWorkbenchWindow();
				IWorkbenchPage wpage = activeWindow.getActivePage();
				if (wpage != null) {
					IEditorPart ep = wpage.getActiveEditor();
					if (ep != null) {
						IEditorInput editorInput = ep.getEditorInput();
						if (editorInput instanceof IFileEditorInput) {
							IFile file = ((IFileEditorInput) editorInput).getFile();
							if (file != null) {
								projects.add(file.getProject());
							}
						}
					}
				}
			} else if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;

				for (Iterator<?> iter = structuredSelection.iterator(); iter.hasNext();) {
					Object element = iter.next();
					if (element != null) {
						if (element instanceof ICProject) {
							projects.add(((ICProject) element).getProject());
						} else if (element instanceof IResource) {
							projects.add(((IResource) element).getProject());
						} else if (element instanceof ICElement) {
							ICElement unit = (ICElement) element;

							// Get parent of the Element until we reach the owner project.
							while (unit != null && !(unit instanceof ICProject))
								unit = unit.getParent();

							if (unit != null) {
								projects.add(((ICProject) unit).getProject());
							}
						} else if (element instanceof IAdaptable) {
							Object adapter = ((IAdaptable) element).getAdapter(IResource.class);
							if (adapter != null && adapter instanceof IResource) {
								projects.add(((IResource) adapter).getProject());
							} else {
								adapter = ((IAdaptable) element).getAdapter(ICProject.class);
								if (adapter != null && adapter instanceof ICProject) {
									projects.add(((ICProject) adapter).getProject());
								}
							}
						}
					}
				}
			}
		}
		return projects;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		// This handles the case where the selection is text in an editor.
		IEditorInput editorInput = editorpart.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editorInput).getFile();
			if (file != null) {
				return file.getProject();
			}
		}
		return null;
	}
}
