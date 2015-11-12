/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jupiter.registry;

import org.jupiter.common.util.Maps;
import org.jupiter.rpc.UnresolvedAddress;

import java.util.concurrent.ConcurrentMap;

import static org.jupiter.common.util.Preconditions.checkArgument;
import static org.jupiter.registry.RegisterMeta.*;

/**
 * Default registry service.
 *
 * jupiter
 * org.jupiter.registry.jupiter
 *
 * @author jiachun.fjc
 */
public class DefaultRegistryService extends AbstractRegistryService {

    private final ConcurrentMap<UnresolvedAddress, ConfigClient> clients = Maps.newConcurrentHashMap();

    @Override
    protected void doSubscribe(ServiceMeta serviceMeta) {
        for (ConfigClient c : clients.values()) {
            c.doSubscribe(serviceMeta);
        }
    }

    @Override
    protected void doRegister(RegisterMeta meta) {
        for (ConfigClient c : clients.values()) {
            c.doRegister(meta);
        }
    }

    @Override
    protected void doUnregister(RegisterMeta meta) {
        for (ConfigClient c : clients.values()) {
            c.doUnregister(meta);
        }
    }

    @Override
    public void init(Object... args) {
        checkArgument(args.length >= 2, "need config server host and port");
        checkArgument(args[0] instanceof String, "args[0] must be a String with host");
        checkArgument(args[1] instanceof Integer, "args[1] must be a Integer with port");

        UnresolvedAddress address = new UnresolvedAddress((String) args[0], (Integer) args[1]);
        ConfigClient client = clients.get(address);
        if (client == null) {
            ConfigClient newClient = new ConfigClient(this);
            client = clients.putIfAbsent(address, newClient);
            if (client == null) {
                client = newClient;
                client.connect(address);
            }
        }
    }
}
