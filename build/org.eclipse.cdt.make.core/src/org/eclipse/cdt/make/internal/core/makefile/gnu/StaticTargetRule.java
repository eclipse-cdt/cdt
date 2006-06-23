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
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.ICommand;
import org.eclipse.cdt.make.core.makefile.IInferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.InferenceRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;


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

	public StaticTargetRule(Directive parent, Target target, String target_pattern, String[] prereq_patterns, Command[] commands) {
		super(parent, target, commands);
		targetPattern = target_pattern;
		prereqPatterns = prereq_patterns;
	}

	public String[] getPrerequisitePatterns() {
		return prereqPatterns;
	}

	public void setPrerequesitePatterns(String[] prereqs) {
		prereqPatterns = prereqs;
	}

	public String getTargetPattern() {
		return targetPattern;
	}

	public void setTargetPattern(String target_pattern) {
		targetPattern = target_pattern;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
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
