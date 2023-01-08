#include "StompClient.h"

int main(int argc, char *argv[]) {
	//TODO: check if this is proper.
	if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
	
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
	//UP UNTIL HERE

	auto inputCallable = [](ConnectionHandler& connectionHandler) {
		while (true) {
			const int bufferSize = 1024;
			char buffer[bufferSize];
			std::cin.getline(buffer,bufferSize);
			string line(buffer);
			int commandLen = line.length() + 1;
			if (!connectionHandler.sendLine(line)) {
            	std::cout << "Disconnected. Exiting...\n" << std::endl;
            	break;
        	}
			std::cout << "Sent " << commandLen << " bytes to server" << std::endl;
			//insert string split here to get first word
			int numberOfSpaces = 0;
			for (int i = 0; i < line.length(); i++)
				if (line.at(i) == ' ')
					numberOfSpaces++;
			size_t pos = 0;
			string words[numberOfSpaces+1];
			int wordInd = 0;
			while ((pos = line.find(' ') != string::npos)) {
				words[wordInd] = line.substr(0,pos);
				line.erase(0,pos + 1);
				wordInd++;
			}
			words[wordInd] = line;
			string frame;
			if (words[0] == "logout") {
				frame = StompClient::buildFrameDisconnect();
			}
			if (words[0] == "login") {
				frame = StompClient::buildFrameConnect(words[2],words[3]);
			}
			if (words[0] == "join") {
				frame = StompClient::buildFrameSubscribe(words[1]);
			}
			if (words[0] == "exit") {
				frame = StompClient::buildFrameUnsubscribe(words[1]);
			}
			if (words[0] == "report") {
				frame = StompClient::buildFrameSend();
			}
			if (words[0] == "summary") {
				StompClient::generateSummary();
			}
		}
	};

	auto outputCallable = [](ConnectionHandler& connectionHandler) {
		while (true) {
			string answer;
			if (!connectionHandler.getLine(answer)) {
            	std::cout << "Disconnected. Exiting...\n" << std::endl;
            	break;
        	}
			int ansLen = answer.length();
			
		}
	};

	std::thread inputThread(inputCallable);
	std::thread outputThread(outputCallable);

	if (inputThread.joinable())
		inputThread.join();
	if (outputThread.joinable())
		outputThread.join();
	return 0;
}

string StompClient::buildFrameConnect(string username, string password)
{
	const string hostName = "stomp.cs.bgu.ac.il";
	const string stompVersion = "1.2";
	string frame;
	frame = "CONNECT\naccept-version:" + 
			stompVersion + 
			"\nhost:" + 
			hostName + 
			"\nlogin:" + 
			username + 
			"\npasscode:" + 
			password + 
			"\n\n^@";
    return frame;
}

//TODO: might need to move all following methods to StompProtocol class
string StompClient::buildFrameDisconnect()
{
	return "DISCONNECT\nreceipt:" + std::to_string(sub_id_counter) + "\n\n^@";
}

string StompClient::buildFrameSubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		sub_id = addToList(game_name);
	return "SUBSCRIBE\ndestination:/" + game_name + "\nid:" + std::to_string(sub_id) + "\n\n^@";
} //TODO might need to add the receipt header even tho it's not 'needed' perse

string StompClient::buildFrameUnsubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		return ""; //TODO this needs to be an error or smth
    return "UNSUBSCRIBE\nid:" + std::to_string(sub_id) + "\n\n^@";
} //TODO might need to add the receipt header even tho it's not 'needed' perse

string StompClient::buildFrameSend()
{
    return string();
}

void StompClient::generateSummary()
{

}

int StompClient::idOf(string game_name)
{
    for (int i = 0; i < StompClient::gameName_to_Id.size(); i++) {
		if (gameName_to_Id.at(i).first == game_name)
			return gameName_to_Id.at(i).second;
	}
	return -1;
}

string StompClient::nameOf(int subscription_id)
{
    for (int i = 0; i < StompClient::gameName_to_Id.size(); i++) {
		if (gameName_to_Id.at(i).second == subscription_id)
			return gameName_to_Id.at(i).first;
	}
	return "No habla Español"; //TODO change this eventually
}

int StompClient::addToList(string game_name)
{
    gameName_to_Id.push_back(std::pair<string,int>(game_name,sub_id_counter));
	return sub_id_counter++;
}