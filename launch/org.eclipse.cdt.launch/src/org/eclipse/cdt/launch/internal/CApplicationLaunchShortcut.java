package org.eclipse.cdt.launch.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.internal.ui.CElementLabelProvider;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 */
public class CApplicationLaunchShortcut implements ILaunchShortcut {

	/**
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(IEditorPart, String)
	 */
	public void launch(IEditorPart editor, String mode) {
		searchAndLaunch(new Object[] { editor.getEditorInput()}, mode);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).toArray(), mode);
		}
	}

	public void launch(IBinary bin, String mode) {
		try {
			ILaunchConfiguration config = findLaunchConfiguration(bin, mode);
			if (config != null) {
				DebugUITools.saveAndBuildBeforeLaunch();
				config.launch(mode, null);
			}
		} catch (CoreException e) {
			LaunchUIPlugin.errorDialog("Launch failed", e.getStatus()); //$NON-NLS-1$
		}
	}

	/**
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 * 
	 * @return a re-useable config or <code>null</code> if none
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
		ILaunchConfiguration configuration = null;
		ILaunchConfigurationType configType = getCLaunchConfigType();
		List candidateConfigs = Collections.EMPTY_LIST;
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				String programName = AbstractCLaunchDelegate.getProgramName(config);
				String projectName = AbstractCLaunchDelegate.getProjectName(config);
				String name = bin.getResource().getProjectRelativePath().toString();
				if (projectName != null && programName.equals(name)) {
					if (projectName != null && projectName.equals(bin.getCProject().getProject().getName())) {
						candidateConfigs.add(config);
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
			// FIXME:  should probably have more filtering here base on
			// the mode, arch, CPU.  For now we only support native.
			// Prompt the user if more then 1 debugger.
			ICDebugConfiguration debugConfig = null;
			ICDebugConfiguration[] debugConfigs = CDebugCorePlugin.getDefault().getDebugConfigurations();
			List debugList = new ArrayList(debugConfigs.length);
			for (int i = 0; i < debugConfigs.length; i++) {
				String platform = debugConfigs[i].getPlatform();
				if (platform == null || platform.equals("native")) {
					debugList.add(debugConfigs[i]);
				}
			}
			debugConfigs = (ICDebugConfiguration[]) debugList.toArray(new ICDebugConfiguration[0]);
			if (debugConfigs.length == 1) {
				debugConfig = debugConfigs[0];
			} else if (debugConfigs.length > 1) {
				debugConfig = chooseDebugConfig(debugConfigs, mode);
			}
			if (debugConfig != null) {
				configuration = createConfiguration(bin, debugConfig);
			}
		} else if (candidateCount == 1) {
			configuration = (ILaunchConfiguration) candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config.  A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since cancelling the dialog should also cancel launching anything.
			configuration = chooseConfiguration(candidateConfigs, mode);
		}
		return configuration;
	}

	/**
	 * Method createConfiguration.
	 * @param bin
	 * @return ILaunchConfiguration
	 */
	private ILaunchConfiguration createConfiguration(IBinary bin, ICDebugConfiguration debugConfig) {
		ILaunchConfiguration config = null;
		try {
			String projectName = bin.getResource().getProjectRelativePath().toString();
			ILaunchConfigurationType configType = getCLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc =
				configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(bin.getElementName()));
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
			wc.setAttribute(IDebugUIConstants.ATTR_TARGET_DEBUG_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_DEFAULT);
			wc.setAttribute(IDebugUIConstants.ATTR_TARGET_RUN_PERSPECTIVE, IDebugUIConstants.PERSPECTIVE_DEFAULT);
			wc.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
			wc.setAttribute(
				ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
				ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, debugConfig.getID());
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
		return CDebugUIPlugin.getActiveWorkbenchShell();
	}

	/**
	 * Method chooseDebugConfig.
	 * @param debugConfigs
	 * @param mode
	 * @return ICDebugConfiguration
	 */
	private ICDebugConfiguration chooseDebugConfig(ICDebugConfiguration[] debugConfigs, String mode) {
		ILabelProvider provider = new LabelProvider() {
			/**
			 * The <code>LabelProvider</code> implementation of this 
			 * <code>ILabelProvider</code> method returns the element's <code>toString</code>
			 * string. Subclasses may override.
			 */
			public String getText(Object element) {
				if (element == null) {
					return "";
				} else if (element instanceof ICDebugConfiguration) {
					return ((ICDebugConfiguration) element).getName();
				}
				return element.toString();
			}
		};
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), provider);
		dialog.setElements(debugConfigs);
		dialog.setTitle("Launch Debug Configuration Selection"); //$NON-NLS-1$
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage("Choose a debug configuration to debug"); //$NON-NLS-1$
		} else {
			dialog.setMessage("Choose a configuration to run"); //$NON-NLS-1$
		}
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		provider.dispose();
		if (result == ElementListSelectionDialog.OK) {
			return (ICDebugConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the specified
	 * launch configurations.  Return the chosen config, or <code>null</code> if the
	 * user cancelled the dialog.
	 */
	protected ILaunchConfiguration chooseConfiguration(List configList, String mode) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle("Launch Configuration Selection"); //$NON-NLS-1$
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage("Choose a launch configuration to debug"); //$NON-NLS-1$
		} else {
			dialog.setMessage("Choose a launch configuration to run"); //$NON-NLS-1$
		}
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == ElementListSelectionDialog.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Prompts the user to select a  binary
	 * 
	 * @return the selected binary or <code>null</code> if none.
	 */
	protected IBinary chooseBinary(List binList, String mode) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new CElementLabelProvider());
		dialog.setElements(binList.toArray());
		dialog.setTitle("C ApplicationAction"); //$NON-NLS-1$
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage("Choose an application to debug"); //$NON-NLS-1$
		} else {
			dialog.setMessage("Choose an application to run"); //$NON-NLS-1$
		}
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			return (IBinary) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Method searchAndLaunch.
	 * @param objects
	 * @param mode
	 */
	private void searchAndLaunch(final Object[] elements, String mode) {
		final List results = new ArrayList();
		if (elements != null && elements.length > 0) {
			try {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor pm) throws InterruptedException {
						int nElements = elements.length;
						pm.beginTask("Looking for executables", nElements); //$NON-NLS-1$
						try {
							IProgressMonitor sub = new SubProgressMonitor(pm, 1);
							for (int i = 0; i < nElements; i++) {
								if (elements[i] instanceof IAdaptable) {
									IResource r = (IResource) ((IAdaptable) elements[i]).getAdapter(IResource.class);
									ICProject cproject = CoreModel.getDefault().create(r.getProject());
									if (cproject != null) {
										IBinary[] bins = cproject.getBinaryContainer().getBinaries();

										for (int j = 0; j < bins.length; j++) {
											if (bins[j].isExecutable()) {
												results.add(bins[j]);
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
				dialog.run(true, true, runnable);
			} catch (InterruptedException e) {
				return;
			} catch (InvocationTargetException e) {
				MessageDialog.openError(getShell(), "C Application Launcher", e.getMessage());
				return;
			}
			if (results.size() == 0) {
				MessageDialog.openError(getShell(), "C Application Launcher", "Launch failed no binaries");
			} else {
				IBinary bin = chooseBinary(results, mode);
				if (bin != null) {
					launch(bin, mode);
				}
			}
		} else {
			MessageDialog.openError(getShell(), "C Application Launcher", "Launch failed no project selected");
		}
	}

}