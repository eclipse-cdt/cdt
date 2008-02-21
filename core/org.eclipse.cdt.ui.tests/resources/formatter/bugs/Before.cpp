// https://bugs.eclipse.org/bugs/show_bug.cgi?id=169382
struct x {};
struct x getX() {}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171520
int bug=sizeof(int);
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=173837
class ABaseClass {protected:ABaseClass(int x);};
class AClass : public ABaseClass {AClass(int x) throw(int); void test1() const throw(int); void test2() throw();};
AClass::AClass(int x)throw(int):ABaseClass(x){for (int i=0;i < 12;i++) {}}
// keep space between decl spec and declarator
int
main(int argc,char **argv) {}
// handling of string concat
char* s1= "this "   "is "  "one ""string.";
char* s2= "this " "is " 
"one " "string.";
// macro definition with line comment
#define ID(x) x  // identity
int main(){return ID(0);}
// semicolons inside for
void g(){for(int i=0;i<10;++i){}}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183220
void bug183220()
{
	int rtc_hdw_cr_sync_next,rtc_hdw_cr_sync,rtc_hdw_cr_resync_enable,rtc_s2000_src_pending,rtc_s2000_cr_sync_pending,rtc_hdw_cr_sync_next,rtc_hdw_current_clock;
	int rtc_s2000_clock_source_state,RTC_CLOCK_PLL;
	if (( ( rtc_hdw_cr_sync_next != rtc_hdw_cr_sync ) || rtc_hdw_cr_resync_enable )&& !rtc_s2000_src_pending && !rtc_s2000_cr_sync_pending) { if (!identify_hdw_fvr_master() || !rtc_hdw_current_clock->external || !rtc_hdw_cr_sync_next ||( ( rtc_hdw_current_clock->external && rtc_hdw_cr_sync_next && rtc_s2000_clock_source_state != RTC_CLOCK_PLL ) )) {
		}
	}
}
// declaration with array initializer
long dummy[]= { 100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,
	100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,
	100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,
	};
