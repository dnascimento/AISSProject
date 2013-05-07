#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "util.h"
#include "com.h"
#include "protocol.h"
#include "AesBox.h"


#define LINEMAX 10
#define SHOW 1
#define HIDE 0
#define USB_MODE 'u'
#define ETH_MODE 'e'

#define ENC_MODE 'c'
#define DEC_MODE 'd'

#define N 2000
#define INC 20

#define MODE FILE
packet_t packet;


//char init(u32 mode);
JNIEXPORT jchar JNICALL Java_aiss_AesBox_init (JNIEnv * env, jobject obj, jint jmode){
	u32 mode = (u32) jmode;
	if(mode == 0){
		//Encrypt
		mode = ROUNDS_10 | CBC_FLAG |FIRST_FLAG| ENCRYPT_FLAG;
	}else{
		mode = ROUNDS_10 | CBC_FLAG |FIRST_FLAG | DECRYPT_FLAG;
	}

	printf("Init mode: %d\n",mode);
    
	char result =  init(mode);
	printf("Init done");
	return result;
}

/*
 * Class:     AesBox
 * Method:    update
 * Signature: ([BI[BI)C
 */
//char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out);
JNIEXPORT jchar JNICALL Java_aiss_AesBox_update (JNIEnv * env, jobject obj, jbyteArray  jdata_in, jbyteArray jdata_out){
	//Read data input
	char * data_in =  (*env)->GetByteArrayElements(env,jdata_in,NULL);
	//Read data input size
	u32 size_in = (u32) sizeof(data_in);

	u8* data_out;
	u32 size_out;

	//Output parameters on object
	 jclass classAesBox = (*env)->GetObjectClass(env, obj);
	 //Get reference
	 jfieldID obj_size_out = (*env)->GetFieldID(env, classAesBox, "size_out", "I");

    
    
	char result = update(data_in, size_in,data_out,&size_out);
	(*env)->SetIntField(env, obj, obj_size_out, size_out);
	(*env)->SetByteArrayRegion(env, jdata_out, 0,size_out,data_out);
    return result;
//	 (*env)->SetIntField(env, obj, obj_size_out, 69);
//	 (*env)->SetByteArrayRegion(env, jdata_out, 0,jsize_in,data_in);
//	 return 'b';
}


/*
 * Class:     AesBox
 * Method:    doFinal
 * Signature: ([BI[B)C
 */
JNIEXPORT jchar JNICALL Java_aiss_AesBox_doFinal (JNIEnv * env, jobject obj, jbyteArray jdata_in, jbyteArray jdata_out){
	printf("\n Do final start\n");

	//Read data input
	char * data_in =  (*env)->GetByteArrayElements(env,jdata_in,NULL);
	//Read data input size
	u32 size_in = (u32) sizeof(data_in);


	u8* data_out = (u8*) jdata_out;
	u32 size_out;

	//Output parameters on object
	 jclass classAesBox = (*env)->GetObjectClass(env, obj);
	 //Get reference
	 jfieldID obj_size_out = (*env)->GetFieldID(env, classAesBox, "size_out", "I");


	char result = doFinal(data_in,size_in,data_out,&size_out);
	(*env)->SetIntField(env, obj, obj_size_out, size_out);
	(*env)->SetByteArrayRegion(env, jdata_out, 0,size_out,data_out);

	printf("\n Do final end \n");

	 return result;

	 //(*env)->SetIntField(env, obj, obj_size_out, 69);
	 //(*env)->SetByteArrayRegion(env, jdata_out, 0,jsize_in,data_in);
	 //return 'b';
}


