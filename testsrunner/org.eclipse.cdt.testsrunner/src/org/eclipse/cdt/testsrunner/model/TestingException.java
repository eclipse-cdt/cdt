/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;


/**
 * Represents a failure in the Tests Runner operations.
 */
public class TestingException extends Exception {

    /**
     * Constructs an exception with the given descriptive message.
     *
     * @param msg Description of the occurred exception.
     */
    public TestingException(String msg) {
        super(msg);
    }

}
