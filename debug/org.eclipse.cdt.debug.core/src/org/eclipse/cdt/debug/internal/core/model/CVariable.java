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
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * Represents a variable in the CDI model.
 */
public abstract class CVariable extends AbstractCVariable implements ICDIEventListener {

	interface IInternalVariable {
		IInternalVariable createShadow( int start, int length ) throws DebugException;
		IInternalVariable createShadow( String type ) throws DebugException;
		CType getType() throws DebugException;
		String getQualifiedName() throws DebugException;
		ICValue getValue() throws DebugException;
		void setValue( String expression ) throws DebugException;
		boolean isChanged();
		void setChanged( boolean changed );
		void dispose( boolean destroy );
		boolean isSameDescriptor( ICDIVariableDescriptor desc );
		boolean isSameVariable( ICDIVariable cdiVar );
		void resetValue();
		boolean isEditable() throws DebugException;
		boolean isArgument();
		int sizeof();
		void invalidateValue();
		void preserve();
	}

	/**
	 * Whether this variable is currently enabled.
	 */
	private boolean fIsEnabled = true;

	/**
	 * The original internal variable.
	 */
	private IInternalVariable fOriginal;

	/**
	 * The shadow internal variable used for casting.
	 */
	private IInternalVariable fShadow;

	/**
	 * The name of this variable.
	 */
	private String fName;

	/**
	 * The current format of this variable.
	 */
	private CVariableFormat fFormat = CVariableFormat.getFormat( CDebugCorePlugin.getDefault().getPluginPreferences().getInt( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT ) );

	/**
	 * Whether this variable has been disposed.
	 */
	private boolean fIsDisposed = false;

	/**
	 * Constructor for CVariable.
	 */
	protected CVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject ) {
		super( parent );
		if ( cdiVariableObject != null ) {
			setName( cdiVariableObject.getName() );
			createOriginal( cdiVariableObject );
		}
		fIsEnabled = ( parent instanceof AbstractCValue ) ? ((AbstractCValue)parent).getParentVariable().isEnabled() : !isBookkeepingEnabled();
		getCDISession().getEventManager().addEventListener( this );
	}

	/**
	 * Constructor for CVariable.
	 */
	protected CVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject, String errorMessage ) {
		super( parent );
		if ( cdiVariableObject != null ) {
			setName( cdiVariableObject.getName() );
			createOriginal( cdiVariableObject );
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus( ICDebugElementStatus.ERROR, MessageFormat.format( CoreModelMessages.getString( "CVariable.1" ), new String[]{ errorMessage } ) ); //$NON-NLS-1$
		getCDISession().getEventManager().addEventListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getType()
	 */
	public ICType getType() throws DebugException {
		if ( isDisposed() )
			return null;
		IInternalVariable iv = getCurrentInternalVariable();
		return ( iv != null ) ? iv.getType() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isEnabled()
	 */
	public boolean isEnabled() {
		return fIsEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#setEnabled(boolean)
	 */
	public void setEnabled( boolean enabled ) throws DebugException {
		IInternalVariable iv = getOriginal();
		if ( iv != null )
			iv.dispose( true );
		iv = getShadow();
		if ( iv != null )
			iv.dispose( true );
		fIsEnabled = enabled;
		fireChangeEvent( DebugEvent.STATE );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable() {
		return !( getParent() instanceof IValue );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#isArgument()
	 */
	public boolean isArgument() {
		IInternalVariable iv = getOriginal();
		return ( iv != null ) ? iv.isArgument() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException {
		if ( !isDisposed() && isEnabled() ) {
			IInternalVariable iv = getCurrentInternalVariable();
			if ( iv != null ) {
				try {
					return iv.getValue();
				}
				catch( DebugException e ) {
					setStatus( ICDebugElementStatus.ERROR, e.getMessage() );
				}
			}
		}
		return CValueFactory.NULL_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		ICType type = getType();
		return ( type != null ) ? type.getName() : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
	 */
	public boolean hasValueChanged() throws DebugException {
		if ( isDisposed() )
			return false;
		IInternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.isChanged() : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#supportsFormatting()
	 */
	public boolean supportsFormatting() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#getFormat()
	 */
	public CVariableFormat getFormat() {
		return fFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.IFormatSupport#changeFormat(org.eclipse.cdt.debug.core.model.CVariableFormat)
	 */
	public void changeFormat( CVariableFormat format ) throws DebugException {
		setFormat( format );
		resetValue();
	}

	/*
	 * (non-Javadoc)
	 * Allow this operation only for the pointer types (???).
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#canCastToArray()
	 */
	public boolean canCastToArray() {
		ICType type;
		try {
			type = getType();
			return ( getOriginal() != null && isEnabled() && type.isPointer() );
		}
		catch( DebugException e ) {
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToArray#castToArray(int, int)
	 */
	public void castToArray( int startIndex, int length ) throws DebugException {
		IInternalVariable current = getCurrentInternalVariable();
		if ( current != null ) {
			IInternalVariable newVar = current.createShadow( startIndex, length );
			if ( getShadow() != null )
				getShadow().dispose( true );
			setShadow( newVar );
			// If casting of variable to a type or array causes an error, the status 
			// of the variable is set to "error" and it can't be reset by subsequent castings.
			resetValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
	 */
	public void setValue( String expression ) throws DebugException {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			String newExpression = processExpression( expression );
			iv.setValue( newExpression );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
	 */
	public void setValue( IValue value ) throws DebugException {
		notSupported( CoreModelMessages.getString( "CVariable.3" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification() {
		try {
			return getCurrentInternalVariable().isEditable();
		}
		catch( DebugException e ) {
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
	 */
	public boolean verifyValue( String expression ) throws DebugException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.IValue)
	 */
	public boolean verifyValue( IValue value ) throws DebugException {
		return value.getDebugTarget().equals( getDebugTarget() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#canCast()
	 */
	public boolean canCast() {
		return ( getOriginal() != null && isEnabled() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#getCurrentType()
	 */
	public String getCurrentType() {
		String typeName = ""; //$NON-NLS-1$
		try {
			typeName = getReferenceTypeName();
		}
		catch( DebugException e ) {
		}
		return typeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#cast(java.lang.String)
	 */
	public void cast( String type ) throws DebugException {
		IInternalVariable current = getCurrentInternalVariable();
		if ( current != null ) {
			IInternalVariable newVar = current.createShadow( type );
			if ( getShadow() != null )
				getShadow().dispose( true );
			setShadow( newVar );
			// If casting of variable to a type or array causes an error, the status 
			// of the variable is set to "error" and it can't be reset by subsequent castings.
			resetValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#restoreOriginal()
	 */
	public void restoreOriginal() throws DebugException {
		IInternalVariable oldVar = getShadow();
		setShadow( null );
		if ( oldVar != null )
			oldVar.dispose( true );
		IInternalVariable iv = getOriginal();
		if ( iv != null )
			iv.invalidateValue();
		// If casting of variable to a type or array causes an error, the status 
		// of the variable is set to "error" and it can't be reset by subsequent castings.
		resetValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICastToType#isCasted()
	 */
	public boolean isCasted() {
		return ( getShadow() != null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents( ICDIEvent[] events ) {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv == null )
			return;
		for( int i = 0; i < events.length; i++ ) {
			ICDIEvent event = events[i];
			ICDIObject source = event.getSource();
			if ( source == null )
				continue;
			ICDITarget target = source.getTarget();
			if ( target.equals( getCDITarget() ) ) {
				if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDIVariable && iv.isSameVariable( (ICDIVariable)source ) ) {
						handleChangedEvent( (ICDIChangedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent ) {
					handleResumedEvent( (ICDIResumedEvent)event );
				}
			}
		}
	}

	private void handleResumedEvent( ICDIResumedEvent event ) {
		boolean changed = false;
		if ( hasErrors() ) {
			resetStatus();
			changed = true;
			IInternalVariable iv = getCurrentInternalVariable();
			if ( iv != null )
				iv.invalidateValue();
		}
		if ( changed )
			fireChangeEvent( DebugEvent.STATE );
	}

	private void handleChangedEvent( ICDIChangedEvent event ) {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			iv.setChanged( true );
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	private IInternalVariable getCurrentInternalVariable() {
		if ( getShadow() != null )
			return getShadow();
		return getOriginal();
	}

	private IInternalVariable getOriginal() {
		return fOriginal;
	}

	protected void setOriginal( IInternalVariable original ) {
		fOriginal = original;
	}

	private IInternalVariable getShadow() {
		return fShadow;
	}

	private void setShadow( IInternalVariable shadow ) {
		fShadow = shadow;
	}

	protected boolean isBookkeepingEnabled() {
		boolean result = false;
		try {
			result = getLaunch().getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING, false );
		}
		catch( CoreException e ) {
		}
		return result;
	}

	abstract protected void createOriginal( ICDIVariableDescriptor vo );

	protected boolean hasErrors() {
		return !isOK();
	}

	protected void setChanged( boolean changed ) {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			iv.setChanged( changed );
		}
	}

	protected void resetValue() {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			resetStatus();
			iv.resetValue();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	private String processExpression( String oldExpression ) throws DebugException {
		return oldExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	public void dispose() {
		// Hack: do not destroy local variables
		internalDispose( false );
		setDisposed( true );
	}

	protected int sizeof() {
		IInternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.sizeof() : -1;
	}

	/**
	 * Compares the original internal variables.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj ) {
		if ( obj instanceof CVariable ) {
			// A disposed copy can be stored in the viewer. 
			// false should be returned to force the viewer to 
			// replace it by a new variable. See bug #115385
			if ( isDisposed() != ((CVariable)obj).isDisposed() )
				return false;
			IInternalVariable iv = getOriginal();
			return ( iv != null ) ? iv.equals( ((CVariable)obj).getOriginal() ) : false;
		}
		return false;
	}

	protected boolean sameVariable( ICDIVariableDescriptor vo ) {
		IInternalVariable iv = getOriginal();
		return ( iv != null && iv.isSameDescriptor( vo ) );
	}

	protected void setFormat( CVariableFormat format ) {
		fFormat = format;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getExpressionString()
	 */
	public String getExpressionString() throws DebugException {
		IInternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.getQualifiedName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#preserve()
	 */
	protected void preserve() {
		resetStatus();
		IInternalVariable iv = getCurrentInternalVariable(); 
		if ( iv != null )
			iv.preserve();
	}

	protected void internalDispose( boolean destroy ) {
		getCDISession().getEventManager().removeEventListener( this );
		IInternalVariable iv = getOriginal();
		if ( iv != null )
			iv.dispose( destroy );
		iv = getShadow();
		if ( iv != null )
			iv.dispose( destroy );
	}
	
	protected boolean isDisposed() {
		return fIsDisposed;
	}

	protected void setDisposed( boolean isDisposed ) {
		fIsDisposed = isDisposed;
	}

	protected void invalidateValue() {
		resetStatus();
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null )
			iv.invalidateValue();
	}
	
	protected void setName( String name ) {
		fName = name;
	}
}
