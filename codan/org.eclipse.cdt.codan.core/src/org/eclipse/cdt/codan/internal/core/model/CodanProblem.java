/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core.model;

import org.eclipse.cdt.codan.core.model.CodanSeverity;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.IProblemPreference;

/**
 * A type of problems reported by Codan.
 */
public class CodanProblem implements IProblemWorkingCopy, Cloneable {

	private String id;
	private String name;
	private String messagePattern;
	private CodanSeverity severity = CodanSeverity.Warning;
	private boolean enabled = true;
	private IProblemPreference preference;
	private boolean frozen;
	private String description;
	private String markerType = IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE;
	private String[] exampleParams = { "X", "Y", "Z", "U", "V" }; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$

	public CodanSeverity getSeverity() {
		return severity;
	}

	/**
	 * @param problemId - the ID of the problem
	 * @param name - the name of the problem
	 */
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
		return messagePattern;
	}

	protected void freeze() {
		frozen = true;
	}

	/**
	 * @param messagePattern
	 *            the message to set
	 */
	public void setMessagePattern(String messagePattern) {
		checkSet();
		this.messagePattern = messagePattern;
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

	/**
	 * Sets the marker id for the problem.

	 * @param markerType
	 */
	public void setMarkerType(String markerType) {
		this.markerType = markerType;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getExampleParameters()
	 */
	public String[] getExampleMessageParameters() {
		return exampleParams.clone();
	}

	/**
	 * Sets an example message parameter to use with {@link #getMessagePattern()}.
	 *
	 * @param exampleParameters - the example message parameters to set,
	 *    e.g. { "&lt;variable_name&gt;" }.
	 * @see #getExampleMessageParameters()
	 */
	public void setExampleMessageParameters(String[] exampleParameters) {
		checkSet();
		this.exampleParams = exampleParameters;
	}
}
