package fuud.test.infra.components;

import fuud.test.infra.ClassPathHelper;
import fuud.test.infra.EnvStarter;
import fuud.test.infra.Node;
import fuud.test.infra.PortAllocator;
import fuud.worker.service.WorkerServiceApplication;
import org.gridkit.nanocloud.VX;
import org.gridkit.vicluster.ViProps;

import java.util.List;

public class WorkerComponent extends Component<WorkerComponent.Config> {
    @SuppressWarnings("unused")
    public static class Config extends BaseComponentConfig {
        final int restPort = PortAllocator.freePort();
        private final String link = "http://localhost:" + restPort;
    }

    public WorkerComponent() {
        super(new Config());
    }

    @Override
    public void start(EnvStarter.NodeProvider cloud, List<Component<?>> env) {
        Node workerNode = cloud.getNode("worker", config);
        workerNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(workerNode).setLocalType();
        ClassPathHelper.getClasspathForArtifact("worker-service")
                .forEach(classPathElement -> workerNode.x(VX.CLASSPATH).add(classPathElement));
        workerNode.x(VX.JVM).setEnv("server.port", config.restPort + "");

        workerNode.exec(() -> WorkerServiceApplication.main(new String[0]));
    }
}
