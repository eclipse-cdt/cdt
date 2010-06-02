/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*
 * This class is meant to be empty.  It enables us to define
 * the annotations which list all the different JUnit class we
 * want to run.  When creating a new test class, it should be
 * added to the list below.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestMIBreakInsertCommand.class,
        TestMICommandConstructCommand.class
        /* Add your test class here */
        })

public class AllTests {}
