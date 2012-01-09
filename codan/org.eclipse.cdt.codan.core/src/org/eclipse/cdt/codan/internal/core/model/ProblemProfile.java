/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.model.IProblemProfileChangeListener;
import org.eclipse.cdt.codan.core.model.ProblemProfileChangeEvent;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

/**
 * @author Alena
 * 
 */
public class ProblemProfile implements IProblemProfile, Cloneable {
	private CodanProblemCategory rootCategory;
	private Object resource;

	/**
	 * @param resource
	 */
	public ProblemProfile(Object resource) {
		rootCategory = new CodanProblemCategory("root", "root"); //$NON-NLS-1$ //$NON-NLS-2$
		rootCategory.setProfile(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemProfile#getProblem(java.lang
	 * .String)
	 */
	@Override
	public IProblem findProblem(String id) {
		return CodanProblemCategory.findProblem(getRoot(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemProfile#getProblems()
	 */
	@Override
	public IProblem[] getProblems() {
		Collection<IProblem> problems = new ArrayList<IProblem>();
		collectProblems(getRoot(), problems);
		return problems.toArray(new IProblem[problems.size()]);
	}

	/**
	 * @param root
	 * @param problems
	 */
	protected void collectProblems(IProblemCategory parent, Collection<IProblem> problems) {
		Object[] children = parent.getChildren();
		for (Object object : children) {
			if (object instanceof IProblemCategory) {
				IProblemCategory cat = (IProblemCategory) object;
				collectProblems(cat, problems);
			} else if (object instanceof IProblem) {
				problems.add((IProblem) object);
			}
		}
	}

	@Override
	public IProblemCategory getRoot() {
		return rootCategory;
	}

	public void addProblem(IProblem p, IProblemCategory cat) {
		if (cat == null)
			cat = getRoot();
		((CodanProblemCategory) cat).addChild(p);
	}

	@Override
	public IProblemCategory findCategory(String id) {
		return CodanProblemCategory.findCategory(getRoot(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			ProblemProfile clone = (ProblemProfile) super.clone();
			clone.rootCategory = (CodanProblemCategory) this.rootCategory.clone();
			CodanProblemCategory cce = clone.rootCategory;
			boolean fro = cce.isFrozen();
			cce.setFrozen(false);
			cce.setProfile(this);
			cce.setFrozen(fro);
			return clone;
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}

	/**
	 * @param p
	 * @param cat
	 */
	public void addCategory(IProblemCategory category, IProblemCategory parent) {
		((CodanProblemCategory) parent).addChild(category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemElement#getProfile()
	 */
	@Override
	public IProblemProfile getProfile() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemElement#getParentCategory()
	 */
	@Override
	public IProblemCategory getParentCategory() {
		return getRoot();
	}
	private ListenerList preferenceChangeListeners;

	/**
	 * @param listener
	 */
	@Override
	public void addProfileChangeListener(IProblemProfileChangeListener listener) {
		if (preferenceChangeListeners == null)
			preferenceChangeListeners = new ListenerList();
		preferenceChangeListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	@Override
	public void removeProfileChangeListener(IProblemProfileChangeListener listener) {
		if (preferenceChangeListeners == null)
			return;
		preferenceChangeListeners.remove(listener);
		if (preferenceChangeListeners.size() == 0)
			preferenceChangeListeners = null;
	}

	/*
	 * Convenience method for notifying preference change listeners.
	 */
	protected void fireProfileChangeEvent(String key, Object oldValue, Object newValue) {
		if (preferenceChangeListeners == null)
			return;
		Object[] listeners = preferenceChangeListeners.getListeners();
		final ProblemProfileChangeEvent event = new ProblemProfileChangeEvent(this, this.resource, key, oldValue, newValue);
		for (int i = 0; i < listeners.length; i++) {
			final IProblemProfileChangeListener listener = (IProblemProfileChangeListener) listeners[i];
			ISafeRunnable job = new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					// already logged in Platform#run()
				}

				@Override
				public void run() throws Exception {
					listener.profileChange(event);
				}
			};
			SafeRunner.run(job);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemProfile#getResource()
	 */
	@Override
	public Object getResource() {
		return resource;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}
}
