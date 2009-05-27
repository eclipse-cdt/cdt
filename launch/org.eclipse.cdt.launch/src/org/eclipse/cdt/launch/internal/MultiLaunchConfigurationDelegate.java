/*******************************************************************************
 *  Copyright (c) 2009 QNX Software Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *      Freescale Semiconductor
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate.LaunchElement.EPostLaunchAction;
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
	private static final String ACTION_PARAM_PROP = "actionParam"; //$NON-NLS-1$
	public static String MULTI_LAUNCH_CONSTANTS_PREFIX = "org.eclipse.cdt.launch.launchGroup"; //$NON-NLS-1$

	public static class LaunchElement {
		public static enum EPostLaunchAction {
			NONE,
			WAIT_FOR_TERMINATION,
			DELAY
		};
		/**
		 * Allows us decouple the enum identifier in the code from its textual representation in the GUI
		 */
		public static String actionEnumToStr(EPostLaunchAction action) {
			switch (action) {
			case NONE:
				return LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.None"); //$NON-NLS-1$
			case WAIT_FOR_TERMINATION:
				return LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.WaitUntilTerminated"); //$NON-NLS-1$
			case DELAY:
				return LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.Delay"); //$NON-NLS-1$
			default:
				assert false : "new post launch action type is missing logic"; //$NON-NLS-1$
				return LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.None"); //$NON-NLS-1$
			}
		}
		/**
		 * Allows us decouple the enum identifier in the code from its textual representation in the GUI
		 */
		public static EPostLaunchAction strToActionEnum(String str) {
			if (str.equals(LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.None"))) { //$NON-NLS-1$
				return EPostLaunchAction.NONE;
			}
			else if (str.equals(LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.WaitUntilTerminated"))) { //$NON-NLS-1$
				return EPostLaunchAction.WAIT_FOR_TERMINATION;
			}
			else if (str.equals(LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.Delay"))) { //$NON-NLS-1$
				return EPostLaunchAction.DELAY;
			}
			else {
				assert false : "new post launch action type is missing logic"; //$NON-NLS-1$
				return EPostLaunchAction.NONE;
			}
		}
		
		private int index;
		private boolean enabled;
		private String mode;
		private EPostLaunchAction action;
		private Object actionParam;
		private String name;
		private ILaunchConfiguration data;
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setAction(EPostLaunchAction action, Object actionParam) {
			this.action = action;
			this.actionParam = actionParam;
		}
		public EPostLaunchAction getAction() {
			return action;
		}
		public Object getActionParam() {
			return actionParam;
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
	 * Listener for launch changes to add processes, also removes itself when parent launch is removed
	 *
	 */
	private class MultiLaunchListener implements ILaunchListener {
		private ILaunch launch;

		// A map of all our sub-launches and the current processes that belong to each one.
		private Map<ILaunch, IProcess[]> subLaunches = new HashMap<ILaunch, IProcess[]>();

		/**
		 * @param launch - parent launch
		 */
		public MultiLaunchListener(ILaunch launch) {
			this.launch = launch;
		}
		
		public void addSubLaunch(ILaunch subLaunch) {
			subLaunches.put(subLaunch, subLaunch.getProcesses());
		}

		public void launchChanged(ILaunch launch2) {
			if (launch == launch2) return;
			// add/remove processes
			if (isChild(launch2, subLaunches)) {
				// Remove old processes
				IProcess[] oldProcesses = subLaunches.get(launch2);
				for (IProcess oldProcess : oldProcesses) {
					launch.removeProcess(oldProcess);
				}
				
				// Add new processes
				IProcess[] newProcesses = launch2.getProcesses();
				for (IProcess newProcess : newProcesses) {
					launch.addProcess(newProcess);
				}
				
				  // Replace the processes of the changed launch
				  subLaunches.put(launch2, newProcesses);
			}
		}

		private boolean isChild(ILaunch launch, Map<ILaunch, IProcess[]> subLaunches) {
			for (ILaunch subLaunch : subLaunches.keySet()) {
				if (subLaunch == launch) { return true; }
			}
			return false;
		}

		public void launchRemoved(ILaunch launch2) {
			if (launch == launch2) {
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				launchManager.removeLaunchListener(this);
			} else if (isChild(launch2, subLaunches)) {
				// Remove old processes
				IProcess[] oldProcesses = subLaunches.get(launch2);
				for (IProcess oldProcess : oldProcesses) {
					launch.removeProcess(oldProcess);
				}
				
				subLaunches.remove(launch2);
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
			
			final List<LaunchElement> input = createLaunchElements(configuration, new ArrayList<LaunchElement>());
			MultiLaunchListener listener = new MultiLaunchListener(launch);
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
					listener.addSubLaunch(subLaunch);
					
					// Now that we added the launch in our list, we have already
					// received the real launchChanged event, and did not know it was part of our list
					// So, fake another event now.
					listener.launchChanged(subLaunch);

					postLaunchAction(subLaunch, le.getAction(), le.getActionParam(), monitor);
					

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

	private void postLaunchAction(ILaunch subLaunch, EPostLaunchAction action, Object actionParam, IProgressMonitor monitor) {
		switch (action) {
		case NONE:
			return;
		case WAIT_FOR_TERMINATION:
			
			monitor.subTask(LaunchMessages.getString("MultiLaunchConfigurationDelegate.Action.WaitingForTermination") + " " + subLaunch.getLaunchConfiguration().getName()); //$NON-NLS-1$ //$NON-NLS-2$
			while (!subLaunch.isTerminated() && !monitor.isCanceled()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
			monitor.subTask(""); //$NON-NLS-1$
			break;
		case DELAY:
			Integer waitSecs = (Integer)actionParam;
			if (waitSecs != null) {
				monitor.subTask(LaunchMessages.getFormattedString("MultiLaunchConfigurationDelegate.Action.Delaying", //$NON-NLS-1$ 
						waitSecs.toString()));			
				try {
					Thread.sleep(waitSecs * 1000);	// param is milliseconds
				} catch (InterruptedException e) {
					// ok
				}
			}
			break;
			
		default:
			assert false : "new post launch action type is missing logic"; //$NON-NLS-1$
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

	public static List<LaunchElement> createLaunchElements(ILaunchConfiguration configuration,
			List<MultiLaunchConfigurationDelegate.LaunchElement> input) {
		try {
			Map<?,?> attrs = configuration.getAttributes();
			for (Iterator<?> iterator = attrs.keySet().iterator(); iterator.hasNext();) {
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
							
							Object actionParam = null;
							final EPostLaunchAction action = EPostLaunchAction.valueOf((String)attrs.get(getProp(index, ACTION_PROP)));
							if (action == EPostLaunchAction.DELAY) {
								try {
									actionParam = Integer.parseInt((String)attrs.get(getProp(index, ACTION_PARAM_PROP)));
								}
								catch (NumberFormatException exc) {
									LaunchUIPlugin.log(exc);
								}  
							}
							el.setAction(action, actionParam);
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

	public static void storeLaunchElements(ILaunchConfigurationWorkingCopy configuration, List<LaunchElement> input) {
		int i = 0;
		removeLaunchElements(configuration);
		for (LaunchElement el : input) {
			if (el == null) continue;
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, NAME_PROP), el.getName());
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ACTION_PROP), el.getAction().toString());
			// note: the saving of the action param will need to be enhanced if ever an action type is introduced that uses something that can't be reconstructed from its toString()
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ACTION_PARAM_PROP), el.getActionParam() != null ? el.getActionParam().toString() : null);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, MODE_PROP), el.getMode());
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ENABLED_PROP), el.isEnabled() + ""); //$NON-NLS-1$
			i++;
		}
	}

	public static void removeLaunchElements(ILaunchConfigurationWorkingCopy configuration) {
		try {
			Map<?,?> attrs = configuration.getAttributes();
			for (Iterator<?> iterator = attrs.keySet().iterator(); iterator.hasNext();) {
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
