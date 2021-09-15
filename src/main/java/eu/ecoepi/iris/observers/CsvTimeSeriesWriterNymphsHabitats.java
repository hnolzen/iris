package eu.ecoepi.iris.observers;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import eu.ecoepi.iris.resources.TimeStep;
import eu.ecoepi.iris.components.*;

import java.io.PrintWriter;
import java.io.IOException;

@All({TickAbundance.class, Temperature.class, Humidity.class})
public class CsvTimeSeriesWriterNymphsHabitats extends IteratingSystem {

    ComponentMapper<TickAbundance> abundanceMapper;
    ComponentMapper<Habitat> habitatMapper;
    ComponentMapper<Temperature> temperatureMapper;
    ComponentMapper<Humidity> humidityMapper;

    private final PrintWriter csvWriter;

    private int nymphsAllHabitats;
    private int nymphsInfectedAllHabitats;
    private int nymphsForest;
    private int nymphsMeadow;
    private int nymphsEcotone;
    private int nymphsInfectedForest;
    private int nymphsInfectedMeadow;
    private int nymphsInfectedEcotone;

    private double dailyMeanTemperature;
    private double dailyMaxTemperature;
    private double dailyHumidity;

    @Wire
    TimeStep timeStep;

    public CsvTimeSeriesWriterNymphsHabitats(String path) throws IOException {
        csvWriter = new PrintWriter(path);
        csvWriter.print(
                "tick," +
                "questing_nymphs," +
                "questing_nymphs_infected," +
                "questing_nymphs_forest," +
                "questing_nymphs_forest_infected," +
                "questing_nymphs_meadow," +
                "questing_nymphs_meadow_infected," +
                "questing_nymphs_ecotone," +
                "questing_nymphs_ecotone_infected," +
                "mean_temperature," +
                "max_temperature," +
                "humidity" +
                "\n"
        );
    }

    @Override
    protected void process(int entityId) {
        var abundance = abundanceMapper.get(entityId);
        var habitat = habitatMapper.get(entityId);
        var temperature = temperatureMapper.get(entityId);
        var humidity = humidityMapper.get(entityId);

        nymphsAllHabitats += abundance.getQuestingNymphs();
        nymphsInfectedAllHabitats += abundance.getInfectedQuestingNymphs();

        if (habitat.getType() == Habitat.Type.WOOD) {
            nymphsForest += abundance.getQuestingNymphs();
            nymphsInfectedForest += abundance.getInfectedQuestingNymphs();
        }

        if (habitat.getType() == Habitat.Type.MEADOW) {
            nymphsMeadow += abundance.getQuestingNymphs();
            nymphsInfectedMeadow += abundance.getInfectedQuestingNymphs();
        }

        if (habitat.getType() == Habitat.Type.ECOTONE) {
            nymphsEcotone += abundance.getQuestingNymphs();
            nymphsInfectedEcotone += abundance.getInfectedQuestingNymphs();
        }

        dailyMeanTemperature = temperature.getMeanTemperature();
        dailyMaxTemperature = temperature.getMaxTemperature();
        dailyHumidity = humidity.getRelativeHumidity();
    }

    @Override
    protected void end() {

        csvWriter.format("%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",
                timeStep.getCurrent(),
                (double)nymphsAllHabitats,
                (double)nymphsInfectedAllHabitats,
                (double)nymphsForest,
                (double)nymphsInfectedForest,
                (double)nymphsMeadow,
                (double)nymphsInfectedMeadow,
                (double)nymphsEcotone,
                (double)nymphsInfectedEcotone,
                dailyMeanTemperature,
                dailyMaxTemperature,
                dailyHumidity
        );

        nymphsAllHabitats = 0;
        nymphsForest = 0;
        nymphsMeadow = 0;
        nymphsEcotone = 0;

        nymphsInfectedAllHabitats = 0;
        nymphsInfectedForest = 0;
        nymphsInfectedMeadow = 0;
        nymphsInfectedEcotone = 0;
    }

    @Override
    protected void dispose() {
        csvWriter.flush();
    }
}
