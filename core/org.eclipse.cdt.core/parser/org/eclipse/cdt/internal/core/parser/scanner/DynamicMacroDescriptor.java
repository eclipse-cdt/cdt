/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.parser.IMacroDescriptor;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DynamicMacroDescriptor implements IMacroDescriptor {

	private final String name;
	private final DynamicMacroEvaluator proxy;
	
	public DynamicMacroDescriptor( String name, DynamicMacroEvaluator proxy )
	{
		this.proxy = proxy;
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getMacroType()
	 */
	public MacroType getMacroType() {
		return MacroType.INTERNAL_LIKE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getParameters()
	 */
	public List getParameters() {
		return Collections.EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getTokenizedExpansion()
	 */
	public List getTokenizedExpansion() {
		return Collections.EMPTY_LIST;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getCompleteSignature()
	 */
	public String getCompleteSignature() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#getExpansionSignature()
	 */
	public String getExpansionSignature() {
		return proxy.execute();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#compatible(org.eclipse.cdt.core.parser.IMacroDescriptor)
	 */
	public boolean compatible(IMacroDescriptor descriptor) {
		if( getMacroType() != descriptor.getMacroType() ) return false;
		if( !name.equals( descriptor.getName() )) return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IMacroDescriptor#isCircular()
	 */
	public boolean isCircular() {
		return false;
	}

}
