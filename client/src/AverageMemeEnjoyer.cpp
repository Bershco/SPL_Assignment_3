#include "../include/AverageMemeEnjoyer.h"
#include "../include/event.h"
#include <fstream>
#include <boost/algorithm/string.hpp>
#include <utility>
#include "AverageMemeEnjoyer.h"
#include <sstream>

using namespace std;

AverageMemeEnjoyer::AverageMemeEnjoyer() : eventByUser(), ch(), sub_id_to_game_name(), sub_id_counter(), receipt_id_to_msg(), closeSocketThread(false) {}

void AverageMemeEnjoyer::RunKeyboard()
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
				ch.sendFrameAscii(frame,'\0');
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
				if (!ch.sendFrameAscii(frame,'\0')) {
					std::cout << "Could not connect to server." << std::endl;
					ch.close();
				}
			}

			else if (words[0] == "join" && isConnected) {
				frame = buildFrameSubscribe(words[1]);
				ch.sendFrameAscii(frame,'\0');
			}

			else if (words[0] == "exit" && isConnected) {
				frame = buildFrameUnsubscribe(words[1]);
				ch.sendFrameAscii(frame,'\0');

			}
			else if (words[0] == "report" && isConnected) {
				frames = buildFramesSend(words[1]);
				while(!frames.empty()) {
					frame = frames.at(frames.size()-1);
					frames.pop_back();
					ch.sendFrameAscii(frame,'\0');
				}
			}

			else if (words[0] == "summary" && isConnected) {
				generateSummary(words[1],words[2],words[3]);
			}

			else if (!isConnected && (words[0] == "logout" || words[0] == "join" || words[0] == "exit" || words[0] == "report" || words[0] == "summary")) {
				std::cout << "You've tried to use the " + words[0] + " command without logging in first.\nPlease log in." << std::endl;
			}

			else if (isConnected && words[0] == "login") {
				std::cout << "The client is already logged in, log out before trying again." << std::endl;
			}

			else if (words[0] == "close_client") {
				std::cout << "Initiating self-destruction, in\n3" << std::endl;
				string line("idk how to log out yet"); //TODO check if logged in, and if so - send a disconnect frame, and wait for 'disconnected'
				if (ch.sendFrameAscii(line,'\0'))
					ch.close();
				if (isConnected) {
					frame = buildFrameDisconnect();
					ch.sendFrameAscii(frame,'\0');
					sleep(1);
					ch.close(); //this should be enough, not sure.
					closeSocketThread = true;
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
}

void AverageMemeEnjoyer::RunSocket()
{
	while (!closeSocketThread) {
		string answer;
		if (!ch.isConnected())
			continue;
		if (!ch.getFrameAscii(answer,'\0')) {
			//std::cout << "Socket not responding, retrying in 30 seconds\n" << std::endl;
			continue;
		} else std::cout << "the socket listener is listening" << std::endl;
		std::vector<string> split_answer;
		boost::split(split_answer,answer,boost::is_any_of("\n"));
		if (split_answer[0] == "CONNECTED")
			decodeFrameConnected();
		else if (split_answer[0] == "RECEIPT")
			decodeFrameReceipt(answer);
		else if (split_answer[0] == "ERROR") 
			decodeFrameError(answer);
		else if (split_answer[0] == "MESSAGE")
			decodeFrameMessage(answer);
	}
}
string AverageMemeEnjoyer::buildFrameConnect(string username, string password)
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

string AverageMemeEnjoyer::buildFrameDisconnect()
{
	string output = "DISCONNECT\nreceipt:" + std::to_string(sub_id_counter) + "\n\n\0";
	addReceipt(output);
	return output;
}

string AverageMemeEnjoyer::buildFrameSubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		sub_id = addToList(game_name);
	string output = "SUBSCRIBE\ndestination:/" + game_name + "\nid:" + std::to_string(sub_id) + "\nreceipt:" + std::to_string(sub_id_counter) + "\n\n\0";
	addReceipt(output);
	return output;
}

string AverageMemeEnjoyer::buildFrameUnsubscribe(string game_name)
{
	int sub_id = idOf(game_name);
	if (sub_id == -1)
		return ""; //TODO this needs to be an error or smth
	string output = "UNSUBSCRIBE\nid:" + std::to_string(sub_id) +"\nreceipt:" + std::to_string(sub_id_counter) + "\n\n\0";
    addReceipt(output);
	return output;
}

std::vector<string> AverageMemeEnjoyer::buildFramesSend(string file_path)
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

void AverageMemeEnjoyer::generateSummary(string game_name, string user, string file_path)
{
	std::queue<Event> events_reported_by_player = eventByUser[game_name];
	std::queue<Event> neededLater(events_reported_by_player); //Copying the queue for later use in this method
	std::map<string,string> general_stats;
	std::map<string,string> team_a_stats;
	std::map<string,string> team_b_stats;

	//The next lines are to insert all proper elements into all proper maps
	while (!events_reported_by_player.empty()) {
		Event e = events_reported_by_player.front(); //TODO might need to be something else
		events_reported_by_player.pop();
		std::map<string,string> gs = e.get_game_updates();
		std::map<string,string> tas = e.get_team_a_updates();
		std::map<string,string> tbs = e.get_team_b_updates();

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

	std::queue<pair<string,string>> sorted_general_stats = AverageMemeEnjoyer::order_lex(general_stats);
	std::queue<pair<string,string>> sorted_team_a_stats = AverageMemeEnjoyer::order_lex(team_a_stats);
	std::queue<pair<string,string>> sorted_team_b_stats = AverageMemeEnjoyer::order_lex(team_b_stats);
	

	std::vector<string> split;
	boost::split(split,game_name,boost::is_any_of("_"));
	string output(split[0] + " vs " + split[1] + "\nGame stats:\nGeneral stats:\n");
	while (!sorted_general_stats.empty()) {
		auto pair = sorted_general_stats.front();
		sorted_general_stats.pop();
		output += pair.first + ": " + pair.second + "\n";
	}
	while (!sorted_team_a_stats.empty()) {
		auto pair = sorted_team_a_stats.front();
		sorted_team_a_stats.pop();
		output += pair.first + ": " + pair.second + "\n";
	}
	while (!sorted_team_b_stats.empty()) {
		auto pair = sorted_team_a_stats.front();
		sorted_team_a_stats.pop();
		output += pair.first + ": " + pair.second + "\n";
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

int AverageMemeEnjoyer::idOf(string game_name)
{
	for (auto& pair : sub_id_to_game_name)
		if (pair.second == game_name)
			return pair.first;
	return -1;
}

string AverageMemeEnjoyer::nameOf(int subscription_id)
{
	for (auto& pair : sub_id_to_game_name) {
		if (pair.first == subscription_id)
			return pair.second;
	}
	return "";
}

int AverageMemeEnjoyer::addToList(string game_name)
{
    sub_id_to_game_name.insert(std::pair<int,string>(sub_id_counter,game_name));

    return sub_id_counter++;
}

void AverageMemeEnjoyer::addReceipt(string msg)
{
	receipt_id_to_msg.insert(make_pair(sub_id_counter++,msg));
}

string AverageMemeEnjoyer::getMsgFromReceipt(int)
{
    return string();
}

std::queue<pair<string,string>> AverageMemeEnjoyer::order_lex(std::map<string,string>& unsorted_map)
{
	std::queue<pair<string,string>> sorted_queue;
	while (!unsorted_map.empty()) {
		string min = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ"; //to make sure the first one checked is already the minimum
		for (auto& pair : unsorted_map) {
			if (std::lexicographical_compare(pair.first.begin(),pair.first.end(),min.begin(),min.end()))
				min = pair.first;
		}
		pair<string,string> addToSorted = make_pair(min,unsorted_map[min]);
		sorted_queue.push(addToSorted);
		unsorted_map.erase(min);
	}
    return sorted_queue;
}

ConnectionHandler& AverageMemeEnjoyer::getCH()
{
    return ch;
}

void AverageMemeEnjoyer::decodeFrameConnected()
{
	std::cout << "Login successful!" << std::endl;
}

void AverageMemeEnjoyer::decodeFrameReceipt(string receipt_frame)
{
	//needs to find out what frame I sent that got the receipt back, it's either disconnect, sub or unsub.
	std::vector<string> split;
	boost::split(split, receipt_frame, boost::is_any_of(":"));
	stringstream ss(split[1]);
	int receipt_id;
	ss >> receipt_id;
	string orig_msg = receipt_id_to_msg[receipt_id];
	std::vector<string> split_msg;
	boost::split(split_msg,orig_msg,boost::is_any_of("\n"));
	if (split_msg[0] == "SUBSCRIBE") {
		std::vector<string> split_dest_header;
		boost::split(split_dest_header,split_msg[1],boost::is_any_of("/"));
		std::cout << "Joined channel " + split_dest_header[1] << std::endl;
	}
	else if (split_msg[0] == "UNSUBSCRIBE") {
		std::vector<string> split_sub_id_header;
		boost::split(split_sub_id_header,split_msg[1],boost::is_any_of(":"));
		int unsub_id = stoi(split_sub_id_header[1]);
		std::cout << "Exited channel " + sub_id_to_game_name[unsub_id];
	}
	else if (split_msg[0] == "DISCONNECT") {
		std::cout << "Disconnection is working" <<std::endl;
		ch.close(); //the close method also edits the host string to "Not connected" properly.
	}
	else {
		std::cout << "Shouldn't have gotten here.." << std::endl;
	}
}

void AverageMemeEnjoyer::decodeFrameError(string error_frame)
{
	auto it = error_frame.find('\0');
	if (it != string::npos)
		error_frame.erase(it);
	std::cout << error_frame << std::endl;
	ch.close();
	//TODO this might need to be more than this, right now it just prints the error frame without the null char
}

void AverageMemeEnjoyer::decodeFrameMessage(string)
{

}