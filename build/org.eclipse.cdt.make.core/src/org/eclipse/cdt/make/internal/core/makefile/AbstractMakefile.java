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
 *     Marc-Andre Laperle (Ericsson) - Prevent StackOverflowError (Bug 430966)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IBuiltinFunction;
import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IRule;
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
	private URI filename;

	public AbstractMakefile(Directive parent) {
		super(parent);
	}

	@Override
	public abstract IDirective[] getBuiltins();

	@Override
	public IBuiltinFunction[] getBuiltinFunctions() {
		return new IBuiltinFunction[0];
	}

	@Override
	public IRule[] getRules() {
		IDirective[] stmts = getDirectives(true);
		List<IDirective> array = new ArrayList<>(stmts.length);
		for (IDirective stmt : stmts) {
			if (stmt instanceof IRule) {
				array.add(stmt);
			}
		}
		return array.toArray(new IRule[0]);
	}

	@Override
	public IRule[] getRules(String target) {
		IRule[] rules = getRules();
		List<IRule> array = new ArrayList<>(rules.length);
		for (IRule rule : rules) {
			if (rule.getTarget().equals(target)) {
				array.add(rule);
			}
		}
		return array.toArray(new IRule[0]);
	}

	@Override
	public IInferenceRule[] getInferenceRules() {
		IRule[] rules = getRules();
		List<IRule> array = new ArrayList<>(rules.length);
		for (IRule rule : rules) {
			if (rule instanceof IInferenceRule) {
				array.add(rule);
			}
		}
		return array.toArray(new IInferenceRule[0]);
	}

	@Override
	public IInferenceRule[] getInferenceRules(String target) {
		IInferenceRule[] irules = getInferenceRules();
		List<IInferenceRule> array = new ArrayList<>(irules.length);
		for (IInferenceRule irule : irules) {
			if (irule.getTarget().equals(target)) {
				array.add(irule);
			}
		}
		return array.toArray(new IInferenceRule[0]);
	}

	@Override
	public ITargetRule[] getTargetRules() {
		IRule[] trules = getRules();
		List<IRule> array = new ArrayList<>(trules.length);
		for (IRule trule : trules) {
			if (trule instanceof ITargetRule) {
				array.add(trule);
			}
		}
		return array.toArray(new ITargetRule[0]);
	}

	@Override
	public ITargetRule[] getTargetRules(String target) {
		ITargetRule[] trules = getTargetRules();
		List<ITargetRule> array = new ArrayList<>(trules.length);
		for (ITargetRule trule : trules) {
			if (trule.getTarget().equals(target)) {
				array.add(trule);
			}
		}
		return array.toArray(new ITargetRule[0]);
	}

	@Override
	public IMacroDefinition[] getMacroDefinitions() {
		IDirective[] stmts = getDirectives(true);
		List<IDirective> array = new ArrayList<>(stmts.length);
		for (IDirective stmt : stmts) {
			if (stmt instanceof IMacroDefinition) {
				array.add(stmt);
			}
		}
		return array.toArray(new IMacroDefinition[0]);
	}

	@Override
	public IMacroDefinition[] getMacroDefinitions(String name) {
		IMacroDefinition[] variables = getMacroDefinitions();
		List<IMacroDefinition> array = new ArrayList<>(variables.length);
		for (IMacroDefinition variable : variables) {
			if (variable.getName().equals(name)) {
				array.add(variable);
			}
		}
		return array.toArray(new IMacroDefinition[0]);
	}

	@Override
	public IMacroDefinition[] getBuiltinMacroDefinitions() {
		IDirective[] stmts = getBuiltins();
		List<IDirective> array = new ArrayList<>(stmts.length);
		for (IDirective stmt : stmts) {
			if (stmt instanceof IMacroDefinition) {
				array.add(stmt);
			}
		}
		return array.toArray(new IMacroDefinition[0]);
	}

	@Override
	public IMacroDefinition[] getBuiltinMacroDefinitions(String name) {
		IMacroDefinition[] variables = getBuiltinMacroDefinitions();
		List<IMacroDefinition> array = new ArrayList<>(variables.length);
		for (IMacroDefinition variable : variables) {
			if (variable.getName().equals(name)) {
				array.add(variable);
			}
		}
		return array.toArray(new IMacroDefinition[0]);
	}

	public IInferenceRule[] getBuiltinInferenceRules() {
		IDirective[] stmts = getBuiltins();
		List<IDirective> array = new ArrayList<>(stmts.length);
		for (IDirective stmt : stmts) {
			if (stmt instanceof IInferenceRule) {
				array.add(stmt);
			}
		}
		return array.toArray(new IInferenceRule[0]);
	}

	public IInferenceRule[] getBuiltinInferenceRules(String target) {
		IInferenceRule[] irules = getBuiltinInferenceRules();
		List<IInferenceRule> array = new ArrayList<>(irules.length);
		for (IInferenceRule irule : irules) {
			if (irule.getTarget().equals(target)) {
				array.add(irule);
			}
		}
		return array.toArray(new IInferenceRule[0]);
	}

	@Override
	public String expandString(String line) {
		return expandString(line, false);
	}

	@Override
	public String expandString(String line, boolean recursive) {
		return expandString(line, recursive, new HashSet<String>());
	}

	/**
	 * @param line
	 *            - line to expand
	 * @param expandedMacros
	 *            - keep track of expanded macros to prevent infinite recursion.
	 *
	 * @return line after expanding any macros.
	 */
	private String expandString(String line, boolean recursive, HashSet<String> expandedMacros) {
		int len = line.length();
		boolean foundDollar = false;
		boolean inMacro = false;
		StringBuilder buffer = new StringBuilder();
		StringBuilder macroName = new StringBuilder();
		for (int i = 0; i < len; i++) {
			char c = line.charAt(i);
			switch (c) {
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
							if (result.indexOf('$') != -1 && recursive && !expandedMacros.contains(result)) {
								String prevResult = result;
								expandedMacros.add(prevResult);
								result = expandString(result, recursive, expandedMacros);
								expandedMacros.remove(prevResult);
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
						if (result.indexOf('$') != -1 && recursive && !expandedMacros.contains(result)) {
							String prevResult = result;
							expandedMacros.add(prevResult);
							result = expandString(result, recursive, expandedMacros);
							expandedMacros.remove(prevResult);
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

	@Override
	public URI getFileURI() {
		return filename;
	}

	public void setFileURI(URI filename) {
		this.filename = filename;
	}

	@Override
	public IMakefile getMakefile() {
		return this;
	}
}
