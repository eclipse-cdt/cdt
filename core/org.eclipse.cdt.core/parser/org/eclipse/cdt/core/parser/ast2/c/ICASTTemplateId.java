/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.ast2.c;

import java.util.List;

/**
 * @author Doug Schaefer
 */
public interface ICASTTemplateId extends ICASTName {

	/**
	 * @return the name for the template
	 */
	public ICASTName getTemplateName();
	
	/**
	 * TODO define the class hierarchy for the template arguments
	 * 
	 * @return the arguments for the template
	 */
	public List getTemplateArguments();
	
}
