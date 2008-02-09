/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.cdt.ui.text.TaskTagRule;

/**
 * Default token-scanner used for plain (non-documentation-comment) single and multi-line comments, with awareness of
 * task tags.
 */
public class CCommentScanner extends AbstractCScanner {
	private static String TASK_TAG_KEY= PreferenceConstants.EDITOR_TASK_TAG_COLOR;
	private Preferences fCorePreferenceStore;
		
	public CCommentScanner(ITokenStoreFactory tokenStoreFactory, String defaultTokenProperty) {
		this(tokenStoreFactory, null, defaultTokenProperty, new String[] { defaultTokenProperty, TASK_TAG_KEY });
	}

	public CCommentScanner(ITokenStoreFactory tokenStoreFactory, Preferences coreStore, String defaultTokenProperty) {
		this(tokenStoreFactory, coreStore, defaultTokenProperty, new String[] { defaultTokenProperty, TASK_TAG_KEY });
	}

	private CCommentScanner(ITokenStoreFactory tokenStoreFactory, Preferences coreStore, String defaultTokenProperty, String[] tokenProperties) {
		super(tokenStoreFactory.createTokenStore(tokenProperties));
		fCorePreferenceStore= coreStore;
		setRules(createRules(defaultTokenProperty));
	}

	 protected List<IRule> createRules(String defaultTokenProperty) {
		 setDefaultReturnToken(getToken(defaultTokenProperty));
		 IPreferenceStore store= fTokenStore.getPreferenceStore();
		 String taskWords= TaskTagRule.getTaskWords(store, fCorePreferenceStore);
		 TaskTagRule taskTagRule= new TaskTagRule(getToken(TASK_TAG_KEY), taskWords);
		 addPropertyChangeParticipant(taskTagRule);
		 return Collections.singletonList((IRule)taskTagRule);
	 }
}
