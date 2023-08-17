package batch.test.job;

import batch.test.chunk.FoodDataApiReader;
import batch.test.chunk.FoodDataApiWriter;
import batch.test.dto.FoodNutritionDto;
import batch.test.service.FoodDataApiService;
import batch.test.service.StoreFoodDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class FetchFoodDataJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final FoodDataApiService foodDataApiService;
    private final StoreFoodDataService storeFoodDataService;


    public Job updateFoodDataJob() {
        log.info(">>>>>>Job start");
        return jobBuilderFactory.get("updateFoodDataJob")
                .start(setTotalDataSizeStep())
                .next(storeFoodDataStep())
                .build();
    }

    @Bean
    @JobScope
    public Step setTotalDataSizeStep() {
        log.info(">>>>>>Step1 start");
        return stepBuilderFactory.get("checkTotalDataSizeStep")
                .tasklet((contribution, chunkContext) -> {
                    foodDataApiService.setTotalData();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    @JobScope
    public Step storeFoodDataStep() {
        log.info(">>>>>>Step2 start");
        return stepBuilderFactory.get("storeFoodDataStep")
                .<List<FoodNutritionDto>, List<FoodNutritionDto>>chunk(1)
                .reader(foodDataApiReader())
                .writer(foodDataApiWriter())
                .build();
    }

    @Bean
    @StepScope
    public FoodDataApiReader foodDataApiReader() {
        log.info(">>>>>>Reader start");
        return new FoodDataApiReader(foodDataApiService);
    }

    @Bean
    @StepScope
    public FoodDataApiWriter foodDataApiWriter() {
        log.info(">>>>>>Writer start");
        return new FoodDataApiWriter(storeFoodDataService);
    }

}