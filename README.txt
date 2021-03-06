This document describes:
General Usage
Command Line Parameters


GENERAL USAGE

QueueBot is an IRC bot which sits on a single server and maintains a message
queue. The bot responds to private messages from users which contain certain
commands. Users have access to the following commands:

* !ask QUESTION
Puts the user's question into the bot's message queue

* !count
Messages the user with the current size of the queue

* !help
Messages the user with the available commands

Additionally, the bot responds specially to a specific user, identified by
nickname only. This superuser has access to the following additional commands:

* !get [X]
Removes 1 or more messages from the bot's queue and prints them on the channel
and to the superuser in the form:
USERNICK asked: USERQUESTION
If the queue is empty, a single MESSAGE QUEUE EMPTY will be sent to the
superuser.

* !trim X
Reduces the bot's message queue to the X most recent messages

* !clear
Clears all messages from the queue

* !auto <off | N D>
Turns on/off the bot's auto mode. While in auto mode, the bot will print N
messages from its queue every D seconds, as if those messages had been
requested using !get. The bot does not send MESSAGE QUEUE EMPTY to the
superuser if it tries to automatically print messages and none exist.


COMMAND LINE PARAMETERS

QueueBot takes several command line parameters:
-s SERVER		Sets the server that the bot should connect to
-p PORT			Sets the port that the bot should connect on
-c CHANNEL		Sets the channel to join (should include the #)
-n NICK			Sets the bot's nickname
-su SUPERUSER	Sets the nick of the bot's superuser
-d				(OPTIONAL) Puts the bot into debug mode, which will cause the
				bot to produce verbose output