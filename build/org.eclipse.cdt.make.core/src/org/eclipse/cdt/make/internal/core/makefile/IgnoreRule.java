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

import org.eclipse.cdt.make.core.makefile.IIgnoreRule;

/**
 * .IGNORE
 * Prerequistes of this special target are targets themselves; this shall cause errors
 * from commands associated with them to be ignored in the same manner as
 * specified by the -i option.
 */
public class IgnoreRule extends SpecialRule implements IIgnoreRule {

	public IgnoreRule(Directive parent, String[] reqs) {
		super(parent, new Target(MakeFileConstants.RULE_IGNORE), reqs, new Command[0]);
	}

}
