/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

/**
 * A translation result consists of all information returned during translation for 
 * a translation unit.  This includes:
 * <ul>
 * <li> the translation unit that was processed
 * <li> any problems (errors, warnings, tasks etc.) produced
 * </ul>
 */

import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class TranslationResult implements ITranslationResult {
	
	public IProblem problems[];
	public IProblem tasks[];
	public int problemCount;
	public int taskCount;
	public ITranslationUnit translationUnit;
	private int maxProblemPerUnit;

	public int unitIndex, totalUnitsKnown;
	public boolean hasBeenAccepted = false;
	public char[] fileName;
	
	public TranslationResult(
		char[] fileName,
		int unitIndex, 
		int totalUnitsKnown,
		int maxProblemPerUnit){
	
		this.fileName = fileName;
		this.unitIndex = unitIndex;
		this.totalUnitsKnown = totalUnitsKnown;
		this.maxProblemPerUnit = maxProblemPerUnit;
	}

    public static final int DEFAULT_MAX_PROBLEMS_PER_UNIT = 100;
    
    public TranslationResult(
        ITranslationUnit translationUnit) {           
            this(translationUnit, 1, 1, DEFAULT_MAX_PROBLEMS_PER_UNIT);
    }
	
	public TranslationResult(
		ITranslationUnit translationUnit,
		int unitIndex, 
		int totalUnitsKnown,
		int maxProblemPerUnit){
	
		this.fileName = translationUnit.getPath().lastSegment().toCharArray();
		this.translationUnit = translationUnit;
		this.unitIndex = unitIndex;
		this.totalUnitsKnown = totalUnitsKnown;
		this.maxProblemPerUnit = maxProblemPerUnit;
	}


	private int computePriority(IProblem problem) {
				
		// early problems first
		int priority = 100000 - problem.getSourceLineNumber();

		return priority;
	}

	
	public IProblem[] getAllProblems() {
		
		IProblem[] problems = this.getProblems();
		int problemCount = problems != null ? problems.length : 0;
		
		IProblem[] tasks = this.getTasks();
		int taskCount = tasks != null ? tasks.length : 0;
		
		if (taskCount == 0) {
			return problems;
		}
		if (problemCount == 0) {
			return tasks;
		}

		int totalNumberOfProblem = problemCount + taskCount;
		IProblem[] allProblems = new IProblem[totalNumberOfProblem];
		
		int allProblemIndex = 0;
		int taskIndex = 0;
		int problemIndex = 0;
		
		while (taskIndex + problemIndex < totalNumberOfProblem) {
			IProblem nextTask = null;
			IProblem nextProblem = null;
			
			if (taskIndex < taskCount) {
				nextTask = tasks[taskIndex];
			}
			if (problemIndex < problemCount) {
				nextProblem = problems[problemIndex];
			}
			
			// select the next problem
			IProblem currentProblem = null;
			if (nextProblem != null) {
				if (nextTask != null) {
					if (nextProblem.getSourceStart() < nextTask.getSourceStart()) {
						currentProblem = nextProblem;
						problemIndex++;
					} else {
						currentProblem = nextTask;
						taskIndex++;
					}
				} else {
					currentProblem = nextProblem;
					problemIndex++;
				}
			} else {
				if (nextTask != null) {
					currentProblem = nextTask;
					taskIndex++;
				}
			}
			allProblems[allProblemIndex++] = currentProblem;
		}
		
		return allProblems;
	}
	
	/**
	 * Answer the initial translation unit corresponding to the present translation result
	 */
	public ITranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	/**
	 * Answer the initial file name
	 */
	public char[] getFileName() {
		return fileName;
	}
	
	/**
	 * Answer the errors encountered during translation.
	 */
	public IProblem[] getErrors() {
	
		IProblem[] problems = getProblems();
		int errorCount = 0;
		
		for (int i = 0; i < this.problemCount; i++) {
			if (problems[i].isError()) errorCount++;
		}
		if (errorCount == this.problemCount) return problems;
		
		IProblem[] errors = new IProblem[errorCount];
		int index = 0;
		
		for (int i = 0; i < this.problemCount; i++) {
			if (problems[i].isError()) errors[index++] = problems[i];
		}
		return errors;
	}
	
	/**
	 * Answer the problems (errors and warnings) encountered during translation.
	 *
	 * This is not a compiler internal API - it has side-effects !
	 * It is intended to be used only once all problems have been detected,
	 * and makes sure the problems slot as the exact size of the number of
	 * problems.
	 */
	public IProblem[] getProblems() {
		
		// Re-adjust the size of the problems if necessary.
		if (problems != null) {
	
			if (this.problemCount != problems.length) {
				System.arraycopy(problems, 0, (problems = new IProblem[problemCount]), 0, problemCount);
			}
	
			if (this.maxProblemPerUnit > 0 && this.problemCount > this.maxProblemPerUnit){
				quickPrioritize(problems, 0, problemCount - 1);
				this.problemCount = this.maxProblemPerUnit;
				System.arraycopy(problems, 0, (problems = new IProblem[problemCount]), 0, problemCount);
			}
	
			// Sort problems per source positions.
			quickSort(problems, 0, problems.length-1);
		}
		return problems;
	}

	/**
	 * Answer the tasks (TO-DO, ...) encountered during translation.
	 *
	 * This is not a compiler internal API - it has side-effects !
	 * It is intended to be used only once all problems have been detected,
	 * and makes sure the problems slot as the exact size of the number of
	 * problems.
	 */
	public IProblem[] getTasks() {
		
		// Re-adjust the size of the tasks if necessary.
		if (this.tasks != null) {
	
			if (this.taskCount != this.tasks.length) {
				System.arraycopy(this.tasks, 0, (this.tasks = new IProblem[this.taskCount]), 0, this.taskCount);
			}
			quickSort(tasks, 0, tasks.length-1);
		}
		return this.tasks;
	}
	
	public boolean hasErrors() {

		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				if (problems[i].isError())
					return true;
			}
		return false;
	}

	public boolean hasProblems() {

		return problemCount != 0;
	}

	public boolean hasSyntaxError(){

		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				IProblem problem = problems[i];
				if ((problem.getID() & IProblem.Syntax) != 0 && problem.isError())
					return true;
			}
		return false;
	}

	public boolean hasTasks() {
		return this.taskCount != 0;
	}
	
	public boolean hasWarnings() {

		if (problems != null)
			for (int i = 0; i < problemCount; i++) {
				if (problems[i].isWarning())
					return true;
			}
		return false;
	}
	
	private static void quickSort(IProblem[] list, int left, int right) {

		if (left >= right) return;
	
		// sort the problems by their source start position... starting with 0
		int original_left = left;
		int original_right = right;
		int mid = list[(left + right) / 2].getSourceStart();
		
		do {
			while (list[left].getSourceStart() < mid)
				left++;
				
			while (mid < list[right].getSourceStart())
				right--;
				
			if (left <= right) {
				IProblem tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		
		if (original_left < right)
			quickSort(list, original_left, right);
			
		if (left < original_right)
			quickSort(list, left, original_right);
	}
	
	private void quickPrioritize(IProblem[] list, int left, int right) {
		
		if (left >= right) return;
	
		// sort the problems by their priority... starting with the highest priority
		int original_left = left;
		int original_right = right;
		int mid = computePriority(list[(left + right) / 2]);
		
		do {
			while (computePriority(list[right]) < mid)
				right--;
				
			while (mid < computePriority(list[left]))
				left++;
				
			if (left <= right) {
				IProblem tmp = list[left];
				list[left] = list[right];
				list[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		
		if (original_left < right)
			quickPrioritize(list, original_left, right);
			
		if (left < original_right)
			quickPrioritize(list, left, original_right);
	}
	

	public void record(IProblem newProblem, IReferenceContext referenceContext) {

		if (newProblem.getID() == IProblem.Task) {
			recordTask(newProblem);
			return;
		}
		
		if (problemCount == 0) {
			problems = new IProblem[5];
		} else if (problemCount == problems.length) {
			System.arraycopy(problems, 0, (problems = new IProblem[problemCount * 2]), 0, problemCount);
		}
		
		problems[problemCount++] = newProblem;
	}


	private void recordTask(IProblem newProblem) {
		if (this.taskCount == 0) {
			this.tasks = new IProblem[5];
		} else if (this.taskCount == this.tasks.length) {
			System.arraycopy(this.tasks, 0, (this.tasks = new IProblem[this.taskCount * 2]), 0, this.taskCount);
		}
		
		this.tasks[this.taskCount++] = newProblem;
	}
	
	
	public ITranslationResult tagAsAccepted() {
		this.hasBeenAccepted = true;
		return this;
	}
	
	
	public String toString() {

		StringBuffer buffer = new StringBuffer();
		if (this.fileName != null){
			buffer.append("Filename : ").append(this.fileName).append('\n'); //$NON-NLS-1$
		}
		if (problems != null){
			buffer.append(this.problemCount).append(" PROBLEM(s) detected \n"); //$NON-NLS-1$//$NON-NLS-2$
			for (int i = 0; i < this.problemCount; i++){
				buffer.append("\t - ").append(this.problems[i]).append('\n'); //$NON-NLS-1$
			}
		} else {
			buffer.append("No PROBLEM\n"); //$NON-NLS-1$
		} 
		return buffer.toString();
	}
}
