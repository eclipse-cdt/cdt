/*******************************************************************************
 * Copyright (c) 2013 Sebastian Bauer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Bauer - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.core.makefile.IAutomaticVariable;
import org.eclipse.cdt.make.internal.core.makefile.Directive;


/**
 * Represents an automatic variable. Automatic variables are implicit and
 * computed for each rule that is applied.
 * 
 * @author Sebastian Bauer <mail@sebastianbauer.info>
 * @see "http://www.gnu.org/software/make/manual/make.html#Automatic-Variables"
 */
public class AutomaticVariable extends VariableDefinition implements IAutomaticVariable {

	public AutomaticVariable(Directive parent, String name, String description) {
		super(parent, name, new StringBuffer(description));
	}

	@Override
	public boolean isAutomatic() {
		return true;
	}
}
