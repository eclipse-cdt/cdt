/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;

/**
 * Represents an expression in the CDI model.
 */
public class CExpression extends CVariable implements IExpression {

	private ICDIExpression fCDIExpression;
	
	private CStackFrame fStackFrame;

	private IValue fValue;

	/**
	 * Constructor for CExpression.
	 */
	public CExpression( CStackFrame frame, ICDIExpression cdiExpression, ICDIVariableDescriptor varObject ) {
		super( frame, varObject );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT ))) ;
		fCDIExpression = cdiExpression;
		fStackFrame = frame;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	public String getExpressionText() {
		return fCDIExpression.getExpressionText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			if ( event instanceof ICDIResumedEvent ) {
				ICDIObject source = event.getSource();
				if ( source != null ) {
					ICDITarget cdiTarget = source.getTarget();
					if (  getCDITarget().equals( cdiTarget ) ) {
						setChanged( false );
						resetValue();
					}
				}
			}
		}
		super.handleDebugEvents( events );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#isBookkeepingEnabled()
	 */
	protected boolean isBookkeepingEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getValue()
	 */
	public IValue getValue() {
		CStackFrame frame = (CStackFrame)getStackFrame();
		try {
			return getValue( frame );
		}
		catch( DebugException e ) {
		}
		return null;
	}

	protected synchronized IValue getValue( CStackFrame context ) throws DebugException {
		if ( fValue == null ) {
			if ( context.isSuspended() ) {
				try {
					ICDIValue value = fCDIExpression.getValue( context.getCDIStackFrame() );
					fValue = CValueFactory.createValue( this, value );
				}
				catch( CDIException e ) {
					targetRequestFailed( e.getMessage(), null );
				}
			}
		}
		return fValue;
	}

	protected ICStackFrame getStackFrame() {
		return fStackFrame;
	}

	protected void resetValue() {
		if ( fValue instanceof AbstractCValue ) {
			((AbstractCValue)fValue).reset();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#getExpressionString()
	 */
	public String getExpressionString() throws DebugException {
		return getExpressionText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	public void dispose() {
		if ( fCDIExpression != null ) {
			try {
				fCDIExpression.dispose();
				fCDIExpression = null;
			}
			catch( CDIException e ) {
			}
		}
		if ( fValue instanceof AbstractCValue ) {
			((AbstractCValue)fValue).dispose();
			fValue = null;
		}
		super.dispose();
	}
}