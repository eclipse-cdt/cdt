/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.macros;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;


/**
 * This interface represents the given build macro
 * Clients may implement or extend this interface.
 *
 * @since 3.0
 */
public interface IBuildMacro extends ICdtVariable{
	int getMacroValueType();

    /**
     * @throws BuildMacroException if macro holds StringList-type value
     */
    @Override
	String getStringValue() throws BuildMacroException;

    /**
     * @throws BuildMacroException if macro holds single String-type value
     */
    @Override
	String[] getStringListValue() throws BuildMacroException;

}

