package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A template consiting of a name and a pattern.
 */
public class Template {

	/** The name of this template */
	private String fName;
	/** A description of this template */
	private String fDescription;
	/** The name of the context type of this template */
	private String fContextTypeName;
	/** The template pattern. */
	private String fPattern;
	/** A flag indicating if the template is active or not. */
	private boolean fEnabled= true;

	/**
	 * Creates an empty template.
	 */
	public Template() {
		this("", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	/**
	 * Creates a copy of a template.
	 */
	public Template(Template template) {
		this(template.getName(), template.getDescription(), template.getContextTypeName(), template.getPattern());	
	}

	/**
	 * Creates a template.
	 * 
	 * @param name the name of the template.
	 * @param description the description of the template.
	 * @param contextTypeName the name of the context type in which the template can be applied.
	 * @param pattern the template pattern.
	 */		
	public Template(String name, String description, String contextTypeName, String pattern) {
		fName= name;
		fDescription= description;
		fContextTypeName= contextTypeName;
		fPattern= pattern;
	}
	
	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Template))
			return false;
			
		Template template= (Template) object;

		if (template == this)
			return true;		

		return
			template.fName.equals(fName) &&
			template.fPattern.equals(fPattern) &&
			template.fContextTypeName.equals(fContextTypeName);
	}
	
	/*
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return fName.hashCode() ^ fPattern.hashCode() ^ fContextTypeName.hashCode();
	}

	/**
	 * Sets the description of the template.
	 */
	public void setDescription(String description) {
		fDescription= description;
	}
	
	/**
	 * Returns the description of the template.
	 */
	public String getDescription() {
		return fDescription;
	}
	
	/**
	 * Sets the name of the context type in which the template can be applied.
	 */
	public void setContext(String contextTypeName) {
		fContextTypeName= contextTypeName;
	}
	
	/**
	 * Returns the name of the context type in which the template can be applied.
	 */
	public String getContextTypeName() {
		return fContextTypeName;
	}

	/**
	 * Sets the name of the template.
	 */
	public void setName(String name) {
		fName= name;
	}
			
	/**
	 * Returns the name of the template.
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Sets the pattern of the template.
	 */
	public void setPattern(String pattern) {
		fPattern= pattern;
	}
		
	/**
	 * Returns the template pattern.
	 */
	public String getPattern() {
		return fPattern;
	}
	
	/**
	 * Sets the enable state of the template.
	 */
	public void setEnabled(boolean enable) {
		fEnabled= enable;	
	}
	
	/**
	 * Returns <code>true</code> if template is enabled, <code>false</code> otherwise.
	 */
	public boolean isEnabled() {
		return fEnabled;	
	}
	
	/**
	 * Returns <code>true</code> if template matches the prefix and context,
	 * <code>false</code> otherwise.
	 */
	public boolean matches(String prefix, String contextTypeName) {
		return 
			fEnabled &&
			fContextTypeName.equals(contextTypeName) &&
			(prefix.length() != 0) &&
			fName.toLowerCase().startsWith(prefix.toLowerCase());
	}

}
