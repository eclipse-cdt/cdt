package org.eclipse.cdt.linkerscript.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
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
@SuppressWarnings("all")
public class InternalLinkerScriptParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_DEC", "RULE_HEX", "RULE_ML_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'SECTIONS'", "'{'", "'}'", "'('", "')'", "':'", "'AT'", "'SUBALIGN'", "'>'", "'='", "','", "'ALIGN'", "'ALIGN_WITH_INPUT'", "'ONLY_IF_RO'", "'ONLY_IF_RW'", "'SPECIAL'", "'NOLOAD'", "'DSECT'", "'COPY'", "'INFO'", "'OVERLAY'", "';'", "'CREATE_OBJECT_SYMBOLS'", "'CONSTRUCTORS'", "'SORT_BY_NAME'", "'FILL'", "'ASSERT'", "'INCLUDE'", "'BYTE'", "'SHORT'", "'LONG'", "'QUAD'", "'SQUAD'", "'HIDDEN'", "'PROVIDE'", "'PROVIDE_HIDDEN'", "'+='", "'-='", "'*='", "'/='", "'<'", "'>='", "'&='", "'|='", "'INPUT_SECTION_FLAGS'", "'&'", "'KEEP'", "'EXCLUDE_FILE'", "'SORT_BY_ALIGNMENT'", "'SORT_NONE'", "'SORT_BY_INIT_PRIORITY'", "'SORT'", "'MEMORY'", "'ORIGIN'", "'org'", "'o'", "'LENGTH'", "'len'", "'l'", "'!'", "'||'", "'&&'", "'|'", "'=='", "'!='", "'+'", "'-'", "'*'", "'/'", "'%'", "'~'", "'++'", "'--'"
    };
    public static final int RULE_HEX=6;
    public static final int T__50=50;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__59=59;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__55=55;
    public static final int T__12=12;
    public static final int T__56=56;
    public static final int T__13=13;
    public static final int T__57=57;
    public static final int T__14=14;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__10=10;
    public static final int T__54=54;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int RULE_ID=4;
    public static final int RULE_DEC=5;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__23=23;
    public static final int T__67=67;
    public static final int T__24=24;
    public static final int T__68=68;
    public static final int T__25=25;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__20=20;
    public static final int T__64=64;
    public static final int T__21=21;
    public static final int T__65=65;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__33=33;
    public static final int T__77=77;
    public static final int T__34=34;
    public static final int T__78=78;
    public static final int T__35=35;
    public static final int T__79=79;
    public static final int T__36=36;
    public static final int T__73=73;
    public static final int EOF=-1;
    public static final int T__30=30;
    public static final int T__74=74;
    public static final int T__31=31;
    public static final int T__75=75;
    public static final int T__32=32;
    public static final int T__76=76;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int RULE_WS=8;
    public static final int RULE_ANY_OTHER=9;
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
    // InternalLinkerScript.g:64:1: entryRuleLinkerScript returns [EObject current=null] : iv_ruleLinkerScript= ruleLinkerScript EOF ;
    public final EObject entryRuleLinkerScript() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLinkerScript = null;


        try {
            // InternalLinkerScript.g:64:53: (iv_ruleLinkerScript= ruleLinkerScript EOF )
            // InternalLinkerScript.g:65:2: iv_ruleLinkerScript= ruleLinkerScript EOF
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
    // InternalLinkerScript.g:71:1: ruleLinkerScript returns [EObject current=null] : ( () ( (lv_memories_1_0= ruleMemoryCommand ) )? ( (lv_sections_2_0= ruleSectionsCommand ) )? ) ;
    public final EObject ruleLinkerScript() throws RecognitionException {
        EObject current = null;

        EObject lv_memories_1_0 = null;

        EObject lv_sections_2_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:77:2: ( ( () ( (lv_memories_1_0= ruleMemoryCommand ) )? ( (lv_sections_2_0= ruleSectionsCommand ) )? ) )
            // InternalLinkerScript.g:78:2: ( () ( (lv_memories_1_0= ruleMemoryCommand ) )? ( (lv_sections_2_0= ruleSectionsCommand ) )? )
            {
            // InternalLinkerScript.g:78:2: ( () ( (lv_memories_1_0= ruleMemoryCommand ) )? ( (lv_sections_2_0= ruleSectionsCommand ) )? )
            // InternalLinkerScript.g:79:3: () ( (lv_memories_1_0= ruleMemoryCommand ) )? ( (lv_sections_2_0= ruleSectionsCommand ) )?
            {
            // InternalLinkerScript.g:79:3: ()
            // InternalLinkerScript.g:80:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLinkerScriptAccess().getLinkerScriptAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:86:3: ( (lv_memories_1_0= ruleMemoryCommand ) )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==62) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // InternalLinkerScript.g:87:4: (lv_memories_1_0= ruleMemoryCommand )
                    {
                    // InternalLinkerScript.g:87:4: (lv_memories_1_0= ruleMemoryCommand )
                    // InternalLinkerScript.g:88:5: lv_memories_1_0= ruleMemoryCommand
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getLinkerScriptAccess().getMemoriesMemoryCommandParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_3);
                    lv_memories_1_0=ruleMemoryCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getLinkerScriptRule());
                      					}
                      					set(
                      						current,
                      						"memories",
                      						lv_memories_1_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.MemoryCommand");
                      					afterParserOrEnumRuleCall();
                      				
                    }

                    }


                    }
                    break;

            }

            // InternalLinkerScript.g:105:3: ( (lv_sections_2_0= ruleSectionsCommand ) )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==10) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // InternalLinkerScript.g:106:4: (lv_sections_2_0= ruleSectionsCommand )
                    {
                    // InternalLinkerScript.g:106:4: (lv_sections_2_0= ruleSectionsCommand )
                    // InternalLinkerScript.g:107:5: lv_sections_2_0= ruleSectionsCommand
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getLinkerScriptAccess().getSectionsSectionsCommandParserRuleCall_2_0());
                      				
                    }
                    pushFollow(FOLLOW_2);
                    lv_sections_2_0=ruleSectionsCommand();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					if (current==null) {
                      						current = createModelElementForParent(grammarAccess.getLinkerScriptRule());
                      					}
                      					set(
                      						current,
                      						"sections",
                      						lv_sections_2_0,
                      						"org.eclipse.cdt.linkerscript.LinkerScript.SectionsCommand");
                      					afterParserOrEnumRuleCall();
                      				
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
    // $ANTLR end "ruleLinkerScript"


    // $ANTLR start "entryRuleSectionsCommand"
    // InternalLinkerScript.g:128:1: entryRuleSectionsCommand returns [EObject current=null] : iv_ruleSectionsCommand= ruleSectionsCommand EOF ;
    public final EObject entryRuleSectionsCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSectionsCommand = null;


        try {
            // InternalLinkerScript.g:128:56: (iv_ruleSectionsCommand= ruleSectionsCommand EOF )
            // InternalLinkerScript.g:129:2: iv_ruleSectionsCommand= ruleSectionsCommand EOF
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
    // InternalLinkerScript.g:135:1: ruleSectionsCommand returns [EObject current=null] : (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sections_3_0= ruleOutputSection ) )* otherlv_4= '}' ) ;
    public final EObject ruleSectionsCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_sections_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:141:2: ( (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sections_3_0= ruleOutputSection ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:142:2: (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sections_3_0= ruleOutputSection ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:142:2: (otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sections_3_0= ruleOutputSection ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:143:3: otherlv_0= 'SECTIONS' otherlv_1= '{' () ( (lv_sections_3_0= ruleOutputSection ) )* otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,10,FOLLOW_4); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getSectionsCommandAccess().getSECTIONSKeyword_0());
              		
            }
            otherlv_1=(Token)match(input,11,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getSectionsCommandAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalLinkerScript.g:151:3: ()
            // InternalLinkerScript.g:152:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getSectionsCommandAccess().getSectionsCommandAction_2(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:158:3: ( (lv_sections_3_0= ruleOutputSection ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==RULE_ID||LA3_0==62||(LA3_0>=64 && LA3_0<=65)||(LA3_0>=67 && LA3_0<=68)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // InternalLinkerScript.g:159:4: (lv_sections_3_0= ruleOutputSection )
            	    {
            	    // InternalLinkerScript.g:159:4: (lv_sections_3_0= ruleOutputSection )
            	    // InternalLinkerScript.g:160:5: lv_sections_3_0= ruleOutputSection
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getSectionsCommandAccess().getSectionsOutputSectionParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_5);
            	    lv_sections_3_0=ruleOutputSection();

            	    state._fsp--;
            	    if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      					if (current==null) {
            	      						current = createModelElementForParent(grammarAccess.getSectionsCommandRule());
            	      					}
            	      					add(
            	      						current,
            	      						"sections",
            	      						lv_sections_3_0,
            	      						"org.eclipse.cdt.linkerscript.LinkerScript.OutputSection");
            	      					afterParserOrEnumRuleCall();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            otherlv_4=(Token)match(input,12,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleOutputSection"
    // InternalLinkerScript.g:185:1: entryRuleOutputSection returns [EObject current=null] : iv_ruleOutputSection= ruleOutputSection EOF ;
    public final EObject entryRuleOutputSection() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSection = null;


        try {
            // InternalLinkerScript.g:185:54: (iv_ruleOutputSection= ruleOutputSection EOF )
            // InternalLinkerScript.g:186:2: iv_ruleOutputSection= ruleOutputSection EOF
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
    // InternalLinkerScript.g:192:1: ruleOutputSection returns [EObject current=null] : ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? ) ;
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
            // InternalLinkerScript.g:198:2: ( ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? ) )
            // InternalLinkerScript.g:199:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? )
            {
            // InternalLinkerScript.g:199:2: ( ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )? )
            // InternalLinkerScript.g:200:3: ( (lv_name_0_0= ruleValidID ) ) ( (lv_address_1_0= ruleLExpression ) )? (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )? otherlv_5= ':' (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )? ( (lv_align_10_0= ruleOutputSectionAlign ) )? (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )? ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )? otherlv_16= '{' ( (lv_statements_17_0= ruleStatement ) )* otherlv_18= '}' (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )? (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )? (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )* (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )? (otherlv_28= ',' )?
            {
            // InternalLinkerScript.g:200:3: ( (lv_name_0_0= ruleValidID ) )
            // InternalLinkerScript.g:201:4: (lv_name_0_0= ruleValidID )
            {
            // InternalLinkerScript.g:201:4: (lv_name_0_0= ruleValidID )
            // InternalLinkerScript.g:202:5: lv_name_0_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getOutputSectionAccess().getNameValidIDParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_6);
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

            // InternalLinkerScript.g:219:3: ( (lv_address_1_0= ruleLExpression ) )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=RULE_ID && LA4_0<=RULE_HEX)||LA4_0==21||LA4_0==62||(LA4_0>=64 && LA4_0<=69)||(LA4_0>=75 && LA4_0<=76)||LA4_0==80) ) {
                alt4=1;
            }
            else if ( (LA4_0==13) ) {
                int LA4_2 = input.LA(2);

                if ( ((LA4_2>=RULE_ID && LA4_2<=RULE_HEX)||LA4_2==13||LA4_2==21||LA4_2==62||(LA4_2>=64 && LA4_2<=69)||(LA4_2>=75 && LA4_2<=76)||LA4_2==80) ) {
                    alt4=1;
                }
            }
            switch (alt4) {
                case 1 :
                    // InternalLinkerScript.g:220:4: (lv_address_1_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:220:4: (lv_address_1_0= ruleLExpression )
                    // InternalLinkerScript.g:221:5: lv_address_1_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getAddressLExpressionParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_7);
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

            // InternalLinkerScript.g:238:3: (otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')' )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==13) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // InternalLinkerScript.g:239:4: otherlv_2= '(' ( (lv_type_3_0= ruleOutputSectionType ) ) otherlv_4= ')'
                    {
                    otherlv_2=(Token)match(input,13,FOLLOW_8); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_2_0());
                      			
                    }
                    // InternalLinkerScript.g:243:4: ( (lv_type_3_0= ruleOutputSectionType ) )
                    // InternalLinkerScript.g:244:5: (lv_type_3_0= ruleOutputSectionType )
                    {
                    // InternalLinkerScript.g:244:5: (lv_type_3_0= ruleOutputSectionType )
                    // InternalLinkerScript.g:245:6: lv_type_3_0= ruleOutputSectionType
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getTypeOutputSectionTypeParserRuleCall_2_1_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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

                    otherlv_4=(Token)match(input,14,FOLLOW_10); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_2_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_5=(Token)match(input,15,FOLLOW_11); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_5, grammarAccess.getOutputSectionAccess().getColonKeyword_3());
              		
            }
            // InternalLinkerScript.g:271:3: (otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==16) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // InternalLinkerScript.g:272:4: otherlv_6= 'AT' otherlv_7= '(' ( (lv_at_8_0= ruleLExpression ) ) otherlv_9= ')'
                    {
                    otherlv_6=(Token)match(input,16,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getOutputSectionAccess().getATKeyword_4_0());
                      			
                    }
                    otherlv_7=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_4_1());
                      			
                    }
                    // InternalLinkerScript.g:280:4: ( (lv_at_8_0= ruleLExpression ) )
                    // InternalLinkerScript.g:281:5: (lv_at_8_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:281:5: (lv_at_8_0= ruleLExpression )
                    // InternalLinkerScript.g:282:6: lv_at_8_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getAtLExpressionParserRuleCall_4_2_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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

                    otherlv_9=(Token)match(input,14,FOLLOW_14); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_4_3());
                      			
                    }

                    }
                    break;

            }

            // InternalLinkerScript.g:304:3: ( (lv_align_10_0= ruleOutputSectionAlign ) )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=21 && LA7_0<=22)) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // InternalLinkerScript.g:305:4: (lv_align_10_0= ruleOutputSectionAlign )
                    {
                    // InternalLinkerScript.g:305:4: (lv_align_10_0= ruleOutputSectionAlign )
                    // InternalLinkerScript.g:306:5: lv_align_10_0= ruleOutputSectionAlign
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getAlignOutputSectionAlignParserRuleCall_5_0());
                      				
                    }
                    pushFollow(FOLLOW_15);
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

            // InternalLinkerScript.g:323:3: (otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==17) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // InternalLinkerScript.g:324:4: otherlv_11= 'SUBALIGN' otherlv_12= '(' ( (lv_subAlign_13_0= ruleLExpression ) ) otherlv_14= ')'
                    {
                    otherlv_11=(Token)match(input,17,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getOutputSectionAccess().getSUBALIGNKeyword_6_0());
                      			
                    }
                    otherlv_12=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getOutputSectionAccess().getLeftParenthesisKeyword_6_1());
                      			
                    }
                    // InternalLinkerScript.g:332:4: ( (lv_subAlign_13_0= ruleLExpression ) )
                    // InternalLinkerScript.g:333:5: (lv_subAlign_13_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:333:5: (lv_subAlign_13_0= ruleLExpression )
                    // InternalLinkerScript.g:334:6: lv_subAlign_13_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getSubAlignLExpressionParserRuleCall_6_2_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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

                    otherlv_14=(Token)match(input,14,FOLLOW_16); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_14, grammarAccess.getOutputSectionAccess().getRightParenthesisKeyword_6_3());
                      			
                    }

                    }
                    break;

            }

            // InternalLinkerScript.g:356:3: ( (lv_constraint_15_0= ruleOutputSectionConstraint ) )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=23 && LA9_0<=25)) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // InternalLinkerScript.g:357:4: (lv_constraint_15_0= ruleOutputSectionConstraint )
                    {
                    // InternalLinkerScript.g:357:4: (lv_constraint_15_0= ruleOutputSectionConstraint )
                    // InternalLinkerScript.g:358:5: lv_constraint_15_0= ruleOutputSectionConstraint
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getOutputSectionAccess().getConstraintOutputSectionConstraintParserRuleCall_7_0());
                      				
                    }
                    pushFollow(FOLLOW_4);
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

            otherlv_16=(Token)match(input,11,FOLLOW_17); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_16, grammarAccess.getOutputSectionAccess().getLeftCurlyBracketKeyword_8());
              		
            }
            // InternalLinkerScript.g:379:3: ( (lv_statements_17_0= ruleStatement ) )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==RULE_ID||(LA10_0>=31 && LA10_0<=45)||LA10_0==54||(LA10_0>=56 && LA10_0<=62)||(LA10_0>=64 && LA10_0<=65)||(LA10_0>=67 && LA10_0<=68)||LA10_0==77) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // InternalLinkerScript.g:380:4: (lv_statements_17_0= ruleStatement )
            	    {
            	    // InternalLinkerScript.g:380:4: (lv_statements_17_0= ruleStatement )
            	    // InternalLinkerScript.g:381:5: lv_statements_17_0= ruleStatement
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getOutputSectionAccess().getStatementsStatementParserRuleCall_9_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_17);
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
            	    break loop10;
                }
            } while (true);

            otherlv_18=(Token)match(input,12,FOLLOW_18); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_18, grammarAccess.getOutputSectionAccess().getRightCurlyBracketKeyword_10());
              		
            }
            // InternalLinkerScript.g:402:3: (otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) ) )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==18) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // InternalLinkerScript.g:403:4: otherlv_19= '>' ( (lv_memory_20_0= ruleValidID ) )
                    {
                    otherlv_19=(Token)match(input,18,FOLLOW_19); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getOutputSectionAccess().getGreaterThanSignKeyword_11_0());
                      			
                    }
                    // InternalLinkerScript.g:407:4: ( (lv_memory_20_0= ruleValidID ) )
                    // InternalLinkerScript.g:408:5: (lv_memory_20_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:408:5: (lv_memory_20_0= ruleValidID )
                    // InternalLinkerScript.g:409:6: lv_memory_20_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getMemoryValidIDParserRuleCall_11_1_0());
                      					
                    }
                    pushFollow(FOLLOW_20);
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

            // InternalLinkerScript.g:427:3: (otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) ) )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==16) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // InternalLinkerScript.g:428:4: otherlv_21= 'AT' otherlv_22= '>' ( (lv_atMemory_23_0= ruleValidID ) )
                    {
                    otherlv_21=(Token)match(input,16,FOLLOW_21); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_21, grammarAccess.getOutputSectionAccess().getATKeyword_12_0());
                      			
                    }
                    otherlv_22=(Token)match(input,18,FOLLOW_19); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getOutputSectionAccess().getGreaterThanSignKeyword_12_1());
                      			
                    }
                    // InternalLinkerScript.g:436:4: ( (lv_atMemory_23_0= ruleValidID ) )
                    // InternalLinkerScript.g:437:5: (lv_atMemory_23_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:437:5: (lv_atMemory_23_0= ruleValidID )
                    // InternalLinkerScript.g:438:6: lv_atMemory_23_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getAtMemoryValidIDParserRuleCall_12_2_0());
                      					
                    }
                    pushFollow(FOLLOW_22);
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

            // InternalLinkerScript.g:456:3: (otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) ) )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==15) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // InternalLinkerScript.g:457:4: otherlv_24= ':' ( (lv_phdrs_25_0= ruleValidID ) )
            	    {
            	    otherlv_24=(Token)match(input,15,FOLLOW_19); if (state.failed) return current;
            	    if ( state.backtracking==0 ) {

            	      				newLeafNode(otherlv_24, grammarAccess.getOutputSectionAccess().getColonKeyword_13_0());
            	      			
            	    }
            	    // InternalLinkerScript.g:461:4: ( (lv_phdrs_25_0= ruleValidID ) )
            	    // InternalLinkerScript.g:462:5: (lv_phdrs_25_0= ruleValidID )
            	    {
            	    // InternalLinkerScript.g:462:5: (lv_phdrs_25_0= ruleValidID )
            	    // InternalLinkerScript.g:463:6: lv_phdrs_25_0= ruleValidID
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getOutputSectionAccess().getPhdrsValidIDParserRuleCall_13_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_22);
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
            	    break loop13;
                }
            } while (true);

            // InternalLinkerScript.g:481:3: (otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) ) )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==19) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // InternalLinkerScript.g:482:4: otherlv_26= '=' ( (lv_fill_27_0= ruleLExpression ) )
                    {
                    otherlv_26=(Token)match(input,19,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_26, grammarAccess.getOutputSectionAccess().getEqualsSignKeyword_14_0());
                      			
                    }
                    // InternalLinkerScript.g:486:4: ( (lv_fill_27_0= ruleLExpression ) )
                    // InternalLinkerScript.g:487:5: (lv_fill_27_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:487:5: (lv_fill_27_0= ruleLExpression )
                    // InternalLinkerScript.g:488:6: lv_fill_27_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAccess().getFillLExpressionParserRuleCall_14_1_0());
                      					
                    }
                    pushFollow(FOLLOW_23);
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

            // InternalLinkerScript.g:506:3: (otherlv_28= ',' )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==20) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // InternalLinkerScript.g:507:4: otherlv_28= ','
                    {
                    otherlv_28=(Token)match(input,20,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:516:1: entryRuleOutputSectionAlign returns [EObject current=null] : iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF ;
    public final EObject entryRuleOutputSectionAlign() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionAlign = null;


        try {
            // InternalLinkerScript.g:516:59: (iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF )
            // InternalLinkerScript.g:517:2: iv_ruleOutputSectionAlign= ruleOutputSectionAlign EOF
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
    // InternalLinkerScript.g:523:1: ruleOutputSectionAlign returns [EObject current=null] : ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) ) ;
    public final EObject ruleOutputSectionAlign() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        EObject lv_exp_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:529:2: ( ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) ) )
            // InternalLinkerScript.g:530:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) )
            {
            // InternalLinkerScript.g:530:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' ) | ( () otherlv_6= 'ALIGN_WITH_INPUT' ) )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==21) ) {
                alt16=1;
            }
            else if ( (LA16_0==22) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // InternalLinkerScript.g:531:3: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
                    {
                    // InternalLinkerScript.g:531:3: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')' )
                    // InternalLinkerScript.g:532:4: () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_exp_3_0= ruleLExpression ) ) otherlv_4= ')'
                    {
                    // InternalLinkerScript.g:532:4: ()
                    // InternalLinkerScript.g:533:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionAlignAccess().getOutputSectionAlignExpressionAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,21,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionAlignAccess().getALIGNKeyword_0_1());
                      			
                    }
                    otherlv_2=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_2, grammarAccess.getOutputSectionAlignAccess().getLeftParenthesisKeyword_0_2());
                      			
                    }
                    // InternalLinkerScript.g:547:4: ( (lv_exp_3_0= ruleLExpression ) )
                    // InternalLinkerScript.g:548:5: (lv_exp_3_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:548:5: (lv_exp_3_0= ruleLExpression )
                    // InternalLinkerScript.g:549:6: lv_exp_3_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getOutputSectionAlignAccess().getExpLExpressionParserRuleCall_0_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:572:3: ( () otherlv_6= 'ALIGN_WITH_INPUT' )
                    {
                    // InternalLinkerScript.g:572:3: ( () otherlv_6= 'ALIGN_WITH_INPUT' )
                    // InternalLinkerScript.g:573:4: () otherlv_6= 'ALIGN_WITH_INPUT'
                    {
                    // InternalLinkerScript.g:573:4: ()
                    // InternalLinkerScript.g:574:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionAlignAccess().getOutputSectionAlignWithInputAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_6=(Token)match(input,22,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:589:1: entryRuleOutputSectionConstraint returns [EObject current=null] : iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF ;
    public final EObject entryRuleOutputSectionConstraint() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionConstraint = null;


        try {
            // InternalLinkerScript.g:589:64: (iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF )
            // InternalLinkerScript.g:590:2: iv_ruleOutputSectionConstraint= ruleOutputSectionConstraint EOF
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
    // InternalLinkerScript.g:596:1: ruleOutputSectionConstraint returns [EObject current=null] : ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) ) ;
    public final EObject ruleOutputSectionConstraint() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:602:2: ( ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) ) )
            // InternalLinkerScript.g:603:2: ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) )
            {
            // InternalLinkerScript.g:603:2: ( ( () otherlv_1= 'ONLY_IF_RO' ) | ( () otherlv_3= 'ONLY_IF_RW' ) | ( () otherlv_5= 'SPECIAL' ) )
            int alt17=3;
            switch ( input.LA(1) ) {
            case 23:
                {
                alt17=1;
                }
                break;
            case 24:
                {
                alt17=2;
                }
                break;
            case 25:
                {
                alt17=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }

            switch (alt17) {
                case 1 :
                    // InternalLinkerScript.g:604:3: ( () otherlv_1= 'ONLY_IF_RO' )
                    {
                    // InternalLinkerScript.g:604:3: ( () otherlv_1= 'ONLY_IF_RO' )
                    // InternalLinkerScript.g:605:4: () otherlv_1= 'ONLY_IF_RO'
                    {
                    // InternalLinkerScript.g:605:4: ()
                    // InternalLinkerScript.g:606:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintOnlyIfROAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,23,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionConstraintAccess().getONLY_IF_ROKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:618:3: ( () otherlv_3= 'ONLY_IF_RW' )
                    {
                    // InternalLinkerScript.g:618:3: ( () otherlv_3= 'ONLY_IF_RW' )
                    // InternalLinkerScript.g:619:4: () otherlv_3= 'ONLY_IF_RW'
                    {
                    // InternalLinkerScript.g:619:4: ()
                    // InternalLinkerScript.g:620:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintOnlyIfRWAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,24,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getOutputSectionConstraintAccess().getONLY_IF_RWKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:632:3: ( () otherlv_5= 'SPECIAL' )
                    {
                    // InternalLinkerScript.g:632:3: ( () otherlv_5= 'SPECIAL' )
                    // InternalLinkerScript.g:633:4: () otherlv_5= 'SPECIAL'
                    {
                    // InternalLinkerScript.g:633:4: ()
                    // InternalLinkerScript.g:634:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionConstraintAccess().getOutputSectionConstraintSpecialAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,25,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:649:1: entryRuleOutputSectionType returns [EObject current=null] : iv_ruleOutputSectionType= ruleOutputSectionType EOF ;
    public final EObject entryRuleOutputSectionType() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOutputSectionType = null;


        try {
            // InternalLinkerScript.g:649:58: (iv_ruleOutputSectionType= ruleOutputSectionType EOF )
            // InternalLinkerScript.g:650:2: iv_ruleOutputSectionType= ruleOutputSectionType EOF
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
    // InternalLinkerScript.g:656:1: ruleOutputSectionType returns [EObject current=null] : ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) ) ;
    public final EObject ruleOutputSectionType() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:662:2: ( ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) ) )
            // InternalLinkerScript.g:663:2: ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) )
            {
            // InternalLinkerScript.g:663:2: ( ( () otherlv_1= 'NOLOAD' ) | ( () otherlv_3= 'DSECT' ) | ( () otherlv_5= 'COPY' ) | ( () otherlv_7= 'INFO' ) | ( () otherlv_9= 'OVERLAY' ) )
            int alt18=5;
            switch ( input.LA(1) ) {
            case 26:
                {
                alt18=1;
                }
                break;
            case 27:
                {
                alt18=2;
                }
                break;
            case 28:
                {
                alt18=3;
                }
                break;
            case 29:
                {
                alt18=4;
                }
                break;
            case 30:
                {
                alt18=5;
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
                    // InternalLinkerScript.g:664:3: ( () otherlv_1= 'NOLOAD' )
                    {
                    // InternalLinkerScript.g:664:3: ( () otherlv_1= 'NOLOAD' )
                    // InternalLinkerScript.g:665:4: () otherlv_1= 'NOLOAD'
                    {
                    // InternalLinkerScript.g:665:4: ()
                    // InternalLinkerScript.g:666:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeNoLoadAction_0_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_1=(Token)match(input,26,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_1, grammarAccess.getOutputSectionTypeAccess().getNOLOADKeyword_0_1());
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:678:3: ( () otherlv_3= 'DSECT' )
                    {
                    // InternalLinkerScript.g:678:3: ( () otherlv_3= 'DSECT' )
                    // InternalLinkerScript.g:679:4: () otherlv_3= 'DSECT'
                    {
                    // InternalLinkerScript.g:679:4: ()
                    // InternalLinkerScript.g:680:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeDSectAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,27,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getOutputSectionTypeAccess().getDSECTKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:692:3: ( () otherlv_5= 'COPY' )
                    {
                    // InternalLinkerScript.g:692:3: ( () otherlv_5= 'COPY' )
                    // InternalLinkerScript.g:693:4: () otherlv_5= 'COPY'
                    {
                    // InternalLinkerScript.g:693:4: ()
                    // InternalLinkerScript.g:694:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeCopyAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,28,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getOutputSectionTypeAccess().getCOPYKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:706:3: ( () otherlv_7= 'INFO' )
                    {
                    // InternalLinkerScript.g:706:3: ( () otherlv_7= 'INFO' )
                    // InternalLinkerScript.g:707:4: () otherlv_7= 'INFO'
                    {
                    // InternalLinkerScript.g:707:4: ()
                    // InternalLinkerScript.g:708:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeInfoAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,29,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getOutputSectionTypeAccess().getINFOKeyword_3_1());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:720:3: ( () otherlv_9= 'OVERLAY' )
                    {
                    // InternalLinkerScript.g:720:3: ( () otherlv_9= 'OVERLAY' )
                    // InternalLinkerScript.g:721:4: () otherlv_9= 'OVERLAY'
                    {
                    // InternalLinkerScript.g:721:4: ()
                    // InternalLinkerScript.g:722:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getOutputSectionTypeAccess().getOutputSectionTypeOverlayAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_9=(Token)match(input,30,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:737:1: entryRuleStatement returns [EObject current=null] : iv_ruleStatement= ruleStatement EOF ;
    public final EObject entryRuleStatement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleStatement = null;


        try {
            // InternalLinkerScript.g:737:50: (iv_ruleStatement= ruleStatement EOF )
            // InternalLinkerScript.g:738:2: iv_ruleStatement= ruleStatement EOF
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
    // InternalLinkerScript.g:744:1: ruleStatement returns [EObject current=null] : ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) ) ;
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
            // InternalLinkerScript.g:750:2: ( ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) ) )
            // InternalLinkerScript.g:751:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )
            {
            // InternalLinkerScript.g:751:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )
            int alt20=10;
            alt20 = dfa20.predict(input);
            switch (alt20) {
                case 1 :
                    // InternalLinkerScript.g:752:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    {
                    // InternalLinkerScript.g:752:3: ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) )
                    // InternalLinkerScript.g:753:4: () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' )
                    {
                    // InternalLinkerScript.g:753:4: ()
                    // InternalLinkerScript.g:754:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:760:4: ( (lv_assignment_1_0= ruleAssignmentRule ) )
                    // InternalLinkerScript.g:761:5: (lv_assignment_1_0= ruleAssignmentRule )
                    {
                    // InternalLinkerScript.g:761:5: (lv_assignment_1_0= ruleAssignmentRule )
                    // InternalLinkerScript.g:762:6: lv_assignment_1_0= ruleAssignmentRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getAssignmentAssignmentRuleParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_24);
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

                    // InternalLinkerScript.g:779:4: (otherlv_2= ',' | otherlv_3= ';' )
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==20) ) {
                        alt19=1;
                    }
                    else if ( (LA19_0==31) ) {
                        alt19=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return current;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 19, 0, input);

                        throw nvae;
                    }
                    switch (alt19) {
                        case 1 :
                            // InternalLinkerScript.g:780:5: otherlv_2= ','
                            {
                            otherlv_2=(Token)match(input,20,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getStatementAccess().getCommaKeyword_0_2_0());
                              				
                            }

                            }
                            break;
                        case 2 :
                            // InternalLinkerScript.g:785:5: otherlv_3= ';'
                            {
                            otherlv_3=(Token)match(input,31,FOLLOW_2); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_3, grammarAccess.getStatementAccess().getSemicolonKeyword_0_2_1());
                              				
                            }

                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:792:3: ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' )
                    {
                    // InternalLinkerScript.g:792:3: ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' )
                    // InternalLinkerScript.g:793:4: () otherlv_5= 'CREATE_OBJECT_SYMBOLS'
                    {
                    // InternalLinkerScript.g:793:4: ()
                    // InternalLinkerScript.g:794:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementCreateObjectSymbolsAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,32,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getStatementAccess().getCREATE_OBJECT_SYMBOLSKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:806:3: ( () otherlv_7= 'CONSTRUCTORS' )
                    {
                    // InternalLinkerScript.g:806:3: ( () otherlv_7= 'CONSTRUCTORS' )
                    // InternalLinkerScript.g:807:4: () otherlv_7= 'CONSTRUCTORS'
                    {
                    // InternalLinkerScript.g:807:4: ()
                    // InternalLinkerScript.g:808:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementConstructorsAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_7=(Token)match(input,33,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_7, grammarAccess.getStatementAccess().getCONSTRUCTORSKeyword_2_1());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:820:3: ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' )
                    {
                    // InternalLinkerScript.g:820:3: ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' )
                    // InternalLinkerScript.g:821:4: () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')'
                    {
                    // InternalLinkerScript.g:821:4: ()
                    // InternalLinkerScript.g:822:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementConstructorsSortedAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_9=(Token)match(input,34,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getStatementAccess().getSORT_BY_NAMEKeyword_3_1());
                      			
                    }
                    otherlv_10=(Token)match(input,13,FOLLOW_25); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    otherlv_11=(Token)match(input,33,FOLLOW_9); if (state.failed) return current;
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
                    // InternalLinkerScript.g:846:3: ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:846:3: ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:847:4: () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:847:4: ()
                    // InternalLinkerScript.g:848:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementDataAction_4_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:854:4: ( (lv_size_14_0= ruleStatementDataSize ) )
                    // InternalLinkerScript.g:855:5: (lv_size_14_0= ruleStatementDataSize )
                    {
                    // InternalLinkerScript.g:855:5: (lv_size_14_0= ruleStatementDataSize )
                    // InternalLinkerScript.g:856:6: lv_size_14_0= ruleStatementDataSize
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getSizeStatementDataSizeParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_12);
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

                    otherlv_15=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:877:4: ( (lv_data_16_0= ruleLExpression ) )
                    // InternalLinkerScript.g:878:5: (lv_data_16_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:878:5: (lv_data_16_0= ruleLExpression )
                    // InternalLinkerScript.g:879:6: lv_data_16_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getDataLExpressionParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:902:3: ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' )
                    {
                    // InternalLinkerScript.g:902:3: ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' )
                    // InternalLinkerScript.g:903:4: () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')'
                    {
                    // InternalLinkerScript.g:903:4: ()
                    // InternalLinkerScript.g:904:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementFillAction_5_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,35,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getStatementAccess().getFILLKeyword_5_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    // InternalLinkerScript.g:918:4: ( (lv_fill_21_0= ruleLExpression ) )
                    // InternalLinkerScript.g:919:5: (lv_fill_21_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:919:5: (lv_fill_21_0= ruleLExpression )
                    // InternalLinkerScript.g:920:6: lv_fill_21_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getFillLExpressionParserRuleCall_5_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:943:3: ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' )
                    {
                    // InternalLinkerScript.g:943:3: ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' )
                    // InternalLinkerScript.g:944:4: () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')'
                    {
                    // InternalLinkerScript.g:944:4: ()
                    // InternalLinkerScript.g:945:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementAssertAction_6_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_24=(Token)match(input,36,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_24, grammarAccess.getStatementAccess().getASSERTKeyword_6_1());
                      			
                    }
                    otherlv_25=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getStatementAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    // InternalLinkerScript.g:959:4: ( (lv_exp_26_0= ruleLExpression ) )
                    // InternalLinkerScript.g:960:5: (lv_exp_26_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:960:5: (lv_exp_26_0= ruleLExpression )
                    // InternalLinkerScript.g:961:6: lv_exp_26_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getExpLExpressionParserRuleCall_6_3_0());
                      					
                    }
                    pushFollow(FOLLOW_26);
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

                    otherlv_27=(Token)match(input,20,FOLLOW_19); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getStatementAccess().getCommaKeyword_6_4());
                      			
                    }
                    // InternalLinkerScript.g:982:4: ( (lv_message_28_0= ruleValidID ) )
                    // InternalLinkerScript.g:983:5: (lv_message_28_0= ruleValidID )
                    {
                    // InternalLinkerScript.g:983:5: (lv_message_28_0= ruleValidID )
                    // InternalLinkerScript.g:984:6: lv_message_28_0= ruleValidID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getStatementAccess().getMessageValidIDParserRuleCall_6_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:1007:3: ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:1007:3: ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:1008:4: () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:1008:4: ()
                    // InternalLinkerScript.g:1009:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementIncludeAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_31=(Token)match(input,37,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_31, grammarAccess.getStatementAccess().getINCLUDEKeyword_7_1());
                      			
                    }
                    // InternalLinkerScript.g:1019:4: ( (lv_filename_32_0= ruleWildID ) )
                    // InternalLinkerScript.g:1020:5: (lv_filename_32_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1020:5: (lv_filename_32_0= ruleWildID )
                    // InternalLinkerScript.g:1021:6: lv_filename_32_0= ruleWildID
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
                    // InternalLinkerScript.g:1040:3: ( () ( (lv_spec_34_0= ruleInputSection ) ) )
                    {
                    // InternalLinkerScript.g:1040:3: ( () ( (lv_spec_34_0= ruleInputSection ) ) )
                    // InternalLinkerScript.g:1041:4: () ( (lv_spec_34_0= ruleInputSection ) )
                    {
                    // InternalLinkerScript.g:1041:4: ()
                    // InternalLinkerScript.g:1042:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementInputSectionAction_8_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1048:4: ( (lv_spec_34_0= ruleInputSection ) )
                    // InternalLinkerScript.g:1049:5: (lv_spec_34_0= ruleInputSection )
                    {
                    // InternalLinkerScript.g:1049:5: (lv_spec_34_0= ruleInputSection )
                    // InternalLinkerScript.g:1050:6: lv_spec_34_0= ruleInputSection
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
                    // InternalLinkerScript.g:1069:3: ( () otherlv_36= ';' )
                    {
                    // InternalLinkerScript.g:1069:3: ( () otherlv_36= ';' )
                    // InternalLinkerScript.g:1070:4: () otherlv_36= ';'
                    {
                    // InternalLinkerScript.g:1070:4: ()
                    // InternalLinkerScript.g:1071:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getStatementAccess().getStatementNopAction_9_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_36=(Token)match(input,31,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleStatementDataSize"
    // InternalLinkerScript.g:1086:1: entryRuleStatementDataSize returns [String current=null] : iv_ruleStatementDataSize= ruleStatementDataSize EOF ;
    public final String entryRuleStatementDataSize() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleStatementDataSize = null;


        try {
            // InternalLinkerScript.g:1086:57: (iv_ruleStatementDataSize= ruleStatementDataSize EOF )
            // InternalLinkerScript.g:1087:2: iv_ruleStatementDataSize= ruleStatementDataSize EOF
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
    // InternalLinkerScript.g:1093:1: ruleStatementDataSize returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' ) ;
    public final AntlrDatatypeRuleToken ruleStatementDataSize() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:1099:2: ( (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' ) )
            // InternalLinkerScript.g:1100:2: (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' )
            {
            // InternalLinkerScript.g:1100:2: (kw= 'BYTE' | kw= 'SHORT' | kw= 'LONG' | kw= 'QUAD' | kw= 'SQUAD' )
            int alt21=5;
            switch ( input.LA(1) ) {
            case 38:
                {
                alt21=1;
                }
                break;
            case 39:
                {
                alt21=2;
                }
                break;
            case 40:
                {
                alt21=3;
                }
                break;
            case 41:
                {
                alt21=4;
                }
                break;
            case 42:
                {
                alt21=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }

            switch (alt21) {
                case 1 :
                    // InternalLinkerScript.g:1101:3: kw= 'BYTE'
                    {
                    kw=(Token)match(input,38,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getBYTEKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1107:3: kw= 'SHORT'
                    {
                    kw=(Token)match(input,39,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getSHORTKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1113:3: kw= 'LONG'
                    {
                    kw=(Token)match(input,40,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getLONGKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:1119:3: kw= 'QUAD'
                    {
                    kw=(Token)match(input,41,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getStatementDataSizeAccess().getQUADKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:1125:3: kw= 'SQUAD'
                    {
                    kw=(Token)match(input,42,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:1134:1: entryRuleAssignmentRule returns [EObject current=null] : iv_ruleAssignmentRule= ruleAssignmentRule EOF ;
    public final EObject entryRuleAssignmentRule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAssignmentRule = null;


        try {
            // InternalLinkerScript.g:1134:55: (iv_ruleAssignmentRule= ruleAssignmentRule EOF )
            // InternalLinkerScript.g:1135:2: iv_ruleAssignmentRule= ruleAssignmentRule EOF
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
    // InternalLinkerScript.g:1141:1: ruleAssignmentRule returns [EObject current=null] : ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) ) ;
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
            // InternalLinkerScript.g:1147:2: ( ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) ) )
            // InternalLinkerScript.g:1148:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) )
            {
            // InternalLinkerScript.g:1148:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) ) | ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' ) | ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' ) )
            int alt22=4;
            switch ( input.LA(1) ) {
            case RULE_ID:
            case 62:
            case 64:
            case 65:
            case 67:
            case 68:
            case 77:
                {
                alt22=1;
                }
                break;
            case 43:
                {
                alt22=2;
                }
                break;
            case 44:
                {
                alt22=3;
                }
                break;
            case 45:
                {
                alt22=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }

            switch (alt22) {
                case 1 :
                    // InternalLinkerScript.g:1149:3: ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) )
                    {
                    // InternalLinkerScript.g:1149:3: ( () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) ) )
                    // InternalLinkerScript.g:1150:4: () ( (lv_name_1_0= ruleWildID ) ) ( (lv_feature_2_0= ruleOpAssign ) ) ( (lv_exp_3_0= ruleLExpression ) )
                    {
                    // InternalLinkerScript.g:1150:4: ()
                    // InternalLinkerScript.g:1151:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1157:4: ( (lv_name_1_0= ruleWildID ) )
                    // InternalLinkerScript.g:1158:5: (lv_name_1_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1158:5: (lv_name_1_0= ruleWildID )
                    // InternalLinkerScript.g:1159:6: lv_name_1_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_28);
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

                    // InternalLinkerScript.g:1176:4: ( (lv_feature_2_0= ruleOpAssign ) )
                    // InternalLinkerScript.g:1177:5: (lv_feature_2_0= ruleOpAssign )
                    {
                    // InternalLinkerScript.g:1177:5: (lv_feature_2_0= ruleOpAssign )
                    // InternalLinkerScript.g:1178:6: lv_feature_2_0= ruleOpAssign
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getFeatureOpAssignParserRuleCall_0_2_0());
                      					
                    }
                    pushFollow(FOLLOW_13);
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

                    // InternalLinkerScript.g:1195:4: ( (lv_exp_3_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1196:5: (lv_exp_3_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1196:5: (lv_exp_3_0= ruleLExpression )
                    // InternalLinkerScript.g:1197:6: lv_exp_3_0= ruleLExpression
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
                    // InternalLinkerScript.g:1216:3: ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' )
                    {
                    // InternalLinkerScript.g:1216:3: ( () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')' )
                    // InternalLinkerScript.g:1217:4: () otherlv_5= 'HIDDEN' otherlv_6= '(' ( (lv_name_7_0= ruleWildID ) ) ( (lv_feature_8_0= '=' ) ) ( (lv_exp_9_0= ruleLExpression ) ) otherlv_10= ')'
                    {
                    // InternalLinkerScript.g:1217:4: ()
                    // InternalLinkerScript.g:1218:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentHiddenAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_5=(Token)match(input,43,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getAssignmentRuleAccess().getHIDDENKeyword_1_1());
                      			
                    }
                    otherlv_6=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:1232:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:1233:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1233:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:1234:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_1_3_0());
                      					
                    }
                    pushFollow(FOLLOW_29);
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

                    // InternalLinkerScript.g:1251:4: ( (lv_feature_8_0= '=' ) )
                    // InternalLinkerScript.g:1252:5: (lv_feature_8_0= '=' )
                    {
                    // InternalLinkerScript.g:1252:5: (lv_feature_8_0= '=' )
                    // InternalLinkerScript.g:1253:6: lv_feature_8_0= '='
                    {
                    lv_feature_8_0=(Token)match(input,19,FOLLOW_13); if (state.failed) return current;
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

                    // InternalLinkerScript.g:1265:4: ( (lv_exp_9_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1266:5: (lv_exp_9_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1266:5: (lv_exp_9_0= ruleLExpression )
                    // InternalLinkerScript.g:1267:6: lv_exp_9_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_1_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:1290:3: ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:1290:3: ( () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:1291:4: () otherlv_12= 'PROVIDE' otherlv_13= '(' ( (lv_name_14_0= ruleWildID ) ) ( (lv_feature_15_0= '=' ) ) ( (lv_exp_16_0= ruleLExpression ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:1291:4: ()
                    // InternalLinkerScript.g:1292:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentProvideAction_2_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_12=(Token)match(input,44,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_12, grammarAccess.getAssignmentRuleAccess().getPROVIDEKeyword_2_1());
                      			
                    }
                    otherlv_13=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_13, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:1306:4: ( (lv_name_14_0= ruleWildID ) )
                    // InternalLinkerScript.g:1307:5: (lv_name_14_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1307:5: (lv_name_14_0= ruleWildID )
                    // InternalLinkerScript.g:1308:6: lv_name_14_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_29);
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

                    // InternalLinkerScript.g:1325:4: ( (lv_feature_15_0= '=' ) )
                    // InternalLinkerScript.g:1326:5: (lv_feature_15_0= '=' )
                    {
                    // InternalLinkerScript.g:1326:5: (lv_feature_15_0= '=' )
                    // InternalLinkerScript.g:1327:6: lv_feature_15_0= '='
                    {
                    lv_feature_15_0=(Token)match(input,19,FOLLOW_13); if (state.failed) return current;
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

                    // InternalLinkerScript.g:1339:4: ( (lv_exp_16_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1340:5: (lv_exp_16_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1340:5: (lv_exp_16_0= ruleLExpression )
                    // InternalLinkerScript.g:1341:6: lv_exp_16_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_2_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:1364:3: ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' )
                    {
                    // InternalLinkerScript.g:1364:3: ( () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')' )
                    // InternalLinkerScript.g:1365:4: () otherlv_19= 'PROVIDE_HIDDEN' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) ( (lv_feature_22_0= '=' ) ) ( (lv_exp_23_0= ruleLExpression ) ) otherlv_24= ')'
                    {
                    // InternalLinkerScript.g:1365:4: ()
                    // InternalLinkerScript.g:1366:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getAssignmentRuleAccess().getAssignmentProvideHiddenAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,45,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getAssignmentRuleAccess().getPROVIDE_HIDDENKeyword_3_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getAssignmentRuleAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:1380:4: ( (lv_name_21_0= ruleWildID ) )
                    // InternalLinkerScript.g:1381:5: (lv_name_21_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1381:5: (lv_name_21_0= ruleWildID )
                    // InternalLinkerScript.g:1382:6: lv_name_21_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getNameWildIDParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_29);
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

                    // InternalLinkerScript.g:1399:4: ( (lv_feature_22_0= '=' ) )
                    // InternalLinkerScript.g:1400:5: (lv_feature_22_0= '=' )
                    {
                    // InternalLinkerScript.g:1400:5: (lv_feature_22_0= '=' )
                    // InternalLinkerScript.g:1401:6: lv_feature_22_0= '='
                    {
                    lv_feature_22_0=(Token)match(input,19,FOLLOW_13); if (state.failed) return current;
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

                    // InternalLinkerScript.g:1413:4: ( (lv_exp_23_0= ruleLExpression ) )
                    // InternalLinkerScript.g:1414:5: (lv_exp_23_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:1414:5: (lv_exp_23_0= ruleLExpression )
                    // InternalLinkerScript.g:1415:6: lv_exp_23_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAssignmentRuleAccess().getExpLExpressionParserRuleCall_3_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
    // InternalLinkerScript.g:1441:1: entryRuleOpAssign returns [String current=null] : iv_ruleOpAssign= ruleOpAssign EOF ;
    public final String entryRuleOpAssign() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAssign = null;


        try {
            // InternalLinkerScript.g:1441:48: (iv_ruleOpAssign= ruleOpAssign EOF )
            // InternalLinkerScript.g:1442:2: iv_ruleOpAssign= ruleOpAssign EOF
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
    // InternalLinkerScript.g:1448:1: ruleOpAssign returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) ;
    public final AntlrDatatypeRuleToken ruleOpAssign() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:1454:2: ( (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) )
            // InternalLinkerScript.g:1455:2: (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            {
            // InternalLinkerScript.g:1455:2: (kw= '=' | kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            int alt23=9;
            switch ( input.LA(1) ) {
            case 19:
                {
                alt23=1;
                }
                break;
            case 46:
                {
                alt23=2;
                }
                break;
            case 47:
                {
                alt23=3;
                }
                break;
            case 48:
                {
                alt23=4;
                }
                break;
            case 49:
                {
                alt23=5;
                }
                break;
            case 50:
                {
                alt23=6;
                }
                break;
            case 18:
                {
                alt23=7;
                }
                break;
            case 52:
                {
                alt23=8;
                }
                break;
            case 53:
                {
                alt23=9;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }

            switch (alt23) {
                case 1 :
                    // InternalLinkerScript.g:1456:3: kw= '='
                    {
                    kw=(Token)match(input,19,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:1462:3: kw= '+='
                    {
                    kw=(Token)match(input,46,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getPlusSignEqualsSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:1468:3: kw= '-='
                    {
                    kw=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getHyphenMinusEqualsSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:1474:3: kw= '*='
                    {
                    kw=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getAsteriskEqualsSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:1480:3: kw= '/='
                    {
                    kw=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getSolidusEqualsSignKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:1486:3: (kw= '<' kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:1486:3: (kw= '<' kw= '<' kw= '=' )
                    // InternalLinkerScript.g:1487:4: kw= '<' kw= '<' kw= '='
                    {
                    kw=(Token)match(input,50,FOLLOW_30); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getLessThanSignKeyword_5_0());
                      			
                    }
                    kw=(Token)match(input,50,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getLessThanSignKeyword_5_1());
                      			
                    }
                    kw=(Token)match(input,19,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getEqualsSignKeyword_5_2());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:1504:3: (kw= '>' kw= '>=' )
                    {
                    // InternalLinkerScript.g:1504:3: (kw= '>' kw= '>=' )
                    // InternalLinkerScript.g:1505:4: kw= '>' kw= '>='
                    {
                    kw=(Token)match(input,18,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getGreaterThanSignKeyword_6_0());
                      			
                    }
                    kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpAssignAccess().getGreaterThanSignEqualsSignKeyword_6_1());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:1517:3: kw= '&='
                    {
                    kw=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAssignAccess().getAmpersandEqualsSignKeyword_7());
                      		
                    }

                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:1523:3: kw= '|='
                    {
                    kw=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:1532:1: entryRuleInputSection returns [EObject current=null] : iv_ruleInputSection= ruleInputSection EOF ;
    public final EObject entryRuleInputSection() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleInputSection = null;


        try {
            // InternalLinkerScript.g:1532:53: (iv_ruleInputSection= ruleInputSection EOF )
            // InternalLinkerScript.g:1533:2: iv_ruleInputSection= ruleInputSection EOF
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
    // InternalLinkerScript.g:1539:1: ruleInputSection returns [EObject current=null] : ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) ) ;
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
            // InternalLinkerScript.g:1545:2: ( ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) ) )
            // InternalLinkerScript.g:1546:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) )
            {
            // InternalLinkerScript.g:1546:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) )
            int alt36=4;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // InternalLinkerScript.g:1547:3: ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:1547:3: ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:1548:4: () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:1548:4: ()
                    // InternalLinkerScript.g:1549:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionFileAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1555:4: (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==54) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // InternalLinkerScript.g:1556:5: otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')'
                            {
                            otherlv_1=(Token)match(input,54,FOLLOW_12); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_1, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_0_1_0());
                              				
                            }
                            otherlv_2=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_2, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_0_1_1());
                              				
                            }
                            // InternalLinkerScript.g:1564:5: ( (lv_flags_3_0= ruleWildID ) )
                            // InternalLinkerScript.g:1565:6: (lv_flags_3_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:1565:6: (lv_flags_3_0= ruleWildID )
                            // InternalLinkerScript.g:1566:7: lv_flags_3_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_0_1_2_0());
                              						
                            }
                            pushFollow(FOLLOW_32);
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

                            // InternalLinkerScript.g:1583:5: (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )*
                            loop24:
                            do {
                                int alt24=2;
                                int LA24_0 = input.LA(1);

                                if ( (LA24_0==55) ) {
                                    alt24=1;
                                }


                                switch (alt24) {
                            	case 1 :
                            	    // InternalLinkerScript.g:1584:6: otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) )
                            	    {
                            	    otherlv_4=(Token)match(input,55,FOLLOW_27); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_4, grammarAccess.getInputSectionAccess().getAmpersandKeyword_0_1_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:1588:6: ( (lv_flags_5_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:1589:7: (lv_flags_5_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:1589:7: (lv_flags_5_0= ruleWildID )
                            	    // InternalLinkerScript.g:1590:8: lv_flags_5_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_0_1_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_32);
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
                            	    break loop24;
                                }
                            } while (true);

                            otherlv_6=(Token)match(input,14,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_6, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_0_1_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:1613:4: ( (lv_file_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:1614:5: (lv_file_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1614:5: (lv_file_7_0= ruleWildID )
                    // InternalLinkerScript.g:1615:6: lv_file_7_0= ruleWildID
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
                    // InternalLinkerScript.g:1634:3: ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' )
                    {
                    // InternalLinkerScript.g:1634:3: ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' )
                    // InternalLinkerScript.g:1635:4: () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')'
                    {
                    // InternalLinkerScript.g:1635:4: ()
                    // InternalLinkerScript.g:1636:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionWildAction_1_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1642:4: (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==54) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // InternalLinkerScript.g:1643:5: otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')'
                            {
                            otherlv_9=(Token)match(input,54,FOLLOW_12); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_9, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_1_1_0());
                              				
                            }
                            otherlv_10=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_10, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_1_1_1());
                              				
                            }
                            // InternalLinkerScript.g:1651:5: ( (lv_flags_11_0= ruleWildID ) )
                            // InternalLinkerScript.g:1652:6: (lv_flags_11_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:1652:6: (lv_flags_11_0= ruleWildID )
                            // InternalLinkerScript.g:1653:7: lv_flags_11_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_1_1_2_0());
                              						
                            }
                            pushFollow(FOLLOW_32);
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

                            // InternalLinkerScript.g:1670:5: (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )*
                            loop26:
                            do {
                                int alt26=2;
                                int LA26_0 = input.LA(1);

                                if ( (LA26_0==55) ) {
                                    alt26=1;
                                }


                                switch (alt26) {
                            	case 1 :
                            	    // InternalLinkerScript.g:1671:6: otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) )
                            	    {
                            	    otherlv_12=(Token)match(input,55,FOLLOW_27); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_12, grammarAccess.getInputSectionAccess().getAmpersandKeyword_1_1_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:1675:6: ( (lv_flags_13_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:1676:7: (lv_flags_13_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:1676:7: (lv_flags_13_0= ruleWildID )
                            	    // InternalLinkerScript.g:1677:8: lv_flags_13_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_1_1_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_32);
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
                            	    break loop26;
                                }
                            } while (true);

                            otherlv_14=(Token)match(input,14,FOLLOW_33); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_14, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_1_1_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:1700:4: ( (lv_wildFile_15_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:1701:5: (lv_wildFile_15_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:1701:5: (lv_wildFile_15_0= ruleWildcardRule )
                    // InternalLinkerScript.g:1702:6: lv_wildFile_15_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getWildFileWildcardRuleParserRuleCall_1_2_0());
                      					
                    }
                    pushFollow(FOLLOW_12);
                    lv_wildFile_15_0=ruleWildcardRule();

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
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_16=(Token)match(input,13,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_16, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_1_3());
                      			
                    }
                    // InternalLinkerScript.g:1723:4: ( (lv_sections_17_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:1724:5: (lv_sections_17_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:1724:5: (lv_sections_17_0= ruleWildcardRule )
                    // InternalLinkerScript.g:1725:6: lv_sections_17_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_1_4_0());
                      					
                    }
                    pushFollow(FOLLOW_34);
                    lv_sections_17_0=ruleWildcardRule();

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
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:1742:4: ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )*
                    loop29:
                    do {
                        int alt29=2;
                        int LA29_0 = input.LA(1);

                        if ( (LA29_0==RULE_ID||LA29_0==20||LA29_0==34||(LA29_0>=57 && LA29_0<=62)||(LA29_0>=64 && LA29_0<=65)||(LA29_0>=67 && LA29_0<=68)||LA29_0==77) ) {
                            alt29=1;
                        }


                        switch (alt29) {
                    	case 1 :
                    	    // InternalLinkerScript.g:1743:5: (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) )
                    	    {
                    	    // InternalLinkerScript.g:1743:5: (otherlv_18= ',' )?
                    	    int alt28=2;
                    	    int LA28_0 = input.LA(1);

                    	    if ( (LA28_0==20) ) {
                    	        alt28=1;
                    	    }
                    	    switch (alt28) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:1744:6: otherlv_18= ','
                    	            {
                    	            otherlv_18=(Token)match(input,20,FOLLOW_33); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_18, grammarAccess.getInputSectionAccess().getCommaKeyword_1_5_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:1749:5: ( (lv_sections_19_0= ruleWildcardRule ) )
                    	    // InternalLinkerScript.g:1750:6: (lv_sections_19_0= ruleWildcardRule )
                    	    {
                    	    // InternalLinkerScript.g:1750:6: (lv_sections_19_0= ruleWildcardRule )
                    	    // InternalLinkerScript.g:1751:7: lv_sections_19_0= ruleWildcardRule
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_1_5_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_34);
                    	    lv_sections_19_0=ruleWildcardRule();

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
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop29;
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
                    // InternalLinkerScript.g:1775:3: ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' )
                    {
                    // InternalLinkerScript.g:1775:3: ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' )
                    // InternalLinkerScript.g:1776:4: () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')'
                    {
                    // InternalLinkerScript.g:1776:4: ()
                    // InternalLinkerScript.g:1777:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionFileAction_2_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1783:4: ( (lv_keep_22_0= 'KEEP' ) )
                    // InternalLinkerScript.g:1784:5: (lv_keep_22_0= 'KEEP' )
                    {
                    // InternalLinkerScript.g:1784:5: (lv_keep_22_0= 'KEEP' )
                    // InternalLinkerScript.g:1785:6: lv_keep_22_0= 'KEEP'
                    {
                    lv_keep_22_0=(Token)match(input,56,FOLLOW_12); if (state.failed) return current;
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

                    otherlv_23=(Token)match(input,13,FOLLOW_35); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_23, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:1801:4: (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==54) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // InternalLinkerScript.g:1802:5: otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')'
                            {
                            otherlv_24=(Token)match(input,54,FOLLOW_12); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_24, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_2_3_0());
                              				
                            }
                            otherlv_25=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_25, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_2_3_1());
                              				
                            }
                            // InternalLinkerScript.g:1810:5: ( (lv_flags_26_0= ruleWildID ) )
                            // InternalLinkerScript.g:1811:6: (lv_flags_26_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:1811:6: (lv_flags_26_0= ruleWildID )
                            // InternalLinkerScript.g:1812:7: lv_flags_26_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_2_3_2_0());
                              						
                            }
                            pushFollow(FOLLOW_32);
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

                            // InternalLinkerScript.g:1829:5: (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )*
                            loop30:
                            do {
                                int alt30=2;
                                int LA30_0 = input.LA(1);

                                if ( (LA30_0==55) ) {
                                    alt30=1;
                                }


                                switch (alt30) {
                            	case 1 :
                            	    // InternalLinkerScript.g:1830:6: otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) )
                            	    {
                            	    otherlv_27=(Token)match(input,55,FOLLOW_27); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_27, grammarAccess.getInputSectionAccess().getAmpersandKeyword_2_3_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:1834:6: ( (lv_flags_28_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:1835:7: (lv_flags_28_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:1835:7: (lv_flags_28_0= ruleWildID )
                            	    // InternalLinkerScript.g:1836:8: lv_flags_28_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_2_3_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_32);
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
                            	    break loop30;
                                }
                            } while (true);

                            otherlv_29=(Token)match(input,14,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_29, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_2_3_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:1859:4: ( (lv_file_30_0= ruleWildID ) )
                    // InternalLinkerScript.g:1860:5: (lv_file_30_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:1860:5: (lv_file_30_0= ruleWildID )
                    // InternalLinkerScript.g:1861:6: lv_file_30_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getFileWildIDParserRuleCall_2_4_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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
                    // InternalLinkerScript.g:1884:3: ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' )
                    {
                    // InternalLinkerScript.g:1884:3: ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' )
                    // InternalLinkerScript.g:1885:4: () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')'
                    {
                    // InternalLinkerScript.g:1885:4: ()
                    // InternalLinkerScript.g:1886:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getInputSectionAccess().getInputSectionWildAction_3_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:1892:4: ( (lv_keep_33_0= 'KEEP' ) )
                    // InternalLinkerScript.g:1893:5: (lv_keep_33_0= 'KEEP' )
                    {
                    // InternalLinkerScript.g:1893:5: (lv_keep_33_0= 'KEEP' )
                    // InternalLinkerScript.g:1894:6: lv_keep_33_0= 'KEEP'
                    {
                    lv_keep_33_0=(Token)match(input,56,FOLLOW_12); if (state.failed) return current;
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

                    otherlv_34=(Token)match(input,13,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_34, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:1910:4: (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==54) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // InternalLinkerScript.g:1911:5: otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')'
                            {
                            otherlv_35=(Token)match(input,54,FOLLOW_12); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_35, grammarAccess.getInputSectionAccess().getINPUT_SECTION_FLAGSKeyword_3_3_0());
                              				
                            }
                            otherlv_36=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_36, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_3_1());
                              				
                            }
                            // InternalLinkerScript.g:1919:5: ( (lv_flags_37_0= ruleWildID ) )
                            // InternalLinkerScript.g:1920:6: (lv_flags_37_0= ruleWildID )
                            {
                            // InternalLinkerScript.g:1920:6: (lv_flags_37_0= ruleWildID )
                            // InternalLinkerScript.g:1921:7: lv_flags_37_0= ruleWildID
                            {
                            if ( state.backtracking==0 ) {

                              							newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_3_3_2_0());
                              						
                            }
                            pushFollow(FOLLOW_32);
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

                            // InternalLinkerScript.g:1938:5: (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )*
                            loop32:
                            do {
                                int alt32=2;
                                int LA32_0 = input.LA(1);

                                if ( (LA32_0==55) ) {
                                    alt32=1;
                                }


                                switch (alt32) {
                            	case 1 :
                            	    // InternalLinkerScript.g:1939:6: otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) )
                            	    {
                            	    otherlv_38=(Token)match(input,55,FOLLOW_27); if (state.failed) return current;
                            	    if ( state.backtracking==0 ) {

                            	      						newLeafNode(otherlv_38, grammarAccess.getInputSectionAccess().getAmpersandKeyword_3_3_3_0());
                            	      					
                            	    }
                            	    // InternalLinkerScript.g:1943:6: ( (lv_flags_39_0= ruleWildID ) )
                            	    // InternalLinkerScript.g:1944:7: (lv_flags_39_0= ruleWildID )
                            	    {
                            	    // InternalLinkerScript.g:1944:7: (lv_flags_39_0= ruleWildID )
                            	    // InternalLinkerScript.g:1945:8: lv_flags_39_0= ruleWildID
                            	    {
                            	    if ( state.backtracking==0 ) {

                            	      								newCompositeNode(grammarAccess.getInputSectionAccess().getFlagsWildIDParserRuleCall_3_3_3_1_0());
                            	      							
                            	    }
                            	    pushFollow(FOLLOW_32);
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
                            	    break loop32;
                                }
                            } while (true);

                            otherlv_40=(Token)match(input,14,FOLLOW_33); if (state.failed) return current;
                            if ( state.backtracking==0 ) {

                              					newLeafNode(otherlv_40, grammarAccess.getInputSectionAccess().getRightParenthesisKeyword_3_3_4());
                              				
                            }

                            }
                            break;

                    }

                    // InternalLinkerScript.g:1968:4: ( (lv_wildFile_41_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:1969:5: (lv_wildFile_41_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:1969:5: (lv_wildFile_41_0= ruleWildcardRule )
                    // InternalLinkerScript.g:1970:6: lv_wildFile_41_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getWildFileWildcardRuleParserRuleCall_3_4_0());
                      					
                    }
                    pushFollow(FOLLOW_12);
                    lv_wildFile_41_0=ruleWildcardRule();

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
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_42=(Token)match(input,13,FOLLOW_33); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_42, grammarAccess.getInputSectionAccess().getLeftParenthesisKeyword_3_5());
                      			
                    }
                    // InternalLinkerScript.g:1991:4: ( (lv_sections_43_0= ruleWildcardRule ) )
                    // InternalLinkerScript.g:1992:5: (lv_sections_43_0= ruleWildcardRule )
                    {
                    // InternalLinkerScript.g:1992:5: (lv_sections_43_0= ruleWildcardRule )
                    // InternalLinkerScript.g:1993:6: lv_sections_43_0= ruleWildcardRule
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_3_6_0());
                      					
                    }
                    pushFollow(FOLLOW_34);
                    lv_sections_43_0=ruleWildcardRule();

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
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    // InternalLinkerScript.g:2010:4: ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )*
                    loop35:
                    do {
                        int alt35=2;
                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==RULE_ID||LA35_0==20||LA35_0==34||(LA35_0>=57 && LA35_0<=62)||(LA35_0>=64 && LA35_0<=65)||(LA35_0>=67 && LA35_0<=68)||LA35_0==77) ) {
                            alt35=1;
                        }


                        switch (alt35) {
                    	case 1 :
                    	    // InternalLinkerScript.g:2011:5: (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) )
                    	    {
                    	    // InternalLinkerScript.g:2011:5: (otherlv_44= ',' )?
                    	    int alt34=2;
                    	    int LA34_0 = input.LA(1);

                    	    if ( (LA34_0==20) ) {
                    	        alt34=1;
                    	    }
                    	    switch (alt34) {
                    	        case 1 :
                    	            // InternalLinkerScript.g:2012:6: otherlv_44= ','
                    	            {
                    	            otherlv_44=(Token)match(input,20,FOLLOW_33); if (state.failed) return current;
                    	            if ( state.backtracking==0 ) {

                    	              						newLeafNode(otherlv_44, grammarAccess.getInputSectionAccess().getCommaKeyword_3_7_0());
                    	              					
                    	            }

                    	            }
                    	            break;

                    	    }

                    	    // InternalLinkerScript.g:2017:5: ( (lv_sections_45_0= ruleWildcardRule ) )
                    	    // InternalLinkerScript.g:2018:6: (lv_sections_45_0= ruleWildcardRule )
                    	    {
                    	    // InternalLinkerScript.g:2018:6: (lv_sections_45_0= ruleWildcardRule )
                    	    // InternalLinkerScript.g:2019:7: lv_sections_45_0= ruleWildcardRule
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      							newCompositeNode(grammarAccess.getInputSectionAccess().getSectionsWildcardRuleParserRuleCall_3_7_1_0());
                    	      						
                    	    }
                    	    pushFollow(FOLLOW_34);
                    	    lv_sections_45_0=ruleWildcardRule();

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
                    	      								"org.eclipse.cdt.linkerscript.LinkerScript.WildcardRule");
                    	      							afterParserOrEnumRuleCall();
                    	      						
                    	    }

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);

                    otherlv_46=(Token)match(input,14,FOLLOW_9); if (state.failed) return current;
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


    // $ANTLR start "entryRuleWildcardRule"
    // InternalLinkerScript.g:2050:1: entryRuleWildcardRule returns [EObject current=null] : iv_ruleWildcardRule= ruleWildcardRule EOF ;
    public final EObject entryRuleWildcardRule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleWildcardRule = null;


        try {
            // InternalLinkerScript.g:2050:53: (iv_ruleWildcardRule= ruleWildcardRule EOF )
            // InternalLinkerScript.g:2051:2: iv_ruleWildcardRule= ruleWildcardRule EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getWildcardRuleRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleWildcardRule=ruleWildcardRule();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleWildcardRule; 
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
    // $ANTLR end "entryRuleWildcardRule"


    // $ANTLR start "ruleWildcardRule"
    // InternalLinkerScript.g:2057:1: ruleWildcardRule returns [EObject current=null] : ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) ) ;
    public final EObject ruleWildcardRule() throws RecognitionException {
        EObject current = null;

        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_25=null;
        Token otherlv_26=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        Token otherlv_30=null;
        Token otherlv_33=null;
        Token otherlv_35=null;
        Token otherlv_37=null;
        Token otherlv_38=null;
        Token otherlv_40=null;
        Token otherlv_41=null;
        Token otherlv_43=null;
        Token otherlv_45=null;
        Token otherlv_46=null;
        Token otherlv_48=null;
        Token otherlv_49=null;
        Token otherlv_50=null;
        Token otherlv_51=null;
        Token otherlv_53=null;
        Token otherlv_54=null;
        Token otherlv_57=null;
        Token otherlv_58=null;
        Token otherlv_59=null;
        Token otherlv_61=null;
        Token otherlv_63=null;
        Token otherlv_65=null;
        Token otherlv_66=null;
        Token otherlv_68=null;
        AntlrDatatypeRuleToken lv_name_1_0 = null;

        AntlrDatatypeRuleToken lv_excludes_5_0 = null;

        AntlrDatatypeRuleToken lv_name_7_0 = null;

        AntlrDatatypeRuleToken lv_name_11_0 = null;

        AntlrDatatypeRuleToken lv_name_16_0 = null;

        AntlrDatatypeRuleToken lv_name_21_0 = null;

        AntlrDatatypeRuleToken lv_name_28_0 = null;

        AntlrDatatypeRuleToken lv_name_36_0 = null;

        AntlrDatatypeRuleToken lv_name_44_0 = null;

        AntlrDatatypeRuleToken lv_name_52_0 = null;

        AntlrDatatypeRuleToken lv_excludes_60_0 = null;

        AntlrDatatypeRuleToken lv_name_62_0 = null;

        AntlrDatatypeRuleToken lv_name_67_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2063:2: ( ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) ) )
            // InternalLinkerScript.g:2064:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) )
            {
            // InternalLinkerScript.g:2064:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) )
            int alt39=11;
            alt39 = dfa39.predict(input);
            switch (alt39) {
                case 1 :
                    // InternalLinkerScript.g:2065:3: ( () ( (lv_name_1_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:2065:3: ( () ( (lv_name_1_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:2066:4: () ( (lv_name_1_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:2066:4: ()
                    // InternalLinkerScript.g:2067:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNoneAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:2073:4: ( (lv_name_1_0= ruleWildID ) )
                    // InternalLinkerScript.g:2074:5: (lv_name_1_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2074:5: (lv_name_1_0= ruleWildID )
                    // InternalLinkerScript.g:2075:6: lv_name_1_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_1_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
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
                    // InternalLinkerScript.g:2094:3: ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) )
                    {
                    // InternalLinkerScript.g:2094:3: ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) )
                    // InternalLinkerScript.g:2095:4: () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) )
                    {
                    // InternalLinkerScript.g:2095:4: ()
                    // InternalLinkerScript.g:2096:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNoneAction_1_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_3=(Token)match(input,57,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getWildcardRuleAccess().getEXCLUDE_FILEKeyword_1_1());
                      			
                    }
                    otherlv_4=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_1_2());
                      			
                    }
                    // InternalLinkerScript.g:2110:4: ( (lv_excludes_5_0= ruleWildID ) )+
                    int cnt37=0;
                    loop37:
                    do {
                        int alt37=2;
                        int LA37_0 = input.LA(1);

                        if ( (LA37_0==RULE_ID||LA37_0==62||(LA37_0>=64 && LA37_0<=65)||(LA37_0>=67 && LA37_0<=68)||LA37_0==77) ) {
                            alt37=1;
                        }


                        switch (alt37) {
                    	case 1 :
                    	    // InternalLinkerScript.g:2111:5: (lv_excludes_5_0= ruleWildID )
                    	    {
                    	    // InternalLinkerScript.g:2111:5: (lv_excludes_5_0= ruleWildID )
                    	    // InternalLinkerScript.g:2112:6: lv_excludes_5_0= ruleWildID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getExcludesWildIDParserRuleCall_1_3_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_36);
                    	    lv_excludes_5_0=ruleWildID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
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
                    	    if ( cnt37 >= 1 ) break loop37;
                    	    if (state.backtracking>0) {state.failed=true; return current;}
                                EarlyExitException eee =
                                    new EarlyExitException(37, input);
                                throw eee;
                        }
                        cnt37++;
                    } while (true);

                    otherlv_6=(Token)match(input,14,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_6, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_1_4());
                      			
                    }
                    // InternalLinkerScript.g:2133:4: ( (lv_name_7_0= ruleWildID ) )
                    // InternalLinkerScript.g:2134:5: (lv_name_7_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2134:5: (lv_name_7_0= ruleWildID )
                    // InternalLinkerScript.g:2135:6: lv_name_7_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_1_5_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_name_7_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
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
                    // InternalLinkerScript.g:2154:3: ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' )
                    {
                    // InternalLinkerScript.g:2154:3: ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' )
                    // InternalLinkerScript.g:2155:4: () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')'
                    {
                    // InternalLinkerScript.g:2155:4: ()
                    // InternalLinkerScript.g:2156:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAction_2_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_2_1());
                      			
                    }
                    pushFollow(FOLLOW_12);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_10=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_2_2());
                      			
                    }
                    // InternalLinkerScript.g:2173:4: ( (lv_name_11_0= ruleWildID ) )
                    // InternalLinkerScript.g:2174:5: (lv_name_11_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2174:5: (lv_name_11_0= ruleWildID )
                    // InternalLinkerScript.g:2175:6: lv_name_11_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_2_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_11_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
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

                      				newLeafNode(otherlv_12, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_2_4());
                      			
                    }

                    }


                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:2198:3: ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' )
                    {
                    // InternalLinkerScript.g:2198:3: ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' )
                    // InternalLinkerScript.g:2199:4: () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')'
                    {
                    // InternalLinkerScript.g:2199:4: ()
                    // InternalLinkerScript.g:2200:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortAlignAction_3_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_14=(Token)match(input,58,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_14, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_3_1());
                      			
                    }
                    otherlv_15=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_15, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_3_2());
                      			
                    }
                    // InternalLinkerScript.g:2214:4: ( (lv_name_16_0= ruleWildID ) )
                    // InternalLinkerScript.g:2215:5: (lv_name_16_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2215:5: (lv_name_16_0= ruleWildID )
                    // InternalLinkerScript.g:2216:6: lv_name_16_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_3_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_16_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_16_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_17=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_17, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_3_4());
                      			
                    }

                    }


                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:2239:3: ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' )
                    {
                    // InternalLinkerScript.g:2239:3: ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' )
                    // InternalLinkerScript.g:2240:4: () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')'
                    {
                    // InternalLinkerScript.g:2240:4: ()
                    // InternalLinkerScript.g:2241:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNoneAction_4_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_19=(Token)match(input,59,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_19, grammarAccess.getWildcardRuleAccess().getSORT_NONEKeyword_4_1());
                      			
                    }
                    otherlv_20=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_20, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_4_2());
                      			
                    }
                    // InternalLinkerScript.g:2255:4: ( (lv_name_21_0= ruleWildID ) )
                    // InternalLinkerScript.g:2256:5: (lv_name_21_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2256:5: (lv_name_21_0= ruleWildID )
                    // InternalLinkerScript.g:2257:6: lv_name_21_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_4_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_21_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
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

                    otherlv_22=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_22, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_4_4());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:2280:3: ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' )
                    {
                    // InternalLinkerScript.g:2280:3: ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' )
                    // InternalLinkerScript.g:2281:4: () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')'
                    {
                    // InternalLinkerScript.g:2281:4: ()
                    // InternalLinkerScript.g:2282:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAlignAction_5_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_5_1());
                      			
                    }
                    pushFollow(FOLLOW_12);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_25=(Token)match(input,13,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_25, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_5_2());
                      			
                    }
                    otherlv_26=(Token)match(input,58,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_26, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_5_3());
                      			
                    }
                    otherlv_27=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_27, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_5_4());
                      			
                    }
                    // InternalLinkerScript.g:2307:4: ( (lv_name_28_0= ruleWildID ) )
                    // InternalLinkerScript.g:2308:5: (lv_name_28_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2308:5: (lv_name_28_0= ruleWildID )
                    // InternalLinkerScript.g:2309:6: lv_name_28_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_5_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_28_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
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

                    otherlv_29=(Token)match(input,14,FOLLOW_9); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_29, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_5_6());
                      			
                    }
                    otherlv_30=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_30, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_5_7());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:2336:3: ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' )
                    {
                    // InternalLinkerScript.g:2336:3: ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' )
                    // InternalLinkerScript.g:2337:4: () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')'
                    {
                    // InternalLinkerScript.g:2337:4: ()
                    // InternalLinkerScript.g:2338:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAction_6_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_6_1());
                      			
                    }
                    pushFollow(FOLLOW_12);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_33=(Token)match(input,13,FOLLOW_38); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_33, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_6_2());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_6_3());
                      			
                    }
                    pushFollow(FOLLOW_12);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_35=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_35, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_6_4());
                      			
                    }
                    // InternalLinkerScript.g:2366:4: ( (lv_name_36_0= ruleWildID ) )
                    // InternalLinkerScript.g:2367:5: (lv_name_36_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2367:5: (lv_name_36_0= ruleWildID )
                    // InternalLinkerScript.g:2368:6: lv_name_36_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_6_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_36_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_36_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_37=(Token)match(input,14,FOLLOW_9); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_37, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_6_6());
                      			
                    }
                    otherlv_38=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_38, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_6_7());
                      			
                    }

                    }


                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:2395:3: ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' )
                    {
                    // InternalLinkerScript.g:2395:3: ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' )
                    // InternalLinkerScript.g:2396:4: () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')'
                    {
                    // InternalLinkerScript.g:2396:4: ()
                    // InternalLinkerScript.g:2397:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortAlignNameAction_7_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_40=(Token)match(input,58,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_40, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_7_1());
                      			
                    }
                    otherlv_41=(Token)match(input,13,FOLLOW_38); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_41, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_7_2());
                      			
                    }
                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_7_3());
                      			
                    }
                    pushFollow(FOLLOW_12);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_43=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_43, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_7_4());
                      			
                    }
                    // InternalLinkerScript.g:2422:4: ( (lv_name_44_0= ruleWildID ) )
                    // InternalLinkerScript.g:2423:5: (lv_name_44_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2423:5: (lv_name_44_0= ruleWildID )
                    // InternalLinkerScript.g:2424:6: lv_name_44_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_7_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_44_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_44_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_45=(Token)match(input,14,FOLLOW_9); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_45, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_7_6());
                      			
                    }
                    otherlv_46=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_46, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_7_7());
                      			
                    }

                    }


                    }
                    break;
                case 9 :
                    // InternalLinkerScript.g:2451:3: ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' )
                    {
                    // InternalLinkerScript.g:2451:3: ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' )
                    // InternalLinkerScript.g:2452:4: () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')'
                    {
                    // InternalLinkerScript.g:2452:4: ()
                    // InternalLinkerScript.g:2453:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortAlignAction_8_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_48=(Token)match(input,58,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_48, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_8_1());
                      			
                    }
                    otherlv_49=(Token)match(input,13,FOLLOW_37); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_49, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_8_2());
                      			
                    }
                    otherlv_50=(Token)match(input,58,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_50, grammarAccess.getWildcardRuleAccess().getSORT_BY_ALIGNMENTKeyword_8_3());
                      			
                    }
                    otherlv_51=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_51, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_8_4());
                      			
                    }
                    // InternalLinkerScript.g:2475:4: ( (lv_name_52_0= ruleWildID ) )
                    // InternalLinkerScript.g:2476:5: (lv_name_52_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2476:5: (lv_name_52_0= ruleWildID )
                    // InternalLinkerScript.g:2477:6: lv_name_52_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_8_5_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_52_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_52_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_53=(Token)match(input,14,FOLLOW_9); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_53, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_8_6());
                      			
                    }
                    otherlv_54=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_54, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_8_7());
                      			
                    }

                    }


                    }
                    break;
                case 10 :
                    // InternalLinkerScript.g:2504:3: ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' )
                    {
                    // InternalLinkerScript.g:2504:3: ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' )
                    // InternalLinkerScript.g:2505:4: () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')'
                    {
                    // InternalLinkerScript.g:2505:4: ()
                    // InternalLinkerScript.g:2506:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortNameAction_9_0(),
                      						current);
                      				
                    }

                    }

                    if ( state.backtracking==0 ) {

                      				newCompositeNode(grammarAccess.getWildcardRuleAccess().getSORT_BY_NAMEParserRuleCall_9_1());
                      			
                    }
                    pushFollow(FOLLOW_12);
                    ruleSORT_BY_NAME();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				afterParserOrEnumRuleCall();
                      			
                    }
                    otherlv_57=(Token)match(input,13,FOLLOW_39); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_57, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_9_2());
                      			
                    }
                    otherlv_58=(Token)match(input,57,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_58, grammarAccess.getWildcardRuleAccess().getEXCLUDE_FILEKeyword_9_3());
                      			
                    }
                    otherlv_59=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_59, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_9_4());
                      			
                    }
                    // InternalLinkerScript.g:2531:4: ( (lv_excludes_60_0= ruleWildID ) )+
                    int cnt38=0;
                    loop38:
                    do {
                        int alt38=2;
                        int LA38_0 = input.LA(1);

                        if ( (LA38_0==RULE_ID||LA38_0==62||(LA38_0>=64 && LA38_0<=65)||(LA38_0>=67 && LA38_0<=68)||LA38_0==77) ) {
                            alt38=1;
                        }


                        switch (alt38) {
                    	case 1 :
                    	    // InternalLinkerScript.g:2532:5: (lv_excludes_60_0= ruleWildID )
                    	    {
                    	    // InternalLinkerScript.g:2532:5: (lv_excludes_60_0= ruleWildID )
                    	    // InternalLinkerScript.g:2533:6: lv_excludes_60_0= ruleWildID
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getExcludesWildIDParserRuleCall_9_5_0());
                    	      					
                    	    }
                    	    pushFollow(FOLLOW_36);
                    	    lv_excludes_60_0=ruleWildID();

                    	    state._fsp--;
                    	    if (state.failed) return current;
                    	    if ( state.backtracking==0 ) {

                    	      						if (current==null) {
                    	      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                    	      						}
                    	      						add(
                    	      							current,
                    	      							"excludes",
                    	      							lv_excludes_60_0,
                    	      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                    	      						afterParserOrEnumRuleCall();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt38 >= 1 ) break loop38;
                    	    if (state.backtracking>0) {state.failed=true; return current;}
                                EarlyExitException eee =
                                    new EarlyExitException(38, input);
                                throw eee;
                        }
                        cnt38++;
                    } while (true);

                    otherlv_61=(Token)match(input,14,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_61, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_9_6());
                      			
                    }
                    // InternalLinkerScript.g:2554:4: ( (lv_name_62_0= ruleWildID ) )
                    // InternalLinkerScript.g:2555:5: (lv_name_62_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2555:5: (lv_name_62_0= ruleWildID )
                    // InternalLinkerScript.g:2556:6: lv_name_62_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_9_7_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_62_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_62_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_63=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_63, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_9_8());
                      			
                    }

                    }


                    }
                    break;
                case 11 :
                    // InternalLinkerScript.g:2579:3: ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' )
                    {
                    // InternalLinkerScript.g:2579:3: ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' )
                    // InternalLinkerScript.g:2580:4: () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')'
                    {
                    // InternalLinkerScript.g:2580:4: ()
                    // InternalLinkerScript.g:2581:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getWildcardRuleAccess().getWildcardSortInitPriorityAction_10_0(),
                      						current);
                      				
                    }

                    }

                    otherlv_65=(Token)match(input,60,FOLLOW_12); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_65, grammarAccess.getWildcardRuleAccess().getSORT_BY_INIT_PRIORITYKeyword_10_1());
                      			
                    }
                    otherlv_66=(Token)match(input,13,FOLLOW_27); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_66, grammarAccess.getWildcardRuleAccess().getLeftParenthesisKeyword_10_2());
                      			
                    }
                    // InternalLinkerScript.g:2595:4: ( (lv_name_67_0= ruleWildID ) )
                    // InternalLinkerScript.g:2596:5: (lv_name_67_0= ruleWildID )
                    {
                    // InternalLinkerScript.g:2596:5: (lv_name_67_0= ruleWildID )
                    // InternalLinkerScript.g:2597:6: lv_name_67_0= ruleWildID
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getWildcardRuleAccess().getNameWildIDParserRuleCall_10_3_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
                    lv_name_67_0=ruleWildID();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getWildcardRuleRule());
                      						}
                      						set(
                      							current,
                      							"name",
                      							lv_name_67_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.WildID");
                      						afterParserOrEnumRuleCall();
                      					
                    }

                    }


                    }

                    otherlv_68=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_68, grammarAccess.getWildcardRuleAccess().getRightParenthesisKeyword_10_4());
                      			
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
    // $ANTLR end "ruleWildcardRule"


    // $ANTLR start "entryRuleSORT_BY_NAME"
    // InternalLinkerScript.g:2623:1: entryRuleSORT_BY_NAME returns [String current=null] : iv_ruleSORT_BY_NAME= ruleSORT_BY_NAME EOF ;
    public final String entryRuleSORT_BY_NAME() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSORT_BY_NAME = null;


        try {
            // InternalLinkerScript.g:2623:52: (iv_ruleSORT_BY_NAME= ruleSORT_BY_NAME EOF )
            // InternalLinkerScript.g:2624:2: iv_ruleSORT_BY_NAME= ruleSORT_BY_NAME EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getSORT_BY_NAMERule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleSORT_BY_NAME=ruleSORT_BY_NAME();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleSORT_BY_NAME.getText(); 
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
    // $ANTLR end "entryRuleSORT_BY_NAME"


    // $ANTLR start "ruleSORT_BY_NAME"
    // InternalLinkerScript.g:2630:1: ruleSORT_BY_NAME returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'SORT' | kw= 'SORT_BY_NAME' ) ;
    public final AntlrDatatypeRuleToken ruleSORT_BY_NAME() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:2636:2: ( (kw= 'SORT' | kw= 'SORT_BY_NAME' ) )
            // InternalLinkerScript.g:2637:2: (kw= 'SORT' | kw= 'SORT_BY_NAME' )
            {
            // InternalLinkerScript.g:2637:2: (kw= 'SORT' | kw= 'SORT_BY_NAME' )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==61) ) {
                alt40=1;
            }
            else if ( (LA40_0==34) ) {
                alt40=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // InternalLinkerScript.g:2638:3: kw= 'SORT'
                    {
                    kw=(Token)match(input,61,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSORT_BY_NAMEAccess().getSORTKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2644:3: kw= 'SORT_BY_NAME'
                    {
                    kw=(Token)match(input,34,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getSORT_BY_NAMEAccess().getSORT_BY_NAMEKeyword_1());
                      		
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
    // $ANTLR end "ruleSORT_BY_NAME"


    // $ANTLR start "entryRuleMemoryCommand"
    // InternalLinkerScript.g:2653:1: entryRuleMemoryCommand returns [EObject current=null] : iv_ruleMemoryCommand= ruleMemoryCommand EOF ;
    public final EObject entryRuleMemoryCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemoryCommand = null;


        try {
            // InternalLinkerScript.g:2653:54: (iv_ruleMemoryCommand= ruleMemoryCommand EOF )
            // InternalLinkerScript.g:2654:2: iv_ruleMemoryCommand= ruleMemoryCommand EOF
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
    // InternalLinkerScript.g:2660:1: ruleMemoryCommand returns [EObject current=null] : (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' ) ;
    public final EObject ruleMemoryCommand() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_4=null;
        EObject lv_memories_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2666:2: ( (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' ) )
            // InternalLinkerScript.g:2667:2: (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' )
            {
            // InternalLinkerScript.g:2667:2: (otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}' )
            // InternalLinkerScript.g:2668:3: otherlv_0= 'MEMORY' otherlv_1= '{' () ( (lv_memories_3_0= ruleMemory ) )* otherlv_4= '}'
            {
            otherlv_0=(Token)match(input,62,FOLLOW_4); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getMemoryCommandAccess().getMEMORYKeyword_0());
              		
            }
            otherlv_1=(Token)match(input,11,FOLLOW_5); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getMemoryCommandAccess().getLeftCurlyBracketKeyword_1());
              		
            }
            // InternalLinkerScript.g:2676:3: ()
            // InternalLinkerScript.g:2677:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getMemoryCommandAccess().getMemoryCommandAction_2(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:2683:3: ( (lv_memories_3_0= ruleMemory ) )*
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( (LA41_0==RULE_ID||LA41_0==62||(LA41_0>=64 && LA41_0<=65)||(LA41_0>=67 && LA41_0<=68)) ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // InternalLinkerScript.g:2684:4: (lv_memories_3_0= ruleMemory )
            	    {
            	    // InternalLinkerScript.g:2684:4: (lv_memories_3_0= ruleMemory )
            	    // InternalLinkerScript.g:2685:5: lv_memories_3_0= ruleMemory
            	    {
            	    if ( state.backtracking==0 ) {

            	      					newCompositeNode(grammarAccess.getMemoryCommandAccess().getMemoriesMemoryParserRuleCall_3_0());
            	      				
            	    }
            	    pushFollow(FOLLOW_5);
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
            	    break loop41;
                }
            } while (true);

            otherlv_4=(Token)match(input,12,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:2710:1: entryRuleMemory returns [EObject current=null] : iv_ruleMemory= ruleMemory EOF ;
    public final EObject entryRuleMemory() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMemory = null;


        try {
            // InternalLinkerScript.g:2710:47: (iv_ruleMemory= ruleMemory EOF )
            // InternalLinkerScript.g:2711:2: iv_ruleMemory= ruleMemory EOF
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
    // InternalLinkerScript.g:2717:1: ruleMemory returns [EObject current=null] : ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) ) ;
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
            // InternalLinkerScript.g:2723:2: ( ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) ) )
            // InternalLinkerScript.g:2724:2: ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) )
            {
            // InternalLinkerScript.g:2724:2: ( ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) ) )
            // InternalLinkerScript.g:2725:3: ( (lv_name_0_0= ruleMemoryName ) ) ( (lv_attr_1_0= ruleMemoryAttribute ) )? otherlv_2= ':' (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' ) otherlv_6= '=' ( (lv_origin_7_0= ruleLExpression ) ) otherlv_8= ',' (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' ) otherlv_12= '=' ( (lv_length_13_0= ruleLExpression ) )
            {
            // InternalLinkerScript.g:2725:3: ( (lv_name_0_0= ruleMemoryName ) )
            // InternalLinkerScript.g:2726:4: (lv_name_0_0= ruleMemoryName )
            {
            // InternalLinkerScript.g:2726:4: (lv_name_0_0= ruleMemoryName )
            // InternalLinkerScript.g:2727:5: lv_name_0_0= ruleMemoryName
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getNameMemoryNameParserRuleCall_0_0());
              				
            }
            pushFollow(FOLLOW_7);
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

            // InternalLinkerScript.g:2744:3: ( (lv_attr_1_0= ruleMemoryAttribute ) )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==13) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // InternalLinkerScript.g:2745:4: (lv_attr_1_0= ruleMemoryAttribute )
                    {
                    // InternalLinkerScript.g:2745:4: (lv_attr_1_0= ruleMemoryAttribute )
                    // InternalLinkerScript.g:2746:5: lv_attr_1_0= ruleMemoryAttribute
                    {
                    if ( state.backtracking==0 ) {

                      					newCompositeNode(grammarAccess.getMemoryAccess().getAttrMemoryAttributeParserRuleCall_1_0());
                      				
                    }
                    pushFollow(FOLLOW_10);
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

            otherlv_2=(Token)match(input,15,FOLLOW_40); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getMemoryAccess().getColonKeyword_2());
              		
            }
            // InternalLinkerScript.g:2767:3: (otherlv_3= 'ORIGIN' | otherlv_4= 'org' | otherlv_5= 'o' )
            int alt43=3;
            switch ( input.LA(1) ) {
            case 63:
                {
                alt43=1;
                }
                break;
            case 64:
                {
                alt43=2;
                }
                break;
            case 65:
                {
                alt43=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }

            switch (alt43) {
                case 1 :
                    // InternalLinkerScript.g:2768:4: otherlv_3= 'ORIGIN'
                    {
                    otherlv_3=(Token)match(input,63,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_3, grammarAccess.getMemoryAccess().getORIGINKeyword_3_0());
                      			
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2773:4: otherlv_4= 'org'
                    {
                    otherlv_4=(Token)match(input,64,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getMemoryAccess().getOrgKeyword_3_1());
                      			
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2778:4: otherlv_5= 'o'
                    {
                    otherlv_5=(Token)match(input,65,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_5, grammarAccess.getMemoryAccess().getOKeyword_3_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_6=(Token)match(input,19,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_6, grammarAccess.getMemoryAccess().getEqualsSignKeyword_4());
              		
            }
            // InternalLinkerScript.g:2787:3: ( (lv_origin_7_0= ruleLExpression ) )
            // InternalLinkerScript.g:2788:4: (lv_origin_7_0= ruleLExpression )
            {
            // InternalLinkerScript.g:2788:4: (lv_origin_7_0= ruleLExpression )
            // InternalLinkerScript.g:2789:5: lv_origin_7_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getMemoryAccess().getOriginLExpressionParserRuleCall_5_0());
              				
            }
            pushFollow(FOLLOW_26);
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

            otherlv_8=(Token)match(input,20,FOLLOW_41); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_8, grammarAccess.getMemoryAccess().getCommaKeyword_6());
              		
            }
            // InternalLinkerScript.g:2810:3: (otherlv_9= 'LENGTH' | otherlv_10= 'len' | otherlv_11= 'l' )
            int alt44=3;
            switch ( input.LA(1) ) {
            case 66:
                {
                alt44=1;
                }
                break;
            case 67:
                {
                alt44=2;
                }
                break;
            case 68:
                {
                alt44=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }

            switch (alt44) {
                case 1 :
                    // InternalLinkerScript.g:2811:4: otherlv_9= 'LENGTH'
                    {
                    otherlv_9=(Token)match(input,66,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_9, grammarAccess.getMemoryAccess().getLENGTHKeyword_7_0());
                      			
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:2816:4: otherlv_10= 'len'
                    {
                    otherlv_10=(Token)match(input,67,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_10, grammarAccess.getMemoryAccess().getLenKeyword_7_1());
                      			
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:2821:4: otherlv_11= 'l'
                    {
                    otherlv_11=(Token)match(input,68,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_11, grammarAccess.getMemoryAccess().getLKeyword_7_2());
                      			
                    }

                    }
                    break;

            }

            otherlv_12=(Token)match(input,19,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_12, grammarAccess.getMemoryAccess().getEqualsSignKeyword_8());
              		
            }
            // InternalLinkerScript.g:2830:3: ( (lv_length_13_0= ruleLExpression ) )
            // InternalLinkerScript.g:2831:4: (lv_length_13_0= ruleLExpression )
            {
            // InternalLinkerScript.g:2831:4: (lv_length_13_0= ruleLExpression )
            // InternalLinkerScript.g:2832:5: lv_length_13_0= ruleLExpression
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
    // InternalLinkerScript.g:2853:1: entryRuleMemoryName returns [String current=null] : iv_ruleMemoryName= ruleMemoryName EOF ;
    public final String entryRuleMemoryName() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMemoryName = null;


        try {
            // InternalLinkerScript.g:2853:50: (iv_ruleMemoryName= ruleMemoryName EOF )
            // InternalLinkerScript.g:2854:2: iv_ruleMemoryName= ruleMemoryName EOF
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
    // InternalLinkerScript.g:2860:1: ruleMemoryName returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : this_ValidID_0= ruleValidID ;
    public final AntlrDatatypeRuleToken ruleMemoryName() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        AntlrDatatypeRuleToken this_ValidID_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2866:2: (this_ValidID_0= ruleValidID )
            // InternalLinkerScript.g:2867:2: this_ValidID_0= ruleValidID
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
    // InternalLinkerScript.g:2880:1: entryRuleMemoryAttribute returns [String current=null] : iv_ruleMemoryAttribute= ruleMemoryAttribute EOF ;
    public final String entryRuleMemoryAttribute() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleMemoryAttribute = null;


        try {
            // InternalLinkerScript.g:2880:55: (iv_ruleMemoryAttribute= ruleMemoryAttribute EOF )
            // InternalLinkerScript.g:2881:2: iv_ruleMemoryAttribute= ruleMemoryAttribute EOF
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
    // InternalLinkerScript.g:2887:1: ruleMemoryAttribute returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' ) ;
    public final AntlrDatatypeRuleToken ruleMemoryAttribute() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_WildID_2 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2893:2: ( (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' ) )
            // InternalLinkerScript.g:2894:2: (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' )
            {
            // InternalLinkerScript.g:2894:2: (kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')' )
            // InternalLinkerScript.g:2895:3: kw= '(' ( (kw= '!' )? this_WildID_2= ruleWildID )+ kw= ')'
            {
            kw=(Token)match(input,13,FOLLOW_42); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current.merge(kw);
              			newLeafNode(kw, grammarAccess.getMemoryAttributeAccess().getLeftParenthesisKeyword_0());
              		
            }
            // InternalLinkerScript.g:2900:3: ( (kw= '!' )? this_WildID_2= ruleWildID )+
            int cnt46=0;
            loop46:
            do {
                int alt46=2;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==RULE_ID||LA46_0==62||(LA46_0>=64 && LA46_0<=65)||(LA46_0>=67 && LA46_0<=69)||LA46_0==77) ) {
                    alt46=1;
                }


                switch (alt46) {
            	case 1 :
            	    // InternalLinkerScript.g:2901:4: (kw= '!' )? this_WildID_2= ruleWildID
            	    {
            	    // InternalLinkerScript.g:2901:4: (kw= '!' )?
            	    int alt45=2;
            	    int LA45_0 = input.LA(1);

            	    if ( (LA45_0==69) ) {
            	        alt45=1;
            	    }
            	    switch (alt45) {
            	        case 1 :
            	            // InternalLinkerScript.g:2902:5: kw= '!'
            	            {
            	            kw=(Token)match(input,69,FOLLOW_27); if (state.failed) return current;
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
            	    pushFollow(FOLLOW_43);
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
            	    if ( cnt46 >= 1 ) break loop46;
            	    if (state.backtracking>0) {state.failed=true; return current;}
                        EarlyExitException eee =
                            new EarlyExitException(46, input);
                        throw eee;
                }
                cnt46++;
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
    // InternalLinkerScript.g:2928:1: entryRuleLExpression returns [EObject current=null] : iv_ruleLExpression= ruleLExpression EOF ;
    public final EObject entryRuleLExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLExpression = null;


        try {
            // InternalLinkerScript.g:2928:52: (iv_ruleLExpression= ruleLExpression EOF )
            // InternalLinkerScript.g:2929:2: iv_ruleLExpression= ruleLExpression EOF
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
    // InternalLinkerScript.g:2935:1: ruleLExpression returns [EObject current=null] : this_LAssignment_0= ruleLAssignment ;
    public final EObject ruleLExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAssignment_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2941:2: (this_LAssignment_0= ruleLAssignment )
            // InternalLinkerScript.g:2942:2: this_LAssignment_0= ruleLAssignment
            {
            if ( state.backtracking==0 ) {

              		newCompositeNode(grammarAccess.getLExpressionAccess().getLAssignmentParserRuleCall());
              	
            }
            pushFollow(FOLLOW_2);
            this_LAssignment_0=ruleLAssignment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              		current = this_LAssignment_0;
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


    // $ANTLR start "entryRuleLAssignment"
    // InternalLinkerScript.g:2953:1: entryRuleLAssignment returns [EObject current=null] : iv_ruleLAssignment= ruleLAssignment EOF ;
    public final EObject entryRuleLAssignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAssignment = null;


        try {
            // InternalLinkerScript.g:2953:52: (iv_ruleLAssignment= ruleLAssignment EOF )
            // InternalLinkerScript.g:2954:2: iv_ruleLAssignment= ruleLAssignment EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLAssignmentRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLAssignment=ruleLAssignment();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLAssignment; 
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
    // $ANTLR end "entryRuleLAssignment"


    // $ANTLR start "ruleLAssignment"
    // InternalLinkerScript.g:2960:1: ruleLAssignment returns [EObject current=null] : (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )? ) ;
    public final EObject ruleLAssignment() throws RecognitionException {
        EObject current = null;

        EObject this_LOrExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:2966:2: ( (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )? ) )
            // InternalLinkerScript.g:2967:2: (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )? )
            {
            // InternalLinkerScript.g:2967:2: (this_LOrExpression_0= ruleLOrExpression ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )? )
            // InternalLinkerScript.g:2968:3: this_LOrExpression_0= ruleLOrExpression ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAssignmentAccess().getLOrExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_44);
            this_LOrExpression_0=ruleLOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LOrExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:2976:3: ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )?
            int alt47=2;
            alt47 = dfa47.predict(input);
            switch (alt47) {
                case 1 :
                    // InternalLinkerScript.g:2977:4: ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) )
                    {
                    // InternalLinkerScript.g:2977:4: ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) )
                    // InternalLinkerScript.g:2978:5: ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) )
                    {
                    // InternalLinkerScript.g:2988:5: ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) )
                    // InternalLinkerScript.g:2989:6: () ( (lv_feature_2_0= ruleOpMultiAssign ) )
                    {
                    // InternalLinkerScript.g:2989:6: ()
                    // InternalLinkerScript.g:2990:7: 
                    {
                    if ( state.backtracking==0 ) {

                      							current = forceCreateModelElementAndSet(
                      								grammarAccess.getLAssignmentAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
                      								current);
                      						
                    }

                    }

                    // InternalLinkerScript.g:2996:6: ( (lv_feature_2_0= ruleOpMultiAssign ) )
                    // InternalLinkerScript.g:2997:7: (lv_feature_2_0= ruleOpMultiAssign )
                    {
                    // InternalLinkerScript.g:2997:7: (lv_feature_2_0= ruleOpMultiAssign )
                    // InternalLinkerScript.g:2998:8: lv_feature_2_0= ruleOpMultiAssign
                    {
                    if ( state.backtracking==0 ) {

                      								newCompositeNode(grammarAccess.getLAssignmentAccess().getFeatureOpMultiAssignParserRuleCall_1_0_0_1_0());
                      							
                    }
                    pushFollow(FOLLOW_13);
                    lv_feature_2_0=ruleOpMultiAssign();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      								if (current==null) {
                      									current = createModelElementForParent(grammarAccess.getLAssignmentRule());
                      								}
                      								set(
                      									current,
                      									"feature",
                      									lv_feature_2_0,
                      									"org.eclipse.cdt.linkerscript.LinkerScript.OpMultiAssign");
                      								afterParserOrEnumRuleCall();
                      							
                    }

                    }


                    }


                    }


                    }

                    // InternalLinkerScript.g:3017:4: ( (lv_rightOperand_3_0= ruleLAssignment ) )
                    // InternalLinkerScript.g:3018:5: (lv_rightOperand_3_0= ruleLAssignment )
                    {
                    // InternalLinkerScript.g:3018:5: (lv_rightOperand_3_0= ruleLAssignment )
                    // InternalLinkerScript.g:3019:6: lv_rightOperand_3_0= ruleLAssignment
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLAssignmentAccess().getRightOperandLAssignmentParserRuleCall_1_1_0());
                      					
                    }
                    pushFollow(FOLLOW_2);
                    lv_rightOperand_3_0=ruleLAssignment();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      						if (current==null) {
                      							current = createModelElementForParent(grammarAccess.getLAssignmentRule());
                      						}
                      						set(
                      							current,
                      							"rightOperand",
                      							lv_rightOperand_3_0,
                      							"org.eclipse.cdt.linkerscript.LinkerScript.LAssignment");
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
    // $ANTLR end "ruleLAssignment"


    // $ANTLR start "entryRuleOpMultiAssign"
    // InternalLinkerScript.g:3041:1: entryRuleOpMultiAssign returns [String current=null] : iv_ruleOpMultiAssign= ruleOpMultiAssign EOF ;
    public final String entryRuleOpMultiAssign() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpMultiAssign = null;


        try {
            // InternalLinkerScript.g:3041:53: (iv_ruleOpMultiAssign= ruleOpMultiAssign EOF )
            // InternalLinkerScript.g:3042:2: iv_ruleOpMultiAssign= ruleOpMultiAssign EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpMultiAssignRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpMultiAssign=ruleOpMultiAssign();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpMultiAssign.getText(); 
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
    // $ANTLR end "entryRuleOpMultiAssign"


    // $ANTLR start "ruleOpMultiAssign"
    // InternalLinkerScript.g:3048:1: ruleOpMultiAssign returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) ;
    public final AntlrDatatypeRuleToken ruleOpMultiAssign() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3054:2: ( (kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' ) )
            // InternalLinkerScript.g:3055:2: (kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            {
            // InternalLinkerScript.g:3055:2: (kw= '+=' | kw= '-=' | kw= '*=' | kw= '/=' | (kw= '<' kw= '<' kw= '=' ) | (kw= '>' kw= '>=' ) | kw= '&=' | kw= '|=' )
            int alt48=8;
            switch ( input.LA(1) ) {
            case 46:
                {
                alt48=1;
                }
                break;
            case 47:
                {
                alt48=2;
                }
                break;
            case 48:
                {
                alt48=3;
                }
                break;
            case 49:
                {
                alt48=4;
                }
                break;
            case 50:
                {
                alt48=5;
                }
                break;
            case 18:
                {
                alt48=6;
                }
                break;
            case 52:
                {
                alt48=7;
                }
                break;
            case 53:
                {
                alt48=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }

            switch (alt48) {
                case 1 :
                    // InternalLinkerScript.g:3056:3: kw= '+='
                    {
                    kw=(Token)match(input,46,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getPlusSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3062:3: kw= '-='
                    {
                    kw=(Token)match(input,47,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getHyphenMinusEqualsSignKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3068:3: kw= '*='
                    {
                    kw=(Token)match(input,48,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getAsteriskEqualsSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:3074:3: kw= '/='
                    {
                    kw=(Token)match(input,49,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getSolidusEqualsSignKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:3080:3: (kw= '<' kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:3080:3: (kw= '<' kw= '<' kw= '=' )
                    // InternalLinkerScript.g:3081:4: kw= '<' kw= '<' kw= '='
                    {
                    kw=(Token)match(input,50,FOLLOW_30); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getLessThanSignKeyword_4_0());
                      			
                    }
                    kw=(Token)match(input,50,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getLessThanSignKeyword_4_1());
                      			
                    }
                    kw=(Token)match(input,19,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getEqualsSignKeyword_4_2());
                      			
                    }

                    }


                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:3098:3: (kw= '>' kw= '>=' )
                    {
                    // InternalLinkerScript.g:3098:3: (kw= '>' kw= '>=' )
                    // InternalLinkerScript.g:3099:4: kw= '>' kw= '>='
                    {
                    kw=(Token)match(input,18,FOLLOW_31); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getGreaterThanSignKeyword_5_0());
                      			
                    }
                    kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getGreaterThanSignEqualsSignKeyword_5_1());
                      			
                    }

                    }


                    }
                    break;
                case 7 :
                    // InternalLinkerScript.g:3111:3: kw= '&='
                    {
                    kw=(Token)match(input,52,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getAmpersandEqualsSignKeyword_6());
                      		
                    }

                    }
                    break;
                case 8 :
                    // InternalLinkerScript.g:3117:3: kw= '|='
                    {
                    kw=(Token)match(input,53,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAssignAccess().getVerticalLineEqualsSignKeyword_7());
                      		
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
    // $ANTLR end "ruleOpMultiAssign"


    // $ANTLR start "entryRuleLOrExpression"
    // InternalLinkerScript.g:3126:1: entryRuleLOrExpression returns [EObject current=null] : iv_ruleLOrExpression= ruleLOrExpression EOF ;
    public final EObject entryRuleLOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLOrExpression = null;


        try {
            // InternalLinkerScript.g:3126:54: (iv_ruleLOrExpression= ruleLOrExpression EOF )
            // InternalLinkerScript.g:3127:2: iv_ruleLOrExpression= ruleLOrExpression EOF
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
    // InternalLinkerScript.g:3133:1: ruleLOrExpression returns [EObject current=null] : (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* ) ;
    public final EObject ruleLOrExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAndExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3139:2: ( (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* ) )
            // InternalLinkerScript.g:3140:2: (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* )
            {
            // InternalLinkerScript.g:3140:2: (this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )* )
            // InternalLinkerScript.g:3141:3: this_LAndExpression_0= ruleLAndExpression ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLOrExpressionAccess().getLAndExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_45);
            this_LAndExpression_0=ruleLAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LAndExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3149:3: ( ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) ) )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==70) && (synpred2_InternalLinkerScript())) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // InternalLinkerScript.g:3150:4: ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLAndExpression ) )
            	    {
            	    // InternalLinkerScript.g:3150:4: ( ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) ) )
            	    // InternalLinkerScript.g:3151:5: ( ( () ( ( ruleOpOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOr ) ) )
            	    {
            	    // InternalLinkerScript.g:3161:5: ( () ( (lv_feature_2_0= ruleOpOr ) ) )
            	    // InternalLinkerScript.g:3162:6: () ( (lv_feature_2_0= ruleOpOr ) )
            	    {
            	    // InternalLinkerScript.g:3162:6: ()
            	    // InternalLinkerScript.g:3163:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLOrExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3169:6: ( (lv_feature_2_0= ruleOpOr ) )
            	    // InternalLinkerScript.g:3170:7: (lv_feature_2_0= ruleOpOr )
            	    {
            	    // InternalLinkerScript.g:3170:7: (lv_feature_2_0= ruleOpOr )
            	    // InternalLinkerScript.g:3171:8: lv_feature_2_0= ruleOpOr
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLOrExpressionAccess().getFeatureOpOrParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3190:4: ( (lv_rightOperand_3_0= ruleLAndExpression ) )
            	    // InternalLinkerScript.g:3191:5: (lv_rightOperand_3_0= ruleLAndExpression )
            	    {
            	    // InternalLinkerScript.g:3191:5: (lv_rightOperand_3_0= ruleLAndExpression )
            	    // InternalLinkerScript.g:3192:6: lv_rightOperand_3_0= ruleLAndExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLOrExpressionAccess().getRightOperandLAndExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_45);
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
            	    break loop49;
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
    // InternalLinkerScript.g:3214:1: entryRuleOpOr returns [String current=null] : iv_ruleOpOr= ruleOpOr EOF ;
    public final String entryRuleOpOr() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpOr = null;


        try {
            // InternalLinkerScript.g:3214:44: (iv_ruleOpOr= ruleOpOr EOF )
            // InternalLinkerScript.g:3215:2: iv_ruleOpOr= ruleOpOr EOF
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
    // InternalLinkerScript.g:3221:1: ruleOpOr returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '||' ;
    public final AntlrDatatypeRuleToken ruleOpOr() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3227:2: (kw= '||' )
            // InternalLinkerScript.g:3228:2: kw= '||'
            {
            kw=(Token)match(input,70,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3236:1: entryRuleLAndExpression returns [EObject current=null] : iv_ruleLAndExpression= ruleLAndExpression EOF ;
    public final EObject entryRuleLAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAndExpression = null;


        try {
            // InternalLinkerScript.g:3236:55: (iv_ruleLAndExpression= ruleLAndExpression EOF )
            // InternalLinkerScript.g:3237:2: iv_ruleLAndExpression= ruleLAndExpression EOF
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
    // InternalLinkerScript.g:3243:1: ruleLAndExpression returns [EObject current=null] : (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* ) ;
    public final EObject ruleLAndExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LBitwiseOrExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3249:2: ( (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* ) )
            // InternalLinkerScript.g:3250:2: (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* )
            {
            // InternalLinkerScript.g:3250:2: (this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )* )
            // InternalLinkerScript.g:3251:3: this_LBitwiseOrExpression_0= ruleLBitwiseOrExpression ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAndExpressionAccess().getLBitwiseOrExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_46);
            this_LBitwiseOrExpression_0=ruleLBitwiseOrExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LBitwiseOrExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3259:3: ( ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) ) )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==71) && (synpred3_InternalLinkerScript())) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // InternalLinkerScript.g:3260:4: ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) )
            	    {
            	    // InternalLinkerScript.g:3260:4: ( ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) ) )
            	    // InternalLinkerScript.g:3261:5: ( ( () ( ( ruleOpAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAnd ) ) )
            	    {
            	    // InternalLinkerScript.g:3271:5: ( () ( (lv_feature_2_0= ruleOpAnd ) ) )
            	    // InternalLinkerScript.g:3272:6: () ( (lv_feature_2_0= ruleOpAnd ) )
            	    {
            	    // InternalLinkerScript.g:3272:6: ()
            	    // InternalLinkerScript.g:3273:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLAndExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3279:6: ( (lv_feature_2_0= ruleOpAnd ) )
            	    // InternalLinkerScript.g:3280:7: (lv_feature_2_0= ruleOpAnd )
            	    {
            	    // InternalLinkerScript.g:3280:7: (lv_feature_2_0= ruleOpAnd )
            	    // InternalLinkerScript.g:3281:8: lv_feature_2_0= ruleOpAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLAndExpressionAccess().getFeatureOpAndParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3300:4: ( (lv_rightOperand_3_0= ruleLBitwiseOrExpression ) )
            	    // InternalLinkerScript.g:3301:5: (lv_rightOperand_3_0= ruleLBitwiseOrExpression )
            	    {
            	    // InternalLinkerScript.g:3301:5: (lv_rightOperand_3_0= ruleLBitwiseOrExpression )
            	    // InternalLinkerScript.g:3302:6: lv_rightOperand_3_0= ruleLBitwiseOrExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLAndExpressionAccess().getRightOperandLBitwiseOrExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_46);
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
            	    break loop50;
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
    // InternalLinkerScript.g:3324:1: entryRuleOpAnd returns [String current=null] : iv_ruleOpAnd= ruleOpAnd EOF ;
    public final String entryRuleOpAnd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAnd = null;


        try {
            // InternalLinkerScript.g:3324:45: (iv_ruleOpAnd= ruleOpAnd EOF )
            // InternalLinkerScript.g:3325:2: iv_ruleOpAnd= ruleOpAnd EOF
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
    // InternalLinkerScript.g:3331:1: ruleOpAnd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '&&' ;
    public final AntlrDatatypeRuleToken ruleOpAnd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3337:2: (kw= '&&' )
            // InternalLinkerScript.g:3338:2: kw= '&&'
            {
            kw=(Token)match(input,71,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3346:1: entryRuleLBitwiseOrExpression returns [EObject current=null] : iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF ;
    public final EObject entryRuleLBitwiseOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLBitwiseOrExpression = null;


        try {
            // InternalLinkerScript.g:3346:61: (iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF )
            // InternalLinkerScript.g:3347:2: iv_ruleLBitwiseOrExpression= ruleLBitwiseOrExpression EOF
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
    // InternalLinkerScript.g:3353:1: ruleLBitwiseOrExpression returns [EObject current=null] : (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* ) ;
    public final EObject ruleLBitwiseOrExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LBitwiseAndExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3359:2: ( (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* ) )
            // InternalLinkerScript.g:3360:2: (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* )
            {
            // InternalLinkerScript.g:3360:2: (this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )* )
            // InternalLinkerScript.g:3361:3: this_LBitwiseAndExpression_0= ruleLBitwiseAndExpression ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getLBitwiseAndExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_47);
            this_LBitwiseAndExpression_0=ruleLBitwiseAndExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LBitwiseAndExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3369:3: ( ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) ) )*
            loop51:
            do {
                int alt51=2;
                int LA51_0 = input.LA(1);

                if ( (LA51_0==72) && (synpred4_InternalLinkerScript())) {
                    alt51=1;
                }


                switch (alt51) {
            	case 1 :
            	    // InternalLinkerScript.g:3370:4: ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) ) ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) )
            	    {
            	    // InternalLinkerScript.g:3370:4: ( ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) ) )
            	    // InternalLinkerScript.g:3371:5: ( ( () ( ( ruleOpBitwiseOr ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) )
            	    {
            	    // InternalLinkerScript.g:3381:5: ( () ( (lv_feature_2_0= ruleOpBitwiseOr ) ) )
            	    // InternalLinkerScript.g:3382:6: () ( (lv_feature_2_0= ruleOpBitwiseOr ) )
            	    {
            	    // InternalLinkerScript.g:3382:6: ()
            	    // InternalLinkerScript.g:3383:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLBitwiseOrExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3389:6: ( (lv_feature_2_0= ruleOpBitwiseOr ) )
            	    // InternalLinkerScript.g:3390:7: (lv_feature_2_0= ruleOpBitwiseOr )
            	    {
            	    // InternalLinkerScript.g:3390:7: (lv_feature_2_0= ruleOpBitwiseOr )
            	    // InternalLinkerScript.g:3391:8: lv_feature_2_0= ruleOpBitwiseOr
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getFeatureOpBitwiseOrParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3410:4: ( (lv_rightOperand_3_0= ruleLBitwiseAndExpression ) )
            	    // InternalLinkerScript.g:3411:5: (lv_rightOperand_3_0= ruleLBitwiseAndExpression )
            	    {
            	    // InternalLinkerScript.g:3411:5: (lv_rightOperand_3_0= ruleLBitwiseAndExpression )
            	    // InternalLinkerScript.g:3412:6: lv_rightOperand_3_0= ruleLBitwiseAndExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLBitwiseOrExpressionAccess().getRightOperandLBitwiseAndExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_47);
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
            	    break loop51;
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
    // InternalLinkerScript.g:3434:1: entryRuleOpBitwiseOr returns [String current=null] : iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF ;
    public final String entryRuleOpBitwiseOr() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpBitwiseOr = null;


        try {
            // InternalLinkerScript.g:3434:51: (iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF )
            // InternalLinkerScript.g:3435:2: iv_ruleOpBitwiseOr= ruleOpBitwiseOr EOF
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
    // InternalLinkerScript.g:3441:1: ruleOpBitwiseOr returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '|' ;
    public final AntlrDatatypeRuleToken ruleOpBitwiseOr() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3447:2: (kw= '|' )
            // InternalLinkerScript.g:3448:2: kw= '|'
            {
            kw=(Token)match(input,72,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3456:1: entryRuleLBitwiseAndExpression returns [EObject current=null] : iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF ;
    public final EObject entryRuleLBitwiseAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLBitwiseAndExpression = null;


        try {
            // InternalLinkerScript.g:3456:62: (iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF )
            // InternalLinkerScript.g:3457:2: iv_ruleLBitwiseAndExpression= ruleLBitwiseAndExpression EOF
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
    // InternalLinkerScript.g:3463:1: ruleLBitwiseAndExpression returns [EObject current=null] : (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* ) ;
    public final EObject ruleLBitwiseAndExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LEqualityExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3469:2: ( (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* ) )
            // InternalLinkerScript.g:3470:2: (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* )
            {
            // InternalLinkerScript.g:3470:2: (this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )* )
            // InternalLinkerScript.g:3471:3: this_LEqualityExpression_0= ruleLEqualityExpression ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getLEqualityExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_48);
            this_LEqualityExpression_0=ruleLEqualityExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LEqualityExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3479:3: ( ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) ) )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==55) && (synpred5_InternalLinkerScript())) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // InternalLinkerScript.g:3480:4: ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) ) ( (lv_rightOperand_3_0= ruleLEqualityExpression ) )
            	    {
            	    // InternalLinkerScript.g:3480:4: ( ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) ) )
            	    // InternalLinkerScript.g:3481:5: ( ( () ( ( ruleOpBitwiseAnd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) )
            	    {
            	    // InternalLinkerScript.g:3491:5: ( () ( (lv_feature_2_0= ruleOpBitwiseAnd ) ) )
            	    // InternalLinkerScript.g:3492:6: () ( (lv_feature_2_0= ruleOpBitwiseAnd ) )
            	    {
            	    // InternalLinkerScript.g:3492:6: ()
            	    // InternalLinkerScript.g:3493:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLBitwiseAndExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3499:6: ( (lv_feature_2_0= ruleOpBitwiseAnd ) )
            	    // InternalLinkerScript.g:3500:7: (lv_feature_2_0= ruleOpBitwiseAnd )
            	    {
            	    // InternalLinkerScript.g:3500:7: (lv_feature_2_0= ruleOpBitwiseAnd )
            	    // InternalLinkerScript.g:3501:8: lv_feature_2_0= ruleOpBitwiseAnd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getFeatureOpBitwiseAndParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3520:4: ( (lv_rightOperand_3_0= ruleLEqualityExpression ) )
            	    // InternalLinkerScript.g:3521:5: (lv_rightOperand_3_0= ruleLEqualityExpression )
            	    {
            	    // InternalLinkerScript.g:3521:5: (lv_rightOperand_3_0= ruleLEqualityExpression )
            	    // InternalLinkerScript.g:3522:6: lv_rightOperand_3_0= ruleLEqualityExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLBitwiseAndExpressionAccess().getRightOperandLEqualityExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_48);
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
            	    break loop52;
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
    // InternalLinkerScript.g:3544:1: entryRuleOpBitwiseAnd returns [String current=null] : iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF ;
    public final String entryRuleOpBitwiseAnd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpBitwiseAnd = null;


        try {
            // InternalLinkerScript.g:3544:52: (iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF )
            // InternalLinkerScript.g:3545:2: iv_ruleOpBitwiseAnd= ruleOpBitwiseAnd EOF
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
    // InternalLinkerScript.g:3551:1: ruleOpBitwiseAnd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : kw= '&' ;
    public final AntlrDatatypeRuleToken ruleOpBitwiseAnd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3557:2: (kw= '&' )
            // InternalLinkerScript.g:3558:2: kw= '&'
            {
            kw=(Token)match(input,55,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3566:1: entryRuleLEqualityExpression returns [EObject current=null] : iv_ruleLEqualityExpression= ruleLEqualityExpression EOF ;
    public final EObject entryRuleLEqualityExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLEqualityExpression = null;


        try {
            // InternalLinkerScript.g:3566:60: (iv_ruleLEqualityExpression= ruleLEqualityExpression EOF )
            // InternalLinkerScript.g:3567:2: iv_ruleLEqualityExpression= ruleLEqualityExpression EOF
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
    // InternalLinkerScript.g:3573:1: ruleLEqualityExpression returns [EObject current=null] : (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* ) ;
    public final EObject ruleLEqualityExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LRelationalExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3579:2: ( (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* ) )
            // InternalLinkerScript.g:3580:2: (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* )
            {
            // InternalLinkerScript.g:3580:2: (this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )* )
            // InternalLinkerScript.g:3581:3: this_LRelationalExpression_0= ruleLRelationalExpression ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getLRelationalExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_49);
            this_LRelationalExpression_0=ruleLRelationalExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LRelationalExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3589:3: ( ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) ) )*
            loop53:
            do {
                int alt53=2;
                int LA53_0 = input.LA(1);

                if ( (LA53_0==73) && (synpred6_InternalLinkerScript())) {
                    alt53=1;
                }
                else if ( (LA53_0==74) && (synpred6_InternalLinkerScript())) {
                    alt53=1;
                }


                switch (alt53) {
            	case 1 :
            	    // InternalLinkerScript.g:3590:4: ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) ) ( (lv_rightOperand_3_0= ruleLRelationalExpression ) )
            	    {
            	    // InternalLinkerScript.g:3590:4: ( ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) ) )
            	    // InternalLinkerScript.g:3591:5: ( ( () ( ( ruleOpEquality ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpEquality ) ) )
            	    {
            	    // InternalLinkerScript.g:3601:5: ( () ( (lv_feature_2_0= ruleOpEquality ) ) )
            	    // InternalLinkerScript.g:3602:6: () ( (lv_feature_2_0= ruleOpEquality ) )
            	    {
            	    // InternalLinkerScript.g:3602:6: ()
            	    // InternalLinkerScript.g:3603:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLEqualityExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3609:6: ( (lv_feature_2_0= ruleOpEquality ) )
            	    // InternalLinkerScript.g:3610:7: (lv_feature_2_0= ruleOpEquality )
            	    {
            	    // InternalLinkerScript.g:3610:7: (lv_feature_2_0= ruleOpEquality )
            	    // InternalLinkerScript.g:3611:8: lv_feature_2_0= ruleOpEquality
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getFeatureOpEqualityParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3630:4: ( (lv_rightOperand_3_0= ruleLRelationalExpression ) )
            	    // InternalLinkerScript.g:3631:5: (lv_rightOperand_3_0= ruleLRelationalExpression )
            	    {
            	    // InternalLinkerScript.g:3631:5: (lv_rightOperand_3_0= ruleLRelationalExpression )
            	    // InternalLinkerScript.g:3632:6: lv_rightOperand_3_0= ruleLRelationalExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLEqualityExpressionAccess().getRightOperandLRelationalExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_49);
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
            	    break loop53;
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
    // InternalLinkerScript.g:3654:1: entryRuleOpEquality returns [String current=null] : iv_ruleOpEquality= ruleOpEquality EOF ;
    public final String entryRuleOpEquality() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpEquality = null;


        try {
            // InternalLinkerScript.g:3654:50: (iv_ruleOpEquality= ruleOpEquality EOF )
            // InternalLinkerScript.g:3655:2: iv_ruleOpEquality= ruleOpEquality EOF
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
    // InternalLinkerScript.g:3661:1: ruleOpEquality returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '==' | kw= '!=' ) ;
    public final AntlrDatatypeRuleToken ruleOpEquality() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3667:2: ( (kw= '==' | kw= '!=' ) )
            // InternalLinkerScript.g:3668:2: (kw= '==' | kw= '!=' )
            {
            // InternalLinkerScript.g:3668:2: (kw= '==' | kw= '!=' )
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==73) ) {
                alt54=1;
            }
            else if ( (LA54_0==74) ) {
                alt54=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }
            switch (alt54) {
                case 1 :
                    // InternalLinkerScript.g:3669:3: kw= '=='
                    {
                    kw=(Token)match(input,73,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpEqualityAccess().getEqualsSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3675:3: kw= '!='
                    {
                    kw=(Token)match(input,74,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3684:1: entryRuleLRelationalExpression returns [EObject current=null] : iv_ruleLRelationalExpression= ruleLRelationalExpression EOF ;
    public final EObject entryRuleLRelationalExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLRelationalExpression = null;


        try {
            // InternalLinkerScript.g:3684:62: (iv_ruleLRelationalExpression= ruleLRelationalExpression EOF )
            // InternalLinkerScript.g:3685:2: iv_ruleLRelationalExpression= ruleLRelationalExpression EOF
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
    // InternalLinkerScript.g:3691:1: ruleLRelationalExpression returns [EObject current=null] : (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* ) ;
    public final EObject ruleLRelationalExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LOtherOperatorExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3697:2: ( (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* ) )
            // InternalLinkerScript.g:3698:2: (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* )
            {
            // InternalLinkerScript.g:3698:2: (this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )* )
            // InternalLinkerScript.g:3699:3: this_LOtherOperatorExpression_0= ruleLOtherOperatorExpression ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getLOtherOperatorExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_50);
            this_LOtherOperatorExpression_0=ruleLOtherOperatorExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LOtherOperatorExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3707:3: ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*
            loop55:
            do {
                int alt55=2;
                alt55 = dfa55.predict(input);
                switch (alt55) {
            	case 1 :
            	    // InternalLinkerScript.g:3708:4: ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) )
            	    {
            	    // InternalLinkerScript.g:3708:4: ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) )
            	    // InternalLinkerScript.g:3709:5: ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) )
            	    {
            	    // InternalLinkerScript.g:3719:5: ( () ( (lv_feature_2_0= ruleOpCompare ) ) )
            	    // InternalLinkerScript.g:3720:6: () ( (lv_feature_2_0= ruleOpCompare ) )
            	    {
            	    // InternalLinkerScript.g:3720:6: ()
            	    // InternalLinkerScript.g:3721:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLRelationalExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3727:6: ( (lv_feature_2_0= ruleOpCompare ) )
            	    // InternalLinkerScript.g:3728:7: (lv_feature_2_0= ruleOpCompare )
            	    {
            	    // InternalLinkerScript.g:3728:7: (lv_feature_2_0= ruleOpCompare )
            	    // InternalLinkerScript.g:3729:8: lv_feature_2_0= ruleOpCompare
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getFeatureOpCompareParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3748:4: ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) )
            	    // InternalLinkerScript.g:3749:5: (lv_rightOperand_3_0= ruleLOtherOperatorExpression )
            	    {
            	    // InternalLinkerScript.g:3749:5: (lv_rightOperand_3_0= ruleLOtherOperatorExpression )
            	    // InternalLinkerScript.g:3750:6: lv_rightOperand_3_0= ruleLOtherOperatorExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLRelationalExpressionAccess().getRightOperandLOtherOperatorExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_50);
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
            	    break loop55;
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
    // InternalLinkerScript.g:3772:1: entryRuleOpCompare returns [String current=null] : iv_ruleOpCompare= ruleOpCompare EOF ;
    public final String entryRuleOpCompare() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpCompare = null;


        try {
            // InternalLinkerScript.g:3772:49: (iv_ruleOpCompare= ruleOpCompare EOF )
            // InternalLinkerScript.g:3773:2: iv_ruleOpCompare= ruleOpCompare EOF
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
    // InternalLinkerScript.g:3779:1: ruleOpCompare returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' ) ;
    public final AntlrDatatypeRuleToken ruleOpCompare() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3785:2: ( (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' ) )
            // InternalLinkerScript.g:3786:2: (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' )
            {
            // InternalLinkerScript.g:3786:2: (kw= '>=' | (kw= '<' kw= '=' ) | kw= '>' | kw= '<' )
            int alt56=4;
            switch ( input.LA(1) ) {
            case 51:
                {
                alt56=1;
                }
                break;
            case 50:
                {
                int LA56_2 = input.LA(2);

                if ( (LA56_2==EOF||(LA56_2>=RULE_ID && LA56_2<=RULE_HEX)||LA56_2==13||LA56_2==21||LA56_2==62||(LA56_2>=64 && LA56_2<=69)||(LA56_2>=75 && LA56_2<=76)||LA56_2==80) ) {
                    alt56=4;
                }
                else if ( (LA56_2==19) ) {
                    alt56=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return current;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 56, 2, input);

                    throw nvae;
                }
                }
                break;
            case 18:
                {
                alt56=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;
            }

            switch (alt56) {
                case 1 :
                    // InternalLinkerScript.g:3787:3: kw= '>='
                    {
                    kw=(Token)match(input,51,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getGreaterThanSignEqualsSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3793:3: (kw= '<' kw= '=' )
                    {
                    // InternalLinkerScript.g:3793:3: (kw= '<' kw= '=' )
                    // InternalLinkerScript.g:3794:4: kw= '<' kw= '='
                    {
                    kw=(Token)match(input,50,FOLLOW_29); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpCompareAccess().getLessThanSignKeyword_1_0());
                      			
                    }
                    kw=(Token)match(input,19,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpCompareAccess().getEqualsSignKeyword_1_1());
                      			
                    }

                    }


                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:3806:3: kw= '>'
                    {
                    kw=(Token)match(input,18,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpCompareAccess().getGreaterThanSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:3812:3: kw= '<'
                    {
                    kw=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3821:1: entryRuleLOtherOperatorExpression returns [EObject current=null] : iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF ;
    public final EObject entryRuleLOtherOperatorExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLOtherOperatorExpression = null;


        try {
            // InternalLinkerScript.g:3821:65: (iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF )
            // InternalLinkerScript.g:3822:2: iv_ruleLOtherOperatorExpression= ruleLOtherOperatorExpression EOF
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
    // InternalLinkerScript.g:3828:1: ruleLOtherOperatorExpression returns [EObject current=null] : (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* ) ;
    public final EObject ruleLOtherOperatorExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LAdditiveExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3834:2: ( (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* ) )
            // InternalLinkerScript.g:3835:2: (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* )
            {
            // InternalLinkerScript.g:3835:2: (this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )* )
            // InternalLinkerScript.g:3836:3: this_LAdditiveExpression_0= ruleLAdditiveExpression ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getLAdditiveExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_51);
            this_LAdditiveExpression_0=ruleLAdditiveExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LAdditiveExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3844:3: ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*
            loop57:
            do {
                int alt57=2;
                alt57 = dfa57.predict(input);
                switch (alt57) {
            	case 1 :
            	    // InternalLinkerScript.g:3845:4: ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) )
            	    {
            	    // InternalLinkerScript.g:3845:4: ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) )
            	    // InternalLinkerScript.g:3846:5: ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) )
            	    {
            	    // InternalLinkerScript.g:3856:5: ( () ( (lv_feature_2_0= ruleOpOther ) ) )
            	    // InternalLinkerScript.g:3857:6: () ( (lv_feature_2_0= ruleOpOther ) )
            	    {
            	    // InternalLinkerScript.g:3857:6: ()
            	    // InternalLinkerScript.g:3858:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLOtherOperatorExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:3864:6: ( (lv_feature_2_0= ruleOpOther ) )
            	    // InternalLinkerScript.g:3865:7: (lv_feature_2_0= ruleOpOther )
            	    {
            	    // InternalLinkerScript.g:3865:7: (lv_feature_2_0= ruleOpOther )
            	    // InternalLinkerScript.g:3866:8: lv_feature_2_0= ruleOpOther
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getFeatureOpOtherParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:3885:4: ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) )
            	    // InternalLinkerScript.g:3886:5: (lv_rightOperand_3_0= ruleLAdditiveExpression )
            	    {
            	    // InternalLinkerScript.g:3886:5: (lv_rightOperand_3_0= ruleLAdditiveExpression )
            	    // InternalLinkerScript.g:3887:6: lv_rightOperand_3_0= ruleLAdditiveExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLOtherOperatorExpressionAccess().getRightOperandLAdditiveExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_51);
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
            	    break loop57;
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
    // InternalLinkerScript.g:3909:1: entryRuleOpOther returns [String current=null] : iv_ruleOpOther= ruleOpOther EOF ;
    public final String entryRuleOpOther() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpOther = null;


        try {
            // InternalLinkerScript.g:3909:47: (iv_ruleOpOther= ruleOpOther EOF )
            // InternalLinkerScript.g:3910:2: iv_ruleOpOther= ruleOpOther EOF
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
    // InternalLinkerScript.g:3916:1: ruleOpOther returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) ) ;
    public final AntlrDatatypeRuleToken ruleOpOther() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:3922:2: ( ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) ) )
            // InternalLinkerScript.g:3923:2: ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) )
            {
            // InternalLinkerScript.g:3923:2: ( (kw= '>' ( ( '>' )=>kw= '>' ) ) | (kw= '<' ( ( '<' )=>kw= '<' ) ) )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==18) ) {
                alt58=1;
            }
            else if ( (LA58_0==50) ) {
                alt58=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1 :
                    // InternalLinkerScript.g:3924:3: (kw= '>' ( ( '>' )=>kw= '>' ) )
                    {
                    // InternalLinkerScript.g:3924:3: (kw= '>' ( ( '>' )=>kw= '>' ) )
                    // InternalLinkerScript.g:3925:4: kw= '>' ( ( '>' )=>kw= '>' )
                    {
                    kw=(Token)match(input,18,FOLLOW_21); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpOtherAccess().getGreaterThanSignKeyword_0_0());
                      			
                    }
                    // InternalLinkerScript.g:3930:4: ( ( '>' )=>kw= '>' )
                    // InternalLinkerScript.g:3931:5: ( '>' )=>kw= '>'
                    {
                    kw=(Token)match(input,18,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      					current.merge(kw);
                      					newLeafNode(kw, grammarAccess.getOpOtherAccess().getGreaterThanSignKeyword_0_1());
                      				
                    }

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:3940:3: (kw= '<' ( ( '<' )=>kw= '<' ) )
                    {
                    // InternalLinkerScript.g:3940:3: (kw= '<' ( ( '<' )=>kw= '<' ) )
                    // InternalLinkerScript.g:3941:4: kw= '<' ( ( '<' )=>kw= '<' )
                    {
                    kw=(Token)match(input,50,FOLLOW_30); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				current.merge(kw);
                      				newLeafNode(kw, grammarAccess.getOpOtherAccess().getLessThanSignKeyword_1_0());
                      			
                    }
                    // InternalLinkerScript.g:3946:4: ( ( '<' )=>kw= '<' )
                    // InternalLinkerScript.g:3947:5: ( '<' )=>kw= '<'
                    {
                    kw=(Token)match(input,50,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:3959:1: entryRuleLAdditiveExpression returns [EObject current=null] : iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF ;
    public final EObject entryRuleLAdditiveExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLAdditiveExpression = null;


        try {
            // InternalLinkerScript.g:3959:60: (iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF )
            // InternalLinkerScript.g:3960:2: iv_ruleLAdditiveExpression= ruleLAdditiveExpression EOF
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
    // InternalLinkerScript.g:3966:1: ruleLAdditiveExpression returns [EObject current=null] : (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* ) ;
    public final EObject ruleLAdditiveExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LMultiplicativeExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:3972:2: ( (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* ) )
            // InternalLinkerScript.g:3973:2: (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* )
            {
            // InternalLinkerScript.g:3973:2: (this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )* )
            // InternalLinkerScript.g:3974:3: this_LMultiplicativeExpression_0= ruleLMultiplicativeExpression ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getLMultiplicativeExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_52);
            this_LMultiplicativeExpression_0=ruleLMultiplicativeExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LMultiplicativeExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:3982:3: ( ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) ) )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==75) && (synpred11_InternalLinkerScript())) {
                    alt59=1;
                }
                else if ( (LA59_0==76) && (synpred11_InternalLinkerScript())) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // InternalLinkerScript.g:3983:4: ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) ) ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) )
            	    {
            	    // InternalLinkerScript.g:3983:4: ( ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) ) )
            	    // InternalLinkerScript.g:3984:5: ( ( () ( ( ruleOpAdd ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpAdd ) ) )
            	    {
            	    // InternalLinkerScript.g:3994:5: ( () ( (lv_feature_2_0= ruleOpAdd ) ) )
            	    // InternalLinkerScript.g:3995:6: () ( (lv_feature_2_0= ruleOpAdd ) )
            	    {
            	    // InternalLinkerScript.g:3995:6: ()
            	    // InternalLinkerScript.g:3996:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLAdditiveExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4002:6: ( (lv_feature_2_0= ruleOpAdd ) )
            	    // InternalLinkerScript.g:4003:7: (lv_feature_2_0= ruleOpAdd )
            	    {
            	    // InternalLinkerScript.g:4003:7: (lv_feature_2_0= ruleOpAdd )
            	    // InternalLinkerScript.g:4004:8: lv_feature_2_0= ruleOpAdd
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getFeatureOpAddParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:4023:4: ( (lv_rightOperand_3_0= ruleLMultiplicativeExpression ) )
            	    // InternalLinkerScript.g:4024:5: (lv_rightOperand_3_0= ruleLMultiplicativeExpression )
            	    {
            	    // InternalLinkerScript.g:4024:5: (lv_rightOperand_3_0= ruleLMultiplicativeExpression )
            	    // InternalLinkerScript.g:4025:6: lv_rightOperand_3_0= ruleLMultiplicativeExpression
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLAdditiveExpressionAccess().getRightOperandLMultiplicativeExpressionParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_52);
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
            	    break loop59;
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
    // InternalLinkerScript.g:4047:1: entryRuleOpAdd returns [String current=null] : iv_ruleOpAdd= ruleOpAdd EOF ;
    public final String entryRuleOpAdd() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpAdd = null;


        try {
            // InternalLinkerScript.g:4047:45: (iv_ruleOpAdd= ruleOpAdd EOF )
            // InternalLinkerScript.g:4048:2: iv_ruleOpAdd= ruleOpAdd EOF
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
    // InternalLinkerScript.g:4054:1: ruleOpAdd returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '+' | kw= '-' ) ;
    public final AntlrDatatypeRuleToken ruleOpAdd() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4060:2: ( (kw= '+' | kw= '-' ) )
            // InternalLinkerScript.g:4061:2: (kw= '+' | kw= '-' )
            {
            // InternalLinkerScript.g:4061:2: (kw= '+' | kw= '-' )
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==75) ) {
                alt60=1;
            }
            else if ( (LA60_0==76) ) {
                alt60=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;
            }
            switch (alt60) {
                case 1 :
                    // InternalLinkerScript.g:4062:3: kw= '+'
                    {
                    kw=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpAddAccess().getPlusSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4068:3: kw= '-'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:4077:1: entryRuleLMultiplicativeExpression returns [EObject current=null] : iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF ;
    public final EObject entryRuleLMultiplicativeExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLMultiplicativeExpression = null;


        try {
            // InternalLinkerScript.g:4077:66: (iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF )
            // InternalLinkerScript.g:4078:2: iv_ruleLMultiplicativeExpression= ruleLMultiplicativeExpression EOF
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
    // InternalLinkerScript.g:4084:1: ruleLMultiplicativeExpression returns [EObject current=null] : (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* ) ;
    public final EObject ruleLMultiplicativeExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LUnaryOperation_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;

        EObject lv_rightOperand_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4090:2: ( (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* ) )
            // InternalLinkerScript.g:4091:2: (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* )
            {
            // InternalLinkerScript.g:4091:2: (this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )* )
            // InternalLinkerScript.g:4092:3: this_LUnaryOperation_0= ruleLUnaryOperation ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getLUnaryOperationParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_53);
            this_LUnaryOperation_0=ruleLUnaryOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LUnaryOperation_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4100:3: ( ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) ) )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==77) && (synpred12_InternalLinkerScript())) {
                    alt61=1;
                }
                else if ( (LA61_0==78) && (synpred12_InternalLinkerScript())) {
                    alt61=1;
                }
                else if ( (LA61_0==79) && (synpred12_InternalLinkerScript())) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
            	    // InternalLinkerScript.g:4101:4: ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) ) ( (lv_rightOperand_3_0= ruleLUnaryOperation ) )
            	    {
            	    // InternalLinkerScript.g:4101:4: ( ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) ) )
            	    // InternalLinkerScript.g:4102:5: ( ( () ( ( ruleOpMulti ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMulti ) ) )
            	    {
            	    // InternalLinkerScript.g:4112:5: ( () ( (lv_feature_2_0= ruleOpMulti ) ) )
            	    // InternalLinkerScript.g:4113:6: () ( (lv_feature_2_0= ruleOpMulti ) )
            	    {
            	    // InternalLinkerScript.g:4113:6: ()
            	    // InternalLinkerScript.g:4114:7: 
            	    {
            	    if ( state.backtracking==0 ) {

            	      							current = forceCreateModelElementAndSet(
            	      								grammarAccess.getLMultiplicativeExpressionAccess().getLBinaryOperationLeftOperandAction_1_0_0_0(),
            	      								current);
            	      						
            	    }

            	    }

            	    // InternalLinkerScript.g:4120:6: ( (lv_feature_2_0= ruleOpMulti ) )
            	    // InternalLinkerScript.g:4121:7: (lv_feature_2_0= ruleOpMulti )
            	    {
            	    // InternalLinkerScript.g:4121:7: (lv_feature_2_0= ruleOpMulti )
            	    // InternalLinkerScript.g:4122:8: lv_feature_2_0= ruleOpMulti
            	    {
            	    if ( state.backtracking==0 ) {

            	      								newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getFeatureOpMultiParserRuleCall_1_0_0_1_0());
            	      							
            	    }
            	    pushFollow(FOLLOW_13);
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

            	    // InternalLinkerScript.g:4141:4: ( (lv_rightOperand_3_0= ruleLUnaryOperation ) )
            	    // InternalLinkerScript.g:4142:5: (lv_rightOperand_3_0= ruleLUnaryOperation )
            	    {
            	    // InternalLinkerScript.g:4142:5: (lv_rightOperand_3_0= ruleLUnaryOperation )
            	    // InternalLinkerScript.g:4143:6: lv_rightOperand_3_0= ruleLUnaryOperation
            	    {
            	    if ( state.backtracking==0 ) {

            	      						newCompositeNode(grammarAccess.getLMultiplicativeExpressionAccess().getRightOperandLUnaryOperationParserRuleCall_1_1_0());
            	      					
            	    }
            	    pushFollow(FOLLOW_53);
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
            	    break loop61;
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
    // InternalLinkerScript.g:4165:1: entryRuleOpMulti returns [String current=null] : iv_ruleOpMulti= ruleOpMulti EOF ;
    public final String entryRuleOpMulti() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpMulti = null;


        try {
            // InternalLinkerScript.g:4165:47: (iv_ruleOpMulti= ruleOpMulti EOF )
            // InternalLinkerScript.g:4166:2: iv_ruleOpMulti= ruleOpMulti EOF
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
    // InternalLinkerScript.g:4172:1: ruleOpMulti returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '*' | kw= '/' | kw= '%' ) ;
    public final AntlrDatatypeRuleToken ruleOpMulti() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4178:2: ( (kw= '*' | kw= '/' | kw= '%' ) )
            // InternalLinkerScript.g:4179:2: (kw= '*' | kw= '/' | kw= '%' )
            {
            // InternalLinkerScript.g:4179:2: (kw= '*' | kw= '/' | kw= '%' )
            int alt62=3;
            switch ( input.LA(1) ) {
            case 77:
                {
                alt62=1;
                }
                break;
            case 78:
                {
                alt62=2;
                }
                break;
            case 79:
                {
                alt62=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;
            }

            switch (alt62) {
                case 1 :
                    // InternalLinkerScript.g:4180:3: kw= '*'
                    {
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getAsteriskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4186:3: kw= '/'
                    {
                    kw=(Token)match(input,78,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpMultiAccess().getSolidusKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4192:3: kw= '%'
                    {
                    kw=(Token)match(input,79,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:4201:1: entryRuleLUnaryOperation returns [EObject current=null] : iv_ruleLUnaryOperation= ruleLUnaryOperation EOF ;
    public final EObject entryRuleLUnaryOperation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLUnaryOperation = null;


        try {
            // InternalLinkerScript.g:4201:56: (iv_ruleLUnaryOperation= ruleLUnaryOperation EOF )
            // InternalLinkerScript.g:4202:2: iv_ruleLUnaryOperation= ruleLUnaryOperation EOF
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
    // InternalLinkerScript.g:4208:1: ruleLUnaryOperation returns [EObject current=null] : ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation ) ;
    public final EObject ruleLUnaryOperation() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_feature_1_0 = null;

        EObject lv_operand_2_0 = null;

        EObject this_LPostfixOperation_3 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4214:2: ( ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation ) )
            // InternalLinkerScript.g:4215:2: ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation )
            {
            // InternalLinkerScript.g:4215:2: ( ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) ) | this_LPostfixOperation_3= ruleLPostfixOperation )
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==69||(LA63_0>=75 && LA63_0<=76)||LA63_0==80) ) {
                alt63=1;
            }
            else if ( ((LA63_0>=RULE_ID && LA63_0<=RULE_HEX)||LA63_0==13||LA63_0==21||LA63_0==62||(LA63_0>=64 && LA63_0<=68)) ) {
                alt63=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // InternalLinkerScript.g:4216:3: ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) )
                    {
                    // InternalLinkerScript.g:4216:3: ( () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) ) )
                    // InternalLinkerScript.g:4217:4: () ( (lv_feature_1_0= ruleOpUnary ) ) ( (lv_operand_2_0= ruleLUnaryOperation ) )
                    {
                    // InternalLinkerScript.g:4217:4: ()
                    // InternalLinkerScript.g:4218:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					current = forceCreateModelElement(
                      						grammarAccess.getLUnaryOperationAccess().getLUnaryOperationAction_0_0(),
                      						current);
                      				
                    }

                    }

                    // InternalLinkerScript.g:4224:4: ( (lv_feature_1_0= ruleOpUnary ) )
                    // InternalLinkerScript.g:4225:5: (lv_feature_1_0= ruleOpUnary )
                    {
                    // InternalLinkerScript.g:4225:5: (lv_feature_1_0= ruleOpUnary )
                    // InternalLinkerScript.g:4226:6: lv_feature_1_0= ruleOpUnary
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getLUnaryOperationAccess().getFeatureOpUnaryParserRuleCall_0_1_0());
                      					
                    }
                    pushFollow(FOLLOW_13);
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

                    // InternalLinkerScript.g:4243:4: ( (lv_operand_2_0= ruleLUnaryOperation ) )
                    // InternalLinkerScript.g:4244:5: (lv_operand_2_0= ruleLUnaryOperation )
                    {
                    // InternalLinkerScript.g:4244:5: (lv_operand_2_0= ruleLUnaryOperation )
                    // InternalLinkerScript.g:4245:6: lv_operand_2_0= ruleLUnaryOperation
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
                    // InternalLinkerScript.g:4264:3: this_LPostfixOperation_3= ruleLPostfixOperation
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLUnaryOperationAccess().getLPostfixOperationParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LPostfixOperation_3=ruleLPostfixOperation();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LPostfixOperation_3;
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
    // InternalLinkerScript.g:4276:1: entryRuleOpUnary returns [String current=null] : iv_ruleOpUnary= ruleOpUnary EOF ;
    public final String entryRuleOpUnary() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpUnary = null;


        try {
            // InternalLinkerScript.g:4276:47: (iv_ruleOpUnary= ruleOpUnary EOF )
            // InternalLinkerScript.g:4277:2: iv_ruleOpUnary= ruleOpUnary EOF
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
    // InternalLinkerScript.g:4283:1: ruleOpUnary returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '!' | kw= '-' | kw= '+' | kw= '~' ) ;
    public final AntlrDatatypeRuleToken ruleOpUnary() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4289:2: ( (kw= '!' | kw= '-' | kw= '+' | kw= '~' ) )
            // InternalLinkerScript.g:4290:2: (kw= '!' | kw= '-' | kw= '+' | kw= '~' )
            {
            // InternalLinkerScript.g:4290:2: (kw= '!' | kw= '-' | kw= '+' | kw= '~' )
            int alt64=4;
            switch ( input.LA(1) ) {
            case 69:
                {
                alt64=1;
                }
                break;
            case 76:
                {
                alt64=2;
                }
                break;
            case 75:
                {
                alt64=3;
                }
                break;
            case 80:
                {
                alt64=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 64, 0, input);

                throw nvae;
            }

            switch (alt64) {
                case 1 :
                    // InternalLinkerScript.g:4291:3: kw= '!'
                    {
                    kw=(Token)match(input,69,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getExclamationMarkKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4297:3: kw= '-'
                    {
                    kw=(Token)match(input,76,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getHyphenMinusKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4303:3: kw= '+'
                    {
                    kw=(Token)match(input,75,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpUnaryAccess().getPlusSignKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:4309:3: kw= '~'
                    {
                    kw=(Token)match(input,80,FOLLOW_2); if (state.failed) return current;
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


    // $ANTLR start "entryRuleLPostfixOperation"
    // InternalLinkerScript.g:4318:1: entryRuleLPostfixOperation returns [EObject current=null] : iv_ruleLPostfixOperation= ruleLPostfixOperation EOF ;
    public final EObject entryRuleLPostfixOperation() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLPostfixOperation = null;


        try {
            // InternalLinkerScript.g:4318:58: (iv_ruleLPostfixOperation= ruleLPostfixOperation EOF )
            // InternalLinkerScript.g:4319:2: iv_ruleLPostfixOperation= ruleLPostfixOperation EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getLPostfixOperationRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleLPostfixOperation=ruleLPostfixOperation();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleLPostfixOperation; 
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
    // $ANTLR end "entryRuleLPostfixOperation"


    // $ANTLR start "ruleLPostfixOperation"
    // InternalLinkerScript.g:4325:1: ruleLPostfixOperation returns [EObject current=null] : (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? ) ;
    public final EObject ruleLPostfixOperation() throws RecognitionException {
        EObject current = null;

        EObject this_LPrimaryExpression_0 = null;

        AntlrDatatypeRuleToken lv_feature_2_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4331:2: ( (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? ) )
            // InternalLinkerScript.g:4332:2: (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? )
            {
            // InternalLinkerScript.g:4332:2: (this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )? )
            // InternalLinkerScript.g:4333:3: this_LPrimaryExpression_0= ruleLPrimaryExpression ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )?
            {
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLPostfixOperationAccess().getLPrimaryExpressionParserRuleCall_0());
              		
            }
            pushFollow(FOLLOW_54);
            this_LPrimaryExpression_0=ruleLPrimaryExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LPrimaryExpression_0;
              			afterParserOrEnumRuleCall();
              		
            }
            // InternalLinkerScript.g:4341:3: ( ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) ) )?
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==81) && (synpred13_InternalLinkerScript())) {
                alt65=1;
            }
            else if ( (LA65_0==82) && (synpred13_InternalLinkerScript())) {
                alt65=1;
            }
            switch (alt65) {
                case 1 :
                    // InternalLinkerScript.g:4342:4: ( ( () ( ( ruleOpPostfix ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpPostfix ) ) )
                    {
                    // InternalLinkerScript.g:4352:4: ( () ( (lv_feature_2_0= ruleOpPostfix ) ) )
                    // InternalLinkerScript.g:4353:5: () ( (lv_feature_2_0= ruleOpPostfix ) )
                    {
                    // InternalLinkerScript.g:4353:5: ()
                    // InternalLinkerScript.g:4354:6: 
                    {
                    if ( state.backtracking==0 ) {

                      						current = forceCreateModelElementAndSet(
                      							grammarAccess.getLPostfixOperationAccess().getLPostfixOperationOperandAction_1_0_0(),
                      							current);
                      					
                    }

                    }

                    // InternalLinkerScript.g:4360:5: ( (lv_feature_2_0= ruleOpPostfix ) )
                    // InternalLinkerScript.g:4361:6: (lv_feature_2_0= ruleOpPostfix )
                    {
                    // InternalLinkerScript.g:4361:6: (lv_feature_2_0= ruleOpPostfix )
                    // InternalLinkerScript.g:4362:7: lv_feature_2_0= ruleOpPostfix
                    {
                    if ( state.backtracking==0 ) {

                      							newCompositeNode(grammarAccess.getLPostfixOperationAccess().getFeatureOpPostfixParserRuleCall_1_0_1_0());
                      						
                    }
                    pushFollow(FOLLOW_2);
                    lv_feature_2_0=ruleOpPostfix();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      							if (current==null) {
                      								current = createModelElementForParent(grammarAccess.getLPostfixOperationRule());
                      							}
                      							set(
                      								current,
                      								"feature",
                      								lv_feature_2_0,
                      								"org.eclipse.cdt.linkerscript.LinkerScript.OpPostfix");
                      							afterParserOrEnumRuleCall();
                      						
                    }

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
    // $ANTLR end "ruleLPostfixOperation"


    // $ANTLR start "entryRuleOpPostfix"
    // InternalLinkerScript.g:4385:1: entryRuleOpPostfix returns [String current=null] : iv_ruleOpPostfix= ruleOpPostfix EOF ;
    public final String entryRuleOpPostfix() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOpPostfix = null;


        try {
            // InternalLinkerScript.g:4385:49: (iv_ruleOpPostfix= ruleOpPostfix EOF )
            // InternalLinkerScript.g:4386:2: iv_ruleOpPostfix= ruleOpPostfix EOF
            {
            if ( state.backtracking==0 ) {
               newCompositeNode(grammarAccess.getOpPostfixRule()); 
            }
            pushFollow(FOLLOW_1);
            iv_ruleOpPostfix=ruleOpPostfix();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {
               current =iv_ruleOpPostfix.getText(); 
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
    // $ANTLR end "entryRuleOpPostfix"


    // $ANTLR start "ruleOpPostfix"
    // InternalLinkerScript.g:4392:1: ruleOpPostfix returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '++' | kw= '--' ) ;
    public final AntlrDatatypeRuleToken ruleOpPostfix() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4398:2: ( (kw= '++' | kw= '--' ) )
            // InternalLinkerScript.g:4399:2: (kw= '++' | kw= '--' )
            {
            // InternalLinkerScript.g:4399:2: (kw= '++' | kw= '--' )
            int alt66=2;
            int LA66_0 = input.LA(1);

            if ( (LA66_0==81) ) {
                alt66=1;
            }
            else if ( (LA66_0==82) ) {
                alt66=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 66, 0, input);

                throw nvae;
            }
            switch (alt66) {
                case 1 :
                    // InternalLinkerScript.g:4400:3: kw= '++'
                    {
                    kw=(Token)match(input,81,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpPostfixAccess().getPlusSignPlusSignKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4406:3: kw= '--'
                    {
                    kw=(Token)match(input,82,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getOpPostfixAccess().getHyphenMinusHyphenMinusKeyword_1());
                      		
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
    // $ANTLR end "ruleOpPostfix"


    // $ANTLR start "entryRuleLPrimaryExpression"
    // InternalLinkerScript.g:4415:1: entryRuleLPrimaryExpression returns [EObject current=null] : iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF ;
    public final EObject entryRuleLPrimaryExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLPrimaryExpression = null;


        try {
            // InternalLinkerScript.g:4415:59: (iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF )
            // InternalLinkerScript.g:4416:2: iv_ruleLPrimaryExpression= ruleLPrimaryExpression EOF
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
    // InternalLinkerScript.g:4422:1: ruleLPrimaryExpression returns [EObject current=null] : (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_LNumberLiteral_2= ruleLNumberLiteral | this_LParenthesizedExpression_3= ruleLParenthesizedExpression | this_LVariable_4= ruleLVariable ) ;
    public final EObject ruleLPrimaryExpression() throws RecognitionException {
        EObject current = null;

        EObject this_LengthCall_0 = null;

        EObject this_AlignCall_1 = null;

        EObject this_LNumberLiteral_2 = null;

        EObject this_LParenthesizedExpression_3 = null;

        EObject this_LVariable_4 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4428:2: ( (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_LNumberLiteral_2= ruleLNumberLiteral | this_LParenthesizedExpression_3= ruleLParenthesizedExpression | this_LVariable_4= ruleLVariable ) )
            // InternalLinkerScript.g:4429:2: (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_LNumberLiteral_2= ruleLNumberLiteral | this_LParenthesizedExpression_3= ruleLParenthesizedExpression | this_LVariable_4= ruleLVariable )
            {
            // InternalLinkerScript.g:4429:2: (this_LengthCall_0= ruleLengthCall | this_AlignCall_1= ruleAlignCall | this_LNumberLiteral_2= ruleLNumberLiteral | this_LParenthesizedExpression_3= ruleLParenthesizedExpression | this_LVariable_4= ruleLVariable )
            int alt67=5;
            switch ( input.LA(1) ) {
            case 66:
                {
                alt67=1;
                }
                break;
            case 21:
                {
                alt67=2;
                }
                break;
            case RULE_DEC:
            case RULE_HEX:
                {
                alt67=3;
                }
                break;
            case 13:
                {
                alt67=4;
                }
                break;
            case RULE_ID:
            case 62:
            case 64:
            case 65:
            case 67:
            case 68:
                {
                alt67=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 67, 0, input);

                throw nvae;
            }

            switch (alt67) {
                case 1 :
                    // InternalLinkerScript.g:4430:3: this_LengthCall_0= ruleLengthCall
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
                    // InternalLinkerScript.g:4439:3: this_AlignCall_1= ruleAlignCall
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getAlignCallParserRuleCall_1());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_AlignCall_1=ruleAlignCall();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_AlignCall_1;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4448:3: this_LNumberLiteral_2= ruleLNumberLiteral
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLNumberLiteralParserRuleCall_2());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LNumberLiteral_2=ruleLNumberLiteral();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LNumberLiteral_2;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:4457:3: this_LParenthesizedExpression_3= ruleLParenthesizedExpression
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLParenthesizedExpressionParserRuleCall_3());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LParenthesizedExpression_3=ruleLParenthesizedExpression();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LParenthesizedExpression_3;
                      			afterParserOrEnumRuleCall();
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:4466:3: this_LVariable_4= ruleLVariable
                    {
                    if ( state.backtracking==0 ) {

                      			newCompositeNode(grammarAccess.getLPrimaryExpressionAccess().getLVariableParserRuleCall_4());
                      		
                    }
                    pushFollow(FOLLOW_2);
                    this_LVariable_4=ruleLVariable();

                    state._fsp--;
                    if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current = this_LVariable_4;
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
    // InternalLinkerScript.g:4478:1: entryRuleLVariable returns [EObject current=null] : iv_ruleLVariable= ruleLVariable EOF ;
    public final EObject entryRuleLVariable() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLVariable = null;


        try {
            // InternalLinkerScript.g:4478:50: (iv_ruleLVariable= ruleLVariable EOF )
            // InternalLinkerScript.g:4479:2: iv_ruleLVariable= ruleLVariable EOF
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
    // InternalLinkerScript.g:4485:1: ruleLVariable returns [EObject current=null] : ( () ( (lv_feature_1_0= ruleValidID ) ) ) ;
    public final EObject ruleLVariable() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_feature_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4491:2: ( ( () ( (lv_feature_1_0= ruleValidID ) ) ) )
            // InternalLinkerScript.g:4492:2: ( () ( (lv_feature_1_0= ruleValidID ) ) )
            {
            // InternalLinkerScript.g:4492:2: ( () ( (lv_feature_1_0= ruleValidID ) ) )
            // InternalLinkerScript.g:4493:3: () ( (lv_feature_1_0= ruleValidID ) )
            {
            // InternalLinkerScript.g:4493:3: ()
            // InternalLinkerScript.g:4494:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLVariableAccess().getLVariableAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:4500:3: ( (lv_feature_1_0= ruleValidID ) )
            // InternalLinkerScript.g:4501:4: (lv_feature_1_0= ruleValidID )
            {
            // InternalLinkerScript.g:4501:4: (lv_feature_1_0= ruleValidID )
            // InternalLinkerScript.g:4502:5: lv_feature_1_0= ruleValidID
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
    // InternalLinkerScript.g:4523:1: entryRuleLParenthesizedExpression returns [EObject current=null] : iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF ;
    public final EObject entryRuleLParenthesizedExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLParenthesizedExpression = null;


        try {
            // InternalLinkerScript.g:4523:65: (iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF )
            // InternalLinkerScript.g:4524:2: iv_ruleLParenthesizedExpression= ruleLParenthesizedExpression EOF
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
    // InternalLinkerScript.g:4530:1: ruleLParenthesizedExpression returns [EObject current=null] : (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' ) ;
    public final EObject ruleLParenthesizedExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        EObject this_LExpression_1 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4536:2: ( (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' ) )
            // InternalLinkerScript.g:4537:2: (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' )
            {
            // InternalLinkerScript.g:4537:2: (otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')' )
            // InternalLinkerScript.g:4538:3: otherlv_0= '(' this_LExpression_1= ruleLExpression otherlv_2= ')'
            {
            otherlv_0=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_0, grammarAccess.getLParenthesizedExpressionAccess().getLeftParenthesisKeyword_0());
              		
            }
            if ( state.backtracking==0 ) {

              			newCompositeNode(grammarAccess.getLParenthesizedExpressionAccess().getLExpressionParserRuleCall_1());
              		
            }
            pushFollow(FOLLOW_9);
            this_LExpression_1=ruleLExpression();

            state._fsp--;
            if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			current = this_LExpression_1;
              			afterParserOrEnumRuleCall();
              		
            }
            otherlv_2=(Token)match(input,14,FOLLOW_2); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getLParenthesizedExpressionAccess().getRightParenthesisKeyword_2());
              		
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
    // InternalLinkerScript.g:4558:1: entryRuleLengthCall returns [EObject current=null] : iv_ruleLengthCall= ruleLengthCall EOF ;
    public final EObject entryRuleLengthCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLengthCall = null;


        try {
            // InternalLinkerScript.g:4558:51: (iv_ruleLengthCall= ruleLengthCall EOF )
            // InternalLinkerScript.g:4559:2: iv_ruleLengthCall= ruleLengthCall EOF
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
    // InternalLinkerScript.g:4565:1: ruleLengthCall returns [EObject current=null] : ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) ;
    public final EObject ruleLengthCall() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        AntlrDatatypeRuleToken lv_memory_3_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4571:2: ( ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' ) )
            // InternalLinkerScript.g:4572:2: ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            {
            // InternalLinkerScript.g:4572:2: ( () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')' )
            // InternalLinkerScript.g:4573:3: () otherlv_1= 'LENGTH' otherlv_2= '(' ( (lv_memory_3_0= ruleValidID ) ) otherlv_4= ')'
            {
            // InternalLinkerScript.g:4573:3: ()
            // InternalLinkerScript.g:4574:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLengthCallAccess().getLengthCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,66,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getLengthCallAccess().getLENGTHKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_19); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getLengthCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:4588:3: ( (lv_memory_3_0= ruleValidID ) )
            // InternalLinkerScript.g:4589:4: (lv_memory_3_0= ruleValidID )
            {
            // InternalLinkerScript.g:4589:4: (lv_memory_3_0= ruleValidID )
            // InternalLinkerScript.g:4590:5: lv_memory_3_0= ruleValidID
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getLengthCallAccess().getMemoryValidIDParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_9);
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


    // $ANTLR start "entryRuleAlignCall"
    // InternalLinkerScript.g:4615:1: entryRuleAlignCall returns [EObject current=null] : iv_ruleAlignCall= ruleAlignCall EOF ;
    public final EObject entryRuleAlignCall() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAlignCall = null;


        try {
            // InternalLinkerScript.g:4615:50: (iv_ruleAlignCall= ruleAlignCall EOF )
            // InternalLinkerScript.g:4616:2: iv_ruleAlignCall= ruleAlignCall EOF
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
    // InternalLinkerScript.g:4622:1: ruleAlignCall returns [EObject current=null] : ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' ) ;
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
            // InternalLinkerScript.g:4628:2: ( ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' ) )
            // InternalLinkerScript.g:4629:2: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' )
            {
            // InternalLinkerScript.g:4629:2: ( () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')' )
            // InternalLinkerScript.g:4630:3: () otherlv_1= 'ALIGN' otherlv_2= '(' ( (lv_expOrAlign_3_0= ruleLExpression ) ) (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )? otherlv_6= ')'
            {
            // InternalLinkerScript.g:4630:3: ()
            // InternalLinkerScript.g:4631:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getAlignCallAccess().getAlignCallAction_0(),
              					current);
              			
            }

            }

            otherlv_1=(Token)match(input,21,FOLLOW_12); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_1, grammarAccess.getAlignCallAccess().getALIGNKeyword_1());
              		
            }
            otherlv_2=(Token)match(input,13,FOLLOW_13); if (state.failed) return current;
            if ( state.backtracking==0 ) {

              			newLeafNode(otherlv_2, grammarAccess.getAlignCallAccess().getLeftParenthesisKeyword_2());
              		
            }
            // InternalLinkerScript.g:4645:3: ( (lv_expOrAlign_3_0= ruleLExpression ) )
            // InternalLinkerScript.g:4646:4: (lv_expOrAlign_3_0= ruleLExpression )
            {
            // InternalLinkerScript.g:4646:4: (lv_expOrAlign_3_0= ruleLExpression )
            // InternalLinkerScript.g:4647:5: lv_expOrAlign_3_0= ruleLExpression
            {
            if ( state.backtracking==0 ) {

              					newCompositeNode(grammarAccess.getAlignCallAccess().getExpOrAlignLExpressionParserRuleCall_3_0());
              				
            }
            pushFollow(FOLLOW_55);
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

            // InternalLinkerScript.g:4664:3: (otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) ) )?
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( (LA68_0==20) ) {
                alt68=1;
            }
            switch (alt68) {
                case 1 :
                    // InternalLinkerScript.g:4665:4: otherlv_4= ',' ( (lv_align_5_0= ruleLExpression ) )
                    {
                    otherlv_4=(Token)match(input,20,FOLLOW_13); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      				newLeafNode(otherlv_4, grammarAccess.getAlignCallAccess().getCommaKeyword_4_0());
                      			
                    }
                    // InternalLinkerScript.g:4669:4: ( (lv_align_5_0= ruleLExpression ) )
                    // InternalLinkerScript.g:4670:5: (lv_align_5_0= ruleLExpression )
                    {
                    // InternalLinkerScript.g:4670:5: (lv_align_5_0= ruleLExpression )
                    // InternalLinkerScript.g:4671:6: lv_align_5_0= ruleLExpression
                    {
                    if ( state.backtracking==0 ) {

                      						newCompositeNode(grammarAccess.getAlignCallAccess().getAlignLExpressionParserRuleCall_4_1_0());
                      					
                    }
                    pushFollow(FOLLOW_9);
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


    // $ANTLR start "entryRuleLNumberLiteral"
    // InternalLinkerScript.g:4697:1: entryRuleLNumberLiteral returns [EObject current=null] : iv_ruleLNumberLiteral= ruleLNumberLiteral EOF ;
    public final EObject entryRuleLNumberLiteral() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLNumberLiteral = null;


        try {
            // InternalLinkerScript.g:4697:55: (iv_ruleLNumberLiteral= ruleLNumberLiteral EOF )
            // InternalLinkerScript.g:4698:2: iv_ruleLNumberLiteral= ruleLNumberLiteral EOF
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
    // InternalLinkerScript.g:4704:1: ruleLNumberLiteral returns [EObject current=null] : ( () ( (lv_value_1_0= ruleNumber ) ) ) ;
    public final EObject ruleLNumberLiteral() throws RecognitionException {
        EObject current = null;

        AntlrDatatypeRuleToken lv_value_1_0 = null;



        	enterRule();

        try {
            // InternalLinkerScript.g:4710:2: ( ( () ( (lv_value_1_0= ruleNumber ) ) ) )
            // InternalLinkerScript.g:4711:2: ( () ( (lv_value_1_0= ruleNumber ) ) )
            {
            // InternalLinkerScript.g:4711:2: ( () ( (lv_value_1_0= ruleNumber ) ) )
            // InternalLinkerScript.g:4712:3: () ( (lv_value_1_0= ruleNumber ) )
            {
            // InternalLinkerScript.g:4712:3: ()
            // InternalLinkerScript.g:4713:4: 
            {
            if ( state.backtracking==0 ) {

              				current = forceCreateModelElement(
              					grammarAccess.getLNumberLiteralAccess().getLNumberLiteralAction_0(),
              					current);
              			
            }

            }

            // InternalLinkerScript.g:4719:3: ( (lv_value_1_0= ruleNumber ) )
            // InternalLinkerScript.g:4720:4: (lv_value_1_0= ruleNumber )
            {
            // InternalLinkerScript.g:4720:4: (lv_value_1_0= ruleNumber )
            // InternalLinkerScript.g:4721:5: lv_value_1_0= ruleNumber
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
    // InternalLinkerScript.g:4742:1: entryRuleValidID returns [String current=null] : iv_ruleValidID= ruleValidID EOF ;
    public final String entryRuleValidID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValidID = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:4744:2: (iv_ruleValidID= ruleValidID EOF )
            // InternalLinkerScript.g:4745:2: iv_ruleValidID= ruleValidID EOF
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
    // InternalLinkerScript.g:4754:1: ruleValidID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' ) ;
    public final AntlrDatatypeRuleToken ruleValidID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_ID_0=null;
        Token kw=null;


        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:4761:2: ( (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' ) )
            // InternalLinkerScript.g:4762:2: (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' )
            {
            // InternalLinkerScript.g:4762:2: (this_ID_0= RULE_ID | kw= 'MEMORY' | kw= 'o' | kw= 'org' | kw= 'l' | kw= 'len' )
            int alt69=6;
            switch ( input.LA(1) ) {
            case RULE_ID:
                {
                alt69=1;
                }
                break;
            case 62:
                {
                alt69=2;
                }
                break;
            case 65:
                {
                alt69=3;
                }
                break;
            case 64:
                {
                alt69=4;
                }
                break;
            case 68:
                {
                alt69=5;
                }
                break;
            case 67:
                {
                alt69=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 69, 0, input);

                throw nvae;
            }

            switch (alt69) {
                case 1 :
                    // InternalLinkerScript.g:4763:3: this_ID_0= RULE_ID
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
                    // InternalLinkerScript.g:4771:3: kw= 'MEMORY'
                    {
                    kw=(Token)match(input,62,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getMEMORYKeyword_1());
                      		
                    }

                    }
                    break;
                case 3 :
                    // InternalLinkerScript.g:4777:3: kw= 'o'
                    {
                    kw=(Token)match(input,65,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getOKeyword_2());
                      		
                    }

                    }
                    break;
                case 4 :
                    // InternalLinkerScript.g:4783:3: kw= 'org'
                    {
                    kw=(Token)match(input,64,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getOrgKeyword_3());
                      		
                    }

                    }
                    break;
                case 5 :
                    // InternalLinkerScript.g:4789:3: kw= 'l'
                    {
                    kw=(Token)match(input,68,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidIDAccess().getLKeyword_4());
                      		
                    }

                    }
                    break;
                case 6 :
                    // InternalLinkerScript.g:4795:3: kw= 'len'
                    {
                    kw=(Token)match(input,67,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:4807:1: entryRuleWildID returns [String current=null] : iv_ruleWildID= ruleWildID EOF ;
    public final String entryRuleWildID() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleWildID = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:4809:2: (iv_ruleWildID= ruleWildID EOF )
            // InternalLinkerScript.g:4810:2: iv_ruleWildID= ruleWildID EOF
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
    // InternalLinkerScript.g:4819:1: ruleWildID returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '*' | this_ValidID_1= ruleValidID ) ;
    public final AntlrDatatypeRuleToken ruleWildID() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;
        AntlrDatatypeRuleToken this_ValidID_1 = null;



        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:4826:2: ( (kw= '*' | this_ValidID_1= ruleValidID ) )
            // InternalLinkerScript.g:4827:2: (kw= '*' | this_ValidID_1= ruleValidID )
            {
            // InternalLinkerScript.g:4827:2: (kw= '*' | this_ValidID_1= ruleValidID )
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==77) ) {
                alt70=1;
            }
            else if ( (LA70_0==RULE_ID||LA70_0==62||(LA70_0>=64 && LA70_0<=65)||(LA70_0>=67 && LA70_0<=68)) ) {
                alt70=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 70, 0, input);

                throw nvae;
            }
            switch (alt70) {
                case 1 :
                    // InternalLinkerScript.g:4828:3: kw= '*'
                    {
                    kw=(Token)match(input,77,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getWildIDAccess().getAsteriskKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4834:3: this_ValidID_1= ruleValidID
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
    // InternalLinkerScript.g:4851:1: entryRuleValidFunc returns [String current=null] : iv_ruleValidFunc= ruleValidFunc EOF ;
    public final String entryRuleValidFunc() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleValidFunc = null;


        try {
            // InternalLinkerScript.g:4851:49: (iv_ruleValidFunc= ruleValidFunc EOF )
            // InternalLinkerScript.g:4852:2: iv_ruleValidFunc= ruleValidFunc EOF
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
    // InternalLinkerScript.g:4858:1: ruleValidFunc returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= 'LENGTH' | kw= 'ALIGN' ) ;
    public final AntlrDatatypeRuleToken ruleValidFunc() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalLinkerScript.g:4864:2: ( (kw= 'LENGTH' | kw= 'ALIGN' ) )
            // InternalLinkerScript.g:4865:2: (kw= 'LENGTH' | kw= 'ALIGN' )
            {
            // InternalLinkerScript.g:4865:2: (kw= 'LENGTH' | kw= 'ALIGN' )
            int alt71=2;
            int LA71_0 = input.LA(1);

            if ( (LA71_0==66) ) {
                alt71=1;
            }
            else if ( (LA71_0==21) ) {
                alt71=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 71, 0, input);

                throw nvae;
            }
            switch (alt71) {
                case 1 :
                    // InternalLinkerScript.g:4866:3: kw= 'LENGTH'
                    {
                    kw=(Token)match(input,66,FOLLOW_2); if (state.failed) return current;
                    if ( state.backtracking==0 ) {

                      			current.merge(kw);
                      			newLeafNode(kw, grammarAccess.getValidFuncAccess().getLENGTHKeyword_0());
                      		
                    }

                    }
                    break;
                case 2 :
                    // InternalLinkerScript.g:4872:3: kw= 'ALIGN'
                    {
                    kw=(Token)match(input,21,FOLLOW_2); if (state.failed) return current;
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
    // InternalLinkerScript.g:4881:1: entryRuleNumber returns [String current=null] : iv_ruleNumber= ruleNumber EOF ;
    public final String entryRuleNumber() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNumber = null;



        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:4883:2: (iv_ruleNumber= ruleNumber EOF )
            // InternalLinkerScript.g:4884:2: iv_ruleNumber= ruleNumber EOF
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
    // InternalLinkerScript.g:4893:1: ruleNumber returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX ) ;
    public final AntlrDatatypeRuleToken ruleNumber() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_DEC_0=null;
        Token this_HEX_1=null;


        	enterRule();
        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // InternalLinkerScript.g:4900:2: ( (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX ) )
            // InternalLinkerScript.g:4901:2: (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX )
            {
            // InternalLinkerScript.g:4901:2: (this_DEC_0= RULE_DEC | this_HEX_1= RULE_HEX )
            int alt72=2;
            int LA72_0 = input.LA(1);

            if ( (LA72_0==RULE_DEC) ) {
                alt72=1;
            }
            else if ( (LA72_0==RULE_HEX) ) {
                alt72=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return current;}
                NoViableAltException nvae =
                    new NoViableAltException("", 72, 0, input);

                throw nvae;
            }
            switch (alt72) {
                case 1 :
                    // InternalLinkerScript.g:4902:3: this_DEC_0= RULE_DEC
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
                    // InternalLinkerScript.g:4910:3: this_HEX_1= RULE_HEX
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

    // $ANTLR start synpred1_InternalLinkerScript
    public final void synpred1_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:2978:5: ( ( () ( ( ruleOpMultiAssign ) ) ) )
        // InternalLinkerScript.g:2978:6: ( () ( ( ruleOpMultiAssign ) ) )
        {
        // InternalLinkerScript.g:2978:6: ( () ( ( ruleOpMultiAssign ) ) )
        // InternalLinkerScript.g:2979:6: () ( ( ruleOpMultiAssign ) )
        {
        // InternalLinkerScript.g:2979:6: ()
        // InternalLinkerScript.g:2980:6: 
        {
        }

        // InternalLinkerScript.g:2981:6: ( ( ruleOpMultiAssign ) )
        // InternalLinkerScript.g:2982:7: ( ruleOpMultiAssign )
        {
        // InternalLinkerScript.g:2982:7: ( ruleOpMultiAssign )
        // InternalLinkerScript.g:2983:8: ruleOpMultiAssign
        {
        pushFollow(FOLLOW_2);
        ruleOpMultiAssign();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred1_InternalLinkerScript

    // $ANTLR start synpred2_InternalLinkerScript
    public final void synpred2_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:3151:5: ( ( () ( ( ruleOpOr ) ) ) )
        // InternalLinkerScript.g:3151:6: ( () ( ( ruleOpOr ) ) )
        {
        // InternalLinkerScript.g:3151:6: ( () ( ( ruleOpOr ) ) )
        // InternalLinkerScript.g:3152:6: () ( ( ruleOpOr ) )
        {
        // InternalLinkerScript.g:3152:6: ()
        // InternalLinkerScript.g:3153:6: 
        {
        }

        // InternalLinkerScript.g:3154:6: ( ( ruleOpOr ) )
        // InternalLinkerScript.g:3155:7: ( ruleOpOr )
        {
        // InternalLinkerScript.g:3155:7: ( ruleOpOr )
        // InternalLinkerScript.g:3156:8: ruleOpOr
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
        // InternalLinkerScript.g:3261:5: ( ( () ( ( ruleOpAnd ) ) ) )
        // InternalLinkerScript.g:3261:6: ( () ( ( ruleOpAnd ) ) )
        {
        // InternalLinkerScript.g:3261:6: ( () ( ( ruleOpAnd ) ) )
        // InternalLinkerScript.g:3262:6: () ( ( ruleOpAnd ) )
        {
        // InternalLinkerScript.g:3262:6: ()
        // InternalLinkerScript.g:3263:6: 
        {
        }

        // InternalLinkerScript.g:3264:6: ( ( ruleOpAnd ) )
        // InternalLinkerScript.g:3265:7: ( ruleOpAnd )
        {
        // InternalLinkerScript.g:3265:7: ( ruleOpAnd )
        // InternalLinkerScript.g:3266:8: ruleOpAnd
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
        // InternalLinkerScript.g:3371:5: ( ( () ( ( ruleOpBitwiseOr ) ) ) )
        // InternalLinkerScript.g:3371:6: ( () ( ( ruleOpBitwiseOr ) ) )
        {
        // InternalLinkerScript.g:3371:6: ( () ( ( ruleOpBitwiseOr ) ) )
        // InternalLinkerScript.g:3372:6: () ( ( ruleOpBitwiseOr ) )
        {
        // InternalLinkerScript.g:3372:6: ()
        // InternalLinkerScript.g:3373:6: 
        {
        }

        // InternalLinkerScript.g:3374:6: ( ( ruleOpBitwiseOr ) )
        // InternalLinkerScript.g:3375:7: ( ruleOpBitwiseOr )
        {
        // InternalLinkerScript.g:3375:7: ( ruleOpBitwiseOr )
        // InternalLinkerScript.g:3376:8: ruleOpBitwiseOr
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
        // InternalLinkerScript.g:3481:5: ( ( () ( ( ruleOpBitwiseAnd ) ) ) )
        // InternalLinkerScript.g:3481:6: ( () ( ( ruleOpBitwiseAnd ) ) )
        {
        // InternalLinkerScript.g:3481:6: ( () ( ( ruleOpBitwiseAnd ) ) )
        // InternalLinkerScript.g:3482:6: () ( ( ruleOpBitwiseAnd ) )
        {
        // InternalLinkerScript.g:3482:6: ()
        // InternalLinkerScript.g:3483:6: 
        {
        }

        // InternalLinkerScript.g:3484:6: ( ( ruleOpBitwiseAnd ) )
        // InternalLinkerScript.g:3485:7: ( ruleOpBitwiseAnd )
        {
        // InternalLinkerScript.g:3485:7: ( ruleOpBitwiseAnd )
        // InternalLinkerScript.g:3486:8: ruleOpBitwiseAnd
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
        // InternalLinkerScript.g:3591:5: ( ( () ( ( ruleOpEquality ) ) ) )
        // InternalLinkerScript.g:3591:6: ( () ( ( ruleOpEquality ) ) )
        {
        // InternalLinkerScript.g:3591:6: ( () ( ( ruleOpEquality ) ) )
        // InternalLinkerScript.g:3592:6: () ( ( ruleOpEquality ) )
        {
        // InternalLinkerScript.g:3592:6: ()
        // InternalLinkerScript.g:3593:6: 
        {
        }

        // InternalLinkerScript.g:3594:6: ( ( ruleOpEquality ) )
        // InternalLinkerScript.g:3595:7: ( ruleOpEquality )
        {
        // InternalLinkerScript.g:3595:7: ( ruleOpEquality )
        // InternalLinkerScript.g:3596:8: ruleOpEquality
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
        // InternalLinkerScript.g:3709:5: ( ( () ( ( ruleOpCompare ) ) ) )
        // InternalLinkerScript.g:3709:6: ( () ( ( ruleOpCompare ) ) )
        {
        // InternalLinkerScript.g:3709:6: ( () ( ( ruleOpCompare ) ) )
        // InternalLinkerScript.g:3710:6: () ( ( ruleOpCompare ) )
        {
        // InternalLinkerScript.g:3710:6: ()
        // InternalLinkerScript.g:3711:6: 
        {
        }

        // InternalLinkerScript.g:3712:6: ( ( ruleOpCompare ) )
        // InternalLinkerScript.g:3713:7: ( ruleOpCompare )
        {
        // InternalLinkerScript.g:3713:7: ( ruleOpCompare )
        // InternalLinkerScript.g:3714:8: ruleOpCompare
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
        // InternalLinkerScript.g:3846:5: ( ( () ( ( ruleOpOther ) ) ) )
        // InternalLinkerScript.g:3846:6: ( () ( ( ruleOpOther ) ) )
        {
        // InternalLinkerScript.g:3846:6: ( () ( ( ruleOpOther ) ) )
        // InternalLinkerScript.g:3847:6: () ( ( ruleOpOther ) )
        {
        // InternalLinkerScript.g:3847:6: ()
        // InternalLinkerScript.g:3848:6: 
        {
        }

        // InternalLinkerScript.g:3849:6: ( ( ruleOpOther ) )
        // InternalLinkerScript.g:3850:7: ( ruleOpOther )
        {
        // InternalLinkerScript.g:3850:7: ( ruleOpOther )
        // InternalLinkerScript.g:3851:8: ruleOpOther
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
        // InternalLinkerScript.g:3984:5: ( ( () ( ( ruleOpAdd ) ) ) )
        // InternalLinkerScript.g:3984:6: ( () ( ( ruleOpAdd ) ) )
        {
        // InternalLinkerScript.g:3984:6: ( () ( ( ruleOpAdd ) ) )
        // InternalLinkerScript.g:3985:6: () ( ( ruleOpAdd ) )
        {
        // InternalLinkerScript.g:3985:6: ()
        // InternalLinkerScript.g:3986:6: 
        {
        }

        // InternalLinkerScript.g:3987:6: ( ( ruleOpAdd ) )
        // InternalLinkerScript.g:3988:7: ( ruleOpAdd )
        {
        // InternalLinkerScript.g:3988:7: ( ruleOpAdd )
        // InternalLinkerScript.g:3989:8: ruleOpAdd
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
        // InternalLinkerScript.g:4102:5: ( ( () ( ( ruleOpMulti ) ) ) )
        // InternalLinkerScript.g:4102:6: ( () ( ( ruleOpMulti ) ) )
        {
        // InternalLinkerScript.g:4102:6: ( () ( ( ruleOpMulti ) ) )
        // InternalLinkerScript.g:4103:6: () ( ( ruleOpMulti ) )
        {
        // InternalLinkerScript.g:4103:6: ()
        // InternalLinkerScript.g:4104:6: 
        {
        }

        // InternalLinkerScript.g:4105:6: ( ( ruleOpMulti ) )
        // InternalLinkerScript.g:4106:7: ( ruleOpMulti )
        {
        // InternalLinkerScript.g:4106:7: ( ruleOpMulti )
        // InternalLinkerScript.g:4107:8: ruleOpMulti
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

    // $ANTLR start synpred13_InternalLinkerScript
    public final void synpred13_InternalLinkerScript_fragment() throws RecognitionException {   
        // InternalLinkerScript.g:4342:4: ( ( () ( ( ruleOpPostfix ) ) ) )
        // InternalLinkerScript.g:4342:5: ( () ( ( ruleOpPostfix ) ) )
        {
        // InternalLinkerScript.g:4342:5: ( () ( ( ruleOpPostfix ) ) )
        // InternalLinkerScript.g:4343:5: () ( ( ruleOpPostfix ) )
        {
        // InternalLinkerScript.g:4343:5: ()
        // InternalLinkerScript.g:4344:5: 
        {
        }

        // InternalLinkerScript.g:4345:5: ( ( ruleOpPostfix ) )
        // InternalLinkerScript.g:4346:6: ( ruleOpPostfix )
        {
        // InternalLinkerScript.g:4346:6: ( ruleOpPostfix )
        // InternalLinkerScript.g:4347:7: ruleOpPostfix
        {
        pushFollow(FOLLOW_2);
        ruleOpPostfix();

        state._fsp--;
        if (state.failed) return ;

        }


        }


        }


        }
    }
    // $ANTLR end synpred13_InternalLinkerScript

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
    public final boolean synpred13_InternalLinkerScript() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_InternalLinkerScript_fragment(); // can never throw exception
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


    protected DFA20 dfa20 = new DFA20(this);
    protected DFA36 dfa36 = new DFA36(this);
    protected DFA39 dfa39 = new DFA39(this);
    protected DFA47 dfa47 = new DFA47(this);
    protected DFA55 dfa55 = new DFA55(this);
    protected DFA57 dfa57 = new DFA57(this);
    static final String dfa_1s = "\24\uffff";
    static final String dfa_2s = "\1\uffff\7\20\14\uffff";
    static final String dfa_3s = "\10\4\3\uffff\1\15\6\uffff\1\4\1\uffff";
    static final String dfa_4s = "\10\115\3\uffff\1\15\6\uffff\1\115\1\uffff";
    static final String dfa_5s = "\10\uffff\1\1\1\2\1\3\1\uffff\1\5\1\6\1\7\1\10\1\11\1\12\1\uffff\1\4";
    static final String dfa_6s = "\24\uffff}>";
    static final String[] dfa_7s = {
            "\1\2\32\uffff\1\21\1\11\1\12\1\13\1\15\1\16\1\17\5\14\3\10\10\uffff\1\20\1\uffff\6\20\1\3\1\uffff\1\5\1\4\1\uffff\1\7\1\6\10\uffff\1\1",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            "\1\20\7\uffff\2\20\4\uffff\2\10\13\uffff\17\20\5\10\1\uffff\2\10\1\20\1\uffff\7\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
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
            "\1\20\34\uffff\1\23\1\20\26\uffff\2\20\2\uffff\2\20\1\uffff\2\20\1\uffff\2\20\10\uffff\1\20",
            ""
    };

    static final short[] dfa_1 = DFA.unpackEncodedString(dfa_1s);
    static final short[] dfa_2 = DFA.unpackEncodedString(dfa_2s);
    static final char[] dfa_3 = DFA.unpackEncodedStringToUnsignedChars(dfa_3s);
    static final char[] dfa_4 = DFA.unpackEncodedStringToUnsignedChars(dfa_4s);
    static final short[] dfa_5 = DFA.unpackEncodedString(dfa_5s);
    static final short[] dfa_6 = DFA.unpackEncodedString(dfa_6s);
    static final short[][] dfa_7 = unpackEncodedStringArray(dfa_7s);

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = dfa_1;
            this.eof = dfa_2;
            this.min = dfa_3;
            this.max = dfa_4;
            this.accept = dfa_5;
            this.special = dfa_6;
            this.transition = dfa_7;
        }
        public String getDescription() {
            return "751:2: ( ( () ( (lv_assignment_1_0= ruleAssignmentRule ) ) (otherlv_2= ',' | otherlv_3= ';' ) ) | ( () otherlv_5= 'CREATE_OBJECT_SYMBOLS' ) | ( () otherlv_7= 'CONSTRUCTORS' ) | ( () otherlv_9= 'SORT_BY_NAME' otherlv_10= '(' otherlv_11= 'CONSTRUCTORS' otherlv_12= ')' ) | ( () ( (lv_size_14_0= ruleStatementDataSize ) ) otherlv_15= '(' ( (lv_data_16_0= ruleLExpression ) ) otherlv_17= ')' ) | ( () otherlv_19= 'FILL' otherlv_20= '(' ( (lv_fill_21_0= ruleLExpression ) ) otherlv_22= ')' ) | ( () otherlv_24= 'ASSERT' otherlv_25= '(' ( (lv_exp_26_0= ruleLExpression ) ) otherlv_27= ',' ( (lv_message_28_0= ruleValidID ) ) otherlv_29= ')' ) | ( () otherlv_31= 'INCLUDE' ( (lv_filename_32_0= ruleWildID ) ) ) | ( () ( (lv_spec_34_0= ruleInputSection ) ) ) | ( () otherlv_36= ';' ) )";
        }
    }
    static final String dfa_8s = "\71\uffff";
    static final String dfa_9s = "\2\uffff\7\14\60\uffff";
    static final String dfa_10s = "\1\4\1\15\7\4\1\uffff\1\15\1\4\1\uffff\1\4\7\16\10\15\1\uffff\3\4\1\uffff\16\16\2\4\7\16";
    static final String dfa_11s = "\1\115\1\15\7\115\1\uffff\1\15\1\115\1\uffff\1\115\7\67\1\15\7\16\1\uffff\3\115\1\uffff\16\67\2\115\7\67";
    static final String dfa_12s = "\11\uffff\1\2\2\uffff\1\1\20\uffff\1\4\3\uffff\1\3\27\uffff";
    static final String dfa_13s = "\71\uffff}>";
    static final String[] dfa_14s = {
            "\1\3\35\uffff\1\11\23\uffff\1\1\1\uffff\1\12\5\11\1\4\1\uffff\1\6\1\5\1\uffff\1\10\1\7\10\uffff\1\2",
            "\1\13",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\14\7\uffff\1\14\1\11\21\uffff\17\14\10\uffff\1\14\1\uffff\7\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "",
            "\1\15",
            "\1\17\71\uffff\1\20\1\uffff\1\22\1\21\1\uffff\1\24\1\23\10\uffff\1\16",
            "",
            "\1\27\35\uffff\1\35\23\uffff\1\25\2\uffff\5\35\1\30\1\uffff\1\32\1\31\1\uffff\1\34\1\33\10\uffff\1\26",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\40",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "\1\35\1\41",
            "",
            "\1\43\71\uffff\1\44\1\uffff\1\46\1\45\1\uffff\1\50\1\47\10\uffff\1\42",
            "\1\3\35\uffff\1\11\26\uffff\5\11\1\4\1\uffff\1\6\1\5\1\uffff\1\10\1\7\10\uffff\1\2",
            "\1\52\71\uffff\1\53\1\uffff\1\55\1\54\1\uffff\1\57\1\56\10\uffff\1\51",
            "",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\37\50\uffff\1\36",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\63\71\uffff\1\64\1\uffff\1\66\1\65\1\uffff\1\70\1\67\10\uffff\1\62",
            "\1\27\35\uffff\1\35\26\uffff\5\35\1\30\1\uffff\1\32\1\31\1\uffff\1\34\1\33\10\uffff\1\26",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60",
            "\1\61\50\uffff\1\60"
    };

    static final short[] dfa_8 = DFA.unpackEncodedString(dfa_8s);
    static final short[] dfa_9 = DFA.unpackEncodedString(dfa_9s);
    static final char[] dfa_10 = DFA.unpackEncodedStringToUnsignedChars(dfa_10s);
    static final char[] dfa_11 = DFA.unpackEncodedStringToUnsignedChars(dfa_11s);
    static final short[] dfa_12 = DFA.unpackEncodedString(dfa_12s);
    static final short[] dfa_13 = DFA.unpackEncodedString(dfa_13s);
    static final short[][] dfa_14 = unpackEncodedStringArray(dfa_14s);

    class DFA36 extends DFA {

        public DFA36(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 36;
            this.eot = dfa_8;
            this.eof = dfa_9;
            this.min = dfa_10;
            this.max = dfa_11;
            this.accept = dfa_12;
            this.special = dfa_13;
            this.transition = dfa_14;
        }
        public String getDescription() {
            return "1546:2: ( ( () (otherlv_1= 'INPUT_SECTION_FLAGS' otherlv_2= '(' ( (lv_flags_3_0= ruleWildID ) ) (otherlv_4= '&' ( (lv_flags_5_0= ruleWildID ) ) )* otherlv_6= ')' )? ( (lv_file_7_0= ruleWildID ) ) ) | ( () (otherlv_9= 'INPUT_SECTION_FLAGS' otherlv_10= '(' ( (lv_flags_11_0= ruleWildID ) ) (otherlv_12= '&' ( (lv_flags_13_0= ruleWildID ) ) )* otherlv_14= ')' )? ( (lv_wildFile_15_0= ruleWildcardRule ) ) otherlv_16= '(' ( (lv_sections_17_0= ruleWildcardRule ) ) ( (otherlv_18= ',' )? ( (lv_sections_19_0= ruleWildcardRule ) ) )* otherlv_20= ')' ) | ( () ( (lv_keep_22_0= 'KEEP' ) ) otherlv_23= '(' (otherlv_24= 'INPUT_SECTION_FLAGS' otherlv_25= '(' ( (lv_flags_26_0= ruleWildID ) ) (otherlv_27= '&' ( (lv_flags_28_0= ruleWildID ) ) )* otherlv_29= ')' )? ( (lv_file_30_0= ruleWildID ) ) otherlv_31= ')' ) | ( () ( (lv_keep_33_0= 'KEEP' ) ) otherlv_34= '(' (otherlv_35= 'INPUT_SECTION_FLAGS' otherlv_36= '(' ( (lv_flags_37_0= ruleWildID ) ) (otherlv_38= '&' ( (lv_flags_39_0= ruleWildID ) ) )* otherlv_40= ')' )? ( (lv_wildFile_41_0= ruleWildcardRule ) ) otherlv_42= '(' ( (lv_sections_43_0= ruleWildcardRule ) ) ( (otherlv_44= ',' )? ( (lv_sections_45_0= ruleWildcardRule ) ) )* otherlv_46= ')' otherlv_47= ')' ) )";
        }
    }
    static final String dfa_15s = "\21\uffff";
    static final String dfa_16s = "\1\4\2\uffff\3\15\2\uffff\2\4\7\uffff";
    static final String dfa_17s = "\1\115\2\uffff\3\15\2\uffff\2\115\7\uffff";
    static final String dfa_18s = "\1\uffff\1\1\1\2\3\uffff\1\5\1\13\2\uffff\1\12\1\7\1\3\1\6\1\4\1\11\1\10";
    static final String dfa_19s = "\21\uffff}>";
    static final String[] dfa_20s = {
            "\1\1\35\uffff\1\4\26\uffff\1\2\1\5\1\6\1\7\1\3\1\1\1\uffff\2\1\1\uffff\2\1\10\uffff\1\1",
            "",
            "",
            "\1\10",
            "\1\10",
            "\1\11",
            "",
            "",
            "\1\14\35\uffff\1\13\26\uffff\1\12\1\15\2\uffff\1\13\1\14\1\uffff\2\14\1\uffff\2\14\10\uffff\1\14",
            "\1\16\35\uffff\1\20\27\uffff\1\17\2\uffff\1\20\1\16\1\uffff\2\16\1\uffff\2\16\10\uffff\1\16",
            "",
            "",
            "",
            "",
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

    class DFA39 extends DFA {

        public DFA39(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 39;
            this.eot = dfa_15;
            this.eof = dfa_15;
            this.min = dfa_16;
            this.max = dfa_17;
            this.accept = dfa_18;
            this.special = dfa_19;
            this.transition = dfa_20;
        }
        public String getDescription() {
            return "2064:2: ( ( () ( (lv_name_1_0= ruleWildID ) ) ) | ( () otherlv_3= 'EXCLUDE_FILE' otherlv_4= '(' ( (lv_excludes_5_0= ruleWildID ) )+ otherlv_6= ')' ( (lv_name_7_0= ruleWildID ) ) ) | ( () ruleSORT_BY_NAME otherlv_10= '(' ( (lv_name_11_0= ruleWildID ) ) otherlv_12= ')' ) | ( () otherlv_14= 'SORT_BY_ALIGNMENT' otherlv_15= '(' ( (lv_name_16_0= ruleWildID ) ) otherlv_17= ')' ) | ( () otherlv_19= 'SORT_NONE' otherlv_20= '(' ( (lv_name_21_0= ruleWildID ) ) otherlv_22= ')' ) | ( () ruleSORT_BY_NAME otherlv_25= '(' otherlv_26= 'SORT_BY_ALIGNMENT' otherlv_27= '(' ( (lv_name_28_0= ruleWildID ) ) otherlv_29= ')' otherlv_30= ')' ) | ( () ruleSORT_BY_NAME otherlv_33= '(' ruleSORT_BY_NAME otherlv_35= '(' ( (lv_name_36_0= ruleWildID ) ) otherlv_37= ')' otherlv_38= ')' ) | ( () otherlv_40= 'SORT_BY_ALIGNMENT' otherlv_41= '(' ruleSORT_BY_NAME otherlv_43= '(' ( (lv_name_44_0= ruleWildID ) ) otherlv_45= ')' otherlv_46= ')' ) | ( () otherlv_48= 'SORT_BY_ALIGNMENT' otherlv_49= '(' otherlv_50= 'SORT_BY_ALIGNMENT' otherlv_51= '(' ( (lv_name_52_0= ruleWildID ) ) otherlv_53= ')' otherlv_54= ')' ) | ( () ruleSORT_BY_NAME otherlv_57= '(' otherlv_58= 'EXCLUDE_FILE' otherlv_59= '(' ( (lv_excludes_60_0= ruleWildID ) )+ otherlv_61= ')' ( (lv_name_62_0= ruleWildID ) ) otherlv_63= ')' ) | ( () otherlv_65= 'SORT_BY_INIT_PRIORITY' otherlv_66= '(' ( (lv_name_67_0= ruleWildID ) ) otherlv_68= ')' ) )";
        }
    }
    static final String dfa_21s = "\12\uffff";
    static final String dfa_22s = "\1\11\11\uffff";
    static final String dfa_23s = "\1\4\11\uffff";
    static final String dfa_24s = "\1\104\11\uffff";
    static final String dfa_25s = "\1\uffff\10\1\1\2";
    static final String dfa_26s = "\1\0\11\uffff}>";
    static final String[] dfa_27s = {
            "\1\11\7\uffff\4\11\2\uffff\1\6\1\uffff\1\11\12\uffff\1\11\16\uffff\1\1\1\2\1\3\1\4\1\5\1\uffff\1\7\1\10\10\uffff\1\11\1\uffff\2\11\1\uffff\2\11",
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

    class DFA47 extends DFA {

        public DFA47(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 47;
            this.eot = dfa_21;
            this.eof = dfa_22;
            this.min = dfa_23;
            this.max = dfa_24;
            this.accept = dfa_25;
            this.special = dfa_26;
            this.transition = dfa_27;
        }
        public String getDescription() {
            return "2976:3: ( ( ( ( () ( ( ruleOpMultiAssign ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpMultiAssign ) ) ) ) ( (lv_rightOperand_3_0= ruleLAssignment ) ) )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA47_0 = input.LA(1);

                         
                        int index47_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA47_0==46) && (synpred1_InternalLinkerScript())) {s = 1;}

                        else if ( (LA47_0==47) && (synpred1_InternalLinkerScript())) {s = 2;}

                        else if ( (LA47_0==48) && (synpred1_InternalLinkerScript())) {s = 3;}

                        else if ( (LA47_0==49) && (synpred1_InternalLinkerScript())) {s = 4;}

                        else if ( (LA47_0==50) && (synpred1_InternalLinkerScript())) {s = 5;}

                        else if ( (LA47_0==18) && (synpred1_InternalLinkerScript())) {s = 6;}

                        else if ( (LA47_0==52) && (synpred1_InternalLinkerScript())) {s = 7;}

                        else if ( (LA47_0==53) && (synpred1_InternalLinkerScript())) {s = 8;}

                        else if ( (LA47_0==EOF||LA47_0==RULE_ID||(LA47_0>=12 && LA47_0<=15)||LA47_0==20||LA47_0==31||LA47_0==62||(LA47_0>=64 && LA47_0<=65)||(LA47_0>=67 && LA47_0<=68)) ) {s = 9;}

                         
                        input.seek(index47_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 47, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_28s = "\25\uffff";
    static final String dfa_29s = "\1\1\24\uffff";
    static final String dfa_30s = "\1\4\1\uffff\2\4\21\uffff";
    static final String dfa_31s = "\1\112\1\uffff\2\120\21\uffff";
    static final String dfa_32s = "\1\uffff\1\2\2\uffff\21\1";
    static final String dfa_33s = "\1\2\1\uffff\1\0\1\1\21\uffff}>";
    static final String[] dfa_34s = {
            "\1\1\7\uffff\4\1\2\uffff\1\3\1\uffff\1\1\12\uffff\1\1\16\uffff\4\1\1\2\1\4\2\1\1\uffff\1\1\6\uffff\1\1\1\uffff\2\1\1\uffff\2\1\1\uffff\5\1",
            "",
            "\1\16\1\13\1\14\6\uffff\1\15\5\uffff\1\24\1\uffff\1\12\34\uffff\1\1\13\uffff\1\17\1\uffff\1\21\1\20\1\11\1\23\1\22\1\5\5\uffff\1\7\1\6\3\uffff\1\10",
            "\1\16\1\13\1\14\6\uffff\1\15\7\uffff\1\12\35\uffff\1\1\12\uffff\1\17\1\uffff\1\21\1\20\1\11\1\23\1\22\1\5\5\uffff\1\7\1\6\3\uffff\1\10",
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

    class DFA55 extends DFA {

        public DFA55(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 55;
            this.eot = dfa_28;
            this.eof = dfa_29;
            this.min = dfa_30;
            this.max = dfa_31;
            this.accept = dfa_32;
            this.special = dfa_33;
            this.transition = dfa_34;
        }
        public String getDescription() {
            return "()* loopback of 3707:3: ( ( ( ( () ( ( ruleOpCompare ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpCompare ) ) ) ) ( (lv_rightOperand_3_0= ruleLOtherOperatorExpression ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA55_2 = input.LA(1);

                         
                        int index55_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA55_2==69) && (synpred7_InternalLinkerScript())) {s = 5;}

                        else if ( (LA55_2==76) && (synpred7_InternalLinkerScript())) {s = 6;}

                        else if ( (LA55_2==75) && (synpred7_InternalLinkerScript())) {s = 7;}

                        else if ( (LA55_2==80) && (synpred7_InternalLinkerScript())) {s = 8;}

                        else if ( (LA55_2==66) && (synpred7_InternalLinkerScript())) {s = 9;}

                        else if ( (LA55_2==21) && (synpred7_InternalLinkerScript())) {s = 10;}

                        else if ( (LA55_2==RULE_DEC) && (synpred7_InternalLinkerScript())) {s = 11;}

                        else if ( (LA55_2==RULE_HEX) && (synpred7_InternalLinkerScript())) {s = 12;}

                        else if ( (LA55_2==13) && (synpred7_InternalLinkerScript())) {s = 13;}

                        else if ( (LA55_2==RULE_ID) && (synpred7_InternalLinkerScript())) {s = 14;}

                        else if ( (LA55_2==62) && (synpred7_InternalLinkerScript())) {s = 15;}

                        else if ( (LA55_2==65) && (synpred7_InternalLinkerScript())) {s = 16;}

                        else if ( (LA55_2==64) && (synpred7_InternalLinkerScript())) {s = 17;}

                        else if ( (LA55_2==68) && (synpred7_InternalLinkerScript())) {s = 18;}

                        else if ( (LA55_2==67) && (synpred7_InternalLinkerScript())) {s = 19;}

                        else if ( (LA55_2==50) ) {s = 1;}

                        else if ( (LA55_2==19) && (synpred7_InternalLinkerScript())) {s = 20;}

                         
                        input.seek(index55_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA55_3 = input.LA(1);

                         
                        int index55_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA55_3==69) && (synpred7_InternalLinkerScript())) {s = 5;}

                        else if ( (LA55_3==76) && (synpred7_InternalLinkerScript())) {s = 6;}

                        else if ( (LA55_3==75) && (synpred7_InternalLinkerScript())) {s = 7;}

                        else if ( (LA55_3==80) && (synpred7_InternalLinkerScript())) {s = 8;}

                        else if ( (LA55_3==66) && (synpred7_InternalLinkerScript())) {s = 9;}

                        else if ( (LA55_3==21) && (synpred7_InternalLinkerScript())) {s = 10;}

                        else if ( (LA55_3==RULE_DEC) && (synpred7_InternalLinkerScript())) {s = 11;}

                        else if ( (LA55_3==RULE_HEX) && (synpred7_InternalLinkerScript())) {s = 12;}

                        else if ( (LA55_3==13) && (synpred7_InternalLinkerScript())) {s = 13;}

                        else if ( (LA55_3==RULE_ID) && (synpred7_InternalLinkerScript())) {s = 14;}

                        else if ( (LA55_3==62) && (synpred7_InternalLinkerScript())) {s = 15;}

                        else if ( (LA55_3==65) && (synpred7_InternalLinkerScript())) {s = 16;}

                        else if ( (LA55_3==64) && (synpred7_InternalLinkerScript())) {s = 17;}

                        else if ( (LA55_3==68) && (synpred7_InternalLinkerScript())) {s = 18;}

                        else if ( (LA55_3==67) && (synpred7_InternalLinkerScript())) {s = 19;}

                        else if ( (LA55_3==51) ) {s = 1;}

                         
                        input.seek(index55_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA55_0 = input.LA(1);

                         
                        int index55_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA55_0==EOF||LA55_0==RULE_ID||(LA55_0>=12 && LA55_0<=15)||LA55_0==20||LA55_0==31||(LA55_0>=46 && LA55_0<=49)||(LA55_0>=52 && LA55_0<=53)||LA55_0==55||LA55_0==62||(LA55_0>=64 && LA55_0<=65)||(LA55_0>=67 && LA55_0<=68)||(LA55_0>=70 && LA55_0<=74)) ) {s = 1;}

                        else if ( (LA55_0==50) ) {s = 2;}

                        else if ( (LA55_0==18) ) {s = 3;}

                        else if ( (LA55_0==51) && (synpred7_InternalLinkerScript())) {s = 4;}

                         
                        input.seek(index55_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 55, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String dfa_35s = "\1\4\1\uffff\3\4\20\uffff";
    static final String dfa_36s = "\1\112\1\uffff\3\120\20\uffff";
    static final String dfa_37s = "\1\uffff\1\2\3\uffff\20\1";
    static final String dfa_38s = "\3\uffff\1\1\1\0\20\uffff}>";
    static final String[] dfa_39s = {
            "\1\1\7\uffff\4\1\2\uffff\1\3\1\uffff\1\1\12\uffff\1\1\16\uffff\4\1\1\2\3\1\1\uffff\1\1\6\uffff\1\1\1\uffff\2\1\1\uffff\2\1\1\uffff\5\1",
            "",
            "\3\1\6\uffff\1\1\5\uffff\1\1\1\uffff\1\1\34\uffff\1\4\13\uffff\1\1\1\uffff\6\1\5\uffff\2\1\3\uffff\1\1",
            "\3\1\6\uffff\1\1\4\uffff\1\5\2\uffff\1\1\35\uffff\1\1\12\uffff\1\1\1\uffff\6\1\5\uffff\2\1\3\uffff\1\1",
            "\1\17\1\14\1\15\6\uffff\1\16\5\uffff\1\1\1\uffff\1\13\50\uffff\1\20\1\uffff\1\22\1\21\1\12\1\24\1\23\1\6\5\uffff\1\10\1\7\3\uffff\1\11",
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
    static final char[] dfa_35 = DFA.unpackEncodedStringToUnsignedChars(dfa_35s);
    static final char[] dfa_36 = DFA.unpackEncodedStringToUnsignedChars(dfa_36s);
    static final short[] dfa_37 = DFA.unpackEncodedString(dfa_37s);
    static final short[] dfa_38 = DFA.unpackEncodedString(dfa_38s);
    static final short[][] dfa_39 = unpackEncodedStringArray(dfa_39s);

    class DFA57 extends DFA {

        public DFA57(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 57;
            this.eot = dfa_28;
            this.eof = dfa_29;
            this.min = dfa_35;
            this.max = dfa_36;
            this.accept = dfa_37;
            this.special = dfa_38;
            this.transition = dfa_39;
        }
        public String getDescription() {
            return "()* loopback of 3844:3: ( ( ( ( () ( ( ruleOpOther ) ) ) )=> ( () ( (lv_feature_2_0= ruleOpOther ) ) ) ) ( (lv_rightOperand_3_0= ruleLAdditiveExpression ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA57_4 = input.LA(1);

                         
                        int index57_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA57_4==19) ) {s = 1;}

                        else if ( (LA57_4==69) && (synpred8_InternalLinkerScript())) {s = 6;}

                        else if ( (LA57_4==76) && (synpred8_InternalLinkerScript())) {s = 7;}

                        else if ( (LA57_4==75) && (synpred8_InternalLinkerScript())) {s = 8;}

                        else if ( (LA57_4==80) && (synpred8_InternalLinkerScript())) {s = 9;}

                        else if ( (LA57_4==66) && (synpred8_InternalLinkerScript())) {s = 10;}

                        else if ( (LA57_4==21) && (synpred8_InternalLinkerScript())) {s = 11;}

                        else if ( (LA57_4==RULE_DEC) && (synpred8_InternalLinkerScript())) {s = 12;}

                        else if ( (LA57_4==RULE_HEX) && (synpred8_InternalLinkerScript())) {s = 13;}

                        else if ( (LA57_4==13) && (synpred8_InternalLinkerScript())) {s = 14;}

                        else if ( (LA57_4==RULE_ID) && (synpred8_InternalLinkerScript())) {s = 15;}

                        else if ( (LA57_4==62) && (synpred8_InternalLinkerScript())) {s = 16;}

                        else if ( (LA57_4==65) && (synpred8_InternalLinkerScript())) {s = 17;}

                        else if ( (LA57_4==64) && (synpred8_InternalLinkerScript())) {s = 18;}

                        else if ( (LA57_4==68) && (synpred8_InternalLinkerScript())) {s = 19;}

                        else if ( (LA57_4==67) && (synpred8_InternalLinkerScript())) {s = 20;}

                         
                        input.seek(index57_4);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA57_3 = input.LA(1);

                         
                        int index57_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA57_3>=RULE_ID && LA57_3<=RULE_HEX)||LA57_3==13||LA57_3==21||LA57_3==51||LA57_3==62||(LA57_3>=64 && LA57_3<=69)||(LA57_3>=75 && LA57_3<=76)||LA57_3==80) ) {s = 1;}

                        else if ( (LA57_3==18) && (synpred8_InternalLinkerScript())) {s = 5;}

                         
                        input.seek(index57_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 57, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x4000000000001010L,0x000000000000001BL});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x400000000020A070L,0x000000000001183FL});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x000000000000A000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x000000007C000000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000003E30800L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x4000000000202070L,0x000000000001183FL});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000003E20800L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000003820800L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000003800800L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x7F403FFF80001010L,0x000000000000201BL});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x00000000001D8002L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x4000000000000010L,0x000000000000001BL});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000000000198002L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x0000000000188002L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x0000000080100000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x4000000000000010L,0x000000000000201BL});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0037C000000C0000L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0080000000004000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x7E40000400000010L,0x000000000000201BL});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x7E40000400104010L,0x000000000000201BL});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x4040000000000010L,0x000000000000201BL});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x4000000000004010L,0x000000000000201BL});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x2000000400000000L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x8000000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0000000000000000L,0x000000000000001CL});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x4000000000000010L,0x000000000000203BL});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x4000000000004010L,0x000000000000203BL});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0037C00000040002L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0080000000000002L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x000C000000040002L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0004000000040002L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001800L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000000002L,0x000000000000E000L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000000002L,0x0000000000060000L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000000000104000L});

}