package fuud.test.infra.components;

import fuud.test.infra.ClassPathHelper;
import fuud.test.infra.Node;
import fuud.test.infra.PortAllocator;
import fuud.worker.service.WorkerServiceApplication;
import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.VX;
import org.gridkit.vicluster.ViProps;

import java.util.List;

public class WorkerComponent implements Component {
    public static class Config {
        final int restPort = PortAllocator.freePort();
    }

    public Config config = new Config();

    @Override
    public void start(Cloud cloud, List<Component> env) {
        Node workerNode = new Node(cloud, "worker");
        workerNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(workerNode).setLocalType();
        ClassPathHelper.getClasspathForArtifact("worker-service")
                .forEach(classPathElement -> workerNode.x(VX.CLASSPATH).add(classPathElement));
        workerNode.x(VX.JVM).setEnv("server.port", config.restPort + "");

        workerNode.exec(() -> WorkerServiceApplication.main(new String[0]));
    }
}
