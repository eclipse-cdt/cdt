package org.eclipse.cdt.launch.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

/**
 * Group Launch delegate. Launches each configuration in the user selected mode
 */
public class MultiLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
	public static final String DEFAULT_MODE = "default";  //$NON-NLS-1$
	private static final String NAME_PROP = "name"; //$NON-NLS-1$
	private static final String ENABLED_PROP = "enabled"; //$NON-NLS-1$
	private static final String MODE_PROP = "mode"; //$NON-NLS-1$
	private static final String ACTION_PROP = "action"; //$NON-NLS-1$
	public static String MULTI_LAUNCH_CONSTANTS_PREFIX = "org.eclipse.cdt.launch.launchGroup"; //$NON-NLS-1$
	
	public static class LaunchElement {
		public int index;
		public boolean enabled;
		public String mode;
		public String action;
		public String name;
		public ILaunchConfiguration data;
	}

	public MultiLaunchConfigurationDelegate() {
		// nothing
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
	        throws CoreException {
		// have to unset "remove terminated launches when new one created" 
		// because it does not work good for multilaunch
		boolean dstore = DebugUIPlugin.getDefault().getPreferenceStore()
		        .getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES);
		ArrayList<LaunchElement> input = createLaunchElements(configuration, new ArrayList<LaunchElement>());
		try {
			
			monitor.beginTask(LaunchMessages.getString("MultiLaunchConfigurationDelegate.0") + configuration.getName(), 1000);  //$NON-NLS-1$
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, false);
			for (Iterator<LaunchElement> iterator = input.iterator(); iterator.hasNext();) {
				LaunchElement le = iterator.next();
				final ILaunchConfiguration conf = findLaunch(le.name);
				if (le.enabled==false) continue;
				if (conf==null) continue;
				final String localMode;
				if (le.mode != null && !le.mode.equals(DEFAULT_MODE)) {
					localMode = le.mode;
				} else {
					localMode = mode;
				}
				ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(conf, localMode);
				if (launchGroup==null) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), LaunchMessages.getString("LaunchUIPlugin.Error"),  //$NON-NLS-1$ 
									LaunchMessages.getFormattedString("MultiLaunchConfigurationDelegate.Cannot", //$NON-NLS-1$ 
											new String[]{conf.toString(), localMode}) 
							        ); 
						}
					});
				
					continue;
				}
				try {
					if (configuration.getName().equals(conf.getName())) throw new StackOverflowError();
					ILaunch launch2 = DebugUIPlugin.buildAndLaunch(conf, localMode, new SubProgressMonitor(monitor,
							1000 / input.size()));
					IProcess[] processes = launch2.getProcesses();
					for (int i = 0; i < processes.length; i++) {
						IProcess process = processes[i];
						launch.addProcess(process);
					}
				} catch (StackOverflowError e) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									LaunchMessages.getString("LaunchUIPlugin.Error"), //$NON-NLS-1$ 
									LaunchMessages.getFormattedString("MultiLaunchConfigurationDelegate.Loop", //$NON-NLS-1$ 
											conf.toString()));
						}
					});
				}
			}
			if (!launch.hasChildren()) {
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				launchManager.removeLaunch(launch);
			}

		} finally {
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, dstore);
			monitor.done();
		}
	}

	protected void buildProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException {
		// do nothing, project can be rebuild for each launch individually
		
	}

	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// not build for this one
		return false;
	}

	protected static ILaunchConfiguration findLaunch(String name) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations();
		for (int i = 0; i < launchConfigurations.length; i++) {
			ILaunchConfiguration lConf = launchConfigurations[i];
			if (lConf.getName().equals(name))
				return lConf;
		}
		return null;
	}

	public static ArrayList<LaunchElement> createLaunchElements(ILaunchConfiguration configuration, ArrayList<MultiLaunchConfigurationDelegate.LaunchElement> input) {
		try {
			Map attrs = configuration.getAttributes();
			for (Iterator iterator = attrs.keySet().iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				try {
					if (attr.startsWith(MultiLaunchConfigurationDelegate.MULTI_LAUNCH_CONSTANTS_PREFIX)) {
						String prop = attr
						        .substring(MultiLaunchConfigurationDelegate.MULTI_LAUNCH_CONSTANTS_PREFIX.length() + 1);
						int k = prop.indexOf('.');
						String num = prop.substring(0, k);
						int index = Integer.parseInt(num);
						String name = prop.substring(k + 1);
						if (name.equals(NAME_PROP)) {
							MultiLaunchConfigurationDelegate.LaunchElement el = new MultiLaunchConfigurationDelegate.LaunchElement();
							el.index = index;
							el.name = (String) attrs.get(attr);
							el.action = (String) attrs.get(getProp(index, ACTION_PROP));
							el.mode = (String) attrs.get(getProp(index, MODE_PROP));
							el.enabled = "true".equals(attrs.get(getProp(index, ENABLED_PROP))); //$NON-NLS-1$
							try {
								el.data = findLaunch(el.name);
							} catch (Exception e) {
								el.data = null;
							}
							while (index >= input.size()) {
								input.add(null);
							}
							input.set(index, el);

						}
					}
				} catch (Exception e) {
					LaunchUIPlugin.log(e);
				}
			}
		} catch (CoreException e) {
			LaunchUIPlugin.log(e);
		}
		return input;
	}

	public static void storeLaunchElements(ILaunchConfigurationWorkingCopy configuration,
			ArrayList<LaunchElement> input) {
		int i = 0;
		removeLaunchElements(configuration);
		for (Iterator<LaunchElement> iterator = input.iterator(); iterator.hasNext();) {
			MultiLaunchConfigurationDelegate.LaunchElement el = iterator.next();
			if (el == null) continue;
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, NAME_PROP), el.name);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ACTION_PROP), el.action);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, MODE_PROP), el.mode);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ENABLED_PROP), el.enabled + ""); //$NON-NLS-1$
			i++;
		}
	}

	public static void removeLaunchElements(ILaunchConfigurationWorkingCopy configuration) {
		try {
			Map attrs = configuration.getAttributes();
			for (Iterator iterator = attrs.keySet().iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				try {
					if (attr.startsWith(MultiLaunchConfigurationDelegate.MULTI_LAUNCH_CONSTANTS_PREFIX)) {
						configuration.removeAttribute(attr);
					}
				} catch (Exception e) {
					LaunchUIPlugin.log(e);
				}
			}
		} catch (CoreException e) {
			LaunchUIPlugin.log(e);
		}
	}

	public static String getProp(int index, String string) {
		return MultiLaunchConfigurationDelegate.MULTI_LAUNCH_CONSTANTS_PREFIX + "." + index + "." + string; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
