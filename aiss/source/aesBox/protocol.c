#include "protocol.h"
#include "com.h"
#include <stdio.h>

extern packet_t packet;

#define DEBUG OFF


void reset( )
{
	u8 buffer[MAX_PACKET_DATA];
	u32 size;

	/* Create reset packet */
 	form_packet(&packet, NULL,0, RESET_CODE);
	/* Send reset packet */
	sendPacket(packet.raw,PACKET_HEADER_SIZE);

}

char init( u32 mode)
{
	printf("PROTOCOL:Init: %d \n",mode);
	u8 buffer[MAX_PACKET_DATA];
	u32 size;

	COMinit();
	/* Create initialization packet */
	form_packet_init(&packet, mode, INIT_CODE);
	/* Send initialization packet */
	sendPacket(packet.raw,PACKET_HEADER_SIZE+INIT_SIZE);
	/* Receive status confirmation */
	recvPacket((u8 *) packet.raw, MAX_PACKET_RAW_DATA);
	/* Get data from incoming packet */
	return get_packet_data( &packet, buffer, &size);

}

char  update_int(u8 * data_in, u32 size, u8 * data_out,u32 * rbytes, char fin_code)
{
	printf("->a");
	static u32 bbytes = 0;
	static u8 buffer[MAX_PACKET_RAW_DATA];
	u32 size_int,n,i;
	u32 sbytes;
	char ret_code;

	ret_code = 0;
	size_int = size;
	sbytes = 0;
	*rbytes = 0;

	while(size_int>0)
	{
		printf("->b");

		if( (bbytes + size_int) >= MAX_PACKET_DATA)
		{
			/* Fill the buffer until MAX_DATA */
			memcpy( &(buffer[bbytes]), &(data_in[sbytes]), MAX_PACKET_DATA - bbytes );
			printf("->c");

			/* Update input stream index and length*/
			sbytes   += (MAX_PACKET_DATA - bbytes);
			size_int -= (MAX_PACKET_DATA - bbytes);

			/* Update number of buffered bytes*/
			bbytes = MAX_PACKET_DATA;
			printf("->d");

			/* Create packet */
			if(size_int == 0) /* if it's the last packet to be sent*/
				form_packet(&packet,buffer, bbytes,fin_code);
			else			/* if there is still more data to be sent*/
				form_packet(&packet,buffer, bbytes,UPDATE_CODE);

			/* Send buffered data */
			sendPacket((u8 *) packet.raw, bbytes + PACKET_HEADER_SIZE);

			/* Receive data */
			n = recvPacket((u8 *) packet.raw, MAX_PACKET_RAW_DATA);

			if(n == -1)
				return ERROR_CODE;

			//for(i=0;i<n;i++)
			//	printf("%02X",packet.raw[i]);

			/* Get data from incoming packet */
			ret_code = get_packet_data( &packet, &(data_out[*rbytes]),&n );
			printf("->d");


			//	for(i=0;i<n;i++)
			//	printf("%02X",data_out[i]);

			/* STOP if an error is detected*/
			if(ret_code == ERROR_CODE)
				return ERROR_CODE;

			/* Update received bytes counter*/
			*rbytes += n;

			/* Update buffered bytes counter*/
			bbytes = 0;
		}
		else
		{
			printf("->e");

			/* Copy the input data to the buffer */
			memcpy( &(buffer[bbytes]), &(data_in[sbytes]), size_int );


			/* Update number of buffered bytes*/
			bbytes += size_int;

			/* Update input stream index and length*/
			sbytes += size_int;
			size_int = 0;
			printf("->f");


			/* If number of buffered bytes is > than the minimum packet data*/
			if( bbytes >= MIN_PACKET_DATA )
			{
				/* Create packet */
				form_packet(&packet,buffer, bbytes, fin_code);

				printf("->g");

				/* Send buffered data */
				sendPacket((u8 *) packet.raw, bbytes + PACKET_HEADER_SIZE);

				/* Receive data */
				recvPacket((u8 *) packet.raw, MAX_PACKET_RAW_DATA);

				/* Get data from incoming packet */
				ret_code = get_packet_data( &packet, &(data_out[*rbytes]),&n );

				/* STOP if an error is detected*/
				if(ret_code == ERROR_CODE)
					return ERROR_CODE;
				/* Update received bytes counter*/
				*rbytes += n;

				/* Update buffered bytes counter*/
				bbytes = 0;
			}
			printf("->g");

		}
	}

	/* If it is a DoFinal and there is still buffered data on the PC
	 !OR! on the FPGA, ask for the last (padded) block*/
	if( (fin_code == DOFINAL_CODE) && ( (bbytes >0) || (ret_code != DATA_COMP_CODE) ) )
	{

			/* Create packet - emptying the buffer */
			form_packet(&packet,buffer, bbytes, DOFINAL_CODE);

			/* Send all the buffered data */
			sendPacket((u8 *) packet.raw, bbytes + PACKET_HEADER_SIZE);

			/* Receive last packet */
			recvPacket((u8 *) packet.raw, MAX_PACKET_RAW_DATA);

			/* Get data from incoming packet */
			ret_code = get_packet_data( &packet, &(data_out[*rbytes]),&n );

			/* HALT if an error is detected*/
			if(ret_code == ERROR_CODE)
				return ERROR_CODE;
			/* Update received bytes counter*/
			*rbytes += n;

			/* Update buffered bytes counter*/
			bbytes = 0;

	}

		/* Return code*/
		return ret_code;

}


char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out)
{
	printf("PROTOCOL:update\n");

	return update_int(data_in, size,data_out, size_out,UPDATE_CODE);
}

char doFinal_small(u8 * data_out,u32 *size_out)
{
	printf("PROTOCOL:small\n");

	u32 n;
	char ret_code;

	/* Clear the update function internal buffer */
	ret_code  = update_int(NULL,0,data_out,&n,DOFINAL_CODE);
	/* Return number of received bytes*/
	*size_out = n;
	/* Return code*/
	return ret_code;
}

char   doFinal(u8 * data_in, u32 size,u8 * data_out,u32 *size_out)
{
	printf("PROTOCOL:doFinal\n");

	u32 n;
	char ret_code;

	/* Update the last block of the stream */
	ret_code  = update(data_in,size,data_out,&n);

	/* STOP if an error is detected*/
	if(ret_code == ERROR_CODE)
		return ERROR_CODE;

	/* update number of received bytes*/
	*size_out = n;

	/* Final transfer, ask for the last block (padded if needed)*/
	ret_code = doFinal_small(&(data_out[n]),&n);
	/* update number of received bytes*/
	*size_out += n;
	/* Return code*/
	return ret_code;
}


 char get_packet_data(packet_t * p, u8 * data, u32 *size)
{
	u32 aux = 0 ;

	memcpy(&aux, p->field.size,4) ;


	#if DEBUG == ON
	//printf("DEBUG: Received packet with %d bytes\n",aux);
	#endif
	*size = aux;

	if(*size >0)
		memcpy(data,(u8*)(p->field.data),*size);

	return p->field.code_op[0] ;
}


 void form_packet_init(packet_t * p,u32 mode, char code)
{

	int size = INIT_SIZE;
    u8 IV[32]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	u32 version = 0x1;

	memcpy(p->param.version, &version,4);
	memcpy(p->param.mode,&mode,4);
	memcpy(p->param.IV,IV,IV_SIZE);

	p->param.code_op[0] = code;
	p->param.code_op[1] = code;

	memcpy(p->field.size,&size,4);


	#if DEBUG == ON
	//printf("DEBUG: Formatted packet with %d bytes\n",INIT_SIZE);
	#endif
}

 void form_packet(packet_t * p, u8 * data, u32 size, char  code)
{
	if(data!=NULL)
		memcpy((u8*)p->field.data,data,size);

	p->field.code_op[0] = code;
	p->field.code_op[1] = code;

	#if DEBUG == ON
	//printf("DEBUG: Formatted packet with %d bytes\n",size);
	#endif


		memcpy(p->field.size,&size,4);


}

 void form_packet_header(packet_t * p, u32 size,char code)
{
	p->field.code_op[0] = code;
	p->field.code_op[1] = code;

	memcpy(p->field.size,&size,4);



}
