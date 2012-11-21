General Usage
=============

QueueBot is an IRC bot which sits on a single server and maintains a message
queue. The bot responds to private messages from users which contain certain
commands. Users have access to the following commands:

Command         | Description
--------------- | -----------
`!ask QUESTION` | Puts the user's question into the bot's message queue
`!count`        | Messages the user with the current size of the queue
`!help`         | Messages the user with the available commands

Additionally, the bot responds specially to a specific user, identified by
nickname only. This superuser has access to the following additional commands:

<table>
	<tr>
		<th>Command</th>
		<th>Description</th>
	</tr>
	<tr>
		<td>
				<code>!get [X]</code>
		</td>
		<td>
				Removes 1 or more messages from the bot's queue and prints them on the channel 
				and to the superuser in the form <code>USERNICK asked: USERQUESTION</code><br/>
					
				If the queue is empty, a single <code>MESSAGE QUEUE EMPTY</code> will be sent to
				the superuser.
		</td>
	</tr>
	<tr>
		<td>
				<code>!trim X</code>
		</td>
		<td>
				Reduces the bot's message queue to the <code>X</code> most recent messages
		</td>
	</tr>
	<tr>
		<td>
				<code>!clear</code>
		</td>
		<td>
				Clears all messages from the queue
		</td>
	</tr>
	<tr>
		<td>
				<code>!auto <off | N D></code>
		</td>
		<td>
				Turns on/off the bot's auto mode. While in auto mode, the bot will print 
				<code>N</code> messages from its queue every <code>D</code> seconds, as if those
				messages had been requested using <code>!get</code>. The bot does not send 
				<code>MESSAGE QUEUE EMPTY</code> to the superuser if it tries to automatically 
				print messages and none exist.
		</td>
	</tr>
</table>

Command Line Parameters
=======================

QueueBot takes several command line parameters:

Parameter       | Description
--------------- | -----------
`-s SERVER`     | Sets the server that the bot should connect to
`-p PORT`       | Sets the port that the bot should connect on
`-c CHANNEL`    | Sets the channel to join (should include the #)
`-n NICK`       | Sets the bot's nickname
`-su SUPERUSER` | Sets the nick of the bot's superuser
`-d`            | (OPTIONAL) Puts the bot into debug mode, which will cause the bot to produce verbose output

Licensing and Legal Information
===============================

Copyright (c) 2012 Robert Winslow Dalpe

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to deal in 
the Software without restriction, including without limitation the rights to 
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.