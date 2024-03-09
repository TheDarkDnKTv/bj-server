package thedarkdnktv.openbjs.core;

public interface IEventBus {

    void register(Object subscriber);

    void unregister(Object subscriber);

    void post(Object event);
}
