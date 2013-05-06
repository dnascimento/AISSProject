#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "protocol.h"

//char init(u32 mode);
JNIEXPORT jchar JNICALL Java_AesBox_init (JNIEnv * env, jobject obj, jint mode){
	u32 modo = (u32) mode;
	printf("Init mode: %d\n",modo);

	//return init(modo)
	char result = '0';
	return result;
}

/*
 * Class:     AesBox
 * Method:    update
 * Signature: ([BI[BI)C
 */
//char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out);
JNIEXPORT jchar JNICALL Java_AesBox_update (JNIEnv * env, jobject obj, jbyteArray  jdata_in, jint jsize_in,jbyteArray jdata_out){
	//Read data input
	char * data_in =  (*env)->GetByteArrayElements(env,jdata_in,NULL);
	//Read data input size
	u32 size_in = (u32) jsize_in;

	u8* data_out;
	u32 size_out;

	//Output parameters on object
	 jclass classAesBox = (*env)->GetObjectClass(env, obj);
	 //Get reference
	 jfieldID obj_size_out = (*env)->GetFieldID(env, classAesBox, "size_out", "I");

	//char result = update(data_in, size_in,data_out,&size_out);
	//(*env)->SetIntField(env, obj, obj_size_out, size_out);
	//(*env)->SetByteArrayRegion(env, jdata_out, 0,size_out,data_out);

	 (*env)->SetIntField(env, obj, obj_size_out, 69);
	 (*env)->SetByteArrayRegion(env, jdata_out, 0,jsize_in,data_in);
	 return 'b';
}


/*
 * Class:     AesBox
 * Method:    doFinal
 * Signature: ([BI[B)C
 */
JNIEXPORT jchar JNICALL Java_AesBox_doFinal (JNIEnv * env, jobject obj, jbyteArray jdata_in, jint jsize_in, jbyteArray jdata_out){
	//Read data input
	char * data_in =  (*env)->GetByteArrayElements(env,jdata_in,NULL);
	//Read data input size
	u32 size_in = (u32) jsize_in;


	u8* data_out = (u8*) jdata_out;
	u32 size_out;

	//Output parameters on object
	 jclass classAesBox = (*env)->GetObjectClass(env, obj);
	 //Get reference
	 jfieldID obj_size_out = (*env)->GetFieldID(env, classAesBox, "size_out", "I");


	//char result = doFinal(data_in,size_in,data_out,&size_out);
	//(*env)->SetIntField(env, obj, obj_size_out, size_out);
	//(*env)->SetByteArrayRegion(env, jdata_out, 0,size_out,data_out);
	 //return result;

	 (*env)->SetIntField(env, obj, obj_size_out, 69);
	 (*env)->SetByteArrayRegion(env, jdata_out, 0,jsize_in,data_in);
	 return 'b';
}


