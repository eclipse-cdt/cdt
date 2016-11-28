package org.eclipse.cdt.linkerscript.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.eclipse.cdt.linkerscript.services.LinkerScriptGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
@SuppressWarnings("all")
public class InternalLinkerScriptParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_DEC", "RULE_HEX", "RULE_ML_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "';'", "','", "'STARTUP'", "'('", "')'", "'ENTRY'", "'ASSERT'", "'TARGET'", "'SEARCH_DIR'", "'OUTPUT'", "'OUTPUT_FORMAT'", "'OUTPUT_ARCH'", "'FORCE_COMMON_ALLOCATION'", "'INHIBIT_COMMON_ALLOCATION'", "'INPUT'", "'GROUP'", "'MAP'", "'NOCROSSREFS'", "'NOCROSSREFS_TO'", "'EXTERN'", "'INCLUDE'", "'AS_NEEDED'", "'-l'", "'PHDRS'", "'{'", "'}'", "'SECTIONS'", "':'", "'AT'", "'SUBALIGN'", "'>'", "'='", "'ALIGN'", "'ALIGN_WITH_INPUT'", "'ONLY_IF_RO'", "'ONLY_IF_RW'", "'SPECIAL'", "'NOLOAD'", "'DSECT'", "'COPY'", "'INFO'", "'OVERLAY'", "'CREATE_OBJECT_SYMBOLS'", "'CONSTRUCTORS'", "'SORT_BY_NAME'", "'FILL'", "'BYTE'", "'SHORT'", "'LONG'", "'QUAD'", "'SQUAD'", "'HIDDEN'", "'PROVIDE'", "'PROVIDE_HIDDEN'", "'+='", "'-='", "'*='", "'/='", "'<'", "'>='", "'&='", "'|='", "'INPUT_SECTION_FLAGS'", "'&'", "'KEEP'", "'EXCLUDE_FILE'", "'MEMORY'", "'ORIGIN'", "'org'", "'o'", "'LENGTH'", "'len'", "'l'", "'!'", "'?'", "'||'", "'&&'", "'|'", "'=='", "'!='", "'+'", "'-'", "'*'", "'/'", "'%'", "'~'", "'SIZEOF'", "'SORT_NONE'", "'SORT'", "'SORT_BY_ALIGNMENT'", "'SORT_BY_INIT_PRIORITY'"
    };
    public static final int RULE_HEX=6;
    public static final int T__50=50;
    public static final int T__59=59;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int RULE_ID=4;
    public static final int RULE_DEC=5;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__91=91;
    public static final int T__100=100;
    public static final int T__92=92;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__90=90;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__99=99;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int T__95=95;
    public static final int T__96=96;
    public static final int T__97=97;
    public static final int T__10=10;
    public static final int T__98=98;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__73=73;
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int RULE_WS=8;
    public static final int RULE_ANY_OTHER=9;
    public static final int T__88=88;
    public static final int T__89=89;
    public static final int T__84=84;
    public static final int T__85=85;
    public static final int T__86=86;
    public static final int T__87=87;

    // delegates
    // delegators


        public InternalLinkerScriptParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalLinkerScriptParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalLinkerScriptParser.tokenNames; }
    public String getGrammarFileName() { return "InternalLinkerScript.g"; }



     	private LinkerScriptGrammarAccess grammarAccess;

        public InternalLinkerScriptParser(TokenStream input, LinkerScriptGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }

        @Override
        protected String getFirstRuleName() {
        	return "LinkerScript";
       	}

       	@Override
       	protected LinkerScriptGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}




    // $ANTLR start "entryRuleLinkerScript"
    // InternalLinkerScript.g:74:1: entryRuleLinkerScript returns [EObject current=null] : iv_ruleLinkerScript= ruleLinkerScript EOF ;
    public final EObject entryRuleLinkerScript() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLinkerScript = null;


        try {
            // InternalLinkerScript.g:74:53: (iv_ruleLinkerScript= ruleLinkerScript EOF )
            // InternalLinkerScript.g:75:2: iv_ruleLinkerScript= ruleLinkerScript EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLinkerScriptRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLinkerScript=ruleLinkerScript();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLinkerScript; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLinkerScript"


    // $ANTLR start "ruleLinkerScript"
    // InternalLinkerScript.g:81:1: ruleLinkerScript returns [EObject current=null] : ( (lv_statements_0_0= ruleLinkerScriptStatement ) )* ;
    public final EObject ruleLinkerScript() throws RecognitionException {
        EObject current = null;

        EObject lv_statements_0_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:87:2: ( ( (lv_statements_0_0= ruleLinkerScriptStatement ) )* )
            // InternalLinkerScript.g:88:2: ( (lv_statements_0_0= ruleLinkerScriptStatement ) )*
            {
            // InternalLinkerScript.g:88:2: ( (lv_statements_0_0= ruleLinkerScriptStatement ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_ID||LA1_0==10||LA1_0==12||(LA1_0>=15 && LA1_0<=30)||LA1_0==33||LA1_0==36||(LA1_0>=61 && LA1_0<=63)||LA1_0==76||(LA1_0>=78 && LA1_0<=79)||(LA1_0>=81 && LA1_0<=82)||LA1_0==92) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalLinkerScript.g:89:3: (lv_statements_0_0= ruleLinkerScriptStatement )
            	    {
            	    // InternalLinkerScript.g:89:3: (lv_statements_0_0= ruleLinkerScriptStatement )
            	    // InternalLinkerScript.g:90:4: lv_statements_0_0= ruleLinkerScriptStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      				newCompositeNode(grammarAccess.getLinkerScriptAccess().getStatementsLinkerScriptStatementParserRuleCall_0());
            	      			
            	    }
            	    pushFollow(FOLLOW_3);
            	    lv_statements_0_0=ruleLinkerScriptStatement();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				if (current==null) {
            	      					current = createModelElementForParent(grammarAccess.getLinkerScriptRule());
            	      				}
            	      				add(
            	      					current,
            	      					"statements",
            	      					lv_statements_0_0,
            	      					"org.eclipse.cdt.linkerscript.LinkerScript.LinkerScriptStatement");
            	      				afterParserOrEnumRuleCall();
            	      			
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLinkerScript"


    // $ANTLR start "entryRuleLinkerScriptStatement"
    // InternalLinkerScript.g:110:1: entryRuleLinkerScriptStatement returns [EObject current=null] : iv_ruleLinkerScriptStatement= ruleLinkerScriptStatement EOF ;
    public final EObject entryRuleLinkerScriptStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLinkerScriptStatement = null;


        try {
            // InternalLinkerScript.g:110:62: (iv_ruleLinkerScriptStatement= ruleLinkerScriptStatement EOF )
            // InternalLinkerScript.g:111:2: iv_ruleLinkerScriptStatement= ruleLinkerScriptStatement EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLinkerScriptStatementRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLinkerScriptStatement=ruleLinkerScriptStatement();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLinkerScriptStatement; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLinkerScriptStatement"


    // $ANTLR start "ruleLinkerScriptStatement"
    // InternalLinkerScript.g:117:1: ruleLinkerScriptStatement returns [EObject current=null] : (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand ) ;
    public final EObject ruleLinkerScriptStatement() throws RecognitionException {
        EObject current = null;

        EObject this_MemoryCommand_0 = null;

        EObject this_SectionsCommand_1 = null;

        EObject this_PhdrsCommand_2 = null;

        EObject this_StatementCommand_3 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:123:2: ( (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand ) )
            // InternalLinkerScript.g:124:2: (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand )
            {
            // InternalLinkerScript.g:124:2: (this_MemoryCommand_0= ruleMemoryCommand | this_SectionsCommand_1= ruleSectionsCommand | this_PhdrsCommand_2= rulePhdrsCommand | this_StatementCommand_3= ruleStatementCommand )
            int alt2=4;
            switch ( input.LA(1) ) {
            case 76:
                {
                int LA2_1 = input.LA(2);

                if ( ((LA2_1>=40 && LA2_1<=41)||(LA2_1>=64 && LA2_1<=68)||(LA2_1>=70 && LA2_1<=71)) ) {
                    alt2=4;
                }
                else if ( (LA2_1==34) ) {
                    alt2=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;
                }
                }
                break;
            case 36:
                {
                alt2=2;
                }
                break;
            case 33:
                {
                alt2=3;
                }
                break;
            case RULE_ID:
            case 10:
            case 12:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 61:
            case 62:
            case 63:
            case 78:
            case 79:
            case 81:
            case 82:
            case 92:
                {
                alt2=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // InternalLinkerScript.g:125:3: this_MemoryCommand_0= ruleMemoryCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getMemoryCommandParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_MemoryCommand_0=ruleMemoryCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_MemoryCommand_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:134:3: this_SectionsCommand_1= ruleSectionsCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getSectionsCommandParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_SectionsCommand_1=ruleSectionsCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_SectionsCommand_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:143:3: this_PhdrsCommand_2= rulePhdrsCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getPhdrsCommandParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_PhdrsCommand_2=rulePhdrsCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_PhdrsCommand_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:152:3: this_StatementCommand_3= ruleStatementCommand
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLinkerScriptStatementAccess().getStatementCommandParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StatementCommand_3=ruleStatementCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StatementCommand_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLinkerScriptStatement"


    // $ANTLR start "entryRuleStatementCommand"
    // InternalLinkerScript.g:164:1: entryRuleStatementCommand returns [EObject current=null] : iv_ruleStatementCommand= ruleStatementCommand EOF ;
    public final EObject entryRuleStatementCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatementCommand = null;


        try {
            // InternalLinkerScript.g:164:57: (iv_ruleStatementCommand= ruleStatementCommand EOF )
            // InternalLinkerScript.g:165:2: iv_ruleStatementCommand= ruleStatementCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatementCommand=ruleStatementCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatementCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatementCommand"


    // $ANTLR start "ruleStatementCommand"
    // InternalLinkerScript.g:171:1: ruleStatementCommand returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) ) ;
    public final EObject ruleStatementCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_23=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_28=null;
        Token otherlv_30=null;
        Token otherlv_32=null;
        Token otherlv_33=null;
        Token otherlv_35=null;
        Token otherlv_37=null;
        Token otherlv_38=null;
        Token otherlv_40=null;
        Token otherlv_42=null;
        Token otherlv_44=null;
        Token otherlv_46=null;
        Token otherlv_47=null;
        Token otherlv_49=null;
        Token otherlv_51=null;
        Token otherlv_53=null;
        Token otherlv_55=null;
        Token otherlv_56=null;
        Token otherlv_58=null;
        Token otherlv_60=null;
        Token otherlv_61=null;
        Token otherlv_63=null;
        Token otherlv_65=null;
        Token otherlv_67=null;
        Token otherlv_68=null;
        Token otherlv_70=null;
        Token otherlv_72=null;
        Token otherlv_73=null;
        Token otherlv_75=null;
        Token otherlv_77=null;
        Token otherlv_78=null;
        Token otherlv_80=null;
        Token otherlv_82=null;
        Token otherlv_83=null;
        Token otherlv_85=null;
        Token otherlv_87=null;
        Token otherlv_90=null;
        EObject lv_assignment_1_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        AntlrDatatypeRuleToken lv_name_12_0 = null;

        EObject lv_exp_17_0 = null;

        AntlrDatatypeRuleToken lv_message_19_0 = null;

        AntlrDatatypeRuleToken lv_name_24_0 = null;

        AntlrDatatypeRuleToken lv_name_29_0 = null;

        AntlrDatatypeRuleToken lv_name_34_0 = null;

        AntlrDatatypeRuleToken lv_name_39_0 = null;

        AntlrDatatypeRuleToken lv_big_41_0 = null;

        AntlrDatatypeRuleToken lv_little_43_0 = null;

        AntlrDatatypeRuleToken lv_name_48_0 = null;

        EObject lv_list_57_0 = null;

        EObject lv_files_62_0 = null;

        EObject lv_files_64_0 = null;

        AntlrDatatypeRuleToken lv_name_69_0 = null;

        AntlrDatatypeRuleToken lv_sections_74_0 = null;

        AntlrDatatypeRuleToken lv_sections_79_0 = null;

        AntlrDatatypeRuleToken lv_sections_84_0 = null;

        AntlrDatatypeRuleToken lv_name_88_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:177:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) ) )
            // InternalLinkerScript.g:178:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) )
            {
            // InternalLinkerScript.g:178:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' ) | ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' ) | ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' ) | ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' ) | ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' ) | ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' ) | ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' ) | ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' ) | ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' ) | ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' ) | ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' ) | ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' ) | ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' ) | ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' ) | ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' ) | ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) ) | ( () otherlv_90= ';' ) )
            int alt10=19;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 61:
            case 62:
            case 63:
            case 76:
            case 78:
            case 79:
            case 81:
            case 82:
            case 92:
                {
                alt10=1;
                }
                break;
            case 12:
                {
                alt10=2;
                }
                break;
            case 15:
                {
                alt10=3;
                }
                break;
            case 16:
                {
                alt10=4;
                }
                break;
            case 17:
                {
                alt10=5;
                }
                break;
            case 18:
                {
                alt10=6;
                }
                break;
            case 19:
                {
                alt10=7;
                }
                break;
            case 20:
                {
                alt10=8;
                }
                break;
            case 21:
                {
                alt10=9;
                }
                break;
            case 22:
                {
                alt10=10;
                }
                break;
            case 23:
                {
                alt10=11;
                }
                break;
            case 24:
                {
                alt10=12;
                }
                break;
            case 25:
                {
                alt10=13;
                }
                break;
            case 26:
                {
                alt10=14;
                }
                break;
            case 27:
                {
                alt10=15;
                }
                break;
            case 28:
                {
                alt10=16;
                }
                break;
            case 29:
                {
                alt10=17;
                }
                break;
            case 30:
                {
                alt10=18;
                }
                break;
            case 10:
                {
                alt10=19;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }

            switch (alt10) {
                case 1 :
                    // InternalLinkerScript.g:179:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) )
                    {
                    // InternalLinkerScript.g:179:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) )
                    // InternalLinkerScript.g:180:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' )
                    {
                    // InternalLinkerScript.g:180:4: ()
                    // InternalLinkerScript.g:181:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:187:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:188:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:188:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:189:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
                    lv_assignment_1_0=ruleAssignmentRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"assignment",
                      							lv_assignment_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.AssignmentRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:206:4: (otherlv_2= ';' | otherlv_3= ',' )
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==10) ) {
                        alt3=1;
                    }
                    else if ( (LA3_0==11) ) {
                        alt3=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 3, 0, input);

                        throw nvae;
                    }
                    switch (alt3) {
                        case 1 :
                            // InternalLinkerScript.g:207:5: otherlv_2= ';'
                            {
                            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementCommandAccess().getSemicolonKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:212:5: otherlv_3= ','
                            {
                            otherlv_3=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementCommandAccess().getCommaKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:219:3: ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' )
                    {
                    // InternalLinkerScript.g:219:3: ( () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')' )
                    // InternalLinkerScript.g:220:4: () otherlv_5= 'STARTUP' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) otherlv_8= ')'
                    {
                    // InternalLinkerScript.g:220:4: ()
                    // InternalLinkerScript.g:221:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementStartupAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,12,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementCommandAccess().getSTARTUPKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:235:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:236:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:236:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:237:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameWildIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_8=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_1_4());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:260:3: ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' )
                    {
                    // InternalLinkerScript.g:260:3: ( () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')' )
                    // InternalLinkerScript.g:261:4: () otherlv_10= 'ENTRY' otherlv_11= '(' ( (lv_name_12_0= ruleValidID ) ) otherlv_13= ')'
                    {
                    // InternalLinkerScript.g:261:4: ()
                    // InternalLinkerScript.g:262:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementEntryAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_10=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementCommandAccess().getENTRYKeyword_2_1());
                      			
                    }
                    otherlv_11=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:276:4: ( (lv_name_12_0= ruleValidID ) )
                    // InternalLinkerScript.g:277:5: (lv_name_12_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:277:5: (lv_name_12_0= ruleValidID )
                    // InternalLinkerScript.g:278:6: lv_name_12_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_12_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_12_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_13=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_2_4());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:301:3: ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' )
                    {
                    // InternalLinkerScript.g:301:3: ( () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')' )
                    // InternalLinkerScript.g:302:4: () otherlv_15= 'ASSERT' otherlv_16= '(' ( (lv_exp_17_0= ruleLExpression ) ) otherlv_18= ',' ( (lv_message_19_0= ruleValidID ) ) otherlv_20= ')'
                    {
                    // InternalLinkerScript.g:302:4: ()
                    // InternalLinkerScript.g:303:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementAssertAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_15=(Token)match(input,16,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementCommandAccess().getASSERTKeyword_3_1());
                      			
                    }
                    otherlv_16=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_16, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:317:4: ( (lv_exp_17_0= ruleLExpression ) )
                    // InternalLinkerScript.g:318:5: (lv_exp_17_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:318:5: (lv_exp_17_0= ruleLExpression )
                    // InternalLinkerScript.g:319:6: lv_exp_17_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getExpLExpressionParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_exp_17_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_17_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_18=(Token)match(input,11,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_18, grammarAccess.getStatementCommandAccess().getCommaKeyword_3_4());
                      			
                    }
                    // InternalLinkerScript.g:340:4: ( (lv_message_19_0= ruleValidID ) )
                    // InternalLinkerScript.g:341:5: (lv_message_19_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:341:5: (lv_message_19_0= ruleValidID )
                    // InternalLinkerScript.g:342:6: lv_message_19_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getMessageValidIDParserRuleCall_3_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_message_19_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"message",
                      							lv_message_19_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_20=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_3_6());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:365:3: ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' )
                    {
                    // InternalLinkerScript.g:365:3: ( () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')' )
                    // InternalLinkerScript.g:366:4: () otherlv_22= 'TARGET' otherlv_23= '(' ( (lv_name_24_0= ruleValidID ) ) otherlv_25= ')'
                    {
                    // InternalLinkerScript.g:366:4: ()
                    // InternalLinkerScript.g:367:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementTargetAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_22=(Token)match(input,17,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getStatementCommandAccess().getTARGETKeyword_4_1());
                      			
                    }
                    otherlv_23=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_23, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:381:4: ( (lv_name_24_0= ruleValidID ) )
                    // InternalLinkerScript.g:382:5: (lv_name_24_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:382:5: (lv_name_24_0= ruleValidID )
                    // InternalLinkerScript.g:383:6: lv_name_24_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_24_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_24_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_25=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_4_4());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:406:3: ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' )
                    {
                    // InternalLinkerScript.g:406:3: ( () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')' )
                    // InternalLinkerScript.g:407:4: () otherlv_27= 'SEARCH_DIR' otherlv_28= '(' ( (lv_name_29_0= ruleValidID ) ) otherlv_30= ')'
                    {
                    // InternalLinkerScript.g:407:4: ()
                    // InternalLinkerScript.g:408:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementSearchDirAction_5_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_27=(Token)match(input,18,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getStatementCommandAccess().getSEARCH_DIRKeyword_5_1());
                      			
                    }
                    otherlv_28=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_28, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    // InternalLinkerScript.g:422:4: ( (lv_name_29_0= ruleValidID ) )
                    // InternalLinkerScript.g:423:5: (lv_name_29_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:423:5: (lv_name_29_0= ruleValidID )
                    // InternalLinkerScript.g:424:6: lv_name_29_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_5_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_29_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_29_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_30=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_30, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_5_4());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:447:3: ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' )
                    {
                    // InternalLinkerScript.g:447:3: ( () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')' )
                    // InternalLinkerScript.g:448:4: () otherlv_32= 'OUTPUT' otherlv_33= '(' ( (lv_name_34_0= ruleValidID ) ) otherlv_35= ')'
                    {
                    // InternalLinkerScript.g:448:4: ()
                    // InternalLinkerScript.g:449:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementOutputAction_6_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_32=(Token)match(input,19,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_32, grammarAccess.getStatementCommandAccess().getOUTPUTKeyword_6_1());
                      			
                    }
                    otherlv_33=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_33, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    // InternalLinkerScript.g:463:4: ( (lv_name_34_0= ruleValidID ) )
                    // InternalLinkerScript.g:464:5: (lv_name_34_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:464:5: (lv_name_34_0= ruleValidID )
                    // InternalLinkerScript.g:465:6: lv_name_34_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_6_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_34_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_34_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_35=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_35, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_6_4());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:488:3: ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' )
                    {
                    // InternalLinkerScript.g:488:3: ( () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')' )
                    // InternalLinkerScript.g:489:4: () otherlv_37= 'OUTPUT_FORMAT' otherlv_38= '(' ( (lv_name_39_0= ruleValidID ) ) (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )? otherlv_44= ')'
                    {
                    // InternalLinkerScript.g:489:4: ()
                    // InternalLinkerScript.g:490:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementOutputFormatAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_37=(Token)match(input,20,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_37, grammarAccess.getStatementCommandAccess().getOUTPUT_FORMATKeyword_7_1());
                      			
                    }
                    otherlv_38=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_38, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_7_2());
                      			
                    }
                    // InternalLinkerScript.g:504:4: ( (lv_name_39_0= ruleValidID ) )
                    // InternalLinkerScript.g:505:5: (lv_name_39_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:505:5: (lv_name_39_0= ruleValidID )
                    // InternalLinkerScript.g:506:6: lv_name_39_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_7_3_0());
                      					
                    }
                    pushFollow(FOLLOW_10);
                    lv_name_39_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_39_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:523:4: (otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) ) )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==11) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // InternalLinkerScript.g:524:5: otherlv_40= ',' ( (lv_big_41_0= ruleValidID ) ) otherlv_42= ',' ( (lv_little_43_0= ruleValidID ) )
                            {
                            otherlv_40=(Token)match(input,11,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_40, grammarAccess.getStatementCommandAccess().getCommaKeyword_7_4_0());
                              				
                            }
                            // InternalLinkerScript.g:528:5: ( (lv_big_41_0= ruleValidID ) )
                            // InternalLinkerScript.g:529:6: (lv_big_41_0= ruleValidID )
                            {
                            // InternalLinkerScript.g:529:6: (lv_big_41_0= ruleValidID )
                            // InternalLinkerScript.g:530:7: lv_big_41_0= ruleValidID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getStatementCommandAccess().getBigValidIDParserRuleCall_7_4_1_0());
                              						
                            }
                            pushFollow(FOLLOW_9);
                            lv_big_41_0=ruleValidID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                              							}
                              							set(
                              								current,
                              								"big",
                              								lv_big_41_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            otherlv_42=(Token)match(input,11,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_42, grammarAccess.getStatementCommandAccess().getCommaKeyword_7_4_2());
                              				
                            }
                            // InternalLinkerScript.g:551:5: ( (lv_little_43_0= ruleValidID ) )
                            // InternalLinkerScript.g:552:6: (lv_little_43_0= ruleValidID )
                            {
                            // InternalLinkerScript.g:552:6: (lv_little_43_0= ruleValidID )
                            // InternalLinkerScript.g:553:7: lv_little_43_0= ruleValidID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getStatementCommandAccess().getLittleValidIDParserRuleCall_7_4_3_0());
                              						
                            }
                            pushFollow(FOLLOW_7);
                            lv_little_43_0=ruleValidID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                              							}
                              							set(
                              								current,
                              								"little",
                              								lv_little_43_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }


                            }
                            break;

                    }

                    otherlv_44=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_44, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_7_5());
                      			
                    }

                    }


                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:577:3: ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' )
                    {
                    // InternalLinkerScript.g:577:3: ( () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')' )
                    // InternalLinkerScript.g:578:4: () otherlv_46= 'OUTPUT_ARCH' otherlv_47= '(' ( (lv_name_48_0= ruleValidID ) ) otherlv_49= ')'
                    {
                    // InternalLinkerScript.g:578:4: ()
                    // InternalLinkerScript.g:579:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementOutputArchAction_8_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_46=(Token)match(input,21,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_46, grammarAccess.getStatementCommandAccess().getOUTPUT_ARCHKeyword_8_1());
                      			
                    }
                    otherlv_47=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_47, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_8_2());
                      			
                    }
                    // InternalLinkerScript.g:593:4: ( (lv_name_48_0= ruleValidID ) )
                    // InternalLinkerScript.g:594:5: (lv_name_48_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:594:5: (lv_name_48_0= ruleValidID )
                    // InternalLinkerScript.g:595:6: lv_name_48_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_8_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_48_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_48_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_49=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_49, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_8_4());
                      			
                    }

                    }


                    }
                    break;
                case 10 :
                    // InternalLinkerScript.g:618:3: ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' )
                    {
                    // InternalLinkerScript.g:618:3: ( () otherlv_51= 'FORCE_COMMON_ALLOCATION' )
                    // InternalLinkerScript.g:619:4: () otherlv_51= 'FORCE_COMMON_ALLOCATION'
                    {
                    // InternalLinkerScript.g:619:4: ()
                    // InternalLinkerScript.g:620:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementForceCommonAllocationAction_9_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_51=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_51, grammarAccess.getStatementCommandAccess().getFORCE_COMMON_ALLOCATIONKeyword_9_1());
                      			
                    }

                    }


                    }
                    break;
                case 11 :
                    // InternalLinkerScript.g:632:3: ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' )
                    {
                    // InternalLinkerScript.g:632:3: ( () otherlv_53= 'INHIBIT_COMMON_ALLOCATION' )
                    // InternalLinkerScript.g:633:4: () otherlv_53= 'INHIBIT_COMMON_ALLOCATION'
                    {
                    // InternalLinkerScript.g:633:4: ()
                    // InternalLinkerScript.g:634:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementInhibitCommonAllocationAction_10_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_53=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_53, grammarAccess.getStatementCommandAccess().getINHIBIT_COMMON_ALLOCATIONKeyword_10_1());
                      			
                    }

                    }


                    }
                    break;
                case 12 :
                    // InternalLinkerScript.g:646:3: ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' )
                    {
                    // InternalLinkerScript.g:646:3: ( () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')' )
                    // InternalLinkerScript.g:647:4: () otherlv_55= 'INPUT' otherlv_56= '(' ( (lv_list_57_0= ruleFileList ) ) otherlv_58= ')'
                    {
                    // InternalLinkerScript.g:647:4: ()
                    // InternalLinkerScript.g:648:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementInputAction_11_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_55=(Token)match(input,24,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_55, grammarAccess.getStatementCommandAccess().getINPUTKeyword_11_1());
                      			
                    }
                    otherlv_56=(Token)match(input,13,FOLLOW_11); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_56, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_11_2());
                      			
                    }
                    // InternalLinkerScript.g:662:4: ( (lv_list_57_0= ruleFileList ) )
                    // InternalLinkerScript.g:663:5: (lv_list_57_0= ruleFileList )
                    {
                    // InternalLinkerScript.g:663:5: (lv_list_57_0= ruleFileList )
                    // InternalLinkerScript.g:664:6: lv_list_57_0= ruleFileList
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getListFileListParserRuleCall_11_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_list_57_0=ruleFileList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"list",
                      							lv_list_57_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileList");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_58=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_58, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_11_4());
                      			
                    }

                    }


                    }
                    break;
                case 13 :
                    // InternalLinkerScript.g:687:3: ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' )
                    {
                    // InternalLinkerScript.g:687:3: ( () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')' )
                    // InternalLinkerScript.g:688:4: () otherlv_60= 'GROUP' otherlv_61= '(' ( (lv_files_62_0= ruleFileListName ) ) ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )* otherlv_65= ')'
                    {
                    // InternalLinkerScript.g:688:4: ()
                    // InternalLinkerScript.g:689:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementGroupAction_12_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_60=(Token)match(input,25,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_60, grammarAccess.getStatementCommandAccess().getGROUPKeyword_12_1());
                      			
                    }
                    otherlv_61=(Token)match(input,13,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_61, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_12_2());
                      			
                    }
                    // InternalLinkerScript.g:703:4: ( (lv_files_62_0= ruleFileListName ) )
                    // InternalLinkerScript.g:704:5: (lv_files_62_0= ruleFileListName )
                    {
                    // InternalLinkerScript.g:704:5: (lv_files_62_0= ruleFileListName )
                    // InternalLinkerScript.g:705:6: lv_files_62_0= ruleFileListName
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getFilesFileListNameParserRuleCall_12_3_0());
                      					
                    }
                    pushFollow(FOLLOW_13);
                    lv_files_62_0=ruleFileListName();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						add(
                      							current,
                      							"files",
                      							lv_files_62_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:722:4: ( (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) ) )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==RULE_ID||LA6_0==11||LA6_0==32||LA6_0==76||(LA6_0>=78 && LA6_0<=79)||(LA6_0>=81 && LA6_0<=82)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // InternalLinkerScript.g:723:5: (otherlv_63= ',' )? ( (lv_files_64_0= ruleFileListName ) )
                    	    {
                    	    // InternalLinkerScript.g:723:5: (otherlv_63= ',' )?
                    	    int alt5=2;
                    	    int LA5_0 = input.LA(1);

                    	    if ( (LA5_0==11) ) {
                    	        alt5=1;
                    	    }
                    	    switch (alt5) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:724:6: otherlv_63= ','
                    	            {
                    	            otherlv_63=(Token)match(input,11,FOLLOW_12); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_63, grammarAccess.getStatementCommandAccess().getCommaKeyword_12_4_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:729:5: ( (lv_files_64_0= ruleFileListName ) )
                    	    // InternalLinkerScript.g:730:6: (lv_files_64_0= ruleFileListName )
                    	    {
                    	    // InternalLinkerScript.g:730:6: (lv_files_64_0= ruleFileListName )
                    	    // InternalLinkerScript.g:731:7: lv_files_64_0= ruleFileListName
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getStatementCommandAccess().getFilesFileListNameParserRuleCall_12_4_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_13);
                    	    lv_files_64_0=ruleFileListName();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"files",
                    	      								lv_files_64_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    otherlv_65=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_65, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_12_5());
                      			
                    }

                    }


                    }
                    break;
                case 14 :
                    // InternalLinkerScript.g:755:3: ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' )
                    {
                    // InternalLinkerScript.g:755:3: ( () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')' )
                    // InternalLinkerScript.g:756:4: () otherlv_67= 'MAP' otherlv_68= '(' ( (lv_name_69_0= ruleValidID ) ) otherlv_70= ')'
                    {
                    // InternalLinkerScript.g:756:4: ()
                    // InternalLinkerScript.g:757:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementMapAction_13_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_67=(Token)match(input,26,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_67, grammarAccess.getStatementCommandAccess().getMAPKeyword_13_1());
                      			
                    }
                    otherlv_68=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_68, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_13_2());
                      			
                    }
                    // InternalLinkerScript.g:771:4: ( (lv_name_69_0= ruleValidID ) )
                    // InternalLinkerScript.g:772:5: (lv_name_69_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:772:5: (lv_name_69_0= ruleValidID )
                    // InternalLinkerScript.g:773:6: lv_name_69_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_13_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_69_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_69_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_70=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_70, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_13_4());
                      			
                    }

                    }


                    }
                    break;
                case 15 :
                    // InternalLinkerScript.g:796:3: ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' )
                    {
                    // InternalLinkerScript.g:796:3: ( () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')' )
                    // InternalLinkerScript.g:797:4: () otherlv_72= 'NOCROSSREFS' otherlv_73= '(' ( (lv_sections_74_0= ruleValidID ) )* otherlv_75= ')'
                    {
                    // InternalLinkerScript.g:797:4: ()
                    // InternalLinkerScript.g:798:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementNoCrossRefsAction_14_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_72=(Token)match(input,27,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_72, grammarAccess.getStatementCommandAccess().getNOCROSSREFSKeyword_14_1());
                      			
                    }
                    otherlv_73=(Token)match(input,13,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_73, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_14_2());
                      			
                    }
                    // InternalLinkerScript.g:812:4: ( (lv_sections_74_0= ruleValidID ) )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==RULE_ID||LA7_0==76||(LA7_0>=78 && LA7_0<=79)||(LA7_0>=81 && LA7_0<=82)) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // InternalLinkerScript.g:813:5: (lv_sections_74_0= ruleValidID )
                    	    {
                    	    // InternalLinkerScript.g:813:5: (lv_sections_74_0= ruleValidID )
                    	    // InternalLinkerScript.g:814:6: lv_sections_74_0= ruleValidID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getStatementCommandAccess().getSectionsValidIDParserRuleCall_14_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_sections_74_0=ruleValidID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"sections",
                    	      							lv_sections_74_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);

                    otherlv_75=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_75, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_14_4());
                      			
                    }

                    }


                    }
                    break;
                case 16 :
                    // InternalLinkerScript.g:837:3: ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' )
                    {
                    // InternalLinkerScript.g:837:3: ( () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')' )
                    // InternalLinkerScript.g:838:4: () otherlv_77= 'NOCROSSREFS_TO' otherlv_78= '(' ( (lv_sections_79_0= ruleValidID ) )* otherlv_80= ')'
                    {
                    // InternalLinkerScript.g:838:4: ()
                    // InternalLinkerScript.g:839:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementNoCrossRefsToAction_15_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_77=(Token)match(input,28,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_77, grammarAccess.getStatementCommandAccess().getNOCROSSREFS_TOKeyword_15_1());
                      			
                    }
                    otherlv_78=(Token)match(input,13,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_78, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_15_2());
                      			
                    }
                    // InternalLinkerScript.g:853:4: ( (lv_sections_79_0= ruleValidID ) )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0==RULE_ID||LA8_0==76||(LA8_0>=78 && LA8_0<=79)||(LA8_0>=81 && LA8_0<=82)) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // InternalLinkerScript.g:854:5: (lv_sections_79_0= ruleValidID )
                    	    {
                    	    // InternalLinkerScript.g:854:5: (lv_sections_79_0= ruleValidID )
                    	    // InternalLinkerScript.g:855:6: lv_sections_79_0= ruleValidID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getStatementCommandAccess().getSectionsValidIDParserRuleCall_15_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_sections_79_0=ruleValidID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"sections",
                    	      							lv_sections_79_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);

                    otherlv_80=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_80, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_15_4());
                      			
                    }

                    }


                    }
                    break;
                case 17 :
                    // InternalLinkerScript.g:878:3: ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' )
                    {
                    // InternalLinkerScript.g:878:3: ( () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')' )
                    // InternalLinkerScript.g:879:4: () otherlv_82= 'EXTERN' otherlv_83= '(' ( (lv_sections_84_0= ruleValidID ) )* otherlv_85= ')'
                    {
                    // InternalLinkerScript.g:879:4: ()
                    // InternalLinkerScript.g:880:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementExternAction_16_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_82=(Token)match(input,29,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_82, grammarAccess.getStatementCommandAccess().getEXTERNKeyword_16_1());
                      			
                    }
                    otherlv_83=(Token)match(input,13,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_83, grammarAccess.getStatementCommandAccess().getLeftParenthesisKeyword_16_2());
                      			
                    }
                    // InternalLinkerScript.g:894:4: ( (lv_sections_84_0= ruleValidID ) )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0==RULE_ID||LA9_0==76||(LA9_0>=78 && LA9_0<=79)||(LA9_0>=81 && LA9_0<=82)) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // InternalLinkerScript.g:895:5: (lv_sections_84_0= ruleValidID )
                    	    {
                    	    // InternalLinkerScript.g:895:5: (lv_sections_84_0= ruleValidID )
                    	    // InternalLinkerScript.g:896:6: lv_sections_84_0= ruleValidID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getStatementCommandAccess().getSectionsValidIDParserRuleCall_16_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_sections_84_0=ruleValidID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"sections",
                    	      							lv_sections_84_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    otherlv_85=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_85, grammarAccess.getStatementCommandAccess().getRightParenthesisKeyword_16_4());
                      			
                    }

                    }


                    }
                    break;
                case 18 :
                    // InternalLinkerScript.g:919:3: ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) )
                    {
                    // InternalLinkerScript.g:919:3: ( () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) ) )
                    // InternalLinkerScript.g:920:4: () otherlv_87= 'INCLUDE' ( (lv_name_88_0= ruleValidID ) )
                    {
                    // InternalLinkerScript.g:920:4: ()
                    // InternalLinkerScript.g:921:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementIncludeAction_17_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_87=(Token)match(input,30,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_87, grammarAccess.getStatementCommandAccess().getINCLUDEKeyword_17_1());
                      			
                    }
                    // InternalLinkerScript.g:931:4: ( (lv_name_88_0= ruleValidID ) )
                    // InternalLinkerScript.g:932:5: (lv_name_88_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:932:5: (lv_name_88_0= ruleValidID )
                    // InternalLinkerScript.g:933:6: lv_name_88_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementCommandAccess().getNameValidIDParserRuleCall_17_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_88_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementCommandRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_88_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 19 :
                    // InternalLinkerScript.g:952:3: ( () otherlv_90= ';' )
                    {
                    // InternalLinkerScript.g:952:3: ( () otherlv_90= ';' )
                    // InternalLinkerScript.g:953:4: () otherlv_90= ';'
                    {
                    // InternalLinkerScript.g:953:4: ()
                    // InternalLinkerScript.g:954:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementCommandAccess().getStatementNopAction_18_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_90=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_90, grammarAccess.getStatementCommandAccess().getSemicolonKeyword_18_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatementCommand"


    // $ANTLR start "entryRuleFileList"
    // InternalLinkerScript.g:969:1: entryRuleFileList returns [EObject current=null] : iv_ruleFileList= ruleFileList EOF ;
    public final EObject entryRuleFileList() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFileList = null;


        try {
            // InternalLinkerScript.g:969:49: (iv_ruleFileList= ruleFileList EOF )
            // InternalLinkerScript.g:970:2: iv_ruleFileList= ruleFileList EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFileListRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFileList=ruleFileList();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFileList; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFileList"


    // $ANTLR start "ruleFileList"
    // InternalLinkerScript.g:976:1: ruleFileList returns [EObject current=null] : ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) ) ;
    public final EObject ruleFileList() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token lv_asNeeded_3_0=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_files_0_0 = null;

        EObject lv_files_2_0 = null;

        EObject lv_list_5_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:982:2: ( ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) ) )
            // InternalLinkerScript.g:983:2: ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) )
            {
            // InternalLinkerScript.g:983:2: ( ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* ) | ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' ) )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_ID||LA13_0==32||LA13_0==76||(LA13_0>=78 && LA13_0<=79)||(LA13_0>=81 && LA13_0<=82)) ) {
                alt13=1;
            }
            else if ( (LA13_0==31) ) {
                alt13=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // InternalLinkerScript.g:984:3: ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* )
                    {
                    // InternalLinkerScript.g:984:3: ( ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )* )
                    // InternalLinkerScript.g:985:4: ( (lv_files_0_0= ruleFileListName ) ) ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )*
                    {
                    // InternalLinkerScript.g:985:4: ( (lv_files_0_0= ruleFileListName ) )
                    // InternalLinkerScript.g:986:5: (lv_files_0_0= ruleFileListName )
                    {
                    // InternalLinkerScript.g:986:5: (lv_files_0_0= ruleFileListName )
                    // InternalLinkerScript.g:987:6: lv_files_0_0= ruleFileListName
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getFileListAccess().getFilesFileListNameParserRuleCall_0_0_0());
                      					
                    }
                    pushFollow(FOLLOW_15);
                    lv_files_0_0=ruleFileListName();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getFileListRule());
                      						}
                      						add(
                      							current,
                      							"files",
                      							lv_files_0_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:1004:4: ( (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) ) )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==RULE_ID||LA12_0==11||LA12_0==32||LA12_0==76||(LA12_0>=78 && LA12_0<=79)||(LA12_0>=81 && LA12_0<=82)) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // InternalLinkerScript.g:1005:5: (otherlv_1= ',' )? ( (lv_files_2_0= ruleFileListName ) )
                    	    {
                    	    // InternalLinkerScript.g:1005:5: (otherlv_1= ',' )?
                    	    int alt11=2;
                    	    int LA11_0 = input.LA(1);

                    	    if ( (LA11_0==11) ) {
                    	        alt11=1;
                    	    }
                    	    switch (alt11) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:1006:6: otherlv_1= ','
                    	            {
                    	            otherlv_1=(Token)match(input,11,FOLLOW_12); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_1, grammarAccess.getFileListAccess().getCommaKeyword_0_1_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:1011:5: ( (lv_files_2_0= ruleFileListName ) )
                    	    // InternalLinkerScript.g:1012:6: (lv_files_2_0= ruleFileListName )
                    	    {
                    	    // InternalLinkerScript.g:1012:6: (lv_files_2_0= ruleFileListName )
                    	    // InternalLinkerScript.g:1013:7: lv_files_2_0= ruleFileListName
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getFileListAccess().getFilesFileListNameParserRuleCall_0_1_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_15);
                    	    lv_files_2_0=ruleFileListName();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getFileListRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"files",
                    	      								lv_files_2_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.FileListName");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1033:3: ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' )
                    {
                    // InternalLinkerScript.g:1033:3: ( ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')' )
                    // InternalLinkerScript.g:1034:4: ( (lv_asNeeded_3_0= 'AS_NEEDED' ) ) otherlv_4= '(' ( (lv_list_5_0= ruleFileList ) ) otherlv_6= ')'
                    {
                    // InternalLinkerScript.g:1034:4: ( (lv_asNeeded_3_0= 'AS_NEEDED' ) )
                    // InternalLinkerScript.g:1035:5: (lv_asNeeded_3_0= 'AS_NEEDED' )
                    {
                    // InternalLinkerScript.g:1035:5: (lv_asNeeded_3_0= 'AS_NEEDED' )
                    // InternalLinkerScript.g:1036:6: lv_asNeeded_3_0= 'AS_NEEDED'
                    {
                    lv_asNeeded_3_0=(Token)match(input,31,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_asNeeded_3_0, grammarAccess.getFileListAccess().getAsNeededAS_NEEDEDKeyword_1_0_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getFileListRule());
                      						}
                      						setWithLastConsumed(current, "asNeeded", true, "AS_NEEDED");
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,13,FOLLOW_11); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getFileListAccess().getLeftParenthesisKeyword_1_1());
                      			
                    }
                    // InternalLinkerScript.g:1052:4: ( (lv_list_5_0= ruleFileList ) )
                    // InternalLinkerScript.g:1053:5: (lv_list_5_0= ruleFileList )
                    {
                    // InternalLinkerScript.g:1053:5: (lv_list_5_0= ruleFileList )
                    // InternalLinkerScript.g:1054:6: lv_list_5_0= ruleFileList
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getFileListAccess().getListFileListParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_list_5_0=ruleFileList();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getFileListRule());
                      						}
                      						set(
                      							current,
                      							"list",
                      							lv_list_5_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.FileList");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_6=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getFileListAccess().getRightParenthesisKeyword_1_3());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFileList"


    // $ANTLR start "entryRuleFileListName"
    // InternalLinkerScript.g:1080:1: entryRuleFileListName returns [EObject current=null] : iv_ruleFileListName= ruleFileListName EOF ;
    public final EObject entryRuleFileListName() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFileListName = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:1082:2: (iv_ruleFileListName= ruleFileListName EOF )
            // InternalLinkerScript.g:1083:2: iv_ruleFileListName= ruleFileListName EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getFileListNameRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleFileListName=ruleFileListName();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleFileListName; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleFileListName"


    // $ANTLR start "ruleFileListName"
    // InternalLinkerScript.g:1092:1: ruleFileListName returns [EObject current=null] : ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) ) ;
    public final EObject ruleFileListName() throws RecognitionException {
        EObject current = null;

        Token lv_library_0_0=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;



        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:1099:2: ( ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) ) )
            // InternalLinkerScript.g:1100:2: ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) )
            {
            // InternalLinkerScript.g:1100:2: ( ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) ) )
            // InternalLinkerScript.g:1101:3: ( (lv_library_0_0= '-l' ) )? ( (lv_name_1_0= ruleValidID ) )
            {
            // InternalLinkerScript.g:1101:3: ( (lv_library_0_0= '-l' ) )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==32) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // InternalLinkerScript.g:1102:4: (lv_library_0_0= '-l' )
                    {
                    // InternalLinkerScript.g:1102:4: (lv_library_0_0= '-l' )
                    // InternalLinkerScript.g:1103:5: lv_library_0_0= '-l'
                    {
                    lv_library_0_0=(Token)match(input,32,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					newLeafNode(lv_library_0_0, grammarAccess.getFileListNameAccess().getLibraryLKeyword_0_0());
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElement(grammarAccess.getFileListNameRule());
                      					}
                      					setWithLastConsumed(current, "library", true, "-l");
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1115:3: ( (lv_name_1_0= ruleValidID ) )
            // InternalLinkerScript.g:1116:4: (lv_name_1_0= ruleValidID )
            {
            // InternalLinkerScript.g:1116:4: (lv_name_1_0= ruleValidID )
            // InternalLinkerScript.g:1117:5: lv_name_1_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getFileListNameAccess().getNameValidIDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_name_1_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getFileListNameRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_1_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleFileListName"


    // $ANTLR start "entryRulePhdrsCommand"
    // InternalLinkerScript.g:1141:1: entryRulePhdrsCommand returns [EObject current=null] : iv_rulePhdrsCommand= rulePhdrsCommand EOF ;
    public final EObject entryRulePhdrsCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePhdrsCommand = null;


        try {
            // InternalLinkerScript.g:1141:53: (iv_rulePhdrsCommand= rulePhdrsCommand EOF )
            // InternalLinkerScript.g:1142:2: iv_rulePhdrsCommand= rulePhdrsCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPhdrsCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePhdrsCommand=rulePhdrsCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePhdrsCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePhdrsCommand"


    // $ANTLR start "rulePhdrsCommand"
    // InternalLinkerScript.g:1148:1: rulePhdrsCommand returns [EObject current=null] : ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' ) ;
    public final EObject rulePhdrsCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_phdrs_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1154:2: ( ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:1155:2: ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:1155:2: ( () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:1156:3: () otherlv_1= 'PHDRS' otherlv_2= '{' ( (lv_phdrs_3_0= rulePhdr ) )* otherlv_4= '}'
            {
            // InternalLinkerScript.g:1156:3: ()
            // InternalLinkerScript.g:1157:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getPhdrsCommandAccess().getPhdrsCommandAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,33,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getPhdrsCommandAccess().getPHDRSKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,34,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getPhdrsCommandAccess().getLeftCurlyBracketKeyword_2());
              		
            }
            // InternalLinkerScript.g:1171:3: ( (lv_phdrs_3_0= rulePhdr ) )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==RULE_ID||LA15_0==76||(LA15_0>=78 && LA15_0<=79)||(LA15_0>=81 && LA15_0<=82)) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // InternalLinkerScript.g:1172:4: (lv_phdrs_3_0= rulePhdr )
            	    {
            	    // InternalLinkerScript.g:1172:4: (lv_phdrs_3_0= rulePhdr )
            	    // InternalLinkerScript.g:1173:5: lv_phdrs_3_0= rulePhdr
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getPhdrsCommandAccess().getPhdrsPhdrParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
            	    lv_phdrs_3_0=rulePhdr();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getPhdrsCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"phdrs",
            	      						lv_phdrs_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.Phdr");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            otherlv_4=(Token)match(input,35,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getPhdrsCommandAccess().getRightCurlyBracketKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePhdrsCommand"


    // $ANTLR start "entryRulePhdr"
    // InternalLinkerScript.g:1198:1: entryRulePhdr returns [EObject current=null] : iv_rulePhdr= rulePhdr EOF ;
    public final EObject entryRulePhdr() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePhdr = null;


        try {
            // InternalLinkerScript.g:1198:45: (iv_rulePhdr= rulePhdr EOF )
            // InternalLinkerScript.g:1199:2: iv_rulePhdr= rulePhdr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getPhdrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_rulePhdr=rulePhdr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_rulePhdr; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePhdr"


    // $ANTLR start "rulePhdr"
    // InternalLinkerScript.g:1205:1: rulePhdr returns [EObject current=null] : ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' ) ;
    public final EObject rulePhdr() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        AntlrDatatypeRuleToken lv_name_0_0 = null;

        EObject lv_opts_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1211:2: ( ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' ) )
            // InternalLinkerScript.g:1212:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' )
            {
            // InternalLinkerScript.g:1212:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';' )
            // InternalLinkerScript.g:1213:3: ( (lv_name_0_0= ruleValidID ) ) ( (lv_opts_1_0= ruleLExpression ) )* otherlv_2= ';'
            {
            // InternalLinkerScript.g:1213:3: ( (lv_name_0_0= ruleValidID ) )
            // InternalLinkerScript.g:1214:4: (lv_name_0_0= ruleValidID )
            {
            // InternalLinkerScript.g:1214:4: (lv_name_0_0= ruleValidID )
            // InternalLinkerScript.g:1215:5: lv_name_0_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getPhdrAccess().getNameValidIDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_18);
            lv_name_0_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getPhdrRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_0_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:1232:3: ( (lv_opts_1_0= ruleLExpression ) )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( ((LA16_0>=RULE_ID && LA16_0<=RULE_HEX)||LA16_0==13||LA16_0==38||LA16_0==42||(LA16_0>=76 && LA16_0<=83)||(LA16_0>=90 && LA16_0<=91)||(LA16_0>=95 && LA16_0<=96)) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // InternalLinkerScript.g:1233:4: (lv_opts_1_0= ruleLExpression )
            	    {
            	    // InternalLinkerScript.g:1233:4: (lv_opts_1_0= ruleLExpression )
            	    // InternalLinkerScript.g:1234:5: lv_opts_1_0= ruleLExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getPhdrAccess().getOptsLExpressionParserRuleCall_1_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_18);
            	    lv_opts_1_0=ruleLExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getPhdrRule());
            	      					}
            	      					add(
            	      						current,
            	      						"opts",
            	      						lv_opts_1_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getPhdrAccess().getSemicolonKeyword_2());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePhdr"


    // $ANTLR start "entryRuleSectionsCommand"
    // InternalLinkerScript.g:1259:1: entryRuleSectionsCommand returns [EObject current=null] : iv_ruleSectionsCommand= ruleSectionsCommand EOF ;
    public final EObject entryRuleSectionsCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSectionsCommand = null;


        try {
            // InternalLinkerScript.g:1259:56: (iv_ruleSectionsCommand= ruleSectionsCommand EOF )
            // InternalLinkerScript.g:1260:2: iv_ruleSectionsCommand= ruleSectionsCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSectionsCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSectionsCommand=ruleSectionsCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSectionsCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSectionsCommand"


    // $ANTLR start "ruleSectionsCommand"
    // InternalLinkerScript.g:1266:1: ruleSectionsCommand returns [EObject current=null] : (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' ) ;
    public final EObject ruleSectionsCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_sectionCommands_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1272:2: ( (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:1273:2: (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:1273:2: (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:1274:3: otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )* otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,36,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSectionsCommandAccess().getSECTIONSKeyword_0());
              		
            }
            otherlv_1=(Token)match(input,34,FOLLOW_19); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getSectionsCommandAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalLinkerScript.g:1282:3: ()
            // InternalLinkerScript.g:1283:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getSectionsCommandAccess().getSectionsCommandAction_2(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:1289:3: ( (lv_sectionCommands_3_0= ruleOutputSectionCommand ) )*
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==RULE_ID||LA17_0==10||(LA17_0>=15 && LA17_0<=16)||(LA17_0>=61 && LA17_0<=63)||LA17_0==76||(LA17_0>=78 && LA17_0<=79)||(LA17_0>=81 && LA17_0<=82)||LA17_0==92) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // InternalLinkerScript.g:1290:4: (lv_sectionCommands_3_0= ruleOutputSectionCommand )
            	    {
            	    // InternalLinkerScript.g:1290:4: (lv_sectionCommands_3_0= ruleOutputSectionCommand )
            	    // InternalLinkerScript.g:1291:5: lv_sectionCommands_3_0= ruleOutputSectionCommand
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getSectionsCommandAccess().getSectionCommandsOutputSectionCommandParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_19);
            	    lv_sectionCommands_3_0=ruleOutputSectionCommand();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getSectionsCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"sectionCommands",
            	      						lv_sectionCommands_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionCommand");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);

            otherlv_4=(Token)match(input,35,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getSectionsCommandAccess().getRightCurlyBracketKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSectionsCommand"


    // $ANTLR start "entryRuleOutputSectionCommand"
    // InternalLinkerScript.g:1316:1: entryRuleOutputSectionCommand returns [EObject current=null] : iv_ruleOutputSectionCommand= ruleOutputSectionCommand EOF ;
    public final EObject entryRuleOutputSectionCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionCommand = null;


        try {
            // InternalLinkerScript.g:1316:61: (iv_ruleOutputSectionCommand= ruleOutputSectionCommand EOF )
            // InternalLinkerScript.g:1317:2: iv_ruleOutputSectionCommand= ruleOutputSectionCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionCommand=ruleOutputSectionCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionCommand"


    // $ANTLR start "ruleOutputSectionCommand"
    // InternalLinkerScript.g:1323:1: ruleOutputSectionCommand returns [EObject current=null] : (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere ) ;
    public final EObject ruleOutputSectionCommand() throws RecognitionException {
        EObject current = null;

        EObject this_OutputSection_0 = null;

        EObject this_StatementAnywhere_1 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1329:2: ( (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere ) )
            // InternalLinkerScript.g:1330:2: (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere )
            {
            // InternalLinkerScript.g:1330:2: (this_OutputSection_0= ruleOutputSection | this_StatementAnywhere_1= ruleStatementAnywhere )
            int alt18=2;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                int LA18_1 = input.LA(2);

                if ( ((LA18_1>=40 && LA18_1<=41)||(LA18_1>=64 && LA18_1<=68)||(LA18_1>=70 && LA18_1<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_1>=RULE_ID && LA18_1<=RULE_HEX)||LA18_1==13||(LA18_1>=37 && LA18_1<=38)||LA18_1==42||(LA18_1>=76 && LA18_1<=83)||(LA18_1>=90 && LA18_1<=91)||(LA18_1>=95 && LA18_1<=96)) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;
                }
                }
                break;
            case 76:
                {
                int LA18_2 = input.LA(2);

                if ( ((LA18_2>=40 && LA18_2<=41)||(LA18_2>=64 && LA18_2<=68)||(LA18_2>=70 && LA18_2<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_2>=RULE_ID && LA18_2<=RULE_HEX)||LA18_2==13||(LA18_2>=37 && LA18_2<=38)||LA18_2==42||(LA18_2>=76 && LA18_2<=83)||(LA18_2>=90 && LA18_2<=91)||(LA18_2>=95 && LA18_2<=96)) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 2, input);

                    throw nvae;
                }
                }
                break;
            case 79:
                {
                int LA18_3 = input.LA(2);

                if ( ((LA18_3>=40 && LA18_3<=41)||(LA18_3>=64 && LA18_3<=68)||(LA18_3>=70 && LA18_3<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_3>=RULE_ID && LA18_3<=RULE_HEX)||LA18_3==13||(LA18_3>=37 && LA18_3<=38)||LA18_3==42||(LA18_3>=76 && LA18_3<=83)||(LA18_3>=90 && LA18_3<=91)||(LA18_3>=95 && LA18_3<=96)) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 3, input);

                    throw nvae;
                }
                }
                break;
            case 78:
                {
                int LA18_4 = input.LA(2);

                if ( ((LA18_4>=40 && LA18_4<=41)||(LA18_4>=64 && LA18_4<=68)||(LA18_4>=70 && LA18_4<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_4>=RULE_ID && LA18_4<=RULE_HEX)||LA18_4==13||(LA18_4>=37 && LA18_4<=38)||LA18_4==42||(LA18_4>=76 && LA18_4<=83)||(LA18_4>=90 && LA18_4<=91)||(LA18_4>=95 && LA18_4<=96)) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 4, input);

                    throw nvae;
                }
                }
                break;
            case 82:
                {
                int LA18_5 = input.LA(2);

                if ( ((LA18_5>=40 && LA18_5<=41)||(LA18_5>=64 && LA18_5<=68)||(LA18_5>=70 && LA18_5<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_5>=RULE_ID && LA18_5<=RULE_HEX)||LA18_5==13||(LA18_5>=37 && LA18_5<=38)||LA18_5==42||(LA18_5>=76 && LA18_5<=83)||(LA18_5>=90 && LA18_5<=91)||(LA18_5>=95 && LA18_5<=96)) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 5, input);

                    throw nvae;
                }
                }
                break;
            case 81:
                {
                int LA18_6 = input.LA(2);

                if ( ((LA18_6>=40 && LA18_6<=41)||(LA18_6>=64 && LA18_6<=68)||(LA18_6>=70 && LA18_6<=71)) ) {
                    alt18=2;
                }
                else if ( ((LA18_6>=RULE_ID && LA18_6<=RULE_HEX)||LA18_6==13||(LA18_6>=37 && LA18_6<=38)||LA18_6==42||(LA18_6>=76 && LA18_6<=83)||(LA18_6>=90 && LA18_6<=91)||(LA18_6>=95 && LA18_6<=96)) ) {
                    alt18=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 6, input);

                    throw nvae;
                }
                }
                break;
            case 10:
            case 15:
            case 16:
            case 61:
            case 62:
            case 63:
            case 92:
                {
                alt18=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // InternalLinkerScript.g:1331:3: this_OutputSection_0= ruleOutputSection
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getOutputSectionCommandAccess().getOutputSectionParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_OutputSection_0=ruleOutputSection();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_OutputSection_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1340:3: this_StatementAnywhere_1= ruleStatementAnywhere
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getOutputSectionCommandAccess().getStatementAnywhereParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_StatementAnywhere_1=ruleStatementAnywhere();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_StatementAnywhere_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionCommand"


    // $ANTLR start "entryRuleOutputSection"
    // InternalLinkerScript.g:1352:1: entryRuleOutputSection returns [EObject current=null] : iv_ruleOutputSection= ruleOutputSection EOF ;
    public final EObject entryRuleOutputSection() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSection = null;


        try {
            // InternalLinkerScript.g:1352:54: (iv_ruleOutputSection= ruleOutputSection EOF )
            // InternalLinkerScript.g:1353:2: iv_ruleOutputSection= ruleOutputSection EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSection=ruleOutputSection();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSection; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSection"


    // $ANTLR start "ruleOutputSection"
    // InternalLinkerScript.g:1359:1: ruleOutputSection returns [EObject current=null] : ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? ) ;
    public final EObject ruleOutputSection() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_19=null;
        Token otherlv_21=null;
        Token otherlv_22=null;
        Token otherlv_24=null;
        Token otherlv_26=null;
        Token otherlv_28=null;
        AntlrDatatypeRuleToken lv_name_0_0 = null;

        EObject lv_address_1_0 = null;

        EObject lv_type_3_0 = null;

        EObject lv_at_8_0 = null;

        EObject lv_align_10_0 = null;

        EObject lv_subAlign_13_0 = null;

        EObject lv_constraint_15_0 = null;

        EObject lv_statements_17_0 = null;

        AntlrDatatypeRuleToken lv_memory_20_0 = null;

        AntlrDatatypeRuleToken lv_atMemory_23_0 = null;

        AntlrDatatypeRuleToken lv_phdrs_25_0 = null;

        EObject lv_fill_27_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1365:2: ( ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? ) )
            // InternalLinkerScript.g:1366:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? )
            {
            // InternalLinkerScript.g:1366:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? )
            // InternalLinkerScript.g:1367:3: ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )?
            {
            // InternalLinkerScript.g:1367:3: ( (lv_name_0_0= ruleValidID ) )
            // InternalLinkerScript.g:1368:4: (lv_name_0_0= ruleValidID )
            {
            // InternalLinkerScript.g:1368:4: (lv_name_0_0= ruleValidID )
            // InternalLinkerScript.g:1369:5: lv_name_0_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getOutputSectionAccess().getNameValidIDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_20);
            lv_name_0_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_0_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:1386:3: ( (lv_address_1_0= ruleLExpression ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( ((LA19_0>=RULE_ID && LA19_0<=RULE_HEX)||LA19_0==38||LA19_0==42||(LA19_0>=76 && LA19_0<=83)||(LA19_0>=90 && LA19_0<=91)||(LA19_0>=95 && LA19_0<=96)) ) {
                alt19=1;
            }
            else if ( (LA19_0==13) ) {
                int LA19_2 = input.LA(2);

                if ( ((LA19_2>=RULE_ID && LA19_2<=RULE_HEX)||LA19_2==13||LA19_2==38||LA19_2==42||(LA19_2>=76 && LA19_2<=83)||(LA19_2>=90 && LA19_2<=91)||(LA19_2>=95 && LA19_2<=96)) ) {
                    alt19=1;
                }
            }
            switch (alt19) {
                case 1 :
                    // InternalLinkerScript.g:1387:4: (lv_address_1_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1387:4: (lv_address_1_0= ruleLExpression )
                    // InternalLinkerScript.g:1388:5: lv_address_1_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getAddressLExpressionParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_21);
                    lv_address_1_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      					}
                      					set(
                      						current,
                      						"address",
                      						lv_address_1_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1405:3: (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==13) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // InternalLinkerScript.g:1406:4: otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,13,FOLLOW_22); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    // InternalLinkerScript.g:1410:4: ( (lv_type_3_0= ruleOutputSectionType ) )
                    // InternalLinkerScript.g:1411:5: (lv_type_3_0= ruleOutputSectionType )
                    {
                    // InternalLinkerScript.g:1411:5: (lv_type_3_0= ruleOutputSectionType )
                    // InternalLinkerScript.g:1412:6: lv_type_3_0= ruleOutputSectionType
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getTypeOutputSectionTypeParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_type_3_0=ruleOutputSectionType();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"type",
                      							lv_type_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionType");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,14,FOLLOW_23); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_5=(Token)match(input,37,FOLLOW_24); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_5, grammarAccess.getOutputSectionAccess().getColonKeyword_3());
              		
            }
            // InternalLinkerScript.g:1438:3: (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==38) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // InternalLinkerScript.g:1439:4: otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')'
                    {
                    otherlv_6=(Token)match(input,38,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getOutputSectionAccess().getATKeyword_4_0());
                      			
                    }
                    otherlv_7=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_4_1());
                      			
                    }
                    // InternalLinkerScript.g:1447:4: ( (lv_at_8_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1448:5: (lv_at_8_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1448:5: (lv_at_8_0= ruleLExpression )
                    // InternalLinkerScript.g:1449:6: lv_at_8_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getAtLExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_at_8_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"at",
                      							lv_at_8_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_9=(Token)match(input,14,FOLLOW_25); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_4_3());
                      			
                    }

                    }
                    break;

            }

            // InternalLinkerScript.g:1471:3: ( (lv_align_10_0= ruleOutputSectionAlign ) )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( ((LA22_0>=42 && LA22_0<=43)) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // InternalLinkerScript.g:1472:4: (lv_align_10_0= ruleOutputSectionAlign )
                    {
                    // InternalLinkerScript.g:1472:4: (lv_align_10_0= ruleOutputSectionAlign )
                    // InternalLinkerScript.g:1473:5: lv_align_10_0= ruleOutputSectionAlign
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getAlignOutputSectionAlignParserRuleCall_5_0());
                      				
                    }
                    pushFollow(FOLLOW_26);
                    lv_align_10_0=ruleOutputSectionAlign();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      					}
                      					set(
                      						current,
                      						"align",
                      						lv_align_10_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionAlign");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1490:3: (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==39) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // InternalLinkerScript.g:1491:4: otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')'
                    {
                    otherlv_11=(Token)match(input,39,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getOutputSectionAccess().getSUBALIGNKeyword_6_0());
                      			
                    }
                    otherlv_12=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_6_1());
                      			
                    }
                    // InternalLinkerScript.g:1499:4: ( (lv_subAlign_13_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1500:5: (lv_subAlign_13_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1500:5: (lv_subAlign_13_0= ruleLExpression )
                    // InternalLinkerScript.g:1501:6: lv_subAlign_13_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getSubAlignLExpressionParserRuleCall_6_2_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_subAlign_13_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"subAlign",
                      							lv_subAlign_13_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_14=(Token)match(input,14,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_14, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_6_3());
                      			
                    }

                    }
                    break;

            }

            // InternalLinkerScript.g:1523:3: ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=44 && LA24_0<=46)) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // InternalLinkerScript.g:1524:4: (lv_constraint_15_0= ruleOutputSectionConstraint )
                    {
                    // InternalLinkerScript.g:1524:4: (lv_constraint_15_0= ruleOutputSectionConstraint )
                    // InternalLinkerScript.g:1525:5: lv_constraint_15_0= ruleOutputSectionConstraint
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getConstraintOutputSectionConstraintParserRuleCall_7_0());
                      				
                    }
                    pushFollow(FOLLOW_16);
                    lv_constraint_15_0=ruleOutputSectionConstraint();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      					}
                      					set(
                      						current,
                      						"constraint",
                      						lv_constraint_15_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSectionConstraint");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_16=(Token)match(input,34,FOLLOW_28); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_16, grammarAccess.getOutputSectionAccess().getLeftCurlyBracketKeyword_8());
              		
            }
            // InternalLinkerScript.g:1546:3: ( (lv_statements_17_0= ruleStatement ) )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==RULE_ID||LA25_0==10||LA25_0==16||LA25_0==30||(LA25_0>=52 && LA25_0<=63)||LA25_0==72||(LA25_0>=74 && LA25_0<=76)||(LA25_0>=78 && LA25_0<=79)||(LA25_0>=81 && LA25_0<=82)||LA25_0==92||(LA25_0>=97 && LA25_0<=100)) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // InternalLinkerScript.g:1547:4: (lv_statements_17_0= ruleStatement )
            	    {
            	    // InternalLinkerScript.g:1547:4: (lv_statements_17_0= ruleStatement )
            	    // InternalLinkerScript.g:1548:5: lv_statements_17_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getOutputSectionAccess().getStatementsStatementParserRuleCall_9_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_28);
            	    lv_statements_17_0=ruleStatement();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getOutputSectionRule());
            	      					}
            	      					add(
            	      						current,
            	      						"statements",
            	      						lv_statements_17_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.Statement");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);

            otherlv_18=(Token)match(input,35,FOLLOW_29); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_18, grammarAccess.getOutputSectionAccess().getRightCurlyBracketKeyword_10());
              		
            }
            // InternalLinkerScript.g:1569:3: (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==40) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // InternalLinkerScript.g:1570:4: otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) )
                    {
                    otherlv_19=(Token)match(input,40,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getOutputSectionAccess().getGreaterThanSignKeyword_11_0());
                      			
                    }
                    // InternalLinkerScript.g:1574:4: ( (lv_memory_20_0= ruleValidID ) )
                    // InternalLinkerScript.g:1575:5: (lv_memory_20_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:1575:5: (lv_memory_20_0= ruleValidID )
                    // InternalLinkerScript.g:1576:6: lv_memory_20_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getMemoryValidIDParserRuleCall_11_1_0());
                      					
                    }
                    pushFollow(FOLLOW_30);
                    lv_memory_20_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"memory",
                      							lv_memory_20_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1594:3: (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==38) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // InternalLinkerScript.g:1595:4: otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) )
                    {
                    otherlv_21=(Token)match(input,38,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_21, grammarAccess.getOutputSectionAccess().getATKeyword_12_0());
                      			
                    }
                    otherlv_22=(Token)match(input,40,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getOutputSectionAccess().getGreaterThanSignKeyword_12_1());
                      			
                    }
                    // InternalLinkerScript.g:1603:4: ( (lv_atMemory_23_0= ruleValidID ) )
                    // InternalLinkerScript.g:1604:5: (lv_atMemory_23_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:1604:5: (lv_atMemory_23_0= ruleValidID )
                    // InternalLinkerScript.g:1605:6: lv_atMemory_23_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getAtMemoryValidIDParserRuleCall_12_2_0());
                      					
                    }
                    pushFollow(FOLLOW_32);
                    lv_atMemory_23_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"atMemory",
                      							lv_atMemory_23_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1623:3: (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==37) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // InternalLinkerScript.g:1624:4: otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) )
            	    {
            	    otherlv_24=(Token)match(input,37,FOLLOW_6); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_24, grammarAccess.getOutputSectionAccess().getColonKeyword_13_0());
            	      			
            	    }
            	    // InternalLinkerScript.g:1628:4: ( (lv_phdrs_25_0= ruleValidID ) )
            	    // InternalLinkerScript.g:1629:5: (lv_phdrs_25_0= ruleValidID )
            	    {
            	    // InternalLinkerScript.g:1629:5: (lv_phdrs_25_0= ruleValidID )
            	    // InternalLinkerScript.g:1630:6: lv_phdrs_25_0= ruleValidID
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOutputSectionAccess().getPhdrsValidIDParserRuleCall_13_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_32);
            	    lv_phdrs_25_0=ruleValidID();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
            	      						}
            	      						add(
            	      							current,
            	      							"phdrs",
            	      							lv_phdrs_25_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            // InternalLinkerScript.g:1648:3: (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==41) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // InternalLinkerScript.g:1649:4: otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) )
                    {
                    otherlv_26=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_26, grammarAccess.getOutputSectionAccess().getEqualsSignKeyword_14_0());
                      			
                    }
                    // InternalLinkerScript.g:1653:4: ( (lv_fill_27_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1654:5: (lv_fill_27_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1654:5: (lv_fill_27_0= ruleLExpression )
                    // InternalLinkerScript.g:1655:6: lv_fill_27_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getFillLExpressionParserRuleCall_14_1_0());
                      					
                    }
                    pushFollow(FOLLOW_33);
                    lv_fill_27_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionRule());
                      						}
                      						set(
                      							current,
                      							"fill",
                      							lv_fill_27_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:1673:3: (otherlv_28= ',' )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==11) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // InternalLinkerScript.g:1674:4: otherlv_28= ','
                    {
                    otherlv_28=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_28, grammarAccess.getOutputSectionAccess().getCommaKeyword_15());
                      			
                    }

                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSection"


    // $ANTLR start "entryRuleOutputSectionAlign"
    // InternalLinkerScript.g:1683:1: entryRuleOutputSectionAlign returns [EObject current=null] : iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF ;
    public final EObject entryRuleOutputSectionAlign() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionAlign = null;


        try {
            // InternalLinkerScript.g:1683:59: (iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF )
            // InternalLinkerScript.g:1684:2: iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionAlignRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionAlign=ruleOutputSectionAlign();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionAlign; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionAlign"


    // $ANTLR start "ruleOutputSectionAlign"
    // InternalLinkerScript.g:1690:1: ruleOutputSectionAlign returns [EObject current=null] : ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) ) ;
    public final EObject ruleOutputSectionAlign() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_exp_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1696:2: ( ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) ) )
            // InternalLinkerScript.g:1697:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) )
            {
            // InternalLinkerScript.g:1697:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==42) ) {
                alt31=1;
            }
            else if ( (LA31_0==43) ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // InternalLinkerScript.g:1698:3: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
                    {
                    // InternalLinkerScript.g:1698:3: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
                    // InternalLinkerScript.g:1699:4: () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')'
                    {
                    // InternalLinkerScript.g:1699:4: ()
                    // InternalLinkerScript.g:1700:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionAlignAccess().getOutputSectionAlignExpressionAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,42,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionAlignAccess().getALIGNKeyword_0_1());
                      			
                    }
                    otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getOutputSectionAlignAccess().getLeftParenthesisKeyword_0_2());
                      			
                    }
                    // InternalLinkerScript.g:1714:4: ( (lv_exp_3_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1715:5: (lv_exp_3_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1715:5: (lv_exp_3_0= ruleLExpression )
                    // InternalLinkerScript.g:1716:6: lv_exp_3_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAlignAccess().getExpLExpressionParserRuleCall_0_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_3_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getOutputSectionAlignRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getOutputSectionAlignAccess().getRightParenthesisKeyword_0_4());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1739:3: ( () otherlv_6= 'ALIGN_WITH_INPUT' )
                    {
                    // InternalLinkerScript.g:1739:3: ( () otherlv_6= 'ALIGN_WITH_INPUT' )
                    // InternalLinkerScript.g:1740:4: () otherlv_6= 'ALIGN_WITH_INPUT'
                    {
                    // InternalLinkerScript.g:1740:4: ()
                    // InternalLinkerScript.g:1741:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionAlignAccess().getOutputSectionAlignWithInputAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_6=(Token)match(input,43,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getOutputSectionAlignAccess().getALIGN_WITH_INPUTKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionAlign"


    // $ANTLR start "entryRuleOutputSectionConstraint"
    // InternalLinkerScript.g:1756:1: entryRuleOutputSectionConstraint returns [EObject current=null] : iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF ;
    public final EObject entryRuleOutputSectionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionConstraint = null;


        try {
            // InternalLinkerScript.g:1756:64: (iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF )
            // InternalLinkerScript.g:1757:2: iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionConstraintRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionConstraint=ruleOutputSectionConstraint();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionConstraint; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionConstraint"


    // $ANTLR start "ruleOutputSectionConstraint"
    // InternalLinkerScript.g:1763:1: ruleOutputSectionConstraint returns [EObject current=null] : ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) ) ;
    public final EObject ruleOutputSectionConstraint() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:1769:2: ( ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) ) )
            // InternalLinkerScript.g:1770:2: ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) )
            {
            // InternalLinkerScript.g:1770:2: ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) )
            int alt32=3;
            switch ( input.LA(1) ) {
            case 44:
                {
                alt32=1;
                }
                break;
            case 45:
                {
                alt32=2;
                }
                break;
            case 46:
                {
                alt32=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }

            switch (alt32) {
                case 1 :
                    // InternalLinkerScript.g:1771:3: ( () otherlv_1= 'ONLY_IF_RO' )
                    {
                    // InternalLinkerScript.g:1771:3: ( () otherlv_1= 'ONLY_IF_RO' )
                    // InternalLinkerScript.g:1772:4: () otherlv_1= 'ONLY_IF_RO'
                    {
                    // InternalLinkerScript.g:1772:4: ()
                    // InternalLinkerScript.g:1773:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintOnlyIfROAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,44,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionConstraintAccess().getONLY_IF_ROKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1785:3: ( () otherlv_3= 'ONLY_IF_RW' )
                    {
                    // InternalLinkerScript.g:1785:3: ( () otherlv_3= 'ONLY_IF_RW' )
                    // InternalLinkerScript.g:1786:4: () otherlv_3= 'ONLY_IF_RW'
                    {
                    // InternalLinkerScript.g:1786:4: ()
                    // InternalLinkerScript.g:1787:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintOnlyIfRWAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,45,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getOutputSectionConstraintAccess().getONLY_IF_RWKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1799:3: ( () otherlv_5= 'SPECIAL' )
                    {
                    // InternalLinkerScript.g:1799:3: ( () otherlv_5= 'SPECIAL' )
                    // InternalLinkerScript.g:1800:4: () otherlv_5= 'SPECIAL'
                    {
                    // InternalLinkerScript.g:1800:4: ()
                    // InternalLinkerScript.g:1801:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintSpecialAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,46,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getOutputSectionConstraintAccess().getSPECIALKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionConstraint"


    // $ANTLR start "entryRuleOutputSectionType"
    // InternalLinkerScript.g:1816:1: entryRuleOutputSectionType returns [EObject current=null] : iv_ruleOutputSectionType= ruleOutputSectionType EOF ;
    public final EObject entryRuleOutputSectionType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionType = null;


        try {
            // InternalLinkerScript.g:1816:58: (iv_ruleOutputSectionType= ruleOutputSectionType EOF )
            // InternalLinkerScript.g:1817:2: iv_ruleOutputSectionType= ruleOutputSectionType EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOutputSectionTypeRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOutputSectionType=ruleOutputSectionType();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOutputSectionType; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOutputSectionType"


    // $ANTLR start "ruleOutputSectionType"
    // InternalLinkerScript.g:1823:1: ruleOutputSectionType returns [EObject current=null] : ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) ) ;
    public final EObject ruleOutputSectionType() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:1829:2: ( ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) ) )
            // InternalLinkerScript.g:1830:2: ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) )
            {
            // InternalLinkerScript.g:1830:2: ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) )
            int alt33=5;
            switch ( input.LA(1) ) {
            case 47:
                {
                alt33=1;
                }
                break;
            case 48:
                {
                alt33=2;
                }
                break;
            case 49:
                {
                alt33=3;
                }
                break;
            case 50:
                {
                alt33=4;
                }
                break;
            case 51:
                {
                alt33=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }

            switch (alt33) {
                case 1 :
                    // InternalLinkerScript.g:1831:3: ( () otherlv_1= 'NOLOAD' )
                    {
                    // InternalLinkerScript.g:1831:3: ( () otherlv_1= 'NOLOAD' )
                    // InternalLinkerScript.g:1832:4: () otherlv_1= 'NOLOAD'
                    {
                    // InternalLinkerScript.g:1832:4: ()
                    // InternalLinkerScript.g:1833:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeNoLoadAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionTypeAccess().getNOLOADKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1845:3: ( () otherlv_3= 'DSECT' )
                    {
                    // InternalLinkerScript.g:1845:3: ( () otherlv_3= 'DSECT' )
                    // InternalLinkerScript.g:1846:4: () otherlv_3= 'DSECT'
                    {
                    // InternalLinkerScript.g:1846:4: ()
                    // InternalLinkerScript.g:1847:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeDSectAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getOutputSectionTypeAccess().getDSECTKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1859:3: ( () otherlv_5= 'COPY' )
                    {
                    // InternalLinkerScript.g:1859:3: ( () otherlv_5= 'COPY' )
                    // InternalLinkerScript.g:1860:4: () otherlv_5= 'COPY'
                    {
                    // InternalLinkerScript.g:1860:4: ()
                    // InternalLinkerScript.g:1861:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeCopyAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getOutputSectionTypeAccess().getCOPYKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:1873:3: ( () otherlv_7= 'INFO' )
                    {
                    // InternalLinkerScript.g:1873:3: ( () otherlv_7= 'INFO' )
                    // InternalLinkerScript.g:1874:4: () otherlv_7= 'INFO'
                    {
                    // InternalLinkerScript.g:1874:4: ()
                    // InternalLinkerScript.g:1875:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeInfoAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getOutputSectionTypeAccess().getINFOKeyword_3_1());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:1887:3: ( () otherlv_9= 'OVERLAY' )
                    {
                    // InternalLinkerScript.g:1887:3: ( () otherlv_9= 'OVERLAY' )
                    // InternalLinkerScript.g:1888:4: () otherlv_9= 'OVERLAY'
                    {
                    // InternalLinkerScript.g:1888:4: ()
                    // InternalLinkerScript.g:1889:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeOverlayAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_9=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getOutputSectionTypeAccess().getOVERLAYKeyword_4_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOutputSectionType"


    // $ANTLR start "entryRuleStatement"
    // InternalLinkerScript.g:1904:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalLinkerScript.g:1904:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalLinkerScript.g:1905:2: iv_ruleStatement= ruleStatement EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatement=ruleStatement();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatement; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatement"


    // $ANTLR start "ruleStatement"
    // InternalLinkerScript.g:1911:1: ruleStatement returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) ) ;
    public final EObject ruleStatement() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        Token otherlv_31=null;
        Token otherlv_36=null;
        EObject lv_assignment_1_0 = null;

        AntlrDatatypeRuleToken lv_size_14_0 = null;

        EObject lv_data_16_0 = null;

        EObject lv_fill_21_0 = null;

        EObject lv_exp_26_0 = null;

        AntlrDatatypeRuleToken lv_message_28_0 = null;

        AntlrDatatypeRuleToken lv_filename_32_0 = null;

        EObject lv_spec_34_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:1917:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) ) )
            // InternalLinkerScript.g:1918:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )
            {
            // InternalLinkerScript.g:1918:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )
            int alt35=10;
            alt35 = dfa35.predict(input);
            switch (alt35) {
                case 1 :
                    // InternalLinkerScript.g:1919:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) )
                    {
                    // InternalLinkerScript.g:1919:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) )
                    // InternalLinkerScript.g:1920:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' )
                    {
                    // InternalLinkerScript.g:1920:4: ()
                    // InternalLinkerScript.g:1921:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1927:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:1928:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:1928:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:1929:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
                    lv_assignment_1_0=ruleAssignmentRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"assignment",
                      							lv_assignment_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.AssignmentRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:1946:4: (otherlv_2= ';' | otherlv_3= ',' )
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==10) ) {
                        alt34=1;
                    }
                    else if ( (LA34_0==11) ) {
                        alt34=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 0, input);

                        throw nvae;
                    }
                    switch (alt34) {
                        case 1 :
                            // InternalLinkerScript.g:1947:5: otherlv_2= ';'
                            {
                            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementAccess().getSemicolonKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:1952:5: otherlv_3= ','
                            {
                            otherlv_3=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementAccess().getCommaKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1959:3: ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' )
                    {
                    // InternalLinkerScript.g:1959:3: ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' )
                    // InternalLinkerScript.g:1960:4: () otherlv_5= 'CREATE_OBJECT_SYMBOLS'
                    {
                    // InternalLinkerScript.g:1960:4: ()
                    // InternalLinkerScript.g:1961:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementCreateObjectSymbolsAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementAccess().getCREATE_OBJECT_SYMBOLSKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1973:3: ( () otherlv_7= 'CONSTRUCTORS' )
                    {
                    // InternalLinkerScript.g:1973:3: ( () otherlv_7= 'CONSTRUCTORS' )
                    // InternalLinkerScript.g:1974:4: () otherlv_7= 'CONSTRUCTORS'
                    {
                    // InternalLinkerScript.g:1974:4: ()
                    // InternalLinkerScript.g:1975:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementConstructorsAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getStatementAccess().getCONSTRUCTORSKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:1987:3: ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' )
                    {
                    // InternalLinkerScript.g:1987:3: ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' )
                    // InternalLinkerScript.g:1988:4: () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')'
                    {
                    // InternalLinkerScript.g:1988:4: ()
                    // InternalLinkerScript.g:1989:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementConstructorsSortedAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_9=(Token)match(input,54,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getStatementAccess().getSORT_BY_NAMEKeyword_3_1());
                      			
                    }
                    otherlv_10=(Token)match(input,13,FOLLOW_34); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    otherlv_11=(Token)match(input,53,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getStatementAccess().getCONSTRUCTORSKeyword_3_3());
                      			
                    }
                    otherlv_12=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getStatementAccess().getRightParenthesisKeyword_3_4());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2013:3: ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:2013:3: ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:2014:4: () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:2014:4: ()
                    // InternalLinkerScript.g:2015:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementDataAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2021:4: ( (lv_size_14_0= ruleStatementDataSize ) )
                    // InternalLinkerScript.g:2022:5: (lv_size_14_0= ruleStatementDataSize )
                    {
                    // InternalLinkerScript.g:2022:5: (lv_size_14_0= ruleStatementDataSize )
                    // InternalLinkerScript.g:2023:6: lv_size_14_0= ruleStatementDataSize
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getSizeStatementDataSizeParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_size_14_0=ruleStatementDataSize();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"size",
                      							lv_size_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.StatementDataSize");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_15=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:2044:4: ( (lv_data_16_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2045:5: (lv_data_16_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2045:5: (lv_data_16_0= ruleLExpression )
                    // InternalLinkerScript.g:2046:6: lv_data_16_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getDataLExpressionParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_data_16_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"data",
                      							lv_data_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getStatementAccess().getRightParenthesisKeyword_4_4());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:2069:3: ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' )
                    {
                    // InternalLinkerScript.g:2069:3: ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' )
                    // InternalLinkerScript.g:2070:4: () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')'
                    {
                    // InternalLinkerScript.g:2070:4: ()
                    // InternalLinkerScript.g:2071:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementFillAction_5_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,55,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getStatementAccess().getFILLKeyword_5_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    // InternalLinkerScript.g:2085:4: ( (lv_fill_21_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2086:5: (lv_fill_21_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2086:5: (lv_fill_21_0= ruleLExpression )
                    // InternalLinkerScript.g:2087:6: lv_fill_21_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getFillLExpressionParserRuleCall_5_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_fill_21_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"fill",
                      							lv_fill_21_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_22=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getStatementAccess().getRightParenthesisKeyword_5_4());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:2110:3: ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' )
                    {
                    // InternalLinkerScript.g:2110:3: ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' )
                    // InternalLinkerScript.g:2111:4: () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')'
                    {
                    // InternalLinkerScript.g:2111:4: ()
                    // InternalLinkerScript.g:2112:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementAssertAction_6_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_24=(Token)match(input,16,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_24, grammarAccess.getStatementAccess().getASSERTKeyword_6_1());
                      			
                    }
                    otherlv_25=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    // InternalLinkerScript.g:2126:4: ( (lv_exp_26_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2127:5: (lv_exp_26_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2127:5: (lv_exp_26_0= ruleLExpression )
                    // InternalLinkerScript.g:2128:6: lv_exp_26_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getExpLExpressionParserRuleCall_6_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_exp_26_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_26_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_27=(Token)match(input,11,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getStatementAccess().getCommaKeyword_6_4());
                      			
                    }
                    // InternalLinkerScript.g:2149:4: ( (lv_message_28_0= ruleValidID ) )
                    // InternalLinkerScript.g:2150:5: (lv_message_28_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:2150:5: (lv_message_28_0= ruleValidID )
                    // InternalLinkerScript.g:2151:6: lv_message_28_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getMessageValidIDParserRuleCall_6_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_message_28_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"message",
                      							lv_message_28_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_29=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_29, grammarAccess.getStatementAccess().getRightParenthesisKeyword_6_6());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:2174:3: ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:2174:3: ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:2175:4: () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:2175:4: ()
                    // InternalLinkerScript.g:2176:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementIncludeAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_31=(Token)match(input,30,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_31, grammarAccess.getStatementAccess().getINCLUDEKeyword_7_1());
                      			
                    }
                    // InternalLinkerScript.g:2186:4: ( (lv_filename_32_0= ruleWildID ) )
                    // InternalLinkerScript.g:2187:5: (lv_filename_32_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2187:5: (lv_filename_32_0= ruleWildID )
                    // InternalLinkerScript.g:2188:6: lv_filename_32_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getFilenameWildIDParserRuleCall_7_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_filename_32_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"filename",
                      							lv_filename_32_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:2207:3: ( () ( (lv_spec_34_0= ruleInputSection ) ) )
                    {
                    // InternalLinkerScript.g:2207:3: ( () ( (lv_spec_34_0= ruleInputSection ) ) )
                    // InternalLinkerScript.g:2208:4: () ( (lv_spec_34_0= ruleInputSection ) )
                    {
                    // InternalLinkerScript.g:2208:4: ()
                    // InternalLinkerScript.g:2209:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementInputSectionAction_8_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2215:4: ( (lv_spec_34_0= ruleInputSection ) )
                    // InternalLinkerScript.g:2216:5: (lv_spec_34_0= ruleInputSection )
                    {
                    // InternalLinkerScript.g:2216:5: (lv_spec_34_0= ruleInputSection )
                    // InternalLinkerScript.g:2217:6: lv_spec_34_0= ruleInputSection
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getSpecInputSectionParserRuleCall_8_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_spec_34_0=ruleInputSection();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementRule());
                      						}
                      						set(
                      							current,
                      							"spec",
                      							lv_spec_34_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.InputSection");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 10 :
                    // InternalLinkerScript.g:2236:3: ( () otherlv_36= ';' )
                    {
                    // InternalLinkerScript.g:2236:3: ( () otherlv_36= ';' )
                    // InternalLinkerScript.g:2237:4: () otherlv_36= ';'
                    {
                    // InternalLinkerScript.g:2237:4: ()
                    // InternalLinkerScript.g:2238:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementNopAction_9_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_36=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_36, grammarAccess.getStatementAccess().getSemicolonKeyword_9_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatement"


    // $ANTLR start "entryRuleStatementAnywhere"
    // InternalLinkerScript.g:2253:1: entryRuleStatementAnywhere returns [EObject current=null] : iv_ruleStatementAnywhere= ruleStatementAnywhere EOF ;
    public final EObject entryRuleStatementAnywhere() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatementAnywhere = null;


        try {
            // InternalLinkerScript.g:2253:58: (iv_ruleStatementAnywhere= ruleStatementAnywhere EOF )
            // InternalLinkerScript.g:2254:2: iv_ruleStatementAnywhere= ruleStatementAnywhere EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementAnywhereRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatementAnywhere=ruleStatementAnywhere();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatementAnywhere; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatementAnywhere"


    // $ANTLR start "ruleStatementAnywhere"
    // InternalLinkerScript.g:2260:1: ruleStatementAnywhere returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) ) ;
    public final EObject ruleStatementAnywhere() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        EObject lv_assignment_1_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        EObject lv_exp_12_0 = null;

        AntlrDatatypeRuleToken lv_message_14_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2266:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) ) )
            // InternalLinkerScript.g:2267:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) )
            {
            // InternalLinkerScript.g:2267:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' ) | ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' ) | ( () otherlv_17= ';' ) )
            int alt37=4;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 61:
            case 62:
            case 63:
            case 76:
            case 78:
            case 79:
            case 81:
            case 82:
            case 92:
                {
                alt37=1;
                }
                break;
            case 15:
                {
                alt37=2;
                }
                break;
            case 16:
                {
                alt37=3;
                }
                break;
            case 10:
                {
                alt37=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }

            switch (alt37) {
                case 1 :
                    // InternalLinkerScript.g:2268:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) )
                    {
                    // InternalLinkerScript.g:2268:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) )
                    // InternalLinkerScript.g:2269:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' )
                    {
                    // InternalLinkerScript.g:2269:4: ()
                    // InternalLinkerScript.g:2270:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2276:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:2277:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:2277:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:2278:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_4);
                    lv_assignment_1_0=ruleAssignmentRule();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"assignment",
                      							lv_assignment_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.AssignmentRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2295:4: (otherlv_2= ';' | otherlv_3= ',' )
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==10) ) {
                        alt36=1;
                    }
                    else if ( (LA36_0==11) ) {
                        alt36=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 36, 0, input);

                        throw nvae;
                    }
                    switch (alt36) {
                        case 1 :
                            // InternalLinkerScript.g:2296:5: otherlv_2= ';'
                            {
                            otherlv_2=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementAnywhereAccess().getSemicolonKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:2301:5: otherlv_3= ','
                            {
                            otherlv_3=(Token)match(input,11,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementAnywhereAccess().getCommaKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2308:3: ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' )
                    {
                    // InternalLinkerScript.g:2308:3: ( () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')' )
                    // InternalLinkerScript.g:2309:4: () otherlv_5= 'ENTRY' otherlv_6= '(' ( (lv_name_7_0= ruleValidID ) ) otherlv_8= ')'
                    {
                    // InternalLinkerScript.g:2309:4: ()
                    // InternalLinkerScript.g:2310:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementEntryAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,15,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementAnywhereAccess().getENTRYKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getStatementAnywhereAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:2324:4: ( (lv_name_7_0= ruleValidID ) )
                    // InternalLinkerScript.g:2325:5: (lv_name_7_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:2325:5: (lv_name_7_0= ruleValidID )
                    // InternalLinkerScript.g:2326:6: lv_name_7_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getNameValidIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_7_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_8=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_8, grammarAccess.getStatementAnywhereAccess().getRightParenthesisKeyword_1_4());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2349:3: ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' )
                    {
                    // InternalLinkerScript.g:2349:3: ( () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')' )
                    // InternalLinkerScript.g:2350:4: () otherlv_10= 'ASSERT' otherlv_11= '(' ( (lv_exp_12_0= ruleLExpression ) ) otherlv_13= ',' ( (lv_message_14_0= ruleValidID ) ) otherlv_15= ')'
                    {
                    // InternalLinkerScript.g:2350:4: ()
                    // InternalLinkerScript.g:2351:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementAssertAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_10=(Token)match(input,16,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementAnywhereAccess().getASSERTKeyword_2_1());
                      			
                    }
                    otherlv_11=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getStatementAnywhereAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:2365:4: ( (lv_exp_12_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2366:5: (lv_exp_12_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2366:5: (lv_exp_12_0= ruleLExpression )
                    // InternalLinkerScript.g:2367:6: lv_exp_12_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getExpLExpressionParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_exp_12_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_12_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_13=(Token)match(input,11,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getStatementAnywhereAccess().getCommaKeyword_2_4());
                      			
                    }
                    // InternalLinkerScript.g:2388:4: ( (lv_message_14_0= ruleValidID ) )
                    // InternalLinkerScript.g:2389:5: (lv_message_14_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:2389:5: (lv_message_14_0= ruleValidID )
                    // InternalLinkerScript.g:2390:6: lv_message_14_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAnywhereAccess().getMessageValidIDParserRuleCall_2_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_message_14_0=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getStatementAnywhereRule());
                      						}
                      						set(
                      							current,
                      							"message",
                      							lv_message_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_15=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementAnywhereAccess().getRightParenthesisKeyword_2_6());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2413:3: ( () otherlv_17= ';' )
                    {
                    // InternalLinkerScript.g:2413:3: ( () otherlv_17= ';' )
                    // InternalLinkerScript.g:2414:4: () otherlv_17= ';'
                    {
                    // InternalLinkerScript.g:2414:4: ()
                    // InternalLinkerScript.g:2415:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAnywhereAccess().getStatementNopAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_17=(Token)match(input,10,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getStatementAnywhereAccess().getSemicolonKeyword_3_1());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatementAnywhere"


    // $ANTLR start "entryRuleStatementDataSize"
    // InternalLinkerScript.g:2430:1: entryRuleStatementDataSize returns [String current=null] : iv_ruleStatementDataSize= ruleStatementDataSize EOF ;
    public final String entryRuleStatementDataSize() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleStatementDataSize = null;


        try {
            // InternalLinkerScript.g:2430:57: (iv_ruleStatementDataSize= ruleStatementDataSize EOF )
            // InternalLinkerScript.g:2431:2: iv_ruleStatementDataSize= ruleStatementDataSize EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getStatementDataSizeRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleStatementDataSize=ruleStatementDataSize();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleStatementDataSize.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleStatementDataSize"


    // $ANTLR start "ruleStatementDataSize"
    // InternalLinkerScript.g:2437:1: ruleStatementDataSize returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' ) ;
    public final AntlrDatatypeRuleToken ruleStatementDataSize() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:2443:2: ( (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' ) )
            // InternalLinkerScript.g:2444:2: (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' )
            {
            // InternalLinkerScript.g:2444:2: (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' )
            int alt38=5;
            switch ( input.LA(1) ) {
            case 56:
                {
                alt38=1;
                }
                break;
            case 57:
                {
                alt38=2;
                }
                break;
            case 58:
                {
                alt38=3;
                }
                break;
            case 59:
                {
                alt38=4;
                }
                break;
            case 60:
                {
                alt38=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }

            switch (alt38) {
                case 1 :
                    // InternalLinkerScript.g:2445:3: kw= 'BYTE'
                    {
                    kw=(Token)match(input,56,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getBYTEKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2451:3: kw= 'SHORT'
                    {
                    kw=(Token)match(input,57,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getSHORTKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2457:3: kw= 'LONG'
                    {
                    kw=(Token)match(input,58,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getLONGKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2463:3: kw= 'QUAD'
                    {
                    kw=(Token)match(input,59,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getQUADKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2469:3: kw= 'SQUAD'
                    {
                    kw=(Token)match(input,60,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getSQUADKeyword_4());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleStatementDataSize"


    // $ANTLR start "entryRuleAssignmentRule"
    // InternalLinkerScript.g:2478:1: entryRuleAssignmentRule returns [EObject current=null] : iv_ruleAssignmentRule= ruleAssignmentRule EOF ;
    public final EObject entryRuleAssignmentRule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAssignmentRule = null;


        try {
            // InternalLinkerScript.g:2478:55: (iv_ruleAssignmentRule= ruleAssignmentRule EOF )
            // InternalLinkerScript.g:2479:2: iv_ruleAssignmentRule= ruleAssignmentRule EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAssignmentRuleRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAssignmentRule=ruleAssignmentRule();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAssignmentRule; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAssignmentRule"


    // $ANTLR start "ruleAssignmentRule"
    // InternalLinkerScript.g:2485:1: ruleAssignmentRule returns [EObject current=null] : ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) ) ;
    public final EObject ruleAssignmentRule() throws RecognitionException {
        EObject current = null;

        Token otherlv_5=null;
        Token otherlv_6=null;
        Token lv_feature_8_0=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        Token lv_feature_15_0=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token lv_feature_22_0=null;
        Token otherlv_24=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_exp_3_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        EObject lv_exp_9_0 = null;

        AntlrDatatypeRuleToken lv_name_14_0 = null;

        EObject lv_exp_16_0 = null;

        AntlrDatatypeRuleToken lv_name_21_0 = null;

        EObject lv_exp_23_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2491:2: ( ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) ) )
            // InternalLinkerScript.g:2492:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) )
            {
            // InternalLinkerScript.g:2492:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) )
            int alt39=4;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 76:
            case 78:
            case 79:
            case 81:
            case 82:
            case 92:
                {
                alt39=1;
                }
                break;
            case 61:
                {
                alt39=2;
                }
                break;
            case 62:
                {
                alt39=3;
                }
                break;
            case 63:
                {
                alt39=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }

            switch (alt39) {
                case 1 :
                    // InternalLinkerScript.g:2493:3: ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) )
                    {
                    // InternalLinkerScript.g:2493:3: ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) )
                    // InternalLinkerScript.g:2494:4: () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) )
                    {
                    // InternalLinkerScript.g:2494:4: ()
                    // InternalLinkerScript.g:2495:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2501:4: ( (lv_name_1_0= ruleWildID ) )
                    // InternalLinkerScript.g:2502:5: (lv_name_1_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2502:5: (lv_name_1_0= ruleWildID )
                    // InternalLinkerScript.g:2503:6: lv_name_1_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_35);
                    lv_name_1_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2520:4: ( (lv_feature_2_0= ruleOpAssign ) )
                    // InternalLinkerScript.g:2521:5: (lv_feature_2_0= ruleOpAssign )
                    {
                    // InternalLinkerScript.g:2521:5: (lv_feature_2_0= ruleOpAssign )
                    // InternalLinkerScript.g:2522:6: lv_feature_2_0= ruleOpAssign
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getFeatureOpAssignParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_8);
                    lv_feature_2_0=ruleOpAssign();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"feature",
                      							lv_feature_2_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.OpAssign");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2539:4: ( (lv_exp_3_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2540:5: (lv_exp_3_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2540:5: (lv_exp_3_0= ruleLExpression )
                    // InternalLinkerScript.g:2541:6: lv_exp_3_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_0_3_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_exp_3_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2560:3: ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' )
                    {
                    // InternalLinkerScript.g:2560:3: ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' )
                    // InternalLinkerScript.g:2561:4: () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')'
                    {
                    // InternalLinkerScript.g:2561:4: ()
                    // InternalLinkerScript.g:2562:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentHiddenAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,61,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getAssignmentRuleAccess().getHIDDENKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:2576:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:2577:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2577:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:2578:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2595:4: ( (lv_feature_8_0= '=' ) )
                    // InternalLinkerScript.g:2596:5: (lv_feature_8_0= '=' )
                    {
                    // InternalLinkerScript.g:2596:5: (lv_feature_8_0= '=' )
                    // InternalLinkerScript.g:2597:6: lv_feature_8_0= '='
                    {
                    lv_feature_8_0=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_feature_8_0, grammarAccess.getAssignmentRuleAccess().getFeatureEqualsSignKeyword_1_4_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getAssignmentRuleRule());
                      						}
                      						setWithLastConsumed(current, "feature", lv_feature_8_0, "=");
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2609:4: ( (lv_exp_9_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2610:5: (lv_exp_9_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2610:5: (lv_exp_9_0= ruleLExpression )
                    // InternalLinkerScript.g:2611:6: lv_exp_9_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_1_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_9_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_9_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_10=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getAssignmentRuleAccess().getRightParenthesisKeyword_1_6());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2634:3: ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:2634:3: ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:2635:4: () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:2635:4: ()
                    // InternalLinkerScript.g:2636:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentProvideAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_12=(Token)match(input,62,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getAssignmentRuleAccess().getPROVIDEKeyword_2_1());
                      			
                    }
                    otherlv_13=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:2650:4: ( (lv_name_14_0= ruleWildID ) )
                    // InternalLinkerScript.g:2651:5: (lv_name_14_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2651:5: (lv_name_14_0= ruleWildID )
                    // InternalLinkerScript.g:2652:6: lv_name_14_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_name_14_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2669:4: ( (lv_feature_15_0= '=' ) )
                    // InternalLinkerScript.g:2670:5: (lv_feature_15_0= '=' )
                    {
                    // InternalLinkerScript.g:2670:5: (lv_feature_15_0= '=' )
                    // InternalLinkerScript.g:2671:6: lv_feature_15_0= '='
                    {
                    lv_feature_15_0=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_feature_15_0, grammarAccess.getAssignmentRuleAccess().getFeatureEqualsSignKeyword_2_4_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getAssignmentRuleRule());
                      						}
                      						setWithLastConsumed(current, "feature", lv_feature_15_0, "=");
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2683:4: ( (lv_exp_16_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2684:5: (lv_exp_16_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2684:5: (lv_exp_16_0= ruleLExpression )
                    // InternalLinkerScript.g:2685:6: lv_exp_16_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_2_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_16_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getAssignmentRuleAccess().getRightParenthesisKeyword_2_6());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2708:3: ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' )
                    {
                    // InternalLinkerScript.g:2708:3: ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' )
                    // InternalLinkerScript.g:2709:4: () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')'
                    {
                    // InternalLinkerScript.g:2709:4: ()
                    // InternalLinkerScript.g:2710:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentProvideHiddenAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,63,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getAssignmentRuleAccess().getPROVIDE_HIDDENKeyword_3_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:2724:4: ( (lv_name_21_0= ruleWildID ) )
                    // InternalLinkerScript.g:2725:5: (lv_name_21_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2725:5: (lv_name_21_0= ruleWildID )
                    // InternalLinkerScript.g:2726:6: lv_name_21_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_36);
                    lv_name_21_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_21_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2743:4: ( (lv_feature_22_0= '=' ) )
                    // InternalLinkerScript.g:2744:5: (lv_feature_22_0= '=' )
                    {
                    // InternalLinkerScript.g:2744:5: (lv_feature_22_0= '=' )
                    // InternalLinkerScript.g:2745:6: lv_feature_22_0= '='
                    {
                    lv_feature_22_0=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_feature_22_0, grammarAccess.getAssignmentRuleAccess().getFeatureEqualsSignKeyword_3_4_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getAssignmentRuleRule());
                      						}
                      						setWithLastConsumed(current, "feature", lv_feature_22_0, "=");
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2757:4: ( (lv_exp_23_0= ruleLExpression ) )
                    // InternalLinkerScript.g:2758:5: (lv_exp_23_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:2758:5: (lv_exp_23_0= ruleLExpression )
                    // InternalLinkerScript.g:2759:6: lv_exp_23_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_3_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_exp_23_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAssignmentRuleRule());
                      						}
                      						set(
                      							current,
                      							"exp",
                      							lv_exp_23_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_24=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_24, grammarAccess.getAssignmentRuleAccess().getRightParenthesisKeyword_3_6());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAssignmentRule"


    // $ANTLR start "entryRuleOpAssign"
    // InternalLinkerScript.g:2785:1: entryRuleOpAssign returns [String current=null] : iv_ruleOpAssign= ruleOpAssign EOF ;
    public final String entryRuleOpAssign() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAssign = null;


        try {
            // InternalLinkerScript.g:2785:48: (iv_ruleOpAssign= ruleOpAssign EOF )
            // InternalLinkerScript.g:2786:2: iv_ruleOpAssign= ruleOpAssign EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpAssignRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpAssign=ruleOpAssign();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpAssign.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAssign"


    // $ANTLR start "ruleOpAssign"
    // InternalLinkerScript.g:2792:1: ruleOpAssign returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) ;
    public final AntlrDatatypeRuleToken ruleOpAssign() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:2798:2: ( (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) )
            // InternalLinkerScript.g:2799:2: (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            {
            // InternalLinkerScript.g:2799:2: (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            int alt40=9;
            switch ( input.LA(1) ) {
            case 41:
                {
                alt40=1;
                }
                break;
            case 64:
                {
                alt40=2;
                }
                break;
            case 65:
                {
                alt40=3;
                }
                break;
            case 66:
                {
                alt40=4;
                }
                break;
            case 67:
                {
                alt40=5;
                }
                break;
            case 68:
                {
                alt40=6;
                }
                break;
            case 40:
                {
                alt40=7;
                }
                break;
            case 70:
                {
                alt40=8;
                }
                break;
            case 71:
                {
                alt40=9;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }

            switch (alt40) {
                case 1 :
                    // InternalLinkerScript.g:2800:3: kw= '='
                    {
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2806:3: kw= '+='
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getPlusSignEqualsSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2812:3: kw= '-='
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getHyphenMinusEqualsSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2818:3: kw= '*='
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getAsteriskEqualsSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2824:3: kw= '/='
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getSolidusEqualsSignKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:2830:3: (kw= '<' kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:2830:3: (kw= '<' kw= '<' kw= '=' )
                    // InternalLinkerScript.g:2831:4: kw= '<' kw= '<' kw= '='
                    {
                    kw=(Token)match(input,68,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getLessThanSignKeyword_5_0());
                      			
                    }
                    kw=(Token)match(input,68,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getLessThanSignKeyword_5_1());
                      			
                    }
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getEqualsSignKeyword_5_2());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:2848:3: (kw= '>' kw= '>=' )
                    {
                    // InternalLinkerScript.g:2848:3: (kw= '>' kw= '>=' )
                    // InternalLinkerScript.g:2849:4: kw= '>' kw= '>='
                    {
                    kw=(Token)match(input,40,FOLLOW_38); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getGreaterThanSignKeyword_6_0());
                      			
                    }
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getGreaterThanSignEqualsSignKeyword_6_1());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:2861:3: kw= '&='
                    {
                    kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getAmpersandEqualsSignKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:2867:3: kw= '|='
                    {
                    kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getVerticalLineEqualsSignKeyword_8());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAssign"


    // $ANTLR start "entryRuleInputSection"
    // InternalLinkerScript.g:2876:1: entryRuleInputSection returns [EObject current=null] : iv_ruleInputSection= ruleInputSection EOF ;
    public final EObject entryRuleInputSection() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleInputSection = null;


        try {
            // InternalLinkerScript.g:2876:53: (iv_ruleInputSection= ruleInputSection EOF )
            // InternalLinkerScript.g:2877:2: iv_ruleInputSection= ruleInputSection EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getInputSectionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleInputSection=ruleInputSection();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleInputSection; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleInputSection"


    // $ANTLR start "ruleInputSection"
    // InternalLinkerScript.g:2883:1: ruleInputSection returns [EObject current=null] : ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' ) ) ;
    public final EObject ruleInputSection() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_20=null;
        Token lv_keep_22_0=null;
        Token otherlv_23=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        Token otherlv_31=null;
        Token lv_keep_33_0=null;
        Token otherlv_34=null;
        Token otherlv_35=null;
        Token otherlv_36=null;
        Token otherlv_38=null;
        Token otherlv_40=null;
        Token otherlv_42=null;
        Token otherlv_44=null;
        Token otherlv_46=null;
        Token otherlv_47=null;
        AntlrDatatypeRuleToken lv_flags_3_0 = null;

        AntlrDatatypeRuleToken lv_flags_5_0 = null;

        AntlrDatatypeRuleToken lv_file_7_0 = null;

        AntlrDatatypeRuleToken lv_flags_11_0 = null;

        AntlrDatatypeRuleToken lv_flags_13_0 = null;

        EObject lv_wildFile_15_0 = null;

        EObject lv_sections_17_0 = null;

        EObject lv_sections_19_0 = null;

        AntlrDatatypeRuleToken lv_flags_26_0 = null;

        AntlrDatatypeRuleToken lv_flags_28_0 = null;

        AntlrDatatypeRuleToken lv_file_30_0 = null;

        AntlrDatatypeRuleToken lv_flags_37_0 = null;

        AntlrDatatypeRuleToken lv_flags_39_0 = null;

        EObject lv_wildFile_41_0 = null;

        EObject lv_sections_43_0 = null;

        EObject lv_sections_45_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2889:2: ( ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' ) ) )
            // InternalLinkerScript.g:2890:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' ) )
            {
            // InternalLinkerScript.g:2890:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' ) )
            int alt53=4;
            alt53 = dfa53.predict(input);
            switch (alt53) {
                case 1 :
                    // InternalLinkerScript.g:2891:3: ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:2891:3: ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:2892:4: () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:2892:4: ()
                    // InternalLinkerScript.g:2893:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionFileAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2899:4: (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )?
                    int alt42=2;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==72) ) {
                        alt42=1;
                    }
                    switch (alt42) {
                        case 1 :
                            // InternalLinkerScript.g:2900:5: otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')'
                            {
                            otherlv_1=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_1, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_0_1_0());
                              				
                            }
                            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_0_1_1());
                              				
                            }
                            // InternalLinkerScript.g:2908:5: ( (lv_flags_3_0= ruleWildID ) )
                            // InternalLinkerScript.g:2909:6: (lv_flags_3_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:2909:6: (lv_flags_3_0= ruleWildID )
                            // InternalLinkerScript.g:2910:7: lv_flags_3_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_0_1_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_3_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_3_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:2927:5: (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )*
                            loop41:
                            do {
                                int alt41=2;
                                int LA41_0 = input.LA(1);

                                if ( (LA41_0==73) ) {
                                    alt41=1;
                                }


                                switch (alt41) {
                            	case 1 :
                            	    // InternalLinkerScript.g:2928:6: otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) )
                            	    {
                            	    otherlv_4=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_4, grammarAccess.getInputSectionAccess().getAmpersandKeyword_0_1_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:2932:6: ( (lv_flags_5_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:2933:7: (lv_flags_5_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:2933:7: (lv_flags_5_0= ruleWildID )
                            	    // InternalLinkerScript.g:2934:8: lv_flags_5_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_0_1_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_5_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_5_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop41;
                                }
                            } while (true);

                            otherlv_6=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_6, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_0_1_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:2957:4: ( (lv_file_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:2958:5: (lv_file_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2958:5: (lv_file_7_0= ruleWildID )
                    // InternalLinkerScript.g:2959:6: lv_file_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getFileWildIDParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_file_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"file",
                      							lv_file_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2978:3: ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' )
                    {
                    // InternalLinkerScript.g:2978:3: ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' )
                    // InternalLinkerScript.g:2979:4: () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')'
                    {
                    // InternalLinkerScript.g:2979:4: ()
                    // InternalLinkerScript.g:2980:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionWildAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2986:4: (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==72) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // InternalLinkerScript.g:2987:5: otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')'
                            {
                            otherlv_9=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_9, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_1_1_0());
                              				
                            }
                            otherlv_10=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_10, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_1_1_1());
                              				
                            }
                            // InternalLinkerScript.g:2995:5: ( (lv_flags_11_0= ruleWildID ) )
                            // InternalLinkerScript.g:2996:6: (lv_flags_11_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:2996:6: (lv_flags_11_0= ruleWildID )
                            // InternalLinkerScript.g:2997:7: lv_flags_11_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_1_1_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_11_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_11_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:3014:5: (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )*
                            loop43:
                            do {
                                int alt43=2;
                                int LA43_0 = input.LA(1);

                                if ( (LA43_0==73) ) {
                                    alt43=1;
                                }


                                switch (alt43) {
                            	case 1 :
                            	    // InternalLinkerScript.g:3015:6: otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) )
                            	    {
                            	    otherlv_12=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_12, grammarAccess.getInputSectionAccess().getAmpersandKeyword_1_1_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:3019:6: ( (lv_flags_13_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:3020:7: (lv_flags_13_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:3020:7: (lv_flags_13_0= ruleWildID )
                            	    // InternalLinkerScript.g:3021:8: lv_flags_13_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_1_1_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_13_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_13_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop43;
                                }
                            } while (true);

                            otherlv_14=(Token)match(input,14,FOLLOW_40); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_1_1_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:3044:4: ( (lv_wildFile_15_0= ruleWildcard ) )
                    // InternalLinkerScript.g:3045:5: (lv_wildFile_15_0= ruleWildcard )
                    {
                    // InternalLinkerScript.g:3045:5: (lv_wildFile_15_0= ruleWildcard )
                    // InternalLinkerScript.g:3046:6: lv_wildFile_15_0= ruleWildcard
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getWildFileWildcardParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_wildFile_15_0=ruleWildcard();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"wildFile",
                      							lv_wildFile_15_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.Wildcard");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_16=(Token)match(input,13,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_16, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_1_3());
                      			
                    }
                    // InternalLinkerScript.g:3067:4: ( (lv_sections_17_0= ruleWildcard ) )
                    // InternalLinkerScript.g:3068:5: (lv_sections_17_0= ruleWildcard )
                    {
                    // InternalLinkerScript.g:3068:5: (lv_sections_17_0= ruleWildcard )
                    // InternalLinkerScript.g:3069:6: lv_sections_17_0= ruleWildcard
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardParserRuleCall_1_4_0());
                      					
                    }
                    pushFollow(FOLLOW_41);
                    lv_sections_17_0=ruleWildcard();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						add(
                      							current,
                      							"sections",
                      							lv_sections_17_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.Wildcard");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:3086:4: ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )*
                    loop46:
                    do {
                        int alt46=2;
                        int LA46_0 = input.LA(1);

                        if ( (LA46_0==RULE_ID||LA46_0==11||LA46_0==54||(LA46_0>=75 && LA46_0<=76)||(LA46_0>=78 && LA46_0<=79)||(LA46_0>=81 && LA46_0<=82)||LA46_0==92||(LA46_0>=97 && LA46_0<=100)) ) {
                            alt46=1;
                        }


                        switch (alt46) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3087:5: (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) )
                    	    {
                    	    // InternalLinkerScript.g:3087:5: (otherlv_18= ',' )?
                    	    int alt45=2;
                    	    int LA45_0 = input.LA(1);

                    	    if ( (LA45_0==11) ) {
                    	        alt45=1;
                    	    }
                    	    switch (alt45) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:3088:6: otherlv_18= ','
                    	            {
                    	            otherlv_18=(Token)match(input,11,FOLLOW_40); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_18, grammarAccess.getInputSectionAccess().getCommaKeyword_1_5_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:3093:5: ( (lv_sections_19_0= ruleWildcard ) )
                    	    // InternalLinkerScript.g:3094:6: (lv_sections_19_0= ruleWildcard )
                    	    {
                    	    // InternalLinkerScript.g:3094:6: (lv_sections_19_0= ruleWildcard )
                    	    // InternalLinkerScript.g:3095:7: lv_sections_19_0= ruleWildcard
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardParserRuleCall_1_5_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_41);
                    	    lv_sections_19_0=ruleWildcard();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"sections",
                    	      								lv_sections_19_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.Wildcard");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop46;
                        }
                    } while (true);

                    otherlv_20=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_1_6());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3119:3: ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' )
                    {
                    // InternalLinkerScript.g:3119:3: ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' )
                    // InternalLinkerScript.g:3120:4: () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')'
                    {
                    // InternalLinkerScript.g:3120:4: ()
                    // InternalLinkerScript.g:3121:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionFileAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3127:4: ( (lv_keep_22_0= 'KEEP' ) )
                    // InternalLinkerScript.g:3128:5: (lv_keep_22_0= 'KEEP' )
                    {
                    // InternalLinkerScript.g:3128:5: (lv_keep_22_0= 'KEEP' )
                    // InternalLinkerScript.g:3129:6: lv_keep_22_0= 'KEEP'
                    {
                    lv_keep_22_0=(Token)match(input,74,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_keep_22_0, grammarAccess.getInputSectionAccess().getKeepKEEPKeyword_2_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getInputSectionRule());
                      						}
                      						setWithLastConsumed(current, "keep", true, "KEEP");
                      					
                    }

                    }


                    }

                    otherlv_23=(Token)match(input,13,FOLLOW_42); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_23, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:3145:4: (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )?
                    int alt48=2;
                    int LA48_0 = input.LA(1);

                    if ( (LA48_0==72) ) {
                        alt48=1;
                    }
                    switch (alt48) {
                        case 1 :
                            // InternalLinkerScript.g:3146:5: otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')'
                            {
                            otherlv_24=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_24, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_2_3_0());
                              				
                            }
                            otherlv_25=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_25, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_2_3_1());
                              				
                            }
                            // InternalLinkerScript.g:3154:5: ( (lv_flags_26_0= ruleWildID ) )
                            // InternalLinkerScript.g:3155:6: (lv_flags_26_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:3155:6: (lv_flags_26_0= ruleWildID )
                            // InternalLinkerScript.g:3156:7: lv_flags_26_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_2_3_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_26_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_26_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:3173:5: (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )*
                            loop47:
                            do {
                                int alt47=2;
                                int LA47_0 = input.LA(1);

                                if ( (LA47_0==73) ) {
                                    alt47=1;
                                }


                                switch (alt47) {
                            	case 1 :
                            	    // InternalLinkerScript.g:3174:6: otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) )
                            	    {
                            	    otherlv_27=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_27, grammarAccess.getInputSectionAccess().getAmpersandKeyword_2_3_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:3178:6: ( (lv_flags_28_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:3179:7: (lv_flags_28_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:3179:7: (lv_flags_28_0= ruleWildID )
                            	    // InternalLinkerScript.g:3180:8: lv_flags_28_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_2_3_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_28_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_28_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop47;
                                }
                            } while (true);

                            otherlv_29=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_29, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_2_3_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:3203:4: ( (lv_file_30_0= ruleWildID ) )
                    // InternalLinkerScript.g:3204:5: (lv_file_30_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3204:5: (lv_file_30_0= ruleWildID )
                    // InternalLinkerScript.g:3205:6: lv_file_30_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getFileWildIDParserRuleCall_2_4_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_file_30_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"file",
                      							lv_file_30_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_31=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_31, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_2_5());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:3228:3: ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' )
                    {
                    // InternalLinkerScript.g:3228:3: ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' )
                    // InternalLinkerScript.g:3229:4: () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')'
                    {
                    // InternalLinkerScript.g:3229:4: ()
                    // InternalLinkerScript.g:3230:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionWildAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3236:4: ( (lv_keep_33_0= 'KEEP' ) )
                    // InternalLinkerScript.g:3237:5: (lv_keep_33_0= 'KEEP' )
                    {
                    // InternalLinkerScript.g:3237:5: (lv_keep_33_0= 'KEEP' )
                    // InternalLinkerScript.g:3238:6: lv_keep_33_0= 'KEEP'
                    {
                    lv_keep_33_0=(Token)match(input,74,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(lv_keep_33_0, grammarAccess.getInputSectionAccess().getKeepKEEPKeyword_3_1_0());
                      					
                    }
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElement(grammarAccess.getInputSectionRule());
                      						}
                      						setWithLastConsumed(current, "keep", true, "KEEP");
                      					
                    }

                    }


                    }

                    otherlv_34=(Token)match(input,13,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_34, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:3254:4: (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )?
                    int alt50=2;
                    int LA50_0 = input.LA(1);

                    if ( (LA50_0==72) ) {
                        alt50=1;
                    }
                    switch (alt50) {
                        case 1 :
                            // InternalLinkerScript.g:3255:5: otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')'
                            {
                            otherlv_35=(Token)match(input,72,FOLLOW_5); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_35, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_3_3_0());
                              				
                            }
                            otherlv_36=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_36, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_3_1());
                              				
                            }
                            // InternalLinkerScript.g:3263:5: ( (lv_flags_37_0= ruleWildID ) )
                            // InternalLinkerScript.g:3264:6: (lv_flags_37_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:3264:6: (lv_flags_37_0= ruleWildID )
                            // InternalLinkerScript.g:3265:7: lv_flags_37_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_3_3_2_0());
                              						
                            }
                            pushFollow(FOLLOW_39);
                            lv_flags_37_0=ruleWildID();

                            state._fsp--;
                            if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              							if (current==null) {
                              								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                              							}
                              							add(
                              								current,
                              								"flags",
                              								lv_flags_37_0,
                              								"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                              							afterParserOrEnumRuleCall();
                              						
                            }

                            }


                            }

                            // InternalLinkerScript.g:3282:5: (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )*
                            loop49:
                            do {
                                int alt49=2;
                                int LA49_0 = input.LA(1);

                                if ( (LA49_0==73) ) {
                                    alt49=1;
                                }


                                switch (alt49) {
                            	case 1 :
                            	    // InternalLinkerScript.g:3283:6: otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) )
                            	    {
                            	    otherlv_38=(Token)match(input,73,FOLLOW_6); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_38, grammarAccess.getInputSectionAccess().getAmpersandKeyword_3_3_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:3287:6: ( (lv_flags_39_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:3288:7: (lv_flags_39_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:3288:7: (lv_flags_39_0= ruleWildID )
                            	    // InternalLinkerScript.g:3289:8: lv_flags_39_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_3_3_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_39);
                            	    lv_flags_39_0=ruleWildID();

                            	    state._fsp--;
                            	    if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      								if (current==null) {
                            	      									current = createModelElementForParent(grammarAccess.getInputSectionRule());
                            	      								}
                            	      								add(
                            	      									current,
                            	      									"flags",
                            	      									lv_flags_39_0,
                            	      									"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                            	      								afterParserOrEnumRuleCall();
                            	      							
                            	    }

                            	    }


                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop49;
                                }
                            } while (true);

                            otherlv_40=(Token)match(input,14,FOLLOW_40); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_40, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_3_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:3312:4: ( (lv_wildFile_41_0= ruleWildcard ) )
                    // InternalLinkerScript.g:3313:5: (lv_wildFile_41_0= ruleWildcard )
                    {
                    // InternalLinkerScript.g:3313:5: (lv_wildFile_41_0= ruleWildcard )
                    // InternalLinkerScript.g:3314:6: lv_wildFile_41_0= ruleWildcard
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getWildFileWildcardParserRuleCall_3_4_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_wildFile_41_0=ruleWildcard();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						set(
                      							current,
                      							"wildFile",
                      							lv_wildFile_41_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.Wildcard");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_42=(Token)match(input,13,FOLLOW_40); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_42, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_5());
                      			
                    }
                    // InternalLinkerScript.g:3335:4: ( (lv_sections_43_0= ruleWildcard ) )
                    // InternalLinkerScript.g:3336:5: (lv_sections_43_0= ruleWildcard )
                    {
                    // InternalLinkerScript.g:3336:5: (lv_sections_43_0= ruleWildcard )
                    // InternalLinkerScript.g:3337:6: lv_sections_43_0= ruleWildcard
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardParserRuleCall_3_6_0());
                      					
                    }
                    pushFollow(FOLLOW_41);
                    lv_sections_43_0=ruleWildcard();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getInputSectionRule());
                      						}
                      						add(
                      							current,
                      							"sections",
                      							lv_sections_43_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.Wildcard");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:3354:4: ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )*
                    loop52:
                    do {
                        int alt52=2;
                        int LA52_0 = input.LA(1);

                        if ( (LA52_0==RULE_ID||LA52_0==11||LA52_0==54||(LA52_0>=75 && LA52_0<=76)||(LA52_0>=78 && LA52_0<=79)||(LA52_0>=81 && LA52_0<=82)||LA52_0==92||(LA52_0>=97 && LA52_0<=100)) ) {
                            alt52=1;
                        }


                        switch (alt52) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3355:5: (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) )
                    	    {
                    	    // InternalLinkerScript.g:3355:5: (otherlv_44= ',' )?
                    	    int alt51=2;
                    	    int LA51_0 = input.LA(1);

                    	    if ( (LA51_0==11) ) {
                    	        alt51=1;
                    	    }
                    	    switch (alt51) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:3356:6: otherlv_44= ','
                    	            {
                    	            otherlv_44=(Token)match(input,11,FOLLOW_40); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_44, grammarAccess.getInputSectionAccess().getCommaKeyword_3_7_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:3361:5: ( (lv_sections_45_0= ruleWildcard ) )
                    	    // InternalLinkerScript.g:3362:6: (lv_sections_45_0= ruleWildcard )
                    	    {
                    	    // InternalLinkerScript.g:3362:6: (lv_sections_45_0= ruleWildcard )
                    	    // InternalLinkerScript.g:3363:7: lv_sections_45_0= ruleWildcard
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardParserRuleCall_3_7_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_41);
                    	    lv_sections_45_0=ruleWildcard();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      							if (current==null) {
                    	      								current = createModelElementForParent(grammarAccess.getInputSectionRule());
                    	      							}
                    	      							add(
                    	      								current,
                    	      								"sections",
                    	      								lv_sections_45_0,
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.Wildcard");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop52;
                        }
                    } while (true);

                    otherlv_46=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_46, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_8());
                      			
                    }
                    otherlv_47=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_47, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_9());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleInputSection"


    // $ANTLR start "entryRuleWildcard"
    // InternalLinkerScript.g:3394:1: entryRuleWildcard returns [EObject current=null] : iv_ruleWildcard= ruleWildcard EOF ;
    public final EObject entryRuleWildcard() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleWildcard = null;


        try {
            // InternalLinkerScript.g:3394:49: (iv_ruleWildcard= ruleWildcard EOF )
            // InternalLinkerScript.g:3395:2: iv_ruleWildcard= ruleWildcard EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getWildcardRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleWildcard=ruleWildcard();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleWildcard; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleWildcard"


    // $ANTLR start "ruleWildcard"
    // InternalLinkerScript.g:3401:1: ruleWildcard returns [EObject current=null] : ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' ) | ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' ) ) ;
    public final EObject ruleWildcard() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token otherlv_23=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;

        AntlrDatatypeRuleToken lv_excludes_5_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        Enumerator lv_primarySort_9_0 = null;

        AntlrDatatypeRuleToken lv_name_11_0 = null;

        Enumerator lv_primarySort_14_0 = null;

        Enumerator lv_secondarySort_16_0 = null;

        AntlrDatatypeRuleToken lv_name_18_0 = null;

        Enumerator lv_primarySort_22_0 = null;

        AntlrDatatypeRuleToken lv_excludes_26_0 = null;

        AntlrDatatypeRuleToken lv_name_28_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3407:2: ( ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' ) | ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' ) ) )
            // InternalLinkerScript.g:3408:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' ) | ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' ) )
            {
            // InternalLinkerScript.g:3408:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' ) | ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' ) )
            int alt56=5;
            alt56 = dfa56.predict(input);
            switch (alt56) {
                case 1 :
                    // InternalLinkerScript.g:3409:3: ( () ( (lv_name_1_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:3409:3: ( () ( (lv_name_1_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:3410:4: () ( (lv_name_1_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:3410:4: ()
                    // InternalLinkerScript.g:3411:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardAccess().getWildcardAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3417:4: ( (lv_name_1_0= ruleWildID ) )
                    // InternalLinkerScript.g:3418:5: (lv_name_1_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3418:5: (lv_name_1_0= ruleWildID )
                    // InternalLinkerScript.g:3419:6: lv_name_1_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getNameWildIDParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_1_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3438:3: ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:3438:3: ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:3439:4: () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:3439:4: ()
                    // InternalLinkerScript.g:3440:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardAccess().getWildcardAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,75,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getWildcardAccess().getEXCLUDE_FILEKeyword_1_1());
                      			
                    }
                    otherlv_4=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getWildcardAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:3454:4: ( (lv_excludes_5_0= ruleWildID ) )+
                    int cnt54=0;
                    loop54:
                    do {
                        int alt54=2;
                        int LA54_0 = input.LA(1);

                        if ( (LA54_0==RULE_ID||LA54_0==76||(LA54_0>=78 && LA54_0<=79)||(LA54_0>=81 && LA54_0<=82)||LA54_0==92) ) {
                            alt54=1;
                        }


                        switch (alt54) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3455:5: (lv_excludes_5_0= ruleWildID )
                    	    {
                    	    // InternalLinkerScript.g:3455:5: (lv_excludes_5_0= ruleWildID )
                    	    // InternalLinkerScript.g:3456:6: lv_excludes_5_0= ruleWildID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getWildcardAccess().getExcludesWildIDParserRuleCall_1_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_excludes_5_0=ruleWildID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"excludes",
                    	      							lv_excludes_5_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt54 >= 1 ) break loop54;
                    	    if (state.backtracking>0) {state.failed=true; return current;}
                                EarlyExitException eee =
                                    new EarlyExitException(54, input);
                                throw eee;
                        }
                        cnt54++;
                    } while (true);

                    otherlv_6=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getWildcardAccess().getRightParenthesisKeyword_1_4());
                      			
                    }
                    // InternalLinkerScript.g:3477:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:3478:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3478:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:3479:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getNameWildIDParserRuleCall_1_5_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_7_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3498:3: ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' )
                    {
                    // InternalLinkerScript.g:3498:3: ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' )
                    // InternalLinkerScript.g:3499:4: () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')'
                    {
                    // InternalLinkerScript.g:3499:4: ()
                    // InternalLinkerScript.g:3500:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardAccess().getWildcardAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3506:4: ( (lv_primarySort_9_0= ruleWildcardSort ) )
                    // InternalLinkerScript.g:3507:5: (lv_primarySort_9_0= ruleWildcardSort )
                    {
                    // InternalLinkerScript.g:3507:5: (lv_primarySort_9_0= ruleWildcardSort )
                    // InternalLinkerScript.g:3508:6: lv_primarySort_9_0= ruleWildcardSort
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getPrimarySortWildcardSortEnumRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_primarySort_9_0=ruleWildcardSort();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"primarySort",
                      							lv_primarySort_9_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardSort");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_10=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getWildcardAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:3529:4: ( (lv_name_11_0= ruleWildID ) )
                    // InternalLinkerScript.g:3530:5: (lv_name_11_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3530:5: (lv_name_11_0= ruleWildID )
                    // InternalLinkerScript.g:3531:6: lv_name_11_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getNameWildIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_11_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_11_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_12=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getWildcardAccess().getRightParenthesisKeyword_2_4());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:3554:3: ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' )
                    {
                    // InternalLinkerScript.g:3554:3: ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' )
                    // InternalLinkerScript.g:3555:4: () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')'
                    {
                    // InternalLinkerScript.g:3555:4: ()
                    // InternalLinkerScript.g:3556:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardAccess().getWildcardAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3562:4: ( (lv_primarySort_14_0= ruleWildcardSort ) )
                    // InternalLinkerScript.g:3563:5: (lv_primarySort_14_0= ruleWildcardSort )
                    {
                    // InternalLinkerScript.g:3563:5: (lv_primarySort_14_0= ruleWildcardSort )
                    // InternalLinkerScript.g:3564:6: lv_primarySort_14_0= ruleWildcardSort
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getPrimarySortWildcardSortEnumRuleCall_3_1_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_primarySort_14_0=ruleWildcardSort();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"primarySort",
                      							lv_primarySort_14_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardSort");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_15=(Token)match(input,13,FOLLOW_43); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getWildcardAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:3585:4: ( (lv_secondarySort_16_0= ruleWildcardSort ) )
                    // InternalLinkerScript.g:3586:5: (lv_secondarySort_16_0= ruleWildcardSort )
                    {
                    // InternalLinkerScript.g:3586:5: (lv_secondarySort_16_0= ruleWildcardSort )
                    // InternalLinkerScript.g:3587:6: lv_secondarySort_16_0= ruleWildcardSort
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getSecondarySortWildcardSortEnumRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_secondarySort_16_0=ruleWildcardSort();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"secondarySort",
                      							lv_secondarySort_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardSort");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getWildcardAccess().getLeftParenthesisKeyword_3_4());
                      			
                    }
                    // InternalLinkerScript.g:3608:4: ( (lv_name_18_0= ruleWildID ) )
                    // InternalLinkerScript.g:3609:5: (lv_name_18_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3609:5: (lv_name_18_0= ruleWildID )
                    // InternalLinkerScript.g:3610:6: lv_name_18_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getNameWildIDParserRuleCall_3_5_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_18_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_18_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_19=(Token)match(input,14,FOLLOW_7); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getWildcardAccess().getRightParenthesisKeyword_3_6());
                      			
                    }
                    otherlv_20=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getWildcardAccess().getRightParenthesisKeyword_3_7());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:3637:3: ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' )
                    {
                    // InternalLinkerScript.g:3637:3: ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' )
                    // InternalLinkerScript.g:3638:4: () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')'
                    {
                    // InternalLinkerScript.g:3638:4: ()
                    // InternalLinkerScript.g:3639:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardAccess().getWildcardAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:3645:4: ( (lv_primarySort_22_0= ruleWildcardSort ) )
                    // InternalLinkerScript.g:3646:5: (lv_primarySort_22_0= ruleWildcardSort )
                    {
                    // InternalLinkerScript.g:3646:5: (lv_primarySort_22_0= ruleWildcardSort )
                    // InternalLinkerScript.g:3647:6: lv_primarySort_22_0= ruleWildcardSort
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getPrimarySortWildcardSortEnumRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_5);
                    lv_primarySort_22_0=ruleWildcardSort();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"primarySort",
                      							lv_primarySort_22_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardSort");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_23=(Token)match(input,13,FOLLOW_44); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_23, grammarAccess.getWildcardAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    otherlv_24=(Token)match(input,75,FOLLOW_5); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_24, grammarAccess.getWildcardAccess().getEXCLUDE_FILEKeyword_4_3());
                      			
                    }
                    otherlv_25=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getWildcardAccess().getLeftParenthesisKeyword_4_4());
                      			
                    }
                    // InternalLinkerScript.g:3676:4: ( (lv_excludes_26_0= ruleWildID ) )+
                    int cnt55=0;
                    loop55:
                    do {
                        int alt55=2;
                        int LA55_0 = input.LA(1);

                        if ( (LA55_0==RULE_ID||LA55_0==76||(LA55_0>=78 && LA55_0<=79)||(LA55_0>=81 && LA55_0<=82)||LA55_0==92) ) {
                            alt55=1;
                        }


                        switch (alt55) {
                    	case 1 :
                    	    // InternalLinkerScript.g:3677:5: (lv_excludes_26_0= ruleWildID )
                    	    {
                    	    // InternalLinkerScript.g:3677:5: (lv_excludes_26_0= ruleWildID )
                    	    // InternalLinkerScript.g:3678:6: lv_excludes_26_0= ruleWildID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getWildcardAccess().getExcludesWildIDParserRuleCall_4_5_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_14);
                    	    lv_excludes_26_0=ruleWildID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"excludes",
                    	      							lv_excludes_26_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt55 >= 1 ) break loop55;
                    	    if (state.backtracking>0) {state.failed=true; return current;}
                                EarlyExitException eee =
                                    new EarlyExitException(55, input);
                                throw eee;
                        }
                        cnt55++;
                    } while (true);

                    otherlv_27=(Token)match(input,14,FOLLOW_6); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getWildcardAccess().getRightParenthesisKeyword_4_6());
                      			
                    }
                    // InternalLinkerScript.g:3699:4: ( (lv_name_28_0= ruleWildID ) )
                    // InternalLinkerScript.g:3700:5: (lv_name_28_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:3700:5: (lv_name_28_0= ruleWildID )
                    // InternalLinkerScript.g:3701:6: lv_name_28_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardAccess().getNameWildIDParserRuleCall_4_7_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_name_28_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_28_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_29=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_29, grammarAccess.getWildcardAccess().getRightParenthesisKeyword_4_8());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleWildcard"


    // $ANTLR start "entryRuleMemoryCommand"
    // InternalLinkerScript.g:3727:1: entryRuleMemoryCommand returns [EObject current=null] : iv_ruleMemoryCommand= ruleMemoryCommand EOF ;
    public final EObject entryRuleMemoryCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemoryCommand = null;


        try {
            // InternalLinkerScript.g:3727:54: (iv_ruleMemoryCommand= ruleMemoryCommand EOF )
            // InternalLinkerScript.g:3728:2: iv_ruleMemoryCommand= ruleMemoryCommand EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryCommandRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemoryCommand=ruleMemoryCommand();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemoryCommand; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemoryCommand"


    // $ANTLR start "ruleMemoryCommand"
    // InternalLinkerScript.g:3734:1: ruleMemoryCommand returns [EObject current=null] : (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' ) ;
    public final EObject ruleMemoryCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_memories_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3740:2: ( (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:3741:2: (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:3741:2: (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:3742:3: otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,76,FOLLOW_16); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getMemoryCommandAccess().getMEMORYKeyword_0());
              		
            }
            otherlv_1=(Token)match(input,34,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getMemoryCommandAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalLinkerScript.g:3750:3: ()
            // InternalLinkerScript.g:3751:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getMemoryCommandAccess().getMemoryCommandAction_2(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:3757:3: ( (lv_memories_3_0= ruleMemory ) )*
            loop57:
            do {
                int alt57=2;
                int LA57_0 = input.LA(1);

                if ( (LA57_0==RULE_ID||LA57_0==76||(LA57_0>=78 && LA57_0<=79)||(LA57_0>=81 && LA57_0<=82)) ) {
                    alt57=1;
                }


                switch (alt57) {
            	case 1 :
            	    // InternalLinkerScript.g:3758:4: (lv_memories_3_0= ruleMemory )
            	    {
            	    // InternalLinkerScript.g:3758:4: (lv_memories_3_0= ruleMemory )
            	    // InternalLinkerScript.g:3759:5: lv_memories_3_0= ruleMemory
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getMemoryCommandAccess().getMemoriesMemoryParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
            	    lv_memories_3_0=ruleMemory();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getMemoryCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"memories",
            	      						lv_memories_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.Memory");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);

            otherlv_4=(Token)match(input,35,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getMemoryCommandAccess().getRightCurlyBracketKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemoryCommand"


    // $ANTLR start "entryRuleMemory"
    // InternalLinkerScript.g:3784:1: entryRuleMemory returns [EObject current=null] : iv_ruleMemory= ruleMemory EOF ;
    public final EObject entryRuleMemory() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemory = null;


        try {
            // InternalLinkerScript.g:3784:47: (iv_ruleMemory= ruleMemory EOF )
            // InternalLinkerScript.g:3785:2: iv_ruleMemory= ruleMemory EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemory=ruleMemory();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemory; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemory"


    // $ANTLR start "ruleMemory"
    // InternalLinkerScript.g:3791:1: ruleMemory returns [EObject current=null] : ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) ) ;
    public final EObject ruleMemory() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        AntlrDatatypeRuleToken lv_name_0_0 = null;

        AntlrDatatypeRuleToken lv_attr_1_0 = null;

        EObject lv_origin_7_0 = null;

        EObject lv_length_13_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3797:2: ( ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) ) )
            // InternalLinkerScript.g:3798:2: ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) )
            {
            // InternalLinkerScript.g:3798:2: ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) )
            // InternalLinkerScript.g:3799:3: ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) )
            {
            // InternalLinkerScript.g:3799:3: ( (lv_name_0_0= ruleMemoryName ) )
            // InternalLinkerScript.g:3800:4: (lv_name_0_0= ruleMemoryName )
            {
            // InternalLinkerScript.g:3800:4: (lv_name_0_0= ruleMemoryName )
            // InternalLinkerScript.g:3801:5: lv_name_0_0= ruleMemoryName
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getNameMemoryNameParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_21);
            lv_name_0_0=ruleMemoryName();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getMemoryRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_0_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.MemoryName");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:3818:3: ( (lv_attr_1_0= ruleMemoryAttribute ) )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==13) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // InternalLinkerScript.g:3819:4: (lv_attr_1_0= ruleMemoryAttribute )
                    {
                    // InternalLinkerScript.g:3819:4: (lv_attr_1_0= ruleMemoryAttribute )
                    // InternalLinkerScript.g:3820:5: lv_attr_1_0= ruleMemoryAttribute
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getMemoryAccess().getAttrMemoryAttributeParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_23);
                    lv_attr_1_0=ruleMemoryAttribute();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getMemoryRule());
                      					}
                      					set(
                      						current,
                      						"attr",
                      						lv_attr_1_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.MemoryAttribute");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            otherlv_2=(Token)match(input,37,FOLLOW_45); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getMemoryAccess().getColonKeyword_2());
              		
            }
            // InternalLinkerScript.g:3841:3: (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' )
            int alt59=3;
            switch ( input.LA(1) ) {
            case 77:
                {
                alt59=1;
                }
                break;
            case 78:
                {
                alt59=2;
                }
                break;
            case 79:
                {
                alt59=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }

            switch (alt59) {
                case 1 :
                    // InternalLinkerScript.g:3842:4: otherlv_3= 'ORIGIN'
                    {
                    otherlv_3=(Token)match(input,77,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getMemoryAccess().getORIGINKeyword_3_0());
                      			
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3847:4: otherlv_4= 'org'
                    {
                    otherlv_4=(Token)match(input,78,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getMemoryAccess().getOrgKeyword_3_1());
                      			
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3852:4: otherlv_5= 'o'
                    {
                    otherlv_5=(Token)match(input,79,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getMemoryAccess().getOKeyword_3_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_6=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_6, grammarAccess.getMemoryAccess().getEqualsSignKeyword_4());
              		
            }
            // InternalLinkerScript.g:3861:3: ( (lv_origin_7_0= ruleLExpression ) )
            // InternalLinkerScript.g:3862:4: (lv_origin_7_0= ruleLExpression )
            {
            // InternalLinkerScript.g:3862:4: (lv_origin_7_0= ruleLExpression )
            // InternalLinkerScript.g:3863:5: lv_origin_7_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getOriginLExpressionParserRuleCall_5_0());
              				
            }
            pushFollow(FOLLOW_9);
            lv_origin_7_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getMemoryRule());
              					}
              					set(
              						current,
              						"origin",
              						lv_origin_7_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_8=(Token)match(input,11,FOLLOW_46); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_8, grammarAccess.getMemoryAccess().getCommaKeyword_6());
              		
            }
            // InternalLinkerScript.g:3884:3: (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' )
            int alt60=3;
            switch ( input.LA(1) ) {
            case 80:
                {
                alt60=1;
                }
                break;
            case 81:
                {
                alt60=2;
                }
                break;
            case 82:
                {
                alt60=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;
            }

            switch (alt60) {
                case 1 :
                    // InternalLinkerScript.g:3885:4: otherlv_9= 'LENGTH'
                    {
                    otherlv_9=(Token)match(input,80,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getMemoryAccess().getLENGTHKeyword_7_0());
                      			
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3890:4: otherlv_10= 'len'
                    {
                    otherlv_10=(Token)match(input,81,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getMemoryAccess().getLenKeyword_7_1());
                      			
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3895:4: otherlv_11= 'l'
                    {
                    otherlv_11=(Token)match(input,82,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getMemoryAccess().getLKeyword_7_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_12=(Token)match(input,41,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_12, grammarAccess.getMemoryAccess().getEqualsSignKeyword_8());
              		
            }
            // InternalLinkerScript.g:3904:3: ( (lv_length_13_0= ruleLExpression ) )
            // InternalLinkerScript.g:3905:4: (lv_length_13_0= ruleLExpression )
            {
            // InternalLinkerScript.g:3905:4: (lv_length_13_0= ruleLExpression )
            // InternalLinkerScript.g:3906:5: lv_length_13_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getLengthLExpressionParserRuleCall_9_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_length_13_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getMemoryRule());
              					}
              					set(
              						current,
              						"length",
              						lv_length_13_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemory"


    // $ANTLR start "entryRuleMemoryName"
    // InternalLinkerScript.g:3927:1: entryRuleMemoryName returns [String current=null] : iv_ruleMemoryName= ruleMemoryName EOF ;
    public final String entryRuleMemoryName() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMemoryName = null;


        try {
            // InternalLinkerScript.g:3927:50: (iv_ruleMemoryName= ruleMemoryName EOF )
            // InternalLinkerScript.g:3928:2: iv_ruleMemoryName= ruleMemoryName EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryNameRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemoryName=ruleMemoryName();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemoryName.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemoryName"


    // $ANTLR start "ruleMemoryName"
    // InternalLinkerScript.g:3934:1: ruleMemoryName returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : this_ValidID_0= ruleValidID ;
    public final AntlrDatatypeRuleToken ruleMemoryName() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        AntlrDatatypeRuleToken this_ValidID_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3940:2: (this_ValidID_0= ruleValidID )
            // InternalLinkerScript.g:3941:2: this_ValidID_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              		newCompositeNode(grammarAccess.getMemoryNameAccess().getValidIDParserRuleCall());
              	
            }
            pushFollow(FOLLOW_2);
            this_ValidID_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(this_ValidID_0);
              	
            }
            if ( state.backtracking==0 ) {

              		afterParserOrEnumRuleCall();
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemoryName"


    // $ANTLR start "entryRuleMemoryAttribute"
    // InternalLinkerScript.g:3954:1: entryRuleMemoryAttribute returns [String current=null] : iv_ruleMemoryAttribute= ruleMemoryAttribute EOF ;
    public final String entryRuleMemoryAttribute() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMemoryAttribute = null;


        try {
            // InternalLinkerScript.g:3954:55: (iv_ruleMemoryAttribute= ruleMemoryAttribute EOF )
            // InternalLinkerScript.g:3955:2: iv_ruleMemoryAttribute= ruleMemoryAttribute EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getMemoryAttributeRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleMemoryAttribute=ruleMemoryAttribute();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleMemoryAttribute.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMemoryAttribute"


    // $ANTLR start "ruleMemoryAttribute"
    // InternalLinkerScript.g:3961:1: ruleMemoryAttribute returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' ) ;
    public final AntlrDatatypeRuleToken ruleMemoryAttribute() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_WildID_2 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3967:2: ( (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' ) )
            // InternalLinkerScript.g:3968:2: (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' )
            {
            // InternalLinkerScript.g:3968:2: (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' )
            // InternalLinkerScript.g:3969:3: kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')'
            {
            kw=(Token)match(input,13,FOLLOW_47); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(kw);
              			newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getLeftParenthesisKeyword_0());
              		
            }
            // InternalLinkerScript.g:3974:3: ( (kw= '!' )? this_WildID_2= ruleWildID )+
            int cnt62=0;
            loop62:
            do {
                int alt62=2;
                int LA62_0 = input.LA(1);

                if ( (LA62_0==RULE_ID||LA62_0==76||(LA62_0>=78 && LA62_0<=79)||(LA62_0>=81 && LA62_0<=83)||LA62_0==92) ) {
                    alt62=1;
                }


                switch (alt62) {
            	case 1 :
            	    // InternalLinkerScript.g:3975:4: (kw= '!' )? this_WildID_2= ruleWildID
            	    {
            	    // InternalLinkerScript.g:3975:4: (kw= '!' )?
            	    int alt61=2;
            	    int LA61_0 = input.LA(1);

            	    if ( (LA61_0==83) ) {
            	        alt61=1;
            	    }
            	    switch (alt61) {
            	        case 1 :
            	            // InternalLinkerScript.g:3976:5: kw= '!'
            	            {
            	            kw=(Token)match(input,83,FOLLOW_6); if (state.failed) return current;
            	            if ( state.backtracking==0 ) {

            	              					current.merge(kw);
            	              					newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getExclamationMarkKeyword_1_0());
            	              				
            	            }

            	            }
            	            break;

            	    }

            	    if ( state.backtracking==0 ) {

            	      				newCompositeNode(grammarAccess.getMemoryAttributeAccess().getWildIDParserRuleCall_1_1());
            	      			
            	    }
            	    pushFollow(FOLLOW_48);
            	    this_WildID_2=ruleWildID();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				current.merge(this_WildID_2);
            	      			
            	    }
            	    if ( state.backtracking==0 ) {

            	      				afterParserOrEnumRuleCall();
            	      			
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt62 >= 1 ) break loop62;
            	    if (state.backtracking>0) {state.failed=true; return current;}
                        EarlyExitException eee =
                            new EarlyExitException(62, input);
                        throw eee;
                }
                cnt62++;
            } while (true);

            kw=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(kw);
              			newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getRightParenthesisKeyword_2());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMemoryAttribute"


    // $ANTLR start "entryRuleLExpression"
    // InternalLinkerScript.g:4002:1: entryRuleLExpression returns [EObject current=null] : iv_ruleLExpression= ruleLExpression EOF ;
    public final EObject entryRuleLExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLExpression = null;


        try {
            // InternalLinkerScript.g:4002:52: (iv_ruleLExpression= ruleLExpression EOF )
            // InternalLinkerScript.g:4003:2: iv_ruleLExpression= ruleLExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLExpression=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLExpression"


    // $ANTLR start "ruleLExpression"
    // InternalLinkerScript.g:4009:1: ruleLExpression returns [EObject current=null] : this_LTernary_0= ruleLTernary ;
    public final EObject ruleLExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LTernary_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4015:2: (this_LTernary_0= ruleLTernary )
            // InternalLinkerScript.g:4016:2: this_LTernary_0= ruleLTernary
            {
            if ( state.backtracking==0 ) {

              		newCompositeNode(grammarAccess.getLExpressionAccess().getLTernaryParserRuleCall());
              	
            }
            pushFollow(FOLLOW_2);
            this_LTernary_0=ruleLTernary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current = this_LTernary_0;
              		afterParserOrEnumRuleCall();
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLExpression"


    // $ANTLR start "entryRuleLTernary"
    // InternalLinkerScript.g:4027:1: entryRuleLTernary returns [EObject current=null] : iv_ruleLTernary= ruleLTernary EOF ;
    public final EObject entryRuleLTernary() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLTernary = null;


        try {
            // InternalLinkerScript.g:4027:49: (iv_ruleLTernary= ruleLTernary EOF )
            // InternalLinkerScript.g:4028:2: iv_ruleLTernary= ruleLTernary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLTernaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLTernary=ruleLTernary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLTernary; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLTernary"


    // $ANTLR start "ruleLTernary"
    // InternalLinkerScript.g:4034:1: ruleLTernary returns [EObject current=null] : (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? ) ;
    public final EObject ruleLTernary() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject this_LOrExpression_0 = null;

        EObject lv_ifPart_3_0 = null;

        EObject lv_thenPart_5_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4040:2: ( (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? ) )
            // InternalLinkerScript.g:4041:2: (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? )
            {
            // InternalLinkerScript.g:4041:2: (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )? )
            // InternalLinkerScript.g:4042:3: this_LOrExpression_0= ruleLOrExpression ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLTernaryAccess().getLOrExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_49);
            this_LOrExpression_0=ruleLOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LOrExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4050:3: ( ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) ) )?
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==84) && (synpred1_InternalLinkerScript())) {
                alt63=1;
            }
            switch (alt63) {
                case 1 :
                    // InternalLinkerScript.g:4051:4: ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) ) ( (lv_thenPart_5_0= ruleLOrExpression ) )
                    {
                    // InternalLinkerScript.g:4051:4: ( ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' ) )
                    // InternalLinkerScript.g:4052:5: ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )=> ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' )
                    {
                    // InternalLinkerScript.g:4064:5: ( () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':' )
                    // InternalLinkerScript.g:4065:6: () otherlv_2= '?' ( (lv_ifPart_3_0= ruleLOrExpression ) ) otherlv_4= ':'
                    {
                    // InternalLinkerScript.g:4065:6: ()
                    // InternalLinkerScript.g:4066:7: 
                    {
                    if ( state.backtracking==0 ) {

                      							current = forceCreateModelElementAndSet(
                      								grammarAccess.getLTernaryAccess().getLTernaryOperationConditionAction_1_0_0_0(),
                      								current);
                      						
                    }

                    }

                    otherlv_2=(Token)match(input,84,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(otherlv_2, grammarAccess.getLTernaryAccess().getQuestionMarkKeyword_1_0_0_1());
                      					
                    }
                    // InternalLinkerScript.g:4076:6: ( (lv_ifPart_3_0= ruleLOrExpression ) )
                    // InternalLinkerScript.g:4077:7: (lv_ifPart_3_0= ruleLOrExpression )
                    {
                    // InternalLinkerScript.g:4077:7: (lv_ifPart_3_0= ruleLOrExpression )
                    // InternalLinkerScript.g:4078:8: lv_ifPart_3_0= ruleLOrExpression
                    {
                    if ( state.backtracking==0 ) {

                      								newCompositeNode(grammarAccess.getLTernaryAccess().getIfPartLOrExpressionParserRuleCall_1_0_0_2_0());
                      							
                    }
                    pushFollow(FOLLOW_23);
                    lv_ifPart_3_0=ruleLOrExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      								if (current==null) {
                      									current = createModelElementForParent(grammarAccess.getLTernaryRule());
                      								}
                      								set(
                      									current,
                      									"ifPart",
                      									lv_ifPart_3_0,
                      									"org.eclipse.cdt.linkerscript.LinkerScript.LOrExpression");
                      								afterParserOrEnumRuleCall();
                      							
                    }

                    }


                    }

                    otherlv_4=(Token)match(input,37,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						newLeafNode(otherlv_4, grammarAccess.getLTernaryAccess().getColonKeyword_1_0_0_3());
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:4101:4: ( (lv_thenPart_5_0= ruleLOrExpression ) )
                    // InternalLinkerScript.g:4102:5: (lv_thenPart_5_0= ruleLOrExpression )
                    {
                    // InternalLinkerScript.g:4102:5: (lv_thenPart_5_0= ruleLOrExpression )
                    // InternalLinkerScript.g:4103:6: lv_thenPart_5_0= ruleLOrExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLTernaryAccess().getThenPartLOrExpressionParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_thenPart_5_0=ruleLOrExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLTernaryRule());
                      						}
                      						set(
                      							current,
                      							"thenPart",
                      							lv_thenPart_5_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LOrExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLTernary"


    // $ANTLR start "entryRuleLOrExpression"
    // InternalLinkerScript.g:4125:1: entryRuleLOrExpression returns [EObject current=null] : iv_ruleLOrExpression= ruleLOrExpression EOF ;
    public final EObject entryRuleLOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLOrExpression = null;


        try {
            // InternalLinkerScript.g:4125:54: (iv_ruleLOrExpression= ruleLOrExpression EOF )
            // InternalLinkerScript.g:4126:2: iv_ruleLOrExpression= ruleLOrExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLOrExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLOrExpression=ruleLOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLOrExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLOrExpression"


    // $ANTLR start "ruleLOrExpression"
    // InternalLinkerScript.g:4132:1: ruleLOrExpression returns [EObject current=null] : (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* ) ;
    public final EObject ruleLOrExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAndExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4138:2: ( (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* ) )
            // InternalLinkerScript.g:4139:2: (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* )
            {
            // InternalLinkerScript.g:4139:2: (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* )
            // InternalLinkerScript.g:4140:3: this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLOrExpressionAccess().getLAndExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_50);
            this_LAndExpression_0=ruleLAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LAndExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4148:3: ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )*
            loop64:
            do {
                int alt64=2;
                int LA64_0 = input.LA(1);

                if ( (LA64_0==85) && (synpred2_InternalLinkerScript())) {
                    alt64=1;
                }


                switch (alt64) {
            	case 1 :
            	    // InternalLinkerScript.g:4149:4: ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) )
            	    {
            	    // InternalLinkerScript.g:4149:4: ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) )
            	    // InternalLinkerScript.g:4150:5: ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) )
            	    {
            	    // InternalLinkerScript.g:4160:5: ( () ( (lv_feature_2_0= ruleOpOr ) ) )
            	    // InternalLinkerScript.g:4161:6: () ( (lv_feature_2_0= ruleOpOr ) )
            	    {
            	    // InternalLinkerScript.g:4161:6: ()
            	    // InternalLinkerScript.g:4162:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLOrExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4168:6: ( (lv_feature_2_0= ruleOpOr ) )
            	    // InternalLinkerScript.g:4169:7: (lv_feature_2_0= ruleOpOr )
            	    {
            	    // InternalLinkerScript.g:4169:7: (lv_feature_2_0= ruleOpOr )
            	    // InternalLinkerScript.g:4170:8: lv_feature_2_0= ruleOpOr
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLOrExpressionAccess().getFeatureOpOrParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpOr();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLOrExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpOr");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4189:4: ( (lv_rightOperand_3_0= ruleLAndExpression ) )
            	    // InternalLinkerScript.g:4190:5: (lv_rightOperand_3_0= ruleLAndExpression )
            	    {
            	    // InternalLinkerScript.g:4190:5: (lv_rightOperand_3_0= ruleLAndExpression )
            	    // InternalLinkerScript.g:4191:6: lv_rightOperand_3_0= ruleLAndExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLOrExpressionAccess().getRightOperandLAndExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_50);
            	    lv_rightOperand_3_0=ruleLAndExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLOrExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LAndExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop64;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLOrExpression"


    // $ANTLR start "entryRuleOpOr"
    // InternalLinkerScript.g:4213:1: entryRuleOpOr returns [String current=null] : iv_ruleOpOr= ruleOpOr EOF ;
    public final String entryRuleOpOr() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpOr = null;


        try {
            // InternalLinkerScript.g:4213:44: (iv_ruleOpOr= ruleOpOr EOF )
            // InternalLinkerScript.g:4214:2: iv_ruleOpOr= ruleOpOr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpOrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpOr=ruleOpOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpOr.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpOr"


    // $ANTLR start "ruleOpOr"
    // InternalLinkerScript.g:4220:1: ruleOpOr returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '||' ;
    public final AntlrDatatypeRuleToken ruleOpOr() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4226:2: (kw= '||' )
            // InternalLinkerScript.g:4227:2: kw= '||'
            {
            kw=(Token)match(input,85,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpOrAccess().getVerticalLineVerticalLineKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpOr"


    // $ANTLR start "entryRuleLAndExpression"
    // InternalLinkerScript.g:4235:1: entryRuleLAndExpression returns [EObject current=null] : iv_ruleLAndExpression= ruleLAndExpression EOF ;
    public final EObject entryRuleLAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAndExpression = null;


        try {
            // InternalLinkerScript.g:4235:55: (iv_ruleLAndExpression= ruleLAndExpression EOF )
            // InternalLinkerScript.g:4236:2: iv_ruleLAndExpression= ruleLAndExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLAndExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLAndExpression=ruleLAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLAndExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLAndExpression"


    // $ANTLR start "ruleLAndExpression"
    // InternalLinkerScript.g:4242:1: ruleLAndExpression returns [EObject current=null] : (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* ) ;
    public final EObject ruleLAndExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LBitwiseOrExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4248:2: ( (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* ) )
            // InternalLinkerScript.g:4249:2: (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* )
            {
            // InternalLinkerScript.g:4249:2: (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* )
            // InternalLinkerScript.g:4250:3: this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAndExpressionAccess().getLBitwiseOrExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_LBitwiseOrExpression_0=ruleLBitwiseOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LBitwiseOrExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4258:3: ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )*
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);

                if ( (LA65_0==86) && (synpred3_InternalLinkerScript())) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // InternalLinkerScript.g:4259:4: ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) )
            	    {
            	    // InternalLinkerScript.g:4259:4: ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) )
            	    // InternalLinkerScript.g:4260:5: ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) )
            	    {
            	    // InternalLinkerScript.g:4270:5: ( () ( (lv_feature_2_0= ruleOpAnd ) ) )
            	    // InternalLinkerScript.g:4271:6: () ( (lv_feature_2_0= ruleOpAnd ) )
            	    {
            	    // InternalLinkerScript.g:4271:6: ()
            	    // InternalLinkerScript.g:4272:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLAndExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4278:6: ( (lv_feature_2_0= ruleOpAnd ) )
            	    // InternalLinkerScript.g:4279:7: (lv_feature_2_0= ruleOpAnd )
            	    {
            	    // InternalLinkerScript.g:4279:7: (lv_feature_2_0= ruleOpAnd )
            	    // InternalLinkerScript.g:4280:8: lv_feature_2_0= ruleOpAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLAndExpressionAccess().getFeatureOpAndParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpAnd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLAndExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpAnd");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4299:4: ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) )
            	    // InternalLinkerScript.g:4300:5: (lv_rightOperand_3_0= ruleLBitwiseOrExpression )
            	    {
            	    // InternalLinkerScript.g:4300:5: (lv_rightOperand_3_0= ruleLBitwiseOrExpression )
            	    // InternalLinkerScript.g:4301:6: lv_rightOperand_3_0= ruleLBitwiseOrExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLAndExpressionAccess().getRightOperandLBitwiseOrExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_51);
            	    lv_rightOperand_3_0=ruleLBitwiseOrExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLAndExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LBitwiseOrExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLAndExpression"


    // $ANTLR start "entryRuleOpAnd"
    // InternalLinkerScript.g:4323:1: entryRuleOpAnd returns [String current=null] : iv_ruleOpAnd= ruleOpAnd EOF ;
    public final String entryRuleOpAnd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAnd = null;


        try {
            // InternalLinkerScript.g:4323:45: (iv_ruleOpAnd= ruleOpAnd EOF )
            // InternalLinkerScript.g:4324:2: iv_ruleOpAnd= ruleOpAnd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpAndRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpAnd=ruleOpAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpAnd.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAnd"


    // $ANTLR start "ruleOpAnd"
    // InternalLinkerScript.g:4330:1: ruleOpAnd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '&&' ;
    public final AntlrDatatypeRuleToken ruleOpAnd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4336:2: (kw= '&&' )
            // InternalLinkerScript.g:4337:2: kw= '&&'
            {
            kw=(Token)match(input,86,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpAndAccess().getAmpersandAmpersandKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAnd"


    // $ANTLR start "entryRuleLBitwiseOrExpression"
    // InternalLinkerScript.g:4345:1: entryRuleLBitwiseOrExpression returns [EObject current=null] : iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF ;
    public final EObject entryRuleLBitwiseOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLBitwiseOrExpression = null;


        try {
            // InternalLinkerScript.g:4345:61: (iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF )
            // InternalLinkerScript.g:4346:2: iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLBitwiseOrExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLBitwiseOrExpression=ruleLBitwiseOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLBitwiseOrExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLBitwiseOrExpression"


    // $ANTLR start "ruleLBitwiseOrExpression"
    // InternalLinkerScript.g:4352:1: ruleLBitwiseOrExpression returns [EObject current=null] : (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* ) ;
    public final EObject ruleLBitwiseOrExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LBitwiseAndExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4358:2: ( (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* ) )
            // InternalLinkerScript.g:4359:2: (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* )
            {
            // InternalLinkerScript.g:4359:2: (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* )
            // InternalLinkerScript.g:4360:3: this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getLBitwiseAndExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_52);
            this_LBitwiseAndExpression_0=ruleLBitwiseAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LBitwiseAndExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4368:3: ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )*
            loop66:
            do {
                int alt66=2;
                int LA66_0 = input.LA(1);

                if ( (LA66_0==87) && (synpred4_InternalLinkerScript())) {
                    alt66=1;
                }


                switch (alt66) {
            	case 1 :
            	    // InternalLinkerScript.g:4369:4: ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) )
            	    {
            	    // InternalLinkerScript.g:4369:4: ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) )
            	    // InternalLinkerScript.g:4370:5: ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) )
            	    {
            	    // InternalLinkerScript.g:4380:5: ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) )
            	    // InternalLinkerScript.g:4381:6: () ( (lv_feature_2_0= ruleOpBitwiseOr ) )
            	    {
            	    // InternalLinkerScript.g:4381:6: ()
            	    // InternalLinkerScript.g:4382:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLBitwiseOrExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4388:6: ( (lv_feature_2_0= ruleOpBitwiseOr ) )
            	    // InternalLinkerScript.g:4389:7: (lv_feature_2_0= ruleOpBitwiseOr )
            	    {
            	    // InternalLinkerScript.g:4389:7: (lv_feature_2_0= ruleOpBitwiseOr )
            	    // InternalLinkerScript.g:4390:8: lv_feature_2_0= ruleOpBitwiseOr
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getFeatureOpBitwiseOrParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpBitwiseOr();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLBitwiseOrExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpBitwiseOr");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4409:4: ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) )
            	    // InternalLinkerScript.g:4410:5: (lv_rightOperand_3_0= ruleLBitwiseAndExpression )
            	    {
            	    // InternalLinkerScript.g:4410:5: (lv_rightOperand_3_0= ruleLBitwiseAndExpression )
            	    // InternalLinkerScript.g:4411:6: lv_rightOperand_3_0= ruleLBitwiseAndExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getRightOperandLBitwiseAndExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_52);
            	    lv_rightOperand_3_0=ruleLBitwiseAndExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLBitwiseOrExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LBitwiseAndExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLBitwiseOrExpression"


    // $ANTLR start "entryRuleOpBitwiseOr"
    // InternalLinkerScript.g:4433:1: entryRuleOpBitwiseOr returns [String current=null] : iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF ;
    public final String entryRuleOpBitwiseOr() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpBitwiseOr = null;


        try {
            // InternalLinkerScript.g:4433:51: (iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF )
            // InternalLinkerScript.g:4434:2: iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpBitwiseOrRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpBitwiseOr=ruleOpBitwiseOr();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpBitwiseOr.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpBitwiseOr"


    // $ANTLR start "ruleOpBitwiseOr"
    // InternalLinkerScript.g:4440:1: ruleOpBitwiseOr returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '|' ;
    public final AntlrDatatypeRuleToken ruleOpBitwiseOr() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4446:2: (kw= '|' )
            // InternalLinkerScript.g:4447:2: kw= '|'
            {
            kw=(Token)match(input,87,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpBitwiseOrAccess().getVerticalLineKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpBitwiseOr"


    // $ANTLR start "entryRuleLBitwiseAndExpression"
    // InternalLinkerScript.g:4455:1: entryRuleLBitwiseAndExpression returns [EObject current=null] : iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF ;
    public final EObject entryRuleLBitwiseAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLBitwiseAndExpression = null;


        try {
            // InternalLinkerScript.g:4455:62: (iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF )
            // InternalLinkerScript.g:4456:2: iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLBitwiseAndExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLBitwiseAndExpression=ruleLBitwiseAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLBitwiseAndExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLBitwiseAndExpression"


    // $ANTLR start "ruleLBitwiseAndExpression"
    // InternalLinkerScript.g:4462:1: ruleLBitwiseAndExpression returns [EObject current=null] : (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* ) ;
    public final EObject ruleLBitwiseAndExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LEqualityExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4468:2: ( (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* ) )
            // InternalLinkerScript.g:4469:2: (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* )
            {
            // InternalLinkerScript.g:4469:2: (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* )
            // InternalLinkerScript.g:4470:3: this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getLEqualityExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_53);
            this_LEqualityExpression_0=ruleLEqualityExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LEqualityExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4478:3: ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )*
            loop67:
            do {
                int alt67=2;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==73) && (synpred5_InternalLinkerScript())) {
                    alt67=1;
                }


                switch (alt67) {
            	case 1 :
            	    // InternalLinkerScript.g:4479:4: ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) )
            	    {
            	    // InternalLinkerScript.g:4479:4: ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) )
            	    // InternalLinkerScript.g:4480:5: ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) )
            	    {
            	    // InternalLinkerScript.g:4490:5: ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) )
            	    // InternalLinkerScript.g:4491:6: () ( (lv_feature_2_0= ruleOpBitwiseAnd ) )
            	    {
            	    // InternalLinkerScript.g:4491:6: ()
            	    // InternalLinkerScript.g:4492:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLBitwiseAndExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4498:6: ( (lv_feature_2_0= ruleOpBitwiseAnd ) )
            	    // InternalLinkerScript.g:4499:7: (lv_feature_2_0= ruleOpBitwiseAnd )
            	    {
            	    // InternalLinkerScript.g:4499:7: (lv_feature_2_0= ruleOpBitwiseAnd )
            	    // InternalLinkerScript.g:4500:8: lv_feature_2_0= ruleOpBitwiseAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getFeatureOpBitwiseAndParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpBitwiseAnd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLBitwiseAndExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpBitwiseAnd");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4519:4: ( (lv_rightOperand_3_0= ruleLEqualityExpression ) )
            	    // InternalLinkerScript.g:4520:5: (lv_rightOperand_3_0= ruleLEqualityExpression )
            	    {
            	    // InternalLinkerScript.g:4520:5: (lv_rightOperand_3_0= ruleLEqualityExpression )
            	    // InternalLinkerScript.g:4521:6: lv_rightOperand_3_0= ruleLEqualityExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getRightOperandLEqualityExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_53);
            	    lv_rightOperand_3_0=ruleLEqualityExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLBitwiseAndExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LEqualityExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLBitwiseAndExpression"


    // $ANTLR start "entryRuleOpBitwiseAnd"
    // InternalLinkerScript.g:4543:1: entryRuleOpBitwiseAnd returns [String current=null] : iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF ;
    public final String entryRuleOpBitwiseAnd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpBitwiseAnd = null;


        try {
            // InternalLinkerScript.g:4543:52: (iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF )
            // InternalLinkerScript.g:4544:2: iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpBitwiseAndRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpBitwiseAnd=ruleOpBitwiseAnd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpBitwiseAnd.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpBitwiseAnd"


    // $ANTLR start "ruleOpBitwiseAnd"
    // InternalLinkerScript.g:4550:1: ruleOpBitwiseAnd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '&' ;
    public final AntlrDatatypeRuleToken ruleOpBitwiseAnd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4556:2: (kw= '&' )
            // InternalLinkerScript.g:4557:2: kw= '&'
            {
            kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current.merge(kw);
              		newLeafNode(kw, grammarAccess.getOpBitwiseAndAccess().getAmpersandKeyword());
              	
            }

            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpBitwiseAnd"


    // $ANTLR start "entryRuleLEqualityExpression"
    // InternalLinkerScript.g:4565:1: entryRuleLEqualityExpression returns [EObject current=null] : iv_ruleLEqualityExpression= ruleLEqualityExpression EOF ;
    public final EObject entryRuleLEqualityExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLEqualityExpression = null;


        try {
            // InternalLinkerScript.g:4565:60: (iv_ruleLEqualityExpression= ruleLEqualityExpression EOF )
            // InternalLinkerScript.g:4566:2: iv_ruleLEqualityExpression= ruleLEqualityExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLEqualityExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLEqualityExpression=ruleLEqualityExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLEqualityExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLEqualityExpression"


    // $ANTLR start "ruleLEqualityExpression"
    // InternalLinkerScript.g:4572:1: ruleLEqualityExpression returns [EObject current=null] : (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* ) ;
    public final EObject ruleLEqualityExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LRelationalExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4578:2: ( (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* ) )
            // InternalLinkerScript.g:4579:2: (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* )
            {
            // InternalLinkerScript.g:4579:2: (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* )
            // InternalLinkerScript.g:4580:3: this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getLRelationalExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_54);
            this_LRelationalExpression_0=ruleLRelationalExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LRelationalExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4588:3: ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )*
            loop68:
            do {
                int alt68=2;
                int LA68_0 = input.LA(1);

                if ( (LA68_0==88) && (synpred6_InternalLinkerScript())) {
                    alt68=1;
                }
                else if ( (LA68_0==89) && (synpred6_InternalLinkerScript())) {
                    alt68=1;
                }


                switch (alt68) {
            	case 1 :
            	    // InternalLinkerScript.g:4589:4: ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) )
            	    {
            	    // InternalLinkerScript.g:4589:4: ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) )
            	    // InternalLinkerScript.g:4590:5: ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) )
            	    {
            	    // InternalLinkerScript.g:4600:5: ( () ( (lv_feature_2_0= ruleOpEquality ) ) )
            	    // InternalLinkerScript.g:4601:6: () ( (lv_feature_2_0= ruleOpEquality ) )
            	    {
            	    // InternalLinkerScript.g:4601:6: ()
            	    // InternalLinkerScript.g:4602:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLEqualityExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4608:6: ( (lv_feature_2_0= ruleOpEquality ) )
            	    // InternalLinkerScript.g:4609:7: (lv_feature_2_0= ruleOpEquality )
            	    {
            	    // InternalLinkerScript.g:4609:7: (lv_feature_2_0= ruleOpEquality )
            	    // InternalLinkerScript.g:4610:8: lv_feature_2_0= ruleOpEquality
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getFeatureOpEqualityParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpEquality();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLEqualityExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpEquality");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4629:4: ( (lv_rightOperand_3_0= ruleLRelationalExpression ) )
            	    // InternalLinkerScript.g:4630:5: (lv_rightOperand_3_0= ruleLRelationalExpression )
            	    {
            	    // InternalLinkerScript.g:4630:5: (lv_rightOperand_3_0= ruleLRelationalExpression )
            	    // InternalLinkerScript.g:4631:6: lv_rightOperand_3_0= ruleLRelationalExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getRightOperandLRelationalExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_54);
            	    lv_rightOperand_3_0=ruleLRelationalExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLEqualityExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LRelationalExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLEqualityExpression"


    // $ANTLR start "entryRuleOpEquality"
    // InternalLinkerScript.g:4653:1: entryRuleOpEquality returns [String current=null] : iv_ruleOpEquality= ruleOpEquality EOF ;
    public final String entryRuleOpEquality() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpEquality = null;


        try {
            // InternalLinkerScript.g:4653:50: (iv_ruleOpEquality= ruleOpEquality EOF )
            // InternalLinkerScript.g:4654:2: iv_ruleOpEquality= ruleOpEquality EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpEqualityRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpEquality=ruleOpEquality();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpEquality.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpEquality"


    // $ANTLR start "ruleOpEquality"
    // InternalLinkerScript.g:4660:1: ruleOpEquality returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '==' | kw= '!=' ) ;
    public final AntlrDatatypeRuleToken ruleOpEquality() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4666:2: ( (kw= '==' | kw= '!=' ) )
            // InternalLinkerScript.g:4667:2: (kw= '==' | kw= '!=' )
            {
            // InternalLinkerScript.g:4667:2: (kw= '==' | kw= '!=' )
            int alt69=2;
            int LA69_0 = input.LA(1);

            if ( (LA69_0==88) ) {
                alt69=1;
            }
            else if ( (LA69_0==89) ) {
                alt69=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 69, 0, input);

                throw nvae;
            }
            switch (alt69) {
                case 1 :
                    // InternalLinkerScript.g:4668:3: kw= '=='
                    {
                    kw=(Token)match(input,88,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpEqualityAccess().getEqualsSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4674:3: kw= '!='
                    {
                    kw=(Token)match(input,89,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpEqualityAccess().getExclamationMarkEqualsSignKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpEquality"


    // $ANTLR start "entryRuleLRelationalExpression"
    // InternalLinkerScript.g:4683:1: entryRuleLRelationalExpression returns [EObject current=null] : iv_ruleLRelationalExpression= ruleLRelationalExpression EOF ;
    public final EObject entryRuleLRelationalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLRelationalExpression = null;


        try {
            // InternalLinkerScript.g:4683:62: (iv_ruleLRelationalExpression= ruleLRelationalExpression EOF )
            // InternalLinkerScript.g:4684:2: iv_ruleLRelationalExpression= ruleLRelationalExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLRelationalExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLRelationalExpression=ruleLRelationalExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLRelationalExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLRelationalExpression"


    // $ANTLR start "ruleLRelationalExpression"
    // InternalLinkerScript.g:4690:1: ruleLRelationalExpression returns [EObject current=null] : (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* ) ;
    public final EObject ruleLRelationalExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LOtherOperatorExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4696:2: ( (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* ) )
            // InternalLinkerScript.g:4697:2: (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* )
            {
            // InternalLinkerScript.g:4697:2: (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* )
            // InternalLinkerScript.g:4698:3: this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getLOtherOperatorExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_55);
            this_LOtherOperatorExpression_0=ruleLOtherOperatorExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LOtherOperatorExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4706:3: ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*
            loop70:
            do {
                int alt70=2;
                int LA70_0 = input.LA(1);

                if ( (LA70_0==69) && (synpred7_InternalLinkerScript())) {
                    alt70=1;
                }
                else if ( (LA70_0==68) && (synpred7_InternalLinkerScript())) {
                    alt70=1;
                }
                else if ( (LA70_0==40) && (synpred7_InternalLinkerScript())) {
                    alt70=1;
                }


                switch (alt70) {
            	case 1 :
            	    // InternalLinkerScript.g:4707:4: ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) )
            	    {
            	    // InternalLinkerScript.g:4707:4: ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) )
            	    // InternalLinkerScript.g:4708:5: ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) )
            	    {
            	    // InternalLinkerScript.g:4718:5: ( () ( (lv_feature_2_0= ruleOpCompare ) ) )
            	    // InternalLinkerScript.g:4719:6: () ( (lv_feature_2_0= ruleOpCompare ) )
            	    {
            	    // InternalLinkerScript.g:4719:6: ()
            	    // InternalLinkerScript.g:4720:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLRelationalExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4726:6: ( (lv_feature_2_0= ruleOpCompare ) )
            	    // InternalLinkerScript.g:4727:7: (lv_feature_2_0= ruleOpCompare )
            	    {
            	    // InternalLinkerScript.g:4727:7: (lv_feature_2_0= ruleOpCompare )
            	    // InternalLinkerScript.g:4728:8: lv_feature_2_0= ruleOpCompare
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getFeatureOpCompareParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpCompare();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLRelationalExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpCompare");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4747:4: ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) )
            	    // InternalLinkerScript.g:4748:5: (lv_rightOperand_3_0= ruleLOtherOperatorExpression )
            	    {
            	    // InternalLinkerScript.g:4748:5: (lv_rightOperand_3_0= ruleLOtherOperatorExpression )
            	    // InternalLinkerScript.g:4749:6: lv_rightOperand_3_0= ruleLOtherOperatorExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getRightOperandLOtherOperatorExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_55);
            	    lv_rightOperand_3_0=ruleLOtherOperatorExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLRelationalExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LOtherOperatorExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop70;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLRelationalExpression"


    // $ANTLR start "entryRuleOpCompare"
    // InternalLinkerScript.g:4771:1: entryRuleOpCompare returns [String current=null] : iv_ruleOpCompare= ruleOpCompare EOF ;
    public final String entryRuleOpCompare() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpCompare = null;


        try {
            // InternalLinkerScript.g:4771:49: (iv_ruleOpCompare= ruleOpCompare EOF )
            // InternalLinkerScript.g:4772:2: iv_ruleOpCompare= ruleOpCompare EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpCompareRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpCompare=ruleOpCompare();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpCompare.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpCompare"


    // $ANTLR start "ruleOpCompare"
    // InternalLinkerScript.g:4778:1: ruleOpCompare returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' ) ;
    public final AntlrDatatypeRuleToken ruleOpCompare() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4784:2: ( (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' ) )
            // InternalLinkerScript.g:4785:2: (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' )
            {
            // InternalLinkerScript.g:4785:2: (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' )
            int alt71=4;
            switch ( input.LA(1) ) {
            case 69:
                {
                alt71=1;
                }
                break;
            case 68:
                {
                int LA71_2 = input.LA(2);

                if ( (LA71_2==EOF||(LA71_2>=RULE_ID && LA71_2<=RULE_HEX)||LA71_2==13||LA71_2==38||LA71_2==42||(LA71_2>=76 && LA71_2<=83)||(LA71_2>=90 && LA71_2<=91)||(LA71_2>=95 && LA71_2<=96)) ) {
                    alt71=4;
                }
                else if ( (LA71_2==41) ) {
                    alt71=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 71, 2, input);

                    throw nvae;
                }
                }
                break;
            case 40:
                {
                alt71=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 71, 0, input);

                throw nvae;
            }

            switch (alt71) {
                case 1 :
                    // InternalLinkerScript.g:4786:3: kw= '>='
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getGreaterThanSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4792:3: (kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:4792:3: (kw= '<' kw= '=' )
                    // InternalLinkerScript.g:4793:4: kw= '<' kw= '='
                    {
                    kw=(Token)match(input,68,FOLLOW_36); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpCompareAccess().getLessThanSignKeyword_1_0());
                      			
                    }
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpCompareAccess().getEqualsSignKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4805:3: kw= '>'
                    {
                    kw=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getGreaterThanSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:4811:3: kw= '<'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getLessThanSignKeyword_3());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpCompare"


    // $ANTLR start "entryRuleLOtherOperatorExpression"
    // InternalLinkerScript.g:4820:1: entryRuleLOtherOperatorExpression returns [EObject current=null] : iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF ;
    public final EObject entryRuleLOtherOperatorExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLOtherOperatorExpression = null;


        try {
            // InternalLinkerScript.g:4820:65: (iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF )
            // InternalLinkerScript.g:4821:2: iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLOtherOperatorExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLOtherOperatorExpression=ruleLOtherOperatorExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLOtherOperatorExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLOtherOperatorExpression"


    // $ANTLR start "ruleLOtherOperatorExpression"
    // InternalLinkerScript.g:4827:1: ruleLOtherOperatorExpression returns [EObject current=null] : (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* ) ;
    public final EObject ruleLOtherOperatorExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAdditiveExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4833:2: ( (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* ) )
            // InternalLinkerScript.g:4834:2: (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* )
            {
            // InternalLinkerScript.g:4834:2: (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* )
            // InternalLinkerScript.g:4835:3: this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getLAdditiveExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_56);
            this_LAdditiveExpression_0=ruleLAdditiveExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LAdditiveExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4843:3: ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*
            loop72:
            do {
                int alt72=2;
                int LA72_0 = input.LA(1);

                if ( (LA72_0==68) ) {
                    int LA72_2 = input.LA(2);

                    if ( (LA72_2==68) && (synpred8_InternalLinkerScript())) {
                        alt72=1;
                    }


                }
                else if ( (LA72_0==40) ) {
                    int LA72_3 = input.LA(2);

                    if ( (LA72_3==40) && (synpred8_InternalLinkerScript())) {
                        alt72=1;
                    }


                }


                switch (alt72) {
            	case 1 :
            	    // InternalLinkerScript.g:4844:4: ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) )
            	    {
            	    // InternalLinkerScript.g:4844:4: ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) )
            	    // InternalLinkerScript.g:4845:5: ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) )
            	    {
            	    // InternalLinkerScript.g:4855:5: ( () ( (lv_feature_2_0= ruleOpOther ) ) )
            	    // InternalLinkerScript.g:4856:6: () ( (lv_feature_2_0= ruleOpOther ) )
            	    {
            	    // InternalLinkerScript.g:4856:6: ()
            	    // InternalLinkerScript.g:4857:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLOtherOperatorExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4863:6: ( (lv_feature_2_0= ruleOpOther ) )
            	    // InternalLinkerScript.g:4864:7: (lv_feature_2_0= ruleOpOther )
            	    {
            	    // InternalLinkerScript.g:4864:7: (lv_feature_2_0= ruleOpOther )
            	    // InternalLinkerScript.g:4865:8: lv_feature_2_0= ruleOpOther
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getFeatureOpOtherParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpOther();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLOtherOperatorExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpOther");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:4884:4: ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) )
            	    // InternalLinkerScript.g:4885:5: (lv_rightOperand_3_0= ruleLAdditiveExpression )
            	    {
            	    // InternalLinkerScript.g:4885:5: (lv_rightOperand_3_0= ruleLAdditiveExpression )
            	    // InternalLinkerScript.g:4886:6: lv_rightOperand_3_0= ruleLAdditiveExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getRightOperandLAdditiveExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_56);
            	    lv_rightOperand_3_0=ruleLAdditiveExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLOtherOperatorExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LAdditiveExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLOtherOperatorExpression"


    // $ANTLR start "entryRuleOpOther"
    // InternalLinkerScript.g:4908:1: entryRuleOpOther returns [String current=null] : iv_ruleOpOther= ruleOpOther EOF ;
    public final String entryRuleOpOther() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpOther = null;


        try {
            // InternalLinkerScript.g:4908:47: (iv_ruleOpOther= ruleOpOther EOF )
            // InternalLinkerScript.g:4909:2: iv_ruleOpOther= ruleOpOther EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpOtherRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpOther=ruleOpOther();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpOther.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpOther"


    // $ANTLR start "ruleOpOther"
    // InternalLinkerScript.g:4915:1: ruleOpOther returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) ) ;
    public final AntlrDatatypeRuleToken ruleOpOther() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4921:2: ( ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) ) )
            // InternalLinkerScript.g:4922:2: ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) )
            {
            // InternalLinkerScript.g:4922:2: ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) )
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==40) ) {
                alt73=1;
            }
            else if ( (LA73_0==68) ) {
                alt73=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                throw nvae;
            }
            switch (alt73) {
                case 1 :
                    // InternalLinkerScript.g:4923:3: (kw= '>' ( ( '>' )=>kw= '>' ) )
                    {
                    // InternalLinkerScript.g:4923:3: (kw= '>' ( ( '>' )=>kw= '>' ) )
                    // InternalLinkerScript.g:4924:4: kw= '>' ( ( '>' )=>kw= '>' )
                    {
                    kw=(Token)match(input,40,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpOtherAccess().getGreaterThanSignKeyword_0_0());
                      			
                    }
                    // InternalLinkerScript.g:4929:4: ( ( '>' )=>kw= '>' )
                    // InternalLinkerScript.g:4930:5: ( '>' )=>kw= '>'
                    {
                    kw=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					current.merge(kw);
                      					newLeafNode(kw, grammarAccess.getOpOtherAccess().getGreaterThanSignKeyword_0_1());
                      				
                    }

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4939:3: (kw= '<' ( ( '<' )=>kw= '<' ) )
                    {
                    // InternalLinkerScript.g:4939:3: (kw= '<' ( ( '<' )=>kw= '<' ) )
                    // InternalLinkerScript.g:4940:4: kw= '<' ( ( '<' )=>kw= '<' )
                    {
                    kw=(Token)match(input,68,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpOtherAccess().getLessThanSignKeyword_1_0());
                      			
                    }
                    // InternalLinkerScript.g:4945:4: ( ( '<' )=>kw= '<' )
                    // InternalLinkerScript.g:4946:5: ( '<' )=>kw= '<'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					current.merge(kw);
                      					newLeafNode(kw, grammarAccess.getOpOtherAccess().getLessThanSignKeyword_1_1());
                      				
                    }

                    }


                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpOther"


    // $ANTLR start "entryRuleLAdditiveExpression"
    // InternalLinkerScript.g:4958:1: entryRuleLAdditiveExpression returns [EObject current=null] : iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF ;
    public final EObject entryRuleLAdditiveExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAdditiveExpression = null;


        try {
            // InternalLinkerScript.g:4958:60: (iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF )
            // InternalLinkerScript.g:4959:2: iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLAdditiveExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLAdditiveExpression=ruleLAdditiveExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLAdditiveExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLAdditiveExpression"


    // $ANTLR start "ruleLAdditiveExpression"
    // InternalLinkerScript.g:4965:1: ruleLAdditiveExpression returns [EObject current=null] : (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* ) ;
    public final EObject ruleLAdditiveExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LMultiplicativeExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4971:2: ( (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* ) )
            // InternalLinkerScript.g:4972:2: (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* )
            {
            // InternalLinkerScript.g:4972:2: (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* )
            // InternalLinkerScript.g:4973:3: this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getLMultiplicativeExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_57);
            this_LMultiplicativeExpression_0=ruleLMultiplicativeExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LMultiplicativeExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4981:3: ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*
            loop74:
            do {
                int alt74=2;
                alt74 = dfa74.predict(input);
                switch (alt74) {
            	case 1 :
            	    // InternalLinkerScript.g:4982:4: ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) )
            	    {
            	    // InternalLinkerScript.g:4982:4: ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) )
            	    // InternalLinkerScript.g:4983:5: ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) )
            	    {
            	    // InternalLinkerScript.g:4993:5: ( () ( (lv_feature_2_0= ruleOpAdd ) ) )
            	    // InternalLinkerScript.g:4994:6: () ( (lv_feature_2_0= ruleOpAdd ) )
            	    {
            	    // InternalLinkerScript.g:4994:6: ()
            	    // InternalLinkerScript.g:4995:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLAdditiveExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:5001:6: ( (lv_feature_2_0= ruleOpAdd ) )
            	    // InternalLinkerScript.g:5002:7: (lv_feature_2_0= ruleOpAdd )
            	    {
            	    // InternalLinkerScript.g:5002:7: (lv_feature_2_0= ruleOpAdd )
            	    // InternalLinkerScript.g:5003:8: lv_feature_2_0= ruleOpAdd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getFeatureOpAddParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpAdd();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLAdditiveExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpAdd");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:5022:4: ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) )
            	    // InternalLinkerScript.g:5023:5: (lv_rightOperand_3_0= ruleLMultiplicativeExpression )
            	    {
            	    // InternalLinkerScript.g:5023:5: (lv_rightOperand_3_0= ruleLMultiplicativeExpression )
            	    // InternalLinkerScript.g:5024:6: lv_rightOperand_3_0= ruleLMultiplicativeExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getRightOperandLMultiplicativeExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_57);
            	    lv_rightOperand_3_0=ruleLMultiplicativeExpression();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLAdditiveExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LMultiplicativeExpression");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLAdditiveExpression"


    // $ANTLR start "entryRuleOpAdd"
    // InternalLinkerScript.g:5046:1: entryRuleOpAdd returns [String current=null] : iv_ruleOpAdd= ruleOpAdd EOF ;
    public final String entryRuleOpAdd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAdd = null;


        try {
            // InternalLinkerScript.g:5046:45: (iv_ruleOpAdd= ruleOpAdd EOF )
            // InternalLinkerScript.g:5047:2: iv_ruleOpAdd= ruleOpAdd EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpAddRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpAdd=ruleOpAdd();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpAdd.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpAdd"


    // $ANTLR start "ruleOpAdd"
    // InternalLinkerScript.g:5053:1: ruleOpAdd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '+' | kw= '-' ) ;
    public final AntlrDatatypeRuleToken ruleOpAdd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5059:2: ( (kw= '+' | kw= '-' ) )
            // InternalLinkerScript.g:5060:2: (kw= '+' | kw= '-' )
            {
            // InternalLinkerScript.g:5060:2: (kw= '+' | kw= '-' )
            int alt75=2;
            int LA75_0 = input.LA(1);

            if ( (LA75_0==90) ) {
                alt75=1;
            }
            else if ( (LA75_0==91) ) {
                alt75=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 75, 0, input);

                throw nvae;
            }
            switch (alt75) {
                case 1 :
                    // InternalLinkerScript.g:5061:3: kw= '+'
                    {
                    kw=(Token)match(input,90,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAddAccess().getPlusSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5067:3: kw= '-'
                    {
                    kw=(Token)match(input,91,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAddAccess().getHyphenMinusKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpAdd"


    // $ANTLR start "entryRuleLMultiplicativeExpression"
    // InternalLinkerScript.g:5076:1: entryRuleLMultiplicativeExpression returns [EObject current=null] : iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF ;
    public final EObject entryRuleLMultiplicativeExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLMultiplicativeExpression = null;


        try {
            // InternalLinkerScript.g:5076:66: (iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF )
            // InternalLinkerScript.g:5077:2: iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLMultiplicativeExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLMultiplicativeExpression=ruleLMultiplicativeExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLMultiplicativeExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLMultiplicativeExpression"


    // $ANTLR start "ruleLMultiplicativeExpression"
    // InternalLinkerScript.g:5083:1: ruleLMultiplicativeExpression returns [EObject current=null] : (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* ) ;
    public final EObject ruleLMultiplicativeExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LUnaryOperation_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5089:2: ( (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* ) )
            // InternalLinkerScript.g:5090:2: (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* )
            {
            // InternalLinkerScript.g:5090:2: (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* )
            // InternalLinkerScript.g:5091:3: this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getLUnaryOperationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_58);
            this_LUnaryOperation_0=ruleLUnaryOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LUnaryOperation_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:5099:3: ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*
            loop76:
            do {
                int alt76=2;
                alt76 = dfa76.predict(input);
                switch (alt76) {
            	case 1 :
            	    // InternalLinkerScript.g:5100:4: ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) )
            	    {
            	    // InternalLinkerScript.g:5100:4: ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) )
            	    // InternalLinkerScript.g:5101:5: ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) )
            	    {
            	    // InternalLinkerScript.g:5111:5: ( () ( (lv_feature_2_0= ruleOpMulti ) ) )
            	    // InternalLinkerScript.g:5112:6: () ( (lv_feature_2_0= ruleOpMulti ) )
            	    {
            	    // InternalLinkerScript.g:5112:6: ()
            	    // InternalLinkerScript.g:5113:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLMultiplicativeExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:5119:6: ( (lv_feature_2_0= ruleOpMulti ) )
            	    // InternalLinkerScript.g:5120:7: (lv_feature_2_0= ruleOpMulti )
            	    {
            	    // InternalLinkerScript.g:5120:7: (lv_feature_2_0= ruleOpMulti )
            	    // InternalLinkerScript.g:5121:8: lv_feature_2_0= ruleOpMulti
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getFeatureOpMultiParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_8);
            	    lv_feature_2_0=ruleOpMulti();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      								if (current==null) {
            	      									current = createModelElementForParent(grammarAccess.getLMultiplicativeExpressionRule());
            	      								}
            	      								set(
            	      									current,
            	      									"feature",
            	      									lv_feature_2_0,
            	      									"org.eclipse.cdt.linkerscript.LinkerScript.OpMulti");
            	      								afterParserOrEnumRuleCall();
            	      							
            	    }

            	    }


            	    }


            	    }


            	    }

            	    // InternalLinkerScript.g:5140:4: ( (lv_rightOperand_3_0= ruleLUnaryOperation ) )
            	    // InternalLinkerScript.g:5141:5: (lv_rightOperand_3_0= ruleLUnaryOperation )
            	    {
            	    // InternalLinkerScript.g:5141:5: (lv_rightOperand_3_0= ruleLUnaryOperation )
            	    // InternalLinkerScript.g:5142:6: lv_rightOperand_3_0= ruleLUnaryOperation
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getRightOperandLUnaryOperationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_58);
            	    lv_rightOperand_3_0=ruleLUnaryOperation();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      						if (current==null) {
            	      							current = createModelElementForParent(grammarAccess.getLMultiplicativeExpressionRule());
            	      						}
            	      						set(
            	      							current,
            	      							"rightOperand",
            	      							lv_rightOperand_3_0,
            	      							"org.eclipse.cdt.linkerscript.LinkerScript.LUnaryOperation");
            	      						afterParserOrEnumRuleCall();
            	      					
            	    }

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop76;
                }
            } while (true);


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLMultiplicativeExpression"


    // $ANTLR start "entryRuleOpMulti"
    // InternalLinkerScript.g:5164:1: entryRuleOpMulti returns [String current=null] : iv_ruleOpMulti= ruleOpMulti EOF ;
    public final String entryRuleOpMulti() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpMulti = null;


        try {
            // InternalLinkerScript.g:5164:47: (iv_ruleOpMulti= ruleOpMulti EOF )
            // InternalLinkerScript.g:5165:2: iv_ruleOpMulti= ruleOpMulti EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpMultiRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpMulti=ruleOpMulti();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpMulti.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpMulti"


    // $ANTLR start "ruleOpMulti"
    // InternalLinkerScript.g:5171:1: ruleOpMulti returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '*' | kw= '/' | kw= '%' ) ;
    public final AntlrDatatypeRuleToken ruleOpMulti() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5177:2: ( (kw= '*' | kw= '/' | kw= '%' ) )
            // InternalLinkerScript.g:5178:2: (kw= '*' | kw= '/' | kw= '%' )
            {
            // InternalLinkerScript.g:5178:2: (kw= '*' | kw= '/' | kw= '%' )
            int alt77=3;
            switch ( input.LA(1) ) {
            case 92:
                {
                alt77=1;
                }
                break;
            case 93:
                {
                alt77=2;
                }
                break;
            case 94:
                {
                alt77=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                throw nvae;
            }

            switch (alt77) {
                case 1 :
                    // InternalLinkerScript.g:5179:3: kw= '*'
                    {
                    kw=(Token)match(input,92,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getAsteriskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5185:3: kw= '/'
                    {
                    kw=(Token)match(input,93,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getSolidusKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5191:3: kw= '%'
                    {
                    kw=(Token)match(input,94,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getPercentSignKeyword_2());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpMulti"


    // $ANTLR start "entryRuleLUnaryOperation"
    // InternalLinkerScript.g:5200:1: entryRuleLUnaryOperation returns [EObject current=null] : iv_ruleLUnaryOperation= ruleLUnaryOperation EOF ;
    public final EObject entryRuleLUnaryOperation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLUnaryOperation = null;


        try {
            // InternalLinkerScript.g:5200:56: (iv_ruleLUnaryOperation= ruleLUnaryOperation EOF )
            // InternalLinkerScript.g:5201:2: iv_ruleLUnaryOperation= ruleLUnaryOperation EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLUnaryOperationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLUnaryOperation=ruleLUnaryOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLUnaryOperation; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLUnaryOperation"


    // $ANTLR start "ruleLUnaryOperation"
    // InternalLinkerScript.g:5207:1: ruleLUnaryOperation returns [EObject current=null] : ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPrimaryExpression_3= ruleLPrimaryExpression ) ;
    public final EObject ruleLUnaryOperation() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_feature_1_0 = null;

        EObject lv_operand_2_0 = null;

        EObject this_LPrimaryExpression_3 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5213:2: ( ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPrimaryExpression_3= ruleLPrimaryExpression ) )
            // InternalLinkerScript.g:5214:2: ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPrimaryExpression_3= ruleLPrimaryExpression )
            {
            // InternalLinkerScript.g:5214:2: ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPrimaryExpression_3= ruleLPrimaryExpression )
            int alt78=2;
            int LA78_0 = input.LA(1);

            if ( (LA78_0==83||(LA78_0>=90 && LA78_0<=91)||LA78_0==95) ) {
                alt78=1;
            }
            else if ( ((LA78_0>=RULE_ID && LA78_0<=RULE_HEX)||LA78_0==13||LA78_0==38||LA78_0==42||(LA78_0>=76 && LA78_0<=82)||LA78_0==96) ) {
                alt78=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 78, 0, input);

                throw nvae;
            }
            switch (alt78) {
                case 1 :
                    // InternalLinkerScript.g:5215:3: ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) )
                    {
                    // InternalLinkerScript.g:5215:3: ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) )
                    // InternalLinkerScript.g:5216:4: () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) )
                    {
                    // InternalLinkerScript.g:5216:4: ()
                    // InternalLinkerScript.g:5217:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getLUnaryOperationAccess().getLUnaryOperationAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:5223:4: ( (lv_feature_1_0= ruleOpUnary ) )
                    // InternalLinkerScript.g:5224:5: (lv_feature_1_0= ruleOpUnary )
                    {
                    // InternalLinkerScript.g:5224:5: (lv_feature_1_0= ruleOpUnary )
                    // InternalLinkerScript.g:5225:6: lv_feature_1_0= ruleOpUnary
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLUnaryOperationAccess().getFeatureOpUnaryParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_8);
                    lv_feature_1_0=ruleOpUnary();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLUnaryOperationRule());
                      						}
                      						set(
                      							current,
                      							"feature",
                      							lv_feature_1_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.OpUnary");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:5242:4: ( (lv_operand_2_0= ruleLUnaryOperation ) )
                    // InternalLinkerScript.g:5243:5: (lv_operand_2_0= ruleLUnaryOperation )
                    {
                    // InternalLinkerScript.g:5243:5: (lv_operand_2_0= ruleLUnaryOperation )
                    // InternalLinkerScript.g:5244:6: lv_operand_2_0= ruleLUnaryOperation
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLUnaryOperationAccess().getOperandLUnaryOperationParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_operand_2_0=ruleLUnaryOperation();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLUnaryOperationRule());
                      						}
                      						set(
                      							current,
                      							"operand",
                      							lv_operand_2_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LUnaryOperation");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5263:3: this_LPrimaryExpression_3= ruleLPrimaryExpression
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLUnaryOperationAccess().getLPrimaryExpressionParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LPrimaryExpression_3=ruleLPrimaryExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LPrimaryExpression_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLUnaryOperation"


    // $ANTLR start "entryRuleOpUnary"
    // InternalLinkerScript.g:5275:1: entryRuleOpUnary returns [String current=null] : iv_ruleOpUnary= ruleOpUnary EOF ;
    public final String entryRuleOpUnary() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpUnary = null;


        try {
            // InternalLinkerScript.g:5275:47: (iv_ruleOpUnary= ruleOpUnary EOF )
            // InternalLinkerScript.g:5276:2: iv_ruleOpUnary= ruleOpUnary EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpUnaryRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpUnary=ruleOpUnary();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpUnary.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOpUnary"


    // $ANTLR start "ruleOpUnary"
    // InternalLinkerScript.g:5282:1: ruleOpUnary returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '!' | kw= '-' | kw= '+' | kw= '~' ) ;
    public final AntlrDatatypeRuleToken ruleOpUnary() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5288:2: ( (kw= '!' | kw= '-' | kw= '+' | kw= '~' ) )
            // InternalLinkerScript.g:5289:2: (kw= '!' | kw= '-' | kw= '+' | kw= '~' )
            {
            // InternalLinkerScript.g:5289:2: (kw= '!' | kw= '-' | kw= '+' | kw= '~' )
            int alt79=4;
            switch ( input.LA(1) ) {
            case 83:
                {
                alt79=1;
                }
                break;
            case 91:
                {
                alt79=2;
                }
                break;
            case 90:
                {
                alt79=3;
                }
                break;
            case 95:
                {
                alt79=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 79, 0, input);

                throw nvae;
            }

            switch (alt79) {
                case 1 :
                    // InternalLinkerScript.g:5290:3: kw= '!'
                    {
                    kw=(Token)match(input,83,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getExclamationMarkKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5296:3: kw= '-'
                    {
                    kw=(Token)match(input,91,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getHyphenMinusKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5302:3: kw= '+'
                    {
                    kw=(Token)match(input,90,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getPlusSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:5308:3: kw= '~'
                    {
                    kw=(Token)match(input,95,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getTildeKeyword_3());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOpUnary"


    // $ANTLR start "entryRuleLPrimaryExpression"
    // InternalLinkerScript.g:5317:1: entryRuleLPrimaryExpression returns [EObject current=null] : iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF ;
    public final EObject entryRuleLPrimaryExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLPrimaryExpression = null;


        try {
            // InternalLinkerScript.g:5317:59: (iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF )
            // InternalLinkerScript.g:5318:2: iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLPrimaryExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLPrimaryExpression=ruleLPrimaryExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLPrimaryExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLPrimaryExpression"


    // $ANTLR start "ruleLPrimaryExpression"
    // InternalLinkerScript.g:5324:1: ruleLPrimaryExpression returns [EObject current=null] : (this_LengthCall_0= ruleLengthCall | this_OriginCall_1= ruleOriginCall | this_AlignCall_2= ruleAlignCall | this_SizeofCall_3= ruleSizeofCall | this_AtCall_4= ruleAtCall | this_LNumberLiteral_5= ruleLNumberLiteral | this_LParenthesizedExpression_6= ruleLParenthesizedExpression | this_LVariable_7= ruleLVariable ) ;
    public final EObject ruleLPrimaryExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LengthCall_0 = null;

        EObject this_OriginCall_1 = null;

        EObject this_AlignCall_2 = null;

        EObject this_SizeofCall_3 = null;

        EObject this_AtCall_4 = null;

        EObject this_LNumberLiteral_5 = null;

        EObject this_LParenthesizedExpression_6 = null;

        EObject this_LVariable_7 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5330:2: ( (this_LengthCall_0= ruleLengthCall | this_OriginCall_1= ruleOriginCall | this_AlignCall_2= ruleAlignCall | this_SizeofCall_3= ruleSizeofCall | this_AtCall_4= ruleAtCall | this_LNumberLiteral_5= ruleLNumberLiteral | this_LParenthesizedExpression_6= ruleLParenthesizedExpression | this_LVariable_7= ruleLVariable ) )
            // InternalLinkerScript.g:5331:2: (this_LengthCall_0= ruleLengthCall | this_OriginCall_1= ruleOriginCall | this_AlignCall_2= ruleAlignCall | this_SizeofCall_3= ruleSizeofCall | this_AtCall_4= ruleAtCall | this_LNumberLiteral_5= ruleLNumberLiteral | this_LParenthesizedExpression_6= ruleLParenthesizedExpression | this_LVariable_7= ruleLVariable )
            {
            // InternalLinkerScript.g:5331:2: (this_LengthCall_0= ruleLengthCall | this_OriginCall_1= ruleOriginCall | this_AlignCall_2= ruleAlignCall | this_SizeofCall_3= ruleSizeofCall | this_AtCall_4= ruleAtCall | this_LNumberLiteral_5= ruleLNumberLiteral | this_LParenthesizedExpression_6= ruleLParenthesizedExpression | this_LVariable_7= ruleLVariable )
            int alt80=8;
            switch ( input.LA(1) ) {
            case 80:
                {
                alt80=1;
                }
                break;
            case 77:
                {
                alt80=2;
                }
                break;
            case 42:
                {
                alt80=3;
                }
                break;
            case 96:
                {
                alt80=4;
                }
                break;
            case 38:
                {
                alt80=5;
                }
                break;
            case RULE_DEC:
            case RULE_HEX:
                {
                alt80=6;
                }
                break;
            case 13:
                {
                alt80=7;
                }
                break;
            case RULE_ID:
            case 76:
            case 78:
            case 79:
            case 81:
            case 82:
                {
                alt80=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                throw nvae;
            }

            switch (alt80) {
                case 1 :
                    // InternalLinkerScript.g:5332:3: this_LengthCall_0= ruleLengthCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLengthCallParserRuleCall_0());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LengthCall_0=ruleLengthCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LengthCall_0;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5341:3: this_OriginCall_1= ruleOriginCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getOriginCallParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_OriginCall_1=ruleOriginCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_OriginCall_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5350:3: this_AlignCall_2= ruleAlignCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getAlignCallParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_AlignCall_2=ruleAlignCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_AlignCall_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:5359:3: this_SizeofCall_3= ruleSizeofCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getSizeofCallParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_SizeofCall_3=ruleSizeofCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_SizeofCall_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:5368:3: this_AtCall_4= ruleAtCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getAtCallParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_AtCall_4=ruleAtCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_AtCall_4;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:5377:3: this_LNumberLiteral_5= ruleLNumberLiteral
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLNumberLiteralParserRuleCall_5());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LNumberLiteral_5=ruleLNumberLiteral();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LNumberLiteral_5;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:5386:3: this_LParenthesizedExpression_6= ruleLParenthesizedExpression
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLParenthesizedExpressionParserRuleCall_6());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LParenthesizedExpression_6=ruleLParenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LParenthesizedExpression_6;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:5395:3: this_LVariable_7= ruleLVariable
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLVariableParserRuleCall_7());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LVariable_7=ruleLVariable();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LVariable_7;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLPrimaryExpression"


    // $ANTLR start "entryRuleLVariable"
    // InternalLinkerScript.g:5407:1: entryRuleLVariable returns [EObject current=null] : iv_ruleLVariable= ruleLVariable EOF ;
    public final EObject entryRuleLVariable() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLVariable = null;


        try {
            // InternalLinkerScript.g:5407:50: (iv_ruleLVariable= ruleLVariable EOF )
            // InternalLinkerScript.g:5408:2: iv_ruleLVariable= ruleLVariable EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLVariableRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLVariable=ruleLVariable();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLVariable; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLVariable"


    // $ANTLR start "ruleLVariable"
    // InternalLinkerScript.g:5414:1: ruleLVariable returns [EObject current=null] : ( () ( (lv_feature_1_0= ruleValidID ) ) ) ;
    public final EObject ruleLVariable() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_feature_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5420:2: ( ( () ( (lv_feature_1_0= ruleValidID ) ) ) )
            // InternalLinkerScript.g:5421:2: ( () ( (lv_feature_1_0= ruleValidID ) ) )
            {
            // InternalLinkerScript.g:5421:2: ( () ( (lv_feature_1_0= ruleValidID ) ) )
            // InternalLinkerScript.g:5422:3: () ( (lv_feature_1_0= ruleValidID ) )
            {
            // InternalLinkerScript.g:5422:3: ()
            // InternalLinkerScript.g:5423:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLVariableAccess().getLVariableAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:5429:3: ( (lv_feature_1_0= ruleValidID ) )
            // InternalLinkerScript.g:5430:4: (lv_feature_1_0= ruleValidID )
            {
            // InternalLinkerScript.g:5430:4: (lv_feature_1_0= ruleValidID )
            // InternalLinkerScript.g:5431:5: lv_feature_1_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLVariableAccess().getFeatureValidIDParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_feature_1_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLVariableRule());
              					}
              					set(
              						current,
              						"feature",
              						lv_feature_1_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLVariable"


    // $ANTLR start "entryRuleLParenthesizedExpression"
    // InternalLinkerScript.g:5452:1: entryRuleLParenthesizedExpression returns [EObject current=null] : iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF ;
    public final EObject entryRuleLParenthesizedExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLParenthesizedExpression = null;


        try {
            // InternalLinkerScript.g:5452:65: (iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF )
            // InternalLinkerScript.g:5453:2: iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLParenthesizedExpressionRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLParenthesizedExpression=ruleLParenthesizedExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLParenthesizedExpression; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLParenthesizedExpression"


    // $ANTLR start "ruleLParenthesizedExpression"
    // InternalLinkerScript.g:5459:1: ruleLParenthesizedExpression returns [EObject current=null] : ( () otherlv_1= '(' ( (lv_exp_2_0= ruleLExpression ) ) otherlv_3= ')' ) ;
    public final EObject ruleLParenthesizedExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_exp_2_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5465:2: ( ( () otherlv_1= '(' ( (lv_exp_2_0= ruleLExpression ) ) otherlv_3= ')' ) )
            // InternalLinkerScript.g:5466:2: ( () otherlv_1= '(' ( (lv_exp_2_0= ruleLExpression ) ) otherlv_3= ')' )
            {
            // InternalLinkerScript.g:5466:2: ( () otherlv_1= '(' ( (lv_exp_2_0= ruleLExpression ) ) otherlv_3= ')' )
            // InternalLinkerScript.g:5467:3: () otherlv_1= '(' ( (lv_exp_2_0= ruleLExpression ) ) otherlv_3= ')'
            {
            // InternalLinkerScript.g:5467:3: ()
            // InternalLinkerScript.g:5468:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLParenthesizedExpressionAccess().getLParenthesizedExpressionAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getLParenthesizedExpressionAccess().getLeftParenthesisKeyword_1());
              		
            }
            // InternalLinkerScript.g:5478:3: ( (lv_exp_2_0= ruleLExpression ) )
            // InternalLinkerScript.g:5479:4: (lv_exp_2_0= ruleLExpression )
            {
            // InternalLinkerScript.g:5479:4: (lv_exp_2_0= ruleLExpression )
            // InternalLinkerScript.g:5480:5: lv_exp_2_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLParenthesizedExpressionAccess().getExpLExpressionParserRuleCall_2_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_exp_2_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLParenthesizedExpressionRule());
              					}
              					set(
              						current,
              						"exp",
              						lv_exp_2_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_3=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_3, grammarAccess.getLParenthesizedExpressionAccess().getRightParenthesisKeyword_3());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLParenthesizedExpression"


    // $ANTLR start "entryRuleLengthCall"
    // InternalLinkerScript.g:5505:1: entryRuleLengthCall returns [EObject current=null] : iv_ruleLengthCall= ruleLengthCall EOF ;
    public final EObject entryRuleLengthCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLengthCall = null;


        try {
            // InternalLinkerScript.g:5505:51: (iv_ruleLengthCall= ruleLengthCall EOF )
            // InternalLinkerScript.g:5506:2: iv_ruleLengthCall= ruleLengthCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLengthCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLengthCall=ruleLengthCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLengthCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLengthCall"


    // $ANTLR start "ruleLengthCall"
    // InternalLinkerScript.g:5512:1: ruleLengthCall returns [EObject current=null] : ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) ;
    public final EObject ruleLengthCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_memory_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5518:2: ( ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:5519:2: ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:5519:2: ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:5520:3: () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:5520:3: ()
            // InternalLinkerScript.g:5521:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLengthCallAccess().getLengthCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,80,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getLengthCallAccess().getLENGTHKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getLengthCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5535:3: ( (lv_memory_3_0= ruleValidID ) )
            // InternalLinkerScript.g:5536:4: (lv_memory_3_0= ruleValidID )
            {
            // InternalLinkerScript.g:5536:4: (lv_memory_3_0= ruleValidID )
            // InternalLinkerScript.g:5537:5: lv_memory_3_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLengthCallAccess().getMemoryValidIDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_memory_3_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLengthCallRule());
              					}
              					set(
              						current,
              						"memory",
              						lv_memory_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getLengthCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLengthCall"


    // $ANTLR start "entryRuleOriginCall"
    // InternalLinkerScript.g:5562:1: entryRuleOriginCall returns [EObject current=null] : iv_ruleOriginCall= ruleOriginCall EOF ;
    public final EObject entryRuleOriginCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOriginCall = null;


        try {
            // InternalLinkerScript.g:5562:51: (iv_ruleOriginCall= ruleOriginCall EOF )
            // InternalLinkerScript.g:5563:2: iv_ruleOriginCall= ruleOriginCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOriginCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOriginCall=ruleOriginCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOriginCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOriginCall"


    // $ANTLR start "ruleOriginCall"
    // InternalLinkerScript.g:5569:1: ruleOriginCall returns [EObject current=null] : ( () otherlv_1= 'ORIGIN' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) ;
    public final EObject ruleOriginCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_memory_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5575:2: ( ( () otherlv_1= 'ORIGIN' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:5576:2: ( () otherlv_1= 'ORIGIN' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:5576:2: ( () otherlv_1= 'ORIGIN' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:5577:3: () otherlv_1= 'ORIGIN' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:5577:3: ()
            // InternalLinkerScript.g:5578:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getOriginCallAccess().getOriginCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,77,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getOriginCallAccess().getORIGINKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getOriginCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5592:3: ( (lv_memory_3_0= ruleValidID ) )
            // InternalLinkerScript.g:5593:4: (lv_memory_3_0= ruleValidID )
            {
            // InternalLinkerScript.g:5593:4: (lv_memory_3_0= ruleValidID )
            // InternalLinkerScript.g:5594:5: lv_memory_3_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getOriginCallAccess().getMemoryValidIDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_memory_3_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getOriginCallRule());
              					}
              					set(
              						current,
              						"memory",
              						lv_memory_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getOriginCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOriginCall"


    // $ANTLR start "entryRuleAlignCall"
    // InternalLinkerScript.g:5619:1: entryRuleAlignCall returns [EObject current=null] : iv_ruleAlignCall= ruleAlignCall EOF ;
    public final EObject entryRuleAlignCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAlignCall = null;


        try {
            // InternalLinkerScript.g:5619:50: (iv_ruleAlignCall= ruleAlignCall EOF )
            // InternalLinkerScript.g:5620:2: iv_ruleAlignCall= ruleAlignCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAlignCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAlignCall=ruleAlignCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAlignCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAlignCall"


    // $ANTLR start "ruleAlignCall"
    // InternalLinkerScript.g:5626:1: ruleAlignCall returns [EObject current=null] : ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' ) ;
    public final EObject ruleAlignCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_expOrAlign_3_0 = null;

        EObject lv_align_5_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5632:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' ) )
            // InternalLinkerScript.g:5633:2: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' )
            {
            // InternalLinkerScript.g:5633:2: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' )
            // InternalLinkerScript.g:5634:3: () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')'
            {
            // InternalLinkerScript.g:5634:3: ()
            // InternalLinkerScript.g:5635:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getAlignCallAccess().getAlignCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,42,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getAlignCallAccess().getALIGNKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getAlignCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5649:3: ( (lv_expOrAlign_3_0= ruleLExpression ) )
            // InternalLinkerScript.g:5650:4: (lv_expOrAlign_3_0= ruleLExpression )
            {
            // InternalLinkerScript.g:5650:4: (lv_expOrAlign_3_0= ruleLExpression )
            // InternalLinkerScript.g:5651:5: lv_expOrAlign_3_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getAlignCallAccess().getExpOrAlignLExpressionParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_10);
            lv_expOrAlign_3_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getAlignCallRule());
              					}
              					set(
              						current,
              						"expOrAlign",
              						lv_expOrAlign_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            // InternalLinkerScript.g:5668:3: (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )?
            int alt81=2;
            int LA81_0 = input.LA(1);

            if ( (LA81_0==11) ) {
                alt81=1;
            }
            switch (alt81) {
                case 1 :
                    // InternalLinkerScript.g:5669:4: otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) )
                    {
                    otherlv_4=(Token)match(input,11,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getAlignCallAccess().getCommaKeyword_4_0());
                      			
                    }
                    // InternalLinkerScript.g:5673:4: ( (lv_align_5_0= ruleLExpression ) )
                    // InternalLinkerScript.g:5674:5: (lv_align_5_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:5674:5: (lv_align_5_0= ruleLExpression )
                    // InternalLinkerScript.g:5675:6: lv_align_5_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAlignCallAccess().getAlignLExpressionParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_7);
                    lv_align_5_0=ruleLExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getAlignCallRule());
                      						}
                      						set(
                      							current,
                      							"align",
                      							lv_align_5_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }


                    }
                    break;

            }

            otherlv_6=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_6, grammarAccess.getAlignCallAccess().getRightParenthesisKeyword_5());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAlignCall"


    // $ANTLR start "entryRuleSizeofCall"
    // InternalLinkerScript.g:5701:1: entryRuleSizeofCall returns [EObject current=null] : iv_ruleSizeofCall= ruleSizeofCall EOF ;
    public final EObject entryRuleSizeofCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSizeofCall = null;


        try {
            // InternalLinkerScript.g:5701:51: (iv_ruleSizeofCall= ruleSizeofCall EOF )
            // InternalLinkerScript.g:5702:2: iv_ruleSizeofCall= ruleSizeofCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSizeofCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSizeofCall=ruleSizeofCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSizeofCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSizeofCall"


    // $ANTLR start "ruleSizeofCall"
    // InternalLinkerScript.g:5708:1: ruleSizeofCall returns [EObject current=null] : ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' ) ;
    public final EObject ruleSizeofCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_name_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5714:2: ( ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:5715:2: ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:5715:2: ( () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:5716:3: () otherlv_1= 'SIZEOF' otherlv_2= '(' ( (lv_name_3_0= ruleValidID ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:5716:3: ()
            // InternalLinkerScript.g:5717:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getSizeofCallAccess().getSizeofCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,96,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getSizeofCallAccess().getSIZEOFKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_6); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getSizeofCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5731:3: ( (lv_name_3_0= ruleValidID ) )
            // InternalLinkerScript.g:5732:4: (lv_name_3_0= ruleValidID )
            {
            // InternalLinkerScript.g:5732:4: (lv_name_3_0= ruleValidID )
            // InternalLinkerScript.g:5733:5: lv_name_3_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getSizeofCallAccess().getNameValidIDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_name_3_0=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getSizeofCallRule());
              					}
              					set(
              						current,
              						"name",
              						lv_name_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.ValidID");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getSizeofCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSizeofCall"


    // $ANTLR start "entryRuleAtCall"
    // InternalLinkerScript.g:5758:1: entryRuleAtCall returns [EObject current=null] : iv_ruleAtCall= ruleAtCall EOF ;
    public final EObject entryRuleAtCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAtCall = null;


        try {
            // InternalLinkerScript.g:5758:47: (iv_ruleAtCall= ruleAtCall EOF )
            // InternalLinkerScript.g:5759:2: iv_ruleAtCall= ruleAtCall EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getAtCallRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleAtCall=ruleAtCall();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleAtCall; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAtCall"


    // $ANTLR start "ruleAtCall"
    // InternalLinkerScript.g:5765:1: ruleAtCall returns [EObject current=null] : ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) ;
    public final EObject ruleAtCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_exp_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5771:2: ( ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:5772:2: ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:5772:2: ( () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:5773:3: () otherlv_1= 'AT' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:5773:3: ()
            // InternalLinkerScript.g:5774:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getAtCallAccess().getAtCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,38,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getAtCallAccess().getATKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getAtCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:5788:3: ( (lv_exp_3_0= ruleLExpression ) )
            // InternalLinkerScript.g:5789:4: (lv_exp_3_0= ruleLExpression )
            {
            // InternalLinkerScript.g:5789:4: (lv_exp_3_0= ruleLExpression )
            // InternalLinkerScript.g:5790:5: lv_exp_3_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getAtCallAccess().getExpLExpressionParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_7);
            lv_exp_3_0=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getAtCallRule());
              					}
              					set(
              						current,
              						"exp",
              						lv_exp_3_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.LExpression");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }

            otherlv_4=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_4, grammarAccess.getAtCallAccess().getRightParenthesisKeyword_4());
              		
            }

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAtCall"


    // $ANTLR start "entryRuleLNumberLiteral"
    // InternalLinkerScript.g:5815:1: entryRuleLNumberLiteral returns [EObject current=null] : iv_ruleLNumberLiteral= ruleLNumberLiteral EOF ;
    public final EObject entryRuleLNumberLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLNumberLiteral = null;


        try {
            // InternalLinkerScript.g:5815:55: (iv_ruleLNumberLiteral= ruleLNumberLiteral EOF )
            // InternalLinkerScript.g:5816:2: iv_ruleLNumberLiteral= ruleLNumberLiteral EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLNumberLiteralRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLNumberLiteral=ruleLNumberLiteral();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLNumberLiteral; 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLNumberLiteral"


    // $ANTLR start "ruleLNumberLiteral"
    // InternalLinkerScript.g:5822:1: ruleLNumberLiteral returns [EObject current=null] : ( () ( (lv_value_1_0= ruleNumber ) ) ) ;
    public final EObject ruleLNumberLiteral() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_value_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:5828:2: ( ( () ( (lv_value_1_0= ruleNumber ) ) ) )
            // InternalLinkerScript.g:5829:2: ( () ( (lv_value_1_0= ruleNumber ) ) )
            {
            // InternalLinkerScript.g:5829:2: ( () ( (lv_value_1_0= ruleNumber ) ) )
            // InternalLinkerScript.g:5830:3: () ( (lv_value_1_0= ruleNumber ) )
            {
            // InternalLinkerScript.g:5830:3: ()
            // InternalLinkerScript.g:5831:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLNumberLiteralAccess().getLNumberLiteralAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:5837:3: ( (lv_value_1_0= ruleNumber ) )
            // InternalLinkerScript.g:5838:4: (lv_value_1_0= ruleNumber )
            {
            // InternalLinkerScript.g:5838:4: (lv_value_1_0= ruleNumber )
            // InternalLinkerScript.g:5839:5: lv_value_1_0= ruleNumber
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLNumberLiteralAccess().getValueNumberParserRuleCall_1_0());
              				
            }
            pushFollow(FOLLOW_2);
            lv_value_1_0=ruleNumber();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              					if (current==null) {
              						current = createModelElementForParent(grammarAccess.getLNumberLiteralRule());
              					}
              					set(
              						current,
              						"value",
              						lv_value_1_0,
              						"org.eclipse.cdt.linkerscript.LinkerScript.Number");
              					afterParserOrEnumRuleCall();
              				
            }

            }


            }


            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLNumberLiteral"


    // $ANTLR start "entryRuleValidID"
    // InternalLinkerScript.g:5860:1: entryRuleValidID returns [String current=null] : iv_ruleValidID= ruleValidID EOF ;
    public final String entryRuleValidID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValidID = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:5862:2: (iv_ruleValidID= ruleValidID EOF )
            // InternalLinkerScript.g:5863:2: iv_ruleValidID= ruleValidID EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getValidIDRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleValidID=ruleValidID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleValidID.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleValidID"


    // $ANTLR start "ruleValidID"
    // InternalLinkerScript.g:5872:1: ruleValidID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' ) ;
    public final AntlrDatatypeRuleToken ruleValidID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;


        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:5879:2: ( (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' ) )
            // InternalLinkerScript.g:5880:2: (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' )
            {
            // InternalLinkerScript.g:5880:2: (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' )
            int alt82=6;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt82=1;
                }
                break;
            case 76:
                {
                alt82=2;
                }
                break;
            case 79:
                {
                alt82=3;
                }
                break;
            case 78:
                {
                alt82=4;
                }
                break;
            case 82:
                {
                alt82=5;
                }
                break;
            case 81:
                {
                alt82=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }

            switch (alt82) {
                case 1 :
                    // InternalLinkerScript.g:5881:3: this_ID_0= RULE_ID
                    {
                    this_ID_0=(Token)match(input,RULE_ID,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ID_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_ID_0, grammarAccess.getValidIDAccess().getIDTerminalRuleCall_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5889:3: kw= 'MEMORY'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getMEMORYKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:5895:3: kw= 'o'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getOKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:5901:3: kw= 'org'
                    {
                    kw=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getOrgKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:5907:3: kw= 'l'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getLKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:5913:3: kw= 'len'
                    {
                    kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getLenKeyword_5());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleValidID"


    // $ANTLR start "entryRuleWildID"
    // InternalLinkerScript.g:5925:1: entryRuleWildID returns [String current=null] : iv_ruleWildID= ruleWildID EOF ;
    public final String entryRuleWildID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleWildID = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:5927:2: (iv_ruleWildID= ruleWildID EOF )
            // InternalLinkerScript.g:5928:2: iv_ruleWildID= ruleWildID EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getWildIDRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleWildID=ruleWildID();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleWildID.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleWildID"


    // $ANTLR start "ruleWildID"
    // InternalLinkerScript.g:5937:1: ruleWildID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '*' | this_ValidID_1= ruleValidID ) ;
    public final AntlrDatatypeRuleToken ruleWildID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_ValidID_1 = null;



        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:5944:2: ( (kw= '*' | this_ValidID_1= ruleValidID ) )
            // InternalLinkerScript.g:5945:2: (kw= '*' | this_ValidID_1= ruleValidID )
            {
            // InternalLinkerScript.g:5945:2: (kw= '*' | this_ValidID_1= ruleValidID )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0==92) ) {
                alt83=1;
            }
            else if ( (LA83_0==RULE_ID||LA83_0==76||(LA83_0>=78 && LA83_0<=79)||(LA83_0>=81 && LA83_0<=82)) ) {
                alt83=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }
            switch (alt83) {
                case 1 :
                    // InternalLinkerScript.g:5946:3: kw= '*'
                    {
                    kw=(Token)match(input,92,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getWildIDAccess().getAsteriskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5952:3: this_ValidID_1= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getWildIDAccess().getValidIDParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_ValidID_1=ruleValidID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_ValidID_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleWildID"


    // $ANTLR start "entryRuleValidFunc"
    // InternalLinkerScript.g:5969:1: entryRuleValidFunc returns [String current=null] : iv_ruleValidFunc= ruleValidFunc EOF ;
    public final String entryRuleValidFunc() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValidFunc = null;


        try {
            // InternalLinkerScript.g:5969:49: (iv_ruleValidFunc= ruleValidFunc EOF )
            // InternalLinkerScript.g:5970:2: iv_ruleValidFunc= ruleValidFunc EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getValidFuncRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleValidFunc=ruleValidFunc();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleValidFunc.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleValidFunc"


    // $ANTLR start "ruleValidFunc"
    // InternalLinkerScript.g:5976:1: ruleValidFunc returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'LENGTH' | kw= 'ALIGN' ) ;
    public final AntlrDatatypeRuleToken ruleValidFunc() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:5982:2: ( (kw= 'LENGTH' | kw= 'ALIGN' ) )
            // InternalLinkerScript.g:5983:2: (kw= 'LENGTH' | kw= 'ALIGN' )
            {
            // InternalLinkerScript.g:5983:2: (kw= 'LENGTH' | kw= 'ALIGN' )
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( (LA84_0==80) ) {
                alt84=1;
            }
            else if ( (LA84_0==42) ) {
                alt84=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 84, 0, input);

                throw nvae;
            }
            switch (alt84) {
                case 1 :
                    // InternalLinkerScript.g:5984:3: kw= 'LENGTH'
                    {
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidFuncAccess().getLENGTHKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:5990:3: kw= 'ALIGN'
                    {
                    kw=(Token)match(input,42,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidFuncAccess().getALIGNKeyword_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleValidFunc"


    // $ANTLR start "entryRuleNumber"
    // InternalLinkerScript.g:5999:1: entryRuleNumber returns [String current=null] : iv_ruleNumber= ruleNumber EOF ;
    public final String entryRuleNumber() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNumber = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6001:2: (iv_ruleNumber= ruleNumber EOF )
            // InternalLinkerScript.g:6002:2: iv_ruleNumber= ruleNumber EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getNumberRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleNumber=ruleNumber();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleNumber.getText(); 
            }
            match(input,EOF,FOLLOW_2); if (state.failed) return current;

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleNumber"


    // $ANTLR start "ruleNumber"
    // InternalLinkerScript.g:6011:1: ruleNumber returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX ) ;
    public final AntlrDatatypeRuleToken ruleNumber() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_DEC_0=null;
        Token this_HEX_1=null;


        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:6018:2: ( (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX ) )
            // InternalLinkerScript.g:6019:2: (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX )
            {
            // InternalLinkerScript.g:6019:2: (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX )
            int alt85=2;
            int LA85_0 = input.LA(1);

            if ( (LA85_0==RULE_DEC) ) {
                alt85=1;
            }
            else if ( (LA85_0==RULE_HEX) ) {
                alt85=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                throw nvae;
            }
            switch (alt85) {
                case 1 :
                    // InternalLinkerScript.g:6020:3: this_DEC_0= RULE_DEC
                    {
                    this_DEC_0=(Token)match(input,RULE_DEC,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_DEC_0);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_DEC_0, grammarAccess.getNumberAccess().getDECTerminalRuleCall_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:6028:3: this_HEX_1= RULE_HEX
                    {
                    this_HEX_1=(Token)match(input,RULE_HEX,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(this_HEX_1);
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			newLeafNode(this_HEX_1, grammarAccess.getNumberAccess().getHEXTerminalRuleCall_1());
                      		
                    }

                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleNumber"


    // $ANTLR start "ruleWildcardSort"
    // InternalLinkerScript.g:6042:1: ruleWildcardSort returns [Enumerator current=null] : ( (enumLiteral_0= 'SORT_NONE' ) | (enumLiteral_1= 'SORT' ) | (enumLiteral_2= 'SORT_BY_NAME' ) | (enumLiteral_3= 'SORT_BY_ALIGNMENT' ) | (enumLiteral_4= 'SORT_BY_INIT_PRIORITY' ) ) ;
    public final Enumerator ruleWildcardSort() throws RecognitionException {
        Enumerator current = null;

        Token enumLiteral_0=null;
        Token enumLiteral_1=null;
        Token enumLiteral_2=null;
        Token enumLiteral_3=null;
        Token enumLiteral_4=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:6048:2: ( ( (enumLiteral_0= 'SORT_NONE' ) | (enumLiteral_1= 'SORT' ) | (enumLiteral_2= 'SORT_BY_NAME' ) | (enumLiteral_3= 'SORT_BY_ALIGNMENT' ) | (enumLiteral_4= 'SORT_BY_INIT_PRIORITY' ) ) )
            // InternalLinkerScript.g:6049:2: ( (enumLiteral_0= 'SORT_NONE' ) | (enumLiteral_1= 'SORT' ) | (enumLiteral_2= 'SORT_BY_NAME' ) | (enumLiteral_3= 'SORT_BY_ALIGNMENT' ) | (enumLiteral_4= 'SORT_BY_INIT_PRIORITY' ) )
            {
            // InternalLinkerScript.g:6049:2: ( (enumLiteral_0= 'SORT_NONE' ) | (enumLiteral_1= 'SORT' ) | (enumLiteral_2= 'SORT_BY_NAME' ) | (enumLiteral_3= 'SORT_BY_ALIGNMENT' ) | (enumLiteral_4= 'SORT_BY_INIT_PRIORITY' ) )
            int alt86=5;
            switch ( input.LA(1) ) {
            case 97:
                {
                alt86=1;
                }
                break;
            case 98:
                {
                alt86=2;
                }
                break;
            case 54:
                {
                alt86=3;
                }
                break;
            case 99:
                {
                alt86=4;
                }
                break;
            case 100:
                {
                alt86=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                throw nvae;
            }

            switch (alt86) {
                case 1 :
                    // InternalLinkerScript.g:6050:3: (enumLiteral_0= 'SORT_NONE' )
                    {
                    // InternalLinkerScript.g:6050:3: (enumLiteral_0= 'SORT_NONE' )
                    // InternalLinkerScript.g:6051:4: enumLiteral_0= 'SORT_NONE'
                    {
                    enumLiteral_0=(Token)match(input,97,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = grammarAccess.getWildcardSortAccess().getSORT_NONEEnumLiteralDeclaration_0().getEnumLiteral().getInstance();
                      				newLeafNode(enumLiteral_0, grammarAccess.getWildcardSortAccess().getSORT_NONEEnumLiteralDeclaration_0());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:6058:3: (enumLiteral_1= 'SORT' )
                    {
                    // InternalLinkerScript.g:6058:3: (enumLiteral_1= 'SORT' )
                    // InternalLinkerScript.g:6059:4: enumLiteral_1= 'SORT'
                    {
                    enumLiteral_1=(Token)match(input,98,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = grammarAccess.getWildcardSortAccess().getSORTEnumLiteralDeclaration_1().getEnumLiteral().getInstance();
                      				newLeafNode(enumLiteral_1, grammarAccess.getWildcardSortAccess().getSORTEnumLiteralDeclaration_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:6066:3: (enumLiteral_2= 'SORT_BY_NAME' )
                    {
                    // InternalLinkerScript.g:6066:3: (enumLiteral_2= 'SORT_BY_NAME' )
                    // InternalLinkerScript.g:6067:4: enumLiteral_2= 'SORT_BY_NAME'
                    {
                    enumLiteral_2=(Token)match(input,54,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = grammarAccess.getWildcardSortAccess().getSORT_BY_NAMEEnumLiteralDeclaration_2().getEnumLiteral().getInstance();
                      				newLeafNode(enumLiteral_2, grammarAccess.getWildcardSortAccess().getSORT_BY_NAMEEnumLiteralDeclaration_2());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:6074:3: (enumLiteral_3= 'SORT_BY_ALIGNMENT' )
                    {
                    // InternalLinkerScript.g:6074:3: (enumLiteral_3= 'SORT_BY_ALIGNMENT' )
                    // InternalLinkerScript.g:6075:4: enumLiteral_3= 'SORT_BY_ALIGNMENT'
                    {
                    enumLiteral_3=(Token)match(input,99,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = grammarAccess.getWildcardSortAccess().getSORT_BY_ALIGNMENTEnumLiteralDeclaration_3().getEnumLiteral().getInstance();
                      				newLeafNode(enumLiteral_3, grammarAccess.getWildcardSortAccess().getSORT_BY_ALIGNMENTEnumLiteralDeclaration_3());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:6082:3: (enumLiteral_4= 'SORT_BY_INIT_PRIORITY' )
                    {
                    // InternalLinkerScript.g:6082:3: (enumLiteral_4= 'SORT_BY_INIT_PRIORITY' )
                    // InternalLinkerScript.g:6083:4: enumLiteral_4= 'SORT_BY_INIT_PRIORITY'
                    {
                    enumLiteral_4=(Token)match(input,100,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current = grammarAccess.getWildcardSortAccess().getSORT_BY_INIT_PRIORITYEnumLiteralDeclaration_4().getEnumLiteral().getInstance();
                      				newLeafNode(enumLiteral_4, grammarAccess.getWildcardSortAccess().getSORT_BY_INIT_PRIORITYEnumLiteralDeclaration_4());
                      			
                    }

                    }


                    }
                    break;

            }


            }

            if ( state.backtracking==0 ) {

              	leaveRule();

            }
        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleWildcardSort"

    // $ANTLR start synpred1_InternalLinkerScript
    public final void synpred1_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4052:5: ( ( () '?' ( ( ruleLOrExpression ) ) ':' ) )
        // InternalLinkerScript.g:4052:6: ( () '?' ( ( ruleLOrExpression ) ) ':' )
        {
        // InternalLinkerScript.g:4052:6: ( () '?' ( ( ruleLOrExpression ) ) ':' )
        // InternalLinkerScript.g:4053:6: () '?' ( ( ruleLOrExpression ) ) ':'
        {
        // InternalLinkerScript.g:4053:6: ()
        // InternalLinkerScript.g:4054:6: 
        {
        }

        match(input,84,FOLLOW_8); if (state.failed) return ;
        // InternalLinkerScript.g:4056:6: ( ( ruleLOrExpression ) )
        // InternalLinkerScript.g:4057:7: ( ruleLOrExpression )
        {
        // InternalLinkerScript.g:4057:7: ( ruleLOrExpression )
        // InternalLinkerScript.g:4058:8: ruleLOrExpression
        {
        pushFollow(FOLLOW_23);
        ruleLOrExpression();

        state._fsp--;
        if (state.failed) return ;

        }


        }

        match(input,37,FOLLOW_2); if (state.failed) return ;

        }


        }
    }
    // $ANTLR end synpred1_InternalLinkerScript

    // $ANTLR start synpred2_InternalLinkerScript
    public final void synpred2_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4150:5: ( ( () ( ( ruleOpOr ) ) ) )
        // InternalLinkerScript.g:4150:6: ( () ( ( ruleOpOr ) ) )
        {
        // InternalLinkerScript.g:4150:6: ( () ( ( ruleOpOr ) ) )
        // InternalLinkerScript.g:4151:6: () ( ( ruleOpOr ) )
        {
        // InternalLinkerScript.g:4151:6: ()
        // InternalLinkerScript.g:4152:6: 
        {
        }

        // InternalLinkerScript.g:4153:6: ( ( ruleOpOr ) )
        // InternalLinkerScript.g:4154:7: ( ruleOpOr )
        {
        // InternalLinkerScript.g:4154:7: ( ruleOpOr )
        // InternalLinkerScript.g:4155:8: ruleOpOr
        {
        pushFollow(FOLLOW_2);
        ruleOpOr();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred2_InternalLinkerScript

    // $ANTLR start synpred3_InternalLinkerScript
    public final void synpred3_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4260:5: ( ( () ( ( ruleOpAnd ) ) ) )
        // InternalLinkerScript.g:4260:6: ( () ( ( ruleOpAnd ) ) )
        {
        // InternalLinkerScript.g:4260:6: ( () ( ( ruleOpAnd ) ) )
        // InternalLinkerScript.g:4261:6: () ( ( ruleOpAnd ) )
        {
        // InternalLinkerScript.g:4261:6: ()
        // InternalLinkerScript.g:4262:6: 
        {
        }

        // InternalLinkerScript.g:4263:6: ( ( ruleOpAnd ) )
        // InternalLinkerScript.g:4264:7: ( ruleOpAnd )
        {
        // InternalLinkerScript.g:4264:7: ( ruleOpAnd )
        // InternalLinkerScript.g:4265:8: ruleOpAnd
        {
        pushFollow(FOLLOW_2);
        ruleOpAnd();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred3_InternalLinkerScript

    // $ANTLR start synpred4_InternalLinkerScript
    public final void synpred4_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4370:5: ( ( () ( ( ruleOpBitwiseOr ) ) ) )
        // InternalLinkerScript.g:4370:6: ( () ( ( ruleOpBitwiseOr ) ) )
        {
        // InternalLinkerScript.g:4370:6: ( () ( ( ruleOpBitwiseOr ) ) )
        // InternalLinkerScript.g:4371:6: () ( ( ruleOpBitwiseOr ) )
        {
        // InternalLinkerScript.g:4371:6: ()
        // InternalLinkerScript.g:4372:6: 
        {
        }

        // InternalLinkerScript.g:4373:6: ( ( ruleOpBitwiseOr ) )
        // InternalLinkerScript.g:4374:7: ( ruleOpBitwiseOr )
        {
        // InternalLinkerScript.g:4374:7: ( ruleOpBitwiseOr )
        // InternalLinkerScript.g:4375:8: ruleOpBitwiseOr
        {
        pushFollow(FOLLOW_2);
        ruleOpBitwiseOr();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred4_InternalLinkerScript

    // $ANTLR start synpred5_InternalLinkerScript
    public final void synpred5_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4480:5: ( ( () ( ( ruleOpBitwiseAnd ) ) ) )
        // InternalLinkerScript.g:4480:6: ( () ( ( ruleOpBitwiseAnd ) ) )
        {
        // InternalLinkerScript.g:4480:6: ( () ( ( ruleOpBitwiseAnd ) ) )
        // InternalLinkerScript.g:4481:6: () ( ( ruleOpBitwiseAnd ) )
        {
        // InternalLinkerScript.g:4481:6: ()
        // InternalLinkerScript.g:4482:6: 
        {
        }

        // InternalLinkerScript.g:4483:6: ( ( ruleOpBitwiseAnd ) )
        // InternalLinkerScript.g:4484:7: ( ruleOpBitwiseAnd )
        {
        // InternalLinkerScript.g:4484:7: ( ruleOpBitwiseAnd )
        // InternalLinkerScript.g:4485:8: ruleOpBitwiseAnd
        {
        pushFollow(FOLLOW_2);
        ruleOpBitwiseAnd();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred5_InternalLinkerScript

    // $ANTLR start synpred6_InternalLinkerScript
    public final void synpred6_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4590:5: ( ( () ( ( ruleOpEquality ) ) ) )
        // InternalLinkerScript.g:4590:6: ( () ( ( ruleOpEquality ) ) )
        {
        // InternalLinkerScript.g:4590:6: ( () ( ( ruleOpEquality ) ) )
        // InternalLinkerScript.g:4591:6: () ( ( ruleOpEquality ) )
        {
        // InternalLinkerScript.g:4591:6: ()
        // InternalLinkerScript.g:4592:6: 
        {
        }

        // InternalLinkerScript.g:4593:6: ( ( ruleOpEquality ) )
        // InternalLinkerScript.g:4594:7: ( ruleOpEquality )
        {
        // InternalLinkerScript.g:4594:7: ( ruleOpEquality )
        // InternalLinkerScript.g:4595:8: ruleOpEquality
        {
        pushFollow(FOLLOW_2);
        ruleOpEquality();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred6_InternalLinkerScript

    // $ANTLR start synpred7_InternalLinkerScript
    public final void synpred7_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4708:5: ( ( () ( ( ruleOpCompare ) ) ) )
        // InternalLinkerScript.g:4708:6: ( () ( ( ruleOpCompare ) ) )
        {
        // InternalLinkerScript.g:4708:6: ( () ( ( ruleOpCompare ) ) )
        // InternalLinkerScript.g:4709:6: () ( ( ruleOpCompare ) )
        {
        // InternalLinkerScript.g:4709:6: ()
        // InternalLinkerScript.g:4710:6: 
        {
        }

        // InternalLinkerScript.g:4711:6: ( ( ruleOpCompare ) )
        // InternalLinkerScript.g:4712:7: ( ruleOpCompare )
        {
        // InternalLinkerScript.g:4712:7: ( ruleOpCompare )
        // InternalLinkerScript.g:4713:8: ruleOpCompare
        {
        pushFollow(FOLLOW_2);
        ruleOpCompare();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred7_InternalLinkerScript

    // $ANTLR start synpred8_InternalLinkerScript
    public final void synpred8_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4845:5: ( ( () ( ( ruleOpOther ) ) ) )
        // InternalLinkerScript.g:4845:6: ( () ( ( ruleOpOther ) ) )
        {
        // InternalLinkerScript.g:4845:6: ( () ( ( ruleOpOther ) ) )
        // InternalLinkerScript.g:4846:6: () ( ( ruleOpOther ) )
        {
        // InternalLinkerScript.g:4846:6: ()
        // InternalLinkerScript.g:4847:6: 
        {
        }

        // InternalLinkerScript.g:4848:6: ( ( ruleOpOther ) )
        // InternalLinkerScript.g:4849:7: ( ruleOpOther )
        {
        // InternalLinkerScript.g:4849:7: ( ruleOpOther )
        // InternalLinkerScript.g:4850:8: ruleOpOther
        {
        pushFollow(FOLLOW_2);
        ruleOpOther();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred8_InternalLinkerScript

    // $ANTLR start synpred11_InternalLinkerScript
    public final void synpred11_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4983:5: ( ( () ( ( ruleOpAdd ) ) ) )
        // InternalLinkerScript.g:4983:6: ( () ( ( ruleOpAdd ) ) )
        {
        // InternalLinkerScript.g:4983:6: ( () ( ( ruleOpAdd ) ) )
        // InternalLinkerScript.g:4984:6: () ( ( ruleOpAdd ) )
        {
        // InternalLinkerScript.g:4984:6: ()
        // InternalLinkerScript.g:4985:6: 
        {
        }

        // InternalLinkerScript.g:4986:6: ( ( ruleOpAdd ) )
        // InternalLinkerScript.g:4987:7: ( ruleOpAdd )
        {
        // InternalLinkerScript.g:4987:7: ( ruleOpAdd )
        // InternalLinkerScript.g:4988:8: ruleOpAdd
        {
        pushFollow(FOLLOW_2);
        ruleOpAdd();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred11_InternalLinkerScript

    // $ANTLR start synpred12_InternalLinkerScript
    public final void synpred12_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:5101:5: ( ( () ( ( ruleOpMulti ) ) ) )
        // InternalLinkerScript.g:5101:6: ( () ( ( ruleOpMulti ) ) )
        {
        // InternalLinkerScript.g:5101:6: ( () ( ( ruleOpMulti ) ) )
        // InternalLinkerScript.g:5102:6: () ( ( ruleOpMulti ) )
        {
        // InternalLinkerScript.g:5102:6: ()
        // InternalLinkerScript.g:5103:6: 
        {
        }

        // InternalLinkerScript.g:5104:6: ( ( ruleOpMulti ) )
        // InternalLinkerScript.g:5105:7: ( ruleOpMulti )
        {
        // InternalLinkerScript.g:5105:7: ( ruleOpMulti )
        // InternalLinkerScript.g:5106:8: ruleOpMulti
        {
        pushFollow(FOLLOW_2);
        ruleOpMulti();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred12_InternalLinkerScript

    // Delegated rules

    public final boolean synpred12_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred11_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_InternalLinkerScript_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA35 dfa35 = new DFA35(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA56 dfa56 = new DFA56(this);
    protected DFA74 dfa74 = new DFA74(this);
    protected DFA76 dfa76 = new DFA76(this);
    static final String dfa_1s = "\24\uffff";
    static final String dfa_2s = "\1\uffff\7\20\14\uffff";
    static final String dfa_3s = "\10\4\3\uffff\1\15\6\uffff\1\4\1\uffff";
    static final String dfa_4s = "\10\144\3\uffff\1\15\6\uffff\1\144\1\uffff";
    static final String dfa_5s = "\10\uffff\1\1\1\2\1\3\1\uffff\1\5\1\6\1\7\1\10\1\11\1\12\1\uffff\1\4";
    static final String dfa_6s = "\24\uffff}>";
    static final String[] dfa_7s = {
            "\1\2\5\uffff\1\21\5\uffff\1\16\15\uffff\1\17\25\uffff\1\11\1\12\1\13\1\15\5\14\3\10\10\uffff\1\20\1\uffff\2\20\1\3\1\uffff\1\5\1\4\1\uffff\1\7\1\6\11\uffff\1\1\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "\1\20\5\uffff\1\20\2\uffff\1\20\2\uffff\1\20\15\uffff\1\20\4\uffff\1\20\4\uffff\2\10\12\uffff\14\20\5\10\1\uffff\2\10\1\20\1\uffff\3\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            "",
            "",
            "",
            "\1\22",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\20\60\uffff\1\23\1\20\24\uffff\2\20\1\uffff\2\20\1\uffff\2\20\11\uffff\1\20\4\uffff\4\20",
            ""
    };

    static final short[] dfa_1 = DFA.unpackEncodedString(dfa_1s);
    static final short[] dfa_2 = DFA.unpackEncodedString(dfa_2s);
    static final char[] dfa_3 = DFA.unpackEncodedStringToUnsignedChars(dfa_3s);
    static final char[] dfa_4 = DFA.unpackEncodedStringToUnsignedChars(dfa_4s);
    static final short[] dfa_5 = DFA.unpackEncodedString(dfa_5s);
    static final short[] dfa_6 = DFA.unpackEncodedString(dfa_6s);
    static final short[][] dfa_7 = unpackEncodedStringArray(dfa_7s);

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = dfa_1;
            this.eof = dfa_2;
            this.min = dfa_3;
            this.max = dfa_4;
            this.accept = dfa_5;
            this.special = dfa_6;
            this.transition = dfa_7;
        }
        public String getDescription() {
            return "1918:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ';' | otherlv_3= ',' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )";
        }
    }
    static final String dfa_8s = "\71\uffff";
    static final String dfa_9s = "\2\uffff\7\14\60\uffff";
    static final String dfa_10s = "\1\4\1\15\7\4\1\uffff\1\15\1\4\1\uffff\1\4\7\16\10\15\1\uffff\3\4\1\uffff\16\16\2\4\7\16";
    static final String dfa_11s = "\1\144\1\15\7\144\1\uffff\1\15\1\134\1\uffff\1\144\7\111\1\15\7\16\1\uffff\1\134\1\144\1\134\1\uffff\16\111\1\134\1\144\7\111";
    static final String dfa_12s = "\11\uffff\1\2\2\uffff\1\1\20\uffff\1\4\3\uffff\1\3\27\uffff";
    static final String dfa_13s = "\71\uffff}>";
    static final String[] dfa_14s = {
            "\1\3\61\uffff\1\11\21\uffff\1\1\1\uffff\1\12\1\11\1\4\1\uffff\1\6\1\5\1\uffff\1\10\1\7\11\uffff\1\2\4\uffff\4\11",
            "\1\13",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "\1\14\5\uffff\1\14\2\uffff\1\11\2\uffff\1\14\15\uffff\1\14\4\uffff\1\14\20\uffff\14\14\10\uffff\1\14\1\uffff\3\14\1\uffff\2\14\1\uffff\2\14\11\uffff\1\14\4\uffff\4\14",
            "",
            "\1\15",
            "\1\17\107\uffff\1\20\1\uffff\1\22\1\21\1\uffff\1\24\1\23\11\uffff\1\16",
            "",
            "\1\27\61\uffff\1\35\21\uffff\1\25\2\uffff\1\35\1\30\1\uffff\1\32\1\31\1\uffff\1\34\1\33\11\uffff\1\26\4\uffff\4\35",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\40",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "",
            "\1\43\107\uffff\1\44\1\uffff\1\46\1\45\1\uffff\1\50\1\47\11\uffff\1\42",
            "\1\3\61\uffff\1\11\24\uffff\1\11\1\4\1\uffff\1\6\1\5\1\uffff\1\10\1\7\11\uffff\1\2\4\uffff\4\11",
            "\1\52\107\uffff\1\53\1\uffff\1\55\1\54\1\uffff\1\57\1\56\11\uffff\1\51",
            "",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\37\72\uffff\1\36",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\63\107\uffff\1\64\1\uffff\1\66\1\65\1\uffff\1\70\1\67\11\uffff\1\62",
            "\1\27\61\uffff\1\35\24\uffff\1\35\1\30\1\uffff\1\32\1\31\1\uffff\1\34\1\33\11\uffff\1\26\4\uffff\4\35",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60",
            "\1\61\72\uffff\1\60"
    };

    static final short[] dfa_8 = DFA.unpackEncodedString(dfa_8s);
    static final short[] dfa_9 = DFA.unpackEncodedString(dfa_9s);
    static final char[] dfa_10 = DFA.unpackEncodedStringToUnsignedChars(dfa_10s);
    static final char[] dfa_11 = DFA.unpackEncodedStringToUnsignedChars(dfa_11s);
    static final short[] dfa_12 = DFA.unpackEncodedString(dfa_12s);
    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final short[][] dfa_14 = unpackEncodedStringArray(dfa_14s);

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = dfa_8;
            this.eof = dfa_9;
            this.min = dfa_10;
            this.max = dfa_11;
            this.accept = dfa_12;
            this.special = dfa_13;
            this.transition = dfa_14;
        }
        public String getDescription() {
            return "2890:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcard ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcard ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcard ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcard ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcard ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcard ) ) )* otherlv_46= ')' otherlv_47= ')' ) )";
        }
    }
    static final String dfa_15s = "\14\uffff";
    static final String dfa_16s = "\1\4\2\uffff\5\15\1\4\3\uffff";
    static final String dfa_17s = "\1\144\2\uffff\5\15\1\144\3\uffff";
    static final String dfa_18s = "\1\uffff\1\1\1\2\6\uffff\1\3\1\4\1\5";
    static final String dfa_19s = "\14\uffff}>";
    static final String[] dfa_20s = {
            "\1\1\61\uffff\1\5\24\uffff\1\2\1\1\1\uffff\2\1\1\uffff\2\1\11\uffff\1\1\4\uffff\1\3\1\4\1\6\1\7",
            "",
            "",
            "\1\10",
            "\1\10",
            "\1\10",
            "\1\10",
            "\1\10",
            "\1\11\61\uffff\1\12\24\uffff\1\13\1\11\1\uffff\2\11\1\uffff\2\11\11\uffff\1\11\4\uffff\4\12",
            "",
            "",
            ""
    };

    static final short[] dfa_15 = DFA.unpackEncodedString(dfa_15s);
    static final char[] dfa_16 = DFA.unpackEncodedStringToUnsignedChars(dfa_16s);
    static final char[] dfa_17 = DFA.unpackEncodedStringToUnsignedChars(dfa_17s);
    static final short[] dfa_18 = DFA.unpackEncodedString(dfa_18s);
    static final short[] dfa_19 = DFA.unpackEncodedString(dfa_19s);
    static final short[][] dfa_20 = unpackEncodedStringArray(dfa_20s);

    class DFA56 extends DFA {

        public DFA56(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 56;
            this.eot = dfa_15;
            this.eof = dfa_15;
            this.min = dfa_16;
            this.max = dfa_17;
            this.accept = dfa_18;
            this.special = dfa_19;
            this.transition = dfa_20;
        }
        public String getDescription() {
            return "3408:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ( (lv_primarySort_9_0= ruleWildcardSort ) ) otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () ( (lv_primarySort_14_0= ruleWildcardSort ) ) otherlv_15= '(' ( (lv_secondarySort_16_0= ruleWildcardSort ) ) otherlv_17= '(' ( (lv_name_18_0= ruleWildID ) ) otherlv_19= ')' otherlv_20= ')' ) | ( () ( (lv_primarySort_22_0= ruleWildcardSort ) ) otherlv_23= '(' otherlv_24= 'EXCLUDE_FILE' otherlv_25= '(' ( (lv_excludes_26_0= ruleWildID ) )+ otherlv_27= ')' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' ) )";
        }
    }
    static final String dfa_21s = "\52\uffff";
    static final String dfa_22s = "\1\1\51\uffff";
    static final String dfa_23s = "\1\4\15\uffff\2\0\32\uffff";
    static final String dfa_24s = "\1\140\15\uffff\2\0\32\uffff";
    static final String dfa_25s = "\1\uffff\1\2\47\uffff\1\1";
    static final String dfa_26s = "\16\uffff\1\0\1\1\32\uffff}>";
    static final String[] dfa_27s = {
            "\3\1\3\uffff\2\1\1\uffff\4\1\22\uffff\1\1\1\uffff\2\1\1\uffff\1\1\1\uffff\1\1\22\uffff\3\1\4\uffff\2\1\3\uffff\1\1\2\uffff\16\1\1\17\1\16\1\1\2\uffff\2\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_21 = DFA.unpackEncodedString(dfa_21s);
    static final short[] dfa_22 = DFA.unpackEncodedString(dfa_22s);
    static final char[] dfa_23 = DFA.unpackEncodedStringToUnsignedChars(dfa_23s);
    static final char[] dfa_24 = DFA.unpackEncodedStringToUnsignedChars(dfa_24s);
    static final short[] dfa_25 = DFA.unpackEncodedString(dfa_25s);
    static final short[] dfa_26 = DFA.unpackEncodedString(dfa_26s);
    static final short[][] dfa_27 = unpackEncodedStringArray(dfa_27s);

    class DFA74 extends DFA {

        public DFA74(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 74;
            this.eot = dfa_21;
            this.eof = dfa_22;
            this.min = dfa_23;
            this.max = dfa_24;
            this.accept = dfa_25;
            this.special = dfa_26;
            this.transition = dfa_27;
        }
        public String getDescription() {
            return "()* loopback of 4981:3: ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA74_14 = input.LA(1);

                         
                        int index74_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_InternalLinkerScript()) ) {s = 41;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index74_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA74_15 = input.LA(1);

                         
                        int index74_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_InternalLinkerScript()) ) {s = 41;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index74_15);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 74, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_28s = "\27\uffff";
    static final String dfa_29s = "\1\1\26\uffff";
    static final String dfa_30s = "\1\4\1\uffff\1\4\24\uffff";
    static final String dfa_31s = "\1\140\1\uffff\1\140\24\uffff";
    static final String dfa_32s = "\1\uffff\1\2\1\uffff\24\1";
    static final String dfa_33s = "\1\0\1\uffff\1\1\24\uffff}>";
    static final String[] dfa_34s = {
            "\3\1\3\uffff\2\1\1\uffff\4\1\22\uffff\1\1\1\uffff\2\1\1\uffff\1\1\1\uffff\1\1\22\uffff\3\1\4\uffff\2\1\3\uffff\1\1\2\uffff\20\1\1\2\1\3\1\4\2\1",
            "",
            "\1\21\1\16\1\17\6\uffff\1\20\30\uffff\1\15\1\uffff\2\1\1\13\25\uffff\5\1\1\uffff\2\1\4\uffff\1\22\1\12\1\24\1\23\1\11\1\26\1\25\1\5\6\uffff\1\7\1\6\3\uffff\1\10\1\14",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] dfa_28 = DFA.unpackEncodedString(dfa_28s);
    static final short[] dfa_29 = DFA.unpackEncodedString(dfa_29s);
    static final char[] dfa_30 = DFA.unpackEncodedStringToUnsignedChars(dfa_30s);
    static final char[] dfa_31 = DFA.unpackEncodedStringToUnsignedChars(dfa_31s);
    static final short[] dfa_32 = DFA.unpackEncodedString(dfa_32s);
    static final short[] dfa_33 = DFA.unpackEncodedString(dfa_33s);
    static final short[][] dfa_34 = unpackEncodedStringArray(dfa_34s);

    class DFA76 extends DFA {

        public DFA76(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 76;
            this.eot = dfa_28;
            this.eof = dfa_29;
            this.min = dfa_30;
            this.max = dfa_31;
            this.accept = dfa_32;
            this.special = dfa_33;
            this.transition = dfa_34;
        }
        public String getDescription() {
            return "()* loopback of 5099:3: ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA76_0 = input.LA(1);

                         
                        int index76_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA76_0==EOF||(LA76_0>=RULE_ID && LA76_0<=RULE_HEX)||(LA76_0>=10 && LA76_0<=11)||(LA76_0>=13 && LA76_0<=16)||LA76_0==35||(LA76_0>=37 && LA76_0<=38)||LA76_0==40||LA76_0==42||(LA76_0>=61 && LA76_0<=63)||(LA76_0>=68 && LA76_0<=69)||LA76_0==73||(LA76_0>=76 && LA76_0<=91)||(LA76_0>=95 && LA76_0<=96)) ) {s = 1;}

                        else if ( (LA76_0==92) ) {s = 2;}

                        else if ( (LA76_0==93) && (synpred12_InternalLinkerScript())) {s = 3;}

                        else if ( (LA76_0==94) && (synpred12_InternalLinkerScript())) {s = 4;}

                         
                        input.seek(index76_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA76_2 = input.LA(1);

                         
                        int index76_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA76_2>=40 && LA76_2<=41)||(LA76_2>=64 && LA76_2<=68)||(LA76_2>=70 && LA76_2<=71)) ) {s = 1;}

                        else if ( (LA76_2==83) && (synpred12_InternalLinkerScript())) {s = 5;}

                        else if ( (LA76_2==91) && (synpred12_InternalLinkerScript())) {s = 6;}

                        else if ( (LA76_2==90) && (synpred12_InternalLinkerScript())) {s = 7;}

                        else if ( (LA76_2==95) && (synpred12_InternalLinkerScript())) {s = 8;}

                        else if ( (LA76_2==80) && (synpred12_InternalLinkerScript())) {s = 9;}

                        else if ( (LA76_2==77) && (synpred12_InternalLinkerScript())) {s = 10;}

                        else if ( (LA76_2==42) && (synpred12_InternalLinkerScript())) {s = 11;}

                        else if ( (LA76_2==96) && (synpred12_InternalLinkerScript())) {s = 12;}

                        else if ( (LA76_2==38) && (synpred12_InternalLinkerScript())) {s = 13;}

                        else if ( (LA76_2==RULE_DEC) && (synpred12_InternalLinkerScript())) {s = 14;}

                        else if ( (LA76_2==RULE_HEX) && (synpred12_InternalLinkerScript())) {s = 15;}

                        else if ( (LA76_2==13) && (synpred12_InternalLinkerScript())) {s = 16;}

                        else if ( (LA76_2==RULE_ID) && (synpred12_InternalLinkerScript())) {s = 17;}

                        else if ( (LA76_2==76) && (synpred12_InternalLinkerScript())) {s = 18;}

                        else if ( (LA76_2==79) && (synpred12_InternalLinkerScript())) {s = 19;}

                        else if ( (LA76_2==78) && (synpred12_InternalLinkerScript())) {s = 20;}

                        else if ( (LA76_2==82) && (synpred12_InternalLinkerScript())) {s = 21;}

                        else if ( (LA76_2==81) && (synpred12_InternalLinkerScript())) {s = 22;}

                         
                        input.seek(index76_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 76, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0xE00000127FFF9412L,0x000000001006D000L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000000010L,0x000000001006D000L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000044000002070L,0x000000019C0FF000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000004800L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000180000010L,0x000000001006D000L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000100000010L,0x000000001006D000L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000100004810L,0x000000001006D000L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000000004010L,0x000000001006D000L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000100000812L,0x000000001006D000L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0000000800000010L,0x000000001006D000L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000044000002470L,0x000000019C0FF000L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0xE000000800018410L,0x000000001006D000L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000046000002070L,0x000000019C0FF000L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000002000002000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x000F800000000000L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x00007CC400000000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x00007C8400000000L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000708400000000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000700400000000L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0xFFF0000840010410L,0x0000001E1006DD00L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0000036000000802L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000026000000802L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000022000000802L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0000030000000000L,0x00000000000000DFL});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000200L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x0040000000000010L,0x0000001E1006D900L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0040000000004810L,0x0000001E1006D900L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x0000000000000010L,0x000000001006D100L});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x0040000000000000L,0x0000001E00000000L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000000L,0x000000000000E000L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000000000L,0x0000000000070000L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000010L,0x00000000100ED000L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000000000004010L,0x00000000100ED000L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100000L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x0000000000000002L,0x0000000000200000L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000000002L,0x0000000003000000L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000010000000002L,0x0000000000000030L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000010000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000000000000002L,0x000000000C000000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000000000000002L,0x0000000070000000L});

}