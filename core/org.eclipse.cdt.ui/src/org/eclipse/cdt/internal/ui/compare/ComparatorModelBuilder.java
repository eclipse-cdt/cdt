/*******************************************************************************
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     QnX Software Systems - initial implementation base on code from rational/IBM
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.compare;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.dom.ClassKey;
import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.DOMFactory;
import org.eclipse.cdt.internal.core.dom.Declaration;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.EnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.IOffsetable;
import org.eclipse.cdt.internal.core.dom.Inclusion;
import org.eclipse.cdt.internal.core.dom.Macro;
import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.ParameterDeclarationClause;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.dom.TypeSpecifier;
import org.eclipse.cdt.internal.core.parser.Name;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.parser.IStructurizerCallback;

/**
 * @author alain
 * TODO: this should be remove when the new parser provides proper callbacks.
 *
 */
public class ComparatorModelBuilder {

	IStructurizerCallback callback;
	String code;

	/**
	 */
	public ComparatorModelBuilder(IStructurizerCallback cb, String buffer) {
		callback = cb;
		code = buffer;
	}

	public void parse() {
		DOMBuilder domBuilder = DOMFactory.createDOMBuilder(false);
		try {

			Parser parser = new Parser(code, domBuilder, true);
			parser.parse();
		} catch (Exception e) {
			callback.reportError(e);
		}
		generateModelElements(domBuilder.getTranslationUnit());
	}

	protected void generateModelElements(TranslationUnit tu) {
		Iterator i = tu.iterateOffsetableElements();
		while (i.hasNext()) {
			IOffsetable offsetable = (IOffsetable) i.next();
			if (offsetable instanceof Inclusion) {
				createInclusion((Inclusion) offsetable);
			} else if (offsetable instanceof Macro) {
				createMacro((Macro) offsetable);
			} else if (offsetable instanceof Declaration) {
				generateModelElements((Declaration) offsetable);
			}
		}
	}

	protected void generateModelElements(Declaration declaration) {
		// Namespace Definition 
		if (declaration instanceof NamespaceDefinition) {
			NamespaceDefinition nsDef = (NamespaceDefinition) declaration;
			createNamespace(nsDef);
			List nsDeclarations = nsDef.getDeclarations();
			Iterator nsDecls = nsDeclarations.iterator();
			while (nsDecls.hasNext()) {
				Declaration subNsDeclaration = (Declaration) nsDecls.next();
				generateModelElements(subNsDeclaration);
			}
		} // end Namespace Definition

		// Simple Declaration 
		if (declaration instanceof SimpleDeclaration) {
			SimpleDeclaration simpleDeclaration = (SimpleDeclaration) declaration;

			/*-------------------------------------------
			 * Checking the type if it is a composite one
			 *-------------------------------------------*/
			TypeSpecifier typeSpec = simpleDeclaration.getTypeSpecifier();
			// Enumeration
			if (typeSpec instanceof EnumerationSpecifier) {
				EnumerationSpecifier enumSpecifier = (EnumerationSpecifier) typeSpec;
				createEnumeration(enumSpecifier);
			} else if (typeSpec instanceof ClassSpecifier) { // Structure
				ClassSpecifier classSpecifier = (ClassSpecifier) typeSpec;
				createClass(simpleDeclaration, classSpecifier, false);
				// create the sub declarations 
				List declarations = classSpecifier.getDeclarations();
				Iterator j = declarations.iterator();
				while (j.hasNext()) {
					Declaration subDeclaration = (Declaration) j.next();
					generateModelElements(subDeclaration);
				} // end while j
			}
			/*-----------------------------------------
			 * Create declarators of simple declaration
			 * ----------------------------------------*/
			List declarators = simpleDeclaration.getDeclarators();
			Iterator d = declarators.iterator();
			while (d.hasNext()) {
				Declarator declarator = (Declarator) d.next();
				createElement(simpleDeclaration, declarator);
			} // end while d		
		} // end if SimpleDeclaration

		// Template Declaration 
		if (declaration instanceof TemplateDeclaration) {
			TemplateDeclaration templateDeclaration = (TemplateDeclaration) declaration;
			SimpleDeclaration simpleDeclaration = (SimpleDeclaration) templateDeclaration.getDeclarations().get(0);
			TypeSpecifier typeSpec = simpleDeclaration.getTypeSpecifier();
			if (typeSpec instanceof ClassSpecifier) {
				ClassSpecifier classSpecifier = (ClassSpecifier) typeSpec;
				createClass(simpleDeclaration, classSpecifier, true);
				// create the sub declarations 
				List declarations = classSpecifier.getDeclarations();
				Iterator j = declarations.iterator();
				while (j.hasNext()) {
					Declaration subDeclaration = (Declaration) j.next();
					generateModelElements(subDeclaration);
				} // end while j
			}
			List declarators = simpleDeclaration.getDeclarators();
			Iterator d = declarators.iterator();
			while (d.hasNext()) {
				Declarator declarator = (Declarator) d.next();
				createTemplateElement(templateDeclaration, simpleDeclaration, declarator);
			} // end while d		

		} // end Template Declaration

	}

	private void createElement(SimpleDeclaration simpleDeclaration, Declarator declarator) {
		// typedef
		if (simpleDeclaration.getDeclSpecifier().isTypedef()) {
			createTypeDef(declarator, simpleDeclaration);
		} else {
			ParameterDeclarationClause pdc = declarator.getParms();
			if (pdc == null) {
				createVariableSpecification(simpleDeclaration, declarator);
			} else {
				createFunctionSpecification(simpleDeclaration, declarator, pdc, false);
			}
		}
	}

	private void createTemplateElement(
		TemplateDeclaration templateDeclaration,
		SimpleDeclaration simpleDeclaration,
		Declarator declarator) {
		// TODO: no template in the old parser
		ParameterDeclarationClause pdc = declarator.getParms();
		if (pdc != null) {
			createFunctionSpecification(simpleDeclaration, declarator, pdc, true);
		}
	}

	private void createInclusion(Inclusion inclusion) {
		callback.includeDecl(inclusion.getName(), inclusion.getStartingOffset(), inclusion.getTotalLength(), 0, 0);
	}

	private void createMacro(Macro macro) {
		callback.defineDecl(macro.getName(), macro.getStartingOffset(), macro.getTotalLength(), 0, 0);
	}

	private void createNamespace(NamespaceDefinition nsDef) {
		// TODO:  the old parser callback does not know about namespace.
	}

	private void createEnumeration(EnumerationSpecifier enumSpecifier) {
		callback.structDeclBegin(
			enumSpecifier.getName().toString(),
			ICElement.C_ENUMERATION,
			enumSpecifier.getName().getStartOffset(),
			enumSpecifier.getName().length(),
			enumSpecifier.getStartingOffset(),
			0,
			0);
		callback.structDeclEnd(enumSpecifier.getTotalLength(), 0);
	}

	private void createClass(SimpleDeclaration simpleDeclaration, ClassSpecifier classSpecifier, boolean isTemplate) {
		// create element
		String className = (classSpecifier.getName() == null) ? "" : classSpecifier.getName().toString();
		int kind;
		switch (classSpecifier.getClassKey()) {
			case ClassKey.t_class :
				kind = ICElement.C_CLASS;
				break;
			case ClassKey.t_struct :
				kind = ICElement.C_STRUCT;
				break;
			default :
				kind = ICElement.C_UNION;
				break;
		}

		// set element position 
		if (classSpecifier.getName() != null) {
			callback.structDeclBegin(
				className,
				kind,
				classSpecifier.getName().getStartOffset(),
				classSpecifier.getName().length(),
				classSpecifier.getStartingOffset(),
				0,
				0);
		} else {
			callback.structDeclBegin(
				className,
				kind,
				classSpecifier.getClassKeyToken().getOffset(),
				classSpecifier.getClassKeyToken().getLength(),
				classSpecifier.getStartingOffset(),
				0,
				0);

		}
		callback.structDeclBegin(
			className,
			kind,
			classSpecifier.getClassKeyToken().getOffset(),
			classSpecifier.getClassKeyToken().getLength(),
			classSpecifier.getStartingOffset(),
			0,
			0);
		callback.structDeclEnd(classSpecifier.getTotalLength(), 0);
	}

	private void createTypeDef(Declarator declarator, SimpleDeclaration simpleDeclaration) {
		// TODO:No typedef in the old callback
		Name domName = (declarator.getDeclarator() != null) ? declarator.getDeclarator().getName() : declarator.getName();
		String declaratorName = domName.toString();
		callback.fieldDecl(
			declaratorName,
			domName.getStartOffset(),
			domName.length(),
			simpleDeclaration.getStartingOffset(),
			simpleDeclaration.getTotalLength(),
			0,
			0,
			0);
	}

	private void createVariableSpecification(SimpleDeclaration simpleDeclaration, Declarator declarator) {
		Name domName = (declarator.getDeclarator() != null) ? declarator.getDeclarator().getName() : declarator.getName();
		String declaratorName = domName.toString();
		callback.fieldDecl(
			declaratorName,
			domName.getStartOffset(),
			domName.length(),
			simpleDeclaration.getStartingOffset(),
			simpleDeclaration.getTotalLength(),
			0,
			0,
			0);
	}

	private void createFunctionSpecification(
		SimpleDeclaration simpleDeclaration,
		Declarator declarator,
		ParameterDeclarationClause pdc,
		boolean isTemplate) {
		Name domName = (declarator.getDeclarator() != null) ? declarator.getDeclarator().getName() : declarator.getName();
		String declaratorName = domName.toString();
		callback.functionDeclBegin(
			declaratorName,
			domName.getStartOffset(),
			domName.length(),
			simpleDeclaration.getStartingOffset(),
			0,
			0,
			0);
		callback.functionDeclEnd(simpleDeclaration.getTotalLength(), 0, simpleDeclaration.isFunctionDefinition());
	}

}
