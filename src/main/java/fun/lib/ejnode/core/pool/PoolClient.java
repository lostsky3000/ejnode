package fun.lib.ejnode.core.pool;

public abstract class PoolClient {

    public final long id;

    protected PoolClient(long id){
        this.id = id;
    }

    public abstract void close();

    public abstract void connect(long connTimeout);

    public abstract boolean isAlive();

    public interface ClientCreator{
        PoolClient create(long id, ClientPoolCore pool);
    }
}


