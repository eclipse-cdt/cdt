DIST = \
	dist/acorn/dist/acorn.js \
	dist/acorn/dist/acorn_loose.js \
	dist/acorn/dist/walk.js \
	dist/tern/lib/signal.js \
	dist/tern/lib/tern.js \
	dist/tern/lib/def.js \
	dist/tern/lib/comment.js \
	dist/tern/lib/infer.js

all:	$(DIST)

dist/%:	node_modules/%
	@mkdir -p $(dir $@)
	cp $^ $@

clean:
	rm -fr dist
