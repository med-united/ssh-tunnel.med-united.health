package health.medunited.service;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class MapAdapter extends XmlAdapter<MapElements[], Map<String, SSHConnection>> {

    public MapElements[] marshal(Map<String, SSHConnection> sshConnections) throws Exception {
        MapElements[] mapElements = new MapElements[sshConnections.size()];
        int i = 0;
        for (Map.Entry<String, SSHConnection> entry : sshConnections.entrySet())
            mapElements[i++] = new MapElements(entry.getKey(), entry.getValue());

        return mapElements;
    }

    public Map<String, SSHConnection> unmarshal(MapElements[] mapElements) throws Exception {
        Map<String, SSHConnection> r = new HashMap<>();
        for (MapElements mapelement : mapElements) {
            r.put(mapelement.key, mapelement.value);
        }
        return r;
    }
}
