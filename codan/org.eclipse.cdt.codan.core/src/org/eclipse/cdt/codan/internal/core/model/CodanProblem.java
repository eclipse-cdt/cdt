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

import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.IProblemPreference;

public class CodanProblem implements IProblemWorkingCopy, Cloneable {
	private String id;
	private String name;
	private String message;
	private CodanSeverity severity = CodanSeverity.Warning;
	private boolean enabled = true;
	private IProblemPreference preference;
	private boolean frozen;
	private String description;
	private String markerType = IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE;

	public CodanSeverity getSeverity() {
		return severity;
	}

	public CodanProblem(String problemId, String name) {
		this.id = problemId;
		this.name = name;
		this.frozen = false;
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
	public Object clone() {
		CodanProblem prob;
		try {
			prob = (CodanProblem) super.clone();
			if (preference != null) {
				prob.preference = (IProblemPreference) preference.clone();
			}
			return prob;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(); // not possible
		}
	}

	public void setPreference(IProblemPreference value) {
		preference = value;
	}

	public IProblemPreference getPreference() {
		return preference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getMessagePattern()
	 */
	public String getMessagePattern() {
		return message;
	}

	protected void freeze() {
		frozen = true;
	}

	/**
	 * @param message
	 *        the message to set
	 */
	public void setMessagePattern(String message) {
		checkSet();
		this.message = message;
	}

	protected void checkSet() {
		if (frozen)
			throw new IllegalStateException("Object is unmodifieble"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IProblemWorkingCopy#setDescription(java
	 * .lang.String)
	 */
	public void setDescription(String desc) {
		this.description = desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getMarkerType()
	 */
	public String getMarkerType() {
		return markerType;
	}

	public void setMarkerType(String type) {
		markerType = type;
	}
}
