package org.eclipse.cdt.internal.corext.template.c;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.corext.template.ITemplateEditor;
import org.eclipse.cdt.internal.corext.template.TemplateContext;


/**
 * A context type for javadoc.
 */
public class CGlobalContextType extends CompilationUnitContextType {

	/**
	 * Creates a C context type.
	 */
	public CGlobalContextType() {
		super(ITemplateEditor.TemplateContextKind.C_GLOBAL_CONTEXT_TYPE);
		
		// global
		addVariable(new GlobalVariables.Cursor());
		addVariable(new GlobalVariables.Dollar());
		addVariable(new GlobalVariables.Date());
		addVariable(new GlobalVariables.Time());
		addVariable(new GlobalVariables.User());
		
		// compilation unit
		addVariable(new File());
		/* addVariable(new Method());
		addVariable(new ReturnType());
		addVariable(new Arguments());
		addVariable(new Type());
		addVariable(new Package()); */
		addVariable(new Project());
		// @@@ Need to add some specific C ones
	}
	
	/*
	 * @see ContextType#createContext()
	 */	
	public TemplateContext createContext() {
		return new CContext(this, fString, fPosition, fCompilationUnit);
	}

}


