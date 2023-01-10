#include "../include/StompClient.h"

int main(int argc, char *argv[]) {

	const AverageKeyboardEnjoyer ake;
	//AverageSocketEnjoyer ase();
	std::thread inputThread(&AverageKeyboardEnjoyer::Run, &ake);
	//std::thread outputThread(&AverageSocketEnjoyer::Run, &ase);

	if (inputThread.joinable())
		inputThread.join();
	//if (outputThread.joinable())
		//outputThread.join();
	return 0;
}