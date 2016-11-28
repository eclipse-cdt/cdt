/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript

import org.eclipse.cdt.linkerscript.serializer.LinkerScriptSerializer

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class LinkerScriptRuntimeModule extends AbstractLinkerScriptRuntimeModule {

    def override bindIValueConverterService() {
        return LinkerScriptConverters;
    }

    def override bindISerializer() {
		return LinkerScriptSerializer
    }
}
