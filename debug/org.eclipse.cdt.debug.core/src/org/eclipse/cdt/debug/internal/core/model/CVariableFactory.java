/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model; 

import java.text.MessageFormat;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Provides factory methods for the variable types.
 */
public class CVariableFactory {

	public static CLocalVariable createLocalVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject ) {
		return new CLocalVariable( parent, cdiVariableObject );
	}

	public static CLocalVariable createLocalVariableWithError( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject, String message ) {
		return new CLocalVariable( parent, cdiVariableObject, message );
	}

	public static IGlobalVariableDescriptor createGlobalVariableDescriptor( final String name, final IPath path ) {
		
		return new IGlobalVariableDescriptor() {

			public String getName() {
				return name;
			}

			public IPath getPath() {
				return ( path != null ) ? path : new Path( "" ); //$NON-NLS-1$
			}

			public String toString() {
				return MessageFormat.format( "{0}::{1}", new String[] { getPath().toOSString(), getName() } ); //$NON-NLS-1$
			}

		    public boolean equals( Object obj ) {
		    	if ( !(obj instanceof IGlobalVariableDescriptor) )
		    		return false;
		    	IGlobalVariableDescriptor d = (IGlobalVariableDescriptor)obj;
		    	return ( getName().compareTo( d.getName() ) == 0 && getPath().equals( d.getPath() ) );
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

	public static IGlobalVariableDescriptor createGlobalVariableDescriptor( ISymbol symbol ) {
		return createGlobalVariableDescriptor( symbol.getName(), symbol.getFilename() );
	}
	
	public static CGlobalVariable createGlobalVariable( CDebugElement parent, IGlobalVariableDescriptor descriptor, ICDIVariableDescriptor cdiVariableObject ) {
		return new CGlobalVariable( parent, descriptor, cdiVariableObject );
	}
}
