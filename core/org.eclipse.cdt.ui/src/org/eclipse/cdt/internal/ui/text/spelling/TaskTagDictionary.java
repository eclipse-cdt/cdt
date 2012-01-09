/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.text.spelling.engine.AbstractSpellDictionary;

/**
 * Dictionary for task tags.
 */
public class TaskTagDictionary extends AbstractSpellDictionary implements IPropertyChangeListener {

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#getName()
	 */
	@Override
	protected final URL getURL() {
		return null;
	}

	/*
	 * @see org.eclipse.cdt.ui.text.spelling.engine.AbstractSpellDictionary#load(java.net.URL)
	 */
	@Override
	protected synchronized boolean load(final URL url) {
		final CUIPlugin plugin= CUIPlugin.getDefault();
		if (plugin != null) {
			plugin.getCorePreferenceStore().addPropertyChangeListener(this);
			return updateTaskTags();
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (CCorePreferenceConstants.TODO_TASK_TAGS.equals(event.getProperty()))
			updateTaskTags();
	}

	/*
	 * @see org.eclipse.cdt.ui.text.spelling.engine.ISpellDictionary#unload()
	 */
	@Override
	public synchronized void unload() {
		final CUIPlugin plugin= CUIPlugin.getDefault();
		if (plugin != null)
			plugin.getCorePreferenceStore().removePropertyChangeListener(this);

		super.unload();
	}

	/**
	 * Handles the compiler task tags property change event.
	 * 
	 * @return  <code>true</code> if the task tags got updated
	 */
	protected boolean updateTaskTags() {
		final String tags= CCorePlugin.getOption(CCorePreferenceConstants.TODO_TASK_TAGS);
		if (tags != null) {
			unload();

			final StringTokenizer tokenizer= new StringTokenizer(tags, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens())
				hashWord(tokenizer.nextToken());

			return true;
		}
		return false;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#stripNonLetters(java.lang.String)
	 */
	@Override
	protected String stripNonLetters(String word) {
		return word;
	}
}
