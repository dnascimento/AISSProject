all: 
	gcc -x c -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers -c AesBox.c -o AesBox.o
	gcc -dynamiclib -o libAesBox.jnilib AesBox.o
	#gcc -shared -o libaesbox.jnilib  -I /System/Library/Frameworks/JavaVM.framework/Versions/A/Headers AesBox.c -lc
	
	

