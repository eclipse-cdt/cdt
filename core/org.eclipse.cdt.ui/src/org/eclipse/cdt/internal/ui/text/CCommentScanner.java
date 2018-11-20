/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IRule;

/**
 * Default token-scanner used for plain (non-documentation-comment) single and multi-line comments, with awareness of
 * task tags.
 */
public class CCommentScanner extends AbstractCScanner {
	private static String TASK_TAG_KEY = PreferenceConstants.EDITOR_TASK_TAG_COLOR;

	public CCommentScanner(ITokenStoreFactory tokenStoreFactory, String defaultTokenProperty) {
		this(tokenStoreFactory, defaultTokenProperty, new String[] { defaultTokenProperty, TASK_TAG_KEY });
	}

	private CCommentScanner(ITokenStoreFactory tokenStoreFactory, String defaultTokenProperty,
			String[] tokenProperties) {
		super(tokenStoreFactory.createTokenStore(tokenProperties));
		setRules(createRules(defaultTokenProperty));
	}

	protected List<IRule> createRules(String defaultTokenProperty) {
		setDefaultReturnToken(getToken(defaultTokenProperty));
		IPreferenceStore store = fTokenStore.getPreferenceStore();
		TaskTagRule taskTagRule = new TaskTagRule(getToken(TASK_TAG_KEY), fDefaultReturnToken, store, null);
		addPropertyChangeParticipant(taskTagRule);
		return Collections.singletonList((IRule) taskTagRule);
	}
}
