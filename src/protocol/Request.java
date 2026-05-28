package protocol;

import java.util.Map;

public class Request {
    private String command;
    private Map<String, Object> body;

    public Request() {
    }

    public Request(String command, Map<String, Object> body) {
        this.command = command;
        this.body = body;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }
}
