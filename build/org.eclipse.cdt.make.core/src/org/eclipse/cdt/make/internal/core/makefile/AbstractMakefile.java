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

public abstract class AbstractMakefile {

	public AbstractMakefile() {
	}

	public abstract Statement[] getStatements();
	public abstract void addStatement(Statement statement);

	public Rule[] getRules() {
		Statement[] stmts = getStatements();
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof Rule) {
				array.add(stmts[i]);
			}
		}
		return (Rule[]) array.toArray(new Rule[0]);
	}

	public Rule getRule(String target) {
		Rule[] rules = getRules();
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].getTarget().equals(target)) {
				return rules[i];
			}
		}
		return null;
	}

	public InferenceRule[] getInferenceRules() {
		Rule[] rules = getRules();
		List array = new ArrayList(rules.length);
		for (int i = 0; i < rules.length; i++) {
			if (rules[i] instanceof InferenceRule) {
				array.add(rules[i]);
			}
		}
		return (InferenceRule[]) array.toArray(new InferenceRule[0]);
	}

	public InferenceRule getInferenceRule(String target) {
		InferenceRule[] irules = getInferenceRules();
		for (int i = 0; i < irules.length; i++) {
			if (irules[i].getTarget().equals(target)) {
				return irules[i];
			}
		}
		return null;
	}

	public TargetRule[] getTargetRules() {
		Rule[] rules = getRules();
		List array = new ArrayList(rules.length);
		for (int i = 0; i < rules.length; i++) {
			if (rules[i] instanceof TargetRule) {
				array.add(rules[i]);
			}
		}
		return (TargetRule[]) array.toArray(new TargetRule[0]);
	}

	public TargetRule getTargetRule(String target) {
		TargetRule[] trules = getTargetRules();
		for (int i = 0; i < trules.length; i++) {
			if (trules[i].getTarget().equals(target)) {
				return trules[i];
			}
		}
		return null;
	}

	public MacroDefinition[] getMacroDefinitions() {
		Statement[] stmts = getStatements();
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof MacroDefinition) {
				array.add(stmts[i]);
			}
		}
		return (MacroDefinition[]) array.toArray(new MacroDefinition[0]);
	}

	public MacroDefinition getMacroDefinition(String name) {
		MacroDefinition[] variables = getMacroDefinitions();
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].getName().equals(name)) {
				return variables[i];
			}
		}
		return null;
	}

	public void addStatements(Statement[] stmts) {
		for (int i = 0; i < stmts.length; i++) {
			addStatement(stmts[i]);
		}
	}
}
