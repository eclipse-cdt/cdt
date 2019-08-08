/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemProfile;

/**
 * @author Alena
 */
public class ProblemProfile implements IProblemProfile {
	private CodanProblemCategory rootCategory;
	private Object resource;

	/**
	 * @param resource
	 */
	public ProblemProfile(Object resource) {
		rootCategory = new CodanProblemCategory("root", "root"); //$NON-NLS-1$ //$NON-NLS-2$
		rootCategory.setProfile(this);
	}

	@Override
	public IProblem findProblem(String id) {
		return CodanProblemCategory.findProblem(getRoot(), id);
	}

	@Override
	public IProblem[] getProblems() {
		Collection<IProblem> problems = new ArrayList<>();
		collectProblems(getRoot(), problems);
		return problems.toArray(new IProblem[problems.size()]);
	}

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

	public void addCategory(IProblemCategory category, IProblemCategory parent) {
		((CodanProblemCategory) parent).addChild(category);
	}

	@Override
	public IProblemProfile getProfile() {
		return this;
	}

	@Override
	public IProblemCategory getParentCategory() {
		return getRoot();
	}

	@Override
	public Object getResource() {
		return resource;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}
}
