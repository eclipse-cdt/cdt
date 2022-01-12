/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
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
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.codan.core.param.IProblemPreference;

/**
 * Modifiable problem.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProblemWorkingCopy extends IProblem {
	/**
	 * Set severity for this this problem instance. Severity can only be changed
	 * in profile not by checker when printing problems.
	 *
	 * @param sev - codan severity
	 */
	void setSeverity(CodanSeverity sev);

	/**
	 * Sets checker enablement.
	 *
	 * @param enabled - true if problem is enabled in profile
	 */
	void setEnabled(boolean enabled);

	/**
	 * Sets default message pattern. UI would call this method if user does not
	 * like default settings, checker should not use method, default message
	 * pattern should be set in checker extension
	 *
	 * @param messagePattern - java style message patter, e.g.
	 *        "Variable {0} is never used".
	 */
	void setMessagePattern(String messagePattern);

	/**
	 * Sets value for the checker parameter, checker may set value during
	 * initialization only, which would the default. User control this values
	 * through ui later.
	 *
	 * @param pref - preference to set
	 *
	 */
	public void setPreference(IProblemPreference pref);

	/**
	 * Sets problem description
	 *
	 * @param desc - problem description - short version, but longer than name
	 */
	public void setDescription(String desc);
}
