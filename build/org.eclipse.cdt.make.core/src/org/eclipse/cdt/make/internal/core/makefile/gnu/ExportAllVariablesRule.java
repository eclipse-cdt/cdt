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

import org.eclipse.cdt.make.core.makefile.gnu.IExportAllVariablesRule;
import org.eclipse.cdt.make.internal.core.makefile.Command;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.SpecialRule;
import org.eclipse.cdt.make.internal.core.makefile.Target;

/**
 * .EXPORT_ALL_VARIABLES
 *  Simply by being mentioned as a target, this tells `make' to export
 *  all variables to child processes by default.
 */
public class ExportAllVariablesRule extends SpecialRule implements IExportAllVariablesRule {

	public ExportAllVariablesRule(Directive parent, String[] reqs) {
		super(parent, new Target(GNUMakefileConstants.RULE_EXPORT_ALL_VARIABLES), reqs, new Command[0]);
	}

}
