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
from socket import AF_INET, SOCK_STREAM

class QueueBot(asynchat.async_chat):
    def __init__(self, nick, user, channel):
        self.nick = nick
        self.user = user
        self.channel = channel
        asynchat.async_chat.__init__(self)
        self.ibuffer = []
        self.create_socket(AF_INET, SOCK_STREAM)
        self.set_terminator('\r\n')
    def introduce(self):
        self.push('NICK %s' % self.nick)
        self.push('USER %s 0 * : %s' % (self.user, self.user))
    def collect_incoming_data(self, data):
        self.ibuffer.append(data)
    def found_terminator(self):
        buffed = "".join(self.ibuffer)
        print buffed
        if(buffed.find('PING') >= 0):
            self.push(buffed.replace('PING', 'PONG'))
        self.ibuffer = []
    def push(self, data):
        data = data + self.get_terminator()
        print data
        asynchat.async_chat.push(self, data)