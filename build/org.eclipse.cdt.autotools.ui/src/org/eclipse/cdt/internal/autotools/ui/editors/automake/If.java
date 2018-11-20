/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - modified to be If class
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.File;

import org.eclipse.cdt.make.core.makefile.ICommand;

public class If extends Conditional implements IAutomakeConditional, ICommand {
	private static final String EMPTY = ""; //$NON-NLS-1$
	private Rule rules[] = null;

	public If(Directive parent, Rule[] rules, String var) {
		super(parent, var, EMPTY, EMPTY);
		if (rules != null) {
			this.rules = new Rule[rules.length];
			System.arraycopy(rules, 0, this.rules, 0, rules.length);
		}
	}

	@Override
	public Rule[] getRules() {
		if (rules != null)
			return rules.clone();
		return null;
	}

	@Override
	public void setRules(Rule[] rules) {
		if (rules != null)
			this.rules = rules.clone();
		else
			this.rules = rules;
	}

	@Override
	public boolean isIf() {
		return true;
	}

	@Override
	public boolean isAutomake() {
		return true;
	}

	@Override
	public void setAutomake(boolean value) {
		// ignore value
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(GNUMakefileConstants.CONDITIONAL_IF);
		sb.append(' ').append(getVariable());
		return sb.toString();
	}

	public String getVariable() {
		return getConditional();
	}

	// ICommand methods so Automake if can be a child of an IRule
	@Override
	public Process execute(String shell, String[] envp, File dir) {
		return null;
	}

	@Override
	public boolean shouldBeSilent() {
		return false;
	}

	@Override
	public boolean shouldIgnoreError() {
		return false;
	}

	@Override
	public boolean shouldExecute() {
		return false;
	}
}
