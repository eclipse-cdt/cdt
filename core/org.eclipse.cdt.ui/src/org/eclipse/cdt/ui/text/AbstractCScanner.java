/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.cdt.ui.IPropertyChangeParticipant;

/**
 * Convenience implementation for {@link ICTokenScanner}.
 * Subclasses need to initialize scanner rules by calling {@link #setRules(IRule[])} or {@link #setRules(List)}.
 * <p>
 * Clients may instantiate and extend this class.
 * </p>
 * 
 * @since 5.1
 */
public abstract class AbstractCScanner extends BufferedRuleBasedScanner implements ICTokenScanner {
	private List<IPropertyChangeParticipant> fParticipants;
	final protected ITokenStore fTokenStore;
	
	/**
	 * Create a new scanner for the given token store with default buffer size.
	 * 
	 * @param tokenStore
	 */
	public AbstractCScanner(ITokenStore tokenStore) {
		fTokenStore= tokenStore;
		fParticipants= new ArrayList<IPropertyChangeParticipant>();
	}
	
	/**
	 * Create a new scanner for the given token store and buffer size.
	 * 
	 * @param tokenStore
	 * @param size
	 */
	public AbstractCScanner(ITokenStore tokenStore, int size) {
		this(tokenStore);
		setBufferSize(size);
	}
	
	protected void addPropertyChangeParticipant(IPropertyChangeParticipant participant) {
		fParticipants.add(participant);
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
	@Override
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if(fTokenStore.affectsBehavior(event)) {
			fTokenStore.adaptToPreferenceChange(event);
		}
		for (IPropertyChangeParticipant propertyChangeParticipant : fParticipants) {
			propertyChangeParticipant.adaptToPreferenceChange(event);
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.IPropertyChangeParticipant#affectsBehavior(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public boolean affectsBehavior(PropertyChangeEvent event) {
		boolean result= fTokenStore.affectsBehavior(event);
		for(Iterator<IPropertyChangeParticipant> i= fParticipants.iterator(); !result && i.hasNext(); ) {
			result |= (i.next()).affectsBehavior(event);
		}
		return result;
	}
}
