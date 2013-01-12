QMAKE = {{qmake}}

OUTDIR = build

$(OUTDIR)/Makefile:
	@mkdir -p $(OUTDIR)
	$(QMAKE) -o $(OUTDIR)/Makefile {{baseName}}.pro 

all:	$(OUTDIR)/Makefile
	$(MAKE) -C $(OUTDIR)

clean:
	rm -fr $(OUTDIR)
