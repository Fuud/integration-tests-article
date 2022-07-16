package fuud.test.infra;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.vicluster.ViConfExtender;
import org.gridkit.vicluster.ViConfigurable;
import org.gridkit.vicluster.ViNode;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * Wrapper around ViNode, provides jdk8-friendly (lambda-friendly) methods exec(Runnable) and execAndReturn(Callable)
 */
@SuppressWarnings("unused")
public class Node implements ViConfigurable {

    private final ViNode node;

    public Node(Cloud cloud, String name) {
        node = cloud.node(name);
    }

    public void exec(SerializableRunnable runnable) {
        node.exec(runnable);
    }

    public <T> T execAndReturn(SerializableCallable<T> callable) {
        return node.exec(callable);
    }

    // ViConfigurable methods delegated to ViNode

    @Override
    public <X> X x(ViConfExtender<X> extender) {
        return node.x(extender);
    }

    @Override
    public void setProp(String propName, String value) {
        node.setProp(propName, value);
    }

    @Override
    public void setProps(Map<String, String> props) {
        node.setProps(props);
    }

    @Override
    public void setConfigElement(String key, Object value) {
        node.setConfigElement(key, value);
    }

    @Override
    public void setConfigElements(Map<String, Object> config) {
        node.setConfigElements(config);
    }

    public interface SerializableRunnable extends Runnable, Serializable {
        void run();
    }

    public interface SerializableCallable<T> extends Callable<T>, Serializable {
        T call();
    }


}
