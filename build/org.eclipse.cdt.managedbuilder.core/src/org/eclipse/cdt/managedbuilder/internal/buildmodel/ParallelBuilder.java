/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * This is the main class for parallel internal builder implementation 
 *
 * NOTE: This class is subject to change and discuss, 
 * and is currently available in experimental mode only
 */
public class ParallelBuilder {
	public static final int STATUS_OK = 0;
	public static final int STATUS_ERROR = 1;
	public static final int STATUS_CANCELED = 2;
	public static final int STATUS_INVALID = -1;
	public static final long MAIN_LOOP_DELAY = 50L;
	
	private static final String BUILDER_MSG_HEADER = "InternalBuilder.msg.header"; //$NON-NLS-1$ 
	private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static int lastThreadsUsed = 0; // use externally for report purposes only
	
	protected IPath cwd;
	protected GenDirInfo dirs;
	protected IProgressMonitor monitor;
	protected OutputStream out;
	protected OutputStream err;
	protected boolean resumeOnErrors;
	protected boolean buildIncrementally;
	protected HashSet<BuildQueueElement> unsorted = new HashSet<BuildQueueElement>();
	protected HashMap<IBuildStep, BuildQueueElement> queueHash = new HashMap<IBuildStep, BuildQueueElement>();
	protected LinkedList<BuildQueueElement> queue = new LinkedList<BuildQueueElement>();

	/**
	 * This class implements queue element
	 */
	protected class BuildQueueElement implements Comparable<BuildQueueElement> {
		protected IBuildStep step;
		protected int level;
		
		public BuildQueueElement(IBuildStep _step, int _level) {
			step = _step;
			level = _level;
		}
		
		public IBuildStep getStep() {
			return step;
		}
		
		public int getLevel() {
			return level;
		}
		
		public void setLevel(int _level) {
			level = _level;
		}
		
		@Override
		public int hashCode() {
			return step.hashCode();
		}
		
		public int compareTo(BuildQueueElement elem) {
			if (elem == null)
				throw new NullPointerException();
			
			if (elem.getLevel() > level)
				return -1;
			if (elem.getLevel() < level)
				return 1;
			return 0;
		}

		/**
		 * Updates level value 
		 */
		public boolean check(IBuildStep _step, int _level) {
			if (level < _level && step.equals(_step)) {
				level = _level;
				return true;
			} else { return false; }
		}
		
		@Override
		public String toString() {
			return"[BuildQueueElement] " + DbgUtil.stepName(step) + " @ " + level; //$NON-NLS-1$ //$NON-NLS-2$
		}
 	}
	
	/**
	 * This class stores information about step being built
	 */
	protected class ActiveBuildStep {
		protected IPath stepCwd;
		protected GenDirInfo stepDirs;
		protected IBuildStep step;
		protected IBuildCommand[] cmds;
		protected int activeCmd;
		protected boolean done;
		protected ProcessLauncher launcher;
		
		public ActiveBuildStep(IBuildStep _step) {
			step = _step;
			
			if(dirs == null)
				stepDirs = new GenDirInfo(step.getBuildDescription().getConfiguration());
			else
				stepDirs = dirs;
			if (cwd == null)
				stepCwd = step.getBuildDescription().getDefaultBuildDirLocation();
			else
				stepCwd = cwd;
			cmds = step.getCommands(stepCwd, null, null, true);
			activeCmd = -1;
			done = false;
			createOutDirs();
		}
		
		public boolean launchNextCmd(BuildProcessManager mgr) {
			if (monitor.isCanceled()) {
				done = true;
				return false;
			}
			if (activeCmd + 1 >= cmds.length)
				done = true;
			else {
				IBuildCommand cmd = cmds[++activeCmd];
				launcher = mgr.launchProcess(cmd, stepCwd, monitor); 
				if (launcher != null) return true;
				activeCmd--;
				done = true; // temporary
			}
			return false;
		}
		
		public boolean isDone() {
			return done;
		}
		
		public IBuildStep getStep() {
			return step;
		}
		
		public ProcessLauncher getLauncher() {
			return launcher;
		}
		protected void createOutDirs(){
			IBuildResource rcs[] = step.getOutputResources();
			
			for(int i = 0; i < rcs.length; i++){
				dirs.createDir(rcs[i], new NullProgressMonitor());
			}
		}
	}

	/**
	 * Build process is divided into following steps:
	 * 1. Resources enqueueing & levelling
	 * 2. Queue sorting
	 * 3. Queue dispatching
	 * 
	 * @param des Build description
	 * @param cwd Working directory
	 * @param dirs GenDirInfo?
	 * @param out Output stream
	 * @param err Error output stream
	 * @param monitor Progress monitor
	 * @param resumeOnErrors If true, build process will not stop when
	 * compilation errors encountered
	 */
	static public int build(IBuildDescription des, IPath cwd, GenDirInfo dirs, OutputStream out, OutputStream err, IProgressMonitor monitor, boolean resumeOnErrors, boolean buildIncrementally) {
		IConfiguration cfg = des.getConfiguration();
		if(dirs == null) dirs = new GenDirInfo(cfg);
		if(cwd == null)  cwd = des.getDefaultBuildDirLocation();
		int threads = 1;
		if (cfg instanceof Configuration) {
			if (((Configuration)cfg).getParallelDef())
				threads = BuildProcessManager.checkCPUNumber();
			else
				threads = ((Configuration)cfg).getParallelNumber();  
		}
		ParallelBuilder builder = new ParallelBuilder(cwd, dirs, out, err, monitor, resumeOnErrors, buildIncrementally);
		builder.enqueueAll(des);
		builder.sortQueue();
		monitor.beginTask("", builder.queue.size()); //$NON-NLS-1$
		builder.dispatch(new BuildProcessManager(out, err, true, threads));
		monitor.done();
		lastThreadsUsed = threads;
		return IBuildModelBuilder.STATUS_OK;
	}
	
	/**
	 * Initializes parallel builder
	 */
	protected ParallelBuilder(IPath _cwd, GenDirInfo _dirs, OutputStream _out, OutputStream _err, IProgressMonitor _monitor, boolean _resumeOnErrors, boolean _buildIncrementally) {
		cwd = _cwd;
		dirs = _dirs;
		out = _out;
		err = _err;
		monitor = _monitor;
		resumeOnErrors = _resumeOnErrors;
		buildIncrementally = _buildIncrementally;
	}
	
	/**
	 * Enqueues build steps, calculating their levels
	 */
	protected void enqueueAll(IBuildDescription des) {
		enqueueSteps(des.getInputStep(), 0);
	}
	
	/**
	 * Sorts the queue
	 */
	protected void sortQueue() {
		for (BuildQueueElement elem : unsorted) {
			queue.add(elem);
		}
		unsorted.clear();
		unsorted = null;
		queueHash.clear();
		queueHash = null;

		Collections.sort(queue);
	}

	/**
	 * Enqueues build steps directly accessed from the given one. Each
	 * new element will have level 1 if it needs rebuild and 0 otherwise.
	 */
	protected void enqueueSteps(IBuildStep step, int level) {
		IBuildResource[] resources = step.getOutputResources();
		
		for (int i = 0; i < resources.length; i++) {
			IBuildStep steps[] = resources[i].getDependentSteps();
			for (int j = 0; j < steps.length; j++) {
				IBuildStep st = steps[j];
				if (st != null && st.getBuildDescription().getOutputStep() != st) {
					BuildQueueElement b = queueHash.get(st);
					if (b != null){
						if (b.level < level) b.setLevel(level);
				 	} else {
				 		//TODO: whether we need check isRemoved & needRebuild ?
				 		if (!steps[j].isRemoved() && (!buildIncrementally || steps[j].needsRebuild())) {
				 			addElement(steps[j], level);
				 		}
				 		enqueueSteps(steps[j], level + 1);
				 	}
				}
			}
		}
	}
	
	/**
	 * Adds new element to the build queue and step<->element hash map
	 */
	protected void addElement(IBuildStep step, int level) {
		BuildQueueElement elem = new BuildQueueElement(step, level); 
		unsorted.add(elem);
		queueHash.put(step, elem);
	}
	
	/**
	 * Dispatches the build queue and returns build status
	 */
	protected int dispatch(BuildProcessManager mgr) {
		ActiveBuildStep[] active = new ActiveBuildStep[mgr.getMaxProcesses()];
		for (int i = 0; i < active.length; i++) {
			active[i] = null; // new ActiveBuildStep();
		}
		
		int activeCount = 0;
		int maxLevel = 0;
		int status = STATUS_OK;
		String errorMsg = null;
		
		// Going into "infinite" main loop
		main_loop:
		while (true) {
			if (monitor.isCanceled()) {
				status = STATUS_CANCELED;
				errorMsg = CCorePlugin.getResourceString("CommandLauncher.error.commandCanceled"); //$NON-NLS-1$
				break main_loop;
			}
			// Check build process states
			ProcessLauncher launcher = mgr.queryStates();
			if (launcher != null) {
				// Build process has been canceled or failed to launch
				if (launcher.queryState() == ProcessLauncher.STATE_CANCELED)
					status = STATUS_CANCELED;
				else
					status = STATUS_INVALID;
				errorMsg = launcher.getErrorMessage();
				break main_loop;
			}
			// Everything goes OK.
			boolean proceed = true;
			
			// Check if there is room for new process
			if (!mgr.hasEmpty()) {
				proceed = false;
			} else {
				// Check "active steps" list for completed ones
				for (int i = 0; i < active.length; i++) {
					if (active[i] == null) continue;
					ProcessLauncher pl = active[i].getLauncher();
					if (pl == null) continue; 
					if (pl.queryState() == ProcessLauncher.STATE_DONE) {
						// If process has terminated with error, break loop
						// (except resumeOnErrors == true)
						
						if (!resumeOnErrors && pl.getExitCode() != 0) {
							status = STATUS_ERROR;
							break main_loop;
						}
						// Try to launch next command for the current active step
						if (active[i].isDone()) continue;
						if (active[i].launchNextCmd(mgr)) {
							// Command has been launched. Check if process pool is not maximized yet
							if (!mgr.hasEmpty()) {
								proceed = false;
								break;
							}
						} else {
							// Command has not been launched: step complete
							refreshOutputs(active[i].getStep());
							activeCount--;
							monitor.worked(1);
						}
					}
				}
			}
			
			// If nothing to do, then sleep and continue main loop
			if (!proceed) {
				try {
					Thread.sleep(MAIN_LOOP_DELAY);
				} catch (InterruptedException e) {
					// do nothing
				}
				continue main_loop;
			}
			
			// Check if we need to schedule another process
			if (queue.size() != 0 && activeCount < active.length) {
				// Need to schedule another process 
				Iterator<BuildQueueElement> iter = queue.iterator();

				// Iterate over build queue
				while (iter.hasNext()) {
					BuildQueueElement elem = iter.next();
					
					// If "active steps" list is full, then break loop
					if (activeCount == active.length)
						break;
					
					// If current element's level exceeds maximum level of currently built
					// resources, then stop iteration (we can not build it anyway)
					if (elem.getLevel() > maxLevel + 1)
						break;

					//Check if all prerequisites are built
					IBuildResource[] res = elem.getStep().getInputResources();
					boolean prereqBuilt = true;
					for (int j = 0; j < res.length; j++) {
						IBuildStep stp = res[j].getProducerStep(); // step which produces input for curr
						boolean built = true;
						if (stp != stp.getBuildDescription().getInputStep()) {
							for (int k = 0; k < active.length; k++) {
								if (active[k] != null && active[k].getStep().equals(stp) && !active[k].isDone()) {
									built = false;
									break;
								}
							}
						}
						if (!built) {
							prereqBuilt = false;
							break;
						}
					}

					if (prereqBuilt) {
						// All prereqs are built
						IBuildStep step = elem.getStep();
						
						// Remove element from the build queue and add it to the
						// "active steps" list.
						iter.remove();
						for (int i = 0; i < active.length; i++) {
							if (active[i] == null || active[i].isDone()) {
								active[i] = new ActiveBuildStep(step);
								if (active[i].launchNextCmd(mgr)) activeCount++;
								break;
							}
						}
						
						// Update maxLevel
						if (elem.getLevel() > maxLevel)
							maxLevel = elem.getLevel();
						// We don't need to start new process immediately since
						// it will be done on the next main loop iteration
					}
				}
			}
			
			// Now finally, check if we're done
			if (activeCount <= 0 && queue.size() == 0) 
				break main_loop;
		}

		if (status != STATUS_OK && errorMsg != null) 
			printMessage(errorMsg, out);
		return status;
	}
	
	/**
	 * Prints output to the console 
	 */
	protected void printMessage(String msg, OutputStream out) {
		if (out != null) {
			msg = ManagedMakeMessages.getFormattedString(BUILDER_MSG_HEADER, msg) + LINE_SEPARATOR;
			try {
				out.write(msg.getBytes());
				out.flush();
			} catch (IOException e) {
				// do nothing
			}
		}
	}
	
	/**
	 * Updates info about generated files (after step completed)
	 */
	protected void refreshOutputs(IBuildStep step){
		IProgressMonitor mon = new NullProgressMonitor();
		IBuildResource outres[] = step.getOutputResources();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for(int i = 0; i < outres.length; i++){
			IPath path = outres[i].getFullPath();
			if(path != null){
				try { root.getFile(path).refreshLocal(0, mon); }
				catch (CoreException e) {}
			}
		}
	}

	
}
