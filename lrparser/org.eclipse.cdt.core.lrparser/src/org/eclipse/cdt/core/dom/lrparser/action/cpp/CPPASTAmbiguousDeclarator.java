package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguity;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;


@SuppressWarnings("restriction")
public class CPPASTAmbiguousDeclarator extends CPPASTAmbiguity implements IASTDeclarator {

	private List<IASTDeclarator> declarators = new ArrayList<IASTDeclarator>(2);
	
	private int defaultDeclarator = 0;
	
	
	public CPPASTAmbiguousDeclarator(IASTDeclarator ... ds) {
		for(IASTDeclarator declarator : ds)
			addDeclarator(declarator);
	}
	
	@Override
	protected IASTNode[] getNodes() {
		return declarators.toArray(new IASTDeclarator[declarators.size()]);
	}

	
	
	@Override
	public boolean accept(ASTVisitor visitor) {
		// this code was copied from CPPASTAmbiguity.accept() and slightly modified.
		IASTNode nodeToReplace = this;
    	IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
    	
        IASTNode[] nodez = getNodes();
        int[] problems = new int[nodez.length];
        
        for(int i = 0; i < nodez.length; ++i) {
        	defaultDeclarator = i;
            IASTNode node = nodez[i];
            owner.replace(nodeToReplace, node);
            nodeToReplace = node;
            
            node.accept(visitor);
            CPPASTNameCollector nameCollector = new CPPASTNameCollector();
            node.accept(nameCollector);
            IASTName[] names = nameCollector.getNames();
            for(IASTName name : names) {
            	if(name.toCharArray().length > 0) { // don't count dummy name nodes
	                try {
	                    IBinding b = name.resolveBinding();
	                    if(b == null || b instanceof IProblemBinding) {
	                        ++problems[i];
	                    }
	                } catch (Exception t) {
	                	t.printStackTrace();
	                    ++problems[i];
	                }
            	}
            }
            if(names.length > 0) {
                IScope scope = CPPVisitor.getContainingScope(names[0]);
                
                if( scope != null ) {
                    try {
                        ASTInternal.flushCache(scope);
                        IScope parentScope = scope.getParent(); // needed to fix bugs
                        if(parentScope != null) {
                        	ASTInternal.flushCache(parentScope);
                        }
                    } catch (DOMException de) {}
                }
            }
        }
        
        int bestIndex = 0;
        int bestValue = problems[0];
        for (int i = 1; i < problems.length; ++i) {
            if (problems[i] < bestValue) {
                bestIndex = i;
                bestValue = problems[i];
            }
        }

        //IASTAmbiguityParent owner = (IASTAmbiguityParent) getParent();
        owner.replace(nodeToReplace, nodez[bestIndex]);
        defaultDeclarator = 0;
        return true;
	}

	public void addDeclarator(IASTDeclarator declarator) {
		if(declarator != null) {
			declarators.add(declarator);
			declarator.setParent(this);
			declarator.setPropertyInParent(null); // it really doesn't matter
		}
	}

	private IASTDeclarator getDefaultDeclarator() {
		return declarators.get(defaultDeclarator);
	}
	
	public void addPointerOperator(IASTPointerOperator operator) {
		getDefaultDeclarator().addPointerOperator(operator);
	}
	
	public void setInitializer(IASTInitializer initializer) {
		getDefaultDeclarator().setInitializer(initializer);
	}

	public void setName(IASTName name) {
		getDefaultDeclarator().setName(name);
	}

	public void setNestedDeclarator(IASTDeclarator nested) {
		getDefaultDeclarator().setNestedDeclarator(nested);
	}
	
	public IASTInitializer getInitializer() {
		return getDefaultDeclarator().getInitializer();
	}

	public IASTName getName() {
		return getDefaultDeclarator().getName();
	}

	public IASTDeclarator getNestedDeclarator() {
		return getDefaultDeclarator().getNestedDeclarator();
	}

	public IASTPointerOperator[] getPointerOperators() {
		return getDefaultDeclarator().getPointerOperators();
	}

	public int getRoleForName(IASTName n) {
		return getDefaultDeclarator().getRoleForName(n);
	}


}
