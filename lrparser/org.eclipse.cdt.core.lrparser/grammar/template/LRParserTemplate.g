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


-- This template is a modified version of BtParserTemplateD.g. for use with the LR parsers.
-- This template contains a fix for an LPG bug:
-- http://sourceforge.net/tracker/index.php?func=detail&aid=1732851&group_id=155963&atid=797879



--
-- In a parser using this template, the following macro may be redefined:
--
--     $additional_interfaces
--     $ast_class
--
-- B E G I N N I N G   O F   T E M P L A T E   btParserTemplateD
--
%Options programming_language=java,margin=4,backtrack
%Options table,error_maps,scopes
%options prefix=TK_,
%options action=("*.java", "/.", "./")
%options ParseTable=lpg.lpgjavaruntime.ParseTable

--
-- This template requires that the name of the EOF token be set
-- to EOF_TOKEN to be consistent with LexerTemplateD and LexerTemplateE
--
$EOF
    EOF_TOKEN
$End

$ERROR
    ERROR_TOKEN
$End

$Define

    $Header
    /.
                //
                // Rule $rule_number:  $rule_text
                //./

    $BeginAction
    /. $Header
                case $rule_number: {./

    $EndAction
    /.          break;
                }./

    $BeginJava
    /.$BeginAction
                    $symbol_declarations./

    $EndJava /.$EndAction./

    $NoAction
    /. $Header
                case $rule_number:
                    break;./

    $BadAction
    /. $Header
                case $rule_number:
                    throw new Error("No action specified for rule " + $rule_number);./

    $NullAction
    /. $Header
                case $rule_number:
                    setResult(null);
                    break;./

    $BeginActions
    /.
        public void ruleAction(int ruleNumber)
        {
            switch (ruleNumber)
            {./

    $SplitActions
    /.
	            default:
	                ruleAction$rule_number(ruleNumber);
	                break;
	        }
	        return;
	    }
	
	    public void ruleAction$rule_number(int ruleNumber)
	    {
	        switch (ruleNumber)
	        {./

    $EndActions
    /.
                default:
                    break;
            }
            return;
        }./

    --
    -- Macros that may be needed in a parser using this template
    --
    $additional_interfaces /../
    $ast_class /.$ast_type./

    --
    -- Old deprecated macros that should NEVER be used.
    --
    $setSym1 /. // macro setSym1 is deprecated. Use function setResult
                getParser().setSym1./
    $setResult /. // macro setResult is deprecated. Use function setResult
                 getParser().setSym1./
    $getSym /. // macro getSym is deprecated. Use function getRhsSym
              getParser().getSym./
    $getToken /. // macro getToken is deprecated. Use function getRhsTokenIndex
                getParser().getToken./
    $getIToken /. // macro getIToken is deprecated. Use function getRhsIToken
                 super.getIToken./
    $getLeftSpan /. // macro getLeftSpan is deprecated. Use function getLeftSpan
                   getParser().getFirstToken./
    $getRightSpan /. // macro getRightSpan is deprecated. Use function getRightSpan
                    getParser().getLastToken./
$End

$Globals
    /.import lpg.lpgjavaruntime.*;
    ./
$End

$Headers
    /.
    public class $action_type extends PrsStream implements RuleAction, ITokenStream, 
                                                           ITokenCollector, IParser<$ast_class> 
                                                           $additional_interfaces
    {
        private static ParseTable prs = new $prs_type();
        private FixedBacktrackingParser btParser;

        public FixedBacktrackingParser getParser() { return btParser; }
        private void setResult(Object object) { btParser.setSym1(object); }
        public Object getRhsSym(int i) { return btParser.getSym(i); }

        public int getRhsTokenIndex(int i) { return btParser.getToken(i); }
        public IToken getRhsIToken(int i) { return super.getIToken(getRhsTokenIndex(i)); }
        
        public int getRhsFirstTokenIndex(int i) { return btParser.getFirstToken(i); }
        public IToken getRhsFirstIToken(int i) { return super.getIToken(getRhsFirstTokenIndex(i)); }

        public int getRhsLastTokenIndex(int i) { return btParser.getLastToken(i); }
        public IToken getRhsLastIToken(int i) { return super.getIToken(getRhsLastTokenIndex(i)); }

        public int getLeftSpan() { return btParser.getFirstToken(); }
        public IToken getLeftIToken()  { return super.getIToken(getLeftSpan()); }

        public int getRightSpan() { return btParser.getLastToken(); }
        public IToken getRightIToken() { return super.getIToken(getRightSpan()); }

        public int getRhsErrorTokenIndex(int i)
        {
            int index = btParser.getToken(i);
            IToken err = super.getIToken(index);
            return (err instanceof ErrorToken ? index : 0);
        }
        public ErrorToken getRhsErrorIToken(int i)
        {
            int index = btParser.getToken(i);
            IToken err = super.getIToken(index);
            return (ErrorToken) (err instanceof ErrorToken ? err : null);
        }

        public $action_type(LexStream lexStream)
        {
            super(lexStream);

            try
            {
                super.remapTerminalSymbols(orderedTerminalSymbols(), $prs_type.EOFT_SYMBOL);
            }
            catch(NullExportedSymbolsException e) {
            }
            catch(NullTerminalSymbolsException e) {
            }
            catch(UnimplementedTerminalsException e)
            {
                java.util.ArrayList unimplemented_symbols = e.getSymbols();
                System.out.println("The Lexer will not scan the following token(s):");
                for (int i = 0; i < unimplemented_symbols.size(); i++)
                {
                    Integer id = (Integer) unimplemented_symbols.get(i);
                    System.out.println("    " + $sym_type.orderedTerminalSymbols[id.intValue()]);               
                }
                System.out.println();                        
            }
            catch(UndefinedEofSymbolException e)
            {
                throw new Error(new UndefinedEofSymbolException
                                    ("The Lexer does not implement the Eof symbol " +
                                     $sym_type.orderedTerminalSymbols[$prs_type.EOFT_SYMBOL]));
            } 
        }

        public String[] orderedTerminalSymbols() { return $sym_type.orderedTerminalSymbols; }
        public String getTokenKindName(int kind) { return $sym_type.orderedTerminalSymbols[kind]; }
        public int getEOFTokenKind() { return $prs_type.EOFT_SYMBOL; }
        public PrsStream getParseStream() { return (PrsStream) this; }
        
        //
        // Report error message for given error_token.
        //
        public final void reportErrorTokenMessage(int error_token, String msg)
        {
            int firsttok = super.getFirstErrorToken(error_token),
                lasttok = super.getLastErrorToken(error_token);
            String location = super.getFileName() + ':' +
                              (firsttok > lasttok
                                        ? (super.getEndLine(lasttok) + ":" + super.getEndColumn(lasttok))
                                        : (super.getLine(error_token) + ":" +
                                           super.getColumn(error_token) + ":" +
                                           super.getEndLine(error_token) + ":" +
                                           super.getEndColumn(error_token)))
                              + ": ";
            super.reportError((firsttok > lasttok ? ParseErrorCodes.INSERTION_CODE : ParseErrorCodes.SUBSTITUTION_CODE), location, msg);
        }

        public void parser()
        {
            parser(null, 0);
        }
        
        public void parser(Monitor monitor)
        {
            parser(monitor, 0);
        }
        
        public void parser(int error_repair_count)
        {
            parser(null, error_repair_count);
        }

        public void parser(Monitor monitor, int error_repair_count)
        {
            try
            {
                btParser = new FixedBacktrackingParser(monitor, (TokenStream) this, prs, (RuleAction) this);
            }
            catch (NotBacktrackParseTableException e)
            {
                throw new Error(new NotBacktrackParseTableException
                                    ("Regenerate $prs_type.java with -BACKTRACK option"));
            }
            catch (BadParseSymFileException e)
            {
                throw new Error(new BadParseSymFileException("Bad Parser Symbol File -- $sym_type.java"));
            }

            try
            {
                btParser.parse(error_repair_count);
            }
            catch (BadParseException e)
            {
                reset(e.error_token); // point to error token
                DiagnoseParser diagnoseParser = new DiagnoseParser(this, prs);
                diagnoseParser.diagnose(e.error_token);
            }
        }

    ./

$End

$Rules
    /.$BeginActions./
$End

$Trailers
    /.
        $EndActions
    }
    ./
$End


$Notice
-- Copied into all files generated by LPG
/./*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *********************************************************************************/
 
 // This file was generated by LPG
./
$End


$Define
	-- These macros allow the template and header code to be customized by an extending parser.
	
	$ast_class /. IASTTranslationUnit  ./  -- override in secondary parsers
	
	$extra_interfaces /. ./  -- can override this macro to provide additional interfaces
	$additional_interfaces /.  $extra_interfaces ./
	
	$build_action_class /.  ./  -- name of the class that has the AST building callbacks
	$node_factory_create_expression /.  ./  -- expression that will create the INodeFactory
	$parser_factory_create_expression /.  ./ -- expression that will create the ISecondaryParserFactory
	
	$action_initializations /. ./
	
	$Build /. $BeginAction action. ./  -- special action just for calling methods on the builder
	$EndBuild /. $EndAction ./
$End


$Globals
/.	
	import java.util.*;
	import org.eclipse.cdt.core.dom.ast.*;
	import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
	import org.eclipse.cdt.core.dom.lrparser.IParser;
	import org.eclipse.cdt.core.dom.lrparser.ITokenCollector;
	import org.eclipse.cdt.core.dom.lrparser.CPreprocessorAdapter;
	import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
	import org.eclipse.cdt.core.dom.lrparser.lpgextensions.FixedBacktrackingParser;
	import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
	import org.eclipse.cdt.core.parser.IScanner;
	import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
	import org.eclipse.cdt.core.index.IIndex;
./
$End

$Headers
/.
	private $build_action_class action;
	private IASTCompletionNode compNode;
	

	public $action_type(IScanner scanner, IDOMTokenMap tokenMap, IBuiltinBindingsProvider builtinBindingsProvider, IIndex index, Map<String,String> properties) {
		initActions(properties);
		action.initializeTranslationUnit(scanner, builtinBindingsProvider, index);
		CPreprocessorAdapter.runCPreprocessor(scanner, this, tokenMap);
	}
	
	private void initActions(Map<String,String> properties) {
		ScopedStack<Object> astStack = new ScopedStack<Object>();
		
		action = new $build_action_class(this, astStack, $node_factory_create_expression, $parser_factory_create_expression);
		action.setParserProperties(properties);
		
		$action_initializations
	}
	
	
	public void addToken(IToken token) {
		token.setKind(mapKind(token.getKind())); // TODO does mapKind need to be called?
		super.addToken(token);
	}
	
	
	public $ast_class parse() {
		// this has to be done, or... kaboom!
		setStreamLength(getSize());
		
		final int errorRepairCount = -1;  // -1 means full error handling
		parser(null, errorRepairCount); // do the actual parse
		super.resetTokenStream(); // allow tokens to be garbage collected
	
		compNode = action.getASTCompletionNode(); // the completion node may be null
		return ($ast_class) action.getParseResult();
	}
	
	
	public IASTCompletionNode getCompletionNode() {
		return compNode;
	}

	// uncomment this method to use with backtracking parser
	public List<IToken> getRuleTokens() {
	    return getTokens().subList(getLeftSpan(), getRightSpan() + 1);
	}
	
	public String[] getOrderedTerminalSymbols() {
		return $sym_type.orderedTerminalSymbols;
	}
	
	@SuppressWarnings("nls")
	public String getName() {
		return "$action_type";
	}
	
./
$End


