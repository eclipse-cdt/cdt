/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *********************************************************************************/

package org.eclipse.cdt.core.dom.lrparser.lpgextensions;

/**
 * Provides trial, undo, and final actions for the
 * TrialUndoBacktrackingParser.
 */
public interface ITrialUndoActionProvider<RULE_DATA> {
	/**
	 * Invokes the trial action that corresponds to the given rule number.
	 */
	boolean trialAction(int ruleNumber);

	/**
	 * Invokes the undo action that corresponds to the given rule number.
	 */
	void undoAction(int ruleNumber);

	/**
	 * Invokes the final action that corresponds to the given rule number.
	 */
	void finalAction(int ruleNumber);

	/**
	 * Sets the given Rule as the active rule for this provider.
	 */
	void setActiveRule(Rule<RULE_DATA> rule);

	/**
	 * Returns the active rule for this provider.
	 */
	Rule<RULE_DATA> getActiveRule();
}
