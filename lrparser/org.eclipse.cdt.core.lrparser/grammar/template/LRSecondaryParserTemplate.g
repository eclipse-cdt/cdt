----------------------------------------------------------------------------------
-- Copyright (c) 2006, 2010 IBM Corporation and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl_v10.html
--
-- Contributors:
--     IBM Corporation - initial API and implementation
----------------------------------------------------------------------------------


%Options programming_language=java,margin=4,backtrack
%Options table,error_maps,scopes
%options prefix=TK_,
%options action=("*.java", "/.", "./")
%options ParseTable=lpg.lpgjavaruntime.ParseTable


-- additional code needed by secondary parsers

-- path is relative to the grammar file that uses the template
$Include
../template/LRParserTemplate.g
$End




$Define

    $additional_interfaces /. , ISecondaryParser<$ast_class> $extra_interfaces ./

$End

$Globals
/.
    import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
    import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
    import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
./
$End

$Headers
/.

	private ITokenMap tokenMap = null;
	
	public void setTokens(List<IToken> tokens) {
		resetTokenStream();
		addToken(new Token(null, 0, 0, 0)); // dummy token
		for(IToken token : tokens) {
			token.setKind(tokenMap.mapKind(token.getKind()));
			addToken(token);
		}
		addToken(new Token(null, 0, 0, $sym_type.TK_EOF_TOKEN));
	}
	
	public $action_type(ITokenStream stream, Map<String,String> properties) {  // constructor for creating secondary parser
		initActions(properties);
		tokenMap = new TokenMap($sym_type.orderedTerminalSymbols, stream.getOrderedTerminalSymbols());
	}	
	
./
$End

