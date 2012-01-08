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
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemElement;
import org.eclipse.cdt.codan.core.model.IProblemProfile;

/**
 * TODO: add description
 */
public class CodanProblemCategory extends CodanProblemElement implements IProblemCategory, Cloneable {
	private String id;
	private String name;
	private ArrayList<IProblemElement> list = new ArrayList<IProblemElement>();

	public CodanProblemCategory(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public IProblemElement[] getChildren() {
		return list.toArray(new IProblemElement[list.size()]);
	}

	public void addChild(IProblemElement p) {
		list.add(p);
		if (p instanceof CodanProblemElement) {
			CodanProblemElement cce = (CodanProblemElement) p;
			cce.setParentCategory(this);
			cce.setProfile(getProfile());
		}
	}

	public static IProblem findProblem(IProblemCategory c, String id) {
		Object[] children = c.getChildren();
		for (Object object : children) {
			if (object instanceof IProblemCategory) {
				IProblemCategory cat = (IProblemCategory) object;
				IProblem found = findProblem(cat, id);
				if (found != null)
					return found;
			} else if (object instanceof IProblem) {
				IProblem p = (IProblem) object;
				if (p.getId().equals(id))
					return p;
			}
		}
		return null;
	}

	/**
	 * Find all categories in which problem with id present
	 * 
	 * @param c - root category
	 * @param id - problem id
	 * @return list of categories
	 */
	public static IProblemCategory[] findProblemCategories(IProblemCategory c, String id) {
		ArrayList<IProblemCategory> list = new ArrayList<IProblemCategory>();
		Object[] children = c.getChildren();
		for (Object object : children) {
			if (object instanceof IProblemCategory) {
				IProblemCategory cat = (IProblemCategory) object;
				IProblemCategory[] found = findProblemCategories(cat, id);
				if (found.length > 0) {
					list.addAll(Arrays.asList(found));
				}
			} else if (object instanceof IProblem) {
				IProblem p = (IProblem) object;
				if (p.getId().equals(id)) {
					list.add(c);
				}
			}
		}
		return list.toArray(new IProblemCategory[list.size()]);
	}

	public static IProblemCategory findCategory(IProblemCategory cat, String id) {
		if (cat.getId().equals(id))
			return cat;
		Object[] children = cat.getChildren();
		for (Object object : children) {
			if (object instanceof IProblemCategory) {
				IProblemCategory cat2 = (IProblemCategory) object;
				IProblemCategory found = findCategory(cat2, id);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		CodanProblemCategory catClone = (CodanProblemCategory) super.clone();
		catClone.list = new ArrayList<IProblemElement>();
		for (Iterator<IProblemElement> iterator = this.list.iterator(); iterator.hasNext();) {
			IProblemElement child = iterator.next();
			IProblemElement childClone = (IProblemElement) child.clone();
			if (childClone instanceof CodanProblemElement) {
				CodanProblemElement cce = (CodanProblemElement) childClone;
				boolean fro = cce.isFrozen();
				cce.setFrozen(false);
				cce.setParentCategory(catClone);
				cce.setFrozen(fro);
			}
			catClone.list.add(childClone);
		}
		return catClone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.core.model.CodanProblemElement#setProfile
	 * (org.eclipse.cdt.codan.core.model.IProblemProfile)
	 */
	@Override
	public void setProfile(IProblemProfile profile) {
		checkSet();
		super.setProfile(profile);
		for (Iterator<IProblemElement> iterator = this.list.iterator(); iterator.hasNext();) {
			IProblemElement child = iterator.next();
			if (child instanceof CodanProblemElement) {
				((CodanProblemElement) child).setProfile(profile);
			}
		}
	}

	@Override
	public void setFrozen(boolean b) {
		checkSet();
		super.setFrozen(b);
		for (Iterator<IProblemElement> iterator = this.list.iterator(); iterator.hasNext();) {
			IProblemElement child = iterator.next();
			if (child instanceof CodanProblemElement) {
				((CodanProblemElement) child).setFrozen(b);
			}
		}
	}
}
