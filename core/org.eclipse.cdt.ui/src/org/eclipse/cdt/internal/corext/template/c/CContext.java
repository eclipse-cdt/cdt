package org.eclipse.cdt.internal.corext.template.c;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.ITemplateEditor;
import org.eclipse.cdt.internal.corext.template.Template;
import org.eclipse.cdt.internal.corext.template.TemplateBuffer;
import org.eclipse.cdt.internal.corext.template.TemplateTranslator;
import org.eclipse.cdt.internal.corext.textmanipulation.TextBuffer;
import org.eclipse.cdt.internal.corext.textmanipulation.TextUtil;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;



/**
 * A context for c/c++
 */
public class CContext extends CompilationUnitContext {	

	/**
	 * Creates a javadoc template context.
	 * 
	 * @param type   the context type.
	 * @param string the document string.
	 * @param completionPosition the completion position within the document.
	 * @param unit the compilation unit (may be <code>null</code>).
	 */
	public CContext(ContextType type, String string, int completionPosition,
		ICompilationUnit compilationUnit)
	{
		super(type, string, completionPosition, compilationUnit);
	}

	/*
	 * @see DocumentTemplateContext#getStart()
	 */ 
	public int getStart() {
		String string= getString();
		int start= getCompletionPosition();

		while ((start != 0) && Character.isUnicodeIdentifierPart(string.charAt(start - 1)))
			start--;
		
		if ((start != 0) && Character.isUnicodeIdentifierStart(string.charAt(start - 1)))
			start--;

		return start;
	}

	/**
	 * Returns the indentation level at the position of code completion.
	 */
	public int getIndentationLevel() {
		String string= getString();
		int start= getStart();

	    try {
	        TextBuffer textBuffer= TextBuffer.create(string);
	        String lineContent= textBuffer.getLineContentOfOffset(start);

			return TextUtil.getIndent(lineContent, CUIPlugin.getDefault().getPreferenceStore().getInt(CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH));

	    } catch (CoreException e) {
	     	return 0;   
	    }
	}
	
	/*
	 * @see TemplateContext#canEvaluate(Template templates)
	 */
	public boolean canEvaluate(Template template) {
		return template.matches(getKey(), getContextType().getName());
	}

	/*
	 * @see TemplateContext#evaluate(Template)
	 */
	public TemplateBuffer evaluate(Template template) throws CoreException {
		if (!canEvaluate(template))
			return null;
			
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(template.getPattern());

		getContextType().edit(buffer, this);

		ITemplateEditor formatter= new CFormatter();
		formatter.edit(buffer, this);
					
		return buffer;
	}

}



