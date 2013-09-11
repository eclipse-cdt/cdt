
PRO   = {{baseName}}.pro
QMAKE = {{qmake.Qt4}}

all:   debug release

clean: clean-debug clean-release

build-debug/Makefile: $(PRO)
	@mkdir -p $(dir $@)
	$(QMAKE) -o $@ $(PRO) CONFIG+=debug

debug: build-debug/Makefile
	$(MAKE) -wC build-debug all

clean-debug:
	rm -fr build-debug

build-release/Makefile: $(PRO)
	@mkdir -p $(dir $@)
	$(QMAKE) -o $@ $(PRO) CONFIG+=release 

release: build-release/Makefile
	$(MAKE) -wC build-release

clean-release:
	rm -fr build-release

.PHONY: all clean debug clean-debug release clean-release
