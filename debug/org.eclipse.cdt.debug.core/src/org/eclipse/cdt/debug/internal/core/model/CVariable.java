/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIMemoryChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration3;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICType;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.cdt.debug.internal.core.CSettingsManager;
import org.eclipse.cdt.debug.internal.core.ICWatchpointTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IValue;

/**
 * A thin wrapper over the CVariable for injection into the CDI event manager's
 * listener collection. We used to directly inject the CVariable, but that's
 * problematic since CVariable overrides the equals() method to base the
 * decision on the internal variable object. So if two CVariables were added to
 * the listener list for the same underlying value, trying to later remove one
 * of the listeners had a 50/50 chance of removing the wrong one.
 * 
 * How can you end up with two CVariables for the same internal variable on the
 * listener list? Easy. 
 * 1. View a register in the Registers view. 
 * 2. Create a custom register group that contains the same register. 
 * 3. Expand the custom register group 
 * 4. Remove the custom register group 
 * Step 4 removed the wrong CVariable from the listener list.
 * 
 */
class VariableEventListener implements ICDIEventListener {
	private CVariable fVar;
	public VariableEventListener(CVariable var) {
		fVar = var;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(org.eclipse.cdt.debug.core.cdi.event.ICDIEvent[])
	 */
	public void handleDebugEvents(ICDIEvent[] events) {
		fVar.handleDebugEvents(events);
	}
}

/**
 * Represents a variable in the CDI model.
 */
public abstract class CVariable extends AbstractCVariable implements ICDIEventListener, ICWatchpointTarget {

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
		
		// Note: the CDI object association can change; e.g., if a "Cast to Type"
		// or "Display as Array" is done on the element.
		ICDIObject getCdiObject();
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
	 * Thin wrapper for instertion into the CDI event manager's listener list
	 */
	private VariableEventListener fEventListenerWrapper;

	/**
	 * Constructor for CVariable.
	 */
	protected CVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject ) {
		super( parent );
		fEventListenerWrapper = new VariableEventListener(this);
		if ( cdiVariableObject != null ) {
			setName( cdiVariableObject.getName() );
			createOriginal( cdiVariableObject );
		}
		fIsEnabled = ( parent instanceof AbstractCValue ) ? ((AbstractCValue)parent).getParentVariable().isEnabled() : !isBookkeepingEnabled();
		getCDISession().getEventManager().addEventListener( fEventListenerWrapper );
		if ( cdiVariableObject != null ) {
			setInitialFormat();
		}
	}

	/**
	 * Constructor for CVariable.
	 */
	protected CVariable( CDebugElement parent, ICDIVariableDescriptor cdiVariableObject, String errorMessage ) {
		super( parent );
		fEventListenerWrapper = new VariableEventListener(this);
		if ( cdiVariableObject != null ) {
			setName( cdiVariableObject.getName() );
			createOriginal( cdiVariableObject );
		}
		fIsEnabled = !isBookkeepingEnabled();
		setStatus( ICDebugElementStatus.ERROR, MessageFormat.format( CoreModelMessages.getString( "CVariable.1" ), (Object[])new String[]{ errorMessage } ) ); //$NON-NLS-1$
		getCDISession().getEventManager().addEventListener( fEventListenerWrapper );
		if ( cdiVariableObject != null ) {
			setInitialFormat();
		}		
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
		// Debugger engines that use active variable objects will benefit
		// performance-wise if we dispose the internal variable when it's 
		// disabled by the user (it will automatically get lazily recreated if 
		// it's ever needed again). Engines using passive variables probably 
		// won't, so we can defer the dispose until we have no use for the 
		// variable altogether.
		boolean disposeVariable = true;
		ICDITargetConfiguration configuration = getParent().getCDITarget().getConfiguration();
		if (configuration instanceof ICDITargetConfiguration2) {
			disposeVariable = !((ICDITargetConfiguration2)configuration).supportsPassiveVariableUpdate();
		}
		if (disposeVariable) {
			IInternalVariable iv = getOriginal();
			if ( iv != null )
				iv.dispose( true );
			iv = getShadow();
			if ( iv != null )
				iv.dispose( true );
		}
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
		storeFormat( format );
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
			return ( getOriginal() != null && isEnabled() && type != null && type.isPointer() );
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
			storeCastToArray( startIndex, length );
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
			return fIsEnabled ? getCurrentInternalVariable().isEditable() : false;
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
			storeCast(type);
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
		forgetCast();
		forgetCastToArray();
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
				if ( event instanceof ICDIMemoryChangedEvent &&
						target.getConfiguration() instanceof ICDITargetConfiguration3 &&
						((ICDITargetConfiguration3)target.getConfiguration()).needsVariablesUpdated(event)) {
					resetValue();
				}
				else if ( event instanceof ICDIChangedEvent ) {
					if ( source instanceof ICDIVariable && iv.isSameVariable( (ICDIVariable)source ) ) {
						handleChangedEvent( (ICDIChangedEvent)event );
					}
				}
				else if ( event instanceof ICDIResumedEvent ) {
					handleResumedEvent( (ICDIResumedEvent)event );
				}
				else if (event instanceof ICDIDestroyedEvent
						&& iv.getCdiObject() == source) {
					dispose();
					fireChangeEvent(DebugEvent.STATE);
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#setChanged(boolean)
	 */
	@Override
    protected void setChanged( boolean changed ) {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			iv.setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#resetValue()
	 */
	@Override
    protected void resetValue() {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			resetStatus();
			iv.resetValue();
			fireChangeEvent( DebugEvent.STATE );
		}
	}

	private String processExpression( String oldExpression ) {
		return oldExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.AbstractCVariable#dispose()
	 */
	@Override
    public void dispose() {
		// Hack: do not destroy local variables
		internalDispose( false );
		setDisposed( true );
	}

	public int sizeof() {
		IInternalVariable iv = getCurrentInternalVariable(); 
		return ( iv != null ) ? iv.sizeof() : -1;
	}

	/**
	 * Compares the original internal variables.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
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
	@Override
    protected void preserve() {
		resetStatus();
		IInternalVariable iv = getCurrentInternalVariable(); 
		if ( iv != null )
			iv.preserve();
	}

	protected void internalDispose( boolean destroy ) {
		getCDISession().getEventManager().removeEventListener( fEventListenerWrapper );
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

	public ICDIObject getCdiObject() {
		IInternalVariable iv = getCurrentInternalVariable();
		if ( iv != null ) {
			return iv.getCdiObject();
		}
		return null;
	}
	
	protected CSettingsManager getFormatManager() {
		return ((CDebugTarget) getDebugTarget()).getFormatManager();
	}
	
	/**
	 * used to concatenate multiple names to a single identifier 
	 */
	private final static String NAME_PART_SEPARATOR = "-"; //$NON-NLS-1$

	/**
	 * suffix used to identify format informations
	 */
	private final static String FORMAT_SUFFIX = NAME_PART_SEPARATOR + "(format)"; //$NON-NLS-1$

	/**
	 * suffix used to identify cast settings
	 */
	private final static String CAST_SUFFIX = NAME_PART_SEPARATOR + "(cast)"; //$NON-NLS-1$

	/**
	 * suffix used to identify cast to array settings
	 */
	private final static String CAST_TO_ARRAY_SUFFIX = NAME_PART_SEPARATOR + "(cast_to_array)";  //$NON-NLS-1$

	/** retrieve the identification for this variable.
	 * @return a string identifying this variable, to be used to store settings
	 * @throws DebugException
	 */
	String getVariableID() throws DebugException {
    	return getName(); // TODO: better identification if multiple variables have the same name
    }
   
    /** helper to generate a string id used to persist the settings.
     * @param next_obj next object to encode into the id
     * @param buf contains the id of the part encoded so far.
     * @throws DebugException
     */
    static private void buildPesistID( CDebugElement next_obj, StringBuffer buf ) throws DebugException {
		if ( next_obj instanceof CVariable ) {
			CVariable cVariableParent = (CVariable) next_obj;
			buf.append( NAME_PART_SEPARATOR );
			buf.append( cVariableParent.getVariableID() );
			buildPesistID( cVariableParent.getParent(), buf );
		} else if ( next_obj instanceof CStackFrame ) {
			buf.append(NAME_PART_SEPARATOR);
			// TODO: better identification if multiple functions have the same name (say for static functions)
			buf.append( ((CStackFrame)next_obj ).getFunction() );
		} else if ( next_obj instanceof CDebugTarget ) {
			// global, we use a root NAME_PART_SEPARATOR as indicator of that
			buf.append( NAME_PART_SEPARATOR );
		} else if ( next_obj instanceof AbstractCValue ) {
			// index or indirection.
			AbstractCValue av = (AbstractCValue) next_obj;
			buildPesistID( av.getParentVariable(), buf );			
		}
	}
	
    /** returns an string used to identify this variable  
	 * @return
	 * @throws DebugException
	 */
	private final String getPersistID() throws DebugException {
		StringBuffer id = new StringBuffer();
		id.append( getVariableID() );
		buildPesistID( getParent(), id );
		return id.toString();
	}

	/** stores the given format
	 * @param format the format to be used for this variable
	 */
	protected void storeFormat( CVariableFormat format ) {
		try {
			String formatString = Integer.toString( format.getFormatNumber() );

			getFormatManager().putValue( getPersistID() + FORMAT_SUFFIX, formatString );
		} catch ( DebugException e ) {
			// if we do not get the name, we use the default format, no reason for the creation to fail too.
			DebugPlugin.log( e );
		}
	}
	
	/** stores the cast information.
	 * @param type the type to be displayed instead
	 */
	protected void storeCast( String type ) {
		try {
			String id = getPersistID() + CAST_SUFFIX;
			getFormatManager().putValue( id, type );
		} catch ( DebugException e ) {
			DebugPlugin.log( e );
		}
	}

	/** drops the cast information.
	 */
	protected void forgetCast() {
		try {
			String id = getPersistID() + CAST_SUFFIX;
			getFormatManager().removeValue( id );
		} catch ( DebugException e ) {
			DebugPlugin.log( e );
		}
	}
		
	/** stores the cast array information.
	 * @param startIndex the first item to be displayed in the cast array operation
	 * @param length the number of elements to display
	 */
	protected void storeCastToArray(int startIndex, int length) {
		try {
			// we persist the information in a (startIndex):(Length) format.
			String content = Integer.toString( startIndex ) + ":" + Integer.toString( length ); //$NON-NLS-1$
			getFormatManager().putValue( getPersistID() + CAST_TO_ARRAY_SUFFIX, content );
		} catch ( DebugException e ) {
			DebugPlugin.log( e );
		}
	}

	/** drops previously stored cast array information.
	 */
	protected void forgetCastToArray() {
		try {
			String id = getPersistID() + CAST_TO_ARRAY_SUFFIX;
			getFormatManager().removeValue( id );
		} catch ( DebugException e ) {
			DebugPlugin.log( e );
		}
	}
		
	/**
	 * restore the format stored previously for this variable.
	 * Only sets explicitly retrieved formats in order to maintain defaults. 
	 */
	protected void setInitialFormat() {
		try {
			String persistID= getPersistID();
			String stringFormat = getFormatManager().getValue( persistID + FORMAT_SUFFIX );
			if ( stringFormat != null ) {
				try {
					CVariableFormat format = CVariableFormat.getFormat( Integer.parseInt( stringFormat ) );
					setFormat( format );
				} catch ( NumberFormatException e ) {
					DebugPlugin.log( e );
				}
			}
			
			if ( canCast() ) {
				String castString = getFormatManager().getValue( persistID + CAST_SUFFIX );
				if ( castString != null ) {
					cast( castString );
				}
			}
			if ( canCastToArray() ) {
				String castToArrayString = getFormatManager().getValue( persistID + CAST_TO_ARRAY_SUFFIX );
				if (castToArrayString != null) {
					int index = castToArrayString.indexOf( ':' );
					if ( index > 0 ) {
						try {
							int beg = Integer.parseInt( castToArrayString.substring( 0, index ) );
							int num = Integer.parseInt( castToArrayString.substring( index + 1 ) );
							castToArray( beg, num );
						} catch ( NumberFormatException e ) {
							DebugPlugin.log( e );
						}
					} else {
						DebugPlugin.logMessage( "did not find expected : for cast to array", null ); //$NON-NLS-1$
					}
				}
			}
		} catch ( DebugException e ) {
			DebugPlugin.log( e );
			// we drop (and log) the exception here.
			// even if the initial setup fails, we still want the complete creation to be successful
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#getExpression()
	 */
	public String getExpression() {
		try {
			return getExpressionString();
		} catch (DebugException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#getSize()
	 */
	public void getSize(ICWatchpointTarget.GetSizeRequest request) {
		// CDI has synchronous APIs, so this is easy...
		request.setSize(sizeof());
		request.done();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#canCreateWatchpoint(org.eclipse.cdt.debug.internal.core.IWatchpointTarget.CanCreateWatchpointRequest)
	 */
	public void canSetWatchpoint(ICWatchpointTarget.CanCreateWatchpointRequest request) {
		// CDI has synchronous APIs, so this is easy...
		request.setCanCreate(sizeof() > 0);
		request.done();
	}
}
