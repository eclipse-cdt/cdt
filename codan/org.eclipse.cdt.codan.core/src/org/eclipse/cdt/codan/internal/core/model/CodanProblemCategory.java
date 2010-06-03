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
import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;
import org.eclipse.cdt.codan.core.model.IProblemElement;

public class CodanProblemCategory implements IProblemCategory, Cloneable {
	private String id;
	private String name;
	private ArrayList<IProblemElement> list = new ArrayList<IProblemElement>();

	public CodanProblemCategory(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}

	public IProblemElement[] getChildren() {
		return list.toArray(new IProblemElement[list.size()]);
	}

	public void addChild(IProblemElement p) {
		list.add(p);
	}

	public static IProblem findProblem(IProblemCategory c, String id) {
		Object[] children = c.getChildren();
		for (Object object : children) {
			if (object instanceof IProblemCategory) {
				IProblemCategory cat = (IProblemCategory) object;
				IProblem found = findProblem(cat, id);
				if (found != null) return found;
			} else if (object instanceof IProblem) {
				IProblem p = (IProblem) object;
				if (p.getId().equals(id)) return p;
			}
		}
		return null;
	}

	public static IProblemCategory findCategory(IProblemCategory cat, String id) {
		if (cat.getId().equals(id)) return cat;
		Object[] children = cat.getChildren();
		for (Object object : children) {
			if (object instanceof IProblemCategory) {
				IProblemCategory cat2 = (IProblemCategory) object;
				IProblemCategory found = findCategory(cat2, id);
				if (found != null) return found;
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
		try {
			CodanProblemCategory clone = (CodanProblemCategory) super.clone();
			clone.list = new ArrayList<IProblemElement>();
			for (Iterator<IProblemElement> iterator = this.list.iterator(); iterator.hasNext();) {
				IProblemElement child = iterator.next();
				clone.list.add((IProblemElement) child.clone());
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			return this;
		}
	}
}
