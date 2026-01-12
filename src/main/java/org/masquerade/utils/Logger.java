package org.masquerade.utils;
import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.*;

public class Logger {
    private String caller;

    public Logger(Class emitter){
        this.caller = emitter.getSimpleName();
    }

    public void print(Ansi message) {
        System.out.println(ansi().a("[" + this.caller + "] " + message).reset());
    }

    public void log(String message) {
        this.print(ansi().bg(Color.WHITE).a(" LOG ").reset().a(" " + message));
    }

    public void log(String message, Object raw) {
        this.log(message);
        System.out.println(raw);
    }

    public void info(String message) {
        this.print(ansi().bg(Color.GREEN).a(" INFO ").reset().fg(Color.GREEN).a(" " + message));
    }

    public void info(String message, Object raw) {
        this.info(message);
        System.out.println(raw);
    }

    public void warning(String message) {
        this.print(ansi().bg(Color.YELLOW).a(" WARNING ").reset().fg(Color.YELLOW).a(" " + message));
    }

    public void warning(String message, Object raw) {
        this.warning(message);
        System.out.println(raw);
    }

    public void error(String message) {
        this.print(ansi().bg(Color.RED).a(" ERROR ").reset().fg(Color.RED).a(" " + message));
    }

    public void error(String message, Object raw) {
        this.error(message);
        System.out.println(raw);
    }
}
