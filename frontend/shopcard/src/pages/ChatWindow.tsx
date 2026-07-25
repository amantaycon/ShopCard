import React, { useState, useEffect, useRef } from 'react';
import { useAppSelector } from '../store';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

interface Message {
  id?: number;
  senderId: string;
  recipientId: string;
  content: string;
  timestamp?: string;
}

const ChatWindow: React.FC = () => {
  const { user } = useAppSelector((state) => state.auth);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  const recipientId = searchParams.get('recipientId') || '';
  const recipientName = searchParams.get('name') || 'Merchant';

  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const wsRef = useRef<WebSocket | null>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchHistory();
    connectWebSocket();

    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [recipientId]);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const fetchHistory = async () => {
    try {
      const res = await axiosClient.get('/chats/history', {
        params: { withUserId: recipientId }
      });
      setMessages(res.data);
    } catch (err) {
      console.error('Error fetching chat history', err);
    }
  };

  const connectWebSocket = () => {
    // Connect to WebSocket chat endpoint via API Gateway
    const wsUrl = `ws://localhost:8088/ws/chat/websocket`; // standard Spring websocket raw endpoint
    const socket = new WebSocket(wsUrl);
    wsRef.current = socket;

    socket.onopen = () => {
      console.log('Connected to Chat WebSocket Server');
      
      // Send STOMP-like connect frame or direct handshake
      const connectFrame = "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000";
      socket.send(connectFrame);

      // Subscribe to user queue messages: `/user/{userId}/queue/messages`
      const subscribeFrame = `SUBSCRIBE\nid:sub-0\ndestination:/user/${user?.userId}/queue/messages\n\n\u0000`;
      socket.send(subscribeFrame);
    };

    socket.onmessage = (event) => {
      const messageText = event.data;
      
      // Parse STOMP content from message body
      if (messageText.includes("MESSAGE")) {
        const bodyStart = messageText.indexOf("\n\n");
        if (bodyStart !== -1) {
          const body = messageText.substring(bodyStart + 2, messageText.length - 1);
          try {
            const parsedMsg: Message = JSON.parse(body);
            if (parsedMsg.senderId === recipientId) {
              setMessages((prev) => [...prev, parsedMsg]);
            }
          } catch (e) {
            console.error('Failed to parse websocket message', e);
          }
        }
      }
    };

    socket.onerror = (error) => {
      console.error('WebSocket Error', error);
    };

    socket.onclose = () => {
      console.log('WebSocket Connection Closed. Attempting reconnect...');
      // Reconnect fallback after 5s
      setTimeout(connectWebSocket, 5000);
    };
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || !user) return;

    const newMsg: Message = {
      senderId: user.userId,
      recipientId: recipientId,
      content: inputText,
      timestamp: new Date().toISOString()
    };

    // 1. Send via WebSocket if open
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      const sendFrame = `SEND\ndestination:/app/chat.send\ncontent-type:application/json\n\n${JSON.stringify(newMsg)}\u0000`;
      wsRef.current.send(sendFrame);
    } else {
      console.warn("WebSocket not connected. Unable to send live message.");
    }

    // 2. Optimistic local append
    setMessages((prev) => [...prev, newMsg]);
    setInputText('');
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-between text-slate-100">
      
      {/* Header bar */}
      <header className="bg-slate-900 border-b border-slate-800 px-6 py-4 flex items-center gap-4">
        <button
          onClick={() => navigate(-1)}
          className="p-1.5 bg-slate-800 hover:bg-slate-700 rounded-lg text-xs"
        >
          ⬅ Back
        </button>
        <div>
          <h2 className="text-sm font-bold text-white">{recipientName}</h2>
          <span className="text-[10px] text-emerald-400 font-semibold">• Live Online Chat</span>
        </div>
      </header>

      {/* Messages body */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4 max-h-[75vh]">
        {messages.map((msg, index) => {
          const isMe = msg.senderId === user?.userId;
          return (
            <div key={index} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[70%] rounded-2xl px-4 py-2.5 text-xs shadow-md ${
                isMe
                  ? 'bg-indigo-600 text-white rounded-tr-none'
                  : 'bg-slate-900 text-slate-200 rounded-tl-none border border-slate-800'
              }`}>
                <p>{msg.content}</p>
                <span className="block text-[8px] text-right mt-1.5 opacity-60">
                  {msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                </span>
              </div>
            </div>
          );
        })}
        <div ref={scrollRef}></div>
      </div>

      {/* Send form */}
      <form onSubmit={handleSendMessage} className="bg-slate-900 px-6 py-4 border-t border-slate-800 flex gap-4">
        <input
          type="text"
          placeholder="Type your message here..."
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-xs text-white focus:outline-none focus:border-indigo-500"
        />
        <button
          type="submit"
          className="px-5 py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold rounded-xl text-xs shadow-lg transition-colors"
        >
          Send
        </button>
      </form>
    </div>
  );
};

export default ChatWindow;
