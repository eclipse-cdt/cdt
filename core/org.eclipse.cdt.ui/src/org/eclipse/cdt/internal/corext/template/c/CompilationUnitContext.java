package org.eclipse.cdt.internal.corext.template.c;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.DocumentTemplateContext;


/**
 * A compilation unit context.
 */
public abstract class CompilationUnitContext extends DocumentTemplateContext {

	/** The compilation unit, may be <code>null</code>. */
	private final ICompilationUnit fCompilationUnit;

	/**
	 * Creates a compilation unit context.
	 * 
	 * @param type   the context type.
	 * @param string the document string.
	 * @param completionPosition the completion position within the document.
	 * @param compilationUnit the compilation unit (may be <code>null</code>).
	 */
	protected CompilationUnitContext(ContextType type, String string, int completionPosition,
		ICompilationUnit compilationUnit)
	{
		super(type, string, completionPosition);
		fCompilationUnit= compilationUnit;
	}
	
	/**
	 * Returns the compilation unit if one is associated with this context, <code>null</code> otherwise.
	 */
	public final ICompilationUnit getCompilationUnit() {
		return fCompilationUnit;
	}

	/**
	 * Returns the enclosing element of a particular element type, <code>null</code>
	 * if no enclosing element of that type exists.
	 */
	public ICElement findEnclosingElement(int elementType) {
		if (fCompilationUnit == null)
			return null;

		/* try {
			ICElement element= fCompilationUnit.getElementAt(getStart());
			while (element != null && element.getElementType() != elementType)
				element= element.getParent();
			
			return element;

		} catch (JavaModelException e) {
			return null;
		}	*/
		return null;
	}

}


