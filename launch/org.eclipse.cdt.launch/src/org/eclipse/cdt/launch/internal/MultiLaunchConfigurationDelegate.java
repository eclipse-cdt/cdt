/*******************************************************************************
 *  Copyright (c) 2009, 2010 QNX Software Systems and others.
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
import java.util.Arrays;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Group Launch delegate. Launches each configuration in the user selected mode
 */
public class MultiLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate2 {
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
		
		public int index;
		public boolean enabled;
		public String mode;
		public EPostLaunchAction action;
		public Object actionParam;
		public String name;
		public ILaunchConfiguration data;
	}

	public MultiLaunchConfigurationDelegate() {
		// nothing
	}

	/**
	 * A specialization of launch to track sublaunches lifecycle, also terminates itself when all sublaunches are terminated
	 *  
	 */
	private class MultiLaunch extends Launch implements ILaunchesListener2{
	
		/**
		 * Whether this process has been terminated
		 */
		private boolean fTerminated;
			
		/**
		 * A map of all our sub-launches and the current processes that belong
		 * to each one.
		 */
		private Map<ILaunch, IProcess[]> subLaunches = new HashMap<ILaunch, IProcess[]>();
		
	
		public MultiLaunch(ILaunchConfiguration launchConfiguration,
				String mode) {
			super(launchConfiguration, mode, null);
			getLaunchManager().addLaunchListener((ILaunchesListener2)this);
		}
	
		/**
		 * Associate the launch
		 * @param subLaunch
		 */
		public void addSubLaunch(ILaunch subLaunch) {
			subLaunches.put(subLaunch, new IProcess[]{});
		}
	
		private ILaunch[] getSubLaunches() {
			return subLaunches.keySet().toArray(new ILaunch[subLaunches.keySet().size()]);
		}
	
		private boolean isChild(ILaunch launch) {
			for (ILaunch subLaunch : getSubLaunches()) {
				if (subLaunch == launch) { return true; }
			}
			return false;
		}		
	
		/**
		 * Override default behavior by querying all sub-launches to see if they are terminated
		 * @see org.eclipse.debug.core.Launch#isTerminated()
		 */
		@Override
		public boolean isTerminated() {
			if (fTerminated)
				return true;
			
			if (subLaunches.size() == 0)
				return false;
			
			for (ILaunch launch : getSubLaunches()) {
				if (!launch.isTerminated()) {
					return false;
				}
			}
			return true;
		}
	
	
		/**
		 * Override default behavior by querying all sub-launches if they can be terminated
		 * @see org.eclipse.debug.core.Launch#canTerminate()
		 */
		@Override
		public boolean canTerminate() {
			if (subLaunches.size() == 0)
				return false;
			
			for (ILaunch launch : getSubLaunches()) {
				if (launch.canTerminate()) {
					return true;
				}
			}
			return false;
		}
	
		/**
		 * Override default behavior by terminating all sub-launches
		 * @see org.eclipse.debug.core.Launch#terminate()
		 */
		@Override
		public void terminate() throws DebugException {
			MultiStatus status= 
				new MultiStatus(DebugPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, DebugCoreMessages.Launch_terminate_failed, null); 
				
			for (ILaunch launch : getSubLaunches()) {
				if (launch.canTerminate()) {
					try {
						launch.terminate();
					} catch (DebugException e) {
						status.merge(e.getStatus());
					}
				}
			}
			
			if (status.isOK()) {
				return;
			}
			
			IStatus[] children= status.getChildren();
			if (children.length == 1) {
				throw new DebugException(children[0]);
			}
			
			throw new DebugException(status);
		}
	
		/**
		 * Handle terminated sub-launch
		 * @param launch
		 */
		private void launchTerminated(ILaunch launch) {
			if (this == launch) return;
			
			// Remove sub launch, keeping the processes of the terminated launch to 
			// show the association and to keep the console content accessible
			if (subLaunches.remove(launch) != null) {
				// terminate ourselves if this is the last sub launch 
				if (subLaunches.size() == 0) {
					fTerminated = true;
					fireTerminate();
				}
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.Launch#launchChanged(org.eclipse.debug.core.ILaunch)
		 */
		public void launchChanged(ILaunch launch) {
			if (this == launch) return;
			
			// add/remove processes
			if (isChild(launch)) {
				// Remove old processes
				IProcess[] oldProcesses = subLaunches.get(launch);
				IProcess[] newProcesses = launch.getProcesses();
				
				// avoid notifications when processes have not changed.
				if (!Arrays.equals(oldProcesses, newProcesses)) {
					for (IProcess oldProcess : oldProcesses) {
						removeProcess(oldProcess);
					}
					
					// Add new processes
					for (IProcess newProcess : newProcesses) {
						addProcess(newProcess);
					}
				
					// Replace the processes of the changed launch
					subLaunches.put(launch, newProcesses);
				}
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.Launch#launchRemoved(org.eclipse.debug.core.ILaunch)
		 */
		@Override
		public void launchRemoved(ILaunch launch) {
			if (this == launch) {
				super.launchRemoved(launch);
				// Remove the processes we got from the sub-launches from this launch
				IProcess[] processes = getProcesses();				
				for (IProcess process : processes) {
					removeProcess(process);
				}
				
				getLaunchManager().removeLaunchListener((ILaunchesListener2)this);
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesTerminated(ILaunch[] launches) {
			for (ILaunch launch : launches) {
				launchTerminated(launch);
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesAdded(ILaunch[] launches) {
			for (ILaunch launch : launches) {
				launchAdded(launch);
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesChanged(ILaunch[] launches) {
			for (ILaunch launch : launches) {
				launchChanged(launch);
			}
		}
	
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesRemoved(ILaunch[] launches) {
			for (ILaunch launch : launches) {
				launchRemoved(launch);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {
		return new MultiLaunch(configuration, mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, final ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		
		// Have to temporarily turn off the "remove terminated launches when new one created" 
		// preference because it does not work well for multilaunch

		final IPreferenceStore prefStore = DebugUIPlugin.getDefault().getPreferenceStore();
		boolean dstore = prefStore.getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES);

		try {
			monitor.beginTask(LaunchMessages.getString("MultiLaunchConfigurationDelegate.0") + configuration.getName(), 1000); //$NON-NLS-1$
			
			prefStore.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES,	false);
			
			List<LaunchElement> launches = createLaunchElements(configuration, new ArrayList<LaunchElement>());
			for (LaunchElement le : launches) {
				if (!le.enabled) continue;
				
				// find launch; if not found, skip (error?)
				final ILaunchConfiguration conf = findLaunch(le.name);
				if (conf == null) continue;
				
				// determine mode for each launch
				final String localMode;
				if (le.mode != null && !le.mode.equals(DEFAULT_MODE)) {
					localMode = le.mode;
				} else {
					localMode = mode;
				}
				if (!conf.supportsMode(localMode)) {
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
					ILaunch subLaunch = DebugUIPlugin.buildAndLaunch(conf, localMode, new SubProgressMonitor(monitor, 1000 / launches.size()));
					((MultiLaunch)launch).addSubLaunch(subLaunch);
					
					// Now that we added the launch in our list, we have already
					// received the real launchChanged event, and did not know it was part of our list
					// So, fake another event now.
					((MultiLaunch)launch).launchChanged(subLaunch);

					postLaunchAction(subLaunch, le.action, le.actionParam, monitor);
					

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
				DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
			}
		} finally {
			prefStore.setValue(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, dstore);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#buildProjects(org.eclipse.core.resources.IProject[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void buildProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException {
		// do nothing, project can be rebuild for each launch individually
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#buildForLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
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
							el.index = index;
							el.name = (String) attrs.get(attr);
							
							Object actionParam = null;
							String actionStr = (String)attrs.get(getProp(index, ACTION_PROP));

							EPostLaunchAction action;
							try {
								action = EPostLaunchAction.valueOf(actionStr);
							} catch (Exception e) {
								action = EPostLaunchAction.NONE;
							}
							if (action == EPostLaunchAction.DELAY) {
								try {
									actionParam = Integer.parseInt((String)attrs.get(getProp(index, ACTION_PARAM_PROP)));
								}
								catch (NumberFormatException exc) {
									LaunchUIPlugin.log(exc);
								}  
							}
							el.action = action;
							el.actionParam = actionParam;
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

	public static void storeLaunchElements(ILaunchConfigurationWorkingCopy configuration, List<LaunchElement> input) {
		int i = 0;
		removeLaunchElements(configuration);
		for (LaunchElement el : input) {
			if (el == null) continue;
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, NAME_PROP), el.name);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ACTION_PROP), el.action.toString());
			// note: the saving of the action param will need to be enhanced if ever an action type is introduced that uses something that can't be reconstructed from its toString()
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ACTION_PARAM_PROP), el.actionParam != null ? el.actionParam.toString() : null);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, MODE_PROP), el.mode);
			configuration.setAttribute(MultiLaunchConfigurationDelegate.getProp(i, ENABLED_PROP), el.enabled + ""); //$NON-NLS-1$
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
	
	/**
	 * Test if a launch configuration is a valid reference. 
	 * @param config configuration reference
	 * @return <code>true</code> if it is a valid reference, <code>false</code> if launch configuration should be filtered
	 */
	public static boolean isValidLaunchReference(ILaunchConfiguration config) {
		return DebugUIPlugin.doLaunchConfigurationFiltering( config) && !WorkbenchActivityHelper.filterItem(config);
	}
}
