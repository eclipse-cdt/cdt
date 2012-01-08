/*******************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.cdt.internal.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.ui.IPropertyChangeParticipant;

/**
 * Which words should be recognized as task tags is specified under {@link CCorePreferenceConstants#TODO_TASK_TAGS} as a
 * comma delimited list.
 * 
 * @see CCorePreferenceConstants#TODO_TASK_TAGS
 * @since 5.0
 */
public final class TaskTagRule extends CombinedWordRule implements IPropertyChangeParticipant {	
	private static class TaskTagDetector implements IWordDetector {
		@Override
		public boolean isWordStart(char c) {
			return c == '@' || c == '\\' || Character.isJavaIdentifierStart(c);
		}

		@Override
		public boolean isWordPart(char c) {
			return c == '.' || Character.isJavaIdentifierPart(c);
		}
	}

	private class TaskTagMatcher extends WordMatcher {

		private IToken fToken;
		/**
		 * Uppercase words
		 */
		private Map<CharacterBuffer, IToken> fUppercaseWords= new HashMap<CharacterBuffer, IToken>();
		/**
		 * <code>true</code> if task tag detection is case-sensitive.
		 */
		private boolean fCaseSensitive= true;
		/**
		 * Buffer for uppercase word
		 */
		private CharacterBuffer fBuffer= new CharacterBuffer(16);

		public TaskTagMatcher(IToken token) {
			fToken= token;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.CombinedWordRule.WordMatcher#clearWords()
		 */
		@Override
		public synchronized void clearWords() {
			super.clearWords();
			fUppercaseWords.clear();
		}

		public synchronized void addTaskTags(String value) {
			String[] taskTags= value.split(","); //$NON-NLS-1$
			for (String tag : taskTags) {
				if (tag.length() > 0) {
					addWord(tag, fToken);
				}
			}
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CombinedWordRule.WordMatcher#addWord(java.lang.String, org.eclipse.jface.text.rules.IToken)
		 */
		@Override
		public synchronized void addWord(String word, IToken token) {
			Assert.isNotNull(word);
			Assert.isNotNull(token);

			super.addWord(word, token);
			fUppercaseWords.put(new CharacterBuffer(word.toUpperCase()), token);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CombinedWordRule.WordMatcher#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, org.eclipse.jdt.internal.ui.text.CombinedWordRule.CharacterBuffer)
		 */
		@Override
		public synchronized IToken evaluate(ICharacterScanner scanner, CharacterBuffer word) {
			if (fCaseSensitive)
				return super.evaluate(scanner, word);

			fBuffer.clear();
			for (int i= 0, n= word.length(); i < n; i++)
				fBuffer.append(Character.toUpperCase(word.charAt(i)));

			IToken token= fUppercaseWords.get(fBuffer);
			if (token != null)
				return token;
			return Token.UNDEFINED;
		}

		/**
		 * Enables/disables the case-sensitivity of the task tag detection.
		 *
		 * @param caseSensitive <code>true</code> iff case-sensitivity should be enabled
		 */
		public void setCaseSensitive(boolean caseSensitive) {
			fCaseSensitive= caseSensitive;
		}
	}

	private static final String TODO_TASK_TAGS= CCorePreferenceConstants.TODO_TASK_TAGS;
	private static final String TODO_TASK_CASE_SENSITIVE= CCorePreferenceConstants.TODO_TASK_CASE_SENSITIVE;
	private TaskTagMatcher fMatcher;

	/**
	 * Creates a new task tag rule
	 * @param token the token to return for words recognized as task tags
	 * @param defaultToken 
	 * @param preferenceStore
	 * @param corePreferenceStore
	 */
	public TaskTagRule(IToken token, IToken defaultToken, IPreferenceStore preferenceStore,
			IPreferenceStore corePreferenceStore) {
		super(new TaskTagDetector(), defaultToken);
		fMatcher = new TaskTagMatcher(token);
		addWordMatcher(fMatcher);
		String taskWords= null;
		if (preferenceStore.contains(TODO_TASK_TAGS)) {
			taskWords= preferenceStore.getString(TODO_TASK_TAGS);
		} else if (corePreferenceStore != null) {
			taskWords= corePreferenceStore.getString(TODO_TASK_TAGS);
		}
		if (taskWords != null) {
			addTaskTags(taskWords);
		}

		boolean isCaseSensitive= true;
		if (preferenceStore.contains(TODO_TASK_CASE_SENSITIVE)) {
			isCaseSensitive= preferenceStore.getBoolean(TODO_TASK_CASE_SENSITIVE);
		} else if (corePreferenceStore != null) {
			isCaseSensitive= corePreferenceStore.getBoolean(TODO_TASK_CASE_SENSITIVE);
		}
		fMatcher.setCaseSensitive(isCaseSensitive);
	}

	/**
	 * Removes the current list of words that should be
	 * recognized as task tags.
	 */
	public void clearTaskTags() {
		fMatcher.clearWords();
	}

	/**
	 * Adds tags from the specified string as task tags.
	 * @param value a comma delimited list of words to recognize as task tags
	 */
	public void addTaskTags(String value) {
		fMatcher.addTaskTags(value);
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return event.getProperty().equals(TODO_TASK_TAGS) || event.getProperty().equals(TODO_TASK_CASE_SENSITIVE);
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#adaptToPreferenceChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(TODO_TASK_TAGS)) {
			Object value= event.getNewValue();
			if (value instanceof String) {
				synchronized (fMatcher) {
					fMatcher.clearWords();
					fMatcher.addTaskTags((String) value);
				}
			}
		} else if (event.getProperty().equals(TODO_TASK_CASE_SENSITIVE)) {
			Object value= event.getNewValue();
			if (value instanceof String)
				fMatcher.setCaseSensitive(Boolean.parseBoolean((String) value));
		}
	}
}
