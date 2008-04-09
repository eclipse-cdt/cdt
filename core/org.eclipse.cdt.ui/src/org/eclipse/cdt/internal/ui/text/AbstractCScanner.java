/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.ui.IPropertyChangeParticipant;
import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStore;

/**
 * Convenience implementation.
 */
public abstract class AbstractCScanner extends BufferedRuleBasedScanner implements ICTokenScanner {
	private List<IPropertyChangeParticipant> pcps;
	final protected ITokenStore fTokenStore;
	
	public AbstractCScanner(ITokenStore tokenStore, int size) {
		this(tokenStore);
		setBufferSize(size);
	}
	
	public AbstractCScanner(ITokenStore tokenStore) {
		fTokenStore= tokenStore;
		pcps= new ArrayList<IPropertyChangeParticipant>();
	}
	
	protected void addPropertyChangeParticipant(IPropertyChangeParticipant participant) {
		pcps.add(participant);
	}
	
	/**
	 * Convenience method for setting the scanner rules with a list rather
	 * than an array.
	 * @param rules
	 */
	public final void setRules(List<IRule> rules) {
		if(rules==null) {
			setRules((IRule[])null);
		} else {
			IRule[] result= new IRule[rules.size()];
			rules.toArray(result);
			setRules(result);		
		}
	}

	/*
	 * @see org.eclipse.jface.text.rules.RuleBasedScanner#nextToken()
	 */
	@Override
	public IToken nextToken() {
		fTokenStore.ensureTokensInitialised();
		return super.nextToken();
	}
	
	public IToken getToken(String key) {
		return fTokenStore.getToken(key);
	}
	
	public IPreferenceStore getPreferenceStore() {
		return fTokenStore.getPreferenceStore();
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#adaptToPreferenceChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if(fTokenStore.affectsBehavior(event)) {
			fTokenStore.adaptToPreferenceChange(event);
		}
		for(Iterator i= pcps.iterator(); i.hasNext(); ) {
			((IPropertyChangeParticipant)i.next()).adaptToPreferenceChange(event);
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		boolean result= fTokenStore.affectsBehavior(event);
		for(Iterator i= pcps.iterator(); !result && i.hasNext(); ) {
			result |= ((IPropertyChangeParticipant)i.next()).affectsBehavior(event);
		}
		return result;
	}
}