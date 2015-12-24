/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - modified for Automake editor usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.File;

import org.eclipse.cdt.make.core.makefile.ICommand;

public class Else extends Conditional implements IAutomakeConditional, ICommand {

	private boolean isAutomake;
	private Rule[] rules;
	
	public Else(Directive parent) {
		super(parent);
	}

	@Override
	public boolean isAutomake() {
		return isAutomake;
	}
	
	@Override
	public Rule[] getRules() {
		if (rules != null)
			return rules.clone();
		return rules;
	}

	@Override
	public void setRules(Rule[] rules) {
		if (rules != null)
			this.rules = rules.clone();
		else
			this.rules = rules;
	}
	
	@Override
	public void setAutomake(boolean value) {
		isAutomake = value;
	}
	
	@Override
	public boolean isElse() {
		return true;
	}

	@Override
	public String toString() {
		return GNUMakefileConstants.CONDITIONAL_ELSE;
	}
	
	// ICommand methods so Automake else can be a child of an IRule
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
