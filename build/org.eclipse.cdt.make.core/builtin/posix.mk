MAKE = make
AR = ar
ARFLAGS = -rv
YACC = yacc
YFLAGS = 
LEX = lex
LFLAGS = 
LDFLAGS = 
CC = c89
CFLAGS = -O
FC = fort77
FFLAGS = -O 1
GET = get
GFLAGS = 
SCCSFLAGS = 
SCCSGETFLAGS = -s

.SUFFIXES: .o .c .y .l .a .sh .f .c~ .y~ .l~ .sh~ .f~

.SCCS_GET:
	sccs $(SCCSFLAGS) get $(SCCSGETFLAGS) $@
.c:
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<
.f:
	$(FC) $(FFLAGS) $(LDFLAGS) -o $@ $<
.sh:
	cp $< $@
	chmod a+x $@
.c~:
	$(GET) $(GFLAGS) -p $< > $*.c
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $*.c
.f~:
	$(GET) $(GFLAGS) -p $< > $*.f
	$(FC) $(FFLAGS) $(LDFLAGS) -o $@ $*.f
.sh~:
	$(GET) $(GFLAGS) -p $< > $*.sh
	cp $*.sh $@
	chmod a+x $@
.c.o:
	$(CC) $(CFLAGS) -c $<
.f.o:
	$(FC) $(FFLAGS) -c $<
.y.o:
	$(YACC) $(YFLAGS) $<
	$(CC) $(CFLAGS) -c y.tab.c
	rm -f y.tab.c
	mv y.tab.o $@
.l.o:
	$(LEX) $(LFLAGS) $<
	$(CC) $(CFLAGS) -c lex.yy.c
	rm -f lex.yy.c
	mv lex.yy.o $@
.y.c:
	$(YACC) $(YFLAGS) $<
	mv y.tab.c $@
.l.c:
	$(LEX) $(LFLAGS) $<
	mv lex.yy.c $@
.c~.o:
	$(GET) $(GFLAGS) -p $< > $*.c
	$(CC) $(CFLAGS) -c $*.c
.f~.o:
	$(GET) $(GFLAGS) -p $< > $*.f
	$(FC) $(FFLAGS) -c $*.f
.y~.o:
	$(GET) $(GFLAGS) -p $< > $*.y
	$(YACC) $(YFLAGS) $*.y
	$(CC) $(CFLAGS) -c y.tab.c
	rm -f y.tab.c
	mv y.tab.o $@
.l~.o:
	$(GET) $(GFLAGS) -p $< > $*.l
	$(LEX) $(LFLAGS) $*.l
	$(CC) $(CFLAGS) -c lex.yy.c
	rm -f lex.yy.c
	mv lex.yy.o $@
.y~.c:
	$(GET) $(GFLAGS) -p $< > $*.y
	$(YACC) $(YFLAGS) $*.y
	mv y.tab.c $@
.l~.c:
	$(GET) $(GFLAGS) -p $< > $*.l
	$(LEX) $(LFLAGS) $*.l
	mv lex.yy.c $@
.c.a:
	$(CC) -c $(CFLAGS) $<
	$(AR) $(ARFLAGS) $@ $*.o
	rm -f $*.o
.f.a:
	$(FC) -c $(FFLAGS) $<
	$(AR) $(ARFLAGS) $@ $*.o
	rm -f $*.o
