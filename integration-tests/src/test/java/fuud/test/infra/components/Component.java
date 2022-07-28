package fuud.test.infra.components;

import fuud.test.infra.EnvStarter;
import fuud.test.infra.PortAllocator;

import java.util.List;

public abstract class Component<TConfig extends Component.BaseComponentConfig> {
    protected final TConfig config;

    public Component(TConfig config) {
        this.config = config;
    }

    abstract public void start(EnvStarter.NodeProvider cloud, List<Component<?>> env);

    public TConfig getConfig() {
        return config;
    }

    static <T extends Component<?>> T findComponent(List<Component<?>> env, Class<T> componentClass) {
        List<Component<?>> found = env.stream().filter(componentClass::isInstance).toList();
        if (found.isEmpty()) {
            throw new IllegalStateException("Cannot find component " + componentClass);
        }
        if (found.size() > 1) {
            throw new IllegalStateException("There are more than one component " + componentClass);
        }
        return componentClass.cast(found.get(0));
    }

    @SuppressWarnings("unused")
    public static class BaseComponentConfig {
        public final int debugPort = PortAllocator.freePort();
        public final String debugLink = "Listening for transport dt_socket at address: " + debugPort + " ";
    }
}
