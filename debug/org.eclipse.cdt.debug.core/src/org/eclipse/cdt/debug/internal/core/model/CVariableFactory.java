/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core.model; 

import java.text.MessageFormat;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Provides factory methods for the variable types.
 */
public class CVariableFactory {

	public static CVariable createVariable( CDebugElement parent, ICDIVariableObject cdiVariableObject ) {
		return new CVariable( parent, cdiVariableObject );
	}

	public static CVariable createVariableWithError( CDebugElement parent, ICDIVariableObject cdiVariableObject, String message ) {
		return new CVariable( parent, cdiVariableObject, message );
	}

	public static IGlobalVariableDescriptor createGlobalVariableDescriptor( final String name, final IPath path ) {

		return new IGlobalVariableDescriptor() {

			public String getName() {
				return name;
			}

			public IPath getPath() {
				return path;
			}

			public String toString() {
				return MessageFormat.format( "{0}::{1}", new String[] { getPath().toOSString(), getName() } ); //$NON-NLS-1$
			}
		};
	}

	public static IGlobalVariableDescriptor createGlobalVariableDescriptor( final org.eclipse.cdt.core.model.IVariable var ) {
		IPath path = new Path( "" ); //$NON-NLS-1$
		ICElement parent = var.getParent();
		if ( parent instanceof IBinaryModule ) {
			path = ((IBinaryModule)parent).getPath();
		}
		return createGlobalVariableDescriptor( var.getElementName(), path );
	}

	public static IGlobalVariableDescriptor createGlobalVariableDescriptor(ISymbol symbol) {
		IPath path = new Path( "" ); //$NON-NLS-1$
		IBinaryObject parent = symbol.getBinarObject();
		path = parent.getPath();
		return createGlobalVariableDescriptor( symbol.getName(), path );
		
	}
	
	public static CGlobalVariable createGlobalVariable( CDebugElement parent, IGlobalVariableDescriptor descriptor, ICDIVariableObject cdiVariableObject ) {
		return new CGlobalVariable( parent, descriptor, cdiVariableObject );
	}
}
