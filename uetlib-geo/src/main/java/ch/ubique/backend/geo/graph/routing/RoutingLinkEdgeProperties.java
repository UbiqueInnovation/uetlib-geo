package ch.ubique.backend.geo.graph.routing;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RoutingLinkEdgeProperties<E> {

    private final E originalProperties;

    private Long routingEdgeId;

    public RoutingLinkEdgeProperties(E originalProperties) {
        this(originalProperties, null);
    }

    public RoutingLinkEdgeProperties(E originalProperties, Long routingEdgeId) {
        this.originalProperties = originalProperties;
        this.routingEdgeId = routingEdgeId;
    }

    public E getOriginalProperties() {
        return originalProperties;
    }

    public Long getRoutingEdgeId() {
        return routingEdgeId;
    }

    public void setRoutingEdgeId(Long routingEdgeId) {
        this.routingEdgeId = routingEdgeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("originalProperties", originalProperties)
                .append("routingEdgeId", routingEdgeId)
                .toString();
    }
}
