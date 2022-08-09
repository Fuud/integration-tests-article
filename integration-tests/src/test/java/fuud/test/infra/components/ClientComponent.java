package fuud.test.infra.components;

import fuud.client.service.ClientServiceApplication;
import fuud.test.infra.ClassPathHelper;
import fuud.test.infra.EnvStarter;
import fuud.test.infra.Node;
import fuud.test.infra.PortAllocator;
import org.gridkit.nanocloud.VX;

import java.util.List;

public class ClientComponent extends Component<ClientComponent.Config> {
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

    public ClientComponent(PortAllocator portAllocator) {
        super(new Config(portAllocator));
    }

    @Override
    public void start(EnvStarter.NodeProvider cloud, List<Component<?>> env) {
        Node clientNode = cloud.getNode("client", config);
        clientNode.x(VX.CLASSPATH).inheritClasspath(false);
        ClassPathHelper.getClasspathForArtifact("client-service")
                .forEach(classPathElement -> clientNode.x(VX.CLASSPATH).add(classPathElement));
        clientNode.x(VX.JVM).setEnv("server.port", config.restPort + "");

        WorkerComponent worker = findComponent(env, WorkerComponent.class);
        clientNode.x(VX.JVM).setEnv("client-service.worker-url", "http://localhost:" + worker.config.restPort);

        clientNode.exec(() -> ClientServiceApplication.main(new String[0]));
    }
}
