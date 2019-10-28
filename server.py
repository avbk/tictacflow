#!/usr/bin/python3

from SimpleWebSocketServer import SimpleWebSocketServer, WebSocket

clients = []
class SimpleChat(WebSocket):

    def handleMessage(self):
       print(self.address, self.data)
       for client in clients:
          if client != self:
             client.sendMessage(self.data)

    def handleConnected(self):
       print(self.address, 'connected')
       clients.append(self)

    def handleClose(self):
       clients.remove(self)
       print(self.address, 'closed')

server = SimpleWebSocketServer('', 8000, SimpleChat)
server.serveforever()
