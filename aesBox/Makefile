all: 
	gcc -x c -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers -c aesbox.c -o aesbox.o
	gcc -dynamiclib -o libaesbox.jnilib aesbox.o
	cp libaesbox.jnilib ../bin/libaesbox.jnilib
	#gcc -shared -o libaesbox.jnilib  -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers AesBox.c -lc
	
	

