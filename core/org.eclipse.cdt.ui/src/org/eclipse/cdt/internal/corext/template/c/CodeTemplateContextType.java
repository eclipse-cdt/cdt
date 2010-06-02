/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;

/**
  */
public class CodeTemplateContextType extends FileTemplateContextType {
	
	/* context types */
	private static final String CONTEXTTYPE_PREFIX= "org.eclipse.cdt.ui.text.codetemplates."; //$NON-NLS-1$

	public static final String CPPSOURCEFILE_CONTEXTTYPE= CCorePlugin.CONTENT_TYPE_CXXSOURCE + FileTemplateContextType.CONTEXTTYPE_SUFFIX;
	public static final String CPPHEADERFILE_CONTEXTTYPE= CCorePlugin.CONTENT_TYPE_CXXHEADER + FileTemplateContextType.CONTEXTTYPE_SUFFIX;
	public static final String CSOURCEFILE_CONTEXTTYPE= CCorePlugin.CONTENT_TYPE_CSOURCE + FileTemplateContextType.CONTEXTTYPE_SUFFIX;
	public static final String CHEADERFILE_CONTEXTTYPE= CCorePlugin.CONTENT_TYPE_CHEADER + FileTemplateContextType.CONTEXTTYPE_SUFFIX;
	public static final String ASMSOURCEFILE_CONTEXTTYPE= CCorePlugin.CONTENT_TYPE_ASMSOURCE + FileTemplateContextType.CONTEXTTYPE_SUFFIX;
	
	public static final String METHODBODY_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "methodbody_context"; //$NON-NLS-1$
	public static final String CONSTRUCTORBODY_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "constructorbody_context"; //$NON-NLS-1$
	public static final String DESTRUCTORBODY_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "destructorbody_context"; //$NON-NLS-1$
	public static final String FILECOMMENT_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "filecomment_context"; //$NON-NLS-1$
	public static final String TYPECOMMENT_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "typecomment_context"; //$NON-NLS-1$
	public static final String FIELDCOMMENT_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "fieldcomment_context"; //$NON-NLS-1$
	public static final String METHODCOMMENT_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "methodcomment_context"; //$NON-NLS-1$
	public static final String CONSTRUCTORCOMMENT_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "constructorcomment_context"; //$NON-NLS-1$
	public static final String DESTRUCTORCOMMENT_CONTEXTTYPE= CONTEXTTYPE_PREFIX + "destructorcomment_context"; //$NON-NLS-1$

	/* templates */
	private static final String CODETEMPLATES_PREFIX= "org.eclipse.cdt.ui.text.codetemplates."; //$NON-NLS-1$
	public static final String COMMENT_SUFFIX= "comment"; //$NON-NLS-1$
	
	public static final String ASM_SOURCEFILE_ID= CODETEMPLATES_PREFIX + "asmsourcefile"; //$NON-NLS-1$	
	public static final String CPP_SOURCEFILE_ID= CODETEMPLATES_PREFIX + "cppsourcefile"; //$NON-NLS-1$	
	public static final String CPP_HEADERFILE_ID= CODETEMPLATES_PREFIX + "cppheaderfile"; //$NON-NLS-1$	
	public static final String C_SOURCEFILE_ID= CODETEMPLATES_PREFIX + "csourcefile"; //$NON-NLS-1$	
	public static final String C_HEADERFILE_ID= CODETEMPLATES_PREFIX + "cheaderfile"; //$NON-NLS-1$	
	public static final String METHODSTUB_ID= CODETEMPLATES_PREFIX + "methodbody"; //$NON-NLS-1$	
	public static final String CONSTRUCTORSTUB_ID= CODETEMPLATES_PREFIX + "constructorbody"; //$NON-NLS-1$
	public static final String DESTRUCTORSTUB_ID= CODETEMPLATES_PREFIX + "destructorbody"; //$NON-NLS-1$
	public static final String FILECOMMENT_ID= CODETEMPLATES_PREFIX + "file" + COMMENT_SUFFIX; //$NON-NLS-1$
	public static final String TYPECOMMENT_ID= CODETEMPLATES_PREFIX + "type" + COMMENT_SUFFIX; //$NON-NLS-1$
	public static final String FIELDCOMMENT_ID= CODETEMPLATES_PREFIX + "field" + COMMENT_SUFFIX; //$NON-NLS-1$
	public static final String METHODCOMMENT_ID= CODETEMPLATES_PREFIX + "method" + COMMENT_SUFFIX; //$NON-NLS-1$
	public static final String CONSTRUCTORCOMMENT_ID= CODETEMPLATES_PREFIX + "constructor" + COMMENT_SUFFIX; //$NON-NLS-1$
	public static final String DESTRUCTORCOMMENT_ID= CODETEMPLATES_PREFIX + "destructor" + COMMENT_SUFFIX; //$NON-NLS-1$
	
	/* resolver types */
	public static final String ENCLOSING_METHOD= "enclosing_method"; //$NON-NLS-1$
	public static final String ENCLOSING_TYPE= "enclosing_type"; //$NON-NLS-1$
	public static final String BODY_STATEMENT= "body_statement"; //$NON-NLS-1$
	public static final String FIELD= "field"; //$NON-NLS-1$
	public static final String FIELD_TYPE= "field_type"; //$NON-NLS-1$
	
	public static final String RETURN_TYPE= "return_type"; //$NON-NLS-1$
	
	public static final String TYPENAME= "type_name"; //$NON-NLS-1$
	public static final String INCLUDE_GUARD_SYMBOL= "include_guard_symbol"; //$NON-NLS-1$

	public static final String DECLARATIONS= "declarations"; //$NON-NLS-1$
	public static final String TYPE_COMMENT= "typecomment"; //$NON-NLS-1$
	public static final String FILE_COMMENT= "filecomment"; //$NON-NLS-1$
	
	
	/**
	 * Resolver that resolves to the variable defined in the context.
	 */
	public static class CodeTemplateVariableResolver extends FileTemplateVariableResolver {
		public CodeTemplateVariableResolver(String type, String description) {
			super(type, description);
		}
	}
	
	/**
	 * Resolver for task tags.
	 */
	protected static class Todo extends TemplateVariableResolver {

		public Todo() {
			super("todo", TemplateMessages.CodeTemplateContextType_variable_description_todo);  //$NON-NLS-1$
		}
		
		@Override
		protected String resolve(TemplateContext context) {
			ICProject cProject = null;
			if (context instanceof CodeTemplateContext) {
				cProject = ((CodeTemplateContext) context).getCProject();
			}
			String todoTaskTag= StubUtility.getTodoTaskTag(cProject);
			if (todoTaskTag == null)
				return "XXX"; //$NON-NLS-1$
	
			return todoTaskTag;
		}
	}
	
	private boolean fIsComment;
	
	public CodeTemplateContextType(String contextTypeId) {
		this(contextTypeId, contextTypeId);
	}

	public CodeTemplateContextType(String contextTypeId, String contextName) {
		super(contextTypeId, contextName);
		
		fIsComment= false;
		
		// global
		addResolver(new Todo());
		
		if (CPPSOURCEFILE_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(DECLARATIONS,  TemplateMessages.CodeTemplateContextType_variable_description_typedeclaration)); 
			addResolver(new CodeTemplateVariableResolver(TYPE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_typecomment)); 
			addResolver(new CodeTemplateVariableResolver(FILE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_filecomment)); 
			addTranslationUnitVariables();
		} else if (CPPHEADERFILE_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(INCLUDE_GUARD_SYMBOL,  TemplateMessages.CodeTemplateContextType_variable_description_include_guard_symbol)); 
			addResolver(new CodeTemplateVariableResolver(DECLARATIONS,  TemplateMessages.CodeTemplateContextType_variable_description_typedeclaration)); 
			addResolver(new CodeTemplateVariableResolver(TYPE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_typecomment)); 
			addResolver(new CodeTemplateVariableResolver(FILE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_filecomment)); 
			addTranslationUnitVariables();
		} else if (CSOURCEFILE_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(DECLARATIONS,  TemplateMessages.CodeTemplateContextType_variable_description_typedeclaration)); 
			addResolver(new CodeTemplateVariableResolver(FILE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_filecomment)); 
			addTranslationUnitVariables();
		} else if (CHEADERFILE_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(INCLUDE_GUARD_SYMBOL,  TemplateMessages.CodeTemplateContextType_variable_description_include_guard_symbol)); 
			addResolver(new CodeTemplateVariableResolver(DECLARATIONS,  TemplateMessages.CodeTemplateContextType_variable_description_typedeclaration)); 
			addResolver(new CodeTemplateVariableResolver(FILE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_filecomment)); 
			addTranslationUnitVariables();
		} else if (METHODBODY_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingtype)); 
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod)); 
			addResolver(new CodeTemplateVariableResolver(BODY_STATEMENT,  TemplateMessages.CodeTemplateContextType_variable_description_bodystatement)); 
		} else if (CONSTRUCTORBODY_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingtype)); 
			addResolver(new CodeTemplateVariableResolver(BODY_STATEMENT,  TemplateMessages.CodeTemplateContextType_variable_description_bodystatement)); 
		} else if (DESTRUCTORBODY_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingtype)); 
			addResolver(new CodeTemplateVariableResolver(BODY_STATEMENT,  TemplateMessages.CodeTemplateContextType_variable_description_bodystatement)); 
		} else if (TYPECOMMENT_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(TYPENAME,  TemplateMessages.CodeTemplateContextType_variable_description_typename)); 
			addTranslationUnitVariables();
			fIsComment= true;
		} else if (FILECOMMENT_CONTEXTTYPE.equals(contextTypeId)) {
			addTranslationUnitVariables();
			fIsComment= true;
		} else if (FIELDCOMMENT_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(FIELD_TYPE, TemplateMessages.CodeTemplateContextType_variable_description_fieldtype)); 
			addResolver(new CodeTemplateVariableResolver(FIELD, TemplateMessages.CodeTemplateContextType_variable_description_fieldname)); 
			addTranslationUnitVariables();
			fIsComment= true;
		} else if (METHODCOMMENT_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingtype)); 
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_METHOD,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingmethod)); 
			addResolver(new CodeTemplateVariableResolver(RETURN_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_returntype)); 
			addTranslationUnitVariables();
			fIsComment= true;
		} else if (CONSTRUCTORCOMMENT_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingtype)); 
			addTranslationUnitVariables();
			fIsComment= true;
		} else if (DESTRUCTORCOMMENT_CONTEXTTYPE.equals(contextTypeId)) {
			addResolver(new CodeTemplateVariableResolver(ENCLOSING_TYPE,  TemplateMessages.CodeTemplateContextType_variable_description_enclosingtype)); 
			addTranslationUnitVariables();
			fIsComment= true;
		} else {
			addTranslationUnitVariables();
		}
	}
	
	/*
	 * @see org.eclipse.cdt.internal.corext.template.c.FileTemplateContextType#addResourceVariables()
	 */
	@Override
	protected void addResourceVariables() {
		// don't add resource variables by default
	}
	
	private void addTranslationUnitVariables() {
		super.addResourceVariables();
	}
	
	public static void registerContextTypes(ContextTypeRegistry registry) {
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.CPPSOURCEFILE_CONTEXTTYPE, TemplateMessages.CodeTemplateContextType_cppsource_name));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.CPPHEADERFILE_CONTEXTTYPE, TemplateMessages.CodeTemplateContextType_cppheader_name));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.CSOURCEFILE_CONTEXTTYPE, TemplateMessages.CodeTemplateContextType_csource_name));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.CHEADERFILE_CONTEXTTYPE, TemplateMessages.CodeTemplateContextType_cheader_name));
		FileTemplateContextType asmContextType= new FileTemplateContextType(CodeTemplateContextType.ASMSOURCEFILE_CONTEXTTYPE, TemplateMessages.CodeTemplateContextType_asmsource_name);
		asmContextType.addResolver(new CodeTemplateVariableResolver(FILE_COMMENT,  TemplateMessages.CodeTemplateContextType_variable_description_filecomment));
		registry.addContextType(asmContextType);

		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.METHODBODY_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.CONSTRUCTORBODY_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.DESTRUCTORBODY_CONTEXTTYPE));
		
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.FILECOMMENT_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.TYPECOMMENT_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.FIELDCOMMENT_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.METHODCOMMENT_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.CONSTRUCTORCOMMENT_CONTEXTTYPE));
		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.DESTRUCTORCOMMENT_CONTEXTTYPE));
	}

	@Override
	public void validate(String pattern) throws TemplateException {
		super.validate(pattern);
		if (fIsComment) {
			if (!isValidComment(pattern)) {
				throw new TemplateException(TemplateMessages.CodeTemplateContextType_validate_invalidcomment); 
			}
		}
	}
		
	
	private boolean isValidComment(String template) {
//		IScanner scanner= ToolFactory.createScanner(true, false, false, false);
//		scanner.setSource(template.toCharArray());
//		try {
//			int next= scanner.getNextToken();
//			while (TokenScanner.isComment(next)) {
//				next= scanner.getNextToken();
//			}
//			return next == ITerminalSymbols.TokenNameEOF;
//		} catch (InvalidInputException e) {
//		}
//		return false;
		return true;
	}	

}
