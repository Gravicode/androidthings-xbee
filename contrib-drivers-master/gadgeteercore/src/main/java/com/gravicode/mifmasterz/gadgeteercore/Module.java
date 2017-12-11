package com.gravicode.mifmasterz.gadgeteercore;

import java.util.HashMap;
public class Module{
    
}
/**
 * Created by mifmasterz on 7/20/17.
 */
/*
public abstract class Module {
    private HashMap<Integer, ISocket> providedSockets;

    public abstract String Name;
    public abstract String Manufacturer;

    public int RequiredSockets = 1;
    public int ProvidedSockets = this.providedSockets.size();

    protected Module() {
        this.providedSockets = new HashMap<Integer, ISocket>();
    }

    protected  void Initialize() {
        throw new InvalidModuleDefinitionException($"This module does not overload the proper {nameof(this.Initialize)} method.");
    }

    protected void Initialize(ISocket parentSocket) {
        throw new InvalidModuleDefinitionException($"This module does not overload the proper {nameof(this.Initialize)} method.");
    }

    protected void Initialize(params ISocket[] parentSockets) {
        throw new InvalidModuleDefinitionException($"This module does not overload the proper {nameof(this.Initialize)} method.");
    }

    protected Socket CreateSocket(int socketNumber) {
        var socket = new Socket(socketNumber);

        this.providedSockets.Add(socket.Number, socket);

        return socket;
    }

    public virtual void SetDebugLed(bool state) {
        throw new NotSupportedException();
    }

    public ISocket GetProvidedSocket(int socketNumber) {
        if (!this.providedSockets.ContainsKey(socketNumber))
            throw new ArgumentException("That socket does not exist.", nameof(socketNumber));

        return this.providedSockets[socketNumber];
    }

    public static <T extends  Module> CreateAsync(ISocket[] parentSockets){
        Module module = new T();

        if (module.RequiredSockets != parentSockets.Length)
            throw new ArgumentException($"Invalid number of sockets passed. Expected {module.RequiredSockets}.", nameof(parentSockets));

        if (module.RequiredSockets == 0) {
            await module.Initialize();
        }
        else if (module.RequiredSockets == 1) {
            await module.Initialize(parentSockets[0]);
        }
        else {
            await module.Initialize(parentSockets);
        }

        return module;
    }
}*/