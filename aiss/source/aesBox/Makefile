CC = gcc
DEPS = com.h  ethcom.h protocol.h util.h

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

all: makeJNI

run: com.c ethcom.c protocol.c main.c util.c
	gcc -o boxRun com.c ethcom.c protocol.c main.c util.c



makeJNI: aesbox.c
	gcc -x c -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers -c aesbox.c com.c ethcom.c protocol.c util.c
	gcc -dynamiclib -o libaesbox.jnilib aesbox.o com.o ethcom.o protocol.o util.o
	cp libaesbox.jnilib ../bin/libaesbox.jnilib
	cp libaesbox.jnilib ../libaesbox.jnilib	


clean:
	rm -rf *.o
