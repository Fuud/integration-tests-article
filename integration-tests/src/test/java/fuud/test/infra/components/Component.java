package fuud.test.infra.components;

import org.gridkit.nanocloud.Cloud;

import java.util.List;

public interface Component {
    void start(Cloud cloud, List<Component> env);

    static <T> T findComponent(List<Component> env, Class<T> componentClass) {
        List<Component> found = env.stream().filter(componentClass::isInstance).toList();
        if (found.isEmpty()) {
            throw new IllegalStateException("Cannot find component " + componentClass);
        }
        if (found.size() > 1) {
            throw new IllegalStateException("There are more than one component " + componentClass);
        }
        return componentClass.cast(found.get(0));
    }
}
