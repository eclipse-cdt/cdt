/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.generic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStore;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;

import org.eclipse.cdt.internal.ui.text.TaskTagRule;

/**
 * ICTokenScanner which recognizes a specified set of tags, starting with a specified name.
 * It is assumed this will be used within a single-line or multi-line comment context.
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GenericTagCommentScanner extends BufferedRuleBasedScanner implements ICTokenScanner {
	protected TaskTagRule fTaskTagRule;
	protected Preferences fCorePreferenceStore;
	protected String fDefaultTokenProperty;
	protected String fTagToken;

	protected GenericDocTag[] fTags;
	protected char[] fTagMarkers;
	protected ITokenStore fTokenStore;

	/**
	 * @param tags the tags to be recognized and highlighted by this scanner
	 * @param tagMarkers the character prefixes that denote the start of a tag
	 * @param tokenStoreFactory the token store factory used to store tokens
	 * @param docToken the token id associated with the enclosing comment
	 * @param tagToken the token id associated with highlighting any recognized tags
	 */
	public GenericTagCommentScanner(GenericDocTag[] tags, char[] tagMarkers, ITokenStoreFactory tokenStoreFactory, String docToken, String tagToken) {
		Assert.isNotNull(tags);
		Assert.isNotNull(tagMarkers);
		
		fTags= tags;
		fTagMarkers= tagMarkers;
		fTagToken= tagToken;

		fTokenStore= tokenStoreFactory.createTokenStore(mkArray(docToken, tagToken));
		fDefaultTokenProperty= docToken;

		setRules(createRules());
	}

	/*
	 * @see org.eclipse.jface.text.rules.RuleBasedScanner#nextToken()
	 */
	@Override
	public IToken nextToken() {
		fTokenStore.ensureTokensInitialised();
		return super.nextToken();
	}

	/**
	 * @return the rules to use in this scanner
	 */
	protected IRule[] createRules() {
		List<IRule> result= new ArrayList<IRule>();

		class TagDetector implements IWordDetector {
			public boolean isWordStart(char c) {
				for (int i= 0; i < fTagMarkers.length; i++)
					if (fTagMarkers[i] == c)
						return true;
				return false;
			}
			public boolean isWordPart(char c) {
				return c == '.' || Character.isJavaIdentifierPart(c);
			}
		}

		setDefaultReturnToken(fTokenStore.getToken(fDefaultTokenProperty));
		WordRule wr= new WordRule(new TagDetector(), fDefaultReturnToken);
		for (int i= 0; i < fTags.length; i++) {
			String wd= fTags[i].getTagName();
			for (int j= 0; j < fTagMarkers.length; j++) {
				wr.addWord(fTagMarkers[j] + wd, fTokenStore.getToken(fTagToken));
			}
		}
		result.add(wr);

		// Add rule for Task Tags.
		fTaskTagRule= new TaskTagRule(fTokenStore.getToken(PreferenceConstants.EDITOR_TASK_TAG_COLOR),
				fDefaultReturnToken, fTokenStore.getPreferenceStore(), fCorePreferenceStore);
		result.add(fTaskTagRule);

		return result.toArray(new IRule[result.size()]);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractJavaScanner#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return fTaskTagRule.affectsBehavior(event) || fTokenStore.affectsBehavior(event);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractJavaScanner#adaptToPreferenceChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (fTokenStore.affectsBehavior(event)) {
			fTokenStore.adaptToPreferenceChange(event);
		}
		fTaskTagRule.adaptToPreferenceChange(event);
	}
	
	private static String[] mkArray(String defaultTokenProperty, String tagToken) {
		return new String[] { defaultTokenProperty, tagToken, PreferenceConstants.EDITOR_TASK_TAG_COLOR };
	}
}
