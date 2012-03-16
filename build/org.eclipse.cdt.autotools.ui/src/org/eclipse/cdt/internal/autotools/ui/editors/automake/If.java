/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - modified to be If class
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.File;
import java.io.IOException;

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

	public Rule[] getRules() {
		if (rules != null)
			return rules.clone();
		return null;
	}
	
	public void setRules(Rule[] rules) {
		if (rules != null)
			this.rules = rules.clone();
		else
			this.rules = rules;
	}
	
	public boolean isIf() {
		return true;
	}

	public boolean isAutomake() {
		return true;
	}
	
	public void setAutomake(boolean value) {
		// ignore value
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.CONDITIONAL_IF);
		sb.append(' ').append(getVariable());
		return sb.toString();
	}

	public String getVariable() {
		return getConditional();
	}
	
	// ICommand methods so Automake if can be a child of an IRule
	public Process execute(String shell, String[] envp, File dir)
			throws IOException {
		return null;
	}
	
	public boolean shouldBeSilent() {
		return false;
	}
	
	public boolean shouldIgnoreError() {
		return false;
	}
	
	public boolean shouldExecute() {
		return false;
	}
}
