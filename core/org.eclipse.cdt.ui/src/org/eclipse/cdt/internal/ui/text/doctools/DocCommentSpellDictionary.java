/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import java.net.URL;

import org.eclipse.core.runtime.Assert;

import org.eclipse.cdt.ui.text.doctools.IDocCommentSimpleDictionary;

import org.eclipse.cdt.internal.ui.text.spelling.engine.AbstractSpellDictionary;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellDictionary;

/**
 * Adapter from interim public {@link IDocCommentSimpleDictionary} to internal {@link ISpellDictionary}
 */
public class DocCommentSpellDictionary extends AbstractSpellDictionary {

	protected IDocCommentSimpleDictionary fDict;
	
	/**
	 * @param dict
	 */
	public DocCommentSpellDictionary(IDocCommentSimpleDictionary dict) {
		Assert.isNotNull(dict);
		fDict= dict;
	}
	
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
		unload();

		String[] words= fDict.getAdditionalWords();
		for(int i=0; i<words.length; i++)
			hashWord(words[i]);
		
		return false;
	}
}
