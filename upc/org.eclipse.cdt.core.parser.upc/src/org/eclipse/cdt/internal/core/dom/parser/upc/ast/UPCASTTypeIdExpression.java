package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTTypeIdExpression;

@SuppressWarnings("restriction")
public class UPCASTTypeIdExpression extends CASTTypeIdExpression implements
		IUPCASTTypeIdExpression {

	public UPCASTTypeIdExpression() {
	}

	public UPCASTTypeIdExpression(int op, IASTTypeId typeId) {
		super(op, typeId);
	}

}
