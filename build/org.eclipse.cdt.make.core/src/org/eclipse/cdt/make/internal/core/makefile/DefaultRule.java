/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
