#ifndef UTIL_H
#define UTIL_H


#include <stdio.h>
#include "ethcom.h"
#include <stdlib.h>     /* for exit() */
#include <string.h>
//#include "winsock.h"    /* for socket(),... */
//#include <Windows.h>
//#include "usbcom.h"

int InsPadding(unsigned char barr[EPLENGTH],int n);
int RemPadding(unsigned char barr[EPLENGTH],int n);


#endif
