/********************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight (IBM)  - [283033] remoteFileTypes extension point should include "xml" type
 ********************************************************************************/


package org.eclipse.rse.services.clientserver;

import java.io.File;


public interface ISystemFileTypes  
{
    public boolean isBinary(File file);
    public boolean isText(File file);

    /**
	 * @since 3.2
	 */
    public boolean isXML(File file);
    public boolean isBinary(String file);

    public boolean isText(String file);

    /**
	 * @since 3.2
	 */
    public boolean isXML(String file);
} 