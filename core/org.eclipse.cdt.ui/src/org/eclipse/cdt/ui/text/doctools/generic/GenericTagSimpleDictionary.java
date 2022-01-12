/*******************************************************************************
 * Copyright (c) 2008, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.generic;

import org.eclipse.cdt.ui.text.doctools.IDocCommentSimpleDictionary;

/**
 * An implementation of a simple dictionary to allow the spelling engine
 * to not flag documentation tool tags.
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GenericTagSimpleDictionary implements IDocCommentSimpleDictionary {
	protected String[] fTags;

	/**
	 * @param tags the tags that should be recognized as correct
	 * @param tagMarkers the characters that may delimit the start of a tag
	 */
	public GenericTagSimpleDictionary(GenericDocTag[] tags, char[] tagMarkers) {
		fTags = new String[tags.length];

		for (int j = 0; j < tags.length; j++) {
			fTags[j] = tags[j].getTagName();
		}
	}

	/**
	 * @return an array of non-null words to be added to the dictionary when spell-checking
	 */
	@Override
	public String[] getAdditionalWords() {
		return fTags;
	}
}
