/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

/**
 * Common label provider for index based viewers.
 * 
 * @author Doug Schaefer
 */
public class IndexLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof IndexNode) {
			return ((IndexNode) element).fText;
		}
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof IndexNode) {
			return ((IndexNode) element).fImage;
		}
		ImageDescriptor desc= null;
		if (element instanceof ICProject)
			desc = CPluginImages.DESC_OBJS_SEARCHHIERPROJECT;
		else if (element instanceof ICContainer)
			desc = CPluginImages.DESC_OBJS_SEARCHHIERFODLER;
		else if (element instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit)element;
			desc = tu.isHeaderUnit()
				? CPluginImages.DESC_OBJS_TUNIT_HEADER
				: CPluginImages.DESC_OBJS_TUNIT;
		}
		
		if (desc != null)
			return CUIPlugin.getImageDescriptorRegistry().get(desc);
		
		return super.getImage(element);
	}

	public static String getText(IPDOMNode element) {
		if (element instanceof PDOMNamedNode) {
			try {
				String result = ((PDOMNamedNode)element).getDBName().getString();

				if (element instanceof ICPPTemplateInstance) {
					StringBuffer buffer = null;
					if (element instanceof ICPPDeferredClassInstance) {
						buffer = new StringBuffer("Dfrd: "); //$NON-NLS-1$
					} else {
						buffer = new StringBuffer("Inst: "); //$NON-NLS-1$	
					}
					buffer.append(result);
					buffer.append('<');
					ICPPTemplateArgument[] types = ((ICPPTemplateInstance) element).getTemplateArguments();
					for (int i = 0; i < types.length; i++) {
						if (i > 0)
							buffer.append(',');
						buffer.append(ASTTypeUtil.getArgumentString(types[i], false));
					}
					buffer.append('>');
					result = buffer.toString();
				} else if (element instanceof ICPPClassTemplatePartialSpecialization) {
					StringBuffer buffer = new StringBuffer("Part: "); //$NON-NLS-1$
					buffer.append(result);
					buffer.append('<');
					try {
						ICPPTemplateArgument[] types = ((ICPPClassTemplatePartialSpecialization) element).getTemplateArguments();
						for (int i = 0; i < types.length; i++) {
							if (i > 0)
								buffer.append(',');
							buffer.append(ASTTypeUtil.getArgumentString(types[i], false));
						}
					} catch (DOMException e) {
						buffer.append(e.getProblem().toString());
					}
					buffer.append('>');
					result = buffer.toString();
				} else if (element instanceof ICPPSpecialization) {
					ICPPSpecialization spec = (ICPPSpecialization) element;
					
					StringBuffer buffer = null;
					buffer = new StringBuffer("Spec: "); //$NON-NLS-1$
					buffer.append(result);
					
					if (!(spec instanceof ICPPTemplateDefinition)
							&& spec.getSpecializedBinding() instanceof ICPPTemplateDefinition) {
						buffer.append('<');
						buffer.append(((ICPPSpecialization) element).getTemplateParameterMap().toString());
						buffer.append('>');
					}
					
					result = buffer.toString();
				}
				
				/*
				 * aftodo - Ideally here we'd call ASTTypeUtil.getType but
				 * we don't currently store return types
				 */
				if(element instanceof IFunction) {
					result += " "+ASTTypeUtil.getParameterTypeString(((IFunction) element).getType()); //$NON-NLS-1$
				}
				
				return result;
			} catch (CoreException e) {
				return e.getMessage();
			}
		} 
		return ""; //$NON-NLS-1$
	}
	
	public static Image getImage(IPDOMNode element) {
		ImageDescriptor desc = null;
	
		if (element instanceof IVariable)
			desc = CElementImageProvider.getVariableImageDescriptor();
		else if (element instanceof IFunction)
			desc = CElementImageProvider.getFunctionImageDescriptor();
		else if (element instanceof ICPPClassType) {
			switch (((ICPPClassType)element).getKey()) {
			case ICPPClassType.k_class:
				desc = CElementImageProvider.getClassImageDescriptor();
				break;
			case ICompositeType.k_struct:
				desc = CElementImageProvider.getStructImageDescriptor();
				break;
			case ICompositeType.k_union:
				desc = CElementImageProvider.getUnionImageDescriptor();
				break;
			}
		}
		else if (element instanceof ICompositeType)
			desc = CElementImageProvider.getStructImageDescriptor();
		else if (element instanceof ICPPNamespace)
			desc = CElementImageProvider.getNamespaceImageDescriptor();
		else if (element instanceof IEnumeration)
			desc = CElementImageProvider.getEnumerationImageDescriptor();
		else if (element instanceof IEnumerator)
			desc = CElementImageProvider.getEnumeratorImageDescriptor();
		else if (element instanceof ITypedef)
			desc = CElementImageProvider.getTypedefImageDescriptor();
		
		if (desc != null)
			return CUIPlugin.getImageDescriptorRegistry().get(desc);
		else if (element instanceof PDOMLinkage)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

		return null;
	}
	
}