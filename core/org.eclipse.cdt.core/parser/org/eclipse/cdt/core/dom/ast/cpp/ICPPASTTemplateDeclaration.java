/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * Template declaration.
 * 
 * @author jcamelon
 */
public interface ICPPASTTemplateDeclaration extends IASTDeclaration {

	/**
	 * Is the export keyword used?
	 * 
	 * @return boolean
	 */
	public boolean isExported();

	/**
	 * Should the export keyword be used?
	 * 
	 * @param value
	 *            boolean
	 */
	public void setExported(boolean value);

	/**
	 * <code>OWNED_DECLARATION</code> is the subdeclaration that we maintain
	 * grammatically.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTTemplateDeclaration.OWNED_DECLARATION - Subdeclaration maintained grammatically"); //$NON-NLS-1$

	/**
	 * Get templated declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Set the templated declaration.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	public void setDeclaration(IASTDeclaration declaration);

	/**
	 * <code>PARAMETER</code> is used for template parameters.
	 */
	public static final ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"ICPPASTTemplateDeclaration.PARAMETER - Template Parameter"); //$NON-NLS-1$

	/**
	 * Get template parameters.
	 * 
	 * @return <code>ICPPASTTemplateParameter []</code>
	 */
	public ICPPASTTemplateParameter[] getTemplateParameters();

	/**
	 * Add a template parameter.
	 * 
	 * @param parm
	 *            <code>ICPPASTTemplateParameter</code>
	 */
	public void addTemplateParamter(ICPPASTTemplateParameter parm);
	
	/**
	 * get the template scope representing this declaration in the logical tree
	 * @return <code>ICPPTemplateScope</code>
	 */
	public ICPPTemplateScope getScope();
}
