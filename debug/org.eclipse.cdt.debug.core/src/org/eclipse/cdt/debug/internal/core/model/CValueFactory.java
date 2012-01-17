/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;

/**
 * The value factory for variable and expressions.
 */
public class CValueFactory {

	static public final ICValue NULL_VALUE = new ICValue() {

		@Override
		public String getReferenceTypeName() throws DebugException {
			return ""; //$NON-NLS-1$
		}

		@Override
		public String getValueString() throws DebugException {
			return ""; //$NON-NLS-1$
		}

		@Override
		public boolean isAllocated() throws DebugException {
			return true;
		}

		@Override
		public IVariable[] getVariables() throws DebugException {
			return new IVariable[0];
		}

		@Override
		public boolean hasVariables() throws DebugException {
			return false;
		}

		@Override
		public String getModelIdentifier() {
			return CDebugCorePlugin.getUniqueIdentifier();
		}

		@Override
		public IDebugTarget getDebugTarget() {
			return null;
		}

		@Override
		public ILaunch getLaunch() {
			return null;
		}

		@Override
		public Object getAdapter( Class adapter ) {
			return null;
		}

		@Override
		public ICType getType() throws DebugException {
			return null;
		}

		@Override
		public String evaluateAsExpression( ICStackFrame frame ) {
			return ""; //$NON-NLS-1$
		}

		@Override
		public CDebugElementState getState() {
			return CDebugElementState.UNDEFINED;
		}

		@Override
		public Object getCurrentStateInfo() {
			return null;
		}
		
	};

	static public CValue createValue( CVariable parent, ICDIValue cdiValue ) {
		if ( cdiValue instanceof ICDIFloatingPointValue ) {
			return new CFloatingPointValue( parent, cdiValue );
		}
		return new CValue( parent, cdiValue );
	}

	static public CIndexedValue createIndexedValue( AbstractCVariable parent, ICDIArrayValue cdiValue, int start, int length ) {
		return new CIndexedValue( parent, cdiValue, start, length );
	}

	static public CValue createGlobalValue( CVariable parent, ICDIValue cdiValue ) {
		return new CGlobalValue( parent, cdiValue );
	}

	static public ICValue createValueWithError( CVariable parent, String message ) {
		return new CValue( parent, message );
	}
}
