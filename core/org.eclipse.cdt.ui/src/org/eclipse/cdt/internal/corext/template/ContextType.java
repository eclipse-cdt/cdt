package org.eclipse.cdt.internal.corext.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.corext.textmanipulation.MultiTextEdit;
import org.eclipse.cdt.internal.corext.textmanipulation.NopTextEdit;
import org.eclipse.cdt.internal.corext.textmanipulation.SimpleTextEdit;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBuffer;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBufferEditor;
import org.eclipse.cdt.internal.corext.textmanipulation.TextEdit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Vector;
import org.eclipse.core.runtime.CoreException;


/**
 * A context type is a context factory.
 */
public abstract class ContextType implements ITemplateEditor {

	/** name of the context type */
	private final String fName;

	/** variables used by this content type */
	private final Map fVariables= new HashMap();

	/**
	 * Creates a context type with a name.
	 * 
	 * @param name the name of the context. It has to be unique wrt to other context names.
	 */
	public ContextType(String name) {
		fName= name;   
	}

	/**
	 * Returns the name of the context type.
	 */
	public String getName() {
	    return fName;
	}
	
	/**
	 * Adds a template variable to the context type.
	 */
	public void addVariable(TemplateVariable variable) {
		fVariables.put(variable.getName(), variable);   
	}
	
	/**
	 * Removes a template variable from the context type.
	 */
	public void removeVariable(TemplateVariable variable) {
		fVariables.remove(variable.getName());
	}

	/**
	 * Removes all template variables from the context type.
	 */
	public void removeAllVariables() {
		fVariables.clear();
	}

	/**
	 * Returns an iterator for the variables known to the context type.
	 */
	public Iterator variableIterator() {
	 	return fVariables.values().iterator();   
	}

	/**
	 * Creates a template context.
	 */
	public abstract TemplateContext createContext();

    /*
     * @see ITemplateEditor#edit(TemplateBuffer)
     */
    public void edit(TemplateBuffer templateBuffer, TemplateContext context) throws CoreException {
		TextBuffer textBuffer= TextBuffer.create(templateBuffer.getString());
		TemplatePosition[] variables= templateBuffer.getVariables();

		MultiTextEdit positions= variablesToPositions(variables);
		MultiTextEdit multiEdit= new MultiTextEdit();

        // iterate over all variables and try to resolve them
        for (int i= 0; i != variables.length; i++) {
            TemplatePosition variable= variables[i];

			if (variable.isResolved())
				continue;			

			String name= variable.getName();
			int[] offsets= variable.getOffsets();
			int length= variable.getLength();
			
			TemplateVariable evaluator= (TemplateVariable) fVariables.get(name);
			String value= (evaluator == null)
				? null
				: evaluator.evaluate(context);
			
			if (value == null)
				continue;

			variable.setLength(value.length());
			variable.setResolved(evaluator.isResolved(context));

        	for (int k= 0; k != offsets.length; k++)
				multiEdit.add(SimpleTextEdit.createReplace(offsets[k], length, value));
        }

		TextBufferEditor editor= new TextBufferEditor(textBuffer);        
		editor.add(positions);
		editor.add(multiEdit);		
        editor.performEdits(null);

		positionsToVariables(positions, variables);
        
        templateBuffer.setContent(textBuffer.getContent(), variables);
    }

	private static MultiTextEdit variablesToPositions(TemplatePosition[] variables) {
   		MultiTextEdit positions= new MultiTextEdit();
		for (int i= 0; i != variables.length; i++) {
		    int[] offsets= variables[i].getOffsets();
		    for (int j= 0; j != offsets.length; j++)
				positions.add(new NopTextEdit(offsets[j], 0));
		}
		
		return positions;	    
	}
	
	private static void positionsToVariables(MultiTextEdit positions, TemplatePosition[] variables) {
		Iterator iterator= positions.iterator();
		
		for (int i= 0; i != variables.length; i++) {
		    TemplatePosition variable= variables[i];
		    
			int[] offsets= new int[variable.getOffsets().length];
			for (int j= 0; j != offsets.length; j++)
				offsets[j]= ((TextEdit) iterator.next()).getTextRange().getOffset();
			
		 	variable.setOffsets(offsets);   
		}
	}

	/**
	 * Returns the templates associated with this context type.
	 */
	public Template[] getTemplates() {
		Template[] templates= Templates.getInstance().getTemplates();
		
		Vector vector= new Vector();
		for (int i= 0; i != templates.length; i++)
			if (templates[i].getContextTypeName().equals(fName))
				vector.add(templates[i]);

		return (Template[]) vector.toArray(new Template[vector.size()]);
	}	

}
