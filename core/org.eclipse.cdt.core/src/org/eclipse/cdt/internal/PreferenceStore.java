package org.eclipse.cdt.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.*;
import java.util.*;
import org.eclipse.cdt.core.resources.*;
//import org.eclipse.jface.util.*;

/**
 * A concrete preference store implementation based on an internal
 * <code>java.util.Properties</code> object, with support for 
 * persisting the non-default preference values to files or streams.
 * <p>
 * This class was not designed to be subclassed.
 * </p>
 * 
 * @see IPreferenceStore
 */
public class PreferenceStore implements IPropertyStore {
	
	
	/**
	 * The mapping from preference name to
	 * preference value (represented as strings).
	 */
	private Properties properties;
	
	/**
	 * The mapping from preference name to
	 * default preference value (represented as strings);
	 * <code>null</code> if none.
	 */
	private Properties defaultProperties;
	
	/**
	 * Indicates whether a value as been changed by <code>setToDefault</code>
	 * or <code>setValue</code>; initially <code>true</code>.
	 */
	private boolean dirty = true;
	
	/**
	 * The file name used by the <code>load</code> method to load a property 
	 * file. This filename is used to save the properties file when <code>save</code>
	 * is called.
	 */
	private String filename;	
/**
 * Creates an empty preference store.
 * <p>
 * Use the methods <code>load(InputStream)</code> and
 * <code>save(InputStream)</code> to load and store this
 * preference store.
 * </p>
 * @see #load(java.io.InputStream)
 * @see #store(java.io.InputStream)
 */
public PreferenceStore() {
	defaultProperties = new Properties();
	properties = new Properties(defaultProperties);
}
/**
 * Creates an empty preference store that loads from and saves to the
 * a file.
 * <p>
 * Use the methods <code>load()</code> and <code>save()</code> to load and store this
 * preference store.
 * </p>
 *
 * @param filename the file name
 * @see #load()
 * @see #store()
 */
public PreferenceStore(String filename) {
	this();
	this.filename = filename;
}

/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public boolean contains(String name) {
	return (properties.containsKey(name) || defaultProperties.containsKey(name));
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public boolean getBoolean(String name) {
	return getBoolean(properties, name);
}
/**
 * Helper function: gets boolean for a given name.
 */
private boolean getBoolean(Properties p, String name) {
	String value = p != null ? p.getProperty(name) : null;
	if (value == null)
		return BOOLEAN_DEFAULT_DEFAULT;
	if (value.equals(IPropertyStore.TRUE))
		return true;
	return false;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public boolean getDefaultBoolean(String name) {
	return getBoolean(defaultProperties, name);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public double getDefaultDouble(String name) {
	return getDouble(defaultProperties, name);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public float getDefaultFloat(String name) {
	return getFloat(defaultProperties, name);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public int getDefaultInt(String name) {
	return getInt(defaultProperties, name);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public long getDefaultLong(String name) {
	return getLong(defaultProperties, name);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public String getDefaultString(String name) {
	return getString(defaultProperties, name);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public double getDouble(String name) {
	return getDouble(properties, name);
}
/**
 * Helper function: gets double for a given name.
 */
private double getDouble(Properties p, String name) {
	String value = p != null ? p.getProperty(name) : null;
	if (value == null)
		return DOUBLE_DEFAULT_DEFAULT;
	double ival = DOUBLE_DEFAULT_DEFAULT;
	try {
		ival = new Double(value).doubleValue();
	} catch (NumberFormatException e) {
	}
	return ival;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public float getFloat(String name) {
	return getFloat(properties, name);
}
/**
 * Helper function: gets float for a given name.
 */
private float getFloat(Properties p, String name) {
	String value = p != null ? p.getProperty(name) : null;
	if (value == null)
		return FLOAT_DEFAULT_DEFAULT;
	float ival = FLOAT_DEFAULT_DEFAULT;
	try {
		ival = new Float(value).floatValue();
	} catch (NumberFormatException e) {
	}
	return ival;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public int getInt(String name) {
	return getInt(properties, name);
}
/**
 * Helper function: gets int for a given name.
 */
private int getInt(Properties p, String name) {
	String value = p != null ? p.getProperty(name) : null;
	if (value == null)
		return INT_DEFAULT_DEFAULT;
	int ival = 0;
	try {
		ival = Integer.parseInt(value);
	} catch (NumberFormatException e) {
	}
	return ival;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public long getLong(String name) {
	return getLong(properties, name);
}
/**
 * Helper function: gets long for a given name.
 */
private long getLong(Properties p, String name) {
	String value = p != null ? p.getProperty(name) : null;
	if (value == null)
		return LONG_DEFAULT_DEFAULT;
	long ival = LONG_DEFAULT_DEFAULT;
	try {
		ival = Long.parseLong(value);
	} catch (NumberFormatException e) {
	}
	return ival;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public String getString(String name) {
	return getString(properties, name);
}
/**
 * Helper function: gets string for a given name.
 */
private String getString(Properties p, String name) {
	String value = p != null ? p.getProperty(name) : null;
	if (value == null)
		return STRING_DEFAULT_DEFAULT;
	return value;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public boolean isDefault(String name) {
	return (!properties.containsKey(name) && defaultProperties.containsKey(name));
}
/**
 * Prints the contents of this preference store to the given print stream.
 *
 * @param out the print stream
 */
public void list(PrintStream out) {
	properties.list(out);
}
/**
 * Prints the contents of this preference store to the given print writer.
 *
 * @param out the print writer
 */
public void list(PrintWriter out) {
	properties.list(out);
}
/**
 * Loads this preference store from the file established in the constructor 
 * <code>PreferenceStore(java.lang.String)</code> (or by <code>setFileName</code>).
 * Default preference values are not affected.
 *
 * @exception java.io.IOException if there is a problem loading this store
 */
public void load() throws IOException {
	if (filename == null)
		throw new IOException("File name not specified");//$NON-NLS-1$
	FileInputStream in = new FileInputStream(filename);
	load(in);
	in.close();
}
/**
 * Loads this preference store from the given input stream. Default preference
 * values are not affected.
 *
 * @param in the input stream
 * @exception java.io.IOException if there is a problem loading this store
 */
public void load(InputStream in) throws IOException {
	properties.load(in);
	dirty = false;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public boolean needsSaving() {
	return dirty;
}
/**
 * Returns an enumeration of all preferences known to this store which
 * have current values other than their default value.
 *
 * @return an array of preference names 
 */
public String[] preferenceNames() {
	ArrayList list = new ArrayList();
	Enumeration enum = properties.propertyNames();
	while (enum.hasMoreElements()) {
		list.add(enum.nextElement());
	}
	return (String[])list.toArray(new String[list.size()]);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void putValue(String name, String value) {
	setValue(properties, name, value);
}

/**
 * Saves the non-default-valued preferences known to this preference
 * store to the file from which they were originally loaded.
 *
 * @exception java.io.IOException if there is a problem saving this store
 */
public void save() throws IOException {
	if (filename == null)
		throw new IOException("File name not specified");//$NON-NLS-1$
	FileOutputStream out = null;
	try {
		out = new FileOutputStream(filename);
		save(out, null);
	} finally {
		if (out != null)
			out.close();
	}
}
/**
 * Saves this preference store to the given output stream. The
 * given string is inserted as header information.
 *
 * @param out the output stream
 * @param header the header
 * @exception java.io.IOException if there is a problem saving this store
 */
public void save(OutputStream out, String header) throws IOException {
	properties.store(out, header);
	dirty = false;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setDefault(String name, double value) {
	setValue(defaultProperties, name, value);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setDefault(String name, float value) {
	setValue(defaultProperties, name, value);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setDefault(String name, int value) {
	setValue(defaultProperties, name, value);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setDefault(String name, long value) {
	setValue(defaultProperties, name, value);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setDefault(String name, String value) {
	setValue(defaultProperties, name, value);
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setDefault(String name, boolean value) {
	setValue(defaultProperties, name, value);
}
/**
 * Sets the name of the file used when loading and storing this preference store.
 * <p>
 * Afterward, the methods <code>load()</code> and <code>save()</code> can be used
 * to load and store this preference store.
 * </p>
 *
 * @param filename the file name
 * @see #load()
 * @see #store()
 */
public void setFilename(String name) {
	filename = name;
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setToDefault(String name) {
    // do nothing ... since we have not defined a 
    // firePropertyChangeEvent. Without it,
    // this method does nothing anyway ... so
    // why do all this work?
    
//  Object oldValue = properties.get(name);
//	properties.remove(name);
//	dirty = true;
//    Object newValue = null;
//	if (defaultProperties != null){
//		newValue = defaultProperties.get(name);
//    }
//    firePropertyChangeEvent(name, oldValue, newValue);

}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setValue(String name, double value) {
	double oldValue = getDouble(name);
	if (oldValue != value) {
		setValue(properties, name, value);
		dirty = true;
	}
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setValue(String name, float value) {
	float oldValue = getFloat(name);
	if (oldValue != value) {
		setValue(properties, name, value);
		dirty = true;
	}
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setValue(String name, int value) {
	int oldValue = getInt(name);
	if (oldValue != value) {
		setValue(properties, name, value);
		dirty = true;
	}
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setValue(String name, long value) {
	long oldValue = getLong(name);
	if (oldValue != value) {
		setValue(properties, name, value);
		dirty = true;
	}
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setValue(String name, String value) {
	String oldValue = getString(name);
	if (oldValue == null || !oldValue.equals(value)) {
		setValue(properties, name, value);
		dirty = true;
	}
}
/* (non-Javadoc)
 * Method declared on IPreferenceStore.
 */
public void setValue(String name, boolean value) {
	boolean oldValue = getBoolean(name);
	if (oldValue != value) {
		setValue(properties, name, value);
		dirty = true;
	}
}
/**
 * Helper method: sets string for a given name.
 */
private void setValue(Properties p, String name, double value) {
	p.put(name, Double.toString(value));
}
/**
 * Helper method: sets string for a given name.
 */
private void setValue(Properties p, String name, float value) {
	p.put(name, Float.toString(value));
}
/**
 * Helper method: sets string for a given name.
 */
private void setValue(Properties p, String name, int value) {
	p.put(name, Integer.toString(value));
}
/**
 * Helper method: sets string for a given name.
 */
private void setValue(Properties p, String name, long value) {
	p.put(name, Long.toString(value));
}
/**
 * Helper method: sets string for a given name.
 */
private void setValue(Properties p, String name, String value) {
	p.put(name, value);
}
/**
 * Helper method: sets string for a given name.
 */
private void setValue(Properties p, String name, boolean value) {
	p.put(name, value == true ? IPropertyStore.TRUE : IPropertyStore.FALSE);
}
}
