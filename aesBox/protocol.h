#ifndef PROTOCOL_H
#define PROTOCOL_H

#include <string.h>

#define RESET_CODE 		0
#define INIT_CODE  		1
#define INIT_CODE2  		9
#define UPDATE_CODE		2
#define DOFINAL_CODE 		3
#define DATA_CONT_CODE 		5
#define DATA_COMP_CODE  	6
#define STATUS_CODE		7
#define STATUS_OK_CODE		8
#define ERROR_CODE 		-1


#define AES_BLOCK_SIZE 32 /* 256 bit AES */
#define MIN_BLOCK_NUM  20 /* 640 byte  : minimum length of outgoing packet */
#define MAX_BLOCK_NUM  44//44 /* 1408 byte : maximum length of outgoing packet*/

#define MIN_PACKET_DATA (MIN_BLOCK_NUM*AES_BLOCK_SIZE)
#define MAX_PACKET_DATA (MAX_BLOCK_NUM*AES_BLOCK_SIZE) /* 1514 - 4 (packet_t header) - 42 (ip/udp header) */
#define MAX_PACKET_RAW_DATA (PACKET_HEADER_SIZE+MAX_PACKET_DATA+AES_BLOCK_SIZE)
#define MAX_DATA_IN  (MAX_PACKET_DATA*50)
#define MAX_DATA_OUT (MAX_DATA_IN + AES_BLOCK_SIZE)

#define ENDIANNESS LITTLE_ENDIAN

#define PACKET_HEADER_SIZE 6


#define KEY_SIZE 	32
#define IV_SIZE 	32
#define VERSION_SIZE 	4
#define MODE_SIZE 	4
#define INIT_SIZE  (KEY_SIZE+IV_SIZE+VERSION_SIZE+MODE_SIZE)


#define INT_KEYS 6

#define KeyInMessageSIZE 4 //in bytes

/* flag mode constants*/
#define EBC_FLAG 	 0x00000000
#define CBC_FLAG 	 0x00000200
#define FIRST_FLAG 	 0x00000400

#define ENCRYPT_FLAG    0x00000000
#define DECRYPT_FLAG    0x00000100
#define ENCRYPTION_MASK 0x00000100

#define UNSEALED_MODE   0x00001000
#define SEALED_MODE     0x00001000
#define SEALED_MASK     0x00001000

#define ROUNDS_10	 0x0A
#define ROUNDS_12	 0x0C
#define ROUNDS_14	 0x0D
/**/

typedef unsigned char	u8;
typedef unsigned short	u16;
typedef unsigned int	u32;


/** @brief Initialization function

	This function initiates a new transfer from the PC to the board, indicating the
	necessary parameters to control the FPGA processing.

	@param version Application version number identifier
	@param mode AES Core processing mode

	@return Returning code from the acknowledge packet
*/
char init(u32 mode);


/** @brief Update internal function

	This function is redirected to the complete Update function.
	It calls the Update function with fin_code = UPDATE_CODE, bypassing
	all the other parameters.

	@param data_in Input data
	@param size Input data length
	@param data_out Output data buffer
	@param size_out Output data length (return parameter)

	@return Returning code from the last acknowledge + reply packet
*/
char  update(u8 * data_in, u32 size, u8 * data_out,u32 * size_out);


/** @brief Dofinal function with no input data

	This function is the last one to be called when cyphering a given
	file or stream of data. It will first send the remaining buffered
	data, and then ask for the cyphered data (padded if needed).

	@param data_out Output data buffer
	@param size_out Output data length (return parameter)

	@return Returning code from the last acknowledge + reply packet
*/
//char doFinal(u8 * data_out,u32 *size_out);


/** @brief Dofinal function with a last chunk of input data

	This function is the last one to be called when cyphering a given
	file or stream of data. It will first update the last block of input
	data and then will call the other version of the Dofinal function,
	with no input data.

	@param data_in Input data
	@param size Input data length
	@param data_out Output data buffer
	@param size_out Output data length (return parameter)

	@return Returning code from the last acknowledge + reply packet
*/
char  doFinal(u8 * data_in, u32 size,u8 * data_out,u32 *size_out);


/** @brief Get Data from a formatted packet

	This function will read the header info of the packet and write
	the packet data into the buffer and the valid number of bytes
	received into the size variable. Finally, it returns the code
	indicating the type of the packet.

	@param p packet as byte array
	@param data buffer
	@param size buffer length

	@return  Code of the packet
*/
char get_packet_data(packet_t * p, u8 * data, u32 *size);

 #endif

