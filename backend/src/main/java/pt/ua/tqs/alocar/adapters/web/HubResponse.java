package pt.ua.tqs.alocar.adapters.web;

import pt.ua.tqs.alocar.domain.Hub;

public record HubResponse(Long id, String name, String block, int capacity) {

    static HubResponse from(Hub hub) {
        return new HubResponse(hub.getId(), hub.getName(), hub.getBlock(), hub.getCapacity());
    }
}
