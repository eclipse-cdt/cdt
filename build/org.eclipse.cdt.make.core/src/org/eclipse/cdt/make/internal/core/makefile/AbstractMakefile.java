/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.ITargetRule;

/**
 * Makefile : ( statement ) *
 * statement :   rule | macro_definition | comments | empty
 * rule :  inference_rule | target_rule
 * inference_rule : target ':' <nl> ( <tab> command <nl> ) +
 * target_rule : target [ ( target ) * ] ':' [ ( prerequisite ) * ] [ ';' command ] <nl> 
                 [ ( command ) * ]
 * macro_definition : string '=' (string)* 
 * comments : ('#' (string) <nl>) *
 * empty : <nl>
 * command : <tab> prefix_command string <nl>
 * target : string
 * prefix_command : '-' | '@' | '+'
 * internal_macro :  "$<" | "$*" | "$@" | "$?" | "$%" 
 */

public abstract class AbstractMakefile extends Parent implements IMakefile {

	public AbstractMakefile() {
	}

	public abstract IDirective[] getBuiltins();

	public IRule[] getRules() {
		IDirective[] stmts = getStatements();
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IRule) {
				array.add(stmts[i]);
			}
		}
		return (IRule[]) array.toArray(new IRule[0]);
	}

	public IRule[] getRule(String target) {
		IRule[] rules = getRules();
		List array = new ArrayList(rules.length);
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].getTarget().equals(target)) {
				array.add(rules[i]);
			}
		}
		return (IRule[]) array.toArray(new IRule[0]);
	}

	public IInferenceRule[] getInferenceRules() {
		IRule[] rules = getRules();
		List array = new ArrayList(rules.length);
		for (int i = 0; i < rules.length; i++) {
			if (rules[i] instanceof IInferenceRule) {
				array.add(rules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public IInferenceRule[] getInferenceRule(String target) {
		IInferenceRule[] irules = getInferenceRules();
		List array = new ArrayList(irules.length);
		for (int i = 0; i < irules.length; i++) {
			if (irules[i].getTarget().equals(target)) {
				array.add(irules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public ITargetRule[] getTargetRules() {
		IRule[] trules = getRules();
		List array = new ArrayList(trules.length);
		for (int i = 0; i < trules.length; i++) {
			if (trules[i] instanceof ITargetRule) {
				array.add(trules[i]);
			}
		}
		return (ITargetRule[]) array.toArray(new ITargetRule[0]);
	}

	public ITargetRule[] getTargetRule(String target) {
		ITargetRule[] trules = getTargetRules();
		List array = new ArrayList(trules.length);
		for (int i = 0; i < trules.length; i++) {
			if (trules[i].getTarget().equals(target)) {
				array.add(trules[i]);
			}
		}
		return (ITargetRule[]) array.toArray(new ITargetRule[0]);
	}

	public IMacroDefinition[] getMacroDefinitions() {
		IDirective[] stmts = getStatements();
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IMacroDefinition) {
				array.add(stmts[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getMacroDefinition(String name) {
		IMacroDefinition[] variables = getMacroDefinitions();
		List array = new ArrayList(variables.length);
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].getName().equals(name)) {
				array.add(variables[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getBuiltinMacroDefinitions() {
		IDirective[] stmts = getBuiltins();
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IMacroDefinition) {
				array.add(stmts[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getBuiltinMacroDefinition(String name) {
		IMacroDefinition[] variables = getBuiltinMacroDefinitions();
		List array = new ArrayList(variables.length);
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].getName().equals(name)) {
				array.add(variables[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IInferenceRule[] getBuiltinInferenceRules() {
		IDirective[] stmts = getBuiltins();
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IInferenceRule) {
				array.add(stmts[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public IInferenceRule[] getBuiltinInferenceRule(String target) {
		IInferenceRule[] irules = getBuiltinInferenceRules();
		List array = new ArrayList(irules.length);
		for (int i = 0; i < irules.length; i++) {
			if (irules[i].getTarget().equals(target)) {
				array.add(irules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

}
