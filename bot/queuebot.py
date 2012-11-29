# Copyright (c) 2012, Robert Winslow Dalpe
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, 
#   this list of conditions and the following disclaimer.
#
# * Redistributions in binary form must reproduce the above copyright notice, 
#   this list of conditions and the following disclaimer in the documentation 
#   and/or other materials provided with the distribution.
#
# * The names of this software's contributors may not be used to endorse or 
#   promote products derived from this software without specific prior written 
#   permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import asynchat
import thread
from collections import deque
from socket import AF_INET, SOCK_STREAM
from bot.messageparser import MessageParser

m_lock = thread.allocate_lock()

class QueueBot(asynchat.async_chat):
    def __init__(self, nick, user, channel, su):
        self.nick = nick
        self.user = user
        self.channel = channel.lower()
        self.logged_in = False
        self.joined = False
        self.superuser = su.lower()
        
        self.parser = MessageParser(self)
        
        self.message_queue = deque()
        
        asynchat.async_chat.__init__(self)
        self.ibuffer = []
        self.create_socket(AF_INET, SOCK_STREAM)
        self.set_terminator('\r\n')
    def introduce(self):
        self.push('NICK %s' % self.nick)
        self.push('USER %s 0 * : %s' % (self.user, self.user))
    def join(self):
        self.push('JOIN %s' % self.channel)
    def collect_incoming_data(self, data):
        self.ibuffer.append(data)
    def found_terminator(self):
        buffed = "".join(self.ibuffer)
        print buffed
        self.parser.parse_message(buffed)
        self.ibuffer = []
    def push(self, data):
        data = data + self.get_terminator()
        print data
        asynchat.async_chat.push(self, data)
    
    def help(self, sender):
        help_strs = ["!ask QUESTION        Puts a question into the bot's message queue", \
                    "!count               Get the current size of the queue", \
                    "!get [X]             Removes 1 or X messages (if X is given) from the bot's queue and prints them on the channel", \
                    "!trim X              Reduces the bot's message queue to the X most recent messages", \
                    "!clear               Clears all messages from the queue", \
                    "!auto <off | N D>    Turns on/off the bot's auto mode. While in auto mode, the bot will print N messages from its queue every D seconds.", \
                    "!quit                The bot will finish sending queued messages and quit"] \
                    if sender == self.superuser else \
                    ["!ask QUESTION    Puts a question into the bot's message queue", \
                    "!count           Get the current size of the queue"]
        for help_str in help_strs:
            self.push("PRIVMSG %s :%s" % (sender, help_str))
    def quit(self, sender):
        if(sender == self.superuser):
            self.push("QUIT")
            self.close_when_done()
    def ask(self, sender, content):
        question = (sender, content)
        self.message_queue.append(question)
        self.push("PRIVMSG %s :Your question is %d in the queue" % (sender, len(self.message_queue)))
    def get(self, sender, count_str):
        m_lock.acquire()
        if(sender == self.superuser):
            try:
                count = int(count_str) if (len(count_str) > 0) else 1
                for i in range(count):
                    try:
                        question = self.message_queue.popleft()
                        self.push("PRIVMSG %s :%s asked: %s" % (self.superuser, question[0], question[1]))
                    except IndexError:
                        m_lock.release()
                        self.push("PRIVMSG %s :MESSAGE QUEUE EMPTY" % self.superuser)
                        return
            except:
                m_lock.release()
                return
        m_lock.release()
    def count(self, sender):
        self.push("PRIVMSG %s :There are %d messages in the queue" % (sender, len(self.message_queue)))
    def trim(self, sender, count_str):
        m_lock.acquire()
        if(sender == self.superuser):
            try:
                count = int(count_str) if (len(count_str) > 0) else -1
                if(count < 0 or count >= len(self.message_queue)):
                    m_lock.release()
                    return
                self.message_queue.rotate(count)
                nq = deque()
                for i in range(count):
                    nq.append(self.message_queue.popleft())
                self.message_queue = nq
            except:
                m_lock.release()
                return
        m_lock.release()