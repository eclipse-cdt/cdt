/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroElement;
import org.eclipse.cdt.autotools.ui.editors.parser.InvalidMacroException;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.autotools.core.VersionComparator;

public class AcInitElement extends AutoconfMacroElement {

	private static final String BAD_VERSION_NUMBER = "AC_INIT_badVersionNumber";
	
	public AcInitElement(String name) {
		super(name);
	}

	@Override
	public void validate(String version) throws InvalidMacroException {
		super.validate(version);
		
		if (this.getChildren().length == 0)
			return;
		
		if (VersionComparator.compare(version,
				AutotoolsPropertyConstants.AC_VERSION_2_59) >= 0){
			if (this.getChildren().length < 2)
				return;
			
			this.validateMultipleArguments();
		}

		return;
	}
	
	private void validateMultipleArguments () throws InvalidMacroException{

		// There are no restrictions on the first argument.

		// Validate second argument (version number).
		AutoconfElement argument = this.getChildren()[1];
		// match a digit followed by a dot zero or more times
		// but always end with a digit
		if (!argument.getName().matches("(\\d*\\.)*((\\d+))")){
			throw new InvalidMacroException(AutoconfEditorMessages.getString(BAD_VERSION_NUMBER), argument);
		}
	}
}
