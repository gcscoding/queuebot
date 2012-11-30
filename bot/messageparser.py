# Copyright (c) 2012, Robert Winslow Dalpe
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
# * The names of this software's contributors may not be used to endorse or promote products derived from this software without specific prior written permission.
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


class MessageParser:
    def __init__(self, bot):
        self.bot = bot
    def parse_message(self, message):
        parts = message.split(' ')
        self.check_for_ping(message, parts)
        if(not self.bot.logged_in):
            self.check_for_login(parts)
        elif(not self.bot.joined):
            self.check_for_join(parts)
        else:
            self.check_for_command(parts)
    def check_for_ping(self, message, parts):
        if(parts[0] == 'PING'):
            self.bot.push(message.replace('PING', 'PONG'))
    def check_for_join(self, parts):
        uname = parts[0].split('!')[0][1:]
        if(uname == self.bot.nick):
            if(len(parts) >= 3 and parts[1] == 'JOIN' and parts[2].lower() == self.bot.channel):
                self.bot.joined = True
                print '>>>DEBUG: BOT JOINED %s' % self.bot.channel
    def check_for_login(self, parts):
        if(len(parts) >= 4 and (parts[1] == 'MODE' or parts[1] == '221') \
           and parts[2] == self.bot.nick and parts[3] == '+i'):
            self.bot.logged_in = True
            self.bot.join() 
    def check_for_command(self, parts):
        if(len(parts) >= 4 and parts[1] == 'PRIVMSG' and parts[2] == self.bot.nick):
            self.parse_command(parts[0], parts[3:])
    def parse_command(self, sender, message):
        first = (message[0])[1:]
        message = " ".join(message[1:])
        sender = sender.split('!')[0][1:]
        
        if first == '!help':
            self.bot.help(sender)
        elif first == '!quit':
            self.bot.quit(sender.lower())
        elif first == '!ask':
            self.bot.ask(sender, message)
        elif first == '!get':
            self.bot.get(sender.lower(), message)
        elif first == '!count':
            self.bot.count(sender)
        elif first == '!trim':
            self.bot.trim(sender.lower(), message)
        elif first == '!clear':
            self.bot.clear(sender.lower())
