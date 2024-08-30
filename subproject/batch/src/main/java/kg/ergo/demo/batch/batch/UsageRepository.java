package kg.ergo.demo.batch.batch;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface UsageRepository extends JpaRepository<HubUsage, String> {

    @Query("SELECT h FROM HubUsage h WHERE MONTH(h.capturedAt) = :month AND YEAR(h.capturedAt) = :year AND h.metricStatus = 'SUBMITTED'")
    Page<HubUsage> findSubmittedMetricsInSpecifiedMonth(@Param("year") Integer year, @Param("month") Integer month,
            Pageable pageable);

    @Modifying
    @Query("UPDATE HubUsage h SET h.metricStatus='ARCHIVED' WHERE h.id IN :ids")
    int updateArchivedMetricsByIds(@Param("ids") List<Integer> ids);

    @Modifying
    @Transactional
    @Query("DELETE FROM HubUsage h WHERE MONTH(h.capturedAt) = :month AND YEAR(h.capturedAt) = :year AND h.metricStatus = 'ARCHIVED'")
    int deleteSubmittedMetricsInSpecifiedsMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT h.metricsEndTime FROM HubUsage h WHERE h.metricStatus = 'MEASURED' ORDER BY h.metricsEndTime DESC LIMIT 1")
    OffsetDateTime findLastMeteredTime();

    @Query("SELECT h FROM HubUsage h "
            + "WHERE (h.metricStatus = 'MEASURED' OR h.metricStatus = 'SUBMISSION_FAILED')")
    Page<HubUsage> findMetricsToSubmit(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE HubUsage SET metricStatus = :metricStatus WHERE instanceId = :instanceId AND submittedAt = :submittedAt")
    int updateMetricsStatusByInstanceId(@Param("instanceId") String instanceId,
            @Param("metricStatus") String metricStatus, @Param("submittedAt") OffsetDateTime submittedAt);

}
