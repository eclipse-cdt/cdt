package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;

/**
 * The template translator translates a string into a template buffer.
 * The EBNF grammer of a valid string is as follows:
 * 
 * <p>
 * template := (text | escape)*.<br />
 * text := character - dollar.<br />
 * escape := dollar ('{' identifier '}' | dollar).<br />
 * dollar := '$'.<br />
 * </p>
 */
public class TemplateTranslator {

	// states
	private static final int TEXT= 0;
	private static final int ESCAPE= 1;
	private static final int IDENTIFIER= 2;

	// tokens
	private static final char ESCAPE_CHARACTER= '$';
	private static final char IDENTIFIER_BEGIN= '{';
	private static final char IDENTIFIER_END= '}';

	/** a buffer for the translation result string */
    private final StringBuffer fBuffer= new StringBuffer();    
    /** position offsets of variables */
    private final Vector fOffsets= new Vector();
    /** position lengths of variables */
    private final Vector fLengths= new Vector();

	/** the current parsing state */
    private int fState;    
    /** the last translation error */
    private String fErrorMessage;

	/**
	 * Returns an error message if an error occured for the last translation, <code>null</code>
	 * otherwise.
	 */
	public String getErrorMessage() {
	    return fErrorMessage;
	}

	/**
	 * Translates a template string to <code>TemplateBuffer</code>. <code>null</code>
	 * is returned if there was an error. <code>getErrorMessage()</code> retrieves the
	 * associated error message.
	 * 
	 * @param string the string to translate.
	 * @return returns the template buffer corresponding to the string, <code>null</code>
	 *         if there was an error.
	 * @see getErrorMessage()
	 */
	public TemplateBuffer translate(String string) throws CoreException {

	    fBuffer.setLength(0);
	    fOffsets.clear();
	    fLengths.clear();
	    fState= TEXT;
	    fErrorMessage= null;
	    
		if (!parse(string))
			return null;
			
		switch (fState) {
		case TEXT:
			break;
		
		// illegal, but be tolerant
		case ESCAPE:
			fErrorMessage= TemplateMessages.getString("TemplateTranslator.error.incomplete.variable"); //$NON-NLS-1$
			fBuffer.append(ESCAPE_CHARACTER);
			return null;
				
		// illegal, but be tolerant
		case IDENTIFIER:
			fErrorMessage= TemplateMessages.getString("TemplateTranslator.error.incomplete.variable"); //$NON-NLS-1$
			fBuffer.append(ESCAPE_CHARACTER);
			return null;		
		}			
		
		int[] offsets= new int[fOffsets.size()];
		int[] lengths= new int[fLengths.size()];
		
		for (int i= 0; i < fOffsets.size(); i++) {
			offsets[i]= ((Integer) fOffsets.get(i)).intValue();
			lengths[i]= ((Integer) fLengths.get(i)).intValue();
		}

		String translatedString= fBuffer.toString();
		TemplatePosition[] variables= findVariables(translatedString, offsets, lengths);

		return new TemplateBuffer(translatedString, variables);
	}
	
	private static TemplatePosition[] findVariables(String string, int[] offsets, int[] lengths) {

		Map map= new HashMap();
		
		for (int i= 0; i != offsets.length; i++) {
		    int offset= offsets[i];
		    int length= lengths[i];
		    
		    String content= string.substring(offset, offset + length);
		    Vector vector= (Vector) map.get(content);
		    if (vector == null) {
		    	vector= new Vector();
		    	map.put(content, vector);
		    }		    
		    vector.add(new Integer(offset));
		}
		
		TemplatePosition[] variables= new TemplatePosition[map.size()];
		int k= 0;
		
		Set keys= map.keySet();
		for (Iterator i= keys.iterator(); i.hasNext(); ) {
			String name= (String) i.next();			
			Vector vector= (Vector) map.get(name);
			
			int[] offsets_= new int[vector.size()];
			for (int j= 0; j != offsets_.length; j++)
				offsets_[j]= ((Integer) vector.get(j)).intValue();
				
			variables[k]= new TemplatePosition(name, name, offsets_, name.length());
			k++;
		}
		
		return variables;
	}

	/** internal parser */
	private boolean parse(String string) {

		for (int i= 0; i != string.length(); i++) {
		    char ch= string.charAt(i);
			
			switch (fState) {
			case TEXT:
				switch (ch) {
				case ESCAPE_CHARACTER:
					fState= ESCAPE;
					break;
					
				default:
					fBuffer.append(ch);
					break;
				}
				break;
				
			case ESCAPE:
				switch (ch) {
				case ESCAPE_CHARACTER:
					fBuffer.append(ch);
					fState= TEXT;
					break;
				
				case IDENTIFIER_BEGIN:
					fOffsets.add(new Integer(fBuffer.length()));
					fState= IDENTIFIER;
					break;
					
				default:
					// illegal single escape character, but be tolerant
					fErrorMessage= TemplateMessages.getString("TemplateTranslator.error.incomplete.variable"); //$NON-NLS-1$
					fBuffer.append(ESCAPE_CHARACTER);
					fBuffer.append(ch);
					fState= TEXT;
					return false;
				}
				break;

			case IDENTIFIER:
				switch (ch) {
				case IDENTIFIER_END:
					int offset = ((Integer) fOffsets.get(fOffsets.size() - 1)).intValue();
					fLengths.add(new Integer(fBuffer.length() - offset));
					fState= TEXT;
					break;
				
				default:
					if (!Character.isUnicodeIdentifierStart(ch) &&
						!Character.isUnicodeIdentifierPart(ch))
					{
						// illegal identifier character
						fErrorMessage= TemplateMessages.getString("TemplateTranslator.error.invalid.identifier"); //$NON-NLS-1$
						return false;
					}
				
					fBuffer.append(ch);
					break;
				}
				break;
			}
		}
		
		return true;
	}

}
