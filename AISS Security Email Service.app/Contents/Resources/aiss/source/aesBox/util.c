#include "util.h"

int InsPadding(unsigned char barr[EPLENGTH],int n)
{
	int i, extra;

	extra =  (n%32 == 0) ?  32 : 32-n%32 ;
	
	for(i = n ; i < (n+extra) ; i++)
		barr[i] = extra;

	return extra;
}

int RemPadding(unsigned char barr[EPLENGTH],int n)
{
	int aux = barr[n-1];
	return aux;
}