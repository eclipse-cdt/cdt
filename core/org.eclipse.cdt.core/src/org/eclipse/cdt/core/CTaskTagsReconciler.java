package org.eclipse.cdt.core;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ITranslationResult;
import org.eclipse.cdt.core.parser.IProblem;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;


public class CTaskTagsReconciler {
	
	private static CTaskTagsReconciler instance = null;
	
	private CTaskTagsReconciler() {
	}

	public static CTaskTagsReconciler getInstance() {
		if (instance == null) {
			instance = new CTaskTagsReconciler();
		}
		return instance;
	}
	

	public void acceptResult(ITranslationUnit translationUnit, ITranslationResult result) {    
		try {
			updateTasksFor(translationUnit, result); // record tasks
		} catch (CoreException e) {
			System.out.println("Exception while accepting parse results");
			e.printStackTrace();
		}
	}

	protected void updateTasksFor(ITranslationUnit sourceFile, ITranslationResult result) throws CoreException {
		IProblem[] tasks = result.getTasks();

		storeTasksFor(sourceFile, tasks);
	}

	protected void storeTasksFor(ITranslationUnit sourceFile, IProblem[] tasks) throws CoreException {
		if (sourceFile == null) return;
	
		if (tasks == null) tasks = new IProblem[0];

		IResource resource = sourceFile.getResource();
		IMarker[] existingTaskMarkers = resource.findMarkers(ICModelMarker.TASK_MARKER, false, IResource.DEPTH_ONE);
		HashSet taskSet = new HashSet();
	
		if (existingTaskMarkers != null)
			for (int i=0; i<existingTaskMarkers.length; i++) 
				taskSet.add(existingTaskMarkers[i]);

		taskLoop:
		for (int i = 0, l = tasks.length; i < l; i++) {
			IProblem task = tasks[i];
			if (task.getID() == IProblem.Task) {
			
				int priority = IMarker.PRIORITY_NORMAL;
				String compilerPriority = task.getArguments()[2];
				if (CCorePlugin.TRANSLATION_TASK_PRIORITY_HIGH.equals(compilerPriority))
					priority = IMarker.PRIORITY_HIGH;
				else if (CCorePlugin.TRANSLATION_TASK_PRIORITY_LOW.equals(compilerPriority))
					priority = IMarker.PRIORITY_LOW;
			
				/*
				 * Try to find matching markers and don't put in duplicates
				 */
				if ((existingTaskMarkers != null) && (existingTaskMarkers.length > 0)) {
					for (int j = 0; j < existingTaskMarkers.length; j++) {
						if (
							   (((Integer) existingTaskMarkers[j].getAttribute(IMarker.LINE_NUMBER)).intValue() == task.getSourceLineNumber())
							&& (((Integer) existingTaskMarkers[j].getAttribute(IMarker.PRIORITY)).intValue() == priority)
							&& (((Integer) existingTaskMarkers[j].getAttribute(IMarker.CHAR_START)).intValue() == task.getSourceStart())
							&& (((Integer) existingTaskMarkers[j].getAttribute(IMarker.CHAR_END)).intValue() == task.getSourceEnd()+1)
							&& (((String) existingTaskMarkers[j].getAttribute(IMarker.MESSAGE)).equals(task.getMessage()))
							) {
								taskSet.remove(existingTaskMarkers[j]);
								continue taskLoop;
						}
					}
				}
						
				IMarker marker = resource.createMarker(ICModelMarker.TASK_MARKER);

				marker.setAttributes(
					new String[] {
						IMarker.MESSAGE, 
						IMarker.PRIORITY, 
						IMarker.DONE, 
						IMarker.CHAR_START, 
						IMarker.CHAR_END, 
						IMarker.LINE_NUMBER,
						IMarker.USER_EDITABLE, 
					}, 
					new Object[] { 
						task.getMessage(),
						new Integer(priority),
						new Boolean(false),
						new Integer(task.getSourceStart()),
						new Integer(task.getSourceEnd() + 1),
						new Integer(task.getSourceLineNumber()),
						new Boolean(false),
					});
			}
		}
	
		// Remove all obsolete markers
		Iterator setI = taskSet.iterator();
		while (setI.hasNext()) {
			IMarker marker = (IMarker)setI.next();
			marker.delete();
		}
	}
}
