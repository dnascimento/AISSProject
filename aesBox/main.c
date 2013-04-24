#include "util.h"
#include "com.h"
#include "protocol.h"
#include <stdio.h>

#define ENC_MODE 'c'
#define DEC_MODE 'd'

#define N 2000
#define INC 20

#define MODE FILE


int main(int argc, char*argv[])
{
	char src_file[100],c;
	char dest_file[100];

	FILE * fp_r,*fp_w;
	int sz;
	char com;
	double time,bsent;


	u32 mode;
	u32 version=1;
	u32 data_len,n;
	u32 inc = 20;

	u8 *data_in;
	u8 buffer_in[MAX_DATA_IN];
	u8 buffer_out[MAX_DATA_OUT];
	u32 i,m,j;
		
		mode = ROUNDS_10 | ECB_FLAG |FIRST_FLAG| ENCRYPT_FLAG;
		mode = ROUNDS_10 | ECB_FLAG |FIRST_FLAG | DECRYPT_FLAG;

		strcpy(src_file, "infile");
		strcpy(dest_file, "outfile");

		fp_r = fopen(src_file,"rb");
		fp_w = fopen(dest_file,"wb");

		/* Initialization*/

		init( mode );

	 	 /* cipher/decipher */

		while( (data_len = fread(buffer_in,1,MAX_DATA_IN,fp_r))>0){
			if(data_len == ERROR_CODE){
				printf("\n Error : fread() \n");
				return -1;
			}

			if(update(buffer_in,data_len,buffer_out,&n)== ERROR_CODE){
				printf("\n Error : Update() \n");
				return -1;
			}

			fwrite(buffer_out,1,n,fp_w);
		}


		if(doFinal(buffer_out,&n) == ERROR_CODE){
			printf("\n Error : Update() \n");
			return -1;
		}

		if(n>0){ fwrite(buffer_out,1,n,fp_w);}

		fclose(fp_r);
		fclose(fp_w);

}
