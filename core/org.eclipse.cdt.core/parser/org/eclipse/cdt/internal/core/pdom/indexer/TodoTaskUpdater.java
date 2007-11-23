/*******************************************************************************
 * Copyright (c) 2007 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.pdom.ITodoTaskUpdater;
import org.eclipse.cdt.internal.core.pdom.indexer.TodoTaskParser.Task;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;


public class TodoTaskUpdater implements ITodoTaskUpdater {
	private static final String SOURCE_ID = "CDT"; //$NON-NLS-1$
	private static final String[] TASK_MARKER_ATTRIBUTE_NAMES = {
		IMarker.MESSAGE, 
		IMarker.PRIORITY, 
		IMarker.CHAR_START, 
		IMarker.CHAR_END, 
		IMarker.LINE_NUMBER, 
		IMarker.USER_EDITABLE,
		IMarker.SOURCE_ID,
	};

	private final TodoTaskParser taskParser;
	
	public TodoTaskUpdater() {
		String value = CCorePlugin.getOption(CCorePreferenceConstants.TODO_TASK_TAGS);
		if (value == null) {
			value = CCorePreferenceConstants.DEFAULT_TASK_TAG;
		}
        String[] tags = split(value, ","); //$NON-NLS-1$
        char[][] taskTags = new char[tags.length][];
        for (int i = 0; i < tags.length; i++) {
			taskTags[i] = tags[i].toCharArray();
		}
        
		value = CCorePlugin.getOption(CCorePreferenceConstants.TODO_TASK_PRIORITIES);
		if (value == null) {
			value = CCorePreferenceConstants.DEFAULT_TASK_PRIORITY;
		}
        String[] priorities = split(value, ","); //$NON-NLS-1$
        int[] taskPriorities = new int[taskTags.length];
        for (int i = 0; i < taskPriorities.length; i++) {
			String priority = i < priorities.length ?
					priorities[i] : CCorePreferenceConstants.DEFAULT_TASK_PRIORITY;
			taskPriorities[i] =
					CCorePreferenceConstants.TASK_PRIORITY_HIGH.equals(priority) ?
						IMarker.PRIORITY_HIGH :
					CCorePreferenceConstants.TASK_PRIORITY_LOW.equals(priority) ?
						IMarker.PRIORITY_LOW : IMarker.PRIORITY_NORMAL;
		}
        
		value = CCorePlugin.getOption(CCorePreferenceConstants.TODO_TASK_CASE_SENSITIVE);
		if (value == null) {
			value = CCorePreferenceConstants.DEFAULT_TASK_CASE_SENSITIVE;
		}
        boolean isTaskCaseSensitive = Boolean.valueOf(value).booleanValue();
        taskParser = new TodoTaskParser(taskTags, taskPriorities, isTaskCaseSensitive);
	}

	public void updateTasks(IASTComment[] comments, IIndexFileLocation[] filesToUpdate) {
		class TaskList {
			IFile fFile;
			List  fTasks;
			public TaskList(IFile file) {
				fFile= file;
			}
			public void add(Task task) {
				if (fTasks == null) {
					fTasks= new ArrayList();
				}
				fTasks.add(task);
			}
		}

		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		// first collect all valid file-locations
		final HashMap pathToTaskList= new HashMap();
		final HashSet projects= new HashSet();
		for (int i = 0; i < filesToUpdate.length; i++) {
			final IIndexFileLocation indexFileLocation = filesToUpdate[i];
			final String filepath = indexFileLocation.getFullPath();
			if (filepath != null) {
				IFile file = workspaceRoot.getFile(new Path(filepath));
				if (file != null && file.exists()) {
					projects.add(file.getProject());
					pathToTaskList.put(IndexLocationFactory.getAbsolutePath(indexFileLocation), new TaskList(file));
				}
			}
		}

		if (comments.length > 0) {
			final Task[] tasks = taskParser.parse(comments);
			for (int i = 0; i < tasks.length; i++) {
				final Task task = tasks[i];
				TaskList list= (TaskList) pathToTaskList.get(new Path(task.getFileLocation()));
				if (list != null) {
					list.add(task);
				}
			}
		}
		
		// run this in a job in order not to block the indexer (bug 210730).
		if (!pathToTaskList.isEmpty()) {
			Job job= new Job(Messages.TodoTaskUpdater_UpdateJob) {
				protected IStatus run(IProgressMonitor monitor) {
					MultiStatus status= new MultiStatus(CCorePlugin.PLUGIN_ID, 0, Messages.TodoTaskUpdater_UpdateJob, null);

					for (Iterator it = pathToTaskList.values().iterator(); it.hasNext();) {
						final TaskList tasklist = (TaskList) it.next();
						try {
							final IFile file= tasklist.fFile;
							if (file.exists()) {
								file.deleteMarkers(ICModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
								final List tasks= tasklist.fTasks;
								if (tasks != null) {
									for (Iterator itTasks = tasks.iterator(); itTasks.hasNext();) {
										Task task = (Task) itTasks.next();
										applyTask(task, file);
									}
								}
							}
						} catch (CoreException e) {
							status.add(e.getStatus());
						}
					}
					return status;
				}
			};
			job.setRule(new MultiRule((IProject[]) projects.toArray(new IProject[projects.size()])));
			job.setSystem(true);
			job.schedule();
		}
	}

	private void applyTask(Task task, IResource resource) throws CoreException {
		IMarker marker = resource.createMarker(ICModelMarker.TASK_MARKER);
		String description = NLS.bind(Messages.TodoTaskUpdater_taskFormat,
				task.getTag(), task.getMessage());
		marker.setAttributes(
			TASK_MARKER_ATTRIBUTE_NAMES,
			new Object[] { 
				description,
				new Integer(task.getPriority()),
				new Integer(task.getStart()),
				new Integer(task.getEnd()),
				new Integer(task.getLineNumber()),
				Boolean.FALSE,
				SOURCE_ID
			});
	}
	
    private String[] split(String value, String delimiters) {
        StringTokenizer tokenizer = new StringTokenizer(value, delimiters);
        int size = tokenizer.countTokens();
        String[] tokens = new String[size];
        for (int i = 0; i < size; i++)
            tokens[i] = tokenizer.nextToken();
        return tokens;
    }
    
	public static void removeTasksFor(final IResource resource) {
		if (resource == null || !resource.exists()) {
			return;
		}
		
		// run this in a job in order not to block the indexer (bug 210730).
		Job job= new Job(Messages.TodoTaskUpdater_DeleteJob) {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (resource.exists()) {
						resource.deleteMarkers(ICModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
					}
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(resource);
		job.setSystem(true);
		job.schedule();
	}
}
