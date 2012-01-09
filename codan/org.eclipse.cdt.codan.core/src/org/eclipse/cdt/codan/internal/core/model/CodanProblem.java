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
import org.eclipse.cdt.codan.core.model.IProblemMultiple;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.model.ProblemProfileChangeEvent;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;

/**
 * A type of problems reported by Codan.
 */
public class CodanProblem extends CodanProblemElement implements IProblemWorkingCopy, Cloneable, IProblemMultiple {
	private String id;
	private String name;
	private String messagePattern;
	private CodanSeverity severity = CodanSeverity.Warning;
	private boolean enabled = true;
	private IProblemPreference rootPreference;
	private String description;
	private String markerType = IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE;
	private boolean multiple;

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	@Override
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
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setSeverity(CodanSeverity sev) {
		if (sev == null)
			throw new NullPointerException();
		checkSet();
		this.severity = sev;
		notifyChanged(ProblemProfileChangeEvent.PROBLEM_KEY);
	}

	@Override
	public void setEnabled(boolean checked) {
		checkSet();
		this.enabled = checked;
		notifyChanged(ProblemProfileChangeEvent.PROBLEM_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		CodanProblem prob;
		prob = (CodanProblem) super.clone();
		if (rootPreference != null) {
			prob.rootPreference = (IProblemPreference) rootPreference.clone();
		}
		return prob;
	}

	@Override
	public void setPreference(IProblemPreference value) {
		if (value == null)
			throw new NullPointerException();
		rootPreference = value;
		notifyChanged(ProblemProfileChangeEvent.PROBLEM_PREF_KEY);
	}

	@Override
	public IProblemPreference getPreference() {
		return rootPreference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getMessagePattern()
	 */
	@Override
	public String getMessagePattern() {
		return messagePattern;
	}

	/**
	 * @param messagePattern
	 *        the message to set
	 */
	@Override
	public void setMessagePattern(String messagePattern) {
		checkSet();
		this.messagePattern = messagePattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getDescription()
	 */
	@Override
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
	@Override
	public void setDescription(String desc) {
		checkSet();
		this.description = desc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblem#getMarkerType()
	 */
	@Override
	public String getMarkerType() {
		return markerType;
	}

	/**
	 * Sets the marker id for the problem.
	 * 
	 * @param markerType
	 */
	public void setMarkerType(String markerType) {
		checkSet();
		this.markerType = markerType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemMultiple#isMultiple()
	 */
	@Override
	public boolean isMultiple() {
		return multiple;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.model.IProblemMultiple#isOriginal()
	 */
	@Override
	public boolean isOriginal() {
		return !id.contains(CheckersRegistry.CLONE_SUFFIX);
	}
}
