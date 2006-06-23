/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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

	public AbstractMakefile(Directive parent) {
		super(parent);
	}

	public abstract IDirective[] getBuiltins();

	public IRule[] getRules() {
		IDirective[] stmts = getDirectives(true);
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IRule) {
				array.add(stmts[i]);
			}
		}
		return (IRule[]) array.toArray(new IRule[0]);
	}

	public IRule[] getRules(String target) {
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

	public IInferenceRule[] getInferenceRules(String target) {
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

	public ITargetRule[] getTargetRules(String target) {
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
		IDirective[] stmts = getDirectives(true);
		List array = new ArrayList(stmts.length);
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] instanceof IMacroDefinition) {
				array.add(stmts[i]);
			}
		}
		return (IMacroDefinition[]) array.toArray(new IMacroDefinition[0]);
	}

	public IMacroDefinition[] getMacroDefinitions(String name) {
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

	public IMacroDefinition[] getBuiltinMacroDefinitions(String name) {
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

	public IInferenceRule[] getBuiltinInferenceRules(String target) {
		IInferenceRule[] irules = getBuiltinInferenceRules();
		List array = new ArrayList(irules.length);
		for (int i = 0; i < irules.length; i++) {
			if (irules[i].getTarget().equals(target)) {
				array.add(irules[i]);
			}
		}
		return (IInferenceRule[]) array.toArray(new IInferenceRule[0]);
	}

	public String expandString(String line) {
		return expandString(line, false);
	}

	public String expandString(String line, boolean recursive) {
		int len = line.length();
		boolean foundDollar = false;
		boolean inMacro = false;
		StringBuffer buffer = new StringBuffer();
		StringBuffer macroName = new StringBuffer();
		for (int i = 0; i < len; i++) {
			char c = line.charAt(i);
			switch(c) {
				case '$':
					// '$$' --> '$'
					if (foundDollar) {
						buffer.append(c);
						foundDollar = false;
					} else {
						foundDollar = true;
					}
					break;
				case '(':
				case '{':
					if (foundDollar) {
						inMacro = true;
					} else {
						buffer.append(c);
					}
					break;
				case ')':
				case '}':
					if (inMacro) {
						String name = macroName.toString();
						if (name.length() > 0) {
							IMacroDefinition[] defs = getMacroDefinitions(name);
							if (defs.length == 0) {
								defs = getBuiltinMacroDefinitions(name);
							}
							if (defs.length > 0) {
								String result = defs[0].getValue().toString();
								if (result.indexOf('$') != -1 && recursive) {
									result = expandString(result, recursive);
								}
								buffer.append(result);
							} else { // Do not expand
								buffer.append('$').append('(').append(name).append(')');
							}
						}
						macroName.setLength(0);
						inMacro = false;
					} else {
						buffer.append(c);
					}
					break;
				default:
					if (inMacro) {
						macroName.append(c);
					} else if (foundDollar) {
						String name = String.valueOf(c);
						IMacroDefinition[] defs = getMacroDefinitions(name);
						if (defs.length == 0) {
							defs = getBuiltinMacroDefinitions(name);
						}
						if (defs.length > 0) {
							String result = defs[0].getValue().toString();
							if (result.indexOf('$') != -1 && recursive) {
								result = expandString(result, recursive);
							}
							buffer.append(result);
						} else {
							// nothing found
							buffer.append('$').append(c);
						}
						inMacro = false;
					} else {
						buffer.append(c);
					}
					foundDollar = false;
					break;
			}
		}
		return buffer.toString();
	}

}
