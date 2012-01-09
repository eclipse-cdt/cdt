/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin  (Google)
 *     Andrew Ferguson  (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.ui.IPropertyChangeParticipant;

/**
 * Which words should be recognized as task tags is specified under {@link CCorePreferenceConstants#TODO_TASK_TAGS} as a
 * comma delimited list.
 * 
 * @see CCorePreferenceConstants#TODO_TASK_TAGS
 * @since 5.0
 * @deprecated This class doesn't properly implement parsing of task tags
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=246846). It will be removed.
 */
@Deprecated
public final class TaskTagRule extends WordRule implements IPropertyChangeParticipant {	
	private static class TaskTagDetector implements IWordDetector {
		@Override
		public boolean isWordStart(char c) {
			return Character.isLetter(c);
		}
		@Override
		public boolean isWordPart(char c) {
			return Character.isLetter(c);
		}
	}

	/**
	 * Convenience method for extracting a list of words that should be recognized as 
	 * task labels from an {@link IPreferenceStore} and a backup {@link Preferences}
	 * @param preferenceStore
	 * @param corePreferences
	 * @return a list of words that should be recognized as task labels in the format
	 * expected by TaskTagRule
	 */
	public static String getTaskWords(IPreferenceStore preferenceStore, Preferences corePreferences) {
		String result= null;
		if (preferenceStore.contains(CCorePreferenceConstants.TODO_TASK_TAGS)) {
			result= preferenceStore.getString(CCorePreferenceConstants.TODO_TASK_TAGS);
		} else if (corePreferences != null) {
			result= corePreferences.getString(CCorePreferenceConstants.TODO_TASK_TAGS);
		}
		return result;
	}

	private IToken fToken;

	/**
	 * Creates a new task tag rule
	 * @param token the token to return for words recognized as task tags
	 * @param taskWords a comma delimited list of words to recognize as task tags
	 */
	public TaskTagRule(IToken token, String taskWords) {
		super(new TaskTagDetector(), Token.UNDEFINED);
		fToken= token;
		if( taskWords!= null) {
			addTaskTags(taskWords);
		}
	}

	/**
	 * Removes the current list of words that should be
	 * recognized as task tags.
	 */
	public void clearTaskTags() {
		fWords.clear();
	}

	/**
	 * Adds tags from the specified string as task tags.
	 * @param value a comma delimited list of words to recognize as task tags
	 */
	public void addTaskTags(String value) {
		String[] tasks= value.split(","); //$NON-NLS-1$
		for (int i= 0; i < tasks.length; i++) {
			if (tasks[i].length() > 0) {
				addWord(tasks[i], fToken);
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return event.getProperty().equals(CCorePreferenceConstants.TODO_TASK_TAGS);
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#adaptToPreferenceChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(CCorePreferenceConstants.TODO_TASK_TAGS)) {
			Object value= event.getNewValue();

			if (value instanceof String) {
				clearTaskTags();
				addTaskTags((String) value);
			}
		}
	}
}
