#include "../include/StompClient.h"

int main(int argc, char *argv[]) {
	auto inputCallable = []() {
		while (true) {
			const int bufferSize = 1024;
			char buffer[bufferSize];
			std::cin.getline(buffer,bufferSize);
			string line(buffer);
			//int commandLen = line.length() + 1;

			//string splitting starts here
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
			//string splitting ends here

			string frame;
			std::vector<string> frames;

			bool isConnected = StompClient::ch.isConnected();
			if (words[0] == "logout" && isConnected) {
				frame = StompClient::buildFrameDisconnect();
				StompClient::ch.sendLine(frame);
			}
			else if (words[0] == "login" && !isConnected) {
				frame = StompClient::buildFrameConnect(words[2],words[3]);
				string host;
				short port;
				if (std::size_t delimeter = words[1].find(':') != string::npos) {
					host = words[1].substr(0,delimeter);
					words[1].erase(0, delimeter + 1);
					port = stoi(words[1]);
				}
				std::cout << "Attempting log in sequence." << std::endl;
				StompClient::ch.initConnection(host,port);
				StompClient::ch.connect();
				StompClient::ch.setUsername(words[2]);
				StompClient::ch.sendLine(frame);
			}
			else if (words[0] == "join" && isConnected) {
				frame = StompClient::buildFrameSubscribe(words[1]);
				StompClient::ch.sendLine(frame);
			}
			else if (words[0] == "exit" && isConnected) {
				frame = StompClient::buildFrameUnsubscribe(words[1]);
				StompClient::ch.sendLine(frame);
			}
			else if (words[0] == "report" && isConnected) {
				frames = StompClient::buildFramesSend(words[1]);
				while(!frames.empty()) {
					frame = frames.at(frames.size()-1);
					frames.pop_back();
					StompClient::ch.sendLine(frame);
				}

			}
			else if (words[0] == "summary" && isConnected) {
				StompClient::generateSummary();
			}
			else if (!isConnected && (words[0] == "logout" || words[0] == "join" || words[0] == "exit" || words[0] == "report" || words[0] == "summary")) {
				std::cout << "You've tried to use the " + words[0] + " command without logging in first.\nPlease log in." << std::endl;
			}
			else if (isConnected && words[0] == "login") {
				std::cout << "Only one user can log in at any time.";
			}
			else if (words[0] == "close_client") {
				std::cout << "Initiating self-destruction, in" << std::endl << "3" << std::endl;
				string line("idk how to log out yet");
				if (StompClient::ch.sendLine(line))
					StompClient::ch.close();
				sleep(1);
				std::cout << "2" << std::endl;
				sleep(1);
				std::cout << "1" << std::endl;
				sleep(1);
				break;
			}
			else {
				std::cout << "That's not a proper command" << std::endl;
				//TODO might need to throw an error or smth but seems unlikely
			}
			//TODO add sendLine if needed
		}
		//TODO add finishing code here if needed
		//TODO add sending the frame to the server
	};

	auto outputCallable = []() {
		while (true) {
			string answer;
			if (!StompClient::ch.getLine(answer)) {
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
			"\n\n\0";
    return frame;
}

//TODO: might need to move all following methods to StompProtocol class
string StompClient::buildFrameDisconnect()
{
	return "DISCONNECT\nreceipt:" + std::to_string(sub_id_counter) + "\n\n\0";
}

string StompClient::buildFrameSubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		sub_id = addToList(game_name);
	return "SUBSCRIBE\ndestination:/" + game_name + "\nid:" + std::to_string(sub_id) + "\n\n\0";
} //TODO might need to add the receipt header even tho it's not 'needed' perse

string StompClient::buildFrameUnsubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		return ""; //TODO this needs to be an error or smth
    return "UNSUBSCRIBE\nid:" + std::to_string(sub_id) + "\n\n\0";
} //TODO might need to add the receipt header even tho it's not 'needed' perse

std::vector<string> StompClient::buildFramesSend(string file_path)
{
	std::vector<string> output;
	names_and_events l = parseEventsFile(file_path);
	std::vector<Event> events = l.events;
	while (!events.empty()) {
		string currentFrame = "SEND\ndestination:/" + l.team_a_name + "_" + l.team_b_name + "\n\n";
		Event currEvent = events.at(events.size()-1);
		events.pop_back();
		std::map<string,string> genGameUp = currEvent.get_game_updates();
		std::map<string,string> teamAUp = currEvent.get_team_a_updates();
		std::map<string,string> teamBUp = currEvent.get_team_b_updates();
		currentFrame += "user: " + StompClient::ch.getUserName() + "\n";
		currentFrame += "team a: " + currEvent.get_team_a_name() + "\n";
		currentFrame += "team b: " + currEvent.get_team_b_name() + "\n";
		currentFrame += "event name: " + currEvent.get_name() + "\n";
		currentFrame += "time: " + std::to_string(currEvent.get_time()) + "\n";
		currentFrame += "general game updates: \n";
		for (auto& cggu : genGameUp) {
			currentFrame += "\t" + cggu.first + ": " + cggu.second + "\n";
		}
		for (auto& cggu : teamAUp) {
			currentFrame += "\t" + cggu.first + ": " + cggu.second + "\n";
		}
		for (auto& cggu : teamBUp) {
			currentFrame += "\t" + cggu.first + ": " + cggu.second + "\n";
		}
		currentFrame += "description:\n" + currEvent.get_discription() + "\n\0";

		output.push_back(currentFrame);
	}
    return output;
}

void StompClient::generateSummary()
{
	//TODO implement summary generation
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