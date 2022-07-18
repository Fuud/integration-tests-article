package fuud.test.infra.components;

import fuud.client.service.ClientServiceApplication;
import fuud.test.infra.ClassPathHelper;
import fuud.test.infra.Node;
import fuud.test.infra.PortAllocator;
import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.VX;
import org.gridkit.vicluster.ViProps;

import java.util.List;

public class ClientComponent extends Component<ClientComponent.Config> {
    @SuppressWarnings("unused")
    public static class Config {
        public final int restPort = PortAllocator.freePort();
        private final String link = "http://localhost:" + restPort;
    }

    public ClientComponent() {
        super(new Config());
    }

    @Override
    public void start(Cloud cloud, List<Component<?>> env) {
        Node clientNode = new Node(cloud, "client");
        clientNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(clientNode).setLocalType();
        ClassPathHelper.getClasspathForArtifact("client-service")
                .forEach(classPathElement -> clientNode.x(VX.CLASSPATH).add(classPathElement));
        clientNode.x(VX.JVM).setEnv("server.port", config.restPort + "");

        WorkerComponent worker = findComponent(env, WorkerComponent.class);
        clientNode.x(VX.JVM).setEnv("client-service.worker-url", "http://localhost:" + worker.config.restPort);

        clientNode.exec(() -> ClientServiceApplication.main(new String[0]));
    }
}
