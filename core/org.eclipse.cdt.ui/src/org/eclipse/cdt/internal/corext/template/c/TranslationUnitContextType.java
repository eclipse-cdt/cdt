package org.eclipse.cdt.internal.corext.template.c;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Compilation unit context type.
 */
public abstract class TranslationUnitContextType extends TemplateContextType {
	
	/** the document string */
	protected String fString;

	/** the completion position within the document string */
	protected int fPosition;

	/** the associated compilation unit, may be <code>null</code> */
	protected ITranslationUnit fTranslationUnit;
	
	protected static class ReturnType extends TemplateVariableResolver {
	 	public ReturnType() {
	 	 	super("return_type", TemplateMessages.getString("JavaContextType.variable.description.return.type")); //$NON-NLS-1$ //$NON-NLS-2$
	 	}
	 	public String evaluate(TemplateContext context) {
			/* IJavaElement element= ((TranslationUnitContext) context).findEnclosingElement(IJavaElement.METHOD);
			if (element == null)
				return null;

			try {
				return Signature.toString(((IMethod) element).getReturnType());
			} catch (JavaModelException e) {
				return null;
			} */
			return null;
		}
		public boolean isResolved(TemplateContext context) {
			return evaluate(context) != null;
		}		
	}

	protected static class File extends TemplateVariableResolver {
		public File() {
			super("file", TemplateMessages.getString("JavaContextType.variable.description.file")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		public String evaluate(TemplateContext context) {
			//ICompilationUnit unit= ((TranslationUnitContext) context).getCompilationUnit();
			
			//return (unit == null) ? null : unit.getElementName();
			return null;
		}
		public boolean isResolved(TemplateContext context) {
			return evaluate(context) != null;
		}		
	}

	protected static class EnclosingJavaElement extends TemplateVariableResolver {
		protected final int fElementType;
		
		public EnclosingJavaElement(String name, String description, int elementType) {
			super(name, description);
			fElementType= elementType;
		}
		public String evaluate(TemplateContext context) {
			/*IJavaElement element= ((TranslationUnitContext) context).findEnclosingElement(fElementType);
			return (element == null) ? null : element.getElementName(); */
			return null;			
		}
		public boolean isResolved(TemplateContext context) {
			return evaluate(context) != null;
		}
	}
	
	protected static class Method extends EnclosingJavaElement {
		public Method() {
			//super("enclosing_method", TemplateMessages.getString("JavaContextType.variable.description.enclosing.method"), IJavaElement.METHOD);
			super("enclosing_method", TemplateMessages.getString("JavaContextType.variable.description.enclosing.method"), 0); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected static class Type extends EnclosingJavaElement {
		public Type() {
			super("enclosing_type", TemplateMessages.getString("JavaContextType.variable.description.enclosing.type"), 0); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
/*
	protected static class SuperClass extends EnclosingJavaElement {
		public Type() {
			super("super_class", TemplateMessages.getString("JavaContextType.variable.description.type"), IJavaElement.TYPE);
		}
	}
*/
	protected static class Package extends EnclosingJavaElement {
		public Package() {
			super("enclosing_package", TemplateMessages.getString("JavaContextType.variable.description.enclosing.package"), 0); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}	

	protected static class Project extends EnclosingJavaElement {
		public Project() {
			super("enclosing_project", TemplateMessages.getString("JavaContextType.variable.description.enclosing.project"), 0); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}	
/*
	protected static class Project2 extends TemplateVariable {
		public Project2() {
			super("project", TemplateMessages.getString("JavaContextType.variable.description.project"));
		}
		public String evaluate(TemplateContext context) {
			ICompilationUnit unit= ((JavaContext) context).getUnit();
			return (unit == null) ? null : unit.getJavaProject().getElementName();
		}
	}	
*/
	protected static class Arguments extends TemplateVariableResolver {
		public Arguments() {
			super("enclosing_method_arguments", TemplateMessages.getString("JavaContextType.variable.description.enclosing.method.arguments")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		public String evaluate(TemplateContext context) {
			/*IJavaElement element= ((TranslationUnitContext) context).findEnclosingElement(IJavaElement.METHOD);
			if (element == null)
				return null;
				
			IMethod method= (IMethod) element;
			
			try {
				String[] arguments= method.getParameterNames();
				StringBuffer buffer= new StringBuffer();
				
				for (int i= 0; i < arguments.length; i++) {
					if (i > 0)
						buffer.append(", ");
					buffer.append(arguments[i]);				
				}
				
				return buffer.toString();

			} catch (JavaModelException e) {
				return null;
			} */
			return null;
		}
	}

/*	
	protected static class Line extends TemplateVariable {
		public Line() {
			super("line", TemplateMessages.getString("TranslationUnitContextType.variable.description.line"));
		}
		public String evaluate(TemplateContext context) {
			return ((JavaTemplateContext) context).guessLineNumber();
		}
	}
*/	

	/*
	 * @see ContextType#ContextType(String)
	 */
	public TranslationUnitContextType() {
		super();	
	}

	/**
	 * Sets context parameters. Needs to be called before createContext().
	 */
	public void setContextParameters(String string, int position, ITranslationUnit translationUnit) {
		fString= string;
		fPosition= position;
		fTranslationUnit= translationUnit;
	}

	public abstract TranslationUnitContext createContext(IDocument document, int offset, int length, ITranslationUnit translationUnit);

}


