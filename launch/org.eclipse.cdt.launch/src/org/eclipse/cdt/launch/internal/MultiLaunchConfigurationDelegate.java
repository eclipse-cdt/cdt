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
import org.eclipse.debug.core.ILaunchListener;
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
public class MultiLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {
	public static final String DEFAULT_MODE = "default"; //$NON-NLS-1$
	private static final String NAME_PROP = "name"; //$NON-NLS-1$
	private static final String ENABLED_PROP = "enabled"; //$NON-NLS-1$
	private static final String MODE_PROP = "mode"; //$NON-NLS-1$
	private static final String ACTION_PROP = "action"; //$NON-NLS-1$
	public static String MULTI_LAUNCH_CONSTANTS_PREFIX = "org.eclipse.cdt.launch.launchGroup"; //$NON-NLS-1$

	public static class LaunchElement {
		public static final String POST_LAUNCH_WAIT_FOR_TERM = "wait";
		public static final String POST_LAUNCH_CONTINUE = "";
		public static final String POST_LAUNCH_DELAY_3_SEC = "delay 3s";
		public static final String POST_LAUNCH_DELAY_PREFIX = "delay";
		private int index;
		private boolean enabled;
		private String mode;
		private String action;
		private String name;
		private ILaunchConfiguration data;
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setAction(String action) {
			this.action = action;
		}
		public String getAction() {
			return action;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public String getMode() {
			return mode;
		}
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		public boolean isEnabled() {
			return enabled;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public int getIndex() {
			return index;
		}
		public void setData(ILaunchConfiguration data) {
			this.data = data;
		}
		public ILaunchConfiguration getData() {
			return data;
		}
	}

	public MultiLaunchConfigurationDelegate() {
		// nothing
	}

	/**
	 * Listener for launch changes to add processes, also removes itslef when parent launch is removed
	 *
	 */
	private class MultiLaunchListener implements ILaunchListener {
		private ILaunch launch;
		private ArrayList<LaunchElement> input;

		/**
		 * @param launch - parent launch
		 * @param input - list of launch elements (children of group launch)
		 */
		public MultiLaunchListener(ILaunch launch, ArrayList<LaunchElement> input) {
			this.launch = launch;
			this.input = input;
		}

		public void launchChanged(ILaunch launch2) {
			if (launch == launch2) return;
			// add/remove processes
			if (isChild(launch2, input)) {
				IProcess[] processes = launch2.getProcesses();
				for (int i = 0; i < processes.length; i++) {
					IProcess process = processes[i];
					launch.addProcess(process);
				}
			}

		}

		private boolean isChild(ILaunch launch2, ArrayList<LaunchElement> input) {
			for (Iterator<LaunchElement> iterator = input.iterator(); iterator.hasNext();) {
				LaunchElement le = iterator.next();
				if (le.getName().equals(launch2.getLaunchConfiguration().getName())) { return true; }
			}
			return false;
		}

		public void launchRemoved(ILaunch launch2) {
			if (launch == launch2) {
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				launchManager.removeLaunchListener(this);
			}
		}

		public void launchAdded(ILaunch launch2) {
			// ignore

		}
	}

	public void launch(ILaunchConfiguration configuration, String mode, final ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		// have to unset "remove terminated launches when new one created" 
		// because it does not work good for multilaunch

		boolean dstore = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(
				IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES);

		try {
			monitor.beginTask(
					LaunchMessages.getString("MultiLaunchConfigurationDelegate.0") + configuration.getName(), 1000); //$NON-NLS-1$
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES,
					false);
			
			final ArrayList<LaunchElement> input = createLaunchElements(configuration, new ArrayList<LaunchElement>());
			ILaunchListener listener = new MultiLaunchListener(launch, input);
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			launchManager.addLaunchListener(listener); // listener removed when launch is removed

			for (Iterator<LaunchElement> iterator = input.iterator(); iterator.hasNext();) {
				LaunchElement le = iterator.next();
				if (le.isEnabled() == false) continue;
				// find launch
				final ILaunchConfiguration conf = findLaunch(le.getName());
				// not found, skip (error?)
				if (conf == null) continue;
				// determine mode for each launch
				final String localMode;
				if (le.getMode() != null && !le.getMode().equals(DEFAULT_MODE)) {
					localMode = le.getMode();
				} else {
					localMode = mode;
				}
				ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(conf, localMode);
				if (launchGroup == null) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
									LaunchMessages.getString("LaunchUIPlugin.Error"), //$NON-NLS-1$ 
									LaunchMessages.getFormattedString("MultiLaunchConfigurationDelegate.Cannot", //$NON-NLS-1$ 
											new String[] { conf.toString(), localMode }));
						}
					});

					continue;
				}
				try {
					if (configuration.getName().equals(conf.getName())) throw new StackOverflowError();
					// LAUNCH child here
					ILaunch subLaunch = DebugUIPlugin.buildAndLaunch(conf, localMode, new SubProgressMonitor(monitor, 1000 / input.size()));
					postLaunchAction(subLaunch, le.getAction(), monitor);

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
				launchManager.removeLaunch(launch);
			}
		} finally {
			DebugUIPlugin.getDefault().getPreferenceStore().setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES,
					dstore);
			monitor.done();
		}
	}

	private void postLaunchAction(ILaunch subLaunch, String action, IProgressMonitor monitor) {
		if (action==null) return;
		if (LaunchElement.POST_LAUNCH_WAIT_FOR_TERM.equals(action)) {
			monitor.subTask("Waiting for termination of "+subLaunch.getLaunchConfiguration().getName());
			while (!subLaunch.isTerminated() && !monitor.isCanceled()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
			monitor.subTask("");
		} else
		if (action.startsWith(LaunchElement.POST_LAUNCH_DELAY_PREFIX)) {
			String num = action.substring(LaunchElement.POST_LAUNCH_DELAY_PREFIX.length()).trim();
			int k = 1000;
			if (num.endsWith("ms")) {
				num = num.substring(0,num.length()-2);
				k = 1;
			} else if (num.endsWith("s")) {
				num = num.substring(0,num.length()-1);
			}
			int parseInt;
			try {
				parseInt = Integer.parseInt(num);
			} catch (NumberFormatException e) {
				parseInt = 3;
				k = 1000;
			}
			try {
				Thread.sleep(parseInt * k);
			} catch (InterruptedException e) {
				// ok
			}
		}
	}

	protected void buildProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException {
		// do nothing, project can be rebuild for each launch individually

	}

	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		// not build for this one
		return false;
	}

	protected static ILaunchConfiguration findLaunch(String name) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations();
		for (int i = 0; i < launchConfigurations.length; i++) {
			ILaunchConfiguration lConf = launchConfigurations[i];
			if (lConf.getName().equals(name)) return lConf;
		}
		return null;
	}

	public static ArrayList<LaunchElement> createLaunchElements(ILaunchConfiguration configuration,
			ArrayList<MultiLaunchConfigurationDelegate.LaunchElement> input) {
		try {
			Map attrs = configuration.getAttributes();
			for (Iterator iterator = attrs.keySet().iterator(); iterator.hasNext();) {
				String attr = (String) iterator.next();
				try {
					if (attr.startsWith(MultiLaunchConfigurationDelegate.MULTI_LAUNCH_CONSTANTS_PREFIX)) {
						String prop = attr.substring(MultiLaunchConfigurationDelegate.MULTI_LAUNCH_CONSTANTS_PREFIX
								.length() + 1);
						int k = prop.indexOf('.');
						String num = prop.substring(0, k);
						int index = Integer.parseInt(num);
						String name = prop.substring(k + 1);
						if (name.equals(NAME_PROP)) {
							MultiLaunchConfigurationDelegate.LaunchElement el = new MultiLaunchConfigurationDelegate.LaunchElement();
							el.setIndex(index);
							el.setName((String) attrs.get(attr));
							el.setAction((String) attrs.get(getProp(index, ACTION_PROP)));
							el.setMode((String) attrs.get(getProp(index, MODE_PROP)));
							el.setEnabled("true".equals(attrs.get(getProp(index, ENABLED_PROP)))); //$NON-NLS-1$
							try {
								el.setData(findLaunch(el.getName()));
							} catch (Exception e) {
								el.setData(null);
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

	public static void storeLaunchElements(ILaunchConfigurationWorkingCopy configuration, ArrayList<LaunchElement> input) {
		int i = 0;
		removeLaunchElements(configuration);
		for (Iterator<LaunchElement> iterator = input.iterator(); iterator.hasNext();) {
			MultiLaunchConfigurationDelegate.LaunchElement el = iterator.next();
			if (el == null) continue;
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, NAME_PROP), el.getName());
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ACTION_PROP), el.getAction());
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, MODE_PROP), el.getMode());
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ENABLED_PROP), el.isEnabled() + ""); //$NON-NLS-1$
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
