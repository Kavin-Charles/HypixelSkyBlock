package net.swofty.velocity.gamemanager;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import net.swofty.commons.Configuration;
import net.swofty.commons.ServerType;
import net.swofty.commons.proxy.FromProxyChannels;
import net.swofty.velocity.SkyBlockVelocity;
import net.swofty.velocity.redis.RedisMessage;
import net.swofty.velocity.testflow.TestFlowManager;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameManager {
    public static final int SLEEP_TIME = 300;
    @Getter
    private static Map<ServerType, ArrayList<GameServer>> servers = new HashMap<>();

    public static GameServer addServer(ServerType type, UUID serverID, int port, int maxPlayers) {
        port = port == -1 ? getNextAvailablePort() : port;    // if port is -1 then get next available port
        RegisteredServer registeredServer = SkyBlockVelocity.getServer().registerServer(
                new ServerInfo(serverID.toString(), new InetSocketAddress(Configuration.get("host-name"), port))
        );

        String rootName = maxPlayers <= 20 ? "mini" : "mega";
        String shortenedRootName = maxPlayers <= 20 ? "m" : "M";

        String displayName = getNextAvailableDisplayName() + "" +
                Character.toUpperCase((char) (new Random().nextInt(26) + 'a'));

        GameServer server = new GameServer(
                rootName + displayName, shortenedRootName + displayName,
                serverID, registeredServer, maxPlayers
        );
        if (!servers.containsKey(type)) servers.put(type, new ArrayList<>(List.of(server)));
        else servers.get(type).add(server);

        return server;
    }

    public static boolean isAnyEmpty(ServerType type) {
        return servers.get(type).stream().anyMatch(gameServer -> gameServer.maxPlayers() > gameServer.registeredServer().getPlayersConnected().size());
    }

    public static @Nullable GameServer getFromRegisteredServer(RegisteredServer registeredServer) {
        for (ArrayList<GameServer> gameServers : servers.values()) {
            for (GameServer gameServer : gameServers) {
                if (gameServer.registeredServer().equals(registeredServer)) {
                    return gameServer;
                }
            }
        }
        return null;
    }

    public static @Nullable ServerType getTypeFromRegisteredServer(RegisteredServer registeredServer) {
        for (ServerType type : servers.keySet()) {
            for (GameServer gameServer : servers.get(type)) {
                if (gameServer.registeredServer().equals(registeredServer)) return type;
            }
        }
        return null;
    }

    public static @Nullable ServerType getTypeFromUUID(UUID uuid) {
        for (ServerType type : servers.keySet()) {
            for (GameServer gameServer : servers.get(type)) {
                if (gameServer.internalID().equals(uuid)) return type;
            }
        }
        return null;
    }

    public static boolean hasType(ServerType type) {
        return servers.containsKey(type) && !servers.get(type).isEmpty();
    }

    public static List<GameServer> getFromType(ServerType type) {
        return servers.get(type);
    }

    public static GameServer getFromUUID(UUID uuid) {
        for (ArrayList<GameServer> gameServers : servers.values()) {
            for (GameServer gameServer : gameServers) {
                if (gameServer.internalID().equals(uuid)) return gameServer;
            }
        }
        return null;
    }

    public static void loopServers(ProxyServer server) {
        server.getScheduler().buildTask(SkyBlockVelocity.getPlugin(), () -> {
            servers.forEach((serverType, registeredServers) -> {
                registeredServers.forEach(registeredServer -> {
                    RegisteredServer givenServer = registeredServer.registeredServer();
                    AtomicBoolean pingSuccess = new AtomicBoolean(false);
                    long startTime = System.currentTimeMillis();

                    RedisMessage.sendMessageToServer(registeredServer.internalID(),
                            FromProxyChannels.PING_SERVER, new JSONObject()
                    ).thenRun(() -> {
                        pingSuccess.set(true);
                    });

                    server.getScheduler().buildTask(SkyBlockVelocity.getPlugin(), () -> {
                        if (!pingSuccess.get()) {
                            System.out.println("Server " + givenServer.getServerInfo().getName() + " is offline! Removing from list...");
                            System.out.println("Ping was sent at " + startTime + " and was not received at " + System.currentTimeMillis() + " (" + (System.currentTimeMillis() - startTime) + "ms)");
                            servers.get(serverType).remove(registeredServer);

                            TestFlowManager.handleServerDisconnect(registeredServer.internalID);
                        }
                    }).delay(Duration.ofMillis(SLEEP_TIME)).schedule();
                });
            });
        }).repeat(Duration.ofMillis(SLEEP_TIME)).schedule();
    }

    private static int getNextAvailableDisplayName() {
        if (servers.isEmpty()) return 1;
        if (servers.values().stream().allMatch(ArrayList::isEmpty)) return 1;

        List<GameServer> gameServers = new ArrayList<>();
        servers.values().forEach(gameServers::addAll);

        int highestDisplayName = (gameServers.stream().mapToInt(server -> {
            String displayName = server.displayName().replaceAll("[^0-9]", "");
            return Integer.parseInt(displayName);
        }).max().getAsInt());
        return highestDisplayName + 1;
    }

    private static int getNextAvailablePort() {
        if (servers.isEmpty()) return 20000;
        if (servers.values().stream().allMatch(ArrayList::isEmpty)) return 20000;

        ArrayList<GameServer> gameServers = new ArrayList<>();
        servers.values().forEach(gameServers::addAll);

        int highestPort = gameServers.stream().mapToInt(gameServer -> gameServer.registeredServer().getServerInfo().getAddress().getPort()).max().getAsInt();
        return highestPort + 1;
    }

    public record GameServer(String displayName, String shortDisplayName,
                             UUID internalID, RegisteredServer registeredServer,
                             int maxPlayers) {
        public boolean hasEmptySlots() {
            return maxPlayers > registeredServer().getPlayersConnected().size();
        }
    }
}
