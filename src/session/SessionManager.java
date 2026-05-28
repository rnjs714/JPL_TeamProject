package session;

import client.ClientContext;

import java.util.Stack;

public class SessionManager {
    private Stack<Session> sessions;
    private ClientContext context;
    private boolean running;

    public SessionManager(ClientContext context) {
        this.context = context;
        this.sessions = new Stack<>();
        this.running = true;
    }

    public void start(Session initialSession) {
        push(initialSession);
        while (running && !sessions.empty()) {
            sessions.peek().show(this);
        }
    }

    public void push(Session session) {
        sessions.push(session);
    }

    public void back() {
        if (!sessions.empty()) {
            sessions.pop();
        }
    }

    public void replace(Session session) {
        back();
        push(session);
    }

    public void stop() {
        this.running = false;
    }

    public ClientContext getContext() {
        return context;
    }
}
