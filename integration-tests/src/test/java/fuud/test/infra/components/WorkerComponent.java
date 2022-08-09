package fuud.test.infra.components;

import fuud.test.infra.ClassPathHelper;
import fuud.test.infra.EnvStarter;
import fuud.test.infra.Node;
import fuud.test.infra.PortAllocator;
import fuud.worker.service.WorkerServiceApplication;
import org.gridkit.nanocloud.VX;

import java.util.List;

public class WorkerComponent extends Component<WorkerComponent.Config> {
    @SuppressWarnings("unused")
    public static class Config extends BaseComponentConfig {
        public final int restPort;
        public final String link;

        public Config(PortAllocator portAllocator) {
            super(portAllocator);
            restPort = portAllocator.freePort();
            link = "http://localhost:" + restPort;
        }
    }

    public WorkerComponent(PortAllocator portAllocator) {
        super(new Config(portAllocator));
    }

    @Override
    public void start(EnvStarter.NodeProvider cloud, List<Component<?>> env) {
        Node workerNode = cloud.getNode("worker", config);
        workerNode.x(VX.CLASSPATH).inheritClasspath(false);
        ClassPathHelper.getClasspathForArtifact("worker-service")
                .forEach(classPathElement -> workerNode.x(VX.CLASSPATH).add(classPathElement));
        workerNode.x(VX.JVM).setEnv("server.port", config.restPort + "");

        workerNode.exec(() -> WorkerServiceApplication.main(new String[0]));
    }
}
