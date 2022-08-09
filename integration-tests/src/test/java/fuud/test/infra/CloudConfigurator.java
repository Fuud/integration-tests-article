package fuud.test.infra;

import org.gridkit.nanocloud.Cloud;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.nanocloud.RemoteNode;
import org.gridkit.vicluster.ViConfigurable;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;

public class CloudConfigurator {
    private static PortAllocator portAllocator;

    public static ExecutionContext configureCloud(Cloud cloud) {
        ViNode allNodes = cloud.node("**");
        String host;
        if (Boolean.getBoolean("int.tests.remote")) {
            configureRemoteExecution(allNodes);
            host = System.getProperty("int.tests.remote.host");
        } else {
            ViProps.at(allNodes).setLocalType();
            host = "localhost";
        }

        return new ExecutionContext(getPortAllocator(), host);
    }

    private static synchronized PortAllocator getPortAllocator() {
        if (portAllocator == null) {
            if (Boolean.getBoolean("int.tests.remote")) {
                portAllocator = obtainPortAllocatorFromRemoteNode();
            } else {
                portAllocator = new PortAllocator.PortAllocatorImpl();
            }
        }
        return portAllocator;
    }

    private static PortAllocator obtainPortAllocatorFromRemoteNode() {
        Cloud serviceCloud = CloudFactory.createCloud();
        Node serviceNode = new Node(serviceCloud, "service-node");
        configureRemoteExecution(serviceNode);
        return serviceNode.execAndReturn(PortAllocator.PortAllocatorImpl::new);
    }

    private static void configureRemoteExecution(ViConfigurable allNodes) {
        RemoteNode remoteNodeConfig = allNodes.x(RemoteNode.REMOTE);
        remoteNodeConfig.setRemoteNodeType();
        remoteNodeConfig.setHostsConfigFile("?na"); // turn off host config file, configure all manually

        remoteNodeConfig.setRemoteAccount(System.getProperty("int.tests.remote.user"));
        remoteNodeConfig.setPassword(System.getProperty("int.tests.remote.password"));
//        remoteNodeConfig.setSshPrivateKey(System.getProperty("int.tests.remote.key.path"));
        remoteNodeConfig.setRemoteHost(System.getProperty("int.tests.remote.host"));
        remoteNodeConfig.setRemoteJarCachePath("nanocloud-cache"); // directory for uploaded jars
        remoteNodeConfig.setRemoteJavaExec(System.getProperty("int.tests.remote.java"));
    }

    public record ExecutionContext(PortAllocator portAllocator, String hostName) {
    }
}
