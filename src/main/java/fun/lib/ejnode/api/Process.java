package fun.lib.ejnode.api;

import fun.lib.ejnode.core.ForkParamsBuilder;
import fun.lib.ejnode.core.ForkParams;

public abstract class Process {

    public final String name;

    protected Process(String name){
        this.name = name;
    }

    /**
     *
     * @return
     */
    public abstract long pid();

    /**
     *
     */
    public abstract void exit();

    /**
     *
     * @param clzEntry
     * @param params
     * @return
     */
    public abstract long fork(Class<? extends NodeEntry> clzEntry, ForkParams params);

    /**
     *
     * @param clzEntry
     * @return
     */
    public abstract long fork(Class<? extends NodeEntry> clzEntry);

    /**
     *
     * @param pid
     */
    public abstract void kill(long pid);

    /**
     *
     */
    public abstract void killOthers();

    /**
     *
     * @param pid
     * @param msg
     * @return
     */
    public abstract boolean send(long pid, Object msg);


    public abstract ForkParamsBuilder forkParamsBuilder();

}
