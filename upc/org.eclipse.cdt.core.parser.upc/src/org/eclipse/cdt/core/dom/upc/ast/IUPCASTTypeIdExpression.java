package org.eclipse.cdt.core.dom.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;

public interface IUPCASTTypeIdExpression extends IASTTypeIdExpression {

	public final int op_upc_localsizeof = op_last + 1;
	
	public final int op_upc_blocksizeof = op_last + 2;
	
	public final int op_upc_elemsizeof  = op_last + 3;
}
