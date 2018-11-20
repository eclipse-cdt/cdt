/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
package org.eclipse.cdt.make.internal.core.makefile;

import org.eclipse.cdt.make.core.makefile.IDefaultRule;

/**
 * .DEFAULT
 * If the makefile uses this special target, the application shall ensure that it is
 * specified with commands, but without prerequisites.
 */
public class DefaultRule extends SpecialRule implements IDefaultRule {

	public DefaultRule(Directive parent, Command[] cmds) {
		super(parent, new Target(MakeFileConstants.RULE_DEFAULT), new String[0], cmds);
	}

}
