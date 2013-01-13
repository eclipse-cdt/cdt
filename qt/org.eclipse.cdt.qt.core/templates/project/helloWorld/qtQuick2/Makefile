QMAKE = {{qmake}}

all:	debug release

clean: clean-debug clean-release

build-debug/Makefile:
	@mkdir -p $(dir $@)
	$(QMAKE) -o $@ clangtest.pro CONFIG+=debug 

debug:	build-debug/Makefile
	$(MAKE) -w -C build-debug

clean-debug:
	rm -fr build-debug

build-release/Makefile:
	@mkdir -p $(dir $@)
	$(QMAKE) -o $@ clangtest.pro CONFIG+=release 

release:	build-release/Makefile
	$(MAKE) -w -C build-release

clean-release:
	rm -fr build-release
