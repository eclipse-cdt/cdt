/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ling Wang (Nokia) - 126262
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;

/**
 * Represents an expression in the CDI model.
 */
public class CExpression extends CLocalVariable implements IExpression {

	private String fText;

	private ICDIExpression fCDIExpression;
	
	private CStackFrame fStackFrame;

	private IValue fValue = CValueFactory.NULL_VALUE;

	private ICType fType;

	/**
	 * Constructor for CExpression.
	 */
	public CExpression( CStackFrame frame, ICDIExpression cdiExpression, ICDIVariableDescriptor varObject ) {
		super( frame, varObject );
		setFormat( CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT ) ) );
		fText = cdiExpression.getExpressionText();
		fCDIExpression = cdiExpression;
		fStackFrame = frame;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getExpressionText()
	 */
	@Override
	public String getExpressionText() {
		return fText;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	@Override
	public void handleDebugEvents( ICDIEvent[] events ) {
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();

			if ( event instanceof ICDIResumedEvent ) {
				if ( source != null ) {
					ICDITarget cdiTarget = source.getTarget();
					if (  getCDITarget().equals( cdiTarget ) ) {
						setChanged( false );
						resetValue();
					}
				}
			}

			if ( event instanceof ICDIChangedEvent ) {
				// If a variable is changed (by user or program),
				// we should re-evaluate expressions.
				// Though it's better we check if the changed variable 
				// is part of the expression, the effort required is not
				// worth the gain.	
				// This is partial fix to bug 126262. It makes CDT behavior 
				// in line with JDT. 
				// The remaining problem (with both CDT & JDT) is: 
				// Due to platform bug, the change will not show up in
				// Expression View until the view is redrawn (e.g. after stepping,
				// or when the view is uncovered from background). In other words,
				// if the Expression View is at the front (not covered) when the
				// variable is changed, the change won't be reflected in the view.
				if ( source instanceof ICDIVariable) {			
					setChanged( false );
					resetValue();
				}
			}
		}
		super.handleDebugEvents( events );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	@Override
	public boolean canEnableDisable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#isBookkeepingEnabled()
	 */
	@Override
	protected boolean isBookkeepingEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IExpression#getValue()
	 */
	@Override
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
		if ( fValue.equals( CValueFactory.NULL_VALUE ) ) {
			if ( context.isSuspended() ) {
				try {
					ICDIValue value = fCDIExpression.getValue( context.getCDIStackFrame() );
					if ( value != null ) {
						if ( value instanceof ICDIArrayValue ) {
							ICType type = null;
							try {
								type = new CType( value.getType() );
							}
							catch( CDIException e ) {
								// ignore and use default type
							}
							if ( type != null && type.isArray() ) {
								int[] dims = type.getArrayDimensions();
								if ( dims.length > 0 && dims[0] > 0 )
									fValue = CValueFactory.createIndexedValue( this, (ICDIArrayValue)value, 0, dims[0] );
							}
						}
						else {
							fValue = CValueFactory.createValue( this, value );
						}
					}
				}
				catch( CDIException e ) {
					targetRequestFailed( e.getMessage(), null );
				}
			}
		}
		return fValue;
	}

	@Override
	protected ICStackFrame getStackFrame() {
		return fStackFrame;
	}

	@Override
	protected void resetValue() {
		if ( fValue instanceof AbstractCValue ) {
			((AbstractCValue)fValue).reset();
			
			// We can't just reset the value and toss the reference we've been
			// holding. Those things have a dispose() method and that needs to be
			// called when there is no further use for it (so debugger-engine
			// objects tied to the value can be freed). We return the AbstractCValue
			// as an IValue to the platform, which means the platform certainly
			// isn't going to dispose of it (there is no IValue.dispose method).
			// Notice that we call AbstractCValue.dispose in our dispose method
			// above. So, naturally, we need to do the same here. But then what is
			// the purpose of calling AbstractCValue.reset() if we just dispose of
			// the object, anyway? This whole thing seems a bit screwy. We should
			// either be holding on to the AbstractCValue and resetting it, or just
			// discarding it. The reset above doesn't hurt, but it sure seems
			// pointless.
			((AbstractCValue) fValue).dispose();
		}
		fValue = CValueFactory.NULL_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#getExpressionString()
	 */
	@Override
	public String getExpressionString() throws DebugException {
		return getExpressionText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	@Override
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
			fValue = CValueFactory.NULL_VALUE;
		}
		internalDispose( true );
		setDisposed( true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getType()
	 */
	@Override
	public ICType getType() throws DebugException {
		if ( isDisposed() )
			return null;
		if ( fType == null ) {
			synchronized( this ) {
				if ( fType == null ) {
					fType = ((ICValue)fValue).getType();
				}
			}
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		ICType type = getType();
		return ( type != null ) ? type.getName() : ""; //$NON-NLS-1$
	}
}
