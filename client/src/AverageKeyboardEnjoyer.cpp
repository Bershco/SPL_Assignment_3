#include "../include/AverageKeyboardEnjoyer.h"
#include "../include/event.h"
#include <fstream>
#include <boost/algorithm/string.hpp>
#include <utility>
using namespace std;

AverageKeyboardEnjoyer::AverageKeyboardEnjoyer() : ch(), connectedProperly(), gameName_to_Id(), sub_id_counter()
{
	//TODO implement if needed
}
void AverageKeyboardEnjoyer::Run()
{
    while (true) {
			const int bufferSize = 1024;
			char buffer[bufferSize];
			std::cin.getline(buffer,bufferSize);
			string line(buffer);

			//string splitting starts here
			std::vector<string> words;
			boost::split(words,line,boost::is_any_of(" "));
			//string splitting ends here

			string frame;
			std::vector<string> frames;

			bool isConnected = ch.isConnected();

			if (words[0] == "logout" && isConnected) {
				frame = buildFrameDisconnect();
				ch.sendLine(frame);
				//TODO when the proper receipt is received change host to "Not Connected" and close connection handler
			}

			else if (words[0] == "login" && !isConnected) {
				frame = buildFrameConnect(words[2],words[3]);
				string host;
				short port;
				std::vector<string> split;
				boost::split(split,words[1],boost::is_any_of(":"));
				host = split[0];
				port = stoi(split[1]);
				std::cout << "Attempting log in sequence." << std::endl;
				ch.initConnection(host,port);
				ch.connect();
				ch.setUsername(words[2]);
				ch.sendLine(frame);
			}

			else if (words[0] == "join" && isConnected) {
				frame = buildFrameSubscribe(words[1]);
				ch.sendLine(frame);
			}

			else if (words[0] == "exit" && isConnected) {
				frame = buildFrameUnsubscribe(words[1]);
				ch.sendLine(frame);

			}
			else if (words[0] == "report" && isConnected) {
				frames = buildFramesSend(words[1]);
				while(!frames.empty()) {
					frame = frames.at(frames.size()-1);
					frames.pop_back();
					ch.sendLine(frame);
				}
			}

			else if (words[0] == "summary" && isConnected) {
				generateSummary(words[1],words[2],words[3]);
			}

			else if (!isConnected && (words[0] == "logout" || words[0] == "join" || words[0] == "exit" || words[0] == "report" || words[0] == "summary")) {
				std::cout << "You've tried to use the " + words[0] + " command without logging in first.\nPlease log in." << std::endl;
			}

			else if (isConnected && words[0] == "login") {
				std::cout << "Only one user can log in at any time.";
			}

			else if (words[0] == "close_client") {
				std::cout << "Initiating self-destruction, in\n3" << std::endl;
				string line("idk how to log out yet"); //TODO ceck if logged in, and if so - send a disconnect frame, and wait for 'disconnected'
				if (ch.sendLine(line))
					ch.close();
				if (isConnected) {
					frame = buildFrameDisconnect();
					ch.sendLine(frame);
					sleep(1);
					ch.close(); //this should be enough, not sure.
				}
				else {
					sleep(1);
				}
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

		}
		ch.close();
		//TODO add finishing code here if needed
		//TODO add sending the frame to the server
}

string AverageKeyboardEnjoyer::buildFrameConnect(string username, string password)
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
string AverageKeyboardEnjoyer::buildFrameDisconnect()
{
	return "DISCONNECT\nreceipt:" + std::to_string(sub_id_counter) + "\n\n\0";
}

string AverageKeyboardEnjoyer::buildFrameSubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		sub_id = addToList(game_name);
	return "SUBSCRIBE\ndestination:/" + game_name + "\nid:" + std::to_string(sub_id) + "\n\n\0";
} //TODO might need to add the receipt header even tho it's not 'needed' perse

string AverageKeyboardEnjoyer::buildFrameUnsubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		return ""; //TODO this needs to be an error or smth
    return "UNSUBSCRIBE\nid:" + std::to_string(sub_id) + "\n\n\0";
} //TODO might need to add the receipt header even tho it's not 'needed' perse

std::vector<string> AverageKeyboardEnjoyer::buildFramesSend(string file_path)
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
		currentFrame += "user: " + ch.getUserName() + "\n";
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

void AverageKeyboardEnjoyer::generateSummary(string game_name, string user, string file_path)
{
	//TODO implement summary generation
	std::queue<Event> events_reported_by_player = eventByUser[game_name];
	std::queue<Event> neededLater(events_reported_by_player); //Copying the queue for later use in this method
	std::map<string,string> general_stats;
	std::map<string,string> team_a_stats;
	std::map<string,string> team_b_stats;

	//The next lines are to insert all proper elements into all proper maps
	while (!events_reported_by_player.empty()) {
		Event e = events_reported_by_player.front(); //TODO might need to be something else
		events_reported_by_player.pop();
		std::map<string,string> gs_temp = e.get_game_updates();
		std::map<string,string> tas_temp = e.get_team_a_updates();
		std::map<string,string> tbs_temp = e.get_team_b_updates();

		//TODO These next lines are SUPPOSEDLY lexicographically sorting the map as the pairs are getting in it, need to check this is working
		std::map<string,string,compareLexicographically> gs;
		std::map<string,string,compareLexicographically> tas;
		std::map<string,string,compareLexicographically> tbs;
		for (auto& pair : gs_temp) {
			gs.insert(pair);
		}
		for (auto& pair : tas_temp) {
			tas.insert(pair);
		}
		for (auto& pair : tbs_temp) {
			tbs.insert(pair);
		}
		//CHECK UP UNTIL HERE, all else is different things than the one mentioned above
		//From here on out, gs, tas, and tbs should be lexicographically ordered.

		for (auto& update : gs) {
			auto it = general_stats.find(update.first);
			if (it != general_stats.end())
				general_stats[update.first] = update.second;
			else
				general_stats.insert(update);
		}
		for (auto& update : tas) {
			auto it = team_a_stats.find(update.first);
			if (it != team_a_stats.end())
				team_a_stats[update.first] = update.second;
			else
				team_a_stats.insert(update);
		}
		for (auto& update : tbs) {
			auto it = team_b_stats.find(update.first);
			if (it != team_b_stats.end())
				team_b_stats[update.first] = update.second;
			else
				team_b_stats.insert(update);
		}
	}


	std::vector<string> split;
	boost::split(split,game_name,boost::is_any_of("_"));
	string output(split[0] + " vs " + split[1] + "\nGame stats:\nGeneral stats:\n");
	for (auto& stat_pair : general_stats) {
		output += stat_pair.first + ": " + stat_pair.second + "\n";
	}
	output += "\n" + split[0] + " stats:\n";
	for (auto& stat_pair : team_a_stats) {
		output += stat_pair.first + ": " + stat_pair.second + "\n";
	}
	output += "\n" + split[1] + " stats:\n";
	for (auto& stat_pair : team_b_stats) {
		output += stat_pair.first + ": " + stat_pair.second + "\n";
	}
	output += "Game event reports:\n";
	while (!neededLater.empty()) {
		Event e = neededLater.front(); //TODO might need to be something else
		neededLater.pop();
		output += e.get_time() + " - " + e.get_name() + "\n\n";
		output += e.get_discription() + "\n\n\n";
	}

	//The next lines are for outputting the string created into the desired file
	std::ofstream outputFile;
	outputFile.open(file_path, std::ios::trunc); //This flag is to make sure the file always gets truncated (as in, we 'koti'im' the file, or just simply - overwriting it if it exists)
	//TODO make sure this file_path is an actual file path and that the ofstream::open method does what it's supposed to do
	outputFile << output << std::endl;
	outputFile.close();
}

int AverageKeyboardEnjoyer::idOf(string game_name)
{
    for (int i = 0; i < gameName_to_Id.size(); i++) {
		if (gameName_to_Id.at(i).first == game_name)
			return gameName_to_Id.at(i).second;
	}
	return -1;
}

string AverageKeyboardEnjoyer::nameOf(int subscription_id)
{
    for (int i = 0; i < gameName_to_Id.size(); i++) {
		if (gameName_to_Id.at(i).second == subscription_id)
			return gameName_to_Id.at(i).first;
	}
	return "No habla Español"; //TODO change this eventually
}

int AverageKeyboardEnjoyer::addToList(string game_name)
{
    gameName_to_Id.push_back(std::pair<string,int>(game_name,sub_id_counter));
	return sub_id_counter++;
}