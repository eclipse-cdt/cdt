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

import org.eclipse.cdt.make.core.makefile.IPreciousRule;

/**
 * .PRECIOUS
 * Prerequisites of this special target shall not be removed if make recieves an
 * asynchronous events.
 */
public class PreciousRule extends SpecialRule implements IPreciousRule {

	public PreciousRule(Directive parent, String[] reqs) {
		super(parent, new Target(MakeFileConstants.RULE_PRECIOUS), reqs, new Command[0]);
	}

}
