package kg.ergo.demo.batch.batch;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An object of HubUsage represents exactly one record of the hub_usage table.
 * Each record in hub_usage table is meant for a given {instance, metric}
 * combination.
 * This means that if DPX metering were to submit instance count and CUH
 * metrics, there must be 2 HubUsage records
 * for each instance being metered.
 */

@Entity
@Setter
@Getter
@Table(name = "hub_usage")
public class HubUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "resource_instance_id")
    private String resourceInstanceCRN;
    @Column(name = "instance_id")
    private String instanceId;
    @Column(name = "plan_id")
    private String planId;
    @Column(name = "region")
    private String region;
    @Column(name = "measure")
    private String measure;
    @Column(name = "quantity")
    private double quantity;
    @Column(name = "metric_status")
    private String metricStatus;
    @Column(name = "start_time")
    private OffsetDateTime metricsStartTime;
    @Column(name = "end_time")
    private OffsetDateTime metricsEndTime;
    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;
    @Column(name = "captured_at")
    private OffsetDateTime capturedAt;

    public HubUsage() {
        // defaut constructor
    }

    public HubUsage(HubUsage hubUsage) {
        resourceInstanceCRN = hubUsage.resourceInstanceCRN;
        instanceId = hubUsage.instanceId;
        planId = hubUsage.planId;
        region = hubUsage.region;
        instanceId = hubUsage.instanceId;
        metricStatus = hubUsage.metricStatus;
        metricsStartTime = hubUsage.metricsStartTime;
        metricsEndTime = hubUsage.metricsEndTime;
        submittedAt = hubUsage.submittedAt;
        capturedAt = hubUsage.capturedAt;
    }
}
