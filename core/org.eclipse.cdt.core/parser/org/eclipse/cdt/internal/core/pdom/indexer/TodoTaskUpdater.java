/*******************************************************************************
 * Copyright (c) 2007 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.pdom.ITodoTaskUpdater;
import org.eclipse.cdt.internal.core.pdom.indexer.TodoTaskParser.Task;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;


public class TodoTaskUpdater implements ITodoTaskUpdater {
	private static final IMarker[] EMPTY_MARKER_ARRAY = new IMarker[0];
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

	public void updateTasks(IASTComment[] comments, IIndexFileLocation[] fileLocations) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		for (int i = 0; i < fileLocations.length; i++) {
			IIndexFileLocation indexFileLocation = fileLocations[i];
			String filepath = indexFileLocation.getFullPath();
			if (filepath != null) {
				IFile file = workspaceRoot.getFile(new Path(filepath));
				if (file != null && getTasksFor(file).length != 0) {
					removeTasksFor(file);
				}
			}
		}

		if (comments.length == 0) {
			return;
		}

		Task[] tasks = taskParser.parse(comments);

		String location = null;
		IFile[] files = null;
		for (int i = 0; i < tasks.length; i++) {
			Task task = tasks[i];
			if (!task.getFileLocation().equals(location)) {
				location = task.getFileLocation();
				files = workspaceRoot.findFilesForLocation(new Path(location));
			}
			for (int j = 0; j < files.length; j++) {
				try {
					applyTask(task, files[j]);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
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

	private static IMarker[] getTasksFor(IResource resource) {
		try {
			if (resource != null && resource.exists())
				return resource.findMarkers(ICModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return EMPTY_MARKER_ARRAY;
	}
	
	private static void removeTasksFor(IResource resource) {
		try {
			if (resource != null && resource.exists())
				resource.deleteMarkers(ICModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
    private String[] split(String value, String delimiters) {
        StringTokenizer tokenizer = new StringTokenizer(value, delimiters);
        int size = tokenizer.countTokens();
        String[] tokens = new String[size];
        for (int i = 0; i < size; i++)
            tokens[i] = tokenizer.nextToken();
        return tokens;
    }
}
