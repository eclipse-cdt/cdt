/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

public interface IAutomakeConditional {

	boolean isAutomake();

	void setAutomake(boolean value);

	Rule[] getRules();

	void setRules(Rule[] rules);
}
