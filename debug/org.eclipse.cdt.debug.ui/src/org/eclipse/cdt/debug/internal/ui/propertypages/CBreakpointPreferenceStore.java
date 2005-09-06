/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.propertypages;

import java.util.HashMap;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A preference store that presents the state of the properties of a C/C++ breakpoint. Default settings are not supported.
 */
public class CBreakpointPreferenceStore implements IPreferenceStore {

	protected final static String ENABLED = "ENABLED"; //$NON-NLS-1$

	protected final static String CONDITION = "CONDITION"; //$NON-NLS-1$

	protected final static String IGNORE_COUNT = "IGNORE_COUNT"; //$NON-NLS-1$

	protected HashMap fProperties;

	private boolean fIsDirty = false;

	private ListenerList fListeners;

	/**
	 * Constructor for CBreakpointPreferenceStore.
	 */
	public CBreakpointPreferenceStore() {
		fProperties = new HashMap( 3 );
		fListeners = new ListenerList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(IPropertyChangeListener)
	 */
	public void addPropertyChangeListener( IPropertyChangeListener listener ) {
		fListeners.add( listener );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(String)
	 */
	public boolean contains( String name ) {
		return fProperties.containsKey( name );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(String, Object, Object)
	 */
	public void firePropertyChangeEvent( String name, Object oldValue, Object newValue ) {
		Object[] listeners = fListeners.getListeners();
		if ( listeners.length > 0 && (oldValue == null || !oldValue.equals( newValue )) ) {
			PropertyChangeEvent pe = new PropertyChangeEvent( this, name, oldValue, newValue );
			for( int i = 0; i < listeners.length; ++i ) {
				IPropertyChangeListener l = (IPropertyChangeListener)listeners[i];
				l.propertyChange( pe );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(String)
	 */
	public boolean getBoolean( String name ) {
		Object b = fProperties.get( name );
		if ( b instanceof Boolean ) {
			return ((Boolean)b).booleanValue();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(String)
	 */
	public boolean getDefaultBoolean( String name ) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(String)
	 */
	public double getDefaultDouble( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(String)
	 */
	public float getDefaultFloat( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(String)
	 */
	public int getDefaultInt( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(String)
	 */
	public long getDefaultLong( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(String)
	 */
	public String getDefaultString( String name ) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(String)
	 */
	public double getDouble( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(String)
	 */
	public float getFloat( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(String)
	 */
	public int getInt( String name ) {
		Object i = fProperties.get( name );
		if ( i instanceof Integer ) {
			return ((Integer)i).intValue();
		}
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(String)
	 */
	public long getLong( String name ) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(String)
	 */
	public String getString( String name ) {
		Object str = fProperties.get( name );
		if ( str instanceof String ) {
			return (String)str;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(String)
	 */
	public boolean isDefault( String name ) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return fIsDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(String, String)
	 */
	public void putValue( String name, String newValue ) {
		Object oldValue = fProperties.get( name );
		if ( oldValue == null || !oldValue.equals( newValue ) ) {
			fProperties.put( name, newValue );
			setDirty( true );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(IPropertyChangeListener)
	 */
	public void removePropertyChangeListener( IPropertyChangeListener listener ) {
		fListeners.remove( listener );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(String, double)
	 */
	public void setDefault( String name, double value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(String, float)
	 */
	public void setDefault( String name, float value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(String, int)
	 */
	public void setDefault( String name, int value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(String, long)
	 */
	public void setDefault( String name, long value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(String, String)
	 */
	public void setDefault( String name, String defaultObject ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(String, boolean)
	 */
	public void setDefault( String name, boolean value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(String)
	 */
	public void setToDefault( String name ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(String, double)
	 */
	public void setValue( String name, double value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(String, float)
	 */
	public void setValue( String name, float value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(String, int)
	 */
	public void setValue( String name, int newValue ) {
		int oldValue = getInt( name );
		if ( oldValue != newValue ) {
			fProperties.put( name, new Integer( newValue ) );
			setDirty( true );
			firePropertyChangeEvent( name, new Integer( oldValue ), new Integer( newValue ) );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(String, long)
	 */
	public void setValue( String name, long value ) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(String, String)
	 */
	public void setValue( String name, String newValue ) {
		Object oldValue = fProperties.get( name );
		if ( oldValue == null || !oldValue.equals( newValue ) ) {
			fProperties.put( name, newValue );
			setDirty( true );
			firePropertyChangeEvent( name, oldValue, newValue );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(String, boolean)
	 */
	public void setValue( String name, boolean newValue ) {
		boolean oldValue = getBoolean( name );
		if ( oldValue != newValue ) {
			fProperties.put( name, Boolean.valueOf( newValue ) );
			setDirty( true );
			firePropertyChangeEvent( name, Boolean.valueOf( oldValue ), Boolean.valueOf( newValue ) );
		}
	}

	protected void setDirty( boolean isDirty ) {
		fIsDirty = isDirty;
	}
}
