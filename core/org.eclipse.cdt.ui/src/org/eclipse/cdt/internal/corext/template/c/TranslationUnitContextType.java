/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;


/**
 * A context type for translation units.
 */
public abstract class TranslationUnitContextType extends TemplateContextType {

	protected static class ReturnType extends TemplateVariableResolver {
	 	public ReturnType() {
	 	 	super("return_type", TemplateMessages.CContextType_variable_description_return_type);  //$NON-NLS-1$
	 	}
	 	@Override
		public String resolve(TemplateContext context) {
			ICElement element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_METHOD);
			if (element == null) {
				element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_FUNCTION);
			}
			if (element == null) {
				return null;
			}

			if (element instanceof IFunctionDeclaration) {
				return ((IFunctionDeclaration) element).getReturnType();
			}
			return null;
		}
	}

	protected static class File extends TemplateVariableResolver {
		public File() {
			super("file", TemplateMessages.CContextType_variable_description_file);  //$NON-NLS-1$
		}
		@Override
		public String resolve(TemplateContext context) {
			ITranslationUnit unit= ((TranslationUnitContext) context).getTranslationUnit();
			
			return (unit == null) ? null : unit.getElementName();
		}
	}

	protected static class EnclosingCElement extends TemplateVariableResolver {
		protected final int fElementType;
		
		public EnclosingCElement(String name, String description, int elementType) {
			super(name, description);
			fElementType= elementType;
		}
		@Override
		public String resolve(TemplateContext context) {
			ICElement element= ((TranslationUnitContext) context).findEnclosingElement(fElementType);
			return (element == null) ? null : element.getElementName();
		}
	}
	
	protected static class Method extends TemplateVariableResolver {
		public Method() {
			super("enclosing_method", TemplateMessages.CContextType_variable_description_enclosing_method);  //$NON-NLS-1$
		}
		@Override
		public String resolve(TemplateContext context) {
			ICElement element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_FUNCTION);
			if (element == null) {
				element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_FUNCTION_DECLARATION);
				if (element == null) {
					element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_METHOD);
					if (element == null) {
						element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_METHOD_DECLARATION);
					}
				}
			}

			if (element instanceof IFunctionDeclaration) {
				return element.getElementName();
			}
			return null;
		}
	}

	protected static class Project extends TemplateVariableResolver {
		public Project() {
			super("enclosing_project", TemplateMessages.CContextType_variable_description_enclosing_project);  //$NON-NLS-1$
		}
		@Override
		public String resolve(TemplateContext context) {
			ITranslationUnit unit= ((TranslationUnitContext) context).getTranslationUnit();
			return (unit == null) ? null : unit.getCProject().getElementName();
		}
	}	

	protected static class Arguments extends TemplateVariableResolver {
		public Arguments() {
			super("enclosing_method_arguments", TemplateMessages.CContextType_variable_description_enclosing_method_arguments);  //$NON-NLS-1$
		}
		@Override
		public String resolve(TemplateContext context) {
			ICElement element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_FUNCTION);
			if (element == null) {
				element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_FUNCTION_DECLARATION);
				if (element == null) {
					element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_METHOD);
					if (element == null) {
						element= ((TranslationUnitContext) context).findEnclosingElement(ICElement.C_METHOD_DECLARATION);
					}
				}
			}

			if (element instanceof IFunctionDeclaration) {
				String[] arguments= ((IFunctionDeclaration)element).getParameterTypes();
				StringBuffer buffer= new StringBuffer();
				
				for (int i= 0; i < arguments.length; i++) {
					if (i > 0)
						buffer.append(", "); //$NON-NLS-1$
					buffer.append(arguments[i]);				
				}
				
				return buffer.toString();
			}
			return null;
		}
	}

	protected static class Todo extends TemplateVariableResolver {

		public Todo() {
			super("todo", TemplateMessages.CContextType_variable_description_todo);  //$NON-NLS-1$
		}
		@Override
		protected String resolve(TemplateContext context) {
			TranslationUnitContext cContext= (TranslationUnitContext) context;
			ITranslationUnit tUnit= cContext.getTranslationUnit();
			if (tUnit == null)
				return "XXX"; //$NON-NLS-1$
			
			ICProject cProject= tUnit.getCProject();
			String todoTaskTag= StubUtility.getTodoTaskTag(cProject);
			if (todoTaskTag == null)
				return "XXX"; //$NON-NLS-1$

			return todoTaskTag;
		}
	}	

	/*
	 * @see TemplateContextType#TemplateContextType()
	 */
	public TranslationUnitContextType() {
		super();	
		// global
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Year());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.User());
		
		// translation unit
		addResolver(new File());
		addResolver(new ReturnType());
		addResolver(new Method());
		addResolver(new Project());
		addResolver(new Arguments());
		addResolver(new Todo());
	}

	public abstract TranslationUnitContext createContext(IDocument document, int offset, int length, ITranslationUnit translationUnit);
	public abstract TranslationUnitContext createContext(IDocument document, Position position, ITranslationUnit translationUnit);
}


