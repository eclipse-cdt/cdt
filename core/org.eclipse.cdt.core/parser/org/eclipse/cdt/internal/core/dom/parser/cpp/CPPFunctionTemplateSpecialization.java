package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateSpecialization;

public class CPPFunctionTemplateSpecialization extends CPPFunction implements
		ICPPTemplateSpecialization {

	private IASTName name = null;
	private IType [] argumentTypes = null;
	private ICPPFunctionTemplate primaryTemplate = null;
	
	public CPPFunctionTemplateSpecialization(ICPPASTFunctionDeclarator declarator, ICPPFunctionTemplate primaryTemplate ) {
		super(declarator);
		this.primaryTemplate = primaryTemplate;
		IASTName n = declarator.getName();
	    if( n instanceof ICPPASTQualifiedName ){
	        IASTName [] ns = ((ICPPASTQualifiedName)n).getNames();
	        n = ns[ ns.length - 1 ];
	    }
		this.name = n;
	}
	
	public IType [] getArguments() throws DOMException{
		if( argumentTypes == null ){
			IASTNode [] specArgs = ( name instanceof ICPPASTTemplateId ) ? ((ICPPASTTemplateId)name).getTemplateArguments() 
																		 : IASTNode.EMPTY_NODE_ARRAY;
			argumentTypes = CPPTemplates.deduceTemplateFunctionArguments( this, specArgs );
		}
		return argumentTypes;
	}

	public boolean isPartialSpecialization() {
		return false;
	}

	public ICPPTemplateDefinition getPrimaryTemplateDefinition() {
		return primaryTemplate;
	}

	public ICPPTemplateParameter[] getTemplateParameters() {
		return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
	}

	public ICPPTemplateSpecialization[] getTemplateSpecializations() {
		return ICPPTemplateSpecialization.EMPTY_TEMPLATE_SPECIALIZATION_ARRAY;
	}

}
