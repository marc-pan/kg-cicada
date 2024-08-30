package kg.ergo.demo.batch.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;

/**
 * updates archived metrics as 'archived'
 */
@Slf4j
public class UpdateArchiveStatus implements ItemWriter<HubUsage> {

    @Autowired
    private UsageRepository usageRepository;

    @Override
    public void write(@NonNull Chunk<? extends HubUsage> chunk) throws Exception {
        log.info("Start updating archive status");
        List<Integer> hubIds = new ArrayList<>();
        for (HubUsage hubUsage : chunk.getItems()) {
            hubIds.add(hubUsage.getId());
        }
        usageRepository.updateArchivedMetricsByIds(hubIds);
        log.info("Completed updating archive status");
    }

}
