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
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;

/**
 *   Here is the syntax of a static pattern rule:
 *
 *    TARGETS ...: TARGET-PATTERN: DEP-PATTERNS ...
 *            COMMANDS
 *            ...
 */
public class StaticTargetRule extends InferenceRule implements IInferenceRule {

	String targetPattern;
	String[] prereqPatterns;

	public StaticTargetRule(Directive parent, Target target, String targetPattern, String[] prereqPatterns,
			Command[] commands) {
		super(parent, target, commands);
		this.targetPattern = targetPattern;
		this.prereqPatterns = prereqPatterns.clone();
	}

	public String[] getPrerequisitePatterns() {
		return prereqPatterns.clone();
	}

	public void setPrerequisitePatterns(String[] prereqs) {
		prereqPatterns = prereqs.clone();
	}

	public String getTargetPattern() {
		return targetPattern;
	}

	public void setTargetPattern(String target_pattern) {
		targetPattern = target_pattern;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(getTarget()).append(':');
		String pattern = getTargetPattern();
		if (pattern != null && pattern.length() > 0) {
			buffer.append(' ').append(targetPattern);
		}
		buffer.append(':');
		for (int i = 0; i < prereqPatterns.length; i++) {
			buffer.append(' ').append(prereqPatterns[i]);
		}
		buffer.append('\n');
		ICommand[] cmds = getCommands();
		for (int i = 0; i < cmds.length; i++) {
			buffer.append(cmds[i].toString());
		}
		return buffer.toString();
	}
}
