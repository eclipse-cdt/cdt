/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.codan.core.model.CodanProblem;
import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;

/**
 * @author Alena
 * 
 */
public class CodanPreferencesLoader {
	private static String LIST_SEP = ",";
	private IProblemProfile baseModel;

	/**
	 * @param workspaceProfile
	 */
	public CodanPreferencesLoader(IProblemProfile profile) {
		setInput(profile);
	}

	/**
	 * 
	 */
	public CodanPreferencesLoader() {
	}

	public void setInput(Object model) {
		baseModel = (IProblemProfile) model;
	}

	/**
	 * Stored as element=true|false,...
	 * 
	 * @param stringList
	 * @return
	 */
	public Object modelFromString(String stringList) {
		String[] arr = stringList.split(LIST_SEP);
		for (int i = 0; i < arr.length; i++) {
			String elem = arr[i];
			String[] pair = elem.split("=", 2);
			if (pair.length == 0)
				continue;
			String id = pair[0];
			IProblem p = problemFromString(id);
			if (p == null) {
				System.err.println("cannot find '" + id + "'");
				continue;
			}
			if (pair.length == 1) {
				((CodanProblem) p).setEnabled(true);
			} else {
				String check = pair[1];
				Boolean c = Boolean.valueOf(check);
				((CodanProblem) p).setEnabled(c);
			}
		}
		return baseModel;
	}

	protected String problemToString(Object element) {
		IProblem p = ((IProblem) element);
		return p.getId() + ":" + p.getSeverity();
	}

	protected IProblem problemFromString(String string) {
		String[] pair = string.split(":");
		if (pair.length == 0)
			return null;
		String id = pair[0];
		String arg = "";
		if (pair.length > 1) {
			arg = pair[1];
		}
		CodanSeverity sev;
		try {
			sev = CodanSeverity.valueOf(arg);
		} catch (RuntimeException e) {
			sev = CodanSeverity.Warning;
		}
		IProblem prob = baseModel.findProblem(id);
		if (prob instanceof CodanProblem) {
			((CodanProblem) prob).setSeverity(sev);
		}
		return prob;
	}

	/**
	 * Combines the given list of items into a single string. This method is the
	 * converse of <code>parseString</code>.
	 * <p>
	 * Subclasses may implement this method.
	 * </p>
	 * 
	 * @return the combined string
	 * @see #parseString
	 */
	public String modelToString(Object model) {
		StringBuffer buf = new StringBuffer();
		Map<Object, Boolean> map = fillChecked(model,
				new HashMap<Object, Boolean>());
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			Object element = iterator.next();
			buf.append(problemToString(element));
			buf.append('=');
			buf.append(map.get(element));
			if (iterator.hasNext())
				buf.append(LIST_SEP);
		}
		return buf.toString();
	}

	/**
	 * @param input
	 * @param hashMap
	 * @return
	 */
	private Map<Object, Boolean> fillChecked(Object element,
			HashMap<Object, Boolean> hashMap) {
		if (element instanceof IProblemProfile) {
			IProblemProfile profile = (IProblemProfile) element;
			IProblem[] problems = profile.getProblems();
			for (IProblem iProblem : problems) {
				hashMap.put(iProblem, iProblem.isEnabled());
			}
		}
		return hashMap;
	}
}
