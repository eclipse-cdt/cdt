/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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

import org.eclipse.cdt.make.core.makefile.IIgnoreRule;

/**
 * .IGNORE
 * Prerequisites of this special target are targets themselves; this shall cause errors
 * from commands associated with them to be ignored in the same manner as
 * specified by the -i option.
 */
public class IgnoreRule extends SpecialRule implements IIgnoreRule {

	public IgnoreRule(Directive parent, String[] reqs) {
		super(parent, new Target(MakeFileConstants.RULE_IGNORE), reqs, new Command[0]);
	}

}
