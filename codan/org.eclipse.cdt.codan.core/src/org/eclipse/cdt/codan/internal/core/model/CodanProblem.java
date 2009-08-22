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
package org.eclipse.cdt.codan.internal.core.model;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemCategory;

public class CodanProblem implements IProblem {
	private String id;
	private String name;
	private String message;
	private CodanSeverity severity = CodanSeverity.Warning;
	private boolean enabled = true;
	private HashMap<Object, Object> properties = new HashMap<Object, Object>(0);

	public CodanSeverity getSeverity() {
		return severity;
	}

	public CodanProblem(String problemId, String name) {
		this.id = problemId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public IProblemCategory getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setSeverity(CodanSeverity sev) {
		if (sev == null)
			throw new NullPointerException();
		this.severity = sev;
	}

	public void setEnabled(boolean checked) {
		this.enabled = checked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setProperty(Object key, Object value) {
		properties.put(key, value);
	}

	public Object getProperty(Object key) {
		return properties.get(key);
	};

	public Collection<Object> getPropertyKeys() {
		return properties.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getMessagePattern()
	 */
	public String getMessagePattern() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessagePattern(String message) {
		this.message = message;
	}
}
